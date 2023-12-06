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


package org.anyline.data.adapter.init;


import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.xml.XMLPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.GeneratorConfig;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.AnylineException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class DefaultDriverAdapter implements DriverAdapter {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDriverAdapter.class);

	@Autowired(required = false)
	protected DMListener dmListener;
	@Autowired(required = false)
	protected DDListener ddListener;


	public DMListener getListener() {
		return dmListener;
	}

	@Autowired(required=false)
	public void setListener(DMListener listener) {
		this.dmListener = listener;
	}


	public String delimiterFr = "";
	public String delimiterTo = "";

	//根据名称定准数据类型
	protected Map<String, ColumnType> types = new Hashtable();

	@Autowired(required=false)
	protected PrimaryGenerator primaryGenerator;


	protected Map<String, String> versions = new Hashtable<>();


	public String version(){return null;}
	public DatabaseType compatible(){
		return null;
	}
	public DefaultDriverAdapter(){
		//当前数据库支持的数据类型,子类根据情况覆盖
		for(StandardColumnType type: StandardColumnType.values()){
			DatabaseType[] dbs = type.dbs();
			for(DatabaseType db:dbs){
				if(db == this.type()){
					//column type支持当前db
					types.put(type.getName(), type);
					break;
				}
			}
		}
	}

	@Override
	public String getDelimiterFr(){
		return this.delimiterFr;
	}
	@Override
	public String getDelimiterTo(){
		return this.delimiterTo;
	}
	public void setDelimiter(String delimiter){
		if(BasicUtil.isNotEmpty(delimiter)){
			delimiter = delimiter.replaceAll("\\s", "");
			if(delimiter.length() == 1){
				this.delimiterFr = delimiter;
				this.delimiterTo = delimiter;
			}else{
				this.delimiterFr = delimiter.substring(0,1);
				this.delimiterTo = delimiter.substring(1,2);
			}
		}
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
		dest = DataSourceUtil.parseDataSource(dest, data);
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		swt = InterceptorProxy.prepareInsert(runtime, random, batch, dest, data, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareInsert(runtime, random, batch, dest, data, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != data && data instanceof DataSet){
			DataSet set = (DataSet)data;
			Map<String,Object> tags = set.getTags();
			if(null != tags && tags.size()>0){
				LinkedHashMap<String, PartitionTable> ptables = ptables(runtime, random, false, new MasterTable(dest), tags, null);
				if(ptables.size() != 1){
					String msg = "分区表定位异常,主表:" + dest + ",标签:" + BeanUtil.map2json(tags) + ",分区表:" + BeanUtil.object2json(ptables.keySet());
					if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
						throw new SQLUpdateException(msg);
					}else{
						log.error(msg);
						return -1;
					}
				}
				dest = ptables.values().iterator().next().getName();
			}
		}
		Run run = buildInsertRun(runtime, batch, dest, data, configs, columns);
		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(IS_AUTO_CHECK_METADATA(configs)){
			table.setColumns(columns(runtime, random,  false, table, false));
		}
		if(null == run){
			return 0;
		}

		long cnt = 0;
		long fr = System.currentTimeMillis();
		long millis = -1;

		swt = InterceptorProxy.beforeInsert(runtime, random, run, dest, data, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeInsert(runtime, random, run, dest, data, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		cnt = insert(runtime, random, data, configs, run, null);
		if (null != dmListener) {
			dmListener.afterInsert(runtime, random, run, cnt, dest, data, columns, cmd_success, cnt, millis);
		}
		InterceptorProxy.afterInsert(runtime, random, run, dest, data, columns, cmd_success, cnt, System.currentTimeMillis() - fr);
		return cnt;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
		}
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
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();/*确定需要插入的列*/
		if(null == obj){
			return new LinkedHashMap<>();
		}
		if(obj instanceof Map && !(obj instanceof DataRow)){
			obj = new DataRow((Map)obj);
		}
		LinkedHashMap<String, Column> mastKeys = new LinkedHashMap<>();		// 必须插入列
		List<String> ignores = new ArrayList<>();		// 必须不插入列
		List<String> factKeys = new ArrayList<>();		// 根据是否空值

		boolean each = true;//是否需要从row中查找列
		if(null != columns && columns.size()>0){
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1);
					mastKeys.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")){
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")){
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}
		if(each){
			// 是否插入null及""列
			boolean isInsertNullColumn =  false;
			boolean isInsertEmptyColumn = false;
			DataRow row = null;
			if(obj instanceof DataRow){
				row = (DataRow)obj;
				mastKeys.putAll(row.getUpdateColumns(true));

				ignores.addAll(row.getIgnoreUpdateColumns());
				cols = row.getColumns();

				isInsertNullColumn = row.isInsertNullColumn();
				isInsertEmptyColumn = row.isInsertEmptyColumn();

			}else{
				isInsertNullColumn = IS_INSERT_NULL_FIELD(configs);
				isInsertEmptyColumn = IS_INSERT_EMPTY_FIELD(configs);
				if(EntityAdapterProxy.hasAdapter(obj.getClass())){
					cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.INSERT));
				}else {
					cols = new LinkedHashMap<>();
					List<Field> fields = ClassUtil.getFields(obj.getClass(), false, false);
					for (Field field : fields) {
						Class clazz = field.getType();
						if (clazz == String.class || clazz == Date.class || ClassUtil.isPrimitiveClass(clazz)) {
							cols.put(field.getName().toUpperCase(), new Column(field.getName()));
						}
					}
				}
			}
			if(batch){
				isInsertNullColumn = true;
				isInsertEmptyColumn = true;
			}

			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][columns:{}]", cols);
			}
			BeanUtil.removeAll(ignores, columns);
			for(String ignore:ignores){
				cols.remove(ignore.toUpperCase());
			}
			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][ignores:{}]", ignores);
			}
			List<String> keys = BeanUtil.getMapKeys(cols);
			for(String key:keys){
				key = key.toUpperCase();
				if(mastKeys.containsKey(key)){
					// 必须插入
					continue;
				}
				Object value = null;
				if(!(obj instanceof Map) && EntityAdapterProxy.hasAdapter(obj.getClass())){
					value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
				}else{
					value = BeanUtil.getFieldValue(obj, key);
				}

				if(null == value){
					if(factKeys.contains(key)){
						cols.remove(key);
						continue;
					}
					if(!isInsertNullColumn){
						cols.remove(key);
						continue;
					}
				}else if(BasicUtil.isEmpty(true, value)){
					if(factKeys.contains(key)){
						cols.remove(key);
						continue;
					}
					if(!isInsertEmptyColumn){
						cols.remove(key);
						continue;
					}
				}

			}
		}
		if(log.isDebugEnabled()) {
			log.debug("[confirm insert columns][result:{}]", cols);
		}
		cols = checkMetadata(runtime, dest, configs, cols);
 		return cols;
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
	protected void setPrimaryValue(Object obj, Object value){
		if(null == obj){
			return;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			row.put(row.getPrimaryKey(), value);
		}else{
			Column key = EntityAdapterProxy.primaryKey(obj.getClass());
			Field field = EntityAdapterProxy.field(obj.getClass(), key);
			BeanUtil.setFieldValue(obj, field, value);
		}
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
	protected Run createInsertRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run createInsertRun(DataRuntime runtime, String dest, Object obj, List<String> columns)", 37));
		}
		return null;
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
	protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, List<String> columns){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run createInsertRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, List<String> columns)", 37));
		}
		return null;
	}

	/**
	 * insert [after]<br/>
	 * 执行insert后返回自增主键的key
	 * @return String
	 */
	@Override
	public String generatedKey() {
		return null;
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
	public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks)", 37));
		}
		return -1;
	}
	


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
		dest = DataSourceUtil.parseDataSource(dest, data);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null == data){
			if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				throw new SQLUpdateException("更新空数据");
			}else{
				log.error("更新空数据");
			}
		}
		long result = 0;
		if(data instanceof Collection){
			if(batch <= 1){
				Collection list = (Collection) data;
				for (Object item : list) {
					result += update(runtime, random, 0, dest, item, configs, columns);
				}
				return result;
			}
		}

		Run run = buildUpdateRun(runtime, batch, dest, data, configs, columns);

		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(IS_AUTO_CHECK_METADATA(configs)){
			table.setColumns(columns(runtime, null,false, table, false));
		}
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		//String sql = run.getFinalUpdate();
		/*if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
			return -1;
		}
		List<Object> values = run.getValues();*/
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		/*if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}*/
		long millis = -1;
		swt = InterceptorProxy.beforeUpdate(runtime, random, run, dest, data, configs, columns);
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.beforeUpdate(runtime, random, run, dest, data, columns);
		}
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		result = update(runtime, random, dest, data, configs, run);
		cmd_success = true;
		millis = System.currentTimeMillis() - fr;
		if (null != dmListener) {
			dmListener.afterUpdate(runtime, random, run, result, dest, data, columns, cmd_success, result,  millis);
		}
		InterceptorProxy.afterUpdate(runtime, random, run, dest, data, configs, columns, cmd_success, result, System.currentTimeMillis() - fr);
		return result;
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
		Run run = null;
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceUtil.parseDataSource(null,obj);
		}
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		if(null != columns){
			for(String column:columns){
				cols.put(column.toUpperCase(), new Column(column));
			}
		}
		if(obj instanceof DataRow){
		}else if(obj instanceof Map){
			obj = new DataRow((Map)obj);
		}
		if(obj instanceof Collection){
			run = buildUpdateRunFromCollection(runtime, batch, dest, (Collection)obj, configs, cols);
		}else if(obj instanceof DataRow){
			run = buildUpdateRunFromDataRow(runtime, dest, (DataRow)obj, configs, cols);
		}else{
			run = buildUpdateRunFromEntity(runtime, dest, obj, configs, cols);
		}
		convert(runtime, configs, run);
		return run;
	}
	@Override
	public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns){
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(2);
		StringBuilder builder = run.getBuilder();
		// List<Object> values = new ArrayList<Object>();
		List<String> list = new ArrayList<>();
		for(Column column:columns.values()){
			list.add(column.getName());
		}
		LinkedHashMap<String,Column> cols = confirmUpdateColumns(runtime, dest, obj, configs, list);
		List<String> primaryKeys = new ArrayList<>();


		if(EntityAdapterProxy.hasAdapter(obj.getClass())){
			primaryKeys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
		}else{
			primaryKeys = new ArrayList<>();
			primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
		}

		// 不更新主键 除非显示指定
		for(String pk:primaryKeys){
			if(!columns.containsKey(pk.toUpperCase())) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		if(!columns.containsKey(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase())) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}
		boolean isReplaceEmptyNull = IS_REPLACE_EMPTY_NULL(configs);
		cols = checkMetadata(runtime, dest, configs, cols);

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/
		if(!cols.isEmpty()){
			builder.append("UPDATE ").append(parseTable(dest));
			builder.append(" SET").append(BR_TAB);
			boolean first = true;
			for(Column column:cols.values()){
				String key = column.getName();
				Object value = null;
				if(EntityAdapterProxy.hasAdapter(obj.getClass())){
					Field field = EntityAdapterProxy.field(obj.getClass(), key);
					value = BeanUtil.getFieldValue(obj, field);
				}else {
					value = BeanUtil.getFieldValue(obj, key);
				}
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")){
					String str = value.toString();
					value = str.substring(2, str.length()-1);

					if(!first){
						builder.append(",");
					}
					delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
					first = false;
				}else{
					if("NULL".equals(value)){
						value = null;
					}else if("".equals(value) && isReplaceEmptyNull){
						value = null;
					}
					boolean chk = true;
                   /* if(null == value){
                        if(!IS_UPDATE_NULL_FIELD(configs)){
                            chk = false;
                        }
                    }else */if("".equals(value)){
						if(!IS_UPDATE_EMPTY_FIELD(configs)){
							chk = false;
						}
					}
					if(chk){
						if(!first){
							builder.append(",");
						}
						first = false;
						delimiter(builder, key).append(" = ?").append(BR_TAB);
						updateColumns.add(key);
						Compare compare = Compare.EQUAL;
						if(isMultipleValue(runtime, run, key)){
							compare = Compare.IN;
						}
						addRunValue(runtime, run, compare, column, value);
					}
				}
			}
			builder.append(BR);
			builder.append("\nWHERE 1=1").append(BR_TAB);
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			for (String pk : primaryKeys) {
				if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
					Field field = EntityAdapterProxy.field(obj.getClass(), pk);
					configs.and(pk, BeanUtil.getFieldValue(obj, field));
				} else {
					configs.and(pk, BeanUtil.getFieldValue(obj, pk));
				}
			}

			run.setConfigStore(configs);
			run.init();
			run.appendCondition();
		}
		run.setUpdateColumns(updateColumns);

		return run;
	}
	@Override
	public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns){
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(1);
		StringBuilder builder = run.getBuilder();


		// List<Object> values = new ArrayList<Object>();
		/*确定需要更新的列*/
		LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, row, configs, BeanUtil.getMapKeys(columns));

		List<String> primaryKeys = row.getPrimaryKeys();
		if(primaryKeys.size() == 0){
			throw new SQLUpdateException("[更新更新异常][更新条件为空,update方法不支持更新整表操作]");
		}
		//先把pk类型取出来
		if(null == configs){
			configs = new DefaultConfigStore();
		}
		for (String pk : primaryKeys) {
			Object pv = row.get(pk);
			pv = convert(runtime, cols.get(pk.toUpperCase()), pv); //统一调用
			configs.and(pk, pv);
                /*builder.append(" AND ");
                delimiter(builder, pk).append(" = ?");
                updateColumns.add(pk);
                addRunValue(runtime, run, Compare.EQUAL, new Column(pk), row.get(pk));*/
		}

		// 不更新主键 除非显示指定
		for(String pk:primaryKeys){
			if(!columns.containsKey(pk.toUpperCase())) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		if(!columns.containsKey(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase())) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}

		boolean replaceEmptyNull = row.isReplaceEmptyNull();

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/

		if(!cols.isEmpty()){
			builder.append("UPDATE ").append(parseTable(dest));
			builder.append(" SET").append(BR_TAB);
			boolean first = true;
			for(Column col:cols.values()){
				String key = col.getName();
				Object value = row.get(key);
				if(!first){
					builder.append(",");
				}
				first = false;
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ){
					String str = value.toString();
					value = str.substring(2, str.length()-1);
					delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
				}else{
					delimiter(builder, key).append(" = ?").append(BR_TAB);
					if("NULL".equals(value)){
						value = null;
					}else if("".equals(value) && replaceEmptyNull){
						value = null;
					}
					updateColumns.add(key);
					Compare compare = Compare.EQUAL;
					addRunValue(runtime, run, compare, col, value);
				}
			}
			builder.append(BR);
			builder.append("\nWHERE 1=1").append(BR_TAB);
			run.setConfigStore(configs);
			run.init();
			run.appendCondition();

		}
		run.setUpdateColumns(updateColumns);

		return run;
	}
	@Override
	public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns){
		TableRun run = new TableRun(runtime, dest);
		run.setFrom(1);
		Object first = list.iterator().next();
		if (null == first){
			return run;
		}
		if(first instanceof Map && !(first instanceof DataRow)){
			first = new DataRow((Map)first);
		}
		LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
		List<String> primaryKeys = new ArrayList<>();
		boolean replaceEmptyNull = false;
		if(first instanceof DataRow){
			DataRow row = (DataRow)first;
			primaryKeys = row.getPrimaryKeys();
			cols = confirmUpdateColumns(runtime, dest, row, configs, BeanUtil.getMapKeys(columns));
			replaceEmptyNull = row.isReplaceEmptyNull();
		}else{
			List<String> ll = new ArrayList<>();
			for(Column column:columns.values()){
				ll.add(column.getName());
			}
			cols = confirmUpdateColumns(runtime, dest, first, configs, ll);
			if(EntityAdapterProxy.hasAdapter(first.getClass())){
				primaryKeys.addAll(EntityAdapterProxy.primaryKeys(first.getClass()).keySet());
			}else{
				primaryKeys = new ArrayList<>();
				primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
			}
			replaceEmptyNull = IS_REPLACE_EMPTY_NULL(configs);
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		StringBuilder builder = run.getBuilder();
		if(primaryKeys.size() == 0){
			throw new SQLUpdateException("[更新更新异常][更新条件为空,update方法不支持更新整表操作]");
		}

		// 不更新主键 除非显示指定
		for(String pk:primaryKeys){
			if(!columns.containsKey(pk.toUpperCase())) {
				cols.remove(pk.toUpperCase());
			}
		}
		//不更新默认主键  除非显示指定
		if(!columns.containsKey(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase())) {
			cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
		}

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/

		if(!cols.isEmpty()){
			builder.append("UPDATE ").append(parseTable(dest));
			builder.append(" SET ");
			boolean start = true;
			for(Column col:cols.values()){
				String key = col.getName();
				if(!start){
					builder.append(", ");
				}
				start = false;
				builder.append(key);
				builder.append(" = ?");
			}
			if(null == configs){
				configs = new DefaultConfigStore();
			}
			start = true;
			for (String pk : primaryKeys) {
				if(start){
					builder.append(" WHERE ");
				}else {
					builder.append(" AND ");
				}
				delimiter(builder, pk).append(" = ?");
				start = false;
			}
		}
		run.setUpdateColumns(updateColumns);
		List<RunValue> values = new ArrayList<>();
		for(Object item:list){
			for(Column col:cols.values()){
				String key = col.getName();
				Object value = BeanUtil.getFieldValue(item, key);
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ){
					String str = value.toString();
					value = str.substring(2, str.length()-1);
				}else{
					if("NULL".equals(value)){
						value = null;
					}else if("".equals(value) && replaceEmptyNull){
						value = null;
					}
				}
				values.add(new RunValue(key, value));
			}

			for (String pk : primaryKeys) {
				values.add(new RunValue(pk, BeanUtil.getFieldValue(item, pk)));
			}
		}
		run.setBatch(batch);
		run.setVol(cols.size()+primaryKeys.size());
		run.setRunValues(values);
		return run;
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
		LinkedHashMap<String,Column> cols = null;/*确定需要更新的列*/
		if(null == row){
			return new LinkedHashMap<>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> conditions = new ArrayList<>()							; // 更新条件
		LinkedHashMap<String, Column> masters = row.getUpdateColumns(true)		; // 必须更新列
		List<String> ignores = BeanUtil.copy(row.getIgnoreUpdateColumns())	; // 必须不更新列
		List<String> factKeys = new ArrayList<>()							; // 根据是否空值
		BeanUtil.removeAll(ignores, columns);

		if(null != columns && columns.size()>0){
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1);
					masters.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")){
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")){
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}else if(null != masters && masters.size()>0){
			each = false;
			cols = masters;
		}
		if(each){
			cols = row.getUpdateColumns(true);
			cols.putAll(masters);
			// 是否更新null及""列
			boolean isUpdateNullColumn = row.isUpdateNullColumn();
			boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();
			List<String> keys = BeanUtil.getMapKeys(cols);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(masters.containsKey(key)){
					// 必须更新
					continue;
				}

				Object value = row.get(key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}
					if(!isUpdateNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}
					if(!isUpdateEmptyColumn){
						keys.remove(i);
						continue;
					}
				}

			}
		}
		if(null != ignores){
			for(String ignore:ignores){
				cols.remove(ignore.toUpperCase());
			}
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		return cols;
	}
	public LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
		LinkedHashMap<String,Column> cols = null;/*确定需要更新的列*/
		if(null == obj){
			return new LinkedHashMap<>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> conditions = new ArrayList<>()							; // 更新条件
		LinkedHashMap<String, Column> masters = new LinkedHashMap<>()		; // 必须更新列
		List<String> ignores = new ArrayList<>()	; // 必须不更新列
		List<String> factKeys = new ArrayList<>()							; // 根据是否空值
		BeanUtil.removeAll(ignores, columns);

		if(null != columns && columns.size()>0){
			each = false;
			cols = new LinkedHashMap<>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1);
					masters.put(column.toUpperCase(), new Column(column));
					each = true;
				}else if(column.startsWith("-")){
					column = column.substring(1);
					ignores.add(column);
					each = true;
				}else if(column.startsWith("?")){
					column = column.substring(1);
					factKeys.add(column);
					each = true;
				}
				cols.put(column.toUpperCase(), new Column(column));
			}
		}else if(null != masters && masters.size()>0){
			each = false;
			cols = masters;
		}
		if(each){
			cols = new LinkedHashMap<>();
			cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.UPDATE)); ;
			cols.putAll(masters);
			// 是否更新null及""列
			boolean isUpdateNullColumn = IS_UPDATE_NULL_FIELD(configs);
			boolean isUpdateEmptyColumn = IS_UPDATE_EMPTY_FIELD(configs);
			List<String> keys = BeanUtil.getMapKeys(cols);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(masters.containsKey(key)){
					// 必须更新
					continue;
				}

				Object value = BeanUtil.getFieldValue(obj, key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}
					if(!isUpdateNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}
					if(!isUpdateEmptyColumn){
						keys.remove(i);
						continue;
					}
				}

			}
		}
		if(null != ignores){
			for(String ignore:ignores){
				cols.remove(ignore.toUpperCase());
			}
		}
		cols = checkMetadata(runtime, dest, configs, cols);
		return cols;
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
	public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run)", 37));
		}
		return -1;
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
		if(null == random){
			random = random(runtime);
		}
		if(null == data){
			if(IS_THROW_SQL_UPDATE_EXCEPTION(configs)){
				throw new SQLUpdateException("save空数据");
			}else {
				log.error("save空数据");
				return -1;
			}
		}
		if(data instanceof Collection){
			Collection<?> items = (Collection<?>)data;
			long cnt = 0;
			for (Object item : items) {
				cnt += save(runtime, random, dest, item, configs, columns);
			}
			return cnt;
		}
		return saveObject(runtime, random, dest, data, configs, columns);
	}

	protected long saveCollection(DataRuntime runtime, String random, String dest, Collection<?> data, ConfigStore configs, List<String> columns){
		long cnt = 0;
		//List<Run> runs = buildInsertRun(runtime, random, batch, dest, data, columns);
		return cnt;
	}
	protected long saveObject(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		if(null == data){
			return 0;
		}
		boolean isNew = BeanUtil.checkIsNew(data);
		if(isNew){
			return insert(runtime, random, 0,  dest, data, configs, columns);
		}else{
			//是否覆盖(null:不检测直接执行update有可能影响行数=0)
			Boolean override = checkOverride(data);
			ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
			if(null != override){
				RunPrepare prepare = new DefaultTablePrepare(dest);
				Map<String, Object> pvs = checkPv(data);
				ConfigStore stores = new DefaultConfigStore();
				for(String k:pvs.keySet()){
					stores.and(k, pvs.get(k));
				}
				boolean exists = exists(runtime, random, prepare, stores);
				if(exists){
					if(override){
						return update(runtime, random, dest, data, configs, columns);
					}else{
						log.warn("[跳过更新][数据已存在:{}({})]",dest, BeanUtil.map2json(pvs));
					}
				}else{
					return insert(runtime, random, 0, dest, data, configs, columns);
				}
			}else{
				return update(runtime, random, dest, data, configs, columns);
			}
		}
		return 0;
	}
	protected Boolean checkOverride(Object obj){
		Boolean result = null;
		if(null != obj && obj instanceof DataRow){
			result = ((DataRow)obj).getOverride();
		}
		return result;
	}
	protected Map<String,Object> checkPv(Object obj){
		Map<String,Object> pvs = new HashMap<>();
		if(null != obj && obj instanceof DataRow){
			DataRow row = (DataRow) obj;
			List<String> ks = row.getPrimaryKeys();
			for(String k:ks){
				pvs.put(k, row.get(k));
			}
		}
		return pvs;
	}



	/**
	 * 是否是可以接收数组类型的值
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param key key
	 * @return boolean
	 */
	protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key){
		String table = run.getTable();
		if (null != table) {
			LinkedHashMap<String, Column> columns = columns(runtime, null, false, new Table(table), false);
			if(null != columns){
				Column column = columns.get(key.toUpperCase());
				return isMultipleValue(column);
			}
		}
		return false;
	}

	protected boolean isMultipleValue(Column column){
		if(null != column){
			String type = column.getTypeName().toUpperCase();
			if(type.contains("POINT") || type.contains("GEOMETRY") || type.contains("POLYGON")){
				return true;
			}
		}
		return false;
	}
	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, String table, ConfigStore configs, LinkedHashMap<String, Column> columns){
		if(!IS_AUTO_CHECK_METADATA(configs)){
			return columns;
		}
		LinkedHashMap<String, Column> result = new LinkedHashMap<>();
		LinkedHashMap<String, Column> metadatas = columns(runtime, null,false, new Table(table), false);
		if(metadatas.size() > 0) {
			for (String key:columns.keySet()) {
				if (metadatas.containsKey(key)) {
					result.put(key, metadatas.get(key));
				} else {
					if(IS_LOG_SQL_WARN(configs)) {
						log.warn("[{}][column:{}.{}][insert/update忽略当前列名]", LogUtil.format("列名检测不存在", 33), table, key);
					}
				}
			}
		}else{
			if(IS_LOG_SQL_WARN(configs)) {
				log.warn("[{}][table:{}][忽略列名检测]", LogUtil.format("表结构检测失败(检查表名是否存在)", 33), table);
			}
		}
		if(IS_LOG_SQL_WARN(configs)) {
			log.info("[check column metadata][src:{}][result:{}]", columns.size(), result.size());
		}
		return result;
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
		DataSet set = null;
		Long fr = System.currentTimeMillis();
		boolean cmd_success = false;
		Run run = null;
		PageNavi navi = null;

		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new DataSet();
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new DataSet();
		}

		run = buildQueryRun(runtime, prepare, configs, conditions);

		if (log.isWarnEnabled() && IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			String src = "";
			if (prepare instanceof TablePrepare) {
				src = prepare.getTable();
			} else {
				src = prepare.getText();
			}
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		navi = run.getPageNavi();
		long total = 0;
		if (run.isValid()) {
			if (null != navi) {
				if (null != dmListener) {
					dmListener.beforeTotal(runtime, random, run);
				}
				fr = System.currentTimeMillis();
				if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
					// 第一条 query中设置的标识(只查一行)
					total = 1;
				} else {
					// 未计数(总数 )
					if (navi.getTotalRow() == 0) {
						total = count(runtime, random, run);
						navi.setTotalRow(total);
					} else {
						total = navi.getTotalRow();
					}
				}
				if (null != dmListener) {
					dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
				}
				if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
					log.info("[查询记录总数][行数:{}]", total);
				}
			}
		}
		fr = System.currentTimeMillis();
		if (run.isValid()) {
			if(null == navi || total > 0){
				if(null != dmListener){
					dmListener.beforeQuery(runtime, random, run, total);
				}
				swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
				if(swt == ACTION.SWITCH.BREAK){
					return new DataSet();
				}
				set = select(runtime, random, false, prepare.getTable(), configs, run);
				cmd_success = true;
			}else{
				if(null != configs){
					configs.add(run);
				}
				set = new DataSet();
				if(IS_CHECK_EMPTY_SET_METADATA(configs)){
					set.setMetadata(metadata(runtime, prepare, false));
				}
			}
		} else {
			set = new DataSet();
		}

		set.setDataSource(prepare.getDataSource());
		set.setNavi(navi);
		if (null != navi && navi.isLazy()) {
			PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
		}

		if(null != dmListener){
			dmListener.afterQuery(runtime, random, run, cmd_success, set, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, set, navi, System.currentTimeMillis() - fr);
		return set;
	}

	/**
	 * query procedure [调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
	public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)", 37));
		}
		return new DataSet();
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
		if(null == prepare){
			prepare = new DefaultTablePrepare();
		}
		EntitySet<T> list = null;
		Long fr = System.currentTimeMillis();
		Run run = null;
		boolean cmd_success = false;
		PageNavi navi = null;

		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new EntitySet();
		}
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new EntitySet();
		}

		if(BasicUtil.isEmpty(prepare.getDataSource())) {
			//text xml格式的 不检测表名，避免一下步根据表名检测表结构
			if(prepare instanceof TextPrepare || prepare instanceof XMLPrepare){
			}else {
				prepare.setDataSource(EntityAdapterProxy.table(clazz, true));
			}
		}

		run = buildQueryRun(runtime, prepare, configs, conditions);
		if (log.isWarnEnabled() && IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		navi = run.getPageNavi();
		long total = 0;
		if (run.isValid()) {
			if (null != navi) {
				if (null != dmListener) {
					dmListener.beforeTotal(runtime, random, run);
				}
				fr = System.currentTimeMillis();
				if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
					// 第一条 query中设置的标识(只查一行)
					total = 1;
				}  else {
					// 未计数(总数 )
					if (navi.getTotalRow() == 0) {
						total = count(runtime, random, run);
						navi.setTotalRow(total);
					} else {
						total = navi.getTotalRow();
					}
				}
				if (null != dmListener) {
					dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
				}
			}
			if (log.isInfoEnabled() && IS_LOG_SQL(configs)) {
				log.info("[查询记录总数][行数:{}]", total);
			}

		}
		fr = System.currentTimeMillis();
		if (run.isValid()) {
			if((null == navi || total > 0)) {
				swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
				if(swt == ACTION.SWITCH.BREAK){
					return new EntitySet();
				}
				if (null != dmListener) {
					dmListener.beforeQuery(runtime, random, run, total);
				}
				fr = System.currentTimeMillis();
				list = select(runtime, random, clazz, run.getTable(), configs, run);
				cmd_success = false;
			}else{
				list = new EntitySet<>();
			}
		} else {
			list = new EntitySet<>();
		}
		list.setNavi(navi);
		if (null != navi && navi.isLazy()) {
			PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
		}

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, cmd_success, list, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, list, navi, System.currentTimeMillis() - fr);
		return list;
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
	protected  <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, String table, ConfigStore configs, Run run){
		EntitySet<T> set = new EntitySet<>();
		if(null == random){
			random = random(runtime);
		}
		if(null != configs){
			configs.entityClass(clazz);
		}
		DataSet rows = select(runtime, random, false, table, configs, run);
		for(DataRow row:rows){
			T entity = null;
			if(EntityAdapterProxy.hasAdapter(clazz)){
				//jdbc adapter需要参与 或者metadata里添加colun type
				entity = EntityAdapterProxy.entity(clazz, row, null);
			}else{
				entity = row.entity(clazz);
			}
			set.add(entity);
		}

		return set;
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
		List<Map<String,Object>> maps = null;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean cmd_success = false;
		Run run = null;
		if(null == random){
			random = random(runtime);
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new ArrayList<>();
		}

		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new ArrayList<>();
		}
		run = buildQueryRun(runtime, prepare, configs, conditions);
		Long fr = System.currentTimeMillis();
		if (log.isWarnEnabled() && IS_LOG_SQL(configs) && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			String src = "";
			if (prepare instanceof TablePrepare) {
				src = prepare.getTable();
			} else {
				src = prepare.getText();
			}
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
			log.warn(tmp);
		}
		if (run.isValid()) {
			swt = InterceptorProxy.beforeQuery(runtime, random,  run, null);
			if(swt == ACTION.SWITCH.BREAK){
				return new ArrayList<>();
			}
			if (null != dmListener) {
				dmListener.beforeQuery(runtime, random, run, -1);
			}
			maps = maps(runtime, random, configs, run);
			cmd_success = true;
		} else {
			maps = new ArrayList<>();
		}

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, cmd_success, maps, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, cmd_success, maps, null,System.currentTimeMillis() - fr);
		return maps;
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
		Run run = null;
		if(prepare instanceof TablePrepare){
			run = new TableRun(runtime, prepare.getTable());
		}else if(prepare instanceof XMLPrepare){
			run = new XMLRun();
		}else if(prepare instanceof TextPrepare){
			run = new TextRun();
		}
		if(null != run){
			run.setRuntime(runtime);
			//如果是text类型 将解析文本并抽取出变量
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			//先把configs中的占位值取出
			if(null != configs) {
				List<Object> statics = configs.getStaticValues();
				for (Object item : statics) {
					run.addValue(new RunValue("none", item));
				}
			}
			run.addCondition(conditions);
			if(run.checkValid()) {
				//为变量赋值 run.condition赋值
				run.init();
				//构造最终的查询SQL
				fillQueryContent(runtime, run);
			}
		}
		convert(runtime, configs, run);
		return run;
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * select[命令合成-子流程] <br/>
	 * 构造查询主体
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillQueryContent(DataRuntime runtime, Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				fillQueryContent(runtime, r);
			}else if(run instanceof XMLRun){
				XMLRun r = (XMLRun) run;
				fillQueryContent(runtime, r);
			}else if(run instanceof TextRun){
				TextRun r = (TextRun) run;
				fillQueryContent(runtime, r);
			}
		}
	}
	protected void fillQueryContent(DataRuntime runtime, XMLRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillQueryContent(DataRuntime runtime, XMLRun run)", 37));
		}
	}
	protected void fillQueryContent(DataRuntime runtime, TextRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillQueryContent(DataRuntime runtime, TextRun run)", 37));
		}
	}
	protected void fillQueryContent(DataRuntime runtime, TableRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillQueryContent(DataRuntime runtime, TableRun run)", 37));
		}
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
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)", 37));
		}
		return new DataSet();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list)", 37));
		}
		return new ArrayList<>();
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
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		long count = -1;
		Long fr = System.currentTimeMillis();
		Run run = null;
		if(null == random){
			random = random(runtime);
		}

		boolean cmd_success = false;

		ACTION.SWITCH swt = InterceptorProxy.prepareCount(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		run = buildQueryRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		if (null != dmListener) {
			dmListener.beforeCount(runtime, random, run);
		}
		swt = InterceptorProxy.beforeCount(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		fr = System.currentTimeMillis();
		count = count(runtime, random, run);
		cmd_success = true;

		if(null != dmListener){
			dmListener.afterCount(runtime, random, run, cmd_success, count, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterCount(runtime, random, run, cmd_success, count, System.currentTimeMillis() - fr);
		return count;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalTotal(DataRuntime runtime, Run run)", 37));
		}
		return null;
	}

	/**
	 * count [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	public long count(DataRuntime runtime, String random, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long count(DataRuntime runtime, String random, Run run)", 37));
		}
		return -1;
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
	public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)", 37));
		}
		return false;
	}
	@Override
	public String mergeFinalExists(DataRuntime runtime, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalExists(DataRuntime runtime, Run run)", 37));
		}
		return null;
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
		long result = -1;
		boolean cmd_success = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareExecute(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}

		Run run = buildExecuteRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long fr = System.currentTimeMillis();

		long millis = -1;
		swt = InterceptorProxy.beforeExecute(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeExecute(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		result = execute(runtime, random, configs, run);
		cmd_success = true;
		if (null != dmListener) {
			dmListener.afterExecute(runtime, random, run,  cmd_success, result, millis);
		}
		InterceptorProxy.afterExecute(runtime, random, run, cmd_success, result, System.currentTimeMillis()-fr);
		return result;
	}

	public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, String cmd, List<Object> values){
		Run run = new SimpleRun(runtime);
		if(null == random){
			random = random(runtime);
		}
		StringBuilder builder = run.getBuilder();
		builder.append(cmd);
		run.setValues(null, values);
		Object first = values.get(0);
		if(first instanceof Collection){
			List<Object> list = new ArrayList<>();
			for(Object item:values){
				Collection col = (Collection) item;
				list.addAll(col);
			}
			run.setValues(null, list);
		}
		run.setBatch(batch);
		String[] strs = cmd.split("");
		int vol = 0;
		for(String str:strs){
			if(str.equals("?")){
				vol ++;
			}
		}
		run.setVol(vol);
		long result = execute(runtime, random, configs, run);
		return result;
	}
	/**
	 * procedure [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
	public boolean execute(DataRuntime runtime, String random, Procedure procedure){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Procedure procedure)", 37));
		}
		return false;
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
		Run run = null;
		if(prepare instanceof XMLPrepare){
			run = new XMLRun();
		}else if(prepare instanceof TextPrepare){
			run = new TextRun();
		}
		if(null != run){
			run.setRuntime(runtime);
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			run.addCondition(conditions);
			run.init(); //
			//构造最终的执行SQL
			fillQueryContent(runtime, run);
		}
		return run;
	}
	protected void fillExecuteContent(DataRuntime runtime, XMLRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, XMLRun run)", 37));
		}
	}
	protected void fillExecuteContent(DataRuntime runtime, TextRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, TextRun run)", 37));
		}
	}
	protected void fillExecuteContent(DataRuntime runtime, TableRun run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, TableRun run)", 37));
		}
	}

	/**
	 * execute [命令合成-子流程]
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillExecuteContent(DataRuntime runtime, Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				fillExecuteContent(runtime, r);
			}else if(run instanceof XMLRun){
				XMLRun r = (XMLRun) run;
				fillExecuteContent(runtime, r);
			}else if(run instanceof TextRun){
				TextRun r = (TextRun) run;
				fillExecuteContent(runtime, r);
			}
		}
	}
	/**
	 * execute [命令执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	public long execute(DataRuntime runtime, String random, ConfigStore configs, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long execute(DataRuntime runtime, String random, ConfigStore configs, Run run)", 37));
		}
		return -1;
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
		table = DataSourceUtil.parseDataSource(table, null);
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = InterceptorProxy.prepareDelete(runtime, random, batch, table, key, values);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime, random, batch, table, key, values);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		Run run = buildDeleteRun(runtime, batch, table, key, values);
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][table:" +table+ "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long result = delete(runtime, random, configs, run);
		return result;
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
		dest = DataSourceUtil.parseDataSource(dest,obj);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long size = 0;
		if(null != obj){
			if(obj instanceof Collection){
				Collection list = (Collection) obj;
				for(Object item:list){
					long qty = delete(runtime, random, dest, configs, item, columns);
					//如果不执行会返回-1
					if(qty > 0){
						size += qty;
					}
				}
				if(log.isInfoEnabled()) {
					log.info("[delete Collection][影响行数:{}]", LogUtil.format(size, 34));
				}
			}else{
				swt = InterceptorProxy.prepareDelete(runtime, random, 0, dest, obj, columns);
				if(swt == ACTION.SWITCH.BREAK){
					return -1;
				}
				if(null != dmListener){
					swt = dmListener.prepareDelete(runtime, random, 0, dest, obj, columns);
				}
				if(swt == ACTION.SWITCH.BREAK){
					return -1;
				}
				Run run = buildDeleteRun(runtime, dest, obj, columns);
				if(!run.isValid()){
					if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
						log.warn("[valid:false][不具备执行条件][dest:" + dest + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
					}
					return -1;
				}
				size = delete(runtime, random, configs, run);
			}
		}
		return size;
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
		table = DataSourceUtil.parseDataSource(table, null);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		swt = InterceptorProxy.prepareDelete(runtime, random, 0, table, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime, random, 0, table, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		Run run = buildDeleteRun(runtime, table, configs, conditions);
		if(!run.isValid()){
			if(log.isWarnEnabled() && IS_LOG_SQL(configs)){
				log.warn("[valid:false][不具备执行条件][table:" + table + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long result = delete(  runtime, random, configs, run);
		return result;
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
		table = DataSourceUtil.parseDataSource(table);
		List<Run> runs = buildTruncateRun(runtime, table);
		if(null != runs && runs.size()>0) {
			RunPrepare prepare = new DefaultTextPrepare(runs.get(0).getFinalUpdate());
			return (int)execute(runtime, random, prepare, null);
		}
		return -1;
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
		if(null == obj){
			return null;
		}
		Run run = null;
		if(null == dest){
			dest = DataSourceUtil.parseDataSource(dest,obj);
		}
		if(null == dest){
			Object entity = obj;
			if(obj instanceof Collection){
				entity = ((Collection)obj).iterator().next();
			}
			Table table = EntityAdapterProxy.table(entity.getClass());
			if(null != table){
				dest = table.getName();
			}
		}
		if(obj instanceof ConfigStore){
			run = new TableRun(runtime, dest);
			RunPrepare prepare = new DefaultTablePrepare();
			prepare.setDataSource(dest);
			run.setPrepare(prepare);
			run.setConfigStore((ConfigStore)obj);
			run.addCondition(columns);
			run.init();
			fillDeleteRunContent(runtime, run);
		}else{
			run = buildDeleteRunFromEntity(runtime, dest, obj, columns);
		}
		convert(runtime, new DefaultConfigStore(), run);
		return run;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRun(DataRuntime runtime, int batch, String table, String key, Object values){
		Run run = buildDeleteRunFromTable(runtime, batch, table, key, values);
		convert(runtime, new DefaultConfigStore(), run);
		return run;
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
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, String column, Object values) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, String column, Object values)", 37));
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run buildDeleteRunFromEntity(DataRuntime runtime, String table, Object obj, String... columns)", 37));
		}
		return null;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	@Override
	public void fillDeleteRunContent(DataRuntime runtime, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillDeleteRunContent(DataRuntime runtime, Run run)", 37));
		}
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
		long result = -1;
		boolean cmd_success = false;
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long fr = System.currentTimeMillis();
		swt = InterceptorProxy.beforeDelete(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeDelete(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		long millis = -1;

		result = execute(runtime, random, configs, run);
		cmd_success = true;
		millis = System.currentTimeMillis() - fr;

		if(null != dmListener){
			dmListener.afterDelete(runtime, random, run, cmd_success, result, millis);
		}
		InterceptorProxy.afterDelete(runtime, random, run,  cmd_success, result, millis);
		return result;
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
		if(null == random){
			random = random(runtime);
		}
		List<Database> databases = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabaseRun(runtime, greedy, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						databases = databases(runtime, idx++, true, databases, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}ms]", random, databases.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
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
		if(null == random){
			random = random(runtime);
		}
		LinkedHashMap<String, Database> databases = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabaseRun(runtime, false, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						databases = databases(runtime, idx++, true, databases, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}ms]", random, databases.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	@Override
	public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, int index, boolean create, DataSet set)", 37));
		}
		return null;
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
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs)
	 *
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * catalog[调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name){
		if(null == random){
			random = random(runtime);
		}
		LinkedHashMap<String, Catalog> catalogs = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogRun(runtime, false, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						catalogs = catalogs(runtime, idx++, true, catalogs, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}ms]", random, catalogs.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
	}
	/**
	 * catalog[调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param name 名称统配符或正则
	 * @return LinkedHashMap
	 */
	public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name){
		if(null == random){
			random = random(runtime);
		}
		List<Catalog> catalogs = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogRun(runtime, greedy, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						catalogs = catalogs(runtime, idx++, true, catalogs, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}ms]", random, catalogs.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryCatalogRun(DataRuntime runtime, boolean greedy, String name)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	@Override
	public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set)", 37));
		}
		return null;
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
		if(null == random){
			random = random(runtime);
		}
		LinkedHashMap<String, Schema> schemas = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySchemaRun(runtime, false, catalog, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						schemas = schemas(runtime, idx++, true, schemas, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}ms]", random, schemas.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
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
		if(null == random){
			random = random(runtime);
		}
		List<Schema> schemas = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySchemaRun(runtime, greedy, catalog, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						schemas = schemas(runtime, idx++, true, schemas, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}ms]", random, schemas.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
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
	public List<Run> buildQuerySchemaRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySchemaRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param schemas 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	public List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas)", 37));
		}
		return new ArrayList<>();
	}
	public Schema schema(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Schema schema(DataRuntime runtime, int index, boolean create, DataSet set)", 37));
		}
		return null;
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
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			Table search = new Table();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					checkSchema(runtime, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			String origin = CacheProxy.name(greedy, catalog, schema, pattern);
			if(null == origin && ConfigTable.IS_METADATA_IGNORE_CASE){
				//先查出所有key并以大写缓存 用来实现忽略大小写
				tableMap(runtime, random, greedy, catalog, schema);
				origin = CacheProxy.name(greedy, catalog, schema, pattern);
			}
			if(null == origin){
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);

			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryTableRun(runtime, greedy, catalog, schema, origin, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						list = tables(runtime, idx++, true, catalog, schema, list, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(list.size() == 0) {
				try {
					list = tables(runtime, true, list, catalog, schema, origin, tps);
					//删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
					if(!greedy){
						int size = list.size();
						for(int i=size-1;i>=0; i--){
							Table item = list.get(i);
							if(!(catalog+"_"+schema).equalsIgnoreCase(item.getCatalog()+"_"+item.getSchema())){
								list.remove(i);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[tables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(Table table:list){
				if(BasicUtil.isNotEmpty(table.getComment())){
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryTableCommentRun(runtime, catalog, schema, null, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
							list = comments(runtime, idx++, true, catalog, schema, list, set);
							//merge(list, maps);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
						log.info("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, origin, types, list.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(origin)){
				origin = origin.replace("%", ".*");
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
				for(T item:list){
					String name = item.getName(greedy);
					if(RegularUtil.match(name, origin)){
						tmp.add(item);
					}
				}
				list = tmp;
			}
			if(strut){
				//查询所有表结构
				List<Column> columns = columns(runtime, random, greedy, catalog, schema, pattern);
				for(Table table:list){
					String tName = table.getName();
					Catalog tCatalog = table.getCatalog();
					Schema tSchema = table.getSchema();
					Long tObjectId = table.getObjectId();
					LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
					table.setColumns(cols);
					for(Column column:columns){
						if(tName.equalsIgnoreCase(column.getTableName(false))){
							Catalog  cCatalog = column.getCatalog();
							Schema cSchema = column.getSchema();
							Long cObjectId = column.getObjectId();
							if(null != tObjectId && null != cObjectId && tObjectId == cObjectId){
								cols.put(column.getName().toUpperCase(), column);
							}else{
								if( null == cCatalog  || cCatalog.equals(tCatalog)){
									if(null == cSchema || cSchema.equal(tSchema)){
										cols.put(column.getName().toUpperCase(), column);
									}
								}
							}
						}
					}
					columns.removeAll(cols.values());
				}
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[tables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	protected void tableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema){
		Map<String, String> names = CacheProxy.names(catalog, schema);
		if(null == names || names.isEmpty()){
			if(null == random){
				random = random(runtime);
			}
			DriverAdapter adapter = runtime.getAdapter();
			List<Table> tables = null;
			boolean sys = false; //根据系统表查询
			if(greedy){
				catalog = null;
				schema = null;
			}
			try {
				List<Run> runs =buildQueryTableRun(runtime, greedy, catalog, schema, null, null);
				if (null != runs && runs.size() > 0) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tables = tables(runtime, idx++, true, catalog, schema, tables, set);
						CacheProxy.name(tables);
						sys = true;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			if(!sys){
				try {
					tables = tables(runtime, true,  tables, catalog, schema, null, null);
					CacheProxy.name(tables);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}

	}

	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean strut){
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		List<T> list = tables(runtime, random, false, catalog, schema, pattern, types);
		for(T table:list){
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildQueryTableCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTableCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, String ... types)", 37));
		}
		if(null == tables){
			tables = new ArrayList<>();
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
	public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)", 37));
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
	public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set)", 37));
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
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = columns(runtime, random, false, table, true);
				table.setColumns(columns);
				table.setTags(tags(runtime, random, false, table));
				PrimaryKey pk = primary(runtime, random, false, table);
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				table.setPrimaryKey(pk);
				table.setIndexs(indexs(runtime, random, table, null));
				runs = buildCreateRun(runtime, table);
				for(Run run:runs){
					list.add(run.getFinalUpdate());
					table.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
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
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
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
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
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
		LinkedHashMap<String,T> views = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			View search = new View();
			if(null == catalog || null == schema){
				View tmp = new View();
				if(!greedy) {
					checkSchema(runtime, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			search.setName(pattern);
			search.setCatalog(catalog);
			search.setSchema(schema);

			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}else{
				tps = new String[]{"VIEW"};
			}

			DataRow view_map = CacheProxy.getViewMaps(runtime.datasource());
			if(null != pattern){
				if(view_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败,先查询全部表,生成缓存,再从缓存中不区分大小写查询
					LinkedHashMap<String,View> all = views(runtime, random, greedy, catalog, schema, null, types);
					if(!greedy) {
						for (View view : all.values()) {
							if ((catalog + "_" + schema).equals(view.getCatalog() + "_" + view.getSchema())) {
								view_map.put(view.getName(greedy).toUpperCase(), view.getName(greedy));
							}
						}
					}
				}
				if(view_map.containsKey(search.getName(greedy).toUpperCase())){
					pattern = view_map.getString(search.getName(greedy).toUpperCase());
				}else{
					pattern = search.getName(greedy);
				}
			}
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryViewRun(runtime, greedy, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						views = views(runtime, idx++, true, catalog, schema, views, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}
			if(null == views || views.isEmpty()) {
				// 根据驱动内置接口补充
				try {
					LinkedHashMap<String, T> tmps = views(runtime, true, null, catalog, schema, pattern, tps);
					for (String key : tmps.keySet()) {
						if (!views.containsKey(key.toUpperCase())) {
							T item = tmps.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									views.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[views][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, pattern, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[views][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, views.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(pattern)){
				pattern = pattern.replace("%", ".*");
				LinkedHashMap<String,T> tmps = new LinkedHashMap<>();
				List<String> keys = BeanUtil.getMapKeys(views);
				for(String key:keys){
					T item = views.get(key);
					String name = item.getName(greedy);
					if(RegularUtil.match(name, pattern)){
						tmps.put(name.toUpperCase(), item);
					}
				}
				views = tmps;
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[views][result:fail][msg:{}]", e.toString());
			}
		}
		return views;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryViewRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> views, DataSet set)", 37));
		}
		if(null == views){
			views = new LinkedHashMap<>();
		}
		return views;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, String ... types)", 37));
		}
		if(null == views){
			views = new LinkedHashMap<>();
		}
		return views;
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
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, view);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, view, list,  set);
				}
				view.setDdls(list);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[view ddl][view:{}][result:{}][执行耗时:{}ms]", random, view.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[view ddl][{}][view:{}][msg:{}]", random, LogUtil.format("查询视图创建DDL失败", 33), view.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, View view)", 37));
		}
		return runs;
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
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
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
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, String types){
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					checkSchema(runtime, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}
			DataRow table_map = CacheProxy.getTableMaps(runtime.datasource());
			if(null != pattern){
				if(table_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败,先查询全部表,生成缓存,再从缓存中不区分大小写查询
					LinkedHashMap<String, MasterTable> all = mtables(runtime, random, greedy, catalog, schema, null, types);
					for(Table table:all.values()){
						table_map.put(table.getName().toUpperCase(), table.getName());
					}
				}
				if(table_map.containsKey(pattern.toUpperCase())){
					pattern = table_map.getString(pattern.toUpperCase());
				}
			}

			// 根据系统表查询
			try{
				List<Run> runs = buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tables = mtables(runtime, idx++, true, catalog, schema, tables, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			if(null == tables || tables.isEmpty() ) {
				// 根据驱动内置接口补充
				try {
					LinkedHashMap<String, T> tmps = mtables(runtime, true, null, catalog, schema, pattern, tps);
					for (String key : tmps.keySet()) {
						if (!tables.containsKey(key.toUpperCase())) {
							T item = tmps.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									tables.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, pattern, e.toString());
					}
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[stables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[mtables][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryMasterTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
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
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
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
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, String ... types)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 * master table[调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table MasterTable
	 * @return List
	 */
	public List<String> ddl(DataRuntime runtime, String random, MasterTable table){

		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[master table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[master table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询主表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, MasterTable table)", 37));
		}
		return runs;
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
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
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
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern){
		LinkedHashMap<String,T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryPartitionTableRun(runtime, master, tags, pattern);
				if(null != runs) {
					int idx = 0;
					int total = runs.size();
					for(Run run:runs) {
						DataSet set = select(runtime, random, false, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
						tables = ptables(runtime, total, idx++, true, master, master.getCatalog(), master.getSchema(), tables, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE){
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.toString());
				}
			}

			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tables][stable:{}][result:{}][执行耗时:{}ms]", random, master.getName(), tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[ptables][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTableRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags)", 37));
		}
		return new ArrayList<>();
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
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, PartitionTable> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable table, Catalog catalog, Schema schema, LinkedHashMap<String, PartitionTable> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, PartitionTable> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}
	/**
	 * partition table[调用入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[partition table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[partition table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询子表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, PartitionTable table)", 37));
		}
		return runs;
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
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
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
	 * 查询表结构(多方法合成)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary)", 37));
		}
		return new LinkedHashMap<>();
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
	public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table){
		List<T> columns = new ArrayList<>();
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		Table tab = new Table(table);
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		if(BasicUtil.isEmpty(catalog) && BasicUtil.isEmpty(schema) && !greedy){
			checkSchema(runtime, tab);
		}
		//根据系统表查询
		try {
			List<Run> runs = buildQueryColumnRun(runtime, tab, false);
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = select(runtime, random, true, (String) null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run);
					columns = columns(runtime, idx, true, tab, columns, set);
					idx++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return columns;
	}

	/**
	 * column[调用入口]<br/>
	 * DatabaseMetaData(方法3)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @return pattern 列名称通配符
	 * @throws Exception 异常
	 */
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[结果集封装](方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 *  根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * column[结果集封装](方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 *  根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)", 37));
		}
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)", 37));
		}
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
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
	 * PrimaryKey primary(DataRuntime runtime, Table table)
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
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table){
		PrimaryKey primary = null;
		if(!greedy) {
			checkSchema(runtime, table);
		}
		String tab = table.getName();
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		if(null == random) {
			random = random(runtime);
		}
		try{
			List<Run> runs = buildQueryPrimaryRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, false, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					primary = primary(runtime, idx, table, set);
					if(null != primary){
						primary.setTable(table);
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.warn("{}[primary][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.toString());
			}
		}
		table.setPrimaryKey(primary);
		return primary;
	}

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
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
	public PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @throws Exception 异常
	 */
	public PrimaryKey primary(DataRuntime runtime, Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 PrimaryKey primary(DataRuntime runtime, Table table)", 37));
		}
		return null;
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
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table){
		LinkedHashMap<String, T> foreigns = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		try {
			List<Run> runs = buildQueryForeignRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					foreigns = foreigns(runtime, idx,  table, foreigns, set);
					idx++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
		}
		return foreigns;
	}
	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryForeignRun(DataRuntime runtime, Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryForeignRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
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
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}



	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name)
	 * [结果集封装]
	 * <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set)
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set)
	 * <T extends Index> List< T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate)
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
	public <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
		List<T> indexs = null;
		if(null == table){
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		if(null != table.getName()) {
			try {
				LinkedHashMap<String,T> maps = indexs(runtime, true, new LinkedHashMap<>(), table, false, false);
				table.setIndexs(maps);
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
			}
		}
		List<Run> runs = buildQueryIndexRun(runtime, table, pattern);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					indexs = indexs(runtime, idx, true, table, indexs, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		if(null == indexs){
			indexs = new ArrayList<>();
		}
		return indexs;
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
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, Table table, String pattern){
		LinkedHashMap<String,T> indexs = null;
		if(null == table){
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}

		checkSchema(runtime, table);

		if(null != table.getName()) {
			try {
				indexs = indexs(runtime, true, indexs, table, false, false);
				table.setIndexs(indexs);
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
			}
			if(BasicUtil.isNotEmpty(pattern)){
				T index = indexs.get(pattern.toUpperCase());
				indexs = new LinkedHashMap<>();
				indexs.put(pattern.toUpperCase(), index);
			}
		}
		List<Run> runs = buildQueryIndexRun(runtime, table, pattern);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
				try {
					indexs = indexs(runtime, idx, true, table, indexs, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		Index pk = null;
		if(null != indexs) {
			for (Index index : indexs.values()) {
				if (index.isPrimary()) {
					pk = index;
					break;
				}
			}
		}
		if(null == pk) {
			//识别主键索引
			pk = table.getPrimaryKey();
			if (null == pk) {
				pk = primary(runtime, random, false, table);
			}
			if (null != pk) {
				Index index = indexs.get(pk.getName().toUpperCase());
				if (null != index) {
					index.setPrimary(true);
				} else {
					indexs.put(pk.getName().toUpperCase(), (T) pk);
				}
			}
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set)", 37));
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set)", 37));
		}
		if(null == indexs){
			indexs = new ArrayList<>();
		}
		return indexs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate)", 37));
		}
		if(null == indexs){
			indexs = new ArrayList<>();
		}
		return indexs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate)", 37));
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
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
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, Column column, String pattern)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set)", 37));
		}
		if(null == constraints){
			constraints = new ArrayList<>();
		}
		return constraints;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, Constraint>  constraints(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set)", 37));
		}
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * trigger[命令合成]
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)", 37));
		}
		return new ArrayList<>();
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
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)", 37));
		}
		if(null == triggers){
			triggers = new LinkedHashMap<>();
		}
		return triggers;
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random,  boolean greedy, Catalog catalog, Schema schema, String pattern);
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryProcedureRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
	 * [结果集封装]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures)
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception;
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
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random,  boolean greedy, Catalog catalog, Schema schema, String pattern){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> ArrayList<T> procedures(DataRuntime runtime, String random,  boolean greedy, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
	}/**
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
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new LinkedHashMap<>();
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

	public List<Run> buildQueryProcedureRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryProcedureRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, procedure);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, procedure, list,  set);
				}
				if(list.size()>0) {
					procedure.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装

			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[procedure ddl][procedure:{}][result:{}][执行耗时:{}ms]", random, procedure.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[procedure ddl][{}][procedure:{}][msg:{}]", random, LogUtil.format("查询存储过程的创建DDL失败", 33), procedure.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, Procedure procedure)", 37));
		}
		return runs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set)", 37));
		}
		return new ArrayList<>();
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
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions)
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)
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
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 * function[命令合成]
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern 名称统配符或正则
	 * @return sqls
	 */
	public List<Run> buildQueryFunctionRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryFunctionRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[结果集封装]
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * function[结果集封装]
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param functions 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set)", 37));
		}
		return new ArrayList<>();
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
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)", 37));
		}
		return new LinkedHashMap<>();
	}
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Function
	 * @return ddl
	 */
	public List<String> ddl(DataRuntime runtime, String random, Function meta){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, meta);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, new DefaultConfigStore().keyCase(KeyAdapter.KEY_CASE.PUT_UPPER), run).toUpperKey();
					list = ddl(runtime, idx++, meta, list,  set);
				}
				if(list.size()>0) {
					meta.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[function ddl][function:{}][result:{}][执行耗时:{}ms]", random, meta.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[function ddl][{}][function:{}][msg:{}]", random, LogUtil.format("查询函数的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
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
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDDLRun(DataRuntime runtime, Function meta)", 37));
		}
		return runs;
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
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
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
	public <T extends Table> T table(List<T> tables, Catalog catalog, Schema schema, String name){
		if(null != tables){
			for(T table:tables){
				if((null == catalog || catalog.equal(table.getCatalog()))
						&& (null == schema || schema.equal(table.getSchema()))
						&& table.getName().equalsIgnoreCase(name)
				){
					return table;
				}
			}
		}
		return null;
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
	public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name){
		if(null != schemas){
			for(T schema:schemas){
				if((null == catalog || catalog.equal(schema.getCatalog()))
						&& schema.getName().equalsIgnoreCase(name)
				){
					return schema;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * 根据 name检测catalogs集合中是否存在
	 * @param catalogs catalogs
	 * @param name name
	 * @return 如果存在则返回 Catalog 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Catalog> T catalog(List<T> catalogs, String name){
		if(null != catalogs){
			for(T catalog:catalogs){
				if(catalog.getName().equalsIgnoreCase(name)){
					return catalog;
				}
			}
		}
		return null;
	}
	/**
	 *
	 * 根据 name检测databases集合中是否存在
	 * @param databases databases
	 * @param name name
	 * @return 如果存在则返回 Database 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Database> T database(List<T> databases, String name){
		if(null != databases){
			for(T database:databases){
				if(database.getName().equalsIgnoreCase(name)){
					return database;
				}
			}
		}
		return null;
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
	public boolean execute(DataRuntime runtime, String random, BaseMetadata meta, ACTION.DDL action, Run run){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, BaseMetadata meta, ACTION.DDL action, Run run)", 37));
		}
		return false;
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
	 * List<Run> buildCreateRun(DataRuntime runtime, Table table)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table table)
	 * List<Run> buildDropRun(DataRuntime runtime, Table table)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * time runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/
	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean create(DataRuntime runtime, Table meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */

	public boolean alter(DataRuntime runtime, Table meta) throws Exception{
		boolean result = true;
		List<Run> runs = new ArrayList<>();
		Table update = (Table)meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = meta.getName();
		String uname = update.getName();
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_ALTER, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE) {
			swt = ddListener.parepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		checkSchema(runtime, update);

		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, meta, uname);
			meta.setName(uname);
		}
		if(!result){
			return result;
		}

		//修改表备注
		String comment = update.getComment()+"";
		if(!comment.equals(meta.getComment())){
			swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_COMMENT, meta);
			if(swt != ACTION.SWITCH.BREAK) {
				if(BasicUtil.isNotEmpty(meta.getComment())) {
					runs.addAll(buildChangeCommentRun(runtime, update));
				}else{
					runs.addAll(buildAppendCommentRun(runtime, update));
				}
				swt = InterceptorProxy.before(runtime, random, ACTION.DDL.TABLE_COMMENT, meta, runs);
				if(swt != ACTION.SWITCH.BREAK) {
					long cmt_fr = System.currentTimeMillis();
					result = execute(runtime, random, meta, ACTION.DDL.TABLE_COMMENT, runs) && result;
					InterceptorProxy.after(runtime, random, ACTION.DDL.TABLE_COMMENT, meta, runs, result, System.currentTimeMillis()-cmt_fr);
				}
			}
		}

		Map<String, Column> cols = new LinkedHashMap<>();

		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			//先根据原列名 找到数据库中定义的列
			Column column = columns.get(ucolumn.getName().toUpperCase());
			//再检测update(如果name不一样需要rename)
			if(null != ucolumn.getUpdate()){
				ucolumn = ucolumn.getUpdate();
			}
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn, false, false);
					/*
					alter(rutime, column);
					result = true;*/
					column.setAction(ACTION.DDL.COLUMN_ALTER);
					cols.put(column.getName().toUpperCase(), column);
				}
			} else {
				// 添加列
				ucolumn.setTable(update);
				/*
				add(ucolumn);
				result = true;*/
				ucolumn.setAction(ACTION.DDL.COLUMN_ADD);
				cols.put(ucolumn.getName().toUpperCase(), ucolumn);
			}
		}
		List<String> deletes = new ArrayList<>();
		// 删除列(根据删除标记)
		for (Column column : ucolumns.values()) {
			if (column.isDrop()) {
				/*drop(column);*/
				deletes.add(column.getName().toUpperCase());
				column.setAction(ACTION.DDL.COLUMN_DROP);
				cols.put(column.getName().toUpperCase(), column);
			}
		}
		// 删除列(根据新旧对比)
		if (meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if (column instanceof Tag) {
					continue;
				}
				if (column.isDrop() || deletes.contains(column.getName().toUpperCase()) || ACTION.DDL.COLUMN_DROP == column.getAction()) {
					//上一步已删除
					continue;
				}

				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					/*
					drop(column);
					result = true;*/
					column.setAction(ACTION.DDL.COLUMN_DROP);
					cols.put(column.getName().toUpperCase(), column);
				}
			}
		}

		//主键
		PrimaryKey src_primary = primary(runtime, random, false, meta);
		PrimaryKey cur_primary = update.getPrimaryKey();
		String src_define = "";
		String cur_define = "";
		if(null != src_primary) {
			src_define = BeanUtil.concat(src_primary.getColumns().values(), "name", ",", false, true);
		}
		if(null != cur_primary){
			cur_define= BeanUtil.concat(cur_primary.getColumns().values(),"name", ",", false, true);
		}
		boolean change_pk = !cur_define.equalsIgnoreCase(src_define);
		if(null != src_primary){
			//如果主键有更新 先删除主键 避免alters中把原主键列的非空取消时与主键约束冲突
			if(change_pk){
				LinkedHashMap<String,Column> pks = src_primary.getColumns();
				LinkedHashMap<String,Column> npks = cur_primary.getColumns();
				for(String k:pks.keySet()){
					Column auto = columns.get(k.toUpperCase());
					if(null != auto && auto.isAutoIncrement() == 1){//原主键科自增
						if(!npks.containsKey(auto.getName().toUpperCase())){ //当前不是主键
							auto.primary(false);
							runs = buildDropAutoIncrement(runtime, auto);
							result = execute(runtime, random, meta, ACTION.DDL.TABLE_ALTER, runs) && result;
						}
					}
				}
				//删除主键
				if(null != src_primary){
					drop(runtime, src_primary);
				}
			}
		}
		List<Run> alters = buildAlterRun(runtime, meta, cols.values());
		if(null != alters && alters.size()>0){
			result = execute(runtime, random, meta, ACTION.DDL.COLUMN_ALTER, alters) && result;
		}
		//在alters执行完成后 添加主键 避免主键中存在alerts新添加的列
		if(change_pk){
			//添加主键
			if(null != cur_primary) {
				add(runtime, cur_primary);
			}
		}
		return result;
	}
	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */

	public boolean drop(DataRuntime runtime, Table meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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

	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * table[命令合成]<br/>
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns) throws Exception{
		List<Run> runs = new ArrayList<>();
		for(Column column:columns){
			ACTION.DDL action = column.getAction();
			CMD cmd = null;
			if(null != action){
				cmd = action.getCmd();
			}
			if(CMD.CREATE == cmd){
				runs.addAll(buildAddRun(runtime, column, false));
			}else if(CMD.ALTER == cmd){
				runs.addAll(buildAlterRun(runtime, column, false));
			}else if(CMD.DROP == cmd){
				runs.addAll(buildDropRun(runtime, column, false));
			}
		}
		return runs;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
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
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
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
	public StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
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
	public StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}


	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, View view) throws Exception;
	 * boolean alter(DataRuntime runtime, View view) throws Exception;
	 * boolean drop(DataRuntime runtime, View view) throws Exception;
	 * boolean rename(DataRuntime runtime, View origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean create(DataRuntime runtime, View meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.VIEW_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, View meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.VIEW_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}


	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, View meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.VIEW_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.VIEW_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
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
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception{
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		boolean result = true;
		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = meta.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();

		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		// 更新标签
		for(Tag utag : utags.values()){
			Tag tag = tags.get(utag.getName().toUpperCase());
			if(null != tag){
				// 修改列
				tag.setTable(update);
				tag.setUpdate(utag, false, false);
				alter(runtime, tag);
				result = true;
			}else{
				// 添加列
				utag.setTable(update);
				add(runtime, utag);
				result = true;
			}
		}
		// 删除标签
		if(meta.isAutoDropColumn()) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(runtime, tag);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter master table][table:{}][result:{}][执行耗时:{}ms]", random, meta.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				swt = InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception{
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		boolean result = true;

		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		if (null != ddListener) {

		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter partition table][table:{}][result:{}][执行耗时:{}ms]", random, meta.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */

	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getMasterName(), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Column meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);

		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean alter(DataRuntime runtime, Column meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			LinkedHashMap<String, Table> tables = tables(runtime, null,  meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.isEmpty()){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的SQL通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, Column meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/**
	 * column[命令合成]<br/>
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta) throws Exception{
		return buildAddRun(runtime, meta, false);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta) throws Exception{
		return buildAlterRun(runtime, meta, false);
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta) throws Exception{
		return buildDropRun(runtime, meta, false);
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}


	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	@Override
	public String alterColumnKeyword(DataRuntime runtime){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String alterColumnKeyword(DataRuntime runtime)", 37));
		}
		return null;
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
	public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
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
	public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}


	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, boolean isIgnorePrecision, boolean isIgnoreScale)", 37));
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean isIgnorePrecision(DataRuntime runtime, Column meta)", 37));
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean isIgnoreScale(DataRuntime runtime, Column meta)", 37));
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
		type = type.toUpperCase();
		if (type.contains("INT")) {
			return false;
		}
		if (type.contains("DATE")) {
			return true;
		}
		if (type.contains("TIME")) {
			return true;
		}
		if (type.contains("YEAR")) {
			return true;
		}
		if (type.contains("TEXT")) {
			return true;
		}
		if (type.contains("BLOB")) {
			return true;
		}
		if (type.contains("JSON")) {
			return true;
		}
		if (type.contains("POINT")) {
			return true;
		}
		if (type.contains("LINE")) {
			return true;
		}
		if (type.contains("POLYGON")) {
			return true;
		}
		if (type.contains("GEOMETRY")) {
			return true;
		}
		return null;
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
		type = type.toUpperCase();
		if (type.contains("INT")) {
			return true;
		}
		if (type.contains("DATE")) {
			return true;
		}
		if (type.contains("TIME")) {
			return true;
		}
		if (type.contains("YEAR")) {
			return true;
		}
		if (type.contains("TEXT")) {
			return true;
		}
		if (type.contains("BLOB")) {
			return true;
		}
		if (type.contains("JSON")) {
			return true;
		}
		if (type.contains("POINT")) {
			return true;
		}
		if (type.contains("LINE")) {
			return true;
		}
		if (type.contains("POLYGON")) {
			return true;
		}
		if (type.contains("GEOMETRY")) {
			return true;
		}
		return null;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
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
	 * List<Run> buildAddRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag)
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
	public boolean add(DataRuntime runtime, Tag meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改TAG执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改TAG执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.isEmpty()){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else {
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
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
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables( runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.isEmpty()){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PRIMARY_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, table, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PRIMARY_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PRIMARY_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.FOREIGN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(runtime,  null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() == 0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}

	/**
	 * foreign[调用入口]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.FOREIGN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.FOREIGN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/**
	 * foreign[命令合成]
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
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
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Index meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.INDEX_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Index meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(runtime,  null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.isEmpty()){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.INDEX_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Index meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.INDEX_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.INDEX_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
	}
	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
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
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception{
		Table table = meta.getTable(true);
		if(null == table){
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.isEmpty()){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
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
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime,  Trigger meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime,  Trigger meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime,  Trigger origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
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
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta)", 37));
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
	public boolean create(DataRuntime runtime, Procedure meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random,  action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result,  millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Procedure meta) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PROCEDURE_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Procedure meta) throws Exception{
		boolean result = true;
		ACTION.DDL action = ACTION.DDL.PROCEDURE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.PROCEDURE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null == ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, origin.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)", 37));
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
	public boolean create(DataRuntime runtime, Function meta) throws Exception{
		boolean result = true;
		ACTION.DDL action = ACTION.DDL.FUNCTION_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			swt = ACTION.SWITCH.CONTINUE;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}

			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * function[调用入口]
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean alter(DataRuntime runtime, Function meta) throws Exception{
		boolean result = false;
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_ALTER;
		ACTION.SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt =  ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * function[调用入口]
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	public boolean drop(DataRuntime runtime, Function meta) throws Exception{
		boolean result = false;
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_DROP;
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, meta);

		List<Run> runs = buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
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
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception{
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.FUNCTION_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, origin);

		List<Run> runs = buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == ACTION.SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, origin, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][cmds:{}][result:{}][执行耗时:{}ms]" , random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = ACTION.SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == ACTION.SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/**
	 * function[命令合成]
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}


	/**
	 * function[命令合成]
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(DataRuntime runtime, Column column)
	 *  boolean isNumberColumn(DataRuntime runtime, Column column)
	 * boolean isCharColumn(DataRuntime runtime, Column column)
	 * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 *
	 * protected String string(List<String> keys, String key, ResultSet set, String def) throws Exception
	 * protected String string(List<String> keys, String key, ResultSet set) throws Exception
	 * protected Integer integer(List<String> keys, String key, ResultSet set, Integer def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, Boolean def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, int def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set, Object def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set) throws Exception
	 ******************************************************************************************************************/

	protected PrimaryGenerator checkPrimaryGenerator(DatabaseType type, String table){
		//针对当前表的生成器
		PrimaryGenerator generator = GeneratorConfig.get(table);
		if(null != generator){
			if(generator != PrimaryGenerator.GENERATOR.DISABLE && generator != PrimaryGenerator.GENERATOR.AUTO) {
				return generator;
			}
		}
		//全局配置
		if(null == primaryGenerator){
			if(null == primaryGenerator){
				primaryGenerator = GeneratorConfig.get();
			}
			if(null == primaryGenerator) {
				//全局配置
				if (ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.SNOWFLAKE;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.RANDOM;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.UUID;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIME;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIMESTAMP;
				}
			}
		}
		if(null != primaryGenerator) {
			return primaryGenerator;
		}else{
			return null;
		}
	}

	/**
	 * 转换成相应数据库类型
	 * @param type type
	 * @return String
	 */
	@Override
	public ColumnType type(String type){
		if(null == type){
			return null;
		}
		boolean array = false;
		if(type.startsWith("_")){
			type = type.substring(1);
			array = true;
		}
		if(type.endsWith("[]")){
			type = type.replace("[]","");
			array = true;
		}
		ColumnType ct = types.get(type.toUpperCase());
		if(null != ct){
			ct.setArray(array);
		}
		return ct;
	}
	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param meta BaseMetadata
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, BaseMetadata meta){
		Catalog catalog = meta.getCatalog();
		Schema schema = meta.getSchema();
		String name = meta.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, name);
		return builder;
	}
	@Override
	public boolean isBooleanColumn(DataRuntime runtime, Column column) {
		String clazz = column.getClassName();
		if(null != clazz){
			clazz = clazz.toLowerCase();
			if(clazz.contains("boolean")){
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type){
				type = type.toLowerCase();
				if(type.equals("bit") || type.equals("bool")){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public  boolean isNumberColumn(DataRuntime runtime, Column column){
		String clazz = column.getClassName();
		if(null != clazz){
			clazz = clazz.toLowerCase();
			if(
					clazz.startsWith("int")
							|| clazz.contains("integer")
							|| clazz.contains("long")
							|| clazz.contains("decimal")
							|| clazz.contains("float")
							|| clazz.contains("double")
							|| clazz.contains("timestamp")
							// || clazz.contains("bit")
							|| clazz.contains("short")
			){
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type){
				type = type.toLowerCase();
				if(type.startsWith("int")
						||type.contains("float")
						||type.contains("double")
						||type.contains("short")
						||type.contains("long")
						||type.contains("decimal")
						||type.contains("numeric")
						||type.contains("timestamp")
				){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return !isNumberColumn(runtime, column) && !isBooleanColumn(runtime, column);
	}
	/**
	 * 内置函数
	 * @param column 列属性
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
		return null;
	}

	/**
	 * 先检测rs中是否包含当前key 如果包含再取值, 取值时按keys中的大小写为准
	 * @param keys keys
	 * @param key key
	 * @param set ResultSet
	 * @return String
	 * @throws Exception 异常
	 */
	protected String string(Map<String, Integer> keys, String key, ResultSet set, String def) throws Exception{
		Object value = value(keys, key, set);
		if(null != value){
			return value.toString();
		}
		return def;
	}
	protected String string(Map<String, Integer> keys, String key, ResultSet set) throws Exception{
		return string(keys, key, set, null);
	}
	protected Integer integer(Map<String, Integer> keys, String key, ResultSet set, Integer def) throws Exception{
		Object value = value(keys, key, set);
		if(null != value){
			return BasicUtil.parseInt(value, def);
		}
		return null;
	}
	protected Long longs(Map<String, Integer> keys, String key, ResultSet set, Long def) throws Exception{
		Object value = value(keys, key, set);
		if(null != value){
			return BasicUtil.parseLong(value, def);
		}
		return null;
	}
	protected Boolean bool(Map<String, Integer> keys, String key, ResultSet set, Boolean def) throws Exception{
		Object value = value(keys, key, set);
		if(null != value){
			return BasicUtil.parseBoolean(value, def);
		}
		return null;
	}
	protected Boolean bool(Map<String, Integer> keys, String key, ResultSet set, int def) throws Exception{
		Boolean defaultValue = null;
		if(def == 0){
			defaultValue = false;
		}else if(def == 1){
			defaultValue = true;
		}
		return bool(keys, key, set, defaultValue);
	}

	/**
	 * 从resultset中根据名列取值
	 * @param keys 列名位置
	 * @param key 列名 多个以,分隔
	 * @param set result
	 * @param def 默认值
	 * @return Object
	 * @throws Exception Exception
	 */
	protected Object value(Map<String, Integer> keys, String key, ResultSet set, Object def) throws Exception{
		String[] ks = key.split(",");
		Object result = null;
		for(String k:ks){
			Integer index = keys.get(k);
			if(null != index && index >= 0){
				try {
					// db2 直接用 set.getObject(String) 可能发生 参数无效：未知列名 String
					result =  set.getObject(index);
					if(null != result){
						return result;
					}
				}catch (Exception e){

				}
			}
		}
		return def;
	}
	protected Object value(Map<String, Integer> keys, String key, ResultSet set) throws Exception{
		return value(keys, key, set, null);
	}

	@Override
	public String getPrimaryKey(DataRuntime runtime, Object obj){
		if(null == obj){
			return null;
		}
		if(obj instanceof DataRow){
			return ((DataRow)obj).getPrimaryKey();
		}else{
			return EntityAdapterProxy.primaryKey(obj.getClass(), true);
		}
	}
	@Override
	public Object getPrimaryValue(DataRuntime runtime, Object obj){
		if(null == obj){
			return null;
		}
		if(obj instanceof DataRow){
			return ((DataRow)obj).getPrimaryValue();
		}else{
			return EntityAdapterProxy.primaryValue(obj);
		}
	}

	public String parseTable(String table){
		if(null == table){
			return table;
		}
		table = table.replace(getDelimiterFr(), "").replace(getDelimiterTo(), "");
		table = DataSourceUtil.parseDataSource(table, null);
		if(table.contains(".")){
			String tmps[] = table.split("\\.");
			table = SQLUtil.delimiter(tmps[0],getDelimiterFr() , getDelimiterTo())
					+ "."
					+ SQLUtil.delimiter(tmps[1],getDelimiterFr() , getDelimiterTo());
		}else{
			table = SQLUtil.delimiter(table,getDelimiterFr() , getDelimiterTo());
		}
		return table;
	}


	/**
	 * 写入数据库前类型转换<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	@Override
	public Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder){
		if(null == value || "NULL".equals(value)){
			return null;
		}
		Object result = null;
		ColumnType columnType = null;
		DataWriter writer = null;
		boolean isArray = false;
		if(null != metadata){
			isArray = metadata.isArray();
			//根据列类型
			columnType = metadata.getColumnType();
			if(null != columnType){
				writer = writer(columnType);
			}
			if(null == writer){
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					writer = writer(typeName);
					if(null != columnType){
						writer = writer(type(typeName.toUpperCase()));
					}
				}
			}
		}
		if(null == columnType){
			columnType = type(value.getClass().getSimpleName());
		}
		if(null != columnType){
			Class writeClass = columnType.compatible();
			value = ConvertAdapter.convert(value, writeClass, isArray);
		}

		if(null != columnType){//根据列类型定位writer
			writer = writer(columnType);
		}
		if(null == writer && null != value){//根据值类型定位writer
			writer = writer(value.getClass());
		}
		if(null != writer){
			result = writer.write(value, placeholder);
		}
		if(null != result){
			return result;
		}
		if(null != columnType){
			result = columnType.write(value, null, false);
		}
		if(null != result){
			return result;
		}
		//根据值类型
		if(!placeholder) {
			if (BasicUtil.isNumber(value) || "NULL".equals(value)) {
				result = value;
			} else {
				result = "'" + value + "'";
			}
		}

		return result;
	}
	/**
	 * 从数据库中读取数据<br/>
	 * 先由子类根据metadata.typeName(CHAR,INT)定位到具体的数据库类型ColumnType<br/>
	 * 如果定准成功由CoumnType根据class转换(class可不提供)<br/>
	 * 如果没有定位到ColumnType再根据className(String,BigDecimal)定位到JavaType<br/>
	 * 如果定准失败或转换失败(返回null)再由父类转换<br/>
	 * 如果没有提供metadata和class则根据value.class<br/>
	 * 常用类型jdbc可以自动转换直接返回就可以(一般子类DataType返回null父类原样返回)<br/>
	 * 不常用的如json/point/polygon/blob等转换成anyline对应的类型<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时应该指定属性class, DataRow赋值时可以通过JDBChandler指定class)
	 * @return Object
	 */
	@Override
	public Object read(DataRuntime runtime, Column metadata, Object value, Class clazz){
		//Object result = ConvertAdapter.convert(value, clazz);
		Object result = value;
		if(null == value){
			return null;
		}
		DataReader reader = null;
		ColumnType ctype = null;
		if (null != metadata) {
			ctype = metadata.getColumnType();
			if(null != ctype){
				reader = reader(ctype);
			}
			if(null == reader){
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					reader = reader(typeName);
					if(null == reader) {
						reader = reader(type(typeName));
					}
				}
			}
		}
		if(null == reader){
			reader = reader(value.getClass());
		}
		if(null != reader){
			result = reader.read(value);
		}
		if(null == reader || null == result){
			if(null != ctype) {
				result = ctype.read(value, null, clazz);
			}
		}
		return result;
	}

	@Override
	public void value(DataRuntime runtime, StringBuilder builder, Object obj, String key){
		Object value = null;
		if(obj instanceof DataRow){
			value = ((DataRow)obj).get(key);
		}else {
			if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
				Field field = EntityAdapterProxy.field(obj.getClass(), key);
				value = BeanUtil.getFieldValue(obj, field);
			} else {
				value = BeanUtil.getFieldValue(obj, key);
			}
		}
		if(null != value){
			if(value instanceof SQL_BUILD_IN_VALUE){
				builder.append(value(runtime, null, (SQL_BUILD_IN_VALUE)value));
			}else {
				ColumnType type = type(value.getClass().getName());
				if (null != type) {
					value = type.write(value, null, false);
				}
				builder.append(value);

			}
		}else{
			builder.append("null");
		}
	}
	@Override
	public boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue value){
		boolean result = false;
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			LinkedHashMap<String, Column> columns = columns(runtime,null, false, new Table(catalog, schema, table), false);
			result = convert(runtime, columns, value);
		}else{
			result = convert(runtime,(Column)null, value);
		}
		return result;
	}

	/**
	 * 设置参数值,主要根据数据类型格执行式化，如对象,list,map等插入json列
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param column 列
	 * @param value value
	 */
	@Override
	public void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value){
		boolean split = ConfigTable.IS_AUTO_SPLIT_ARRAY;
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			String type = null;
			if(null != column){
				type = column.getTypeName();
				if(null == type && BasicUtil.isNotEmpty(run.getTable())){
					LinkedHashMap<String,Column> columns = columns(runtime,null, false, new Table(run.getTable()), false);
					column = columns.get(column.getName().toUpperCase());
					if(null != column) {
						type = column.getTypeName();
					}
				}
			}
		}
		RunValue rv = run.addValues(compare, column, value, split);
		if(null != column){
			//value = convert(runtime, column, rv); //统一调用
		}
	}
	@Override
	public boolean convert(DataRuntime runtime, ConfigStore configs, Run run){
		boolean result = false;
		if(null != run) {
			result = convert(runtime, new Table<>(run.getTable()), run);
		}
		return result;
	}
	@Override
	public boolean convert(DataRuntime runtime, Table table, Run run){
		boolean result = false;
		LinkedHashMap<String, Column> columns = table.getColumns();

		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			if(null == columns || columns.isEmpty()) {
				columns = columns(runtime, null,false, table, false);
			}
		}
		List<RunValue> values = run.getRunValues();
		if (null != values) {
			for (RunValue value : values) {
				if (ConfigTable.IS_AUTO_CHECK_METADATA) {
					result = convert(runtime, columns, value);
				} else {
					result = convert(runtime, (Column) null, value);
				}
			}
		}
		return result;
	}
	@Override
	public boolean convert(DataRuntime runtime, Map<String,Column> columns, RunValue value){
		boolean result = false;
		if(null != columns && null != value){
			String key = value.getKey();
			if(null != key) {
				Column meta = columns.get(key.toUpperCase());
				result = convert(runtime, meta, value);
			}
		}
		return result;
	}
	/**
	 * 根据数据库列属性 类型转换(一般是在更新数据库时调用)
	 * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
	 * @param metadata 列
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)Value
	 * @return boolean 是否完成类型转换,决定下一步是否继续
	 */
	@Override
	public boolean convert(DataRuntime runtime, Column metadata, RunValue run){
		if(null == run){
			return true;
		}
		Object value = run.getValue();
		if(null == value){
			return true;
		}
		try {
			if(null != metadata) {
				//根据列属性转换(最终也是根据java类型转换)
				value = convert(runtime, metadata, value);
			}else{
				DataWriter writer = writer(value.getClass());
				if(null != writer){
					value = writer.write(value,true);
				}
			}
			run.setValue(value);

		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public Object convert(DataRuntime runtime, Column metadata, Object value){
		if(null == value){
			return value;
		}
		try {
			if(null != metadata) {
				ColumnType columnType = metadata.getColumnType();
				if(null == columnType){
					columnType = type(metadata.getTypeName());
					if(null != columnType) {
						columnType.setArray(metadata.isArray());
						metadata.setColumnType(columnType);
					}
				}
				value = convert(runtime, columnType, value);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return value;
	}
	@Override
	public Object convert(DataRuntime runtime, ColumnType columnType, Object value){
		if(null == columnType){
			return value;
		}
		String typeName = columnType.getName();

		boolean parseJson = false;
		if(null != typeName && !(value instanceof String)){
			if(typeName.contains("JSON")){
				//对象转换成json string
				value = BeanUtil.object2json(value);
				parseJson = true;
			}else if(typeName.contains("XML")){
				value = BeanUtil.object2xml(value);
				parseJson = true;
			}
		}
		if(!parseJson){
			if (null != columnType) {
				DataWriter writer = writer(columnType);
				if(null != writer){
					value = writer.write(value, true);
				}else {
					Class transfer = columnType.transfer();
					Class compatible = columnType.compatible();
					if (null != transfer) {
						value = ConvertAdapter.convert(value, transfer, columnType.isArray());
					}
					if (null != compatible) {
						value = ConvertAdapter.convert(value, compatible, columnType.isArray());
					}
				}
			}
		}
		return value;
	}
	public PrimaryGenerator getPrimaryGenerator() {
		return primaryGenerator;
	}

	public void setPrimaryGenerator(PrimaryGenerator primaryGenerator) {
		this.primaryGenerator = primaryGenerator;
	}

	@Override
	public String objectName(DataRuntime runtime, String name) {
		KeyAdapter.KEY_CASE keyCase = type().nameCase();
		if(null != keyCase){
			return keyCase.convert(name);
		}
		return name;
	}


	protected String random(DataRuntime runtime){
		StringBuilder builder = new StringBuilder();
		builder.append("[SQL:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

	//A.ID,A.COOE,A.NAME
	protected String concat(String prefix, String split, List<String> columns){
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(prefix)){
			prefix = "";
		}else{
			if(!prefix.endsWith(".")){
				prefix += ".";
			}
		}

		boolean first = true;
		for(String column:columns){
			if(!first){
				builder.append(split);
			}
			first = false;
			builder.append(prefix).append(column);
		}
		return builder.toString();
	}
	//master.column = data.column
	protected String concatEqual(String master, String data, String split, List<String> columns){
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(master)){
			master = "";
		}else{
			if(!master.endsWith(".")){
				master += ".";
			}
		}
		if(BasicUtil.isEmpty(data)){
			data = "";
		}else{
			if(!data.endsWith(".")){
				data += ".";
			}
		}

		boolean first = true;
		for(String column:columns){
			if(!first){
				builder.append(split);
			}
			first = false;
			builder.append(master).append(column).append(" = ").append(data).append(column);
		}
		return builder.toString();
	}

	public StringBuilder delimiter(StringBuilder builder, String src){
		return SQLUtil.delimiter(builder, src, getDelimiterFr(), getDelimiterTo());
	}
	public StringBuilder delimiter(StringBuilder builder, BaseMetadata src){
		if(null != src) {
			String name = src.getName();
			if(BasicUtil.isNotEmpty(name)) {
				SQLUtil.delimiter(builder, name, getDelimiterFr(), getDelimiterTo());
			}
		}
		return builder;
	}

	public  <T extends BaseMetadata> T search(List<T> list, String catalog, String schema, String name){
		return BaseMetadata.search(list, catalog, schema, name);
	}

	public  <T extends BaseMetadata> T search(List<T> list, String catalog, String name){
		return BaseMetadata.search(list, catalog, name);
	}

	public  <T extends BaseMetadata> T search(List<T> list, String name){
		return BaseMetadata.search(list, name);
	}


	/**
	 * 是否输出SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SQL(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL();
		}
		return ConfigTable.IS_LOG_SQL;
	}
	/**
	 * insert update 时是否自动检测表结构(删除表中不存在的属性)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_AUTO_CHECK_METADATA(ConfigStore configs){
		if(null != configs){
			return configs.IS_AUTO_CHECK_METADATA();
		}
		return ConfigTable.IS_AUTO_CHECK_METADATA;
	}
	/**
	 * 查询返回空DataSet时，是否检测元数据信息
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_CHECK_EMPTY_SET_METADATA(ConfigStore configs){
		if(null != configs){
			return configs.IS_CHECK_EMPTY_SET_METADATA();
		}
		return ConfigTable.IS_CHECK_EMPTY_SET_METADATA;
	}
	/**
	 * 是否输出慢SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SLOW_SQL(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SLOW_SQL();
		}
		return ConfigTable.IS_LOG_SLOW_SQL;
	}
	/**
	 * 是否输出SQL参数日志(占位符模式下有效)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SQL_PARAM(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL_PARAM();
		}
		return ConfigTable.IS_LOG_SQL_PARAM;
	}

	/**
	 * 是否输出SQL参数日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_BATCH_SQL_PARAM(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_BATCH_SQL_PARAM();
		}
		return ConfigTable.IS_LOG_BATCH_SQL_PARAM;
	}
	/**
	 * 异常时是否输出SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SQL_WHEN_ERROR(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL_WHEN_ERROR();
		}
		return ConfigTable.IS_LOG_SQL_WHEN_ERROR;
	}
	/**
	 * 是否输出异常堆栈日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_PRINT_EXCEPTION_STACK_TRACE(ConfigStore configs){
		if(null != configs){
			return configs.IS_PRINT_EXCEPTION_STACK_TRACE();
		}
		return ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE;
	}

	/**
	 * 异常时是否输出SQL参数日志(占位符模式下有效)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SQL_PARAM_WHEN_ERROR(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL_PARAM_WHEN_ERROR();
		}
		return ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR;
	}
	protected boolean IS_SQL_LOG_PLACEHOLDER(ConfigStore configs){
		if(null != configs){
			return configs.IS_SQL_LOG_PLACEHOLDER();
		}
		return ConfigTable.IS_SQL_LOG_PLACEHOLDER;
	}

	/**
	 * 是否显示SQL耗时
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_LOG_SQL_TIME(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL_TIME();
		}
		return ConfigTable.IS_LOG_SQL_TIME;
	}
	/**
	 * 慢SQL判断标准
	 * @param configs ConfigStore
	 * @return long
	 */
	protected long SLOW_SQL_MILLIS(ConfigStore configs){
		if(null != configs){
			return configs.SLOW_SQL_MILLIS();
		}
		return ConfigTable.SLOW_SQL_MILLIS;
	}
	/**
	 * 是否抛出查询异常
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_THROW_SQL_QUERY_EXCEPTION(ConfigStore configs){
		if(null != configs){
			return configs.IS_THROW_SQL_QUERY_EXCEPTION();
		}
		return ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION;
	}
	/**
	 * 是否抛出更新异常
	 * @param configs ConfigStore
	 * @return boolean
	 */
	protected boolean IS_THROW_SQL_UPDATE_EXCEPTION(ConfigStore configs){
		if(null != configs){
			return configs.IS_THROW_SQL_UPDATE_EXCEPTION();
		}
		return ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION;
	}
	protected boolean IS_UPDATE_NULL_COLUMN(ConfigStore configs){
		if(null != configs){
			return configs.IS_UPDATE_NULL_COLUMN();
		}
		return ConfigTable.IS_UPDATE_NULL_COLUMN;
	}
	protected boolean IS_UPDATE_EMPTY_COLUMN(ConfigStore configs){
		if(null != configs){
			return configs.IS_UPDATE_EMPTY_COLUMN();
		}
		return ConfigTable.IS_UPDATE_EMPTY_COLUMN;
	}

	protected boolean IS_UPDATE_NULL_FIELD(ConfigStore configs){
		if(null != configs){
			return configs.IS_UPDATE_NULL_FIELD();
		}
		return ConfigTable.IS_UPDATE_NULL_FIELD;
	}

	protected boolean IS_UPDATE_EMPTY_FIELD(ConfigStore configs){
		if(null != configs){
			return configs.IS_UPDATE_EMPTY_FIELD();
		}
		return ConfigTable.IS_UPDATE_EMPTY_FIELD;
	}

	protected boolean IS_INSERT_NULL_FIELD(ConfigStore configs){
		if(null != configs){
			return configs.IS_INSERT_NULL_FIELD();
		}
		return ConfigTable.IS_INSERT_NULL_FIELD;
	}

	protected boolean IS_INSERT_EMPTY_FIELD(ConfigStore configs){
		if(null != configs){
			return configs.IS_INSERT_EMPTY_FIELD();
		}
		return ConfigTable.IS_INSERT_EMPTY_FIELD;
	}

	protected boolean IS_INSERT_NULL_COLUMN(ConfigStore configs){
		if(null != configs){
			return configs.IS_INSERT_NULL_COLUMN();
		}
		return ConfigTable.IS_INSERT_NULL_COLUMN;
	}
	protected boolean IS_INSERT_EMPTY_COLUMN(ConfigStore configs){
		if(null != configs){
			return configs.IS_INSERT_EMPTY_COLUMN();
		}
		return ConfigTable.IS_INSERT_EMPTY_COLUMN;
	}
	protected boolean IS_LOG_SQL_WARN(ConfigStore configs){
		if(null != configs){
			return configs.IS_LOG_SQL_WARN();
		}
		return ConfigTable.IS_LOG_SQL_WARN;
	}
	protected boolean IS_REPLACE_EMPTY_NULL(ConfigStore configs){
		if(null != configs){
			return configs.IS_REPLACE_EMPTY_NULL();
		}
		return ConfigTable.IS_REPLACE_EMPTY_NULL;
	}
	protected boolean IS_KEYHOLDER_IDENTITY(ConfigStore configs){
		if(null != configs){
			return configs.IS_KEYHOLDER_IDENTITY();
		}
		return ConfigTable.IS_KEYHOLDER_IDENTITY;
	}

}