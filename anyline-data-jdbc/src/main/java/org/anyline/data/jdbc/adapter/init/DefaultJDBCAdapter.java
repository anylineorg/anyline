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


package org.anyline.data.jdbc.adapter.init;


import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.handler.*;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.runtime.JDBCRuntime;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.AutoPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;
import org.anyline.util.encrypt.MD5Util;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */
@Repository("anyline.data.jdbc.adapter.default")
public class DefaultJDBCAdapter extends DefaultDriverAdapter implements JDBCAdapter {
	protected static final Logger log = LoggerFactory.getLogger(DefaultJDBCAdapter.class);

	@Override
	public DatabaseType type() {
		return DatabaseType.COMMON;
	}
	protected JdbcTemplate jdbc(DataRuntime runtime){
		Object processor = runtime.getProcessor();
		return (JdbcTemplate) processor;
	}

	/**
	 * 当前环境与指定运行环境(数据源)是否匹配
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return boolean
	 */
	@Override
	public boolean match(DataRuntime runtime){
		return super.match(runtime);
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
	 * long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * public Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
	 * public String batchInsertSeparator()
	 * public boolean supportInsertPlaceholder ()
	 * protected Run createInsertRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns)
	 * protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, List<String> columns)
	 * public String generatedKey()
	 * [命令执行]
	 * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);
	 ******************************************************************************************************************/

	/**
	 * insert [调用入口]<br/>
	 * 执行前根据主键生成器补充主键值,执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 需要插入入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return super.insert(runtime, random, batch, dest, data, configs, columns);
	}
	/**
	 * insert [命令合成]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 需要插入的数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns){
		Run run = null;
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceUtil.parseDataSource(dest, obj);
		}

		if(obj instanceof Collection){
			Collection list = (Collection) obj;
			if(list.size() > 0){
				run = createInsertRunFromCollection(runtime, batch, dest, list, configs, columns);
			}
		}else {
			run = createInsertRun(runtime, dest, obj, configs, columns);
		}
		convert(runtime, configs, run);
		return run;
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param set 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns){
		StringBuilder builder = run.getBuilder();
		int batch = run.getBatch();
		if(null == builder){
			builder = new StringBuilder();
			run.setBuilder(builder);
		}
		LinkedHashMap<String, Column> pks = null;
		PrimaryGenerator generator = checkPrimaryGenerator(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));
		if(null != generator){
			pks = set.getRow(0).getPrimaryColumns();
			columns.putAll(pks);
		}
		String head = insertHead(configs);
		builder.append(head).append(parseTable(dest));
		builder.append("(");
		boolean first = true;
		for(Column column:columns.values()){
			if(!first){
				builder.append(",");
			}
			first = false;
			String key = column.getName();
			delimiter(builder, key);
		}
		builder.append(") VALUES ");
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
		int dataSize = set.size();
		for(int i=0; i<dataSize; i++){
			DataRow row = set.getRow(i);
			if(null == row){
				continue;
			}
			if(row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())){
				if(null != generator){
					generator.create(row, type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), BeanUtil.getMapKeys(pks), null);
				}
				//createPrimaryValue(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
			}
			builder.append(insertValue(runtime, run, row, true,true, false,true, columns));
			if(batch <=1) {
				if (i < dataSize - 1) {
					//多行数据之间的分隔符
					builder.append(batchInsertSeparator());
				}
			}
		}
		builder.append(insertFoot(configs, columns));
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 填充inset命令内容(创建批量INSERT RunPrepare)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 需要插入的数据集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns){
		StringBuilder builder = run.getBuilder();
		int batch = run.getBatch();
		if(null == builder){
			builder = new StringBuilder();
			run.setBuilder(builder);
		}
		if(list instanceof DataSet){
			DataSet set = (DataSet) list;
			this.fillInsertContent(runtime, run, dest, set, configs, columns);
			return;
		}
		PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));
		Object entity = list.iterator().next();
		List<String> pks = null;
		if(null != generator) {
			columns.putAll(EntityAdapterProxy.primaryKeys(entity.getClass()));
		}
		String head = insertHead(configs);
		builder.append(head).append(parseTable(dest));
		builder.append("(");

		boolean first = true;
		for(Column column:columns.values()){
			if(!first){
				builder.append(",");
			}
			first = false;
			delimiter(builder, column.getName());
		}
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
                    createPrimaryValue(row, type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                }
                insertValue(template, run, row, true, false,true, keys);
            }else{*/
			boolean create = EntityAdapterProxy.createPrimaryValue(obj, BeanUtil.getMapKeys(columns));
			if(!create && null != generator){
				generator.create(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
				//createPrimaryValue(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
			}
			builder.append(insertValue(runtime, run, obj, true,true, false, true, columns));
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
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj  Entity或DataRow
	 * @param batch  是否批量，批量时不检测值是否为空
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch){
		return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	@Override
	public String batchInsertSeparator (){
		return ",";
	}

	/**
	 * insert [命令合成-子流程]<br/>
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	@Override
	public boolean supportInsertPlaceholder (){
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
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 数据
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
		Run run = new TableRun(runtime, dest);
		// List<Object> values = new ArrayList<Object>();
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(dest)){
			throw new org.anyline.exception.SQLException("未指定表");
		}

		PrimaryGenerator generator = checkPrimaryGenerator(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));

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
				generator.create(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
				//createPrimaryValue(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
			}
		}else{
			from = 2;
			boolean create = EntityAdapterProxy.createPrimaryValue(obj, columns);
			LinkedHashMap<String,Column> pks = EntityAdapterProxy.primaryKeys(obj.getClass());
			if(!create && null != generator){
				generator.create(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
				//createPrimaryValue(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
			}
		}
		run.setFrom(from);
		/*确定需要插入的列*/
		LinkedHashMap<String,Column> cols = confirmInsertColumns(runtime, dest, obj, configs, columns, false);
		if(null == cols || cols.size() == 0){
			throw new org.anyline.exception.SQLException("未指定列(DataRow或Entity中没有需要插入的属性值)["+obj.getClass().getName()+":"+BeanUtil.object2json(obj)+"]");
		}
		boolean replaceEmptyNull = false;
		if(obj instanceof DataRow){
			row = (DataRow)obj;
			replaceEmptyNull = row.isReplaceEmptyNull();
		}else{
			replaceEmptyNull = IS_REPLACE_EMPTY_NULL(configs);
		}
		String head = insertHead(configs);
		builder.append(head).append(parseTable(dest));
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

			if(null != str && str.startsWith("${") && str.endsWith("}")){
				value = str.substring(2, str.length()-1);
				valuesBuilder.append(value);
			}else if(null != value && value instanceof SQL_BUILD_IN_VALUE){
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
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param list 对象集合
	 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, List<String> columns){
		Run run = new TableRun(runtime, dest);
		run.setBatch(batch);
		if(null == list || list.isEmpty()){
			throw new org.anyline.exception.SQLException("空数据");
		}
		Object first = null;
		if(list instanceof DataSet){
			DataSet set = (DataSet)list;
			first = set.getRow(0);
			if(BasicUtil.isEmpty(dest)){
				dest = DataSourceUtil.parseDataSource(dest,set);
			}
			if(BasicUtil.isEmpty(dest)){
				dest = DataSourceUtil.parseDataSource(dest,first);
			}
		}else{
			first = list.iterator().next();
			if(BasicUtil.isEmpty(dest)) {
				dest = EntityAdapterProxy.table(first.getClass(), true);
			}
		}
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
		fillInsertContent(runtime, run,  dest, list, configs, cols);

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
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][action:{}][table:{}][不具备执行条件]", action, run.getTable());
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
		if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
			if(batch > 1 && !IS_LOG_BATCH_SQL_PARAM(configs)){
				log.info("{}[action:{}][table:{}][sql:\n{}\n]\n[param size:{}]", random, action, run.getTable(), sql, values.size());
			}else {
				log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
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
		KeyHolder keyholder = null;
		JdbcTemplate jdbc = jdbc(runtime);
		try {
			if(batch > 1){
				cnt = batch(jdbc, sql, batch, run.getVol(), values);
			}else {
				//是否支持返回自增值
				if(IS_KEYHOLDER_IDENTITY(configs)){
					//需要返回自增
					keyholder = new GeneratedKeyHolder();
					cnt = jdbc.update(new PreparedStatementCreator() {
						@Override
						public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
							PreparedStatement ps = null;
							if (null != pks && pks.length > 0) {
								//返回多个值
								ps = con.prepareStatement(sql, pks);
							} else {
								ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
							}
							int idx = 0;
							if (null != values) {
								for (Object obj : values) {
									ps.setObject(++idx, obj);
								}
							}
							return ps;
						}
					}, keyholder);
				}else{
					if (null != values && !values.isEmpty()) {
						cnt = jdbc.update(sql, values.toArray());
					}else {
						cnt = jdbc.update(sql);
					}
				}
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}ms]{}", random, action, run.getTable(), millis, run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, sql, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)) {
				String qty = LogUtil.format(cnt, 34);
				if(batch > 1){
					qty = LogUtil.format("约"+cnt, 34);
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}ms][影响行数:{}]", random, action, run.getTable(), millis, qty);
			}
			identity(runtime, random, data, configs, keyholder);
		}catch(Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:{}][table:{}]{}", random, LogUtil.format("插入异常:", 33)+e, action, run.getTable(), run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return cnt;
	}

	/**
	 * insert [命令执行]
	 * <br/>
	 * 有些不支持返回自增的单独执行<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	/*@Override
	public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run){
		long cnt = 0;
		if(null == random){
			random = random(runtime);
		}
		int batch = run.getBatch();
		String action = "insert";
		if(batch > 1){
			action = "batch insert";
		}

		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][action:{}][table:{}][不具备执行条件]", action, run.getTable());
			}
			return -1;
		}
		String sql = run.getFinalInsert();
		if(BasicUtil.isEmpty(sql) && log.isWarnEnabled() && IS_LOG_SQL(configs)){
			log.warn("[不具备执行条件][action:{}][table:{}]", action, run.getTable());
			return -1;
		}
		if(null != configs){
			configs.add(run);
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		*//*执行SQL*//*
		if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
			log.info("{}[action:{}][table:{}]{}", random, action, run.getTable(), run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
		}

		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return -1;
		}
		long millis = -1;

		try {
			JdbcTemplate jdbc = jdbc(runtime);
			if(batch > 1){
				cnt = batch(jdbc, sql, batch, run.getVol(), values);
			}else {
				if (null == values || values.isEmpty()) {
					cnt = jdbc.update(sql);
				} else {
					cnt = jdbc.update(sql, values.toArray());
				}
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}ms]{}}", random, action, run.getTable(), millis, run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, sql, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)) {
				String qty = ""+cnt;
				if(batch>1){
					qty = "约"+cnt;
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}ms][影响行数:{}]", random, action, run.getTable(), millis, LogUtil.format(qty, 34));
			}
		}catch(Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:{}][table:{}]{}", random, action , run.getTable(), LogUtil.format("插入异常:", 33)+e.toString(), run.log(ACTION.DML.INSERT, IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return cnt;
	}
*/

	/* *****************************************************************************************************************
	 * 													UPDATE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
	 * [命令合成]
	 * Run buildUpdateRun(DataRuntime runtime, int batch,  String dest, Object obj, ConfigStore configs, List<String> columns)
	 * Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
	 * Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns)
	 * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns)
	 * LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns)
	 * LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns)
	 * [命令执行]
	 * long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * UPDATE [调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return super.update(runtime, random, batch, dest, data, configs, columns);
	}
	/**
	 * update [命令合成]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj Entity或DtaRow
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildUpdateRun(DataRuntime runtime, int batch,  String dest, Object obj, ConfigStore configs, List<String> columns){
		return super.buildUpdateRun(runtime, batch, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns){
		return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, columns);
	}
	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns){
		return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, columns);
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
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return List
	 */
	@Override
	public LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns){
		return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
	}
	@Override
	public LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
		return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
	}
	/**
	 * update [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run){
		long result = 0;
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		String sql = null;
		sql = run.getFinalUpdate();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
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
		if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
			if(batch > 1 && !IS_LOG_BATCH_SQL_PARAM(configs)){
				log.info("{}[action:{}][table:{}]{}", random, action, run.getTable(), run.log(ACTION.DML.UPDATE, IS_SQL_LOG_PLACEHOLDER(configs)));
			}else {
				log.info("{}[action:update][table:{}]{}", random, run.getTable(), run.log(ACTION.DML.UPDATE, IS_SQL_LOG_PLACEHOLDER(configs)));
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
			JdbcTemplate jdbc = jdbc(runtime);
			if(batch > 1){
				result = batch(jdbc, sql, batch, run.getVol(), values);
			}else {
				result = jdbc.update(sql, values.toArray());
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}ms]{}", random, action, run.getTable(), millis, run.log(ACTION.DML.UPDATE, IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, sql, values, null, true , result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)) {
				String qty = result+"";
				if(batch>1){
					qty = "约"+result;
				}
				log.info("{}[action:{}][table:{}][执行耗时:{}ms][影响行数:{}]", random, action, run.getTable(), millis, LogUtil.format(qty, 34));
			}

		}catch(Exception e) {
			if (IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if (IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
				SQLUpdateException ex = new SQLUpdateException("update异常:" + e.toString(), e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
			if (IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.error("{}[{}][action:update][table:{}]{}", random, run.getTable(), LogUtil.format("更新异常:", 33) + e.toString(), run.log(ACTION.DML.UPDATE, IS_SQL_LOG_PLACEHOLDER(configs)));
			}

		}
		return result;
	}



	/**
	 * save [调用入口]
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data 数据
	 * @param configs 更新条件
	 * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
	 *                列可以加前缀<br/>
	 *                +:表示必须更新<br/>
	 *                -:表示必须不更新<br/>
	 *                ?:根据是否有值<br/>
	 *
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列<br/>
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		return super.save(runtime, random,  dest, data, configs, columns);
	}

	@Override
	protected long saveCollection(DataRuntime runtime, String random, String dest, Collection<?> data, ConfigStore configs, List<String> columns){
		return super.saveCollection(runtime, random,  dest, data, configs, columns);
	}
	@Override
	protected long saveObject(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		return super.saveObject(runtime, random,  dest, data, configs, columns);
	}
	@Override
	protected Boolean checkOverride(Object obj){
		return super.checkOverride(obj);
	}
	@Override
	protected Map<String,Object> checkPv(Object obj){
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
	public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, String table, ConfigStore configs, LinkedHashMap<String, Column> columns){
		return super.checkMetadata(runtime, table, configs, columns);
	}

	/* *****************************************************************************************************************
	 * 													QUERY
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * DataSet querys(DataRuntime runtime, String random,  RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)
	 * <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions)
	 * List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * [命令合成]
	 * Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)
	 * void fillQueryContent(DataRuntime runtime, Run run)
	 * String mergeFinalQuery(DataRuntime runtime, Run run)
	 * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value)
	 * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value)
	 * StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value)
	 * [命令执行]
	 * DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)
	 * List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 * Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 * DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)
	 * List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list)
	 ******************************************************************************************************************/

	/**
	 * query [调用入口]
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
	 * query procedure [调用入口]
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
			log.info("{}[action:procedure][sql:\n{}\n][input param:{}]\n[output param:{}]", random, procedure.getName(), LogUtil.param(inputs), LogUtil.param(outputs));
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
			JdbcTemplate jdbc = jdbc(runtime);
			long fr = System.currentTimeMillis();
			set = (DataSet) jdbc.execute(new CallableStatementCreator(){
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					String sql = "{call " +procedure.getName()+"(";
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

					CallableStatement cs = conn.prepareCall(sql);
					for(int i=1; i<=sizeIn; i++){
						Parameter param = inputs.get(i-1);
						Object value = param.getValue();
						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
							value = null;
						}
						cs.setObject(i, value, param.getType());
					}
					for(int i=1; i<=sizeOut; i++){
						Parameter param = outputs.get(i-1);
						if(null == param.getValue()){
							cs.registerOutParameter(i+sizeIn, param.getType());
						}else{
							cs.setObject(i, param.getValue(), param.getType());
						}

					}
					return cs;
				}
			}, new CallableStatementCallback<Object>(){
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					ResultSet rs = cs.executeQuery();
					DataSet set = new DataSet();
					ResultSetMetaData rsmd = rs.getMetaData();
					int cols = rsmd.getColumnCount();
					for(int i=1; i<=cols; i++){
						String name = rsmd.getColumnLabel(i);
						if(null == name){
							name = rsmd.getColumnName(i);
						}
						set.addHead(name);
					}
					long mid = System.currentTimeMillis();
					int index = 0;
					long first = -1;
					long last = -1;
					if(null != navi){
						first = navi.getFirstRow();
						last = navi.getLastRow();
					}
					while(rs.next()){
						if(first ==-1 || (index >= first && index <= last)){
							DataRow row = new DataRow();
							for(int i=1; i<=cols; i++){
								row.put(false, rsmd.getColumnLabel(i), rs.getObject(i));
							}
							set.addRow(row);
						}
						index ++;
						if(first != -1){
							if(index > last){
								break;
							}
							if(first ==0 && last==0){// 只取一行
								break;
							}
						}
					}
					if(null != navi){
						navi.setTotalRow(index);
						set.setNavi(navi);
					}

					set.setDatalink(rt.datasource());
					if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()){
						log.info("{}[封装耗时:{}ms][封装行数:{}]", rdm, System.currentTimeMillis() - mid,set.size());
					}
					return set;
				}
			});
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
			if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[slow cmd][action:procedure][执行耗时:{}ms][sql:\n{}\n][input param:{}]\n[output param:{}]"
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
					log.error("{}[{}][action:procedure][sql:\n{}\n]\n[input param:{}]\n[output param:{}]"
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
	 * query [调用入口]
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
	protected  <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, String table, ConfigStore configs, Run run){
		return super.select(runtime, random, clazz, table, configs, run);
	}

	/**
	 * query [调用入口]
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.maps(runtime, random, prepare, configs, conditions);
	}
	/**
	 * select[命令合成] 最终可执行命令
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
		replaceVariable(runtime, run);
		run.appendCondition();
		run.appendGroup();
		// appendOrderStore();
		run.checkValid();
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
		List<String> columns = sql.getQueryColumns();
		if(null == columns || columns.isEmpty()){
			ConfigStore configs = run.getConfigStore();
			if(null != configs) {
				columns = configs.columns();
			}
		}
		if(null != columns && columns.size()>0){
			// 指定查询列
			int size = columns.size();
			for(int i=0; i<size; i++){
				String column = columns.get(i);
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("${") && column.endsWith("}")){
					column = column.substring(2, column.length()-1);
					builder.append(column);
				}else{
					if(column.toUpperCase().contains(" AS ") || column.contains("(") || column.contains(",")){
						builder.append(column);
					}else if("*".equals(column)){
						builder.append("*");
					}else{
						delimiter(builder, column);
					}
				}
				if(i<size-1){
					builder.append(",");
				}
			}
			builder.append(BR);
		}else{
			// 全部查询
			builder.append("*");
			builder.append(BR);
		}
		builder.append("FROM").append(BR_TAB);
		if(null != run.getSchema()){
			delimiter(builder, run.getSchema()).append(".");
		}
		delimiter(builder, run.getTable());
		builder.append(BR);
		if(BasicUtil.isNotEmpty(sql.getAlias())){
			// builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(sql.getAlias());
		}
		List<Join> joins = sql.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(BR_TAB).append(join.getType().getCode()).append(" ");

				if(null != join.getSchema()){
					delimiter(builder, join.getSchema()).append(".");
				}
				delimiter(builder, join.getName());
				if(BasicUtil.isNotEmpty(join.getAlias())){
					// builder.append(" AS ").append(join.getAlias());
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t");
		/*添加查询条件*/
		// appendConfigStore();
		run.appendCondition();
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
	public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
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
			builder.append(" LIKE ").append(concat(runtime, "'%'", "?" , "'%'"));
		}else if(code == 51){
			builder.append(" LIKE ").append(concat(runtime, "?" , "'%'"));
		}else if(code == 52){
			builder.append(" LIKE ").append(concat(runtime, "'%'", "?"));
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
	public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) {
		return super.createConditionFindInSet(runtime, builder, column, compare, value);
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
	public StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
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
	 * select [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	@Override
	public DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run) {
		if(run instanceof ProcedureRun){
			ProcedureRun pr = (ProcedureRun)run;
			return querys(runtime, random, pr.getProcedure(), configs.getPageNavi());
		}
		String sql = run.getFinalQuery();
		if(BasicUtil.isEmpty(sql)){
			return new DataSet();
		}
		List<Object> values = run.getValues();
		return select(runtime, random, system, ACTION.DML.SELECT, table, configs, run, sql, values);
	}


	/**
	 * select [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return maps
	 */
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run){
		List<Map<String,Object>> maps = null;
		if(null == random){
			random = random(runtime);
		}
		if(null != configs){
			configs.add(run);
		}
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		if(BasicUtil.isEmpty(sql)){
			if(IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new ArrayList<>();
			}
		}
		long fr = System.currentTimeMillis();
		if(log.isInfoEnabled() && IS_LOG_SQL(configs)){
			log.info("{}[action:select][sql:\n{}\n]", random, run.log(ACTION.DML.SELECT, IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return new ArrayList<>();
		}
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			StreamHandler _handler = null;
			if(null != configs){
				_handler = configs.stream();
			}
			long[] count = new long[]{0};
			final boolean[] process = {false};
			final StreamHandler handler = _handler;
			final long[] mid = {System.currentTimeMillis()};
			if(null != handler){
				jdbc.query(con -> {
					PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					ps.setFetchSize(handler.size());
					ps.setFetchDirection(ResultSet.FETCH_FORWARD);
					if (null != values && values.size() > 0) {
						int idx = 0;
						for (Object value : values) {
							ps.setObject(++idx, value);
						}
					}
					return ps;
				}, rs -> {
					if(!process[0]){
						mid[0] = System.currentTimeMillis();
						process[0] = true;
					}
					stream(handler, rs, configs, true, runtime, null);
					count[0] ++;
				});
				maps = new ArrayList<>();
				//end stream handler
			}else {
				if (null != values && values.size() > 0) {
					maps = jdbc.queryForList(sql, values.toArray());
				} else {
					maps = jdbc.queryForList(sql);
				}
				mid[0] = System.currentTimeMillis();
				count[0] = maps.size();
			}
			boolean slow = false;
			if(SLOW_SQL_MILLIS(configs) > 0){
				if(mid[0]-fr > SLOW_SQL_MILLIS(configs)){
					slow = true;
					log.warn("{}[slow cmd][action:select][执行耗时:{}ms]{}", random, mid[0]-fr, run.log(ACTION.DML.SELECT, IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT,null, sql, values, null, true, maps, mid[0]-fr);
					}
				}
			}
			if(!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)){
				log.info("{}[action:select][执行耗时:{}ms]", random, mid[0] - fr);
			}
			maps = process(runtime, maps);
			if(!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)){
				log.info("{}[action:select][封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], count[0]);
			}
		}catch(Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e.toString(), run.log(ACTION.DML.SELECT,IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(IS_THROW_SQL_QUERY_EXCEPTION(configs)){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(), e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return maps;
	}
	/**
	 * select [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return map
	 */
	@Override
	public Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run){
		Map<String, Object> map = null;
		String sql = run.getFinalExists();
		List<Object> values = run.getValues();
		if(null != configs){
			configs.add(run);
		}
		long fr = System.currentTimeMillis();
		if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
			log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT, IS_SQL_LOG_PLACEHOLDER(configs)));
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
			JdbcTemplate jdbc = jdbc(runtime);
			if (null != values && values.size() > 0) {
				map = jdbc.queryForMap(sql, values.toArray());
			} else {
				map = jdbc.queryForMap(sql);
			}
		}catch (Exception e) {
			if(IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw e;
			}
			if (IS_LOG_SQL_WHEN_ERROR(configs)) {
				if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
					e.printStackTrace();
				}
				log.error("{}[{}][action:select][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e, sql, LogUtil.param(values));
			}
		}
		//}
		Long millis = System.currentTimeMillis() - fr;
		boolean slow = false;
		long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
		if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
			if(millis > SLOW_SQL_MILLIS){
				slow = true;
				log.warn("{}[slow cmd][action:exists][执行耗时:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
				if(null != dmListener){
					dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql,  values, null, true, map, millis);
				}
			}
		}
		if (!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)) {
			log.info("{}[action:select][执行耗时:{}ms][封装行数:{}]", random, millis, LogUtil.format(map == null ?0:1, 34));
		}
		return map;
	}

	/**
	 * select [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	@Override
	public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names){
		List<Run> runs = buildQuerySequence(runtime, next, names);
		if (null != runs && runs.size() > 0) {
			Run run = runs.get(0);
			if(!run.isValid()){
				if(ConfigTable.IS_LOG_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][sequence:"+names);
				}
				return new DataRow();
			}
			DataSet set = select(runtime, random, true, null, null, run);
			if (set.size() > 0) {
				return set.getRow(0);
			}
		}
		return new DataRow();
	}

	/**
	 * select [命令执行-子流程]
	 * JDBC执行完成后的结果处理
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param list JDBC执行返回的结果集
	 * @return  maps
	 */
	@Override
	public List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list){
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
	 * count [调用入口]
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
	 * count [命令合成]
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
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
		return sql;
	}

	/**
	 * count [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, Run run){
		long total = 0;
		DataSet set = select(runtime, random, false, ACTION.DML.COUNT, null, null, run, run.getTotalQuery(), run.getValues());
		total = set.toUpperKey().getInt(0,"CNT",0);
		return total;
	}


	/* *****************************************************************************************************************
	 * 													EXISTS
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * String mergeFinalExists(DataRuntime runtime, Run run)
	 ******************************************************************************************************************/

	/**
	 * exists [调用入口]
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
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
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
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
		return sql;
	}


	/* *****************************************************************************************************************
	 * 													EXECUTE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, String sql, List<Object> values)
	 * boolean execute(DataRuntime runtime, String random, Procedure procedure)
	 * [命令合成]
	 * Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * void fillExecuteContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long execute(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 ******************************************************************************************************************/

	/**
	 * execute [调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		return super.execute(runtime, random,  prepare, configs, conditions);
	}

	@Override
	public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, String cmd, List<Object> values){
		return super.execute(runtime, random,  batch, configs, cmd, values);
	}
	/**
	 * procedure [命令执行]
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
			log.info("{}[action:procedure][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, sql, LogUtil.param(inputs), LogUtil.param(outputs));
		}
		long millis= -1;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			list = (List<Object>) jdbc.execute(sql, new CallableStatementCallback<Object>() {
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException, DataAccessException {
					final List<Object> result = new ArrayList<Object>();
					// 带有返回参数
					int returnIndex = 0;
					if (procedure.hasReturn()) {
						returnIndex = 1;
						cs.registerOutParameter(1, Types.VARCHAR);
					}
					for (int i = 1; i <= sizeIn; i++) {
						Parameter param = inputs.get(i - 1);
						Object value = param.getValue();
						if (null == value || "NULL".equalsIgnoreCase(value.toString())) {
							value = null;
						}
						cs.setObject(i + returnIndex, value, param.getType());
					}
					for (int i = 1; i <= sizeOut; i++) {
						Parameter param = outputs.get(i - 1);
						if (null == param.getValue()) {
							cs.registerOutParameter(i + sizeIn + returnIndex, param.getType());
						} else {
							cs.setObject(i + sizeIn + returnIndex, param.getValue(), param.getType());
						}
					}
					cs.execute();
					if (procedure.hasReturn()) {
						result.add(cs.getObject(1));
					}
					if (sizeOut > 0) {
						// 注册输出参数
						for (int i = 1; i <= sizeOut; i++) {
							final Object output = cs.getObject(sizeIn + returnIndex + i);
							result.add(output);
						}
					}
					return result;
				}
			});

			cmd_success = true;
			procedure.setResult(list);
			result = true;
			millis = System.currentTimeMillis() - fr;

			boolean slow = false;
			long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
			if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[slow cmd][action:procedure][执行耗时:{}ms][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, millis, sql, LogUtil.param(inputs), LogUtil.param(list));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.PROCEDURE,null, sql, inputs,  list, true, result, millis);
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
				SQLUpdateException ex = new SQLUpdateException("execute异常:"+e.toString(),e);
				ex.setSql(sql);
				throw ex;
			}else{
				if(ConfigTable.IS_LOG_SQL_WHEN_ERROR){
					log.error("{}[{}][action:procedure][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, LogUtil.format("存储过程执行异常:", 33)+e.toString(), sql, LogUtil.param(inputs), LogUtil.param(outputs));
				}
			}
		}
		return result;
	}
	/**
	 * execute [命令合成]
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
		replaceVariable(runtime,run);
		run.appendCondition();
		run.appendGroup();
		run.checkValid();
	}
	@Override
	protected void fillExecuteContent(DataRuntime runtime, TableRun run){
		super.fillExecuteContent(runtime, run);
	}

	/**
	 * execute [命令合成-子流程]
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillExecuteContent(DataRuntime runtime, Run run){
		super.fillExecuteContent(runtime, run);
	}
	/**
	 * execute [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, ConfigStore configs, Run run){
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
		if(log.isInfoEnabled() && IS_LOG_SQL(configs)){
			if(batch >1 && !IS_LOG_BATCH_SQL_PARAM(configs)) {
				log.info("{}[action:{}][sql:\n{}\n]\n[param size:{}]", random, action, sql, values.size());
			}else {
				log.info("{}[action:{}][sql:\n{}\n]", random, action, run.log(ACTION.DML.EXECUTE, IS_SQL_LOG_PLACEHOLDER(configs)));
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
			JdbcTemplate jdbc = jdbc(runtime);
			if(batch>1){
				result = batch(jdbc, sql, batch, run.getVol(), values);
			}else {
				if (null != values && values.size() > 0) {
					result = jdbc.update(sql, values.toArray());
				} else {
					result = jdbc.update(sql);
				}
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[slow cmd][action:{}][执行耗时:{}ms][sql:\n{}\n]\n[param:{}]", random, action, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
					}
				}
			}
			if (!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)) {
				String qty = ""+result;
				if(batch>1){
					qty = "约"+result;
				}
				log.info("{}[action:{}][执行耗时:{}ms][影响行数:{}]", random, action, millis, LogUtil.format(qty, 34));
			}
		}catch(Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:{}]{}" , random, LogUtil.format("SQL执行异常:", 33)+e, action, run.log(ACTION.DML.EXECUTE, IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				throw e;
			}

		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													DELETE
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, Collection<T> values)
	 * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, Object obj, String... columns)
	 * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions)
	 * long truncate(DataRuntime runtime, String random, String table)
	 * [命令合成]
	 * Run buildDeleteRun(DataRuntime runtime, String table, Object obj, String ... columns)
	 * Run buildDeleteRun(DataRuntime runtime, int batch, String table, String column, Object values)
	 * List<Run> buildTruncateRun(DataRuntime runtime, String table)
	 * Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, String column, Object values)
	 * Run buildDeleteRunFromEntity(DataRuntime runtime, String table, Object obj, String ... columns)
	 * void fillDeleteRunContent(DataRuntime runtime, Run run)
	 * [命令执行]
	 * long delete(DataRuntime runtime, String random, ConfigStore configs, Run run)
	 ******************************************************************************************************************/
	/**
	 * delete [调用入口]
	 * <br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String key, Collection<T> values){
		return super.deletes(runtime, random,  batch, table, configs, key, values);
	}

	/**
	 * delete [调用入口]
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String... columns){
		return super.delete(runtime, random,  dest, configs, obj, columns);
	}

	/**
	 * delete [调用入口]
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
	public long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions){
		return super.delete(runtime, random,  table, configs, conditions);
	}

	/**
	 * truncate [调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	@Override
	public long truncate(DataRuntime runtime, String random, String table){
		return super.truncate(runtime, random,  table);
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
	public Run buildDeleteRun(DataRuntime runtime, String dest, Object obj, String ... columns){
		return super.buildDeleteRun(runtime, dest,  obj, columns);
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
	public Run buildDeleteRun(DataRuntime runtime, int batch, String table, String key, Object values){
		return super.buildDeleteRun(runtime, batch, table, key, values);
	}

	@Override
	public List<Run> buildTruncateRun(DataRuntime runtime, String table){
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("TRUNCATE TABLE ");
		delimiter(builder, table);
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
	public Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, String key, Object values) {
		if(null == table || null == key || null == values){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		TableRun run = new TableRun(runtime, table);
		builder.append("DELETE FROM ");
		delimiter(builder, table);
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
	public Run buildDeleteRunFromEntity(DataRuntime runtime, String table, Object obj, String... columns) {
		TableRun run = new TableRun(runtime, table);
		run.setFrom(2);
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ");
		delimiter(builder, parseTable(table));
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
		if(null != run.getSchema()){
			delimiter(builder, run.getSchema()).append(".");
		}

		delimiter(builder, run.getTable());
		builder.append(BR);
		if(BasicUtil.isNotEmpty(prepare.getAlias())){
			// builder.append(" AS ").append(sql.getAlias());
			builder.append("  ").append(prepare.getAlias());
		}
		List<Join> joins = prepare.getJoins();
		if(null != joins) {
			for (Join join:joins) {
				builder.append(BR_TAB).append(join.getType().getCode()).append(" ");
				if(null != join.getSchema()){
					delimiter(builder, join.getSchema()).append(".");
				}
				delimiter(builder, join.getName());
				if(BasicUtil.isNotEmpty(join.getAlias())){
					builder.append("  ").append(join.getAlias());
				}
				builder.append(" ON ").append(join.getCondition());
			}
		}

		builder.append("\nWHERE 1=1\n\t");

		/*添加查询条件*/
		// appendConfigStore();
		run.appendCondition();
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
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库(catalog, schema)
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
	 * 根据sql获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param comment 是否需要查询列注释
	 * @return LinkedHashMap
	 */
	public LinkedHashMap<String,Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment){
		LinkedHashMap<String,Column> columns = null;
		JdbcTemplate jdbc =jdbc(runtime);
		String random = random(runtime);
		Run run = buildQueryRun(runtime, prepare, null, null);
		String sql = run.getFinalQuery(false);
		long fr = System.currentTimeMillis();
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[action:metadata][sql:\n{}\n]", random, sql);
		}
		SqlRowSet rs = jdbc.queryForRowSet(sql);
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[action:metadata][执行耗时:{}ms]", random,  System.currentTimeMillis() - fr);
		}
		fr = System.currentTimeMillis();
		try {
			columns = columns(runtime, true, null, null, rs);
			if(comment){
				Map<String,Table> tables = new HashMap<>();
				for(Column column:columns.values()){
					Table table = column.getTable(false);
					if(null != table && BasicUtil.isNotEmpty(table.getName()) && !tables.containsKey(table.getIdentity())){
						tables.put(table.getIdentity(), table);
					}
				}
				//提取所有表名和列名的别名
				//解析一层
				String col_sql = sql.toUpperCase().split("FROM")[0];
				List<String> chks = RegularUtil.fetch(col_sql,"\\S+\\s+AS\\s+\\S+");
				for(String col:chks){
					String[] tmps =col.split("AS");
					String original = tmps[0];
					String label = tmps[1];
					if(original.contains(".")){
						String[] names = original.split("\\.");
						String table = names[1];
						original = names[1];
					}
					original = original.trim();
					label = label.trim();
					Column column = columns.get(label);
					if(null != column){
						column.setOriginalName(original);
					}
				}
				//TODO JDBC没有返回列.表名的 解析SQL确认表与列的关系
				//mssql 列元数据中 不返回 表名
				if(tables.isEmpty()){
					List<String> tmps = RegularUtil.fetch(sql, "(\\s+FROM\\s+\\S+)|(\\s+JOIN\\s+\\S+)");
					for(String tmp:tmps){
						String name = tmp.trim().split("\\s+")[1].trim();
						tables.put(name.toUpperCase(), new Table(name));
					}
				}
				for(Table table:tables.values()){
					LinkedHashMap<String,Column> ccols = columns(runtime, random, false, table, false);
					for(Column ccol:ccols.values()){
						String name = ccol.getName();
						for(Column column:columns.values()){
							if(column.getTableName(false).equals(ccol.getTableName(false))){
								String label = column.getName();
								String original = column.getOriginalName();
								if(name.equalsIgnoreCase(label) || name.equalsIgnoreCase(original)){
									column.setComment(ccol.getComment());
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			columns = new LinkedHashMap<>();
			e.printStackTrace();
		}
		log.info("{}[action:metadata][封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - fr, LogUtil.format(columns.size(), 34));
		return columns;
	}
	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
	 * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
	 * Database database(DataRuntime runtime, String random, String name)
	 * [命令合成]
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)
	 * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set)
	 * Database database(DataRuntime runtime, int index, boolean create, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * database[调用入口]
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
	 * database[调用入口]
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
	 * 查询所有数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name) throws Exception{
		return super.buildQueryDatabaseRun(runtime, greedy, name);
	}
	/**
	 * database[结果集封装]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception{
		return super.databases(runtime, index, create, databases, set);
	}
	@Override
	public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set) throws Exception{
		return super.databases(runtime, index, create, databases, set);
	}
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		return super.database(runtime, index, create, set);
	}

	/* *****************************************************************************************************************
	 * 													catalog
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name)
	 * List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
	 * [命令合成]
	 * List<Run> buildQueryCatalogRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * catalog[调用入口]
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
	 * catalog[调用入口]
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
	 * 查询所有数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogRun(DataRuntime runtime, boolean greedy, String name) throws Exception{
		return super.buildQueryCatalogRun(runtime, greedy, name);
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
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set) throws Exception{
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
	public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set) throws Exception{
		return super.catalogs(runtime, index, create, catalogs, set);
	}/**
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
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs) throws Exception {
		return super.catalogs(runtime, create, catalogs);
	}
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		return super.catalog(runtime, index, create, set);
	}


	/* *****************************************************************************************************************
	 * 													schema
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name)
	 * List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name)
	 * [命令合成]
	 * List<Run> buildQuerySchemaRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)
	 * [结果集封装]
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set)
	 * List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * schema[调用入口]
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
	 * schema[调用入口]
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
	 * 查询所有数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name 名称统配符或正则
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @return sqls
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemaRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception{
		return super.buildQuerySchemaRun(runtime, greedy, catalog, name);
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
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set) throws Exception{
		return super.schemas(runtime, index, create, schemas, set);
	}
	@Override
	public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set) throws Exception{
		return super.schemas(runtime, index, create, schemas, set);
	}
	@Override
	public Schema schema(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		return super.schema(runtime, index, create, set);
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types, boolean strut)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean strut)
	 * [命令合成]
	 * List<Run> buildQueryTableRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, String types)
	 * List<Run> buildQueryTableCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)
	 * [结果集封装]
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types)
	 * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, String ... types)
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Table table, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, Table table)
	 * [结果集封装]
	 * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/

	/**
	 *
	 * table[调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @param strut 是否查询表结构
	 * @return List
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types, boolean strut){
		return super.tables(runtime, random, greedy, catalog, schema, pattern, types, strut);
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
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean strut){
		return super.tables(runtime, random, catalog, schema, pattern, types, strut);
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		return super.buildQueryTableRun(runtime, greedy, catalog, schema, pattern, types);
	}


	/**
	 * table[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		return super.buildQueryTableCommentRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * table[结果集封装] <br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String _catalog = row.getString("TABLE_CATALOG");
			String _schema = row.getString("TABLE_SCHEMA", "TABSCHEMA", "SCHEMA_NAME");
			if(null == _catalog && null != catalog){
				_catalog = catalog.getName();
			}
			if(null == _schema && null != schema){
				_schema = schema.getName();
			}
			String name = row.getString("TABLE_NAME", "NAME", "TABNAME");
			if(null == name){
				continue;
			}
			T table = tables.get(name.toUpperCase());
			if(null == table){
				if("VIEW".equals(row.getString("TABLE_TYPE"))){
					table = (T) new View();
				}else {
					table = (T) new Table();
				}
			}
			if(null != _catalog){
				_catalog = _catalog.trim();
			}
			if(null != _schema){
				_schema = _schema.trim();
			}
			table.setCatalog(_catalog);
			table.setSchema(_schema);
			table.setName(name);
			table.setObjectId(row.getLong("OBJECT_ID", (Long)null));
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT", "COMMENTS", "COMMENT"));
			tables.put(name.toUpperCase(), table);
		}
		return tables;
	}

	/**
	 * table[结果集封装] <br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new ArrayList<>();
		}
		for(DataRow row:set){
			String _catalog = row.getString("TABLE_CATALOG");
			String _schema = row.getString("TABLE_SCHEMA", "TABSCHEMA", "SCHEMA_NAME");
			if(null == _catalog && null != catalog){
				_catalog = catalog.getName();
			}
			if(null == _schema && null != schema){
				_schema = schema.getName();
			}
			String name = row.getString("TABLE_NAME", "NAME", "TABNAME");
			T table = table(tables, new Catalog(_catalog), new Schema(_schema), name);
			boolean conains = true;
			if(null == table){
				conains = false;
				if("VIEW".equals(row.getString("TABLE_TYPE"))){
					table = (T)new View();
				}else {
					table = (T) new Table();
				}
			}
			if(null != _catalog){
				_catalog = _catalog.trim();
			}
			if(null != _schema){
				_schema = _schema.trim();
			}
			table.setCatalog(_catalog);
			table.setSchema(_schema);
			table.setName(name);
			table.setObjectId(row.getLong("OBJECT_ID", (Long)null));
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT", "COMMENTS", "COMMENT"));
			if(!conains) {
				tables.add(table);
			}
		}
		return tables;
	}
	/**
	 * table[结果集封装] <br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return tables
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			String catalogName = null;
			String schemaName = null;
			if(null != catalog){
				catalogName = catalog.getName();
			}
			if(null != schema){
				schemaName = schema.getName();
			}
			ResultSet set = dbmd.getTables(catalogName, schemaName, pattern, types);
			if(null == tables){
				tables = new LinkedHashMap<>();
			}
			Map<String,Integer> keys = keys(set);
			while(set.next()) {
				String tableName = string(keys, "TABLE_NAME", set);

				if(BasicUtil.isEmpty(tableName)){
					tableName = string(keys, "NAME", set);
				}
				if(BasicUtil.isEmpty(tableName)){
					continue;
				}
				T table = tables.get(tableName.toUpperCase());
				if(null == table){
					if(create){
						table = (T)new Table();
						tables.put(tableName.toUpperCase(), table);
					}else{
						continue;
					}
				}
				table.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalogName));
				table.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schemaName));
				table.setName(tableName);
				table.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), table.getType()));
				table.setComment(BasicUtil.evl(string(keys, "REMARKS", set), table.getComment()));
				table.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), table.getTypeCat()));
				table.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), table.getTypeName()));
				table.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), table.getSelfReferencingColumn()));
				table.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), table.getRefGeneration()));
				tables.put(tableName.toUpperCase(), table);

				// table_map.put(table.getType().toUpperCase()+"_"+tableName.toUpperCase(), tableName);
			}
		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			String catalogName = null;
			String schemaName = null;
			if(null != catalog){
				catalogName = catalog.getName();
			}
			if(null != schema){
				schemaName = schema.getName();
			}

			ResultSet set = dbmd.getTables(catalogName, schemaName, pattern, types);
			if(null == tables){
				tables = new ArrayList<>();
			}
			Map<String,Integer> keys = keys(set);
			while(set.next()) {
				String tableName = string(keys, "TABLE_NAME", set);

				if(BasicUtil.isEmpty(tableName)){
					tableName = string(keys, "NAME", set);
				}
				if(BasicUtil.isEmpty(tableName)){
					continue;
				}
				catalogName = BasicUtil.evl(string(keys, "TABLE_CATALOG", set), string(keys, "TABLE_CAT", set), catalogName);
				schemaName = BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schemaName);
				T table = table(tables, catalog, schema, tableName);
				boolean contains = true;
				if(null == table){
					if(create){
						table = (T)new Table();
						contains = false;
					}else{
						continue;
					}
				}
				table.setCatalog(catalog);
				table.setSchema(schema);
				table.setName(tableName);
				table.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), table.getType()));
				table.setComment(BasicUtil.evl(string(keys, "REMARKS", set), table.getComment()));
				table.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), table.getTypeCat()));
				table.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), table.getTypeName()));
				table.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), table.getSelfReferencingColumn()));
				table.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), table.getRefGeneration()));
				if(!contains) {
					tables.add(table);
				}
			}
		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("TABLE_NAME");
			String comment = row.getString("TABLE_COMMENT");
			if(null != name && null != comment){
				Table table = tables.get(name.toUpperCase());
				if(null != table){
					table.setComment(comment);
				}
			}
		}
		return tables;
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new ArrayList<>();
		}
		for(DataRow row:set){
			String name = row.getString("TABLE_NAME");
			String comment = row.getString("TABLE_COMMENT");
			if(null == catalog){
				catalog = new Catalog(row.getString("TABLE_CATALOG"));
			}
			if(null == schema){
				schema = new Schema(row.getString("TABLE_SCHEMA"));
			}

			boolean contains = true;
			T table = table(tables, catalog, schema, name);
			if (null == table) {
				if (create) {
					table = (T) new Table(catalog, schema, name);
					contains = false;
				} else {
					continue;
				}
			}
			table.setComment(comment);
			if (!contains) {
				tables.add(table);
			}
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildQueryDDLRun(runtime, table);
	}

	/**
	 * table[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, table, ddls, set);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types)
	 * [命令合成]
	 * List<Run> buildQueryViewRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, String types)
	 * [结果集封装]
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> views, DataSet set)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, String ... types)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, View view)
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, View view)
	 * [结果集封装]
	 * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/


	/**
	 * view[调用入口]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types  "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return List
	 * @param <T> View
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types){
		return super.views(runtime, random, greedy, catalog, schema, pattern, types);
	}
	/**
	 * view[命令合成]<br/>
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return List
	 */
	@Override
	public List<Run> buildQueryViewRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		return super.buildQueryViewRun(runtime, greedy, catalog, schema, pattern, types);
	}


	/**
	 * view[结果集封装]<br/>
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param views 上一步查询结果
	 * @param set 查询结果集
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> views, DataSet set) throws Exception{
		return super.views(runtime, index, create, catalog, schema, views, set);
	}
	/**
	 * view[结果集封装]<br/>
	 * 根据根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try {
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();

			String catalogName = null;
			String schemaName = null;
			if(null != catalog){
				catalogName = catalog.getName();
			}
			if(null != schema){
				schemaName = schema.getName();
			}
			ResultSet set = dbmd.getTables(catalogName, schemaName, pattern, new String[]{"VIEW"});

			if (null == views) {
				views = new LinkedHashMap<>();
			}
			Map<String, Integer> keys = keys(set);
			while (set.next()) {
				String viewName = string(keys, "VIEW_NAME", set);

				if (BasicUtil.isEmpty(viewName)) {
					viewName = string(keys, "NAME", set);
				}
				if (BasicUtil.isEmpty(viewName)) {
					viewName = string(keys, "TABLE_NAME", set);
				}
				if (BasicUtil.isEmpty(viewName)) {
					continue;
				}
				T view = views.get(viewName.toUpperCase());
				if (null == view) {
					if (create) {
						view = (T) new View();
						views.put(viewName.toUpperCase(), view);
					} else {
						continue;
					}
				}
				view.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalogName));
				view.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schemaName));
				view.setName(viewName);
				view.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), view.getType()));
				view.setComment(BasicUtil.evl(string(keys, "REMARKS", set), view.getComment()));
				view.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), view.getTypeCat()));
				view.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), view.getTypeName()));
				view.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), view.getSelfReferencingColumn()));
				view.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), view.getRefGeneration()));
				views.put(viewName.toUpperCase(), view);
			}
		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return  views;
	}

	/**
	 * view[调用入口]
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, View view) throws Exception{
		return super.buildQueryDDLRun(runtime, view);
	}

	/**
	 * view[结果集封装]<br/>
	 * 查询 view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
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
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types)
	 * [命令合成]
	 * List<Run> buildQueryMasterTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)
	 * [结果集封装]
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * [结果集封装]
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, MasterTable table)
	 * [结果集封装]
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
	 * @param types  "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types){
		return super.mtables(runtime, random, greedy, catalog, schema, pattern, types);
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
	public List<Run> buildQueryMasterTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		return super.buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * master table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.mtables(runtime, index, create, catalog, schema, tables, set);
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
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types) throws Exception{
		return super.mtables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/**
	 * master table[调用入口]
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildQueryDDLRun(runtime, table);
	}
	/**
	 * master table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
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
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String pattern)
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags)
	 * [结果集封装]
	 * <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, PartitionTable table)
	 * [结果集封装]
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
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern){
		return super.ptables(runtime, random, greedy, master, tags, pattern);
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
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, catalog, schema, pattern, types);
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
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name) throws Exception{
		return super.buildQueryPartitionTableRun(runtime,  master, tags, name);
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
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags) throws Exception{
		return super.buildQueryPartitionTableRun(runtime,  master, tags);
	}
	/**
	 * partition table[结果集封装]<br/>
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
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
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.ptables(runtime, total, index, create, master, catalog, schema, tables, set);
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
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception{
		return super.ptables(runtime, create, tables, catalog, schema, master);
	}
	/**
	 * partition table[调用入口]
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildQueryDDLRun(runtime, table);
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
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
	 * <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table);
	 * [命令合成]
	 * List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	 * [结果集封装]
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

		LinkedHashMap<String,T> columns = CacheProxy.columns(runtime.getKey(), table);
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
				List<Run> runs = buildQueryColumnRun(runtime, table, false);
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
				if(primary) {
					e.printStackTrace();
				} if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			// 根据驱动内置接口补充
			// 再根据metadata解析 SELECT * FROM T WHERE 1=0
			if (null == columns || columns.size() == 0) {
				try {
					List<Run> runs = buildQueryColumnRun(runtime, table, true);
					if (null != runs) {
						for (Run run  : runs) {
							SqlRowSet set = ((JDBCRuntime)runtime).jdbc().queryForRowSet(run.getFinalQuery());
							columns = columns(runtime, true, columns, table, set);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
					}
				}
				if(null != columns) {
					qty_metadata = columns.size() - qty_dialect;
					qty_total = columns.size();
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}

			// 方法(3)根据根据驱动内置接口补充

			if (null == columns || columns.size() == 0) {
				DataSource ds = null;
				Connection con = null;
				DatabaseMetaData metadata = null;
				try {
					ds = ((JDBCRuntime)runtime).jdbc().getDataSource();
					con = DataSourceUtils.getConnection(ds);
					metadata = con.getMetaData();
					columns = columns(runtime, true, columns, metadata, table, null);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}finally {
					if (!DataSourceUtils.isConnectionTransactional(con, ds)) {
						DataSourceUtils.releaseConnection(con, ds);
					}
				}

				if(null != columns) {
					qty_total = columns.size();
					qty_jdbc = columns.size() - qty_metadata - qty_dialect;
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据根据驱动内置接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}
			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (null != columns || columns.size() > 0) {
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
				log.error("[columns][result:fail][table:{}][msg:{}]", random, table, e.toString());
			}
		}
		if(null != columns) {
			CacheProxy.columns(runtime.getKey(), table, columns);
		}else{
			columns = new LinkedHashMap<>();
		}
		return columns;
	}

	/**
	 * column[调用入口]<br/>
	 * 查询所有表的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 查询所有表时 输入null
	 * @return List
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table){
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
	public List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		Catalog catalog = null;
		Schema schema = null;
		String name = null;
		if(null != table){
			name = table.getName();
			catalog = table.getCatalog();
			schema = table.getSchema();
		}
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(runtime, builder, table);
			builder.append(" WHERE 1=0");
		}
		return runs;
	}

	/**
	 * column[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 查询结果集
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("COLUMN_NAME", "COLNAME");
			T column = columns.get(name.toUpperCase());
			if(null == column){
				column = (T)new Column();
			}
			column.setName(name);
			init(column, table, row);
			columns.put(name.toUpperCase(), column);
		}
		return columns;
	}
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new ArrayList<>();
		}
		for(DataRow row:set){
			String name = row.getString("COLUMN_NAME", "COLNAME");
			T tmp = (T)new Column();
			tmp.setName(name);
			init(tmp, table, row);
			T column = column(tmp, columns);
			if(null == column) {
				column = (T) new Column();
				column.setName(name);
				init(column, table, row);
				columns.add(column);
			}
		}
		return columns;
	}

	/**
	 * column[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @return pattern attern
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		DataSource ds = null;
		Connection con = null;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);

			String catalog = table.getCatalogName();
			String schema = table.getSchemaName();
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getColumns(catalog, schema, table.getName(), pattern);
			Map<String,Integer> keys = keys(set);
			while (set.next()){
				String name = set.getString("COLUMN_NAME");
				if(null == name){
					continue;
				}
				String columnCatalog = string(keys,"TABLE_CAT", set, null);
				if(null != columnCatalog){
					columnCatalog = columnCatalog.trim();
				}
				String columnSchema = string(keys,"TABLE_SCHEM", set, null);
				if(null != columnSchema){
					columnSchema = columnSchema.trim();
				}
				if(!BasicUtil.equalsIgnoreCase(catalog, columnCatalog)){
					continue;
				}
				if(!BasicUtil.equalsIgnoreCase(schema, columnSchema)){
					continue;
				}
				T column = columns.get(name.toUpperCase());
				if(null == column){
					if(create) {
						column = (T)new Column(name);
						columns.put(name.toUpperCase(), column);
					}else {
						continue;
					}
				}
				String remark = string(keys, "REMARKS", set, column.getComment());
				if("TAG".equals(remark)){
					column = (T)new Tag();
				}
				column.setCatalog(columnCatalog);
				column.setSchema(columnSchema);
				column.setComment(remark);
				column.setTable(BasicUtil.evl(string(keys,"TABLE_NAME", set, table.getName()), column.getTableName(true)));
				column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
				column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
				String jdbcType = string(keys, "TYPE_NAME", set, column.getTypeName());
				if(BasicUtil.isEmpty(column.getTypeName())) {
					//数据库中 有jdbc是支持的类型 如果数据库中有了就不用jdbc的了
					column.setTypeName(jdbcType);
				}
				column.setJdbcType(jdbcType);
				column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
				column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
				column.nullable(bool(keys, "NULLABLE", set, column.isNullable()));
				column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
				column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
				column.autoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
				ColumnType columnType = type(column.getTypeName());
				column.setColumnType(columnType);
				column(runtime, column, set);
				column.setName(name);
			}
			// 主键
			ResultSet rs = dbmd.getPrimaryKeys(table.getCatalogName(), table.getSchemaName(), table.getName());
			while (rs.next()) {
				String name = rs.getString(4);
				Column column = columns.get(name.toUpperCase());
				if (null == column) {
					continue;
				}
				column.primary(true);
			}
		}catch (Exception e){

		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return columns;
	}




	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table)
	 * [命令合成]
	 * List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata)
	 * [结果集封装]
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return Tag
	 * @param <T>  Tag
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table){
		if(null == table || BasicUtil.isEmpty(table.getName())){
			return new LinkedHashMap();
		}
		LinkedHashMap<String,T> tags = CacheProxy.tags(runtime.getKey(), table);
		if(null != tags && !tags.isEmpty()){
			return tags;
		}

		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {
			if (!greedy) {
				checkSchema(runtime, table);
			}
			Catalog catalog = table.getCatalog();
			Schema schema = table.getSchema();

			// 先根据系统表查询
			try {
				List<Run> runs = buildQueryTagRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tags = tags(runtime, idx, true, table, tags, set);
						idx++;
					}
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
				if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			if (null == tags || tags.size() == 0) {
				// 根据驱动内置接口补充
				try {
					// isAutoIncrement isGenerated remark default
					// 这一步会查出所有列(包括非tag列)
					tags = tags(runtime, false, tags, table, null);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}

			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tags][catalog:{}][schema:{}][table:{}][执行耗时:{}ms]", random, catalog, schema, table, System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{

			}
		}
		CacheProxy.tags(runtime.getKey(), table, tags);
		return tags;
	}
	/**
	 * tag[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		return super.buildQueryTagRun(runtime, table, metadata);
	}

	/**
	 * tag[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果集
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
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
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception{
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
	 * PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set)
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
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildQueryPrimaryRun(runtime, table);
	}

	/**
	 * primary[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception{
		return super.primary(runtime, index, table, set);
	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	 * [命令合成]
	 * List<Run> buildQueryForeignRun(DataRuntime runtime, Table table) throws Exception;
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
	public List<Run> buildQueryForeignRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildQueryForeignRun(runtime, table);
	}
	/**
	 * foreign[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
		return super.foreigns(runtime, index, table, foreigns, set);
	}



	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<T, Index> indexs(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name)
	 * [结果集封装]
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
	public List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name){
		return super.buildQueryIndexRun(runtime, table, name);
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set 查询结果集
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception{
		return super.indexs(runtime, index, create, table, indexs, set);
	}
	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set 查询结果集
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set) throws Exception{
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
	public <T extends Index> List<T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate) throws Exception{
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
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception{
		DataSource ds = null;
		Connection con = null;
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		JdbcTemplate jdbc = jdbc(runtime);
		try{
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getIndexInfo(table.getCatalogName(), table.getSchemaName(), table.getName(), unique, approximate);
			Map<String, Integer> keys = keys(set);
			LinkedHashMap<String, Column> columns = null;
			while (set.next()) {
				String name = string(keys, "INDEX_NAME", set);
				if(null == name){
					continue;
				}
				T index = indexs.get(name.toUpperCase());
				if(null == index){
					if(create){
						index = (T)new Index();
						indexs.put(name.toUpperCase(), index);
					}else{
						continue;
					}
					index.setName(string(keys, "INDEX_NAME", set));
					//index.setType(integer(keys, "TYPE", set, null));
					index.setUnique(!bool(keys, "NON_UNIQUE", set, false));
					index.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), table.getCatalogName()));
					index.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), table.getSchemaName()));
					index.setTable(string(keys, "TABLE_NAME", set));
					indexs.put(name.toUpperCase(), index);
					columns = new LinkedHashMap<>();
					index.setColumns(columns);
					if(name.equalsIgnoreCase("PRIMARY")){
						index.setCluster(true);
						index.setPrimary(true);
					}else if(name.equalsIgnoreCase("PK_"+table.getName())){
						index.setCluster(true);
						index.setPrimary(true);
					}
				}else {
					columns = index.getColumns();
				}
				String columnName = string(keys, "COLUMN_NAME", set);
				Column col = table.getColumn(columnName.toUpperCase());
				Column column = null;
				if(null != col){
					column = (Column) col.clone();
				}else{
					column = new Column();
					column.setName(columnName);
				}
				String order = string(keys, "ASC_OR_DESC", set);
				if(null != order && order.startsWith("D")){
					order = "DESC";
				}else{
					order = "ASC";
				}
				column.setOrder(order);
				column.setPosition(integer(keys,"ORDINAL_POSITION", set, null));
				columns.put(column.getName().toUpperCase(), column);
			}
		}finally{
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return indexs;
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, Column column, String pattern) ;
	 * [结果集封装]
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
		List<Run> runs = buildQueryConstraintRun(runtime, table, null, pattern);
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
		return super.constraints(runtime, random, table, column, pattern);
	}

	/**
	 * constraint[命令合成]
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param pattern 名称通配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, Column column, String pattern) {
		return super.buildQueryConstraintRun(runtime, table, column, pattern);
	}

	/**
	 * constraint[结果集封装]
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception{
		return super.constraints(runtime, index, create, table, constraints, set);
	}
	/**
	 * constraint[结果集封装]
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param column 列
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception{
		return super.constraints(runtime, index, create, table, column, constraints, set);
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
	 * [命令合成]
	 * List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
	 * [结果集封装]
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
	 * trigger[命令合成]
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	public List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events){
		return super.buildQueryTriggerRun(runtime, table, events);
	}
	/**
	 * trigger[结果集封装]
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
		return super.triggers(runtime, index, create, table, triggers, set);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryProcedureRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * [结果集封装]
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

		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryProcedureRun(runtime, catalog, schema, pattern);
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

		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryProcedureRun(runtime, catalog, schema, pattern);
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
	 * procedure[命令合成]
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryProcedureRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		return super.buildQueryProcedureRun(runtime, catalog, schema, pattern);
	}
	/**
	 * procedure[结果集封装]
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param procedures 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception{
		return super.procedures(runtime, index, create, procedures, set);
	}

	/**
	 * procedure[结果集封装]
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
	 * procedure[结果集封装]
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildQueryDDLRun(runtime, procedure);
	}

	/**
	 * procedure[结果集封装]
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
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
	 * List<Run> buildQueryFunctionRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]
	 * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Function function);
	 * [命令合成]
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, Function function) throws Exception;
	 * [结果集封装]
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

		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryFunctionRun(runtime, catalog, schema, pattern);
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
		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryFunctionRun(runtime, catalog, schema, pattern);
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
	 * function[命令合成]
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param name 名称统配符或正则
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryFunctionRun(DataRuntime runtime, Catalog catalog, Schema schema, String name) {
		return super.buildQueryFunctionRun(runtime, catalog, schema, name);
	}

	/**
	 * function[结果集封装]
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception{
		return super.functions(runtime, index, create, functions, set);
	}
	/**
	 * function[结果集封装]
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception{
		return super.functions(runtime, index, create, functions, set);
	}

	/**
	 * function[结果集封装]
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
	public List<Run> buildQueryDDLRun(DataRuntime runtime, Function meta) throws Exception{
		return super.buildQueryDDLRun(runtime, meta);
	}
	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param function Function
	 * @param ddls 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set){
		return super.ddl(runtime, index, function, ddls, set);
	}

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
				update(runtime, random, null, null, null, run);
				CacheProxy.clear();
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
	public boolean create(DataRuntime runtime, Table meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Table meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, Table meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}


	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
	@Override
	public  String keyword(Table meta){
		return meta.getKeyword();
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
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		//builder.append("CREATE ").append(keyword(table)).append(" ");
		//这时可能是MasterTable(pg)
		builder.append("CREATE TABLE ");
		checkTableExists(runtime, builder, false);
		name(runtime, builder, meta);
		//分区表
		Table master = meta.getMaster();
		if(null != master){
			partitionOf(runtime, builder, meta);
		}
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
		}
		if(null != columMap){
			columns = columMap.values();
			if(null != columns && columns.size() >0){
				builder.append("(\n");
				int idx = 0;
				for(Column column:columns){
					builder.append("\t");
					if(idx > 0){
						builder.append(",");
					}
					column.setAction(ACTION.DDL.COLUMN_ADD);
					delimiter(builder, column.getName()).append(" ");
					define(runtime, builder, column).append("\n");
					idx ++;
				}
				builder.append("\t");
				if(!pks.isEmpty()) {
					primary(runtime, builder, meta);
				}
				builder.append(")");
			}
		}
		//分区依据 PARTITION BY RANGE (code);
		partitionBy(runtime, builder, meta);
		//继承表
		if(BasicUtil.isNotEmpty(meta.getInherits())){
			builder.append(" INHERITS(");
			name(runtime, builder, meta.getInherits());
			builder.append(")");
		}
		//CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='备注';
		charset(runtime, builder, meta);
		comment(runtime, builder, meta);
		List<Run> tableComment = buildAppendCommentRun(runtime, meta);
		if(null != tableComment) {
			runs.addAll(tableComment);
		}
		if(null != columns){
			for(Column column:columns){
				List<Run> columnComment = buildAppendCommentRun(runtime, column);
				if(null != columnComment){
					runs.addAll(columnComment);
				}
			}
		}
		if(null != primary){
			List<Run> pksql = buildAddRunAfterTable(runtime, primary);
			if(null != pksql){
				runs.addAll(pksql);
			}
		}
		LinkedHashMap<String,Index> indexs = meta.getIndexs();
		if(null != indexs){
			for(Index index:indexs.values()){
				//创建表过程已添加过主键，这里不重复添加
				if(!index.isPrimary()) {
					runs.addAll(buildAddRun(runtime, index));
				}
			}
		}
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
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns) throws Exception{
		return super.buildAlterRun(runtime, table, columns);
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
	public List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		builder.append("DROP ").append(keyword(meta)).append(" ");
		checkTableExists(runtime, builder, true);
		name(runtime, builder, meta);
		return runs;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception{
		return super.buildAppendCommentRun(runtime, meta);
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta) throws Exception{
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
		if(!pks.isEmpty()){
			builder.append(",PRIMARY KEY (");
			boolean first = true;
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
	 * 备注
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
	 * 主表设置分区依据(根据哪几列分区)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception{
		// PARTITION BY RANGE (code); #根据code值分区
		Partition partition = meta.getPartitionBy();
		if(null != partition) {
			builder.append(" PARTITION BY ").append(partition.getType()).append("(");
			LinkedHashMap<String, Column> columns = partition.getColumns();
			int idx = 0;
			for (Column column : columns.values()) {
				if (idx > 0) {
					builder.append(",");
				}
				delimiter(builder, column.getName());
				idx++;
			}
			builder.append(")");
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表及分区值)
	 * 如CREATE TABLE hr_user_hr PARTITION OF hr_user FOR VALUES IN ('HR')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception{
		//CREATE TABLE partition_name2 PARTITION OF main_table_name FOR VALUES FROM (100) TO (199);
		//CREATE TABLE emp_0 PARTITION OF emp FOR VALUES WITH (MODULUS 3,REMAINDER 0);
		//CREATE TABLE hr_user_1 PARTITION OF hr_user FOR VALUES IN ('HR');
		builder.append(" PARTITION OF ");
		Table master = meta.getMaster();
		if(null == master){
			throw new SQLException("未提供 Master Table");
		}
		name(runtime, builder, master);
		builder.append(" FOR VALUES");
		Partition partition = meta.getPartitionFor();
		Partition.TYPE type = partition.getType();
		if(null == type && null != master.getPartitionBy()){
			type = master.getPartitionBy().getType();
		}
		if(type == Partition.TYPE.LIST){
			List<Object> list = partition.getList();
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
		}else if(type == Partition.TYPE.RANGE){
			Object from = partition.getFrom();
			Object to = partition.getTo();
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
		}else if(type == Partition.TYPE.HASH){
			int modulus = partition.getModulus();
			if(modulus == 0){
				throw new SQLException("未提供分区表MODULUS");
			}
			builder.append(" WITH(MODULUS ").append(modulus).append(",REMAINDER ").append(partition.getRemainder()).append(")");
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
	public boolean create(DataRuntime runtime, View meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, View meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, View meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception{
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
	public List<Run> buildCreateRun(DataRuntime runtime, View meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE VIEW ");
		name(runtime, builder, meta);
		builder.append(" AS \n").append(meta.getDefinition());

		runs.addAll(buildAppendCommentRun(runtime, meta));
		return runs;
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
	public List<Run> buildAlterRun(DataRuntime runtime, View meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, View meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, View meta) throws Exception{
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
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View meta) throws Exception{
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, View meta) throws Exception{
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
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception{
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
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta) throws Exception{
		return super.buildRenameRun(runtime, meta);
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta) throws Exception{
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
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception{
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
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta) throws Exception{
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception{
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
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column, String type, boolean isIgnorePrecision, boolean isIgnoreScale)
	 * boolean isIgnorePrecision(DataRuntime runtime, Column column)
	 * boolean isIgnoreScale(DataRuntime runtime, Column column)
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype)
	 * Boolean checkIgnoreScale(DataRuntime runtime, String datatype)
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
	public boolean add(DataRuntime runtime, Column meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception{
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
	public boolean alter(DataRuntime runtime, Column meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, Column meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception{
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
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice) {
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
	public List<Run> buildAddRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Column update = meta.getUpdate();
		if(null != update){
			if(null == update.getTable(false)){
				update.setTable(meta.getTable(false));
			}
			// 修改列名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
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
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(meta instanceof Tag){
			Tag tag = (Tag)meta;
			return buildDropRun(runtime, tag);
		}
		if(!slice) {
			Table table = meta.getTable(true);
			builder.append("ALTER ").append(keyword(table)).append(" ");
			name(runtime, builder, table);
		}
		dropColumnGuide(runtime, builder, meta);
		delimiter(builder, meta.getName());
		return runs;
	}

	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta) throws Exception{
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
		delimiter(builder, meta.getUpdate());
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
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta) throws Exception{
		return super.buildChangeCommentRun(runtime, meta);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta) throws Exception{
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
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta) throws Exception{
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
		// 数据类型
		type(runtime, builder, meta);
		// 编码
		charset(runtime, builder, meta);
		// 默认值
		defaultValue(runtime, builder, meta);
		// 非空
		nullable(runtime, builder, meta);
		//主键
		primary(runtime, builder, meta);
		// 递增
		if(meta.isPrimaryKey() == 1) {
			increment(runtime, builder, meta);
		}
		// 更新行事件
		onupdate(runtime, builder, meta);
		// 备注
		comment(runtime, builder, meta);
		// 位置
		position(runtime, builder, meta);
		return builder;
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
		boolean isIgnorePrecision = false;
		boolean isIgnoreScale = false;
		String typeName = meta.getTypeName();
		ColumnType type = type(typeName);
		if(null != type){
			if(!type.support()){
				throw new RuntimeException("数据类型不支持:"+typeName);
			}
			isIgnorePrecision = type.ignorePrecision();
			isIgnoreScale = type.ignoreScale();
			typeName = type.getName();
		}else{
			isIgnorePrecision = isIgnorePrecision(runtime, meta);
			isIgnoreScale = isIgnoreScale(runtime, meta);
		}
		return type(runtime, builder, meta, typeName, isIgnorePrecision, isIgnoreScale);
	}
	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param isIgnorePrecision 是否忽略长度
	 * @param isIgnoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, boolean isIgnorePrecision, boolean isIgnoreScale){
		if(null == builder){
			builder = new StringBuilder();
		}
		builder.append(type);
		if(!isIgnorePrecision) {
			Integer precision =  meta.getPrecision();
			if (null != precision) {
				if (precision > 0) {
					builder.append("(").append(precision);
					Integer scale = meta.getScale();
					if (null != scale && scale > 0 && !isIgnoreScale) {
						builder.append(",").append(scale);
					}
					builder.append(")");
				} else if (precision == -1) {
					builder.append("(max)");
				}
			}
		}
		String child = meta.getChildTypeName();
		Integer srid = meta.getSrid();
		if(null != child){
			builder.append("(");
			builder.append(child);
			if(null != srid){
				builder.append(",").append(srid);
			}
			builder.append(")");
		}
		if(meta.isArray()){
			builder.append("[]");
		}
		return builder;
	}


	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:是否忽略长度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean
	 */
	@Override
	public boolean isIgnorePrecision(DataRuntime runtime, Column meta) {
		ColumnType type = meta.getColumnType();
		if(null != type){
			return type.ignorePrecision();
		}
		String typeName = meta.getTypeName();
		if(null != typeName){
			String chk = typeName.toUpperCase();
			Boolean chkResult = checkIgnorePrecision(runtime, chk);
			if(null != chkResult){
				return chkResult;
			}
		}
		return false;
	}
	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:是否忽略精度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean
	 */
	@Override
	public boolean isIgnoreScale(DataRuntime runtime, Column meta) {
		ColumnType type = meta.getColumnType();
		if(null != type){
			return type.ignoreScale();
		}
		String name = meta.getTypeName();
		if(null != name){
			String chk = name.toUpperCase();
			Boolean chkResult = checkIgnoreScale(runtime, chk);
			if(null != chkResult){
				return chkResult;
			}
		}
		return false;
	}
	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:是否忽略长度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 列数据类型
	 * @return Boolean 检测不到时返回null
	 */
	@Override
	public Boolean checkIgnorePrecision(DataRuntime runtime, String type) {
		return super.checkIgnorePrecision(runtime, type);
	}
	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:是否忽略精度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 列数据类型
	 * @return Boolean 检测不到时返回null
	 */
	@Override
	public Boolean checkIgnoreScale(DataRuntime runtime, String type) {
		return super.checkIgnoreScale(runtime, type);
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
		if(meta.isNullable() == 0) {
			int nullable = meta.isNullable();
			if(nullable != -1) {
				if (nullable == 0) {
					builder.append(" NOT");
				}
				builder.append(" NULL");
			}
		}
		return builder;
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
		Object def = null;
		if(null != meta.getUpdate()){
			def = meta.getUpdate().getDefaultValue();
		}else {
			def = meta.getDefaultValue();
		}
		if(null != def) {
			builder.append(" DEFAULT ");
			//boolean isCharColumn = isCharColumn(runtime, column);
			SQL_BUILD_IN_VALUE val = checkDefaultBuildInValue(runtime, def);
			if(null != val){
				def = val;
			}
			if(def instanceof SQL_BUILD_IN_VALUE){
				String value = value(runtime, meta, (SQL_BUILD_IN_VALUE)def);
				if(null != value){
					builder.append(value);
				}
			}else {
				def = write(runtime, meta, def, false);
				if(null == def){
					def = meta.getDefaultValue();
				}
				//format(builder, def);
				builder.append(def);
			}
		}
		return builder;
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
	 * 列定义:递增列
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
	public boolean add(DataRuntime runtime, Tag meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception{
		return super.alter(runtime, table, meta);
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
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception{
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
	public List<Run> buildAddRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, Tag meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Tag update = meta.getUpdate();
		if(null != update){
			// 修改标签名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
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
	public List<Run> buildDropRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, Tag meta) throws Exception{
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
		delimiter(builder, meta.getUpdate());
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
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta) throws Exception{
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
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception{
		return super.alter(runtime, meta);
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
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}
	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta) throws Exception{
		return super.buildAddRun(runtime, meta);
	}
	/**
	 * primary[命令合成]<br/>
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}
	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, meta.getTable(true));
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
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey meta) throws Exception{
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
	 * foreign[调用入口]
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception{
		return super.add(runtime, meta);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception{
		return super.alter(runtime, meta);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception{
		return super.alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception{
		return super.drop(runtime, meta);
	}

	/**
	 * foreign[调用入口]
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}


	/**
	 * foreign[命令合成]
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Map<String,Column> columns = meta.getColumns();
		if(columns.size()>0) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, meta.getTable(true));
			builder.append(" ADD");
			if(BasicUtil.isNotEmpty(meta.getName())){
				builder.append(" CONSTRAINT ").append(meta.getName());
			}
			builder.append(" FOREIGN KEY (");
			boolean first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				delimiter(builder, column.getName());
				first = false;
			}
			builder.append(")");
			builder.append(" REFERENCES ").append(meta.getReference().getName()).append("(");
			first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				delimiter(builder, column.getReference());
				first = false;
			}
			builder.append(")");

		}
		return runs;
	}
	/**
	 * foreign[命令合成]
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
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * foreign[命令合成]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception{
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
	 * foreign[命令合成]
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception{
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
	 * List<Run> buildAddRun(DataRuntime runtime, Index meta)
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
	public boolean add(DataRuntime runtime, Index meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Index meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, Index meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception{
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
		builder.append(" INDEX ").append(name)
				.append(" ON ");//.append(index.getTableName(true));
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append("(");
		int qty = 0;
		Collection<Column> cols = meta.getColumns().values();
		for(Column column:cols){
			if(qty>0){
				builder.append(",");
			}
			delimiter(builder, column.getName());
			String order = column.getOrder();
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
	public List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}
	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception{
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
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception{
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
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception{
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
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception{
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
	public List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception{
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
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		Catalog catalog = meta.getCatalog();
		Schema schema = table.getSchema();
		builder.append("ALTER CONSTRAINT ");
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
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
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception{
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
	public boolean alter(DataRuntime runtime, Trigger meta) throws Exception{
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
	public boolean drop(DataRuntime runtime,  Trigger meta) throws Exception{
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
	public boolean rename(DataRuntime runtime,  Trigger origin, String name) throws Exception{
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
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception{
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
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception{
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
	public List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP TRIGGER ");
		Table table = meta.getTable(true);
		if(null != table) {
			Catalog catalog = table.getCatalog();
			Schema schema = table.getSchema();
			if (BasicUtil.isNotEmpty(catalog)) {
				delimiter(builder, catalog).append(".");
			}
			if (BasicUtil.isNotEmpty(schema)) {
				delimiter(builder, schema).append(".");
			}
		}
		delimiter(builder, meta.getName());
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
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		builder.append("ALTER TRIGGER ");
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate());

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
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Procedure meta)
	 * boolean alter(DataRuntime runtime, Procedure meta)
	 * boolean drop(DataRuntime runtime, Procedure meta)
	 * boolean rename(DataRuntime runtime, Procedure origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)
	 * [命令合成-子流程]
	 * StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)
	 ******************************************************************************************************************/

	/**
	 * procedure[调用入口]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Procedure meta) throws Exception{
		return super.create(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Procedure meta) throws Exception{
		return super.alter(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Procedure meta) throws Exception{
		return super.drop(runtime, meta);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 重命名存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 存储过程
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE PROCEDURE ");
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append("(\n");
		List<Parameter> ins = meta.getInputs();
		List<Parameter> outs = meta.getOutputs();
		boolean first = true;
		for(Parameter parameter:ins){
			if(parameter.isOutput()){
				continue;
			}
			if(!first){
				builder.append(",");
			}
			parameter(runtime, builder, parameter);
		}
		for(Parameter parameter:outs){
			if(!first){
				builder.append(",");
			}
			parameter(runtime, builder, parameter);
		}
		builder.append("\n)");
		String returnType = meta.getReturnType();
		if(BasicUtil.isNotEmpty(returnType)){
			builder.append(" RETURNS ").append(returnType);
		}
		builder.append("\n");
		builder.append(meta.getDefinition());
		return runs;
	}
	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP PROCEDURE ");
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		return runs;
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		builder.append("ALTER PROCEDURE ");
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate());
		return runs;
	}

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	@Override
	public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter){
		boolean in = parameter.isInput();
		boolean out = parameter.isOutput();
		if(in){
			builder.append("IN");
		}
		if(out){
			builder.append("OUT");
		}
		builder.append(" ").append(parameter.getName());
		ColumnType type = parameter.getColumnType();
		boolean isIgnorePrecision= type.ignorePrecision();
		boolean isIgnoreScale = type.ignoreScale();
		builder.append(type);
		if(!isIgnorePrecision) {
			Integer precision =  parameter.getPrecision();
			Integer scale = parameter.getScale();
			if (null != precision) {
				if (precision > 0) {
					builder.append("(").append(precision);
					if (null != scale && scale > 0 && !isIgnoreScale) {
						builder.append(",").append(scale);
					}
					builder.append(")");
				} else if (precision == -1) {
					builder.append("(max)");
				}
			}
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
	 * function[调用入口]
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Function meta) throws Exception{
		return super.create(runtime, meta);
	}

	/**
	 * function[调用入口]
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Function meta) throws Exception{
		return super.alter(runtime, meta);
	}

	/**
	 * function[调用入口]
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Function meta) throws Exception{
		return super.drop(runtime, meta);
	}

	/**
	 * function[调用入口]
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception{
		return super.rename(runtime, origin, name);
	}


	/**
	 * function[命令合成]
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception{
		return super.buildCreateRun(runtime, meta);
	}

	/**
	 * function[命令合成]
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception{
		return super.buildAlterRun(runtime, meta);
	}

	/**
	 * function[命令合成]
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP FUNCTION ");
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());

		return runs;
	}

	/**
	 * function[命令合成]
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		builder.append("ALTER FUNCTION ");
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog.getName()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());
		return runs;
	}


/***********************************************************************************************************************
 *
 * 													JDBC
 *
 **********************************************************************************************************************/
	/**
	 * insert[命令执行后]
	 * insert执行后 通过KeyHolder获取主键值赋值给data
	 * @param random log标记
	 * @param data data
	 * @param keyholder  keyholder
	 * @return boolean
	 */
	@Override
	public boolean identity(DataRuntime runtime, String random, Object data, ConfigStore configs, KeyHolder keyholder){
		try {
			if(null == keyholder){
				return false;
			}
			if(!IS_KEYHOLDER_IDENTITY(configs)){
				return false;
			}
			List<Map<String,Object>> keys = keyholder.getKeyList();
			String id_key = generatedKey();
			if(null == id_key && keys.size()>0){
				Map<String,Object> key = keys.get(0);
				id_key = key.keySet().iterator().next();
			}
			if(data instanceof Collection){
				//批量插入
				List<Object> ids = new ArrayList<>();
				Collection list = (Collection) data;
				//检测是否有主键值
				for(Object item:list){
					if(BasicUtil.isNotEmpty(true, getPrimaryValue(runtime, item))){
						//已经有主键值了
						return true;
					}
					break;
				}
				if(BasicUtil.isEmpty(id_key)){
					return false;
				}
				int i = 0;
				int data_size = list.size();
				if(list.size() == keys.size()) {
					for (Object item : list) {
						Map<String, Object> key = keys.get(i);
						Object id = key.get(id_key);
						ids.add(id);
						setPrimaryValue(item, id);
						i++;
					}
				}else{
					if(null != keys && keys.size() > 0) {
						Object last = keys.get(0).get(id_key);
						if (last instanceof Number) {
							Long num = BasicUtil.parseLong(last.toString(), null);
							if (null != num) {
								num = num - data_size + 1;
								for (Object item : list) {
									setPrimaryValue(item, num++);
								}
							}
						}
					}
				}
				if(IS_LOG_SQL(configs) && log.isWarnEnabled()) {
					log.warn("{}[exe insert][生成主键:{}]", random, ids);
				}
			}else{
				if(null != keys && keys.size() > 0) {
					if(BasicUtil.isEmpty(true, getPrimaryValue(runtime, data))){
						Object id = keys.get(0).get(id_key);
						setPrimaryValue(data, id);
						if (IS_LOG_SQL(configs) && log.isWarnEnabled()) {
							log.warn("{}[exe insert][生成主键:{}]", random, id);
						}
					}
				}
			}
		}catch (Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)) {
				log.warn("{}[exe insert][返回主键失败]", random);
			}
			return false;
		}
		return true;
	}

	/**
	 * column [结果集封装-子流程](方法1)<br/>
	 * 方法(1)内部遍历
	 * @param column
	 * @param table
	 * @param row
	 */
	protected void init(Column column, Table table, DataRow row){
		String catalog = BasicUtil.evl(row.getString("TABLE_CATALOG"), column.getCatalogName(), table.getCatalogName());
		if(null != catalog){
			catalog = catalog.trim();
		}
		String schema = BasicUtil.evl(row.getString("TABLE_SCHEMA", "TABSCHEMA", "SCHEMA_NAME", "OWNER"), column.getSchemaName(), table.getSchemaName());
		if(null != schema){
			schema = schema.trim();
		}
		column.setCatalog(catalog);
		column.setSchema(schema);
		if(null != table.getName()) {//查询全部表
			column.setTable(table);
		}
		column.setTable(BasicUtil.evl(row.getString("TABLE_NAME", "TABNAME"), table.getName(), column.getTableName(true)));

		if(null == column.getPosition()) {
			try {
				column.setPosition(row.getInt("ORDINAL_POSITION", "COLNO", "POSITION"));
			}catch (Exception e){}
		}
		column.setComment(BasicUtil.evl(row.getString("COLUMN_COMMENT", "COMMENTS", "REMARKS"), column.getComment()));
		String type = row.getString("FULL_TYPE","DATA_TYPE", "TYPE_NAME", "TYPENAME", "DATA_TYPE_NAME");
		/*if(null != type){
			type = type.replace("character varying","VARCHAR");
		}*/
		//FULL_TYPE pg中pg_catalog.format_type合成的
		//character varying
		//TODO timestamp without time zone
		//TODO 子类型  geometry(Polygon,4326) geometry(Polygon) geography(Polygon,4326)
		if(null != type && type.contains(" ")){
			type = row.getString("UDT_NAME","DATA_TYPE", "TYPENAME", "DATA_TYPE_NAME");
		}
		column.setTypeName(BasicUtil.evl(type, column.getTypeName()));
		String def = BasicUtil.evl(row.get("COLUMN_DEFAULT", "DATA_DEFAULT", "DEFAULT", "DEFAULT_VALUE","DEFAULT_DEFINITION"), column.getDefaultValue())+"";
		if(BasicUtil.isNotEmpty(def)) {
			while(def.startsWith("(") && def.endsWith(")")){
				def = def.substring(1, def.length()-1);
			}
			while(def.startsWith("'") && def.endsWith("'")){
				def = def.substring(1, def.length()-1);
			}
			column.setDefaultValue(def);
		}
		//默认值约束
		column.setDefaultConstraint(row.getString("DEFAULT_CONSTRAINT"));
		if(-1 == column.isAutoIncrement()){
			column.autoIncrement(row.getBoolean("IS_IDENTITY", null));
		}
		if(-1 == column.isAutoIncrement()){
			column.autoIncrement(row.getBoolean("IS_AUTOINCREMENT", null));
		}
		if(-1 == column.isAutoIncrement()){
			column.autoIncrement(row.getBoolean("IDENTITY", null));
		}
		if(-1 == column.isAutoIncrement()){
			if(row.getStringNvl("EXTRA").toLowerCase().contains("auto_increment")){
				column.autoIncrement(true);
			}
		}

		column.setObjectId(row.getLong("OBJECT_ID", (Long)null));
		//主键
		String column_key = row.getString("COLUMN_KEY");
		if("PRI".equals(column_key)){
			column.primary(1);
		}
		if(row.getBoolean("PK", Boolean.FALSE)){
			column.primary(1);
		}

		//非空
		if(-1 == column.isNullable()) {
			try {
				column.nullable(row.getBoolean("IS_NULLABLE", "NULLABLE", "NULLS"));
			}catch (Exception e){}
		}
		//oracle中decimal(18,9) data_length == 22 DATA_PRECISION=18
		try {
			Integer len = row.getInt("NUMERIC_PRECISION", "PRECISION", "DATA_PRECISION","");
			if (null == len || len == 0) {
				len = row.getInt("CHARACTER_MAXIMUM_LENGTH", "MAX_LENGTH", "DATA_LENGTH", "LENGTH");
			}
			column.setPrecision(len);
		}catch (Exception e){}
		try {
			if (null == column.getScale()) {
				column.setScale(row.getInt("NUMERIC_SCALE", "SCALE", "DATA_SCALE"));
			}
		}catch (Exception e){}
		if(null == column.getCharset()) {
			column.setCharset(row.getString("CHARACTER_SET_NAME"));
		}
		if(null == column.getCollate()) {
			column.setCollate(row.getString("COLLATION_NAME"));
		}
		if(null == column.getColumnType()) {
			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
		}
	}
	/**
	 *
	 * column[结果集封装-子流程](方法2)<br/>
	 * 方法(2)表头内部遍历
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column column
	 * @param rsm ResultSetMetaData
	 * @param index 第几列
	 * @return Column
	 */

	@Override
	public Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index){
		if(null == column){
			column = new Column();
		}
		try{
			column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
		}
		try{
			column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
		}
		try{
			column.setClassName(rsm.getColumnClassName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
		}
		try{
			column.caseSensitive(rsm.isCaseSensitive(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isCaseSensitive]");
		}
		try{
			column.currency(rsm.isCurrency(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
		}
		try{
			column.setOriginalName(rsm.getColumnName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
		}
		try{
			column.setName(rsm.getColumnLabel(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
		}
		try{
			column.setPrecision(rsm.getPrecision(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
		}
		try{
			column.setScale(rsm.getScale(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getScale]");
		}
		try{
			column.setDisplaySize(rsm.getColumnDisplaySize(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
		}
		try{
			column.setSigned(rsm.isSigned(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isSigned]");
		}
		try{
			column.setTable(rsm.getTableName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getTableName]");
		}
		try {
			column.setType(rsm.getColumnType(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
		}
		try {
			//不准确 POINT 返回 GEOMETRY
			String jdbcType = rsm.getColumnTypeName(index);
			column.setJdbcType(jdbcType);
			if(BasicUtil.isEmpty(column.getTypeName())) {
				column.setTypeName(jdbcType);
			}
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
		}
		ColumnType columnType = type(column.getTypeName());
		column.setColumnType(columnType);
		return column;
	}


	/**
	 * column[结果集封装](方法3)<br/>
	 * 有表名的情况下可用<br/>
	 * 根据jdbc.datasource.connection.DatabaseMetaData获取指定表的列数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param columns columns
	 * @param dbmd DatabaseMetaData
	 * @param table 表
	 * @param pattern 列名称通配符
	 * @return LinkedHashMap
	 * @param <T> Column
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		if(BasicUtil.isEmpty(table.getName())){
			return columns;
		}
		String catalogName = null;
		String schemaName = null;
		if(null != catalog){
			catalogName = catalog.getName();
		}
		if(null != schema){
			schemaName = schema.getName();
		}
		ResultSet set = dbmd.getColumns(catalogName, schemaName, table.getName(), pattern);
		Map<String,Integer> keys = keys(set);
		while (set.next()){
			String name = set.getString("COLUMN_NAME");
			if(null == name){
				continue;
			}
			String columnCatalog = string(keys,"TABLE_CAT", set, null);
			if(null != columnCatalog){
				columnCatalog = columnCatalog.trim();
			}
			String columnSchema = string(keys,"TABLE_SCHEM", set, null);
			if(null != columnSchema){
				columnSchema = columnSchema.trim();
			}
			if(!BasicUtil.equalsIgnoreCase(catalog, columnCatalog)){
				continue;
			}
			if(!BasicUtil.equalsIgnoreCase(schema, columnSchema)){
				continue;
			}


			T column = columns.get(name.toUpperCase());
			if(null == column){
				if(create) {
					column = (T)new Column(name);
					columns.put(name.toUpperCase(), column);
				}else {
					continue;
				}
			}
			String remark = string(keys, "REMARKS", set, column.getComment());
			if("TAG".equals(remark)){
				column = (T)new Tag();
			}
			column.setCatalog(columnCatalog);
			column.setSchema(columnSchema);
			column.setComment(remark);
			column.setTable(BasicUtil.evl(string(keys,"TABLE_NAME", set, table.getName()), column.getTableName(true)));
			column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
			column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
			String jdbcType = string(keys, "TYPE_NAME", set, column.getTypeName());
			if(BasicUtil.isEmpty(column.getTypeName())) {
				//数据库中 有jdbc是支持的类型 如果数据库中有了就不用jdbc的了
				column.setTypeName(jdbcType);
			}
			column.setJdbcType(jdbcType);
			column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
			column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
			column.nullable(bool(keys, "NULLABLE", set, column.isNullable()));
			column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
			column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
			column.autoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
			column(runtime, column, set);
			column.setName(name);
		}

		// 主键
		ResultSet rs = dbmd.getPrimaryKeys(table.getCatalogName(), table.getSchemaName(), table.getName());
		while (rs.next()) {
			String name = rs.getString(4);
			Column column = columns.get(name.toUpperCase());
			if (null == column) {
				continue;
			}
			column.primary(true);
		}
		return columns;
	}


	/**
	 * column[结果集封装-子流程](方法3)<br/>
	 * 方法(3)内部遍历
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column column
	 * @param rs ResultSet
	 * @return Column
	 */
	@Override
	public Column column(DataRuntime runtime, Column column, ResultSet rs){
		if(null == column){
			column = new Column();
		}
		try {
			Map<String,Integer> keys = keys(rs);
			if(null == column.getName()){
				column.setName(string(keys, "COLUMN_NAME", rs));
			}
			if(null == column.getType()){
				column.setType(BasicUtil.parseInt(string(keys, "DATA_TYPE", rs), null));
			}
			if(null == column.getType()){
				column.setType(BasicUtil.parseInt(string(keys, "SQL_DATA_TYPE", rs), null));
			}
			if(null == column.getTypeName()){
				String jdbcType = string(keys, "TYPE_NAME", rs);
				column.setJdbcType(jdbcType);
				if(BasicUtil.isEmpty(column.getTypeName())) {
					column.setTypeName(jdbcType);
				}
			}
			if(null == column.getPrecision()) {
				column.setPrecision(integer(keys, "COLUMN_SIZE", rs, null));
			}
			if(null == column.getScale()) {
				column.setScale(BasicUtil.parseInt(string(keys, "DECIMAL_DIGITS", rs), null));
			}
			if(null == column.getPosition()) {
				column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
			}
			if(-1 == column.isAutoIncrement()) {
				column.autoIncrement(BasicUtil.parseBoolean(string(keys, "IS_AUTOINCREMENT", rs), false));
			}
			if(-1 == column.isGenerated()) {
				column.generated(BasicUtil.parseBoolean(string(keys, "IS_GENERATEDCOLUMN", rs), false));
			}
			if(null == column.getComment()) {
				column.setComment(string(keys, "REMARKS", rs));
			}
			if(null == column.getPosition()){
				column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
			}
			if (BasicUtil.isEmpty(column.getDefaultValue())) {
				column.setDefaultValue(string(keys, "COLUMN_DEF", rs));
			}
			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
		}catch (Exception e){
			e.printStackTrace();
		}
		return column;
	}


	/**
	 * column[结果集封装](方法4)<br/>
	 * 解析查询结果metadata(0=1)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param columns columns
	 * @param table 表
	 * @param set SqlRowSet由spring封装过的结果集ResultSet
	 * @return LinkedHashMap
	 * @param <T> Column
	 * @throws Exception
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		SqlRowSetMetaData rsm = set.getMetaData();
		for (int i = 1; i <= rsm.getColumnCount(); i++) {
			String name = rsm.getColumnName(i);
			if(BasicUtil.isEmpty(name)){
				continue;
			}
			T column = columns.get(name.toUpperCase());
			if(null == column){
				if(create){
					column = (T)column(runtime, column, rsm, i);
					if(BasicUtil.isEmpty(column.getName())) {
						column.setName(name);
					}
					columns.put(column.getName().toUpperCase(), column);
				}
			}
		}
		return columns;
	}

	/**
	 * column[结果集封装-子流程](方法4)<br/>
	 * 内部遍历<br/>
	 * columns(DataRuntime runtime, boolean create, LinkedHashMap columns, Table table, SqlRowSet set)遍历内部<br/>
	 * 根据SqlRowSetMetaData获取列属性 jdbc.queryForRowSet(where 1=0)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 获取的数据赋值给column如果为空则新创建一个
	 * @param rsm 通过spring封装过的SqlRowSet获取的SqlRowSetMetaData
	 * @param index 第几列
	 * @return Column
	 */
	@Override
	public Column column(DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index){
		if(null == column) {
			column = new Column();
			try {
				column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
			}
			try {
				column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
			}
			try {
				column.setClassName(rsm.getColumnClassName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
			}
			try {
				column.currency(rsm.isCurrency(index));
			} catch (Exception e) {
				column.caseSensitive(rsm.isCaseSensitive(index));
				log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
			}
			try {
				column.setOriginalName(rsm.getColumnName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
			}
			try {
				column.setName(rsm.getColumnLabel(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
			}
			try {
				column.setPrecision(rsm.getPrecision(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
			}
			try {
				column.setScale(rsm.getScale(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getScale]");
			}
			try {
				column.setDisplaySize(rsm.getColumnDisplaySize(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
			}
			try {
				column.setSigned(rsm.isSigned(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:isSigned]");
			}
			try {
				column.setTable(rsm.getTableName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getTableName]");
			}
			try {
				column.setType(rsm.getColumnType(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
			}
			try {
				String jdbcType = rsm.getColumnTypeName(index);
				column.setJdbcType(jdbcType);
				if(BasicUtil.isEmpty(column.getTypeName())) {
					column.setTypeName(jdbcType);
				}
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
			}

			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
		}
		return column;
	}

	/**
	 * query[结果集封装-子流程]
	 * 封装查询结果行,在外层遍历中修改rs下标
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	@Override
	public DataRow row(boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ConfigStore configs, ResultSet rs){
		DataRow row = null;
		KeyAdapter.KEY_CASE kc = null;
		if(null != configs){
			kc = configs.keyCase();
		}
		boolean upper = false;
		if(KeyAdapter.KEY_CASE.PUT_UPPER == kc){
			//put时大写,DataRow按SRC处理
			upper = true;
			row = new DataRow(KeyAdapter.KEY_CASE.SRC);
		}else if(null != kc){
			row = new DataRow(kc);
		}else{
			row = new DataRow();
		}
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int qty = rsmd.getColumnCount();
			if (!system && (null == metadatas || metadatas.isEmpty())) {
				for (int i = 1; i <= qty; i++) {
					String name = rsmd.getColumnLabel(i);
					if(null == name){
						name = rsmd.getColumnName(i);
					}
					if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
						continue;
					}
					Column column = metadatas.get(name) ;
					column = column(runtime, (Column) column, rsmd, i);
					metadatas.put(name.toUpperCase(), column);
				}
			}
			for (int i = 1; i <= qty; i++) {
				String name = rsmd.getColumnLabel(i);
				if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
					continue;
				}
				try {
					Column column = metadatas.get(name.toUpperCase());
					//Object v = BeanUtil.value(column.getTypeName(), rs.getObject(name));
					Object value = read(runtime, column, rs.getObject(name), null);
					if (upper) {
						name = name.toUpperCase();
					}
					row.put(false, name, value);
				}catch (Exception e){

				}
			}
			row.setMetadata(metadatas);
		}catch (Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}else{
				log.error("[结果集封装] [result:fail][msg:{}]", e.toString());
			}
		}
		return row;
	}

	protected boolean stream(StreamHandler handler, ResultSet rs, ConfigStore configs, boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas) {
		try {
			if (handler instanceof ResultSetHandler) {
				return ((ResultSetHandler) handler).read(rs);
			} else {
				if (handler instanceof DataRowHandler) {
					DataRowHandler dataRowHandler = (DataRowHandler) handler;
					DataRow row = row(system, runtime, metadatas, configs, rs);
					if (!dataRowHandler.read(row)) {
						return false;
					}
				} else if (handler instanceof EntityHandler) {
					Class clazz = configs.entityClass();
					if (null != clazz) {
						EntityHandler entityHandler = (EntityHandler) handler;
						DataRow row = row(system, runtime, metadatas, configs, rs);
						if (!entityHandler.read(row.entity(clazz))) {
							return false;
						}
					}
				} else if (handler instanceof MapHandler) {
					MapHandler mh = (MapHandler) handler;
					ResultSetMetaData rsmd = rs.getMetaData();
					int cols = rsmd.getColumnCount();
					Map<String, Object> map = new HashMap<>();
					for (int i = 1; i <= cols; i++) {
						String name = rsmd.getColumnLabel(i);
						if(null == name){
							name = rsmd.getColumnName(i);
						}
						map.put(name, rs.getObject(i));
					}
					if (!mh.read(map)) {
						return false;
					}
				}
			}
		}catch (Exception e){
			return false;
		}
		return true;
	}
	protected DataSet select(DataRuntime runtime, String random, boolean system, ACTION.DML action, String table, ConfigStore configs, Run run, String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			if(IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new DataSet();
			}
		}

		if(null != configs){
			configs.add(run);
		}
		long fr = System.currentTimeMillis();
		if(null == random){
			random = random(runtime);
		}
		if(log.isInfoEnabled() && IS_LOG_SQL(configs)){
			log.info("{}[action:select]{}", random, run.log(action, IS_SQL_LOG_PLACEHOLDER(configs)));
		}
		DataSet set = new DataSet();
		boolean exe = true;
		if(null != configs){
			exe = configs.execute();
		}
		if(!exe){
			return set;
		}
		//根据这一步中的JDBC结果集检测类型不准确,如:实际POINT 返回 GEOMETRY 如果要求准确 需要开启到自动检测
		//在DataRow中 如果检测到准确类型 JSON XML POINT 等 返回相应的类型,不返回byte[]（所以需要开启自动检测）
		//Entity中 JSON XML POINT 等根据属性类型返回相应的类型（所以不需要开启自动检测）
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
		if(!system && IS_AUTO_CHECK_METADATA(configs) && null != table){
			columns = columns(runtime,  random, false,  new Table( table), false);
		}
		try{
			final DataRuntime rt = runtime;
			final long[] mid = {System.currentTimeMillis()};
			final boolean[] process = {false};
			final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
			metadatas.putAll(columns);
			set.setMetadata(metadatas);
			JdbcTemplate jdbc = jdbc(runtime);
			StreamHandler _handler = null;
			if(null != configs){
				_handler = configs.stream();
			}
			long[] count = new long[]{0};
			final StreamHandler handler = _handler;
			if(null != handler){
				jdbc.query(con -> {
					PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					ps.setFetchSize(handler.size());
					ps.setFetchDirection(ResultSet.FETCH_FORWARD);
					if (null != values && values.size() > 0) {
						int idx = 0;
						for (Object value : values) {
							ps.setObject(++idx, value);
						}
					}
					return ps;
				}, rs -> {
					if(!process[0]){
						mid[0] = System.currentTimeMillis();
						process[0] = true;
					}
					stream(handler, rs, configs, system, runtime, metadatas);
					count[0] ++;
				});
				//end stream handler
			}else {
				if(null != values && values.size()>0){
					jdbc.query(sql, values.toArray(), new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							if(!process[0]){
								mid[0] = System.currentTimeMillis();
								process[0] = true;
							}
							DataRow row = row(system, rt, metadatas, configs, rs);
							set.add(row);
						}
					});
				}else {
					jdbc.query(sql, new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							if(!process[0]){
								mid[0] = System.currentTimeMillis();
								process[0] = true;
							}
							DataRow row = row(system, rt, metadatas, configs, rs);
							set.add(row);
						}
					});
				}
				count[0] = set.size();
			}
			if(!process[0]){
				mid[0] = System.currentTimeMillis();
			}
			if(metadatas.isEmpty() && IS_CHECK_EMPTY_SET_METADATA(configs)){
				metadatas.putAll(metadata(runtime, new DefaultTextPrepare(sql), false));
			}
			boolean slow = false;
			long SLOW_SQL_MILLIS = SLOW_SQL_MILLIS(configs);
			if(SLOW_SQL_MILLIS > 0 && IS_LOG_SLOW_SQL(configs)){
				slow = true;
				if(mid[0] - fr > SLOW_SQL_MILLIS){
					log.warn("{}[slow cmd][action:select][执行耗时:{}ms]{}", random, mid[0] - fr, run.log(ACTION.DML.SELECT, IS_SQL_LOG_PLACEHOLDER(configs)));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, set,mid[0] - fr);
					}
				}
			}
			if(!slow && log.isInfoEnabled() && IS_LOG_SQL_TIME(configs)){
				log.info("{}[action:select][执行耗时:{}ms]", random, mid[0] - fr);
				log.info("{}[action:select][封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], count[0]);
			}
			set.setDatalink(runtime.datasource());
		}catch(Exception e){
			if(IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
				e.printStackTrace();
			}
			if(IS_LOG_SQL_WHEN_ERROR(configs)){
				log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e.toString(), run.log(ACTION.DML.SELECT, IS_SQL_LOG_PLACEHOLDER(configs)));
			}
			if(IS_THROW_SQL_QUERY_EXCEPTION(configs)){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}

		}
		return set;
	}
	protected long batch(JdbcTemplate jdbc, String sql, int batch, int vol,  List<Object> values){
		int size = values.size(); //一共多少参数
		int line = size/vol; //一共多少行
		//batch insert保持SQL一致,如果不一致应该调用save方法
		//返回每个SQL的影响行数
		jdbc.batchUpdate(sql,
				new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						//i从0开始 参数下标从1开始
						for(int p=1; p<=vol; p++){
							ps.setObject(p, values.get(vol*i+p-1));
						}
					}
					public int getBatchSize() {
						return line;
					}
				});
		return line;
	}


	@Override
	public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, DataSource ds, T meta){
		if(null == meta || null != meta.getCheckSchemaTime()){
			return;
		}
		Connection con = null;
		try {
			if (null == meta.getCatalog() || null == meta.getSchema()) {
				con = DataSourceUtils.getConnection(ds);
				checkSchema(runtime, con, meta);
			}
		}catch (Exception e){
			log.warn("[check schema][fail:{}]", e.toString());
		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
	}

	@Override
	public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, Connection con, T meta){
		try {
			if (null == meta.getCatalog()) {
				meta.setCatalog(con.getCatalog());
			}
			if (null == meta.getSchema()) {
				meta.setSchema(con.getSchema());
			}
		}catch (Exception e){
		}
		meta.setCheckSchemaTime(new Date());
	}
	@Override
	public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, T meta){
		if(null != meta){
			checkSchema(runtime, jdbc(runtime).getDataSource(), meta);
		}
	}


	public  <T extends Column> T column(Catalog catalog, Schema schema, String table, String name, List<T> columns){
		for(T column:columns){
			if(null != table && null != name) {
				String identity = BasicUtil.nvl(catalog, "") + "_" + BasicUtil.nvl(schema, "") + "_" + BasicUtil.nvl(table, "") + "_" + name;
				identity = MD5Util.crypto(identity.toUpperCase());
				if (identity.equals(column.getIdentity())) {
					return column;
				}
			}
		}
		return null;
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
	 * 获取ResultSet中的列
	 * @param set ResultSet
	 * @return list
	 * @throws Exception 异常 Exception
	 */
	protected Map<String, Integer> keys(ResultSet set) throws Exception{
		ResultSetMetaData rsmd = set.getMetaData();
		Map<String, Integer> keys = new HashMap<>();
		if(null != rsmd){
			for (int i = 1; i < rsmd.getColumnCount(); i++) {
				String name = rsmd.getColumnLabel(i);
				if(null == name){
					name = rsmd.getColumnName(i);
				}
				keys.put(name.toUpperCase(), i);
			}
		}
		return keys;
	}

	public String insertHead(ConfigStore configs){
		return "INSERT INTO ";
	}
	public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns){
		return "";
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
					if(str.startsWith("${") && str.endsWith("}")){
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
						builder.append(write(runtime, null, value, false));
					}
				} else {
					addRunValue(runtime, run, Compare.EQUAL, column, value);
				}
			}

			if(alias && batch<=1){
				builder.append(" AS ").append(key);
			}
		}
		if(scope && batch<=1) {
			builder.append(")");
		}
		return builder.toString();
	}
	public String getPrimayKey(Object obj){
		String key = null;
		if(obj instanceof Collection){
			obj = ((Collection)obj).iterator().next();
		}
		if(obj instanceof DataRow){
			key = ((DataRow)obj).getPrimaryKey();
		}else{
			key = EntityAdapterProxy.primaryKey(obj.getClass(), true);
		}
		return key;
	}

	protected void replaceVariable(DataRuntime runtime, TextRun run){
		StringBuilder builder = run.getBuilder();
		RunPrepare prepare = run.getPrepare();
		List<Variable> variables = run.getVariables();
		String result = prepare.getText();
		if(null != variables){
			for(Variable var:variables){
				if(null == var){
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_REPLACE){
					// CD = ::CD
					List<Object> values = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true,values)){
						if(var.getCompare() == Compare.IN){
							value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
						}else {
							value = values.get(0).toString();
						}
					}
					if(null != value){
						result = result.replace(var.getFullKey(), value);
					}else{
						result = result.replace(var.getFullKey(), "NULL");
					}
				}
			}
			for(Variable var:variables){
				if(null == var){
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE){
					// CD = ':CD'
					List<Object> values = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true,values)){
						if(var.getCompare() == Compare.IN){
							value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
						}else {
							value = values.get(0).toString();
						}
					}
					if(null != value){
						result = result.replace(var.getFullKey(), value);
					}else{
						result = result.replace(var.getFullKey(), "");
					}
				}
			}
			for(Variable var:variables){
				if(null == var){
					continue;
				}
				if(var.getType() == Variable.VAR_TYPE_KEY){
					// CD = :CD
					List<Object> varValues = var.getValues();
					if(BasicUtil.isNotEmpty(true, varValues)){
						if(var.getCompare() == Compare.IN){
							// 多个值IN
							String replaceDst = "";
							for(Object tmp:varValues){
								replaceDst += " ?";
							}
							addRunValue(runtime, run, Compare.IN, new Column(var.getKey()), varValues);
							replaceDst = replaceDst.trim().replace(" ", ",");
							result = result.replace(var.getFullKey(), replaceDst);
						}else{
							// 单个值
							result = result.replace(var.getFullKey(), "?");
							addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), varValues.get(0));
						}
					}else{
						//没有提供参数值
						result = result.replace(var.getFullKey(), "NULL");
					}
				}
			}
			// 添加其他变量值
			for(Variable var:variables){
				if(null == var){
					continue;
				}
				// CD = ?
				if(var.getType() == Variable.VAR_TYPE_INDEX){
					List<Object> varValues = var.getValues();
					String value = null;
					if(BasicUtil.isNotEmpty(true, varValues)){
						value = (String)varValues.get(0);
					}
					addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), value);
				}
			}
		}

		builder.append(result);
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
			sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
		}
		PageNavi navi = run.getPageNavi();
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
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
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
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
			builder.append("SELECT "+cols+" FROM( \n");
			builder.append("SELECT TAB_I.* ,ROWNUM AS PAGE_ROW_NUMBER_ \n");
			builder.append("FROM( \n");
			builder.append(sql);
			builder.append("\n").append(order);
			builder.append(")  TAB_I \n");
			builder.append(")  TAB_O WHERE PAGE_ROW_NUMBER_ >= "+(first+1)+" AND PAGE_ROW_NUMBER_ <= "+(last+1));

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
			String sub = sql.substring(sql.toUpperCase().indexOf("SELECT")+6);
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
			String desc = order.replace("ASC", "<A_ORDER>");
			desc = desc.replace("DESC", "ASC");
			desc = desc.replace("<A_ORDER>", "DESC");
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
			builder.append("SELECT _TAB_I.* ,ROW_NUMBER() OVER(")
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


}
