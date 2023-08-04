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
 *
 *          
 */


package org.anyline.data.adapter.init;


import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.xml.XMLPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.generator.GeneratorConfig;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class DefaultDriverAdapter implements DriverAdapter {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDriverAdapter.class);
	protected DatabaseType db;

	public String delimiterFr = "";
	public String delimiterTo = "";

	//根据名称定准数据类型
	protected Map<String, ColumnType> types = new Hashtable();

	//从数据库中读取(有些数据库会返回特定类型如PgPoint,可以根据Class定位reader,有些数据库返回通用类型好byte[]需要根据ColumnType定位reader)
	protected Map<Object, DataReader> readers = new Hashtable();
	//写入数据库
	protected Map<Object, DataWriter> writers = new Hashtable();

	@Autowired(required=false)
	protected PrimaryGenerator primaryGenerator;


	//单数据源 或 固定数据源(不可切换)时赋
	protected AnylineDao dao;

	protected Map<String,String> versions = new Hashtable<>();

	@Override
	public AnylineDao getDao() {
		return dao;
	}

	@Override
	public void setDao(AnylineDao dao) {
		this.dao = dao;
	}


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
	}/*
	public boolean createPrimaryValue(Object entity, DatabaseType type, String table, List<String> pks, String other){
		//针对当前表的生成器
		PrimaryGenerator generator = GeneratorConfig.get(table);
		if(null != generator){
			return generator.create(entity, type, table, pks, other);
		}
		//全局配置
		if(null == primaryGenerator){
			if(null == primaryGenerator){
				primaryGenerator = GeneratorConfig.get();
			}
			if(null == primaryGenerator) {
				//全局配置
				if (ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATORS.SNOWFLAKE;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATORS.RANDOM;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATORS.UUID;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATORS.TIME;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATORS.TIMESTAMP;
				}
			}
		}
		if(null != primaryGenerator) {
			return primaryGenerator.create(entity, type, table, pks, other);
		}else{
			return false;
		}
	}*/
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
		return types.get(type.toUpperCase());
	}

	@Override
	public DataReader reader(Class clazz){
		if(null == clazz){
			return null;
		}
		return readers.get(clazz);
	}
	@Override
	public DataReader reader(ColumnType type){
		if(null == type){
			return null;
		}
		return readers.get(type);
	}
	@Override
	public DataWriter writer(Object support){
		if(null == support){
			return null;
		}
		return writers.get(support);
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
	 * Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns)
	 * void createInserts(Run run, String dest, DataSet set,  List<String> keys)
	 * void createInserts(Run run, String dest, Collection list,  List<String> keys)
	 * List<String> confirmInsertColumns(String dest, Object obj, List<String> columns)
	 * String batchInsertSeparator ()
	 * boolean supportInsertPlaceholder ()
	 * List<Map<String,Object>> process(List<Map<String,Object>> list)
	 *
	 * protected void insertValue(Run run, Object obj, boolean placeholder, List<String> keys)
	 * protected Run createInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns)
	 * protected Run createInsertRunFromCollection(DataRuntime runtime, String dest, Collection list, boolean checkPrimary, List<String> columns)
	 ******************************************************************************************************************/

	/**
	 * 创建INSERT RunPrepare
	 * @param runtime runtime
	 * @param dest 表
	 * @param obj 实体
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceUtil.parseDataSource(dest, obj);
		}

		if(obj instanceof Collection){
			Collection list = (Collection) obj;
			if(list.size() >0){
				return createInsertRunFromCollection(runtime, dest, list, checkPrimary, columns);
			}
			return null;
		}else {
			return createInsertRun(runtime, dest, obj, checkPrimary, columns);
		}

	}

	/**
	 * 根据DataSet创建批量INSERT RunPrepare
	 * @param runtime runtime
	 * @param run run
	 * @param dest 表 如果不指定则根据set解析
	 * @param set 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInserts(DataRuntime runtime, Run run, String dest, DataSet set, List<String> keys){
	}

	/**
	 * 根据Collection创建批量INSERT RunPrepare
	 * @param runtime runtime
	 * @param run run
	 * @param dest 表 如果不指定则根据set解析
	 * @param list 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInserts(DataRuntime runtime, Run run, String dest, Collection list, List<String> keys){
	}

	/**
	 * 确认需要插入的列
	 * @param obj  Entity或DataRow
	 * @param batch  是否批量，批量时不检测值是否为空
	 * @param columns 提供额外的判断依据<br/>
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
	public List<String> confirmInsertColumns(String dest, Object obj, List<String> columns, boolean batch){
		List<String> keys = new ArrayList<>();/*确定需要插入的列*/
		if(null == obj){
			return new ArrayList<>();
		}
		List<String> mastKeys = new ArrayList<>();		// 必须插入列
		List<String> ignores = new ArrayList<>();		// 必须不插入列
		List<String> factKeys = new ArrayList<>();		// 根据是否空值

		boolean each = true;//是否需要从row中查找列
		if(null != columns && columns.size()>0){
			each = false;
			keys = new ArrayList<>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1);
					mastKeys.add(column);
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
				keys.add(column);
			}
		}
		if(each){
			// 是否插入null及""列
			boolean isInsertNullColumn =  false;
			boolean isInsertEmptyColumn = false;
			DataRow row = null;
			if(obj instanceof DataRow){
				row = (DataRow)obj;
				mastKeys.addAll(row.getUpdateColumns());
				ignores.addAll(row.getIgnoreUpdateColumns());
				keys = row.keys();

				isInsertNullColumn = row.isInsertNullColumn();
				isInsertEmptyColumn = row.isInsertEmptyColumn();

			}else{
				isInsertNullColumn = ConfigTable.IS_INSERT_NULL_FIELD;
				isInsertEmptyColumn = ConfigTable.IS_INSERT_EMPTY_FIELD;
				if(EntityAdapterProxy.hasAdapter(obj.getClass())){
					keys.addAll(Column.names(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.INSERT)));
				}else {
					keys = new ArrayList<>();
					List<Field> fields = ClassUtil.getFields(obj.getClass(), false, false);
					for (Field field : fields) {
						Class clazz = field.getType();
						if (clazz == String.class || clazz == Date.class || ClassUtil.isPrimitiveClass(clazz)) {
							keys.add(field.getName());
						}
					}
				}
			}
			if(batch){
				isInsertNullColumn = true;
				isInsertEmptyColumn = true;
			}

			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][columns:{}]", keys);
			}
			BeanUtil.removeAll(ignores, columns);
			BeanUtil.removeAll(keys, ignores);
			if(log.isDebugEnabled()) {
				log.debug("[confirm insert columns][ignores:{}]", ignores);
			}
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
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
						keys.remove(key);
						continue;
					}
					if(!isInsertNullColumn){
						keys.remove(i);	
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}
					if(!isInsertEmptyColumn){
						keys.remove(i);
						continue;
					}
				}

			}
		}
		if(log.isDebugEnabled()) {
			log.debug("[confirm insert columns][result:{}]", keys);
		}
		keys = checkMetadata(dest, keys);
		keys = BeanUtil.distinct(keys);
		return keys;
	}

	/**
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	@Override
	public String batchInsertSeparator (){
		return ",";
	}

	/**
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	@Override
	public boolean supportInsertPlaceholder (){
		return true;
	}
	/**
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
	 * 根据entity创建 INSERT RunPrepare
	 * @param runtime runtime
	 * @param dest 表
	 * @param obj 数据
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 列
	 * @return Run
	 */
	protected Run createInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){
		return null;
	}

	/**
	 * 根据collection创建 INSERT RunPrepare
	 * @param runtime runtime
	 * @param dest 表
	 * @param list 对象集合
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要插入的列,如果不指定则全部插入
	 * @return Run
	 */
	protected Run createInsertRunFromCollection(DataRuntime runtime, String dest, Collection list, boolean checkPrimary, List<String> columns){
		return null;
	}

	@Override
	public String generatedKey() {
		return null;
	}
	/* *****************************************************************************************************************
	 * 													UPDATE
	 * -----------------------------------------------------------------------------------------------------------------
	 * Run buildUpdateRun(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns)
	 * List<String> checkMetadata(String table, List<String> columns)
	 *
	 * protected Run buildUpdateRunFromEntity(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns)
	 * protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns)
	 * protected List<String> confirmUpdateColumns(String dest, DataRow row, List<String> columns)
	 ******************************************************************************************************************/


	@Override
	public Run buildUpdateRun(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceUtil.parseDataSource(null,obj);
		}
		if(obj instanceof DataRow){
		}else if(obj instanceof Map){
			obj = new DataRow((Map)obj);
		}

		if(obj instanceof DataRow){
			return buildUpdateRunFromDataRow(dest, (DataRow)obj, configs, checkPrimary, columns);
		}else{
			return buildUpdateRunFromEntity(dest, obj, configs, checkPrimary, columns);
		}
	}

	protected Run buildUpdateRunFromEntity(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
		return null;
	}
	protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns){
		return null;
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	public List<String> checkMetadata(String table, List<String> columns){
		if(!ConfigTable.IS_AUTO_CHECK_METADATA || null == dao){
			return columns;
		}
		List<String> list = new ArrayList<>();
		Set<String> metadatas = dao.columns(table).keySet();
		if(metadatas.size() > 0) {
			for (String item : columns) {
				if (metadatas.contains(item.toUpperCase())) {
					list.add(item);
				} else {
					log.warn("[{}][column:{}.{}][insert/update忽略当前列名]", LogUtil.format("列名检测不存在", 33), table, item);
				}
			}
		}else{
			log.warn("[{}][table:{}][忽略列名检测]", LogUtil.format("表结构检测失败(检查表名是否存在)", 33), table);
		}
		log.info("[check column metadata][src:{}][result:{}]", columns.size(), list.size());
		return list;
	}

	/**
	 * 确认需要更新的列
	 * @param row DataRow
	 * @param configs 更新条件
	 * @param columns 提供额外的判断依据
	 *                列可以加前缀
	 *                +:表示必须插入
	 *                -:表示必须不插入
	 *                ?:根据是否有值
	 *
	 *        先DataRow解析出必须更新的列与colums中必须更新的列合并
	 *        再从DataRow中解析出必须忽略的列与columns中必须忽略更新的列合并
	 *        DataRow.put时可以设置 必须更新(插入)或必须忽略更新(插入) put("+KEY", "VALUE") put("-KEY", "VALUE")
	 *
	 *        如果提供了columns并且长度>0则不遍历row.keys
	 *        如果没有提供columns 但row.keys中有必须更新的列 也不再遍历row.keys
	 *        其他情况需要遍历row.keys
	 *
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true
	 *        则把执行结果与表结构对比,删除表中没有的列
	 * @return List
	 */
	protected List<String> confirmUpdateColumns(String dest, DataRow row, ConfigStore configs, List<String> columns){
		List<String> keys = null;/*确定需要更新的列*/
		if(null == row){
			return new ArrayList<>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> conditions = new ArrayList<>()							; // 更新条件
 		List<String> masters = BeanUtil.copy(row.getUpdateColumns())		; // 必须更新列
		List<String> ignores = BeanUtil.copy(row.getIgnoreUpdateColumns())	; // 必须不更新列
		List<String> factKeys = new ArrayList<>()							; // 根据是否空值
		BeanUtil.removeAll(ignores, columns);

		if(null != columns && columns.size()>0){
			each = false;
			keys = new ArrayList<>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1);
					masters.add(column);
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
				keys.add(column);
			}
		}else if(null != masters && masters.size()>0){
			each = false;
			keys = masters;
		}
		if(each){
			keys = row.keys();
			for(String k:masters){
				if(!keys.contains(k)){
					keys.add(k);
				}
			}
			// 是否更新null及""列
			boolean isUpdateNullColumn = row.isUpdateNullColumn();
			boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();
			BeanUtil.removeAll(keys, ignores);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(masters.contains(key)){
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
		keys.removeAll(ignores);
		keys = checkMetadata(dest, keys);
		keys = BeanUtil.distinct(keys);
		return keys;
	}


	/* *****************************************************************************************************************
	 * 													QUERY
	 * -----------------------------------------------------------------------------------------------------------------
	 * Run buildQueryRun(RunPrepare prepare, ConfigStore configs, String ... conditions)
	 * List<Map<String,Object>> process(List<Map<String,Object>> list)
	 * Run buildExecuteRun(RunPrepare prepare, ConfigStore configs, String ... conditions)
	 *
	 * void buildQueryRunContent(Run run)
	 * protected void buildQueryRunContent(XMLRun run)
	 * protected void buildQueryRunContent(TextRun run)
	 * protected void buildQueryRunContent(TableRun run)
	 ******************************************************************************************************************/

	/**
	 * 创建查询SQL
	 * @param prepare  prepare
	 * @param configs 查询条件配置
	 * @param conditions 查询条件
	 * @return Run
	 */
	@Override
	public Run buildQueryRun(RunPrepare prepare, ConfigStore configs, String ... conditions){
		Run run = null;
		if(prepare instanceof TablePrepare){
			run = new TableRun(this,prepare.getTable());
		}else if(prepare instanceof XMLPrepare){
			run = new XMLRun();
		}else if(prepare instanceof TextPrepare){
			run = new TextRun();
		}
		if(null != run){
			run.setAdapter(this);
			//如果是text类型 将解析文本并抽取出变量
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			run.addCondition(conditions);
			if(run.checkValid()) {
				//为变量赋值
				run.init();
				//构造最终的查询SQL
				buildQueryRunContent(run);
			}
		}
		return run;
	}

	/**
	 * 构造查询主体
	 * @param run run
	 */
	@Override
	public void buildQueryRunContent(Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				buildQueryRunContent(r);
			}else if(run instanceof XMLRun){
				XMLRun r = (XMLRun) run;
				buildQueryRunContent(r);
			}else if(run instanceof TextRun){
				TextRun r = (TextRun) run;
				buildQueryRunContent(r);
			}
		}
	}
	protected void buildQueryRunContent(XMLRun run){
	}
	protected void buildQueryRunContent(TextRun run){
	}
	protected void buildQueryRunContent(TableRun run){
	}

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	public List<Run> buildQuerySequence(boolean next, String ... names){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQuerySequence(boolean next, String ... names)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 构造查询主体
	 * @param run run
	 */
	@Override
	public void buildExecuteRunContent(Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				buildExecuteRunContent(r);
			}else if(run instanceof XMLRun){
				XMLRun r = (XMLRun) run;
				buildExecuteRunContent(r);
			}else if(run instanceof TextRun){
				TextRun r = (TextRun) run;
				buildExecuteRunContent(r);
			}
		}
	}
	protected void buildExecuteRunContent(XMLRun run){
	}
	protected void buildExecuteRunContent(TextRun run){
	}
	protected void buildExecuteRunContent(TableRun run){
	}


	/**
	 * JDBC执行完成后的结果处理
	 * @param list JDBC执行结果
	 * @return  DataSet
	 */
	@Override
	public List<Map<String,Object>> process(List<Map<String,Object>> list){
		return list;
	}

	@Override
	public Run buildExecuteRun(RunPrepare prepare, ConfigStore configs, String ... conditions){
		Run run = null;
		if(prepare instanceof XMLPrepare){
			run = new XMLRun();
		}else if(prepare instanceof TextPrepare){
			run = new TextRun();
		}
		if(null != run){
			run.setAdapter(this);
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			run.addCondition(conditions);
			run.init(); //
			//构造最终的执行SQL
			buildQueryRunContent(run);
		}
		return run;
	}


	/* *****************************************************************************************************************
	 * 													EXISTS
	 * -----------------------------------------------------------------------------------------------------------------
	 * String parseExists(Run run)
	 ******************************************************************************************************************/

	@Override
	public String parseExists(Run run){
		return null;
	}

	/* *****************************************************************************************************************
	 * 													COUNT
	 * -----------------------------------------------------------------------------------------------------------------
	 * String parseTotalQuery(Run run)
	 ******************************************************************************************************************/
	/**
	 * 求总数SQL
	 * Run 反转调用
	 * @param run  run
	 * @return String
	 */
	@Override
	public String parseTotalQuery(Run run){
		return null;
	}



	/* *****************************************************************************************************************
	 * 													DELETE
	 * -----------------------------------------------------------------------------------------------------------------
	 * Run buildDeleteRun(String table, String key, Object values)
	 * Run buildDeleteRun(String dest, Object obj, String ... columns)
	 * Run buildDeleteRunContent(Run run)
	 *
	 * protected Run createDeleteRunSQLFromTable(String table, String key, Object values)
	 * protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns)
	 ******************************************************************************************************************/
	@Override
	public Run buildDeleteRun(String table, String key, Object values){
		return createDeleteRunSQLFromTable(table, key, values);
	}
	@Override
	public Run buildDeleteRun(String dest, Object obj, String ... columns){
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
			run = new TableRun(this,dest);
			RunPrepare prepare = new DefaultTablePrepare();
			prepare.setDataSource(dest);
			run.setPrepare(prepare);
			run.setConfigStore((ConfigStore)obj);
			run.addCondition(columns);
			run.init();
			buildDeleteRunContent(run);
		}else{
			run = createDeleteRunSQLFromEntity(dest, obj, columns);
		}
		return run;
	}

	/**
	 * 构造删除主体
	 * @param run run
	 * @return Run
	 */
	@Override
	public Run buildDeleteRunContent(Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				return buildDeleteRunContent(r);
			}
		}
		return run;
	}

	protected Run buildDeleteRunContent(TableRun run){
		return null;
	}
 
	protected Run createDeleteRunSQLFromTable(String table, String key, Object values){
		return null;
	}
	protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){
		return null;
	}

	@Override
	public List<Run> buildTruncateSQL(String table){
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("TRUNCATE TABLE ");
		SQLUtil.delimiter(builder, table, delimiterFr, delimiterTo);
		return runs;
	}


	/* *****************************************************************************************************************
	 *
	 * 													metadata
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

	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryDatabaseRunSQL() throws Exception
	 * public LinkedHashMap<String, Database> databases(int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception
	 ******************************************************************************************************************/
	@Override
	public List<Run> buildQueryDatabaseRunSQL() throws Exception{
        if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryDatabaseRunSQL()", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public LinkedHashMap<String, Database> databases(int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 LinkedHashMap<String, Database> databases(int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types)
	 * <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * List<Run> buildQueryDDLRunSQL(Table table) throws Exception
	 * public List<String> ddl(int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 查询表备注
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	public List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 * 表备注
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	public <T extends Table> LinkedHashMap<String, T> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
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
	 * 查询表DDL
	 * @param table 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDDLRunSQL(Table table) throws Exception{
		//有支持直接查询DDL的在子类中实现
		List<Run> runs = buildCreateRunSQL(table);
		return runs;
	}

	/**
	 * 查询表DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(int index, Table table, List<String> ddls, DataSet set){
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<String> ddl(int index, Table table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}
	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryViewRunSQL(String catalog, String schema, String pattern, String types)
	 * <T extends View> LinkedHashMap<String, T> views(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception
	 * <T extends View> LinkedHashMap<String, T> views(boolean create, LinkedHashMap<String, T> views, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryViewRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryViewRunSQL(String catalog, String schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
	}

	@Override
	public <T extends View> LinkedHashMap<String, T> views(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends View> LinkedHashMap<String, T> views(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set)", 37));
		}
		if(null == views){
			views = new LinkedHashMap<>();
		}
		return views;
	}
	@Override
	public <T extends View> LinkedHashMap<String, T> views(boolean create, LinkedHashMap<String, T> views, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends View> LinkedHashMap<String, T> views(boolean create, LinkedHashMap<String, T> views, DataRuntime runtime, String catalog, String schema, String pattern, String ... types)", 37));
		}
		if(null == views){
			views = new LinkedHashMap<>();
		}
		return views;
	}

	/**
	 * 查询 view DDL
	 * @param view view
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDDLRunSQL(View view) throws Exception{
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryDDLRunSQL(View view)", 37));
		}
		return runs;
	}

	/**
	 * 查询 view DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param view view
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(int index, View view, List<String> ddls, DataSet set){
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<String> ddl(int index, View view, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types)
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 *
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param runtime runtime
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}


	/**
	 * 从上一步生成的SQL查询结果中 提取表结构
	 * @param index 第几条SQL
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}


	/**
	 * 查询 MasterTable DDL
	 * @param table MasterTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDDLRunSQL(MasterTable table) throws Exception{
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryDDLRunSQL(MasterTable table)", 37));
		}
		return runs;
	}

	/**
	 * 查询 MasterTable DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(int index, MasterTable table, List<String> ddls, DataSet set){
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<String> ddl(int index, MasterTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name)
	 * List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags)
 	 * <T extends PartitionTable> LinkedHashMap<String, T> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, MasterTable master) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询分区表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造Table
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable table, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set)", 37));
        }
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 *
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param runtime runtime
	 * @param catalog catalog
	 * @param schema schema
	 * @param master 主表
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, MasterTable master) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, MasterTable master)", 37));
		}
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 * 查询 PartitionTable DDL
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDDLRunSQL(PartitionTable table) throws Exception{
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryDDLRunSQL(PartitionTable table)", 37));
		}
		return runs;
	}

	/**
	 * 查询 PartitionTable DDL
	 * @param index 第几条SQL 对照 buildQueryDDLRunSQL 返回顺序
	 * @param table PartitionTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(int index, PartitionTable table, List<String> ddls, DataSet set){
		if(null == ddls){
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<String> ddl(int index, PartitionTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryColumnRunSQL(Table table, boolean metadata)
	 * <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DataRuntime runtime, Table table, String pattern) throws Exception
	 * Column column(Column column, SqlRowSetMetaData rsm, int index);
	 * Column column(Column column, ResultSet rs);
	 * protected Column column(Column column, SqlRowSetMetaData rsm, int index)
	 * protected Column column(Column column, ResultSet rs)
	 * protected List<String> keys(ResultSet rs) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表)
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryColumnRunSQL(Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("COLUMN_NAME");
			T column = columns.get(name.toUpperCase());
			if(null == column){
				column = (T)new Column();
			}
			column.setCatalog(BasicUtil.evl(row.getString("TABLE_CATALOG"), table.getCatalog(), column.getCatalog()));
			column.setSchema(BasicUtil.evl(row.getString("TABLE_SCHEMA"), table.getSchema(), column.getSchema()));
			column.setTable(table);
			column.setTable(BasicUtil.evl(row.getString("TABLE_NAME"), table.getName(), column.getTableName(true)));
			column.setName(name);
			if(null == column.getPosition()) {
				column.setPosition(row.getInt("ORDINAL_POSITION", null));
			}
			column.setComment(BasicUtil.evl(row.getString("COLUMN_COMMENT","COMMENTS"), column.getComment()));
			column.setTypeName(BasicUtil.evl(row.getString("DATA_TYPE"), column.getTypeName()));
			String def = BasicUtil.evl(row.get("COLUMN_DEFAULT", "DATA_DEFAULT"), column.getDefaultValue())+"";
			if(BasicUtil.isNotEmpty(def)) {
				while(def.startsWith("(") && def.endsWith(")")){
					def = def.substring(1, def.length()-1);
				}
				column.setDefaultValue(def);
			}
			if(-1 == column.isAutoIncrement()){
				column.setAutoIncrement(row.getBoolean("IS_IDENTITY", null));
			}
			if(-1 == column.isAutoIncrement()){
				column.setAutoIncrement(row.getBoolean("IS_AUTOINCREMENT", null));
			}
			if(-1 == column.isAutoIncrement()){
				if(row.getStringNvl("EXTRA").toLowerCase().contains("auto_increment")){
					column.setAutoIncrement(true);
				}
			}

			//主键
			String column_key = row.getString("COLUMN_KEY");
			if("PRI".equals(column_key)){
				column.setPrimaryKey(1);
			}


			//非空
			if(-1 == column.isNullable()) {
				column.setNullable(row.getBoolean("IS_NULLABLE", "NULLABLE"));
			}
			//oracle中decimal(18,9) data_length == 22 DATA_PRECISION=18
			Integer len = row.getInt("NUMERIC_PRECISION","PRECISION","DATA_PRECISION");
			if(null == len){
				len = row.getInt("CHARACTER_MAXIMUM_LENGTH","MAX_LENGTH","DATA_LENGTH");
			}
			column.setPrecision(len);
			if(null == column.getScale()) {
				column.setScale(row.getInt("NUMERIC_SCALE", "SCALE", "DATA_SCALE"));
			}
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
			columns.put(name.toUpperCase(), column);
		}
		return columns;
	}

	/**
	 * 构建Column
	 * @param column 列
	 * @param rs  ResultSet
	 * @return Column
	 */
	/*
		TABLE_CAT                             = api
		TABLE_SCHEM                           = null
		TABLE_NAME                            = 表名
		COLUMN_NAME                           = ID
		DATA_TYPE                             = -5
		TYPE_NAME                             = BIGINT/JSON/VARCHAR
		COLUMN_SIZE                           = 19
		BUFFER_LENGTH                         = 65535
		DECIMAL_DIGITS                        = null
		NUM_PREC_RADIX                        = 10
		NULLABLE                              = 0
		REMARKS                               = ID
		COLUMN_DEF                            = null
		SQL_DATA_TYPE                         = 0
		SQL_DATETIME_SUB                      = 0
		CHAR_OCTET_LENGTH                     = null
		ORDINAL_POSITION                      = 1
		IS_NULLABLE                           = NO
		SCOPE_CATALOG                         = null
		SCOPE_SCHEMA                          = null
		SCOPE_TABLE                           = null
		SOURCE_DATA_TYPE                      = null
		IS_AUTOINCREMENT                      = YES
		IS_GENERATEDCOLUMN                    = NO
*/
	@Override
	public Column column(Column column, ResultSet rs){
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
				column.setAutoIncrement(BasicUtil.parseBoolean(string(keys, "IS_AUTOINCREMENT", rs), false));
			}
			if(-1 == column.isGenerated()) {
				column.setGenerated(BasicUtil.parseBoolean(string(keys, "IS_GENERATEDCOLUMN", rs), false));
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
				keys.put(rsmd.getColumnLabel(i).toUpperCase(), i);
			}
		}
		return keys;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTagRunSQL(Table table, boolean metadata)
	 * <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern) throws Exception
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryTagRunSQL(Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)", 37));
		}
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern)", 37));
		}
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPrimaryRunSQL(Table table) throws Exception
	 * PrimaryKey primary(int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryPrimaryRunSQL(Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryPrimaryRunSQL(Table table)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public PrimaryKey primary(int index, Table table, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 PrimaryKey primary(int index, Table table, DataSet set)", 37));
		}
		return null;
	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRunSQL(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryForeignsRunSQL(Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryForeignsRunSQL(Table table)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRunSQL 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set)", 37));
		}
		return new LinkedHashMap<>();
	}



	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryIndexRunSQL(Table table, boolean metadata)
	 * <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的索引
	 * @param table 表
	 * @param name 名称
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryIndexRunSQL(Table table, String name){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildQueryIndexRunSQL(Table table, String name)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set)", 37));
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate)", 37));
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata)
	 * LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception{
		log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.","") + ")未实现 List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata)",37));
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param constraint 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(int constraint, boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set)", 37));
		}
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set)", 37));
		}
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events)
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * 查询表上的trigger
	 * @param table 表
	 * @param events INSERT|UPATE|DELETE
	 * @return sqls
	 */

	@Override
	public List<Run> buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)", 37));
		}
		if(null == triggers){
			readers = new LinkedHashMap<>();
		}
		return triggers;
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	public List<Run> buildQueryProcedureRunSQL(String catalog, String schema, String name) {
		return new ArrayList<>();
	}

	public <T extends Procedure> LinkedHashMap<String, T> procedures(int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception{
		return new LinkedHashMap<>();
	}

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	public List<Run> buildQueryFunctionRunSQL(String catalog, String schema, String name) {
		return new ArrayList<>();
	}

	public <T extends Function> LinkedHashMap<String, T> functions(int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception{
		return new LinkedHashMap<>();
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

	
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Table table);
	 * List<Run> buildAddCommentRunSQL(Table table);
	 * List<Run> buildAlterRunSQL(Table table)
	 * List<Run> buildAlterRunSQL(Table table, Collection<Column> columns);
     * List<Run> buildRenameRunSQL(Table table);
	 * List<Run> buildChangeCommentRunSQL(Table table);
	 * List<Run> buildDropRunSQL(Table table);
	 * StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, Table table)
	 * StringBuilder comment(StringBuilder builder, Table table)
	 * StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRunSQL(Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
 		builder.append("CREATE ").append(table.getKeyword()).append(" ");
		checkTableExists(builder, false);
		name(builder, table);
		LinkedHashMap columMap = table.getColumns();
		Collection<Column> columns = null;
		List<Column> pks = table.primarys();
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
					SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
					define(builder, column).append("\n");
					idx ++;
				}
				builder.append("\t");
				if(pks.size()== 1) {
					primary(builder, table);
				}
				builder.append(")");
			}
		}
		comment(builder, table);
		List<Run> tableComment = buildAddCommentRunSQL(table);
		if(null != tableComment) {
			runs.addAll(tableComment);
		}
		if(null != columns){
			for(Column column:columns){
				List<Run> columnComment = buildAddCommentRunSQL(column);
				if(null != columnComment){
					runs.addAll(columnComment);
				}
			}
		}
		if(pks.size() > 1){
			PrimaryKey primary = new PrimaryKey();
			primary.setTable(table);
			for (Column col:pks){
				primary.addColumn(col);
			}
			List<Run> pksql = buildAddRunSQL(primary);
			if(null != pksql){
				runs.addAll(pksql);
			}
		}
		return runs;
	}

	@Override
	 public List<Run> buildAddCommentRunSQL(Table table) throws Exception{
		return new ArrayList<>();
	 }


	@Override
	public List<Run> buildAlterRunSQL(Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(Table table)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Table table, Collection<Column> columns) throws Exception{
		List<Run> runs = new ArrayList<>();
		for(Column column:columns){
			String action = column.getAction();
			if("add".equalsIgnoreCase(action)){
				runs.addAll(buildAddRunSQL(column, false));
			}else if("alter".equalsIgnoreCase(action)){
				runs.addAll(buildAlterRunSQL(column, false));
			}else if("drop".equalsIgnoreCase(action)){
				runs.addAll(buildDropRunSQL(column, false));
			}
		}
		return runs;
	}
	/**
	 * 修改表名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildRenameRunSQL(Table table)", 37));
		}
		return new ArrayList<>();
	}

	@Override
	public List<Run> buildChangeCommentRunSQL(Table table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(Table table)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		builder.append("DROP ").append(table.getKeyword()).append(" ");
		checkTableExists(builder, true);
		name(builder, table);
		return runs;
	}

	/**
	 * 创建或删除表时检测表是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		builder.append(" IF ");
		if(!exists){
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
		return builder;
	}


	/**
	 * 主键
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.primarys();
		if(pks.size()>0){
			builder.append(",PRIMARY KEY (");
			boolean first = true;
			for(Column pk:pks){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				first = false;
			}
			builder.append(")");
		}
		return builder;
	}


	/**
	 * 备注 不支持创建表时带备注的 在子表中忽略
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Table table){
		String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			builder.append(" COMMENT'").append(comment).append("'");
		}
		return builder;
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(StringBuilder builder, Table table){
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		String name = table.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, name, getDelimiterFr(), getDelimiterTo());
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(View view);
	 * List<Run> buildAddCommentRunSQL(View view);
	 * List<Run> buildAlterRunSQL(View view);
	 * List<Run> buildRenameRunSQL(View view);
	 * List<Run> buildChangeCommentRunSQL(View view);
	 * List<Run> buildDropRunSQL(View view);
	 * StringBuilder checkViewExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, View view)
	 * StringBuilder comment(StringBuilder builder, View view)
	 * StringBuilder name(StringBuilder builder, View view)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRunSQL(View view) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE VIEW ");
		name(builder, view);
		builder.append(" AS \n").append(view.getDefinition());

		runs.addAll(buildAddCommentRunSQL(view));
		return runs;
	}

	@Override
	public List<Run> buildAddCommentRunSQL(View view) throws Exception{
		return new ArrayList<>();
	}


	@Override
	public List<Run> buildAlterRunSQL(View view) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER VIEW ");
		name(builder, view);
		builder.append(" AS \n").append(view.getDefinition());
		return runs;
	}
	/**
	 * 修改视图名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param view 视图
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(View view) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildRenameRunSQL(View view)", 37));
		}
		return new ArrayList<>();
	}

	@Override
	public List<Run> buildChangeCommentRunSQL(View view) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(View view)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 删除视图
	 * @param view 视图
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(View view) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP ").append(view.getKeyword()).append(" ");
		checkViewExists(builder, true);
		name(builder, view);
		return runs;
	}

	/**
	 * 创建或删除视图时检测视图是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(StringBuilder builder, boolean exists){
		builder.append(" IF ");
		if(!exists){
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
		return builder;
	}

	/**
	 * 备注 不支持创建视图时带备注的 在子视图中忽略
	 * @param builder builder
	 * @param view 视图
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, View view){
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(MasterTable table);
	 * List<Run> buildAddCommentRunSQL(MasterTable table);
	 * List<Run> buildAlterRunSQL(MasterTable table);
	 * List<Run> buildDropRunSQL(MasterTable table);
	 * List<Run> buildRenameRunSQL(MasterTable table);
	 * List<Run> buildChangeCommentRunSQL(MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(MasterTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildCreateRunSQL(MasterTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAddCommentRunSQL(MasterTable table) throws Exception{
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAlterRunSQL(MasterTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(MasterTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildDropRunSQL(MasterTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildDropRunSQL(MasterTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildRenameRunSQL(MasterTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildRenameRunSQL(MasterTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildChangeCommentRunSQL(MasterTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(MasterTable table)", 37));
		}
		return new ArrayList<>();
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(PartitionTable table);
	 * List<Run> buildAddCommentRunSQL(MasterTable table) throws Exception
	 * List<Run> buildAlterRunSQL(PartitionTable table);
	 * List<Run> buildDropRunSQL(PartitionTable table);
	 * List<Run> buildRenameRunSQL(PartitionTable table);
	 * List<Run> buildChangeCommentRunSQL(PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(PartitionTable table) throws Exception{

		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildCreateRunSQL(PartitionTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAddCommentRunSQL(PartitionTable table) throws Exception{
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildAlterRunSQL(PartitionTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(PartitionTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildDropRunSQL(PartitionTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildDropRunSQL(PartitionTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildRenameRunSQL(PartitionTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildRenameRunSQL(PartitionTable table)", 37));
		}
		return new ArrayList<>();
	}
	@Override
	public List<Run> buildChangeCommentRunSQL(PartitionTable table) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(PartitionTable table)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * String alterColumnKeyword()
	 * List<Run> buildAddRunSQL(Column column, boolean slice)
	 * List<Run> buildAddRunSQL(Column column)
	 * List<Run> buildAlterRunSQL(Column column, boolean slice)
	 * List<Run> buildAlterRunSQL(Column column)
	 * List<Run> buildDropRunSQL(Column column, boolean slice)
	 * List<Run> buildDropRunSQL(Column column)
	 * List<Run> buildRenameRunSQL(Column column)
	 * List<Run> buildChangeTypeRunSQL(Column column)
	 * List<Run> buildChangeDefaultRunSQL(Column column)
	 * List<Run> buildChangeNullableRunSQL(Column column)
	 * List<Run> buildChangeCommentRunSQL(Column column)
	 * List<Run> buildAddCommentRunSQL(Column column)
	 * StringBuilder define(StringBuilder builder, Column column)
	 * StringBuilder type(StringBuilder builder, Column column)
	 * boolean isIgnorePrecision(Column column);
	 * boolean isIgnoreScale(Column column);
	 * Boolean checkIgnorePrecision(String datatype);
	 * Boolean checkIgnoreScale(String datatype);
	 * StringBuilder nullable(StringBuilder builder, Column column)
	 * StringBuilder charset(StringBuilder builder, Column column)
	 * StringBuilder defaultValue(StringBuilder builder, Column column)
	 * StringBuilder increment(StringBuilder builder, Column column)
	 * StringBuilder onupdate(StringBuilder builder, Column column)
	 * StringBuilder position(StringBuilder builder, Column column)
	 * StringBuilder comment(StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/
	@Override
	public String alterColumnKeyword(){
		return "ALTER";
	}

	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Column column, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice) {
			Table table = column.getTable(true);
			builder.append("ALTER ").append(table.getKeyword()).append(" ");
			name(builder, table);
		}
		// Column update = column.getUpdate();
		// if(null == update){
		// 添加列
		//builder.append(" ADD ").append(column.getKeyword()).append(" ");
		addColumnGuide(builder, column);
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, column);
		// }
		runs.addAll(buildAddCommentRunSQL(column));
		return runs;
	}
	@Override
	public List<Run> buildAddRunSQL(Column column) throws Exception{
		return buildAddRunSQL(column, false);
	}

	/**
	 * 添加列引导
	 * @param builder StringBuilder
	 * @param column column
	 * @return String
	 */
	public StringBuilder addColumnGuide(StringBuilder builder, Column column){
		return builder.append(" ADD ").append(column.getKeyword()).append(" ");
	}

	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Column column, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();

		Column update = column.getUpdate();
		if(null != update){

			// 修改列名
			String name = column.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
				runs.addAll(buildRenameRunSQL(column));
			}
			// 修改数据类型
			String type = type(null, column).toString();
			String utype = type(null, update).toString();
			boolean exe = false;
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<Run> list = buildChangeTypeRunSQL(column);
				if(null != list){
					runs.addAll(list);
					exe = true;
				}
			}else{
				//数据类型没变但长度变了
				if(column.getPrecision() != update.getPrecision() || column.getScale() != update.getScale()){
					List<Run> list = buildChangeTypeRunSQL(column);
					if(null != list){
						runs.addAll(list);
						exe = true;
					}
				}
			}
			// 修改默认值
			Object def = column.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				List<Run> defs = buildChangeDefaultRunSQL(column);
				if(null != defs){
					runs.addAll(defs);
				}
			}
			// 修改非空限制
			int nullable = column.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				List<Run> nulls = buildChangeNullableRunSQL(column);
				if(null != nulls){
					runs.addAll(nulls);
				}
			}
			// 修改备注
			String comment = column.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				List<Run> cmts = buildChangeCommentRunSQL(column);
				if(null != cmts){
					runs.addAll(cmts);
				}
			}
		}

		return runs;
	}

	@Override
	public List<Run> buildAlterRunSQL(Column column) throws Exception{
		return buildAlterRunSQL(column, false);
	}

	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Column column, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(column instanceof Tag){
			Tag tag = (Tag)column;
			return buildDropRunSQL(tag);
		}
		if(!slice) {
			Table table = column.getTable(true);
			builder.append("ALTER ").append(table.getKeyword()).append(" ");
			name(builder, table);
		}
		dropColumnGuide(builder, column);
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}

	@Override
	public List<Run> buildDropRunSQL(Column column) throws Exception{
		return buildDropRunSQL(column, false);
	}

	/**
	 * 删除列引导
	 * @param builder StringBuilder
	 * @param column column
	 * @return String
	 */
	public StringBuilder dropColumnGuide(StringBuilder builder, Column column){
		return builder.append(" DROP ").append(column.getKeyword()).append(" ");
	}
	/**
	 * 修改列名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = column.getTable(true);
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" RENAME ").append(column.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" ");
		SQLUtil.delimiter(builder, column.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
		column.setName(column.getUpdate().getName());
		return runs;
	}


	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<Run> buildChangeTypeRunSQL(Column column) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeTypeRunSQL(Column column)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRunSQL(Column column) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeDefaultRunSQL(Column column)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRunSQL(Column column) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeNullableRunSQL(Column column)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRunSQL(Column column) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(Column column)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAddCommentRunSQL(Column column) throws Exception{
		return new ArrayList<>();
	}

	/**
	 * 取消自增
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildDropAutoIncrement(Column column) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildDropAutoIncrement(Column column)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(StringBuilder builder, Column column){
		// 数据类型
		type(builder, column);
		// 编码
		charset(builder, column);
		// 默认值
		defaultValue(builder, column);
		// 非空
		nullable(builder, column);
		//主键
		primary(builder, column);
		// 递增
		if(column.isPrimaryKey() == 1) {
			increment(builder, column);
		}
		// 更新行事件
		onupdate(builder, column);
		// 备注
		comment(builder, column);
		// 位置
		position(builder, column);
		return builder;
	}
	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder type(StringBuilder builder, Column column){
		if(null == builder){
			builder = new StringBuilder();
		}
		boolean isIgnorePrecision = false;
		boolean isIgnoreScale = false;
		String typeName = column.getTypeName();
		ColumnType type = type(typeName);
		if(null != type){
			if(!type.support()){
				throw new RuntimeException("数据类型不支持:"+typeName);
			}
			isIgnorePrecision = type.ignorePrecision();
			isIgnoreScale = type.ignoreScale();
			typeName = type.getName();
		}else{
			isIgnorePrecision = isIgnorePrecision(column);
			isIgnoreScale = isIgnoreScale(column);
		}
		return type(builder, column, typeName, isIgnorePrecision, isIgnoreScale);
	}

	/**
	 * 列数据类型定义
	 * @param builder builder
	 * @param column 列
	 * @param type 数据类型(已经过转换)
	 * @param isIgnorePrecision 是否忽略长度
	 * @param isIgnoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(StringBuilder builder, Column column, String type, boolean isIgnorePrecision, boolean isIgnoreScale){
		if(null == builder){
			builder = new StringBuilder();
		}
		builder.append(type);
		if(!isIgnorePrecision) {
			Integer precision =  column.getPrecision();
			if (null != precision) {
				if (precision > 0) {
					builder.append("(").append(precision);
					Integer scale = column.getScale();
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

	@Override
	public Boolean checkIgnorePrecision(String type) {
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
	 *
	 * @param type 数据类型 如varchar int
	 * @return Boolean 检测不妯来时返回null
	 */
	@Override
	public Boolean checkIgnoreScale(String type) {
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

	@Override
	public boolean isIgnorePrecision(Column column) {
		ColumnType type = column.getColumnType();
		if(null != type){
			return type.ignorePrecision();
		}
		String typeName = column.getTypeName();
		if(null != typeName){
			String chk = typeName.toUpperCase();
			Boolean chkResult = checkIgnorePrecision(chk);
			if(null != chkResult){
				return chkResult;
			}
		}
		return false;
	}

	@Override
	public boolean isIgnoreScale(Column column) {
		ColumnType type = column.getColumnType();
		if(null != type){
			return type.ignoreScale();
		}
		String name = column.getTypeName();
		if(null != name){
			String chk = name.toUpperCase();
			Boolean chkResult = checkIgnoreScale(chk);
			if(null != chkResult){
				return chkResult;
			}
		}
		return false;
	}

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder nullable(StringBuilder builder, Column column){
		if(column.isNullable() == 0) {
			int nullable = column.isNullable();
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
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder charset(StringBuilder builder, Column column){
		// CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
		String typeName = column.getTypeName();
		if(null != typeName && typeName.toLowerCase().contains("char")) {
			String charset = column.getCharset();
			if (BasicUtil.isNotEmpty(charset)) {
				builder.append(" CHARACTER SET ").append(charset);
				String collate = column.getCollate();
				if (BasicUtil.isNotEmpty(collate)) {
					builder.append(" COLLATE ").append(collate);
				}
			}
		}
		return builder;
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder defaultValue(StringBuilder builder, Column column){
		Object def = column.getDefaultValue();
		if(null != def) {
			builder.append(" DEFAULT ");
			boolean isCharColumn = isCharColumn(column);
			if(def instanceof SQL_BUILD_IN_VALUE){
				String value = value(column, (SQL_BUILD_IN_VALUE)def);
				if(null != value){
					builder.append(value);
				}
			}else {
				def = write(column, def, false);
				if(null == def){
					def = column.getDefaultValue();
				}
				//format(builder, def);
				builder.append(def);
			}
		}
		return builder;
	}
	/**
	 * 主键(注意不要跟表定义中的主键重复)
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Column column){
		return builder;
	}

	/**
	 * 递增列
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(StringBuilder builder, Column column){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 StringBuilder increment(StringBuilder builder, Column column)", 37));
		}
		return builder;
	}




	/**
	 * 更新行事件
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder onupdate(StringBuilder builder, Column column){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 StringBuilder onupdate(StringBuilder builder, Column column)", 37));
		}
		return builder;
	}

	/**
	 * 位置
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(StringBuilder builder, Column column){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 StringBuilder position(StringBuilder builder, Column column)", 37));
		}
		return builder;
	}

	/**
	 * 备注
	 * 子类实现
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Column column){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 StringBuilder comment(StringBuilder builder, Column column)", 37));
		}
		return builder;
	}

	/**
	 * 创建或删除列时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkColumnExists(StringBuilder builder, boolean exists){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 checkColumnExists(StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRunSQL(Tag tag)
	 * List<Run> buildAlterRunSQL(Tag tag)
	 * List<Run> buildDropRunSQL(Tag tag)
	 * List<Run> buildRenameRunSQL(Tag tag)
	 * List<Run> buildChangeDefaultRunSQL(Tag tag)
	 * List<Run> buildChangeNullableRunSQL(Tag tag)
	 * List<Run> buildChangeCommentRunSQL(Tag tag)
	 * List<Run> buildChangeTypeRunSQL(Tag tag)
	 * StringBuilder checkTagExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Tag tag) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = tag.getTable(true);
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		// Tag update = tag.getUpdate();
		// if(null == update){
		// 添加标签
		builder.append(" ADD TAG ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, tag);
		// }
		return runs;
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Tag tag) throws Exception{
		List<Run> runs = new ArrayList<>();

		Tag update = tag.getUpdate();
		if(null != update){
			// 修改标签名
			String name = tag.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
				runs.addAll(buildRenameRunSQL(tag));
			}
			tag.setName(uname);
			// 修改数据类型
			String type = type(null, tag).toString();
			String utype = type(null, update).toString();
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<Run> list = buildChangeTypeRunSQL(tag);
				if(null != list){
					runs.addAll(list);
				}
			}else{
				//数据类型没变但长度变了
				if(tag.getPrecision() != update.getPrecision() || tag.getScale() != update.getScale()){
					List<Run> list = buildChangeTypeRunSQL(tag);
					if(null != list){
						runs.addAll(list);
					}
				}
			}
			// 修改默认值
			Object def = tag.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				runs.addAll(buildChangeDefaultRunSQL(tag));
			}
			// 修改非空限制
			int nullable = tag.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				runs.addAll(buildChangeNullableRunSQL(tag));
			}
			// 修改备注
			String comment = tag.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				runs.addAll(buildChangeCommentRunSQL(tag));
			}
		}

		return runs;
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Tag tag) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = tag.getTable(true);
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" DROP ").append(tag.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}


	/**
	 * 修改标签名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Tag tag) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = tag.getTable(true);
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" RENAME ").append(tag.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" ");
		SQLUtil.delimiter(builder, tag.getUpdate().getName(), getDelimiterFr(), getDelimiterTo()); 
		return runs;
	}

	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRunSQL(Tag tag) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeDefaultRunSQL(Tag tag)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRunSQL(Tag tag) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeNullableRunSQL(Tag tag)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRunSQL(Tag tag) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeCommentRunSQL(Tag tag)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<Run> buildChangeTypeRunSQL(Tag tag) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildChangeTypeRunSQL(Tag tag)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTagExists(StringBuilder builder, boolean exists){
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 StringBuilder checkTagExists(StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}


	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildAlterRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildDropRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildRenameRunSQL(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(PrimaryKey primary) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAddRunSQL(PrimaryKey primary)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param primary 主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(PrimaryKey primary) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(PrimaryKey primary)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(builder, primary.getTable(true));
		builder.append(" DROP CONSTRAINT ");
		SQLUtil.delimiter(builder, primary.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}
	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(PrimaryKey primary) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAddRunSQL(PrimaryKey primary)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildAddRunSQL(ForeignKey foreign) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Map<String,Column> columns = foreign.getColumns();
		if(columns.size()>0) {
			builder.append("ALTER TABLE ");
			name(builder, foreign.getTable(true));
			builder.append(" ADD");
			if(BasicUtil.isNotEmpty(foreign.getName())){
				builder.append(" CONSTRAINT ").append(foreign.getName());
			}
			builder.append(" FOREIGN KEY (");
			boolean first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				first = false;
			}
			builder.append(")");
			builder.append(" REFERENCES ").append(foreign.getReference().getName()).append("(");
			first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, column.getReference(), getDelimiterFr(), getDelimiterTo());
				first = false;
			}
			builder.append(")");

		}
		return runs;
	}
	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(ForeignKey foreign) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(PrimaryKey primary)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildDropRunSQL(ForeignKey foreign) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE");
		name(builder, foreign.getTable(true));
		builder.append(" DROP FOREIGN KEY ").append(foreign.getName());
		return runs;
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(ForeignKey foreign) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildRenameRunSQL(ForeignKey foreign) ", 37));
		}
		return new ArrayList<>();
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRunSQL(Index index) throws Exception
	 * List<Run> buildAlterRunSQL(Index index) throws Exception
	 * List<Run> buildDropRunSQL(Index index) throws Exception
	 * List<Run> buildRenameRunSQL(Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * ADD UNIQUE INDEX `A`(`ID`, `REG_TIME`) USING BTREE COMMENT '索引'
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Index index) throws Exception{
		String name = index.getName();
		if(BasicUtil.isEmpty(name)){
			name = "index_"+BasicUtil.getRandomString(10);
		}
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE");
		if(index.isUnique()){
			builder.append(" UNIQUE");
		}else if(index.isFulltext()){
			builder.append(" FULLTEXT");
		}else if(index.isSpatial()){
			builder.append(" SPATIAL");
		}
		builder.append(" INDEX ").append(name)
				.append(" ON ");//.append(index.getTableName(true));
		Table table = index.getTable(true);
		name(builder, table);
		builder.append("(");
		int qty = 0;
		for(Column column:index.getColumns().values()){
			if(qty>0){
				builder.append(",");
			}
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			String order = column.getOrder();
			if(BasicUtil.isNotEmpty(order)){
				builder.append(" ").append(order);
			}
			qty ++;
		}
		builder.append(")");
		String type = index.getType();
		if(BasicUtil.isNotEmpty(type)){
			builder.append("USING ").append(type).append(" ");
		}
		comment(builder, index);
		return runs;
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Index index) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAddRunSQL(Index index)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Index index) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = index.getTable(true);
		if(index.isPrimary()){
			builder.append("ALTER TABLE ");
			name(builder, table);
			builder.append(" DROP CONSTRAINT ").append(index.getName());
		}else {
			builder.append("DROP INDEX ").append(index.getName());
			if (BasicUtil.isNotEmpty(table)) {
				builder.append(" ON ");
				name(builder, table);
			}
		}
		return runs;
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Index index) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAddRunSQL(Index index)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 索引备注
	 * @param builder
	 * @param index
	 */
	public void comment(StringBuilder builder, Index index){
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildAlterRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildDropRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildRenameRunSQL(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Constraint constraint) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAddRunSQL(Constraint constraint)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Constraint constraint) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 List<Run> buildAlterRunSQL(Constraint constraint)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Constraint constraint) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		Table table = constraint.getTable(true);
		name(builder, table);
		builder.append(" DROP CONSTRAINT ").append(constraint.getName());
		 return runs;
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Constraint constraint) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = constraint.getTable(true);
		String catalog = constraint.getCatalog();
		String schema = table.getSchema();
		builder.append("ALTER CONSTRAINT ");
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, constraint.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" RENAME TO ");
		SQLUtil.delimiter(builder, constraint.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
 
		return runs;
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Trigger trigger) throws Exception
	 * List<Run> buildAlterRunSQL(Trigger trigger) throws Exception;
	 * List<Run> buildDropRunSQL(Trigger trigger) throws Exception;
	 * List<Run> buildRenameRunSQL(Trigger trigger) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(Trigger trigger) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE TRIGGER ").append(trigger.getName());
		builder.append(" ").append(trigger.getTime().sql()).append(" ");
		List<Trigger.EVENT> events = trigger.getEvents();
		boolean first = true;
		for(Trigger.EVENT event:events){
			if(!first){
				builder.append(" OR ");
			}
			builder.append(event);
			first = false;
		}
		builder.append(" ON ");
		name(builder, trigger.getTable(true));
		each(builder, trigger);

		builder.append("\n").append(trigger.getDefinition());

		return runs;
	}
	public void each(StringBuilder builder, Trigger trigger){
		if(trigger.isEach()){
			builder.append(" FOR EACH ROW ");
		}else{
			builder.append(" FOR EACH STATEMENT ");
		}
	}
	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param trigger 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Trigger trigger) throws Exception{
		return new ArrayList<>();
	}

	/**
	 * 删除触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Trigger trigger) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP TRIGGER ");
		Table table = trigger.getTable(true);
		if(null != table) {
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			if (BasicUtil.isNotEmpty(catalog)) {
				SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
			}
			if (BasicUtil.isNotEmpty(schema)) {
				SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
			}
		}
		SQLUtil.delimiter(builder, trigger.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Trigger trigger) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = trigger.getTable(true);
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		builder.append("ALTER TRIGGER ");
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, trigger.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" RENAME TO ");
		SQLUtil.delimiter(builder, trigger.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
 
		return runs;
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Procedure procedure) throws Exception
	 * List<Run> buildAlterRunSQL(Procedure procedure) throws Exception;
	 * List<Run> buildDropRunSQL(Procedure procedure) throws Exception;
	 * List<Run> buildRenameRunSQL(Procedure procedure) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRunSQL(Procedure procedure) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE PROCEDURE ");
		String catalog = procedure.getCatalog();
		String schema = procedure.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, procedure.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append("(\n");
		List<Parameter> ins = procedure.getInputs();
		List<Parameter> outs = procedure.getOutputs();
		boolean first = true;
		for(Parameter parameter:ins){
			if(parameter.isOutput()){
				continue;
			}
			if(!first){
				builder.append(",");
			}
			parameter(builder, parameter);
		}
		for(Parameter parameter:outs){
			if(!first){
				builder.append(",");
			}
			parameter(builder, parameter);
		}
		builder.append("\n)");
		String returnType = procedure.getReturnType();
		if(BasicUtil.isNotEmpty(returnType)){
			builder.append(" RETURNS ").append(returnType);
		}
		builder.append("\n");
		builder.append(procedure.getDefinition());
		return runs;
	}

	/**
	 * 生在输入输出参数
	 * @param builder builder
	 * @param parameter parameter
	 */
	public void parameter(StringBuilder builder, Parameter parameter){
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

	}
	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param procedure 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Procedure procedure) throws Exception{
		return new ArrayList<>();
	}

	/**
	 * 删除存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildDropRunSQL(Procedure procedure) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP PROCEDURE ");
		String catalog = procedure.getCatalog();
		String schema = procedure.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, procedure.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(Procedure procedure) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder(); 
		String catalog = procedure.getCatalog();
		String schema = procedure.getSchema();
		builder.append("ALTER PROCEDURE ");
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, procedure.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" RENAME TO ");
		SQLUtil.delimiter(builder, procedure.getUpdate().getName(), getDelimiterFr(), getDelimiterTo()); 
		return runs;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Function function) throws Exception
	 * List<Run> buildAlterRunSQL(Function function) throws Exception;
	 * List<Run> buildDropRunSQL(Function function) throws Exception;
	 * List<Run> buildRenameRunSQL(Function function) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildCreateRunSQL(Function function) throws Exception{
		return new ArrayList<>();
	}

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param function 函数
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Function function) throws Exception{
		return new ArrayList<>();
	}

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildDropRunSQL(Function function) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP FUNCTION ");
		String catalog = function.getCatalog();
		String schema = function.getSchema();
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, function.getName(), getDelimiterFr(), getDelimiterTo());
 
		return runs;
	}

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(Function function) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		String catalog = function.getCatalog();
		String schema = function.getSchema();
		builder.append("ALTER FUNCTION ");
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, function.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" RENAME TO ");
		SQLUtil.delimiter(builder, function.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(Column column)
	 *  boolean isNumberColumn(Column column)
	 * boolean isCharColumn(Column column)
	 * String value(Column column, SQL_BUILD_IN_VALUE value)
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

	@Override
	public boolean isBooleanColumn(Column column) {
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
	public  boolean isNumberColumn(Column column){
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
	public boolean isCharColumn(Column column) {
		return !isNumberColumn(column) && !isBooleanColumn(column);
	}
	/**
	 * 内置函数
	 * @param column 列属性
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(Column column, SQL_BUILD_IN_VALUE value){
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
	protected Object value(Map<String, Integer> keys, String key, ResultSet set, Object def) throws Exception{
		Integer index = keys.get(key);
		if(null != index && index >= 0){
			try {
				// db2 直接用 set.getObject(String) 可能发生 参数无效：未知列名 String
				return set.getObject(index);
			}catch (Exception e){
				return def;
			}
		}
		return def;
	}
	protected Object value(Map<String, Integer> keys, String key, ResultSet set) throws Exception{
		return value(keys, key, set, null);
	}

	@Override
	public String getPrimaryKey(Object obj){
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
	public Object getPrimaryValue(Object obj){
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
	public Object write(Column metadata, Object value, boolean placeholder){
		if(null == value){
			return value;
		}
		Object result = value;
		if(null != metadata && null != value){
			//根据列类型
			ColumnType ctype = metadata.getColumnType();
			if(null == ctype) {
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					ctype = type(typeName.toUpperCase());
				}
			}
			if(null != ctype){
				//拼接SQL需要引号或转换函数
				if(!placeholder){
					result = ctype.write(value, null, false);
				}else{
					Class writeClass = ctype.compatible();
					result = ConvertAdapter.convert(value, writeClass);
				}
			}
		}else{
			//根据值类型
			if(!placeholder) {
				if (null == value || BasicUtil.isNumber(value)) {

				} else {
					result = "'" + result + "'";
				}
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
	public Object read(Column metadata, Object value, Class clazz){
		//Object result = ConvertAdapter.convert(value, clazz);
		Object result = value;
		if(null == value){
			return null;
		}
		DataReader reader = null;
		ColumnType ctype = null;
		if (null != metadata) {
			ctype = metadata.getColumnType();
			if(null == ctype) {
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					ctype = type(typeName);
				}
			}
			if(null != ctype){
				reader = reader(ctype);
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
	public void value(StringBuilder builder, Object obj, String key){
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
		if(null == value){
			if(value instanceof SQL_BUILD_IN_VALUE){
				builder.append(value(null, (SQL_BUILD_IN_VALUE)value));
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
	}/*
	@Override
	public void format(StringBuilder builder, Object value){
		if(null == value || "NULL".equalsIgnoreCase(value.toString())){
			builder.append("null");
		}else if(value instanceof SQL_BUILD_IN_VALUE){
			builder.append(value((SQL_BUILD_IN_VALUE)value));
		}else if(value instanceof String){
			String str = (String)value;
			if(str.startsWith("${") && str.endsWith("}")){
				str = str.substring(2, str.length()-1);
			}else if(str.startsWith("'") && str.endsWith("'")){
			}else{
				str = "'" + str.replace("'", "''") + "'";
			}
			builder.append(str);
		}else if(value instanceof Timestamp){
			builder.append("'").append(value).append("'");
		}else if(value instanceof java.sql.Date){
			builder.append("'").append(value).append("'");
		}else if(value instanceof LocalDate){
			builder.append("'").append(value).append("'");
		}else if(value instanceof LocalTime){
			builder.append("'").append(value).append("'");
		}else if(value instanceof LocalDateTime){
			builder.append("'").append(value).append("'");
		}else if(value instanceof Date){
			builder.append("'").append(DateUtil.format((Date)value,DateUtil.FORMAT_DATE_TIME)).append("'");
		}else if(value instanceof Number || value instanceof Boolean){
			builder.append(value);
		}else if(value instanceof DataRow){
			builder.append("'").append(((DataRow)value).toJSON().replace("'","''")).append("'");
		} else if(value instanceof DataSet){
			builder.append("'").append(((DataSet)value).toJSON().replace("'","''")).append("'");
		} else{
			builder.append(value);
		}
	}
*/
	@Override
	public boolean convert(String catalog, String schema, String table, RunValue value){
		boolean result = false;
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			LinkedHashMap<String, Column> columns = null;
			if(null != dao) {
				columns = dao.columns(catalog, schema, table);
			}
			result = convert(columns, value);
		}else{
			result = convert((Column)null, value);
		}
		return result;
	}
	public LinkedHashMap<String, Column> columns(DataRuntime runtime, Table table, boolean metadata){
		LinkedHashMap<String, Column> columns = CacheProxy.columns(runtime.getKey(), table.getName());
		if(null == columns || columns.isEmpty()) {
			String random = random(runtime);
			try {
				List<Run> runs = buildQueryColumnRunSQL(table, metadata);
				if (null != runs) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, run);
						columns = columns(idx, true, table, columns, set);
						idx++;
					}
				}
			} catch (Exception e) {
				if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), table.getCatalog(), table.getSchema(), table.getName(), e.toString());
				}
			}
		}
		return columns;
	}
	@Override
	public boolean convert(DataRuntime runtime, Table table, Run run){
		boolean result = false;
		LinkedHashMap<String, Column> columns = table.getColumns();

		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			if(null == columns || columns.isEmpty()) {
				columns = columns(runtime, table, false);
			}
		}
		List<RunValue> values = run.getRunValues();
		for(RunValue value:values){
			if(ConfigTable.IS_AUTO_CHECK_METADATA){
				result = convert(columns, value);
			}else{
				result = convert((Column)null, value);
			}
		}
		return result;
	}
	@Override
	public boolean convert(Map<String,Column> columns, RunValue value){
		boolean result = false;
		if(null != columns && null != value){
			Column meta = columns.get(value.getKey().toUpperCase());
			result = convert(meta, value);
		}
		return result;
	}
	/**
	 * 根据数据库列属性 类型转换(一般是在更新数据库时调用)
	 * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
	 * @param metadata 列
	 * @param run RunValue
	 * @return boolean 是否完成类型转换,决定下一步是否继续
	 */
	public boolean convert(Column metadata, RunValue run){
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
				value = convert(metadata, value);
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
	public Object convert(Column metadata, Object value){
		if(null == value){
			return value;
		}
		try {
			if(null != metadata) {
				ColumnType columnType = metadata.getColumnType();
				value = convert(columnType, value);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return value;
	}
	public Object convert(ColumnType columnType, Object value){
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
						value = ConvertAdapter.convert(value, transfer);
					}
					if (null != compatible) {
						value = ConvertAdapter.convert(value, compatible);
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
	public String objectName(String name) {
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

}
