/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.data.graph.adapter.init;

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.AutoPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.ColumnMetadataAdapter;
import org.anyline.metadata.adapter.IndexMetadataAdapter;
import org.anyline.metadata.adapter.PrimaryMetadataAdapter;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class AbstractGraphAdapter extends AbstractDriverAdapter {

	public AbstractGraphAdapter(){
		super();
	}
	@Override
	public DatabaseType type() {
		return DatabaseType.COMMON;
	}

	@Override
	public boolean supportCatalog() {
		return true;
	}

	@Override
	public boolean supportSchema() {
		return true;
	}

	private static Map<Type, String> types = new HashMap<>();
	static {
		types.put(Table.TYPE.NORMAL, "BASE TABLE");
		types.put(Table.TYPE.VIEW, "VIEW");
		types.put(View.TYPE.NORMAL, "VIEW");
		types.put(BaseMetadata.TYPE.TABLE, "BASE TABLE");
		types.put(BaseMetadata.TYPE.VIEW, "VIEW");
	}
	@Override
	public String name(Type type){
		return types.get(type);
	}
	/**
	 * 验证运行环境与当前适配器是否匹配<br/>
	 * 默认不连接只根据连接参数<br/>
	 * 只有同一个库区分不同版本(如mmsql2000/mssql2005)或不同模式(如kingbase的oracle/pg模式)时才需要单独实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
	 * @return boolean
	 */
	@Override
	public boolean match(DataRuntime runtime, boolean compensate) {
		return super.match(runtime, compensate);
	}

	@Override
	public boolean match(String feature, List<String> keywords, boolean compensate) {
		return super.match(feature, keywords, compensate);
	}

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * UPDATE			: 更新
	 * SAVE				: 根据情况插入或更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													INSERT
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
	 * public String batchInsertSeparator()
	 * public boolean supportInsertPlaceholder()
	 * protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns)
	 * public String generatedKey()
	 * [命令执行]
	 * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);
	 ******************************************************************************************************************/

	/**
	 * insert [调用入口]<br/>
	 * 执行前根据主键生成器补充主键值, 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 需要插入入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
		return super.insert(runtime, random, batch, dest, data, configs, columns);
	}

	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 需要插入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns){
		Run run = null;
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceUtil.parseDest(null, obj, configs);
		}

		if(obj instanceof Collection){
			Collection list = (Collection) obj;
			if(!list.isEmpty()){
				run = createInsertRunFromCollection(runtime, batch, dest, list, configs, columns);
			}
			run.setRows(list.size());
		}else {
			run = createInsertRun(runtime, dest, obj, configs, columns);
			run.setRows(1);
		}
		convert(runtime, configs, run);
		return run;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns){
		super.fillInsertContent(runtime, run, dest, set, configs, columns);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns){
		StringBuilder builder = run.getBuilder();
		int batch = run.getBatch();
		if(null == builder){
			builder = new StringBuilder();
			run.setBuilder(builder);
		}
		checkName(runtime, null, dest);
		if(list instanceof DataSet){
			DataSet set = (DataSet) list;
			this.fillInsertContent(runtime, run, dest, set, configs, columns);
			return;
		}
		PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
		Object entity = list.iterator().next();
		List<String> pks = null;
		if(null != generator) {
			columns.putAll(EntityAdapterProxy.primaryKeys(entity.getClass()));
		}
		String head = insertHead(configs);
		builder.append(head);//
		name(runtime, builder, dest);// .append(parseTable(dest));
		builder.append("(");
		delimiter(builder, Column.names(columns));
		builder.append(") VALUES ");
		int dataSize = list.size();
		int idx = 0;
		if(batch > 1){
			//批量执行
			builder.append("(");
			int size = columns.size();
			run.setVol(size);
			for(int i=0; i<size; i++){
				if(i>0){
					builder.append(",");
				}
				builder.append("?");
			}
			builder.append(")");
		}
		for(Object obj:list){
            /*if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                if (row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())) {
                    createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                }
                insertValue(template, run, row, true, false, true, keys);
            }else{*/
			boolean create = EntityAdapterProxy.createPrimaryValue(obj, Column.names(columns));
			if(!create && null != generator){
				generator.create(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
				//createPrimaryValue(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
			}
			builder.append(insertValue(runtime, run, obj, true, true, false, true, columns));
			//}
			if(idx<dataSize-1 && batch <= 1){
				//多行数据之间的分隔符
				builder.append(batchInsertSeparator());
			}
			idx ++;
		}
		builder.append(insertFoot(configs, columns));
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 确认需要插入的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj  Entity或DataRow
	 * @param batch  是否批量，批量时不检测值是否为空
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch){
		return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 批量插入数据时, 多行数据之间分隔符
	 * @return String
	 */
	@Override
	public String batchInsertSeparator(){
		return ",";
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	@Override
	public boolean supportInsertPlaceholder(){
		return true;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 设置主键值
	 * @param obj obj
	 * @param value value
	 */
	@Override
	protected void setPrimaryValue(Object obj, Object value){
		super.setPrimaryValue(obj, value);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据entity创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns){
		Run run = new TableRun(runtime, dest);
		// List<Object> values = new ArrayList<Object>();
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(dest)){
			throw new org.anyline.exception.SQLException("未指定表");
		}

		checkName(runtime, null, dest);
		PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());

		int from = 1;
		StringBuilder valuesBuilder = new StringBuilder();
		DataRow row = null;
		if(obj instanceof Map){
			if(!(obj instanceof DataRow)) {
				obj = new DataRow((Map) obj);
			}
		}
		if(obj instanceof DataRow){
			row = (DataRow)obj;
			if(row.hasPrimaryKeys() && null != generator){
				generator.create(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
				//createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
			}
		}else{
			from = 2;
			boolean create = EntityAdapterProxy.createPrimaryValue(obj, columns);
			LinkedHashMap<String, Column> pks = EntityAdapterProxy.primaryKeys(obj.getClass());
			if(!create && null != generator){
				generator.create(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
				//createPrimaryValue(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
			}
		}
		run.setFrom(from);
		/*确定需要插入的列*/
		LinkedHashMap<String, Column> cols = confirmInsertColumns(runtime, dest, obj, configs, columns, false);
		if(null == cols || cols.size() == 0){
			throw new org.anyline.exception.SQLException("未指定列(DataRow或Entity中没有需要插入的属性值)["+obj.getClass().getName()+":"+BeanUtil.object2json(obj)+"]");
		}
		boolean replaceEmptyNull = false;
		if(obj instanceof DataRow){
			row = (DataRow)obj;
			replaceEmptyNull = row.isReplaceEmptyNull();
		}else{
			replaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
		}
		String head = insertHead(configs);
		builder.append(head);//.append(parseTable(dest));
		name(runtime, builder, dest);
		builder.append("(");
		valuesBuilder.append(") VALUES (");
		List<String> insertColumns = new ArrayList<>();
		boolean first = true;
		for(Column column:cols.values()){
			if(!first){
				builder.append(",");
				valuesBuilder.append(",");
			}
			first = false;
			String key = column.getName();
			Object value = null;
			if(!(obj instanceof Map) && EntityAdapterProxy.hasAdapter(obj.getClass())){
				value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
			}else{
				value = BeanUtil.getFieldValue(obj, key);
			}

			String str = null;
			if(value instanceof String){
				str = (String)value;
			}
			delimiter(builder, key);

			//if (str.startsWith("${") && str.endsWith("}")) {
			if (BasicUtil.checkEl(str)) {
				value = str.substring(2, str.length()-1);
				valuesBuilder.append(value);
			}else if(value instanceof SQL_BUILD_IN_VALUE){
				//内置函数值
				value = value(runtime, null, (SQL_BUILD_IN_VALUE)value);
				valuesBuilder.append(value);
			}else{
				insertColumns.add(key);
				if(supportInsertPlaceholder()) {
					valuesBuilder.append("?");
					if ("NULL".equals(value)) {
						value = null;
					}else if("".equals(value) && replaceEmptyNull){
						value = null;
					}
					addRunValue(runtime, run, Compare.EQUAL, column, value);
				}else{
					//format(valuesBuilder, value);
					valuesBuilder.append(write(runtime, null, value, false));
				}
			}
		}
		valuesBuilder.append(")");
		builder.append(valuesBuilder);
		builder.append(insertFoot(configs, cols));
		run.setBuilder(builder);
		run.setInsertColumns(insertColumns);
		return run;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 对象集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns){
		Run run = new TableRun(runtime, dest);
		run.setBatch(batch);
		if(null == list || list.isEmpty()){
			throw new org.anyline.exception.SQLException("空数据");
		}
		Object first = list.iterator().next();

		if(BasicUtil.isEmpty(dest)){
			throw new org.anyline.exception.SQLException("未指定表");
		}
		/*确定需要插入的列*/
		LinkedHashMap<String, Column> cols = confirmInsertColumns(runtime, dest, first, configs, columns, true);
		if(null == cols || cols.size() == 0){
			throw new org.anyline.exception.SQLException("未指定列(DataRow或Entity中没有需要插入的属性值)["+first.getClass().getName()+":"+BeanUtil.object2json(first)+"]");
		}
		run.setInsertColumns(cols);
		run.setVol(cols.size());
		fillInsertContent(runtime, run, dest, list, configs, cols);

		return run;
	}

	/**
	 * insert [after]<br/>
	 * 执行insert后返回自增主键的key
	 * @return String
	 */
	@Override
	public String generatedKey() {
		return super.generatedKey();
	}

	/**
	 * insert [命令执行]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks 需要返回的主键
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks){
		long cnt = 0;
		int batch = run.getBatch();
		String action = "insert";
		if(batch > 1){
			action = "batch insert";
		}
		if(!run.isValid()){
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)){
				log.warn("[valid:false][action:{}][table:{}][不具备执行条件]", action, run.getTableName());
			}
			return -1;
		}
		String sql = run.getFinalInsert();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备执行条件][action:{}][table:{}]", action, run.getTable());
			return -1;
		}
		if(null != configs){
			configs.add(run);
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
			if(batch > 1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)){
				log.info("{}[action:{}][table:{}][cmd:\n{}\n]\n[param size:{}]", random, action, run.getTable(), sql, values.size());
			}else {
				log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
		}
		long millis = -1;

		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return -1;
		}
		try {
			if(batch > 1){
			}else {
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}ms]{}", random, action, run.getTable(), millis, run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, sql, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = LogUtil.format(cnt, 34);
				if(batch > 1){
					qty = LogUtil.format("约"+cnt, 34);
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}ms][影响行数:{}]", random, action, run.getTable(), millis, qty);
			}

		}catch(Exception e){
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:{}][table:{}]{}", random, LogUtil.format("插入异常:", 33)+e, action, run.getTable(), run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(), e);
				ex.setCmd(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return cnt;
	}

	public String insertHead(ConfigStore configs){
		return "INSERT INTO ";
	}
	public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns){
		return "";
	}
	/* *****************************************************************************************************************
	 * 													UPDATE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns)
	 * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
	 * [命令执行]
	 * long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * UPDATE [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
		return super.update(runtime, random, batch, dest, data, configs, columns);
	}

	/**
	 * update [命令合成]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj Entity或DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns){
		return super.buildUpdateRun(runtime, batch, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, columns);
	}

	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return null;
	}

	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return null;
	}

	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
		return null;
	}

	/**
	 * update [命令合成-子流程]<br/>
	 * 确认需要更新的列
	 * @param row DataRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns){
		return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
	}
	@Override
	public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns){
		return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
	}

	/**
	 * update [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run){
		long result = 0;
		if(!run.isValid()){
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		String sql = null;
		sql = run.getFinalUpdate();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]", dest);
			return -1;
		}
		if(null != configs){
			configs.add(run);
		}
		List<Object> values = run.getValues();
		int batch = run.getBatch();
		String action = "update";
		if(batch > 1){
			action = "batch update";
		}
		long fr = System.currentTimeMillis();

		/*执行SQL*/
		if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
			if(batch > 1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)){
				log.info("{}[action:{}][table:{}]{}", random, action, run.getTable(), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}else {
				log.info("{}[action:update][table:{}]{}", random, run.getTable(), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
		}

		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return -1;
		}
		long millis = -1;
		try{

			if(batch > 1){
			}else {
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}ms]{}", random, action, run.getTable(), millis, run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, sql, values, null, true, result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = result+"";
				if(batch>1){
					qty = "约"+result;
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}ms][影响行数:{}]", random, action, run.getTable(), millis, LogUtil.format(qty, 34));
			}

		}catch(Exception e) {
			if (ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if (ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				SQLUpdateException ex = new SQLUpdateException("update异常:" + e.toString(), e);
				ex.setCmd(sql);
				ex.setValues(values);
				throw ex;
			}
			if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:update][table:{}]{}", random, run.getTable(), LogUtil.format("更新异常:", 33) + e.toString(), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}

		}
		return result;
	}

	/**
	 * save [调用入口]<br/>
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns, 长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
	 *
	 *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比, 删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns){
		return super.save(runtime, random, dest, data, configs, columns);
	}

	@Override
	protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns){
		return super.saveCollection(runtime, random, dest, data, configs, columns);
	}
	@Override
	protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns){
		return super.saveObject(runtime, random, dest, data, configs, columns);
	}
	@Override
	protected Boolean checkOverride(Object obj){
		return super.checkOverride(obj);
	}
	@Override
	protected Map<String, Object> checkPv(Object obj){
		return super.checkPv(obj);
	}

	/**
	 * 是否是可以接收数组类型的值
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param key key
	 * @return boolean
	 */
	@Override
	protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key){
		return super.isMultipleValue(runtime, run, key);
	}
	@Override
	protected boolean isMultipleValue(Column column){
		return super.isMultipleValue(column);
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.checkMetadata(runtime, table, configs, columns);
	}

	/* *****************************************************************************************************************
	 * 													QUERY
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)
	 * <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions)
	 * List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * [命令合成]
	 * Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)
	 * void fillQueryContent(DataRuntime runtime, Run run)
	 * String mergeFinalQuery(DataRuntime runtime, Run run)
	 * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
	 * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder)
	 * StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
	 * [命令执行]
	 * DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)
	 * List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 * Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) 
	 * DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)
	 * List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list)
	 ******************************************************************************************************************/

	/**
	 * query [调用入口]<br/>
	 * <br/>
	 * 返回DataSet中包含元数据信息，如果性能有要求换成maps
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.querys(runtime, random, prepare, configs, conditions);
	}

	/**
	 * query procedure [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi){
		DataSet set = null;
		final List<Parameter> inputs = procedure.getInputs();
		final List<Parameter> outputs = procedure.getOutputs();
		if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()){
			log.info("{}[action:procedure][cmd:\n{}\n][input param:{}]\n[output param:{}]", random, procedure.getName(), LogUtil.param(inputs), LogUtil.param(outputs));
		}
		final String rdm = random;
		long millis = -1;
		try{

			ACTION.SWITCH swt = InterceptorProxy.prepareQuery(runtime, random, procedure, navi);
			if(swt == ACTION.SWITCH.BREAK){
				return new DataSet();
			}
			swt = InterceptorProxy.beforeQuery(runtime, random, procedure, navi);
			if(swt == ACTION.SWITCH.BREAK){
				return new DataSet();
			}
			if(null != dmListener){
				dmListener.beforeQuery(runtime, random, procedure);
			}
			final DataRuntime rt = runtime;
			long fr = System.currentTimeMillis();
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
			if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[slow cmd][action:procedure][执行耗时:{}ms][cmd:\n{}\n][input param:{}]\n[output param:{}]"
							, random
							, millis
							, procedure.getName()
							, LogUtil.param(inputs)
							, LogUtil.param(outputs));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.PROCEDURE, null, procedure.getName(), inputs, outputs, true, set, millis);
					}
				}
			}
/*			if(null != queryInterceptor){
				queryInterceptor.after(procedure, set, millis);
			}*/
			if(!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()){
				log.info("{}[action:procedure][执行耗时:{}ms]", random, millis);
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(), e);
				throw ex;
			}else{
				if(ConfigTable.IS_LOG_SQL_WHEN_ERROR){
					log.error("{}[{}][action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]"
							, random
							, LogUtil.format("存储过程查询异常:", 33)+e.toString()
							, procedure.getName()
							, LogUtil.param(inputs)
							, LogUtil.param(outputs));
				}
			}
		}
		return set;
	}

	/**
	 * query [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param clazz 类
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return EntitySet
	 * @param <T> Entity
	 */
	@Override
	public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions){
		return super.selects(runtime, random, prepare, clazz, configs, conditions);
	}

	/**
	 * select [命令执行-子流程]<br/>
	 * DataRow转换成Entity
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param clazz entity class
	 * @param table table
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return EntitySet
	 * @param <T> entity.class
	 *
	 */
	@Override
	protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run){
		return super.select(runtime, random, clazz, table, configs, run);
	}

	/**
	 * query [调用入口]<br/>
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合, 结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.maps(runtime, random, prepare, configs, conditions);
	}

	/**
	 * select[命令合成]<br/> 最终可执行命令<br/>
	 * 创建查询SQL
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.buildQueryRun(runtime, prepare, configs, conditions);
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	@Override
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names){
		return super.buildQuerySequence(runtime, next, names);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillQueryContent(DataRuntime runtime, Run run){
		super.fillQueryContent(runtime, run);
	}
	@Override
	protected void fillQueryContent(DataRuntime runtime, XMLRun run){
		super.fillQueryContent(runtime, run);
	}
	@Override
	protected void fillQueryContent(DataRuntime runtime, TextRun run){
		super.fillQueryContent(runtime, run);
	}
	@Override
	protected void fillQueryContent(DataRuntime runtime, TableRun run){
		StringBuilder builder = run.getBuilder();
		TablePrepare sql = (TablePrepare)run.getPrepare();
		builder.append("SELECT ");
		if(null != sql.getDistinct()){
			builder.append(sql.getDistinct());
		}
		builder.append(BR_TAB);
		LinkedHashMap<String,Column> columns = sql.getColumns();
		if(null == columns || columns.isEmpty()){
			ConfigStore configs = run.getConfigs();
			if(null != configs) {
				List<String> cols = configs.columns();
				columns = new LinkedHashMap<>();
				for(String col:cols){
					columns.put(col.toUpperCase(), new Column(col));
				}
			}
		}
		if(null != columns && columns.size()>0){
			// 指定查询列
			boolean first = true;
			for(Column column:columns.values()){
				if(BasicUtil.isEmpty(column) || BasicUtil.isEmpty(column.getName())){
					continue;
				}
				if(!first){
					builder.append(",");
				}
				first = false;
				String name = column.getName();
				//if (column.startsWith("${") && column.endsWith("}")) {
				if (BasicUtil.checkEl(name)) {
					name = name.substring(2, name.length()-1);
					builder.append(column);
				}else{
					if(name.contains("(") || name.contains(",")){
						builder.append(name);
					}else if(name.toUpperCase().contains(" AS ")){
						int split = name.toUpperCase().indexOf(" AS ");
						String tmp = name.substring(0, split).trim();
						delimiter(builder, tmp);
						builder.append(" ");
						tmp = name.substring(split+4).trim();
						delimiter(builder, tmp);
					}else if("*".equals(name)){
						builder.append("*");
					}else{
						delimiter(builder, name);
					}
				}
			}
			builder.append(BR);
		}else{
			// 全部查询
			builder.append("*");
			builder.append(BR);
		}
		Table table = run.getTable();
		builder.append("FROM").append(BR_TAB);
		name(runtime, builder, table);
		String alias = table.getAlias();
		if(BasicUtil.isNotEmpty(alias)){
			builder.append(" ");
			delimiter(builder, alias);
		}
		builder.append(BR);
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(BR_TAB).append(join.getType().getCode()).append(" ");
				Table joinTable = join.getTable();
				String joinTableAlias = joinTable.getAlias();
				name(runtime, builder, joinTable);
				if(BasicUtil.isNotEmpty(joinTableAlias)){
					builder.append("  ");
					delimiter(builder, joinTableAlias);
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		//builder.append("\nWHERE 1=1\n\t");
		/*添加查询条件*/
		// appendConfigStore();
		run.appendCondition(this, true,false);
		run.appendGroup();
		run.appendOrderStore();
		run.checkValid();
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 合成最终 select 命令 包含分页 排序
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	@Override
	public String mergeFinalQuery(DataRuntime runtime, Run run) {
		return super.mergeFinalQuery(runtime, run);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 LIKE 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value 有占位符时返回占位值，没有占位符返回null
	 */
	@Override
	public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
		int code = compare.getCode();
		if(code > 100){
			builder.append(" NOT");
			code = code - 100;
		}
		// %A% 50
		// A%  51
		// %A  52
		// NOT %A% 150
		// NOT A%  151
		// NOT %A  152
		if(code == 50){
			builder.append(" LIKE ").append(concat(runtime, "'%'","?","'%'"));
		}else if(code == 51){
			builder.append(" LIKE ").append(concat(runtime, "?","'%'"));
		}else if(code == 52){
			builder.append(" LIKE ").append(concat(runtime, "'%'","?"));
		}
		RunValue run = new RunValue();
		run.setValue(value);
		return run;
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造 FIND_IN_SET 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	@Override
	public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) {
		return super.createConditionFindInSet(runtime, builder, column, compare, value, placeholder);
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造(NOT) IN 查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return builder
	 */
	@Override
	public StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
		if(compare == Compare.NOT_IN){
			builder.append(" NOT");
		}
		builder.append(" IN (");
		if(value instanceof Collection){
			Collection<Object> coll = (Collection)value;
			int size = coll.size();
			for(int i=0; i<size; i++){
				builder.append("?");
				if(i < size-1){
					builder.append(",");
				}
			}
			builder.append(")");
		}else{
			builder.append("= ?");
		}
		return builder;
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	@Override
	public DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
		if(run instanceof ProcedureRun){
			ProcedureRun pr = (ProcedureRun)run;
			return querys(runtime, random, pr.getProcedure(), configs.getPageNavi());
		}
		String cmd = run.getFinalQuery();
		if(BasicUtil.isEmpty(cmd)){
			return new DataSet().setTable(table);
		}
		List<Object> values = run.getValues();
		return select(runtime, random, system, ACTION.DML.SELECT, table, configs, run, cmd, values);
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return maps
	 */
	@Override
	public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run){
		List<Map<String, Object>> maps = null;
		if(null == random){
			random = random(runtime);
		}
		if(null != configs){
			configs.add(run);
		}
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		if(BasicUtil.isEmpty(sql)){
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw new SQLQueryException("未指定命令");
			}else{
				log.error("未指定命令");
				return new ArrayList<>();
			}
		}
		long fr = System.currentTimeMillis();
		if(log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)){
			log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return new ArrayList<>();
		}
		try{

			StreamHandler _handler = null;
			if(null != configs){
				_handler = configs.stream();
			}
			long[] count = new long[]{0};
			final boolean[] process = {false};
			final StreamHandler handler = _handler;
			final long[] mid = {System.currentTimeMillis()};
			if(null != handler){
				boolean keep = handler.keep();
				try {

				}finally {

				}

				maps = new ArrayList<>();
				//end stream handler
			}else {

			}
			boolean slow = false;
			if(ConfigStore.SLOW_SQL_MILLIS(configs) > 0){
				if(mid[0]-fr > ConfigStore.SLOW_SQL_MILLIS(configs)){
					slow = true;
					log.warn("{}[slow cmd][action:select][执行耗时:{}ms]{}", random, mid[0]-fr, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, maps, mid[0]-fr);
					}
				}
			}
			if(!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)){
				log.info("{}[action:select][执行耗时:{}ms]", random, mid[0] - fr);
			}
			maps = process(runtime, maps);
			if(!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)){
				log.info("{}[action:select][封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], count[0]);
			}
		}catch(Exception e){
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e.toString(), run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(), e);
				ex.setCmd(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return maps;
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return map
	 */
	@Override
	public Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		Map<String, Object> map = null;
		String sql = run.getFinalExists();
		List<Object> values = run.getValues();
		if(null != configs){
			configs.add(run);
		}
		long fr = System.currentTimeMillis();
		if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
			log.info("{}[action:select]{}", random, run.log(ACTION.DML.EXISTS, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		/*if(null != values && values.size()>0 && BasicUtil.isEmpty(true, values)){
			//>0:有占位 isEmpty:值为空
		}else{*/
		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return new HashMap<>();
		}
		try {

		}catch (Exception e) {
			if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw e;
			}
			if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
				if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
					e.printStackTrace();
				}
				log.error("{}[{}][action:select][cmd:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e, sql, LogUtil.param(values));
			}
		}
		//}
		Long millis = System.currentTimeMillis() - fr;
		boolean slow = false;
		long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
		if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)){
			if(millis > SLOW_SQL_MILLIS){
				slow = true;
				log.warn("{}[slow cmd][action:exists][执行耗时:{}ms][cmd:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
				if(null != dmListener){
					dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql, values, null, true, map, millis);
				}
			}
		}
		if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
			log.info("{}[action:select][执行耗时:{}ms][封装行数:{}]", random, millis, LogUtil.format(map == null ?0:1, 34));
		}
		return map;
	}

	/**
	 * select [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	@Override
	public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names){
		List<Run> runs = buildQuerySequence(runtime, next, names);
		if (null != runs && !runs.isEmpty()) {
			Run run = runs.get(0);
			if(!run.isValid()){
				if(ConfigTable.IS_LOG_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][sequence:"+names);
				}
				return new DataRow();
			}
			DataSet set = select(runtime, random, true, (Table)null, null, run);
			if (!set.isEmpty()) {
				return set.getRow(0);
			}
		}
		return new DataRow();
	}

	/**
	 * select [结果集封装-子流程]<br/>
	 * JDBC执行完成后的结果处理
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param list JDBC执行返回的结果集
	 * @return  maps
	 */
	@Override
	public List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list){
		return super.process(runtime, list);
	}

	/* *****************************************************************************************************************
	 * 													COUNT
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * [命令合成]
	 * String mergeFinalTotal(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long count(DataRuntime runtime, String random, Run run)
	 ******************************************************************************************************************/
	/**
	 * count [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.count(runtime, random, prepare, configs, conditions);
	}

	/**
	 * count [命令合成]<br/>
	 * 合成最终 select count 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	@Override
	public String mergeFinalTotal(DataRuntime runtime, Run run){
		//select * from user
		//select (select id from a) as a, id as b from (select * from suer) where a in (select a from b)
		String base = run.getBuilder().toString();
		StringBuilder builder = new StringBuilder();
		boolean simple= false;
		String upper = base.toUpperCase();
		if(upper.split("FROM").length == 2){
			//只有一个表
			//没有聚合 去重
			if(!upper.contains("DISTINCT") && !upper.contains("GROUP")){
				simple = true;
			}
		}
		if(simple){
			int idx = base.toUpperCase().indexOf("FROM");
			builder.append("SELECT COUNT(*) AS CNT FROM ").append(base.substring(idx+5));
		}else{
			builder.append("SELECT COUNT(*) AS CNT FROM (\n").append(base).append("\n) F");
		}
		String sql = builder.toString();
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND","WHERE ");
		return sql;
	}

	/**
	 * count [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, Run run){
		long total = 0;
		DataSet set = select(runtime, random, false, ACTION.DML.COUNT, null, null, run, run.getTotalQuery(), run.getValues());
		total = set.toUpperKey().getInt(0, "CNT", 0);
		return total;
	}

	/* *****************************************************************************************************************
	 * 													EXISTS
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * String mergeFinalExists(DataRuntime runtime, Run run)
	 ******************************************************************************************************************/

	/**
	 * exists [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return boolean
	 */
	@Override
	public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		boolean result = false;
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		Run run = buildQueryRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return false;
		}
		if(null != dmListener){
			dmListener.beforeExists(runtime, random, run);
		}
		long fr = System.currentTimeMillis();
		Map<String, Object> map = map(runtime, random, configs, run);
		if (null == map) {
			result = false;
		} else {
			result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
		}
		Long millis = System.currentTimeMillis() - fr;
		if(null != dmListener){
			dmListener.afterExists(runtime, random, run, true, result, millis);
		}
		return result;
	}
	@Override
	public String mergeFinalExists(DataRuntime runtime, Run run){
		String sql = "SELECT EXISTS(\n" + run.getBuilder().toString() +"\n)  IS_EXISTS";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND","WHERE ");
		return sql;
	}

	/* *****************************************************************************************************************
	 * 													EXECUTE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values)
	 * boolean execute(DataRuntime runtime, String random, Procedure procedure)
	 * [命令合成]
	 * Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * void fillExecuteContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) 
	 ******************************************************************************************************************/

	/**
	 * execute [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)  {
		return super.execute(runtime, random, prepare, configs, conditions);
	}

	@Override
	public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values){
		return super.execute(runtime, random, batch, configs, prepare, values);
	}

	/**
	 * procedure [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Procedure procedure){
		boolean result = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		List<Object> list = new ArrayList<Object>();
		final List<Parameter> inputs = procedure.getInputs();
		final List<Parameter> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
		String sql = " {";

		// 带有返回值
		int returnIndex = 0;
		if(procedure.hasReturn()){
			sql += "? = ";
			returnIndex = 1;
		}
		sql += "call " +procedure.getName()+"(";
		final int sizeIn = inputs.size();
		final int sizeOut = outputs.size();
		final int size = sizeIn + sizeOut;
		for(int i=0; i<size; i++){
			sql += "?";
			if(i < size-1){
				sql += ",";
			}
		}
		sql += ")}";

		if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()){
			log.info("{}[action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, sql, LogUtil.param(inputs), LogUtil.param(outputs));
		}
		long millis= -1;
		try{

			cmd_success = true;
			procedure.setResult(list);
			result = true;
			millis = System.currentTimeMillis() - fr;

			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
			if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[slow cmd][action:procedure][执行耗时:{}ms][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, millis, sql, LogUtil.param(inputs), LogUtil.param(list));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.PROCEDURE, null, sql, inputs, list, true, result, millis);
					}
				}
			}
			if (null != dmListener) {
				dmListener.afterExecute(runtime, random, procedure, result, millis);
			}
			if (!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:procedure][执行耗时:{}ms]\n[output param:{}]", random, millis, list);
			}

		}catch(Exception e){
			result = false;
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("execute异常:"+e.toString(), e);
				ex.setCmd(sql);
				throw ex;
			}else{
				if(ConfigTable.IS_LOG_SQL_WHEN_ERROR){
					log.error("{}[{}][action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, LogUtil.format("存储过程执行异常:", 33)+e.toString(), sql, LogUtil.param(inputs), LogUtil.param(outputs));
				}
			}
		}
		return result;
	}

	/**
	 * execute [命令合成]<br/>
	 * 创建执行SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.buildExecuteRun(runtime, prepare, configs, conditions);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, XMLRun run){
		super.fillExecuteContent(runtime, run);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, TextRun run){
		super.fillExecuteContent(runtime, run);
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, TableRun run){
		super.fillExecuteContent(runtime, run);
	}

	/**
	 * execute [命令合成-子流程]<br/>
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillExecuteContent(DataRuntime runtime, Run run){
		super.fillExecuteContent(runtime, run);
	}

	/**
	 * execute [命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		long result = -1;
		if(null == random){
			random = random(runtime);
		}
		String sql = run.getFinalExecute();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		int batch = run.getBatch();
		String action = "execute";
		if(batch > 1){
			action = "batch execute";
		}
		if(log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)){
			if(batch >1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
				log.info("{}[action:{}][cmd:\n{}\n]\n[param size:{}]", random, action, sql, values.size());
			}else {
				log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.EXECUTE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
		}
		if(null != configs){
			configs.add(run);
		}
		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return -1;
		}
		long millis = -1;
		try{

			if(batch>1){
			}else {
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][执行耗时:{}ms][cmd:\n{}\n]\n[param:{}]", random, action, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
				String qty = ""+result;
				if(batch>1){
					qty = "约"+result;
				}
				log.info("{}[action:{}][执行耗时:{}ms][影响行数:{}]", random, action, millis, LogUtil.format(qty, 34));
			}
		}catch(Exception e){
			if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:{}]{}", random, LogUtil.format("命令执行异常:", 33)+e, action, run.log(ACTION.DML.EXECUTE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				throw e;
			}

		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													DELETE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, Collection<T> values)
	 * long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, Object obj, String... columns)
	 * long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions)
	 * long truncate(DataRuntime runtime, String random, Table table)
	 * [命令合成]
	 * Run buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns)
	 * Run buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values)
	 * List<Run> buildTruncateRun(DataRuntime runtime, Table table)
	 * Run buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values)
	 * Run buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns)
	 * void fillDeleteRunContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long delete(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String key, Collection<T> values){
		return super.deletes(runtime, random, batch, table, configs, key, values);
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String... columns){
		return super.delete(runtime, random, dest, configs, obj, columns);
	}

	/**
	 * delete [调用入口]<br/>
	 * <br/>
	 * 根据configs和conditions过滤条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions){
		return super.delete(runtime, random, table, configs, conditions);
	}

	/**
	 * truncate [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	@Override
	public long truncate(DataRuntime runtime, String random, Table table){
		return super.truncate(runtime, random, table);
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, String ... columns){
		return super.buildDeleteRun(runtime, dest, configs, obj, columns);
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 根据属性解析出列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String key, Object values){
		return super.buildDeleteRun(runtime, batch, table, configs, key, values);
	}

	@Override
	public List<Run> buildTruncateRun(DataRuntime runtime, Table table){
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("TRUNCATE TABLE ");
		name(runtime, builder, table);
		return runs;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String key, Object values) {
		if(null == table || null == key || null == values){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		TableRun run = new TableRun(runtime, table);
		builder.append("DELETE FROM ");
		name(runtime, builder, table);
		builder.append(" WHERE ");

		if(values instanceof Collection){
			Collection cons = (Collection)values;
			delimiter(builder, key);
			if(batch >1){
				builder.append(" = ?");
				List<Object> list = null;
				if(values instanceof List){
					list = (List<Object>) values;
				}else{
					list = new ArrayList<>();
					for(Object item:cons){
						list.add(item);
					}
				}
				run.setValues(key, list);
				run.setVol(1);
				run.setBatch(batch);
			}else {
				if (cons.size() > 1) {
					builder.append(" IN(");
					int idx = 0;
					for (Object obj : cons) {
						if (idx > 0) {
							builder.append(",");
						}
						// builder.append("'").append(obj).append("'");
						builder.append("?");
						idx++;
					}
					builder.append(")");
				} else if (cons.size() == 1) {
					for (Object obj : cons) {
						builder.append("=?");
					}
				} else {
					throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
				}
				addRunValue(runtime, run, Compare.IN, new Column(key), values);
			}
		}else{
			delimiter(builder, key);
			builder.append("=?");
			addRunValue(runtime, run, Compare.EQUAL, new Column(key), values);
		}

		run.setBuilder(builder);

		return run;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String... columns) {
		TableRun run = new TableRun(runtime, table);
		run.setFrom(2);
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ");
		name(runtime, builder, table);
		builder.append(" WHERE ");
		List<String> keys = new ArrayList<>();
		if(null != columns && columns.length>0){
			for(String col:columns){
				keys.add(col);
			}
		}else{
			if(obj instanceof DataRow){
				keys = ((DataRow)obj).getPrimaryKeys();
			}else{
				keys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
			}
		}
		int size = keys.size();
		if(size >0){
			for(int i=0; i<size; i++){
				if(i > 0){
					builder.append("\nAND ");
				}
				String key = keys.get(i);

				delimiter(builder, key).append(" = ? ");
				Object value = null;
				if(obj instanceof DataRow){
					value = ((DataRow)obj).get(key);
				}else{
					if(EntityAdapterProxy.hasAdapter(obj.getClass())){
						value = BeanUtil.getFieldValue(obj,EntityAdapterProxy.field(obj.getClass(), key));
					}else{
						value = BeanUtil.getFieldValue(obj, key);
					}
				}
				addRunValue(runtime, run, Compare.EQUAL, new Column(key),value);
			}
		}else{
			throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
		}
		run.setBuilder(builder);

		return run;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillDeleteRunContent(DataRuntime runtime, Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				fillDeleteRunContent(runtime, r);
			}
		}
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	protected void fillDeleteRunContent(DataRuntime runtime, TableRun run){
		AutoPrepare prepare =  (AutoPrepare)run.getPrepare();
		StringBuilder builder = run.getBuilder();
		builder.append("DELETE FROM ");
		name(runtime, builder, run.getTable());
		builder.append(BR);
		if(BasicUtil.isNotEmpty(prepare.getAlias())){
			// builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(prepare.getAlias());
		}
		List<Join> joins = prepare.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(BR_TAB).append(join.getType().getCode()).append(" ");
				Table joinTable = join.getTable();
				String jionTableAlias = joinTable.getAlias();
				name(runtime, builder, joinTable);
				if(BasicUtil.isNotEmpty(jionTableAlias)){
					builder.append("  ").append(jionTableAlias);
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		//builder.append("\nWHERE 1=1\n\t");

		/*添加查询条件*/
		// appendConfigStore();
		run.appendCondition(this, true,false);
		run.appendGroup();
		run.appendOrderStore();
		run.checkValid();
	}

	/**
	 * delete[命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param configs 查询条件及相关设置
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run){
		return super.delete(runtime, random, configs, run);
	}
	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
	 * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
	 * Database database(DataRuntime runtime, String random, String name)
	 * Database database(DataRuntime runtime, String random)
	 * String String product(DataRuntime runtime, String random);
	 * String String version(DataRuntime runtime, String random);
	 * [命令合成]
	 * List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryProductRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)
	 * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase)
	 * String product(DataRuntime runtime, boolean create, Database product, DataSet set)
	 * String product(DataRuntime runtime, boolean create, String product)
	 * String version(DataRuntime runtime, int index, boolean create, String version, DataSet set)
	 * String version(DataRuntime runtime, boolean create, String version)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema)
	 ******************************************************************************************************************/
	/**
	 * database[调用入口]<br/>
	 * 当前数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Database
	 */
	@Override
	public Database database(DataRuntime runtime, String random){
		Catalog catalog = catalog(runtime, random);
		if(null != catalog){
			return new Database(catalog.getName());
		}
		return super.database(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String product(DataRuntime runtime, String random){
		return super.product(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	public String version(DataRuntime runtime, String random){
		return super.version(runtime, random);
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name){
		return super.databases(runtime, random, greedy, name);
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name){
		return super.databases(runtime, random, name);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库产品说明(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception {
		return super.buildQueryProductRun(runtime);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception {
		return super.buildQueryVersionRun(runtime);
	}

	/**
	 * database[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
		return super.buildQueryDatabasesRun(runtime, greedy, name);
	}

	/**
	 * database[结果集封装]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception {
		return super.databases(runtime, index, create, databases, set);
	}
	@Override
	public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set) throws Exception {
		return super.databases(runtime, index, create, databases, set);
	}


	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @param set 查询结果集
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, Database database, DataSet set) throws Exception {
		return super.database(runtime, index, create, database, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param database 上一步查询结果
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, boolean create, Database database) throws Exception {
		return super.database(runtime, create, database);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @param set 查询结果集
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, int index, boolean create, String product, DataSet set){
		return super.product(runtime, index, create, product, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, boolean create, String product){
		return product;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @param set 查询结果集
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, int index, boolean create, String version, DataSet set){
		return super.version(runtime, index, create, version, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, boolean create, String version){
		return version;
	}
	/* *****************************************************************************************************************
	 * 													catalog
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name)
	 * List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
	 * [命令合成]
	 * List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]<br/>
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog)
	 ******************************************************************************************************************/
	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name){
		return super.catalogs(runtime, random, name);
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name){
		return super.catalogs(runtime, random, greedy, name);
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
		return super.buildQueryCatalogsRun(runtime, greedy, name);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set) throws Exception {
		return super.catalogs(runtime, index, create, catalogs, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set) throws Exception {
		return super.catalogs(runtime, index, create, catalogs, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs) throws Exception {
		return super.catalogs(runtime, create, catalogs);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalogs 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs) throws Exception {
		return super.catalogs(runtime, create, catalogs);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 上一步查询结果
	 * @param set 查询结果集
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set) throws Exception {
		return super.catalog(runtime, index, create, catalog, set);
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 上一步查询结果
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog) throws Exception {
		if(null == catalog){
			Table table = new Table();
			checkSchema(runtime, table);
			catalog = table.getCatalog();
		}
		return catalog;
	}

	/* *****************************************************************************************************************
	 * 													schema
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name)
	 * List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name)
	 * [命令合成]
	 * List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set)
	 * List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema)
	 ******************************************************************************************************************/
	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name){
		return super.schemas(runtime, random, catalog, name);
	}

	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	@Override
	public List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name){
		return super.schemas(runtime, random, greedy, catalog, name);
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception {
		return super.buildQuerySchemasRun(runtime, greedy, catalog, name);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set) throws Exception {
		return super.schemas(runtime, index, create, schemas, set);
	}
	@Override
	public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set) throws Exception {
		return super.schemas(runtime, index, create, schemas, set);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @param set 查询结果集
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set) throws Exception {
		return super.schema(runtime, index, create, schema, set);
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception {
		if(null == schema){
			Table table = new Table();
			checkSchema(runtime, table);
			schema = table.getSchema();
		}
		return schema;
	}

	/**
	 * 检测name,name中可能包含catalog.schema.name<br/>
	 * 如果有一项或三项，在父类中解析<br/>
	 * 如果只有两项，需要根据不同数据库区分出最前一项是catalog还是schema，如果区分不出来的抛出异常
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表,视图等
	 * @return T
	 * @throws Exception 如果区分不出来的抛出异常
	 */
	public <T extends BaseMetadata> T checkName(DataRuntime runtime, String random, T meta) throws RuntimeException{
		if(null == meta){
			return null;
		}
		String name = meta.getName();
		if(null != name && name.contains(".")){
			String[] ks = name.split("\\.");
			if(ks.length == 3){
				meta.setCatalog(ks[0]);
				meta.setSchema(ks[1]);
				meta.setName(ks[2]);
			}else if(ks.length == 2){
				meta.setSchema(ks[0]);
				meta.setName(ks[1]);
			}else{
				throw new RuntimeException("无法实别schema或catalog(子类未" + this.getClass().getSimpleName() + "实现)");
			}
		}
		return meta;
	}
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Table table, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.tables(runtime, random, greedy, catalog, schema, pattern, types, struct);
	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	@Override
	protected void tableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema){
		super.tableMap(runtime, random, greedy, catalog, schema);
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.tables(runtime, random, catalog, schema, pattern, types, struct);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryTablesRun(runtime, greedy, catalog, schema, pattern, types);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryTablesCommentRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			T table = null;
			table = init(runtime, index, table, catalog, schema, row);
			table = detail(runtime, index, table, row);
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
	}

	/**
	 * table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception {
		if(null == tables){
			tables = new ArrayList<>();
		}
		for(DataRow row:set){
			T table = null;
			table = init(runtime, index, table, catalog, schema, row);
			if(null == table(tables, table.getCatalog(), table.getSchema(), table.getName())){
				tables.add(table);
			}
			detail(runtime, index, table, row);
		}
		return tables;
	}

	/**
	 *
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init){
		return super.ddl(runtime, random, table, init);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * table[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, table, ddls, set);
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index index
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return Table
	 * @param <T> Table
	 */
	public <T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row){
		return meta;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Table
	 */
	public <T extends Table> T detail(DataRuntime runtime, int index, T meta, DataRow row){
		return meta;
	}
	protected void init(Table table, ResultSet set, Map<String,Integer> keys){
	}
	protected void init(View view, ResultSet set, Map<String, Integer> keys){
	}

	/**
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, View view){
		return super.ddl(runtime, random, view);
	}

	/**
	 * view[命令合成]<br/>
	 * 查询view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view view
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception {
		return super.buildQueryDdlsRun(runtime, view);
	}

	/**
	 * view[结果集封装]<br/>
	 * 查询 view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param view view
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, view, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													VertexTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> vertexTables, DataSet set)
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> vertexTables, DataSet set)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> vertexTables, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * vertexTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.vertexTables(runtime, random, greedy, catalog, schema, pattern, types, struct);
	}

	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.vertexTables(runtime, random, catalog, schema, pattern, types, struct);
	}

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryVertexTablesRun(runtime, greedy, catalog, schema, pattern, types);
	}

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	public List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryVertexTablesCommentRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 *  根据查询结果集构造VertexTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryVertexTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
		return super.vertexTables(runtime, index, create, catalog, schema, tables, set);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 *  根据查询结果集构造VertexTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryVertexTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception {
		return super.vertexTables(runtime, index, create, catalog, schema, tables, set);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return vertexTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.vertexTables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return vertexTables
	 * @throws Exception 异常
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.vertexTables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return VertexTable
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row){
		return init(runtime, index, meta, catalog, schema, row);
	}
	/**
	 * vertexTable[结果集封装]<br/>
	 * 根据查询结果封装VertexTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return VertexTable
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, DataRow row){
		return detail(runtime, index, meta, row);
	}
	/**
	 *
	 * vertexTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, VertexTable meta, boolean init){
		return super.ddl(runtime, random, meta, init);
	}

	/**
	 * vertexTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * vertexTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param meta 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, VertexTable meta, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, meta, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													EdgeTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> edgeTables, DataSet set)
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> edgeTables, DataSet set)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends EdgeTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> edgeTables, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, EdgeTable edgeTable, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable edgeTable)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, EdgeTable edgeTable, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * edgeTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @param struct 是否查询表结构
	 * @return List
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.edgeTables(runtime, random, greedy, catalog, schema, pattern, types, struct);
	}

	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct){
		return super.edgeTables(runtime, random, catalog, schema, pattern, types, struct);
	}

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryEdgeTablesRun(runtime, greedy, catalog, schema, pattern, types);
	}

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return String
	 * @throws Exception Exception
	 */
	public List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryEdgeTablesCommentRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 *  根据查询结果集构造EdgeTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryEdgeTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
		return super.edgeTables(runtime, index, create, catalog, schema, tables, set);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 *  根据查询结果集构造EdgeTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryEdgeTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception {
		return super.edgeTables(runtime, index, create, catalog, schema, tables, set);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return edgeTables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.edgeTables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types BaseMetadata.TYPE.
	 * @return edgeTables
	 * @throws Exception 异常
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.edgeTables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row){
		return super.init(runtime, index, meta, catalog, schema, row);
	}
	/**
	 * edgeTable[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, DataRow row){
		return super.detail(runtime, index, meta, row);
	}
	/**
	 *
	 * edgeTable[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init){
		return super.ddl(runtime, random, meta, init);
	}

	/**
	 * edgeTable[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * edgeTable[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param meta 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, meta, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
	 * [命令合成]
	 * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 * master table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  BaseMetadata.TYPE.
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types){
		return super.masterTables(runtime, random, greedy, catalog, schema, pattern, types);
	}

	/**
	 * master table[命令合成]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryMasterTablesRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * master table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
		return super.masterTables(runtime, index, create, catalog, schema, tables, set);
	}

	/**
	 * master table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.masterTables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * master table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table MasterTable
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, MasterTable table){
		return super.ddl(runtime, random, table);
	}

	/**
	 * master table[命令合成]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table MasterTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * master table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, table, ddls, set);
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String pattern)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags)
	 * [结果集封装]<br/>
	 * <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * partition table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param master 主表
	 * @param pattern 名称统配符或正则
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern){
		return super.partitionTables(runtime, random, greedy, master, tags, pattern);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
		return super.buildQueryPartitionTablesRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @param name 名称统配符或正则
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String name) throws Exception {
		return super.buildQueryPartitionTablesRun(runtime, master, tags, name);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags) throws Exception {
		return super.buildQueryPartitionTablesRun(runtime, master, tags);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表=
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master) throws Exception {
		return super.buildQueryPartitionTablesRun(runtime, master);
	}

	/**
	 * partition table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
		return super.partitionTables(runtime, total, index, create, master, catalog, schema, tables, set);
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception {
		return super.partitionTables(runtime, create, tables, catalog, schema, master);
	}

	/**
	 * partition table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table){
		return super.ddl(runtime, random, table);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询 PartitionTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table) throws Exception {
		return super.buildQueryDdlsRun(runtime, table);
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, table, ddls, set);
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);
	 * <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table);
	 * [命令合成]
	 * List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	 * [结果集封装]<br/>
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
	 * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * column[调用入口]<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary){
		if (!greedy) {
			checkSchema(runtime, table);
		}
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();

		LinkedHashMap<String,T> columns = CacheProxy.columns(this, runtime.getKey(), table);
		if(null != columns && !columns.isEmpty()){
			return columns;
		}
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {

			int qty_total = 0;
			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充

			// 1.优先根据系统表查询
			try {
				List<Run> runs = buildQueryColumnsRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run);
						columns = columns(runtime, idx, true, table, columns, set);
						idx++;
					}
				}
				if(null != columns) {
					qty_dialect = columns.size();
					qty_total=columns.size();
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE){
					e.printStackTrace();
				}
				if(primary) {
					e.printStackTrace();
				} if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}

			// 方法(3)根据根据驱动内置接口补充

			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (null != columns || !columns.isEmpty()) {
					boolean exists = false;
					for(Column column:columns.values()){
						if(column.isPrimaryKey() != -1){
							exists = true;
							break;
						}
					}
					if(!exists){
						PrimaryKey pk = primary(runtime, random, false, table);
						if(null != pk){
							LinkedHashMap<String,Column> pks = pk.getColumns();
							if(null != pks){
								for(String k:pks.keySet()){
									Column column = columns.get(k);
									if(null != column){
										column.primary(true);
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("{}[columns][result:fail][table:{}][msg:{}]", random, table, e.toString());
			}
		}
		if(null != columns) {
			CacheProxy.columns(this, runtime.getKey(), table, columns);
		}else{
			columns = new LinkedHashMap<>();
		}
		int index = 0;
		for(Column column:columns.values()){
			if(null == column.getPosition() || -1 == column.getPosition()) {
				column.setPosition(index++);
			}
			if(column.isAutoIncrement() != 1){
				column.autoIncrement(false);
			}
			if(column.isPrimaryKey() != 1){
				column.setPrimary(false);
			}
			if(null == column.getTable() && !greedy){
				column.setTable(table);
			}
		}
		return columns;
	}

	/**
	 * column[调用入口]<br/>
	 * 查询列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 查询全部表时 输入null
	 * @return List
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table){
		return super.columns(runtime, random, greedy, catalog, schema, table);
	}

	/**
	 * column[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception {
		List<Run> runs = new ArrayList<>();
		return runs;
	}

	/**
	 * column[命令合成]<br/>(方法1)<br/>
	 * 查询多个表的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tables 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, List<Table> tables, boolean metadata) throws Exception {
		return super.buildQueryColumnsRun(runtime, tables, metadata);
	}
	/**
	 * column[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 查询结果集
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception {
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			T column = null;
			column = init(runtime, index, column, table, row);
			if(null != column) {
				column = detail(runtime, index, column, row);
				columns.put(column.getName().toUpperCase(), column);
			}
		}
		return columns;
	}
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception {
		if(null == columns){
			columns = new ArrayList<>();
		}
		for(DataRow row:set){
			T column = null;
			column = init(runtime, index, column, table, row);
			if(null == column(column, columns)){
				columns.add(column);
			}
			detail(runtime, index, column, row);
		}
		return columns;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 * 根据查询结果集构造Column,并分配到各自的表中
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<Table> tables, List<T> columns, DataSet set) throws Exception {
		if(null == columns){
			columns = new ArrayList<>();
		}
		Map<String,Table> tbls = new HashMap<>();
		for(Table table:tables){
			tbls.put(table.getName().toUpperCase(), table);
		}
		for(DataRow row:set){
			T column = null;
			column = init(runtime, index, column, null, row);
			if(null == column(column, columns)){
				columns.add(column);
			}
			detail(runtime, index, column, row);
			String tableName = column.getTableName();
			if(null != tableName){
				Table table = tbls.get(tableName.toUpperCase());
				if(null != table){
					table.addColumn(column);
				}
			}
		}
		return columns;
	}
	/**
	 * column[调用入口]<br/>(方法1)<br/>
	 * 查询多个表列，并分配到每个表中，需要保持所有表的catalog,schema相同
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 表
	 * @return List
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, List<Table> tables){
		return super.columns(runtime, random, greedy, catalog, schema, tables);
	}

	/**
	 * column [结果集封装-子流程](方法1)<br/>
	 * 方法(1)内部遍历
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 查询结果集
	 */
	public <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row){
		if(null == meta){
			meta = (T)new Column();
		}
		return meta;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Column
	 * @param <T> Column
	 */
	public <T extends Column> T detail(DataRuntime runtime, int index, T meta, DataRow row){

		return meta;
	}

	/**
	 * tag[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果集
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception {
		return super.tags(runtime, index, create, table, tags, set);
	}

	/**
	 *
	 * tag[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param pattern 名称统配符或正则
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception {
		return super.tags(runtime, create, tags, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
	 * [命令合成]
	 * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
	 * [结构集封装]
	 * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * primary[调用入口]<br/>
	 * 查询主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return PrimaryKey
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table){
		return super.primary(runtime, random, greedy, table);
	}

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception {
		return super.buildQueryPrimaryRun(runtime, table);
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set) throws Exception {
		PrimaryMetadataAdapter config = primaryMetadataAdapter(runtime);
		for(DataRow row:set){
			if(null == primary){
				primary = (T)new PrimaryKey();
				primary.setName(row.getString(config.getNameRefers()));
				if(null == table){
					table = new Table(row.getString(config.getCatalogRefers()), row.getString(config.getSchemaRefers()), row.getString(config.getTableRefer()));
				}
				primary.setTable(table);
			}
			String col = row.getString(config.getColumnRefers());
			if(BasicUtil.isEmpty(col)){
				throw new Exception("主键相关列名异常,请检查buildQueryPrimaryRun与primaryMetadataColumn");
			}
			Column column = primary.getColumn(col);
			if(null == column){
				column = new Column(col);
			}
			column.setTable(table);
			String position = row.getString(config.getColumnPositionRefers());
			primary.setPosition(column, BasicUtil.parseInt(position, 0));
			String order = row.getString(config.getColumnOrderRefers());
			if(BasicUtil.isNotEmpty(order)) {
				column.setOrder(order);
			}
			primary.addColumn(column);
		}
		return primary;
	}
	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T primary, Table table, DataSet set) throws Exception {
		return super.detail(runtime, index, primary, table, set);
	}

	/**
	 * primary[结构集封装-依据]<br/>
	 * 读取primary key元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return PrimaryMetadataAdapter
	 */
	@Override
	public PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime){
		return new PrimaryMetadataAdapter();
	}

	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, Table table) throws Exception {
		return super.primary(runtime, table);
	}
	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	 * [命令合成]
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception;
	 * [结构集封装]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 查询外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return PrimaryKey
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table){
		return super.foreigns(runtime, random, greedy,table);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception {
		return super.buildQueryForeignsRun(runtime, table);
	}

	/**
	 * foreign[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception {
		return super.foreigns(runtime, index, table, foreigns, set);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<T, Index> indexs(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)
	 * [结果集封装]<br/>
	 * <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set)
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set)
	 * <T extends Index> List<T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate)
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate)
	 ******************************************************************************************************************/
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
		return super.indexs(runtime, random, greedy, table, pattern);
	}

	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, Table table, String pattern){
		return super.indexs(runtime, random, table, pattern);
	}

	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param name 名称
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name){
		return super.buildQueryIndexesRun(runtime, table, name);
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set 查询结果集
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception {
		return super.indexs(runtime, index, create, table, indexs, set);
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set 查询结果集
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set) throws Exception {
		return super.indexs(runtime, index, create, table, indexs, set);
	}

	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param unique 是否唯一
	 * @param approximate 索引允许结果反映近似值
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate) throws Exception {
		return super.indexs(runtime, create, indexs, table, unique, approximate);
	}

	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param unique 是否唯一
	 * @param approximate 索引允许结果反映近似值
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception {
		DataSource datasource = null;
		Connection con = null;
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index基础属性(name,table,schema,catalog)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
		return super.init(runtime, index, meta, table, row);
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index更多属性(column,order, position)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
		return super.detail(runtime, index, meta, table, row);
	}
	/**
	 * index[结构集封装-依据]<br/>
	 * 读取index元数据结果集的依据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return IndexMetadataAdapter
	 */
	@Override
	public IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime){
		IndexMetadataAdapter adapter =  super.indexMetadataAdapter(runtime);
		adapter.setNameRefer("Index Name");
		adapter.setTableRefer("By Tag,By Edge");
		adapter.setColumnRefer("Columns");
		return adapter;
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
		List<T> constraints = null;
		if(null == table){
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		List<Run> runs = buildQueryConstraintsRun(runtime, table, null, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					constraints = constraints(runtime, idx, true, table, constraints, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return constraints;
	}

	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param column 列
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern){
		LinkedHashMap<String, T> constraints = null;
		if(null == table){
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
		checkSchema(runtime, table);
		List<Run> runs = buildQueryConstraintsRun(runtime, table, null, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					constraints = constraints(runtime, idx, true, table, column, constraints, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return constraints;
	}

	/**
	 * constraint[命令合成]<br/>
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param pattern 名称通配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE 1=1");
		String catalog = null;
		String schema = null;
		String tab = null;
		if(null != table){
			catalog = table.getCatalogName();
			schema = table.getSchemaName();
			tab = table.getName();
		}
		if(BasicUtil.isNotEmpty(catalog)){
			builder.append(" AND CONSTRAINT_CATALOG = '").append(catalog).append("'");
		}
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND CONSTRAINT_SCHEMA = '").append(schema).append("'");
		}
		if(BasicUtil.isNotEmpty(tab)){
			builder.append(" AND TABLE_NAME = '").append(tab).append("'");
		}
		return runs;
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception {
		return super.constraints(runtime, index, create, table, constraints, set);
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param column 列
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception {
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("CONSTRAINT_NAME");
			if(null == name){
				continue;
			}
			T constraint = constraints.get(name.toUpperCase());
			if(null == constraint && create){
				constraint = (T)new Constraint();
				constraints.put(name.toUpperCase(), constraint);
			};

			String catalog = row.getString("CONSTRAINT_CATALOG");
			String schema = row.getString("CONSTRAINT_SCHEMA");
			constraint.setCatalog(catalog);
			constraint.setSchema(schema);
			if(null == table){
				table = new Table(catalog, schema, row.getString("TABLE_NAME"));
			}
			constraint.setTable(table);
			constraint.setName(name);
			constraint.setType(row.getString("CONSTRAINT_TYPE"));

		}
		return constraints;
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
	 * [命令合成]
	 * List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
	 * [结果集封装]<br/>
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * trigger[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events){
		return super.triggers(runtime, random, greedy, table, events);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	public List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events){
		return super.buildQueryTriggersRun(runtime, table, events);
	}

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception {
		return super.triggers(runtime, index, create, table, triggers, set);
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);
	 ******************************************************************************************************************/
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern){
		List<T> procedures = new ArrayList<>();
		if(null == random){
			random = random(runtime);
		}

		if(null == catalog || null == schema || BasicUtil.isEmpty(catalog.getName()) || BasicUtil.isEmpty(schema.getName()) ){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog || BasicUtil.isEmpty(catalog.getName())){
				catalog = tmp.getCatalog();
			}
			if(null == schema || BasicUtil.isEmpty(schema.getName())){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryProceduresRun(runtime, catalog, schema, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					procedures = procedures(runtime, idx, true, procedures, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return procedures;
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern){
		LinkedHashMap<String,T> procedures = new LinkedHashMap<>();
		if(null == random){
			random = random(runtime);
		}

		if(null == catalog || null == schema || BasicUtil.isEmpty(catalog.getName()) || BasicUtil.isEmpty(schema.getName()) ){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog || BasicUtil.isEmpty(catalog.getName())){
				catalog = tmp.getCatalog();
			}
			if(null == schema || BasicUtil.isEmpty(schema.getName())){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryProceduresRun(runtime, catalog, schema, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					procedures = procedures(runtime, idx, true, procedures, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return procedures;
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		return super.buildQueryProceduresRun(runtime, catalog, schema, pattern);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception {
		return super.procedures(runtime, index, create, procedures, set);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures) throws Exception {
		return super.procedures(runtime, create, procedures);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception {
		return super.procedures(runtime, create, procedures);
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure){
		return super.ddl(runtime, random, procedure);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception {
		return super.buildQueryDdlsRun(runtime, procedure);
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param procedure Procedure
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, procedure, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Function function);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Function function) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		List<T> functions = new ArrayList<>();
		if(null == random){
			random = random(runtime);
		}

		if(null == catalog || null == schema || BasicUtil.isEmpty(catalog.getName()) || BasicUtil.isEmpty(schema.getName()) ){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog || BasicUtil.isEmpty(catalog.getName())){
				catalog = tmp.getCatalog();
			}
			if(null == schema || BasicUtil.isEmpty(schema.getName())){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryFunctionsRun(runtime, catalog, schema, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					functions = functions(runtime, idx, true, functions, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return functions;
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		LinkedHashMap<String,T> functions = new LinkedHashMap<>();
		if(null == random){
			random = random(runtime);
		}
		if(null == catalog || null == schema || BasicUtil.isEmpty(catalog.getName()) || BasicUtil.isEmpty(schema.getName()) ){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog || BasicUtil.isEmpty(catalog.getName())){
				catalog = tmp.getCatalog();
			}
			if(null == schema || BasicUtil.isEmpty(schema.getName())){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryFunctionsRun(runtime, catalog, schema, pattern);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					functions = functions(runtime, idx, true, functions, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return functions;
	}

	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param name 名称统配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String name) {
		return super.buildQueryFunctionsRun(runtime, catalog, schema, name);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception {
		return super.functions(runtime, index, create, functions, set);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception {
		return super.functions(runtime, index, create, functions, set);
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions) throws Exception {
		return super.functions(runtime, create, functions);
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Function
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Function meta){
		return super.ddl(runtime, random, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param function Function
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, function, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	 * [命令合成]
	 * List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence sequence) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		return super.sequences(runtime, random, greedy, catalog, schema, pattern);
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		return super.sequences(runtime, random, catalog, schema, pattern);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param name 名称统配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String name) {
		return super.buildQuerySequencesRun(runtime, catalog, schema, name);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception {
		return super.sequences(runtime, index, create, sequences, set);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception {
		return super.sequences(runtime, index, create, sequences, set);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param sequences 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences) throws Exception {
		return super.sequences(runtime, create, sequences);
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Sequence
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Sequence meta){
		return super.ddl(runtime, random, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询序列DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildQueryDdlsRun(runtime, meta);
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 查询 Sequence DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
	 * @param sequence Sequence
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, sequence, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													common
	 * ----------------------------------------------------------------------------------------------------------------
	 */
	/**
	 *
	 * 根据 catalog, schema, name检测tables集合中是否存在
	 * @param tables tables
	 * @param catalog catalog
	 * @param schema schema
	 * @param name name
	 * @return 如果存在则返回Table 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> T table(List<T> tables, Catalog catalog, Schema schema, String name){
		return super.table(tables, catalog, schema, name);
	}

	/**
	 *
	 * 根据 catalog, name检测schemas集合中是否存在
	 * @param schemas schemas
	 * @param catalog catalog
	 * @param name name
	 * @return 如果存在则返回 Schema 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name){
		return super.schema(schemas, catalog, name);
	}

	/**
	 *
	 * 根据 name检测catalogs集合中是否存在
	 * @param catalogs catalogs
	 * @param name name
	 * @return 如果存在则返回 Catalog 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Catalog> T catalog(List<T> catalogs, String name){
		return super.catalog(catalogs, name);
	}

	/**
	 *
	 * 根据 name检测databases集合中是否存在
	 * @param databases databases
	 * @param name name
	 * @return 如果存在则返回 Database 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Database> T database(List<T> databases, String name){
		return super.database(databases, name);
	}
	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	/**
	 * ddl [执行命令]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta BaseMetadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return boolean
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, BaseMetadata meta, ACTION.DDL action, Run run){
		if(null == run){
			return false;
		}
		boolean result = false;
		String sql = run.getFinalUpdate();
		meta.addDdl(sql);
		if(BasicUtil.isNotEmpty(sql)) {
			if(meta.execute()) {
				try {
					update(runtime, random, (Table) null, null, null, run);
				}finally {
					CacheProxy.clear();
				}
			}
			result = true;
		}
		return result;
	}
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Table meta)
	 * boolean alter(DataRuntime runtime, Table meta)
	 * boolean drop(DataRuntime runtime, Table meta)
	 * boolean rename(DataRuntime runtime, Table origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Table meta)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta)
	 * time runtime, StringBuilder builder, Table meta)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta)
	 * StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta)
	 * StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta)
	 ******************************************************************************************************************/
	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Table meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Table meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * table[调用入口]<br/>
	 * 重命名表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
	@Override
	public String keyword(BaseMetadata meta){
		return super.keyword(meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 创建表<br/>
	 * 关于创建主键的几个环节<br/>
	 * 1.1.定义列时 标识 primary(DataRuntime runtime, StringBuilder builder, Column column)<br/>
	 * 1.2.定义表时 标识 primary(DataRuntime runtime, StringBuilder builder, Table table)<br/>
	 * 1.3.定义完表DDL后，单独创建 primary(DataRuntime runtime, PrimaryKey primary)根据三选一情况调用buildCreateRun<br/>
	 * 2.单独创建 buildCreateRun(DataRuntime runtime, PrimaryKey primary)<br/>
	 * 其中1.x三选一 不要重复
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return runs
	 * @throws Exception
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE ").append(keyword(meta)).append(" ");
		checkTableExists(runtime, builder, false);
		name(runtime, builder, meta);
		//分区表
		partitionOf(runtime, builder, meta);
		partitionFor(runtime, builder, meta);
		body(runtime, builder, meta);
		//分区依据列(主表执行) PARTITION BY RANGE (code);
		partitionBy(runtime, builder, meta);
		//继承表CREATE TABLE simple.public.tab_1c1() INHERITS(simple.public.tab_parent)
		inherit(runtime, builder, meta);
		//引擎
		engine(runtime, builder, meta);
		//编码方式
		charset(runtime, builder, meta);
		//keys type
		keys(runtime, builder, meta);
		//分桶方式
		distribution(runtime, builder, meta);
		//物化视图
		materialize(runtime, builder, meta);
		//扩展属性
		property(runtime, builder, meta);
		//备注
		comment(runtime, builder, meta);

		//创建表后追加
		runs.addAll(buildAppendCommentRun(runtime, meta));
		runs.addAll(buildAppendColumnCommentRun(runtime, meta));
		runs.addAll(buildAppendPrimaryRun(runtime, meta));
		runs.addAll(buildAppendIndexRun(runtime, meta));
		return runs;
	}

	/**
	 * table[命令合成]<br/>
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param columns 列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns) throws Exception {
		return super.buildAlterRun(runtime, meta, columns);
	}

	/**
	 * table[命令合成]<br/>
	 * 重命名
	 * 子类实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP ").append(keyword(meta)).append(" ");
		checkTableExists(runtime, builder, true);
		name(runtime, builder, meta);
		return runs;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendColumnCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		builder.append(" IF ");
		if(!exists){
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	@Override
	public void checkPrimary(DataRuntime runtime, Table table){
		super.checkPrimary(runtime, table);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta){
		PrimaryKey primary = meta.getPrimaryKey();
		LinkedHashMap<String, Column> pks = null;
		if(null != primary){
			pks = primary.getColumns();
		}else{
			pks = meta.primarys();
		}
		if(!pks.isEmpty() && pks.size() >1){//单列主键时在列名上设置
			builder.append(",PRIMARY KEY (");
			boolean first = true;
			Column.sort(primary.getPositions(), pks);
			for(Column pk:pks.values()){
				if(!first){
					builder.append(",");
				}
				delimiter(builder, pk.getName());
				String order = pk.getOrder();
				if(BasicUtil.isNotEmpty(order)){
					builder.append(" ").append(order);
				}
				first = false;
			}
			builder.append(")");
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 engine
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta){
		String engine = meta.getEngine();
		if(BasicUtil.isNotEmpty(engine)){
			builder.append("\nENGINE = ").append(engine);
		}
		String params = meta.getEngineParameters();
		if(BasicUtil.isNotEmpty(params)){
			builder.append(" ").append(params);
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 body部分包含column index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta){
		LinkedHashMap<String, Column> columns = meta.getColumns();
		if(null == columns || columns.isEmpty()){
			if(BasicUtil.isEmpty(meta.getInherit())){
				//继承表没有列也需要() CREATE TABLE IF NOT EXISTS simple.public.tab_c2() INHERITS(simple.public.tab_parent)
				//分区表不需要 CREATE TABLE IF NOT EXISTS simple.public.LOG2 PARTITION OF simple.public.log_master FOR VALUES FROM (100) TO (199)
				return builder;
			}
		}
		builder.append("(");
		columns(runtime, builder, meta);
		indexs(runtime, builder, meta);
		builder.append(")");
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta){
		LinkedHashMap columMap = meta.getColumns();
		Collection<Column> columns = null;
		PrimaryKey primary = meta.getPrimaryKey();
		LinkedHashMap<String, Column> pks = null;
		if(null != primary){
			pks = primary.getColumns();
		}else{
			pks = meta.primarys();
			primary = new PrimaryKey();
			primary.setTable(meta);
			for (Column col:pks.values()){
				primary.addColumn(col);
			}
			meta.setPrimaryKey(primary);
		}
		if(null == pks){
			pks = new LinkedHashMap<>();
		}
		if(null != columMap){
			columns = columMap.values();
			if(null != columns && !columns.isEmpty()){
				//builder.append("(");
				int idx = 0;
				for(Column column:columns){
					TypeMetadata metadata = column.getTypeMetadata();
					if(null == metadata){
						metadata = typeMetadata(runtime, column);
						column.setTypeMetadata(metadata);
					}
					if(pks.containsKey(column.getName().toUpperCase())){
						column.setNullable(false);
					}
					builder.append("\n\t");
					if(idx > 0){
						builder.append(",");
					}
					column.setAction(ACTION.DDL.COLUMN_ADD);
					delimiter(builder, column.getName()).append(" ");
					define(runtime, builder, column);
					idx ++;
				}
				if(!pks.isEmpty()) {
					builder.append("\n\t");
					primary(runtime, builder, meta);
				}
				//builder.append("\n)");
			}
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 索引部分，与buildAppendIndexRun二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder indexs(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.indexs(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.charset(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 备注 创建表的完整DDL拼接COMMENT部分，与buildAppendCommentRun二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.comment(runtime, builder, meta);
	}
	
	/**
	 * table[命令合成-子流程]<br/>
	 * 数据模型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.keys(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 分桶方式
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.distribution(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 物化视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta){
		return super.materialize(runtime, builder, meta);
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 扩展属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta){
		if(null != meta.getProperty()){
			builder.append(" ").append(meta.getProperty());
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 主表设置分区依据(根据哪几列分区)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		// PARTITION BY RANGE (code); #根据code值分区
		Table.Partition partition = meta.getPartition();
		if(null == partition){
			return builder;
		}
		//只有主表需要执行
		if(null != meta.getMaster()){
			return builder;
		}
		Table.Partition.TYPE  type = partition.getType();
		if(null == type){
			return builder;
		}
		builder.append(" PARTITION BY ").append(type.name()).append("(");
		LinkedHashMap<String, Column> columns = partition.getColumns();
		if(null != columns) {
			delimiter(builder, Column.names(columns));
		}
		builder.append(")");
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表)<br/>
	 * 如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		//CREATE TABLE partition_name2 PARTITION OF main_table_name FOR VALUES FROM (100) TO (199);
		//CREATE TABLE emp_0 PARTITION OF emp FOR VALUES WITH (MODULUS 3,REMAINDER 0);
		//CREATE TABLE hr_user_1 PARTITION OF hr_user FOR VALUES IN ('HR');
		Table master = meta.getMaster();
		if(null == master){
			return builder;
		}
		builder.append(" PARTITION OF ");
		if(null == master){
			throw new SQLException("未提供 Master Table");
		}
		name(runtime, builder, master);
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(分区依据值)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		Table master = meta.getMaster();
		if(null == master){
			//只有子表才需要执行
			return builder;
		}
		Table.Partition partition = meta.getPartition();
		Table.Partition.TYPE type = null;
		if(null != partition){
			type = partition.getType();
		}
		if(null == type && null != master.getPartition()){
			type = master.getPartition().getType();
		}
		if(null == type){
			return builder;
		}
		builder.append(" FOR VALUES");
		if(type == Table.Partition.TYPE.LIST){
			List<Object> list = partition.getValues();
			if(null == list){
				throw new SQLException("未提供分区表枚举值(Partition.list)");
			}
			builder.append(" IN(");
			boolean first = true;
			for(Object item:list){
				if(!first){
					builder.append(",");
				}
				first = false;
				if(item instanceof Number){
					builder.append(item);
				}else{
					builder.append("'").append(item).append("'");
				}
			}
			builder.append(")");
		}else if(type == Table.Partition.TYPE.RANGE){
			Object from = partition.getMin();
			Object to = partition.getMax();
			if(BasicUtil.isEmpty(from) || BasicUtil.isEmpty(to)){
				throw new SQLException("未提供分区表范围值(Partition.from/to)");
			}
			builder.append(" FROM (");
			if(from instanceof Number){
				builder.append(from);
			}else{
				builder.append("'").append(from).append("'");
			}
			builder.append(")");

			builder.append(" TO (");
			if(to instanceof Number){
				builder.append(to);
			}else{
				builder.append("'").append(to).append("'");
			}
			builder.append(")");
		}else if(type == Table.Partition.TYPE.HASH){
			int modulus = partition.getModulus();
			if(modulus == 0){
				throw new SQLException("未提供分区表MODULUS");
			}
			builder.append(" WITH(MODULUS ").append(modulus).append(",REMAINDER ").append(partition.getRemainder()).append(")");
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 继承自table.getInherit
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		//继承表CREATE TABLE simple.public.tab_1c1() INHERITS(simple.public.tab_parent)
		if(BasicUtil.isNotEmpty(meta.getInherit())){
			LinkedHashMap<String, Column> columns = meta.getColumns();
			if(null == columns || columns.isEmpty()){
				// TODO body中已实现
				//继承关系中 子表如果没有新添加的列 需要空()
				//builder.append("()");
			}
			builder.append(" INHERITS(");
			name(runtime, builder, meta.getInherit());
			builder.append(")");
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, View meta)
	 * boolean alter(DataRuntime runtime, View meta)
	 * boolean drop(DataRuntime runtime, View meta)
	 * boolean rename(DataRuntime runtime, View origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, View meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, View meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, View meta)
	 * List<Run> buildDropRun(DataRuntime runtime, View meta)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View meta)
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta)
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, View meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, View meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, View meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * view[调用入口]<br/>
	 * 重命名视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 视图
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * view[命令合成]<br/>
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return Run
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, View meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		buildCreateRunHead(runtime, builder, meta);
		buildCreateRunOption(runtime, builder, meta);
		builder.append(" AS \n").append(meta.getDefinition());
		runs.addAll(buildAppendCommentRun(runtime, meta));
		return runs;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图头部
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		if (null == builder) {
			builder = new StringBuilder();
		}
		builder.append("CREATE VIEW ");
		name(runtime, builder, meta);
		return builder;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图选项
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		return super.buildCreateRunOption(runtime, builder, meta);
	}

	/**
	 * view[命令合成]<br/>
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, View meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER VIEW ");
		name(runtime,builder, meta);
		builder.append(" AS \n").append(meta.getDefinition());
		return runs;
	}

	/**
	 * view[命令合成]<br/>
	 * 重命名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * view[命令合成]<br/>
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, View meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP ").append(meta.getKeyword()).append(" ");
		checkViewExists(runtime,builder, true);
		name(runtime,builder, meta);
		return runs;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, View meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		builder.append(" IF ");
		if(!exists){
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
		return builder;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta){
		return super.comment(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													MasterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, MasterTable meta)
	 * boolean alter(DataRuntime runtime, MasterTable meta)
	 * boolean drop(DataRuntime runtime, MasterTable meta)
	 * boolean rename(DataRuntime runtime, MasterTable origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table)
	 * [命令合成-子流程]
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table)
	 ******************************************************************************************************************/

	/**
	 * master table[调用入口]<br/>
	 * 创建主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * master table[调用入口]<br/>
	 * 重命名主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * master table[命令合成]<br/>
	 * 创建主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * master table[命令合成]<br/>
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 *
	 ******************************************************************************************************************/

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * partition table[命令合成-]<br/>
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 分区表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * partition table[命令合成-子流程]<br/>
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Column meta)
	 * boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Column meta)
	 * boolean drop(DataRuntime runtime, Column meta)
	 * boolean rename(DataRuntime runtime, Column origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column)
	 * List<Run> buildRenameRun(DataRuntime runtime, Column column)
	 * [命令合成-子流程]
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Column column)
	 * String alterColumnKeyword(DataRuntime runtime)
	 * StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column)
	 * StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column, String type, int ignorePrecision, boolean ignoreScale)
	 * int ignorePrecision(DataRuntime runtime, Column column)
	 * int ignoreScale(DataRuntime runtime, Column column)
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype)
	 * int checkIgnoreScale(DataRuntime runtime, String datatype)
	 * StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * column[调用入口]<br/>
	 * 添加列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Column meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param trigger 修改异常时，是否触发监听器
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception {
		return super.alter(runtime, table, meta, trigger);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Column meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Column meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * column[调用入口]<br/>
	 * 重命名列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 列
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * column[命令合成]<br/>
	 * 添加列<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice(slice)) {
			Table table = meta.getTable(true);
			builder.append("ALTER ").append(keyword(table)).append(" ");
			name(runtime, builder, table);
		}
		// Column update = column.getUpdate();
		// if(null == update){
		// 添加列
		//builder.append(" ADD ").append(column.getKeyword()).append(" ");
		addColumnGuide(runtime, builder, meta);
		delimiter(builder, meta.getName()).append(" ");
		define(runtime, builder, meta);
		// }
		runs.addAll(buildAppendCommentRun(runtime, meta));
		return runs;
	}
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildAddRun(runtime, meta);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列<br/>
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Column update = meta.getUpdate();
		if(null != update){
			if(null == update.getTable(false)){
				update.setTable(meta.getTable(false));
			}
			// 修改列名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith(ConfigTable.ALTER_COLUMN_TYPE_SUFFIX)){
				runs.addAll(buildRenameRun(runtime, meta));
			}
			// 修改数据类型
			String type = type(runtime, null, meta).toString();
			String utype = type(runtime, null, update).toString();
			boolean exe = false;
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<Run> list = buildChangeTypeRun(runtime, meta);
				if(null != list){
					runs.addAll(list);
					exe = true;
				}
			}else{
				//数据类型没变但长度变了
				if(meta.getPrecision() != update.getPrecision() || meta.getScale() != update.getScale()){
					List<Run> list = buildChangeTypeRun(runtime, meta);
					if(null != list){
						runs.addAll(list);
						exe = true;
					}
				}
			}
			// 修改默认值
			Object def = meta.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				List<Run> defs = buildChangeDefaultRun(runtime, meta);
				if(null != defs){
					runs.addAll(defs);
				}
			}
			// 修改非空限制
			int nullable = meta.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				List<Run> nulls = buildChangeNullableRun(runtime, meta);
				if(null != nulls){
					runs.addAll(nulls);
				}
			}
			// 修改备注
			String comment = meta.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				List<Run> cmts = buildChangeCommentRun(runtime, meta);
				if(null != cmts){
					runs.addAll(cmts);
				}
			}
		}
		return runs;
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * column[命令合成]<br/>
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(meta instanceof Tag){
			Tag tag = (Tag)meta;
			return buildDropRun(runtime, tag);
		}
		if(!slice(slice)) {
			Table table = meta.getTable(true);
			builder.append("ALTER ").append(keyword(table)).append(" ");
			name(runtime, builder, table);
		}
		dropColumnGuide(runtime, builder, meta);
		delimiter(builder, meta.getName());
		return runs;
	}

	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildDropRun(runtime, meta);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" RENAME ").append(meta.getKeyword()).append(" ");
		delimiter(builder, meta.getName());
		builder.append(" ");
		name(runtime, builder, meta.getUpdate());
		meta.setName(meta.getUpdate().getName());
		return runs;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildChangeTypeRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	@Override
	public String alterColumnKeyword(DataRuntime runtime){
		return super.alterColumnKeyword(runtime);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列引导<br/>
	 * alter table sso_user [add column] type_code int
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
		builder.append(" ADD ").append(meta.getKeyword()).append(" ");
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 删除列引导<br/>
	 * alter table sso_user [drop column] type_code
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
		builder.append(" DROP ").append(meta.getKeyword()).append(" ");
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildChangeDefaultRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildChangeNullableRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta) throws Exception {
		return super.buildAppendCommentRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta) throws Exception {
		return super.buildDropAutoIncrement(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列，依次拼接下面几个属性注意不同数据库可能顺序不一样
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.define(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkColumnExists(runtime, builder, exists);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta){
		if(null == builder){
			builder = new StringBuilder();
		}
		int ignoreLength = -1;
		int ignorePrecision = -1;
		int ignoreScale = -1;
		String typeName = meta.getTypeName();
		TypeMetadata type = typeMetadata(runtime, meta);
		if(null != type){
			if(!type.support()){
				throw new RuntimeException("数据类型不支持:"+meta.getName() + " " + typeName);
			}
			typeName = type.getName();
		}
		ColumnMetadataAdapter adapter = columnMetadataAdapter(runtime, type);
		TypeMetadata.Config config = adapter.getTypeConfig();
		ignoreLength = config.ignoreLength();
		ignorePrecision = config.ignorePrecision();
		ignoreScale = config.ignoreScale();
		return type(runtime, builder, meta, typeName, ignoreLength, ignorePrecision, ignoreScale);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:聚合类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.aggregation(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param ignoreLength 是否忽略长度
	 * @param ignorePrecision 是否忽略有效位数
	 * @param ignoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale){
		return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.nullable(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.charset(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:默认值
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.defaultValue(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:定义列的主键标识(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.primary(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:递增列,需要通过serial实现递增的在type(DataRuntime runtime, StringBuilder builder, Column meta)中实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.increment(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.onupdate(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.position(runtime, builder, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta){
		return super.comment(runtime, builder, meta);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Tag meta)
	 * boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Tag meta)
	 * boolean drop(DataRuntime runtime, Tag meta)
	 * boolean rename(DataRuntime runtime, Tag origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)
	 * StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Tag meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @param trigger 修改异常时，是否触发监听器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception {
		return super.alter(runtime, table, meta, trigger);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * tag[调用入口]<br/>
	 * 重命名标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原标签
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		// Tag update = tag.getUpdate();
		// if(null == update){
		// 添加标签
		builder.append(" ADD TAG ");
		delimiter(builder, meta.getName()).append(" ");
		define(runtime, builder, meta);
		// }
		return runs;
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Tag meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Tag update = meta.getUpdate();
		if(null != update){
			// 修改标签名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith(ConfigTable.ALTER_COLUMN_TYPE_SUFFIX)){
				runs.addAll(buildRenameRun(runtime, meta));
			}
			meta.setName(uname);
			// 修改数据类型
			String type = type(runtime, null, meta).toString();
			String utype = type(runtime, null, update).toString();
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<Run> list = buildChangeTypeRun(runtime, meta);
				if(null != list){
					runs.addAll(list);
				}
			}else{
				//数据类型没变但长度变了
				if(meta.getPrecision() != update.getPrecision() || meta.getScale() != update.getScale()){
					List<Run> list = buildChangeTypeRun(runtime, meta);
					if(null != list){
						runs.addAll(list);
					}
				}
			}
			// 修改默认值
			Object def = meta.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				runs.addAll(buildChangeDefaultRun(runtime, meta));
			}
			// 修改非空限制
			int nullable = meta.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				runs.addAll(buildChangeNullableRun(runtime, meta));
			}
			// 修改备注
			String comment = meta.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				runs.addAll(buildChangeCommentRun(runtime, meta));
			}
		}

		return runs;
	}

	/**
	 * tag[命令合成]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Tag meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" DROP ").append(meta.getKeyword()).append(" ");
		delimiter(builder, meta.getName());
		return runs;
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" RENAME ").append(meta.getKeyword()).append(" ");
		delimiter(builder, meta.getName());
		builder.append(" ");
		name(runtime, builder, meta.getUpdate());
		return runs;
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta) throws Exception {
		return super.buildChangeDefaultRun(runtime, meta);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta) throws Exception {
		return super.buildChangeNullableRun(runtime, meta);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta) throws Exception {
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta) throws Exception {
		return super.buildChangeTypeRun(runtime, meta);
	}

	/**
	 * tag[命令合成]<br/>
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkTagExists(runtime, builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, Table table, PrimaryKey meta)
	 * boolean drop(DataRuntime runtime, PrimaryKey meta)
	 * boolean rename(DataRuntime runtime, PrimaryKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary)
	 ******************************************************************************************************************/

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception {
		return super.alter(runtime, table, origin, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 主键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		return super.buildAddRun(runtime, meta, slice);
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(null != meta) {//没有新主键的就不执行了
			Table table = null;
			if(null != meta){
				table = meta.getTable();
			}else{
				table = origin.getTable();
			}
			List<Run> slices = new ArrayList<>();
			if (null != origin) {
				slices.addAll(buildDropRun(runtime, origin, true));
			}
			if (null != meta && !meta.isDrop()) {
				slices.addAll(buildAddRun(runtime, meta, true));
			}
			if(slice(true)) {
				if (!slices.isEmpty()) {
					Run run = new SimpleRun(runtime);
					StringBuilder builder = run.getBuilder();
					builder.append("ALTER TABLE ");
					name(runtime, builder, table);
					boolean first = true;
					for (Run item : slices) {
						if (item.getBuilder().length() == 0) {
							continue;
						}
						if (!first) {
							builder.append(",");
						}
						builder.append(item.getBuilder());
						first = false;
					}
					if (!first) {//非空
						runs.add(run);
					}
				}
			}else{
				runs.addAll(slices);
			}
		}
		return runs;
	}

	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice(slice)) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, meta.getTable(true));
		}
		builder.append(" DROP CONSTRAINT ");
		delimiter(builder, meta.getName());
		return runs;
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, Table table, ForeignKey meta)
	 * boolean drop(DataRuntime runtime, ForeignKey meta)
	 * boolean rename(DataRuntime runtime, ForeignKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta)
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * foreign[调用入口]<br/>
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		LinkedHashMap<String,Column> columns = meta.getColumns();
		if(null != columns && !columns.isEmpty()) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, meta.getTable(true));
			builder.append(" ADD");
			if(BasicUtil.isNotEmpty(meta.getName())){
				builder.append(" CONSTRAINT ").append(meta.getName());
			}
			builder.append(" FOREIGN KEY (");
			delimiter(builder, Column.names(columns));
			builder.append(")");
			builder.append(" REFERENCES ").append(meta.getReference().getName()).append("(");
			boolean first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				name(runtime, builder, column.getReference());
				first = false;
			}
			builder.append(")");

		}
		return runs;
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param meta 外键
	 * @return List
	 */

	/**
	 * 添加外键
	 * @param meta 外键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, meta.getTable(true));
		builder.append(" DROP FOREIGN KEY ").append(meta.getName());
		return runs;
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Table table, Index meta)
	 * boolean drop(DataRuntime runtime, Index meta)
	 * boolean rename(DataRuntime runtime, Index origin, String name)
	 * [命令合成]
	 * List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Index meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Index meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Index meta)
	 * [命令合成-子流程]
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)
	 ******************************************************************************************************************/

	/**
	 * index[调用入口]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Index meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Index meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Index meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 重命名索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 索引
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * index[命令合成]<br/>
	 * 创建表过程添加索引,表创建完成后添加索引,于表内索引index(DataRuntime, StringBuilder, Table)二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	@Override
	public List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception {
		return super.buildAppendIndexRun(runtime, meta);
	}

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception {
		String name = meta.getName();
		if(BasicUtil.isEmpty(name)){
			name = "index_"+BasicUtil.getRandomString(10);
		}
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE");
		if(meta.isUnique()){
			builder.append(" UNIQUE");
		}else if(meta.isFulltext()){
			builder.append(" FULLTEXT");
		}else if(meta.isSpatial()){
			builder.append(" SPATIAL");
		}
		builder.append(" ").append(keyword(meta)).append(" ");
		checkIndexExists(runtime, builder, false);
		name(runtime, builder, meta);
		builder.append(" ON ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append("(");
		int qty = 0;
		LinkedHashMap<String, Column> columns = meta.getColumns();
		//排序
		Column.sort(meta.getPositions(), columns);
		for(Column column:columns.values()){
			if(qty>0){
				builder.append(",");
			}
			delimiter(builder, column.getName());
			Order.TYPE order = meta.getOrder(column.getName());
			if(BasicUtil.isNotEmpty(order)){
				builder.append(" ").append(order);
			}
			qty ++;
		}
		builder.append(")");
		type(runtime, builder, meta);
		comment(runtime, builder, meta);
		return runs;
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception {
		List<Run> runs = buildDropRun(runtime, meta);
		Index update = (Index)meta.getUpdate();
		if(null != update){
			runs.addAll(buildAddRun(runtime, update));
		}else {
			runs.addAll(buildAddRun(runtime, meta));
		}
		return runs;
	}

	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		if(meta.isPrimary()){
			builder.append("ALTER TABLE ");
			name(runtime, builder, table);
			builder.append(" DROP CONSTRAINT ").append(meta.getName());
		}else {
			builder.append("DROP INDEX ").append(meta.getName());
			if (BasicUtil.isNotEmpty(table)) {
				builder.append(" ON ");
				name(runtime, builder, table);
			}
		}
		return runs;
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception {
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta){
		return super.type(runtime, builder, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta){
		return super.comment(runtime, builder, meta);
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 创建或删除表索引前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkIndexExists(runtime, builder, exists);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Table table, Constraint meta)
	 * boolean drop(DataRuntime runtime, Constraint meta)
	 * boolean rename(DataRuntime runtime, Constraint origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)
	 ******************************************************************************************************************/

	/**
	 * constraint[调用入口]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception {
		return super.alter(runtime, table, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 重命名约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 约束
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception {
		String name = meta.getName();
		if(BasicUtil.isEmpty(name)){
			name = "constraint_"+BasicUtil.getRandomString(10);
		}
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append(" ADD CONSTRAINT ").append(name);
		if(meta.isUnique()){
			builder.append(" UNIQUE");
		}
		builder.append("(");
		boolean first = true;
		Collection<Column> cols = meta.getColumns().values();
		for(Column column:cols){
			if(!first){
				builder.append(",");
			}
			first = false;
			delimiter(builder, column.getName());
			String order = column.getOrder();
			if(BasicUtil.isNotEmpty(order)){
				builder.append(" ").append(order);
			}
		}
		builder.append(")");
		return runs;
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append(" DROP CONSTRAINT ").append(meta.getName());
		return runs;
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER CONSTRAINT ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());

		return runs;
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * trigger[调用入口]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception {
		return super.add(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Trigger meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Trigger meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 重命名触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 触发器
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE TRIGGER ").append(meta.getName());
		builder.append(" ").append(meta.getTime().sql()).append(" ");
		List<Trigger.EVENT> events = meta.getEvents();
		boolean first = true;
		for(Trigger.EVENT event:events){
			if(!first){
				builder.append(" OR ");
			}
			builder.append(event);
			first = false;
		}
		builder.append(" ON ");
		name(runtime, builder, meta.getTable(true));
		each(runtime, builder, meta);

		builder.append("\n").append(meta.getDefinition());

		return runs;
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP TRIGGER ");
		name(runtime, builder, meta);
		return runs;
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		builder.append("ALTER TRIGGER ");
		if(BasicUtil.isNotEmpty(catalog)) {
			name(runtime, builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			name(runtime, builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append(" RENAME TO ");
		name(runtime, builder, meta.getUpdate());

		return runs;
	}

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta){
		if(meta.isEach()){
			builder.append(" FOR EACH ROW ");
		}else{
			builder.append(" FOR EACH STATEMENT ");
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Function meta)
	 * boolean alter(DataRuntime runtime, Function meta)
	 * boolean drop(DataRuntime runtime, Function meta)
	 * boolean rename(DataRuntime runtime, Function origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Function function)
	 * List<Run> buildAlterRun(DataRuntime runtime, Function function)
	 * List<Run> buildDropRun(DataRuntime runtime, Function function)
	 * List<Run> buildRenameRun(DataRuntime runtime, Function function)
	 ******************************************************************************************************************/

	/**
	 * function[调用入口]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Function meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Function meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Function meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * function[调用入口]<br/>
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP FUNCTION ");
		name(runtime, builder, meta);
		return runs;
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER FUNCTION ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());
		return runs;
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Sequence meta)
	 * boolean alter(DataRuntime runtime, Sequence meta)
	 * boolean drop(DataRuntime runtime, Sequence meta)
	 * boolean rename(DataRuntime runtime, Sequence origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildAlterRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildDropRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildRenameRun(DataRuntime runtime, Sequence sequence)
	 ******************************************************************************************************************/

	/**
	 * sequence[调用入口]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Sequence meta) throws Exception {
		return super.create(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Sequence meta) throws Exception {
		return super.alter(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Sequence meta) throws Exception {
		return super.drop(runtime, meta);
	}

	/**
	 * sequence[调用入口]<br/>
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception {
		return super.rename(runtime, origin, name);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception {
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP SEQUENCE ");
		name(runtime, builder, meta);
		return runs;
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER SEQUENCE ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());
		return runs;
	}

	protected DataSet select(DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String sql, List<Object> values){

		return new DataSet();
	}
	public <T extends Column> T column(T column, List<T> columns){
		for(T item:columns){
			if (item.getIdentity().equals(column.getIdentity())) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 生成insert sql的value部分,每个Entity(每行数据)调用一次
	 * (1,2,3)
	 * (?,?,?)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run           run
	 * @param obj           Entity或DataRow
	 * @param placeholder   是否使用占位符(批量操作时不要超出数量)
	 * @param scope         是否带(), 拼接在select后时不需要
	 * @param alias         是否添加别名
	 * @param columns          需要插入的列
	 * @param child          是否在子查询中，子查询中不要用序列
	 */
	protected String insertValue(DataRuntime runtime, Run run, Object obj, boolean child, boolean placeholder, boolean alias, boolean scope, LinkedHashMap<String,Column> columns){
		int batch = run.getBatch();
		StringBuilder builder = new StringBuilder();
		if(scope && batch<=1) {
			builder.append("(");
		}
		int from = 1;
		if(obj instanceof DataRow){
			from = 1;
		}
		run.setFrom(from);
		boolean first = true;
		for(Column column:columns.values()){
			boolean place = placeholder;
			boolean src = false; //直接拼接 如${now()} ${序列}
			String key = column.getName();
			if (!first && batch<=1) {
				builder.append(",");
			}
			first = false;
			Object value = null;
			if(obj instanceof DataRow){
				value = BeanUtil.getFieldValue(obj, key);
			}else if(obj instanceof Map){
				value = ((Map)obj).get(key);
			}else{
				value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
			}
			if(value != null){
				if(value instanceof SQL_BUILD_IN_VALUE){
					place = false;
				}else if(value instanceof String){
					String str = (String)value;
					//if(str.startsWith("${") && str.endsWith("}")){
					if(BasicUtil.checkEl(str)){
						src = true;
						place = false;
						value = str.substring(2, str.length()-1);
						if (child && str.toUpperCase().contains(".NEXTVAL")) {
							value = null;
						}
					}else if("NULL".equals(str)){
						value = null;
					}
				}
			}
			if(src){
				builder.append(value);
			}else {
				if (batch <= 1) {
					if (place) {
						builder.append("?");
						addRunValue(runtime, run, Compare.EQUAL, column, value);
					} else {
						//value(runtime, builder, obj, key);
						builder.append(write(runtime, column, value, false));
					}
				} else {
					addRunValue(runtime, run, Compare.EQUAL, column, value);
				}
			}

			if(alias && batch<=1){
				builder.append(" AS ");
				delimiter(builder, key);
			}
		}
		if(scope && batch<=1) {
			builder.append(")");
		}
		return builder.toString();
	}

	/**
	 * 拼接字符串
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param args args
	 * @return String
	 */
	@Override
	public String concat(DataRuntime runtime, String... args) {
		return null;
	}

	/**
	 * 伪表
	 * @return String
	 */
	protected String dummy(){
		return "dual";
	}
	/* *****************************************************************************************************************
	 * 													多分支子类型选择(子类只选择调用不要出现不要覆盖)
	 * -----------------------------------------------------------------------------------------------------------------
	 * protected String pageXXX()
	 * protected String concatXXX()
	 ******************************************************************************************************************/

	/**
	 * 合成分页 mysql适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageLimit(DataRuntime runtime, Run run) {
		String sql = run.getBaseQuery();
		String cols = run.getQueryColumn();
		if(!"*".equals(cols)){
			String reg = "(?i)^select[\\s\\S]+from";
			sql = sql.replaceAll(reg,"SELECT "+cols+" FROM ");
		}
		OrderStore orders = run.getOrderStore();
		if(null != orders){
			sql += orders.getRunText(getDelimiterFr() + getDelimiterTo());
		}
		PageNavi navi = run.getPageNavi();
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = compressCondition(runtime, sql);
		return sql;
	}

	/**
	 * 合成分页 pg适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageLimitOffset(DataRuntime runtime, Run run){
		String sql = run.getBaseQuery();
		String cols = run.getQueryColumn();
		if(!"*".equals(cols)){
			String reg = "(?i)^select[\\s\\S]+from";
			sql = sql.replaceAll(reg,"SELECT "+cols+" FROM ");
		}
		OrderStore orders = run.getOrderStore();
		if(null != orders){
			sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		PageNavi navi = run.getPageNavi();
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow();
		}
		sql = compressCondition(runtime, sql);
		return sql;
	}

	/**
	 * 合成分页 oracle12-适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageRowNum(DataRuntime runtime, Run run){
		StringBuilder builder = new StringBuilder();
		String cols = run.getQueryColumn();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQuery();
		OrderStore orders = run.getOrderStore();
		long first = 0;
		long last = 0;
		String order = "";
		if(null != orders){
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		if(null != navi){
			first = navi.getFirstRow();
			last = navi.getLastRow();
		}
		if(null == navi){
			builder.append(sql).append("\n").append(order);
		}else{
			// 分页
			builder.append("SELECT ").append(cols).append(" FROM( \n");
			builder.append("SELECT TAB_I.*,ROWNUM AS PAGE_ROW_NUMBER_ \n");
			builder.append("FROM( \n");
			builder.append(sql);
			builder.append("\n").append(order);
			builder.append(")  TAB_I \n");
			builder.append(")  TAB_O WHERE PAGE_ROW_NUMBER_ >= ").append(first + 1).append(" AND PAGE_ROW_NUMBER_ <= ").append(last + 1);

		}

		return builder.toString();

	}

	/**
	 * 合成分页 oracle12=+适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageOffsetNext(DataRuntime runtime, Run run) {
		StringBuilder builder = new StringBuilder();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQuery();
		OrderStore orders = run.getOrderStore();
		long first = 0;
		String order = "";
		if(null != orders){
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		if(null != navi){
			first = navi.getFirstRow();
		}
		if(null == navi){
			builder.append(sql).append("\n").append(order);
		}else{
			// 分页
			builder.append(sql).append("\n").append(order);
			builder.append(" OFFSET ").append(first).append(" ROWS FETCH NEXT ").append(navi.getPageRows()).append(" ROWS ONLY");
		}
		return builder.toString();
	}

	/**
	 * 合成分页 informix适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageSkip(DataRuntime runtime, Run run){
		String sql = run.getBaseQuery();
		String cols = run.getQueryColumn();
		if(!"*".equals(cols)){
			String reg = "(?i)^select[\\s\\S]+from";
			sql = sql.replaceAll(reg,"SELECT " + cols + " FROM ");
		}
		OrderStore orders = run.getOrderStore();
		if(null != orders){
			sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		PageNavi navi = run.getPageNavi();
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			String sub = sql.substring(sql.toUpperCase().indexOf("SELECT") + 6);
			sql = "SELECT SKIP " + navi.getFirstRow() + " FIRST " + limit + sub;
		}
		return sql;
	}

	/**
	 * 合成分页 mssql 2005-适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageTop(DataRuntime runtime, Run run){
		StringBuilder builder = new StringBuilder();
		String cols = run.getQueryColumn();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQuery();
		OrderStore orders = run.getOrderStore();
		long first = 0;
		long last = 0;
		String order = "";
		if(null != orders){
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		if(null != navi){
			first = navi.getFirstRow();
			last = navi.getLastRow();
		}
		if(first == 0 && null != navi){
			// top
			builder.append("SELECT TOP ").append(last+1).append(" "+cols+" FROM(\n");
			builder.append(sql).append("\n) AS _TAB_O \n");
			builder.append(order);
			return builder.toString();
		}
		if(null == navi){
			builder.append(sql).append("\n").append(order);
		}else{
			// 分页
			long rows = navi.getPageRows();
			if(rows * navi.getCurPage() > navi.getTotalRow()){
				// 最后一页不足10条
				rows = navi.getTotalRow() % navi.getPageRows();
			}
			String asc = order;
			String desc = order.replace("ASC","<A_ORDER>");
			desc = desc.replace("DESC","ASC");
			desc = desc.replace("<A_ORDER>","DESC");
			builder.append("SELECT "+cols+" FROM (\n ");
			builder.append("SELECT TOP ").append(rows).append(" * FROM (\n");
			builder.append("SELECT TOP ").append(navi.getPageRows()*navi.getCurPage()).append(" * ");
			builder.append(" FROM (" + sql + ") AS T0 ").append(asc).append("\n");
			builder.append(") AS T1 ").append(desc).append("\n");
			builder.append(") AS T2").append(asc);
		}
		return builder.toString();
	}

	/**
	 * 合成分页 mssql 2005=+适用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	protected String pageRowNumber(DataRuntime runtime, Run run){

		StringBuilder builder = new StringBuilder();
		String cols = run.getQueryColumn();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQuery();
		OrderStore orders = run.getOrderStore();
		long first = 0;
		long last = 0;
		String order = "";
		if(null != orders){
			order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		if(null != navi){
			first = navi.getFirstRow();
			last = navi.getLastRow();
		}
		if(first == 0 && null != navi){
			// top
			builder.append("SELECT TOP ").append(last+1).append(" "+cols+" FROM(\n");
			builder.append(sql).append("\n) AS _TAB_O \n");
			builder.append(order);
			return builder.toString();
		}
		if(null == navi){
			builder.append(sql).append("\n").append(order);
		}else{
			// 分页
			// 2005 及以上
			if(BasicUtil.isEmpty(order)){
				order = "ORDER BY "+ ConfigTable.DEFAULT_PRIMARY_KEY;
			}
			builder.append("SELECT "+cols+" FROM( \n");
			builder.append("SELECT _TAB_I.*,ROW_NUMBER() OVER(")
					.append(order)
					.append(") AS PAGE_ROW_NUMBER_ \n");
			builder.append("FROM( \n");
			builder.append(sql);
			builder.append(") AS _TAB_I \n");
			builder.append(") AS _TAB_O WHERE PAGE_ROW_NUMBER_ BETWEEN "+(first+1)+" AND "+(last+1));
		}
		return builder.toString();
	}

	protected String concatFun(DataRuntime runtime, String ... args){
		String result = "";
		if(null != args && args.length > 0){
			result = "concat(";
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += ",";
				}
				result += arg;
			}
			result += ")";
		}
		return result;
	}

	protected String concatOr(DataRuntime runtime, String ... args){
		String result = "";
		if(null != args && args.length > 0){
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += " || ";
				}
				result += arg;
			}
		}
		return result;
	}
	protected String concatAdd(DataRuntime runtime, String ... args){
		String result = "";
		if(null != args && args.length > 0){
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += " + ";
				}
				result += arg;
			}
		}
		return result;
	}
	protected String concatAnd(DataRuntime runtime, String ... args){
		String result = "";
		if(null != args && args.length > 0){
			int size = args.length;
			for(int i=0; i<size; i++){
				String arg = args[i];
				if(i>0){
					result += " & ";
				}
				result += arg;
			}
		}
		return result;
	}
	private void queryTimeout(Statement statement, ConfigStore configs){
		int timeout = ConfigStore.SQL_QUERY_TIMEOUT(configs);
		if(timeout > 0){
			try {
				statement.setQueryTimeout(timeout);
			}catch (Exception e){
				log.warn("设置超时时间异常:{}", e);
			}
		}
	}
	private void updateTimeout(Statement statement, ConfigStore configs){
		int timeout = ConfigStore.SQL_QUERY_TIMEOUT(configs);
		if(timeout > 0){
			try {
				statement.setQueryTimeout(timeout);
			}catch (Exception e){
				log.warn("设置超时时间异常:{}", e);
			}
		}
	}

}
