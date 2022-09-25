/* 
 * Copyright 2006-2022 www.anyline.org
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


package org.anyline.jdbc.adapter;


import org.anyline.dao.PrimaryCreater;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.exception.SQLException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.entity.*;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.prepare.Variable;
import org.anyline.jdbc.prepare.sql.auto.AutoSQL;
import org.anyline.jdbc.prepare.sql.auto.TableSQL;
import org.anyline.jdbc.prepare.sql.auto.TextSQL;
import org.anyline.jdbc.prepare.sql.auto.init.Join;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleTableSQL;
import org.anyline.jdbc.prepare.sql.xml.XMLSQL;
import org.anyline.jdbc.run.*;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class SimpleAdapter implements JDBCAdapter {
	protected static final Logger log = LoggerFactory.getLogger(SimpleAdapter.class);

	@Autowired(required=false)
	protected PrimaryCreater primaryCreater;


	@Autowired(required = false)
	@Qualifier("anyline.service")
	protected AnylineService service;


	public String delimiterFr = "";
	public String delimiterTo = "";
	public DB_TYPE type(){
		return null;
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
	 * public Run buildInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns)
	 * public void createInsertsTxt(StringBuilder builder, String dest, DataSet set,  List<String> keys)
	 * public void createInsertsTxt(StringBuilder builder, String dest, Collection list,  List<String> keys)
	 * public List<String> confirmInsertColumns(String dst, Object obj, String ... columns)
	 * protected void insertValue(StringBuilder builder, Object obj, List<String> keys)
	 *
	 * protected Run createInsertTxtFromEntity(String dest, Object obj, boolean checkParimary, String ... columns)
	 * protected Run createInsertTxtFromCollection(String dest, Collection list, boolean checkParimary, String ... columns)
	 ******************************************************************************************************************/

	/**
	 * 创建INSERT RunPrepare
	 * @param dest 表
	 * @param obj 实体
	 * @param checkParimary 是否检测主键
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run
	 */
	@Override
	public Run buildInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceHolder.parseDataSource(dest,obj);
		}

		if(obj instanceof Collection){
			Collection list = (Collection) obj;
			if(list.size() >0){
				return createInsertTxtFromCollection(dest, list, checkParimary, columns);
			}
			return null;
		}else {
			return createInsertTxtFromEntity(dest, obj, checkParimary, columns);
		}

	}

	/**
	 * 根据DataSet创建批量INSERT RunPrepare
	 * @param builder builder
	 * @param dest 表 如果不指定则根据set解析
	 * @param set 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set,  List<String> keys){
	}

	/**
	 * 根据Collection创建批量INSERT RunPrepare
	 * @param builder builder
	 * @param dest 表 如果不指定则根据set解析
	 * @param set 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInsertsTxt(StringBuilder builder, String dest, Collection list,  List<String> keys){
	}

	/**
	 * 确认需要插入的列
	 * @param obj  Entity或DataRow
	 * @param columns 提供额外的判断依据
	 *                列可以加前缀
	 *                +:表示必须插入
	 *                -:表示必须不插入
	 *                ?:根据是否有值
	 *
	 *        如果没有提供columns,长度为0也算没有提供
	 *        则解析obj(遍历所有的属性工Key)获取insert列
	 *
	 *        如果提供了columns则根据columns获取insert列
	 *
	 *        但是columns中出现了添加前缀列，则解析完columns后，继续解析obj
	 *
	 *        以上执行完后，如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true
	 *        则把执行结果与表结构对比，删除表中没有的列
	 * @return List
	 */
	@Override
	public List<String> confirmInsertColumns(String dst, Object obj, String ... columns){
		List<String> keys = null;/*确定需要插入的列*/
		if(null == obj){
			return new ArrayList<>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> mastKeys = new ArrayList<>();		//必须插入列
		List<String> ignores = new ArrayList<>();		//必须不插入列
		List<String> factKeys = new ArrayList<>();		//根据是否空值

		if(null != columns && columns.length>0){
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
			//是否插入null及""列
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
				isInsertNullColumn = ConfigTable.IS_INSERT_NULL_COLUMN;
				isInsertEmptyColumn = ConfigTable.IS_INSERT_EMPTY_COLUMN;
				if(AdapterProxy.hasAdapter()){
					keys = AdapterProxy.columns(obj.getClass());
				}else {
					keys = new ArrayList<>();
					List<Field> fields = ClassUtil.getFields(obj.getClass());
					for (Field field : fields) {
						Class clazz = field.getType();
						if (clazz == String.class || clazz == Date.class || ClassUtil.isPrimitiveClass(clazz)) {
							keys.add(field.getName());
						}
					}
				}
			}
			BeanUtil.removeAll(ignores, columns);
			BeanUtil.removeAll(keys, ignores);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
					//必须插入
					continue;
				}
				Object value = null;
				if(null != row) {
					value = row.get(key);
				}else{
					if(AdapterProxy.hasAdapter()){
						value = BeanUtil.getFieldValue(obj, AdapterProxy.field(obj.getClass(), key));
					}else{
						value = BeanUtil.getFieldValue(obj, key);
					}
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
		keys = checkMetadata(dst, keys);
		keys = BeanUtil.distinct(keys);
		return keys;
	}


	/**
	 * 根据entity创建 INSERT RunPrepare
	 * @param dest
	 * @param obj
	 * @param checkParimary
	 * @param columns
	 * @return Run
	 */
	protected Run createInsertTxtFromEntity(String dest, Object obj, boolean checkParimary, String ... columns){
		return null;
	}

	/**
	 * 根据collection创建 INSERT RunPrepare
	 * @param dest 表
	 * @param list 对象集合
	 * @param checkParimary 是否检测主键
	 * @param columns 需要插入的列，如果不指定则全部插入
	 * @return Run
	 */
	protected Run createInsertTxtFromCollection(String dest, Collection list, boolean checkParimary, String ... columns){
		return null;
	}




	/**
	 * 创建查询SQL
	 */
	@Override
	public Run buildQueryRun(RunPrepare prepare, ConfigStore configs, String ... conditions){
		Run run = null;
		if(prepare instanceof TableSQL){
			run = new TableRun(this,prepare.getTable());
		}else if(prepare instanceof XMLSQL){
			run = new XMLRun();
		}else if(prepare instanceof TextSQL){
			run = new TextRun();
		}
		if(null != run){
			run.setStrict(prepare.isStrict());
			run.setAdapter(this);
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			run.addConditions(conditions);
			run.init();
			buildQueryRunContent(run);
		}
		return run;
	}

	/**
	 * 构造查询主体
	 * @param run run
	 * @return Run
	 */
	@Override
	public Run buildQueryRunContent(Run run){
		if(null != run){
			if(run instanceof TableRun){
				TableRun r = (TableRun) run;
				return buildQueryRunContent(r);
			}else if(run instanceof XMLRun){
				XMLRun r = (XMLRun) run;
				return buildQueryRunContent(r);
			}else if(run instanceof TextRun){
				TextRun r = (TextRun) run;
				return buildQueryRunContent(r);
			}
		}
		return run;
	}
	protected Run buildQueryRunContent(XMLRun run){
		return run;
	}
	protected Run buildQueryRunContent(TextRun run){
		return run;
	}
	protected Run buildQueryRunContent(TableRun run){
		return run;
	}
	@Override
	public Run buildExecuteRunSQL(RunPrepare prepare, ConfigStore configs, String ... conditions){
		Run run = null;
		if(prepare instanceof XMLSQL){
			run = new XMLRun();
		}else if(prepare instanceof TextSQL){
			run = new TextRun();
		}
		if(null != run){
			run.setAdapter(this);
			run.setPrepare(prepare);
			run.setConfigStore(configs);
			run.addConditions(conditions);
			run.init();
		}
		return run;
	}
	@Override
	public Run buildDeleteRunSQL(String table, String key, Object values){
		return createDeleteRunSQLFromTable(table, key, values);
	}
	@Override
	public Run buildDeleteRunSQL(String dest, Object obj, String ... columns){
		if(null == obj){
			return null;
		}
		Run run = null;
		if(null == dest){
			dest = DataSourceHolder.parseDataSource(dest,obj);
		}
		if(null == dest){
			Object entity = obj;
			if(obj instanceof Collection){
				entity = ((Collection)obj).iterator().next();
			}
			if(AdapterProxy.hasAdapter()){
				dest = AdapterProxy.table(entity.getClass());
			}
		}
		if(obj instanceof ConfigStore){
			run = new TableRun(this,dest);
			RunPrepare prepare = new SimpleTableSQL();
			prepare.setDataSource(dest);
			run.setPrepare(prepare);
			run.setConfigStore((ConfigStore)obj);
			run.addConditions(columns);
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

	@SuppressWarnings("rawtypes")
	protected Run createDeleteRunSQLFromTable(String table, String key, Object values){
		return null;
	}
	protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){

		return null;
	}

	@Override
	public String getPrimaryKey(Object obj){
		if(null == obj){
			return null;
		}
		if(obj instanceof DataRow){
			return ((DataRow)obj).getPrimaryKey();
		}else{
			return null;
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
			return null;
		}
	}

	/**
	 * 求总数SQL
	 * Run 反转调用
	 * @param run  run
	 * @return String
	 */
	@Override
	public String parseTotalQueryTxt(Run run){
		return null;
	}

	@Override
	public String parseExistsTxt(Run run){
		return null;
	}

	@Override
	public void value(StringBuilder builder, Object obj, String key){
		Object value = null;
		if(obj instanceof DataRow){
			value = ((DataRow)obj).get(key);
		}else {
			if (AdapterProxy.hasAdapter()) {
				Field field = AdapterProxy.field(obj.getClass(), key);
				value = BeanUtil.getFieldValue(obj, field);
			} else {
				value = BeanUtil.getFieldValue(obj, key);
			}
		}
		format(builder, value);
	}
	@Override
	public void format(StringBuilder builder, Object value){
		if(null == value || "NULL".equalsIgnoreCase(value.toString())){
			builder.append("null");
		}else if(value instanceof String){
			String str = value.toString();
			if(str.startsWith("${") && str.endsWith("}") && !BeanUtil.isJson(value)){
				str = str.substring(2, str.length()-1);
			}else if(str.startsWith("'") && str.endsWith("'")){
			}else{
				str = "'" + str.replace("'", "''") + "'";
			}
			builder.append(str);
		}else if(value instanceof Timestamp){
			builder.append("'").append(value.toString()).append("'");
		}else if(value instanceof java.sql.Date){
			builder.append("'").append(value.toString()).append("'");
		}else if(value instanceof LocalDate){
			builder.append("'").append(value.toString()).append("'");
		}else if(value instanceof LocalTime){
			builder.append("'").append(value.toString()).append("'");
		}else if(value instanceof LocalDateTime){
			builder.append("'").append(value.toString()).append("'");
		}else if(value instanceof Date){
			builder.append("'").append(DateUtil.format((Date)value,DateUtil.FORMAT_DATE_TIME)).append("'");
		}else if(value instanceof Number || value instanceof Boolean){
			builder.append(value.toString());
		}else{
			builder.append(value.toString());
		}
	}


	@Override
	public Run createUpdateTxt(String dest, Object obj, boolean checkParimary, String ... columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = DataSourceHolder.parseDataSource(null,obj);
		}
		if(obj instanceof DataRow){
			return createUpdateTxtFromDataRow(dest,(DataRow)obj,checkParimary, columns);
		}else{
			return createUpdateTxtFromObject(dest, obj,checkParimary, columns);
		}
	}

	protected Run createUpdateTxtFromObject(String dest, Object obj, boolean checkParimary, String ... columns){
		return null;
	}
	protected Run createUpdateTxtFromDataRow(String dest, DataRow row, boolean checkParimary, String ... columns){
		return null;
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table 表
	 * @param columns columns
	 * @return List
	 */
	public List<String> checkMetadata(String table, List<String> columns){
		if(!ConfigTable.IS_AUTO_CHECK_METADATA || null == service){
			return columns;
		}
		List<String> list = new ArrayList<>();
		List<String> metadatas = service.columns(table);
		metadatas = BeanUtil.toUpperCase(metadatas);
		for (String item:columns){
			if(metadatas.contains(item.toUpperCase())){
				list.add(item);
			}
		}
		return list;
	}

	/**
	 * 确认需要更新的列
	 * @param row DataRow
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
	 *        以上执行完后，如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true
	 *        则把执行结果与表结构对比，删除表中没有的列
	 * @return List
	 */
	protected List<String> confirmUpdateColumns(String dest, DataRow row, String ... columns){
		List<String> keys = null;/*确定需要更新的列*/
		if(null == row){ 
			return new ArrayList<>();
		} 
		boolean each = true;//是否需要从row中查找列 
		List<String> masters = BeanUtil.copy(row.getUpdateColumns())		; //必须更新列
		List<String> ignores = BeanUtil.copy(row.getIgnoreUpdateColumns())	; //必须不更新列
		List<String> factKeys = new ArrayList<>()							; //根据是否空值
		BeanUtil.removeAll(ignores, columns);

		if(null != columns && columns.length>0){ 
			each = false; 
			keys = new ArrayList<>();
			for(String column:columns){ 
				if(BasicUtil.isEmpty(column)){ 
					continue; 
				} 
				if(column.startsWith("+")){ 
					column = column.substring(1, column.length());
					masters.add(column);
					each = true; 
				}else if(column.startsWith("-")){
					column = column.substring(1, column.length()); 
					ignores.add(column);
					each = true; 
				}else if(column.startsWith("?")){
					column = column.substring(1, column.length()); 
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
			//是否更新null及""列 
			boolean isUpdateNullColumn = row.isUpdateNullColumn();
			boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();
			BeanUtil.removeAll(keys, ignores);
			int size = keys.size(); 
			for(int i=size-1;i>=0; i--){ 
				String key = keys.get(i); 
				if(masters.contains(key)){
					//必须更新 
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
	public String parseTable(String table){
		if(null == table){
			return table;
		}
		table = table.replace(getDelimiterFr(), "").replace(getDelimiterTo(), "");
		table = DataSourceHolder.parseDataSource(table, null);
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


	@Override
	public boolean convert(String catalog, String schema, String table, RunValue run){
		boolean result = false;
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			LinkedHashMap<String, Column> columns = service.metadata().columns(catalog, schema, table);
			result = convert(columns, run);
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
	 * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
	 * @param column 列
	 * @param run RunValue
	 * @return boolean 是否完成类型转换，决定下一步是否继续
	 */
	@Override
	public boolean convert(Column column, RunValue run){
		if(null == column){
			return false;
		}
		if(null == run){
			return true;
		}
		Object value = run.getValue();
		if(null == value){
			return true;
		}
		try {
			String clazz = column.getClassName();
			String typeName = column.getTypeName().toUpperCase();
			if(null != typeName){
				//根据数据库类型
				if(typeName.equals("UUID")){
					if(value instanceof UUID) {
					}else{
						run.setValue(UUID.fromString(value.toString()));
					}
					return true;
				}else if(
						typeName.contains("CHAR")
								|| typeName.contains("TEXT")
				){
					if(value instanceof String){
					}else if(value instanceof Date){
						run.setValue(DateUtil.format((Date)value));
					}else{
						run.setValue(value.toString());
					}
					return true;
				}else if(typeName.equals("BIT")){
					if("0".equals(value.toString()) || "false".equalsIgnoreCase(value.toString())){
						run.setValue("0");
					}else{
						run.setValue("1");
					}
					return true;
				}else if(typeName.equals("DATETIME")){
					if(value instanceof Timestamp || value instanceof Date){
					}else {
						Date date = DateUtil.parse(value);
						if(null != date) {
							run.setValue(new Timestamp(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}else if(typeName.equals("DATE")){
					if(value instanceof java.sql.Date){
					}else {
						Date date = DateUtil.parse(value);
						if (null != date) {
							run.setValue(new java.sql.Date(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}else if(typeName.equals("TIME")){
					if(value instanceof Time){
					}else {
						Date date = DateUtil.parse(value);
						if (null != date) {
							run.setValue( new Time(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}
			}
			if(null != clazz){
				// 根据Java类
				// 不要解析String 许多不识别的类型会对应String 交给子类解析
				// 不要解析Boolean类型 有可能是 0,1
				if(clazz.contains("Integer")){
					if(value instanceof Integer){
					}else {
						run.setValue(BasicUtil.parseInt(value, null));
					}
					return true;
				}else if(clazz.contains("Long")){
					if(value instanceof Long){
					}else {
						run.setValue(BasicUtil.parseLong(value, null));
					}
					return true;
				}else if(clazz.contains("Double")){
					if(value instanceof Double){
					}else {
						run.setValue(BasicUtil.parseDouble(value, null));
					}
					return true;
				}else if(clazz.contains("Float")){
					if(value instanceof Float){
					}else {
						run.setValue(BasicUtil.parseFloat(value, null));
					}
					return true;
				}else if(clazz.contains("BigDecimal")){
					if(value instanceof BigDecimal){
					}else {
						run.setValue(BasicUtil.parseDecimal(value, null));
					}
					return true;
				}else if(clazz.contains("java.sql.Timestamp")){
					if(value instanceof Timestamp){
					}else {
						Date date = DateUtil.parse(value);
						if(null != date) {
							run.setValue(new Timestamp(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}else if(clazz.equals("java.sql.Time")){
					if(value instanceof Time){
					}else {
						Date date = DateUtil.parse(value);
						if (null != date) {
							run.setValue( new Time(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}else if(clazz.contains("java.sql.Date")){
					if(value instanceof java.sql.Date){
					}else {
						Date date = DateUtil.parse(value);
						if (null != date) {
							run.setValue(new java.sql.Date(date.getTime()));
						}else{
							run.setValue(null);
						}
					}
					return true;
				}
			}

		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}


	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区有
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)
	 * public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception
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
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)",37));
		return null;
	}

	@Override
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set)",37));
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}
	@Override
	public LinkedHashMap<String, Table> tables(boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, ResultSet set) throws Exception{
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
			Table table = tables.get(tableName.toUpperCase());
			if(null == table){
				if(create){
					table = new Table();
					tables.put(tableName.toUpperCase(), table);
				}else{
					continue;
				}
			}
			table.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalog));
			table.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema));
			table.setName(tableName);
			table.setType(string(keys, "TABLE_TYPE", set));
			table.setComment(string(keys, "REMARKS", set));
			table.setTypeCat(string(keys, "TYPE_CAT", set));
			table.setTypeName(string(keys, "TYPE_NAME", set));
			table.setSelfReferencingColumn(string(keys, "SELF_REFERENCING_COL_NAME", set));
			table.setRefGeneration(string(keys, "REF_GENERATION", set));
			tables.put(tableName.toUpperCase(), table);

			//table_map.put(table.getType().toUpperCase()+"_"+tableName.toUpperCase(), tableName);
		}
		return tables;
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types)
	 * public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, MasterTable> mtables(boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, ResultSet set) throws Exception
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
	public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types)",37));
		return null;
	}

	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param set 查询结果
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, ResultSet set) throws Exception{
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
			MasterTable table = tables.get(tableName.toUpperCase());
			if(null == table){
				if(create) {
					table = new MasterTable(tableName);
					tables.put(tableName.toUpperCase(), table);
				}else {
					continue;
				}
			}
			table.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalog));
			table.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema));
			table.setType(string(keys, "TABLE_TYPE", set));
			table.setComment(string(keys, "REMARKS", set));
			table.setTypeCat(string(keys, "TYPE_CAT", set));
			table.setTypeName(string(keys, "TYPE_NAME", set));
			table.setSelfReferencingColumn(string(keys, "SELF_REFERENCING_COL_NAME", set));
			table.setRefGeneration(string(keys, "REF_GENERATION", set));
			tables.put(tableName.toUpperCase(), table);

			//table_map.put(table.getType().toUpperCase()+"_"+tableName.toUpperCase(), tableName);
		}
		return tables;
	}


	/**
	 * 从上一步生成的SQL查询结果中 提取表结构
	 * @param index 第几条SQL
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set)",37));
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types)
 	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master)
 	 * public LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable master, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception
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
	public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types)",37));
		return null;
	}
	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryPartitionTableRunSQL(MasterTable master)",37));
		return null;
	}

	/**
	 *  根据查询结果集构造Table
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, PartitionTable> ptables(int index, boolean create, MasterTable table, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set)",37));
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		return tables;
	}

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable master, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception{
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
			PartitionTable table = tables.get(tableName.toUpperCase());
			if(null == table){
				if(create) {
					table = new PartitionTable(tableName);
					tables.put(tableName.toUpperCase(), table);
				}else {
					continue;
				}
			}
			table.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalog));
			table.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema));
			table.setType(string(keys, "TABLE_TYPE", set));
			table.setComment(string(keys, "REMARKS", set));
			table.setTypeCat(string(keys, "TYPE_CAT", set));
			table.setTypeName(string(keys, "TYPE_NAME", set));
			table.setSelfReferencingColumn(string(keys, "SELF_REFERENCING_COL_NAME", set));
			table.setRefGeneration(string(keys, "REF_GENERATION", set));
			tables.put(tableName.toUpperCase(), table);

			//table_map.put(table.getType().toUpperCase()+"_"+tableName.toUpperCase(), tableName);
		}
		return tables;
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception
	 * public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception
	 *
	 * protected Column column(Column column, SqlRowSetMetaData rsm, int index)
	 * protected Column column(Column column, ResultSet rs)
	 * protected List<String> keys(ResultSet rs) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryColumnRunSQL(Table table, boolean metadata)",37));
		return null;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set)",37));
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		return columns;
	}
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, SqlRowSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		SqlRowSetMetaData rsm = set.getMetaData();
		for (int i = 1; i <= rsm.getColumnCount(); i++) {
			String name = rsm.getColumnName(i);
			if(BasicUtil.isEmpty(name)){
				continue;
			}
			Column column = columns.get(name.toUpperCase());
			if(null == column){
				if(create){
					column = column(column, rsm, i);
					columns.put(column.getName().toUpperCase(), column);
				}else{
					continue;
				}
			}
		}
		return columns;
	}
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, Table table, LinkedHashMap<String, Column> columns, ResultSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		Map<String,Integer> keys = keys(set);
		while (set.next()){
			String name = set.getString("COLUMN_NAME");
			if(null == name){
				continue;
			}
			Column column = columns.get(name.toUpperCase());
			if(null == column){
				if(create) {
					column = new Column(name);
					columns.put(name.toUpperCase(), column);
				}else {
					continue;
				}
			}
			String remark = string(keys, "REMARKS", set, column.getComment());
			if("TAG".equals(remark)){
				column = new Tag();
			}
			column.setName(name);
			column.setComment(remark);
			column.setCatalog(string(keys,"TABLE_CAT", set, table.getCatalog()));
			column.setSchema(string(keys,"TABLE_SCHEM", set, table.getSchema()));
			column.setTableName(string(keys,"TABLE_NAME", set, table.getName()));
			column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
			column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
			column.setTypeName(string(keys, "TYPE_NAME", set, column.getTypeName()));
			column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
			column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
			column.setNullable(bool(keys, "NULLABLE", set, column.isNullable()));
			column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
			column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
			column.setAutoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
			column(column, set);
		}
		return columns;
	}


	protected Column column(Column column, SqlRowSetMetaData rsm, int index){
		if(null == column){
			column = new Column();
		}
		try {
			column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
			column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
			column.setClassName(rsm.getColumnClassName(index));
			column.setCaseSensitive(rsm.isCaseSensitive(index));
			column.setCurrency(rsm.isCurrency(index));
			column.setComment(rsm.getColumnLabel(index));
			column.setName(rsm.getColumnName(index));
			column.setPrecision(rsm.getPrecision(index));
			column.setScale(rsm.getScale(index));
			column.setDisplaySize(rsm.getColumnDisplaySize(index));
			column.setSigned(rsm.isSigned(index));
			column.setTableName(rsm.getTableName(index));
			column.setType(rsm.getColumnType(index));
			column.setTypeName(rsm.getColumnTypeName(index));
		}catch (Exception e){
			e.printStackTrace();
		}
		return column;
	}

	/**
	 * 构建Column
	 * @param column 列
	 * @param rs  ResultSet
	 * @return Column
	 */
	protected Column column(Column column, ResultSet rs){
		if(null == column){
			column = new Column();
		}
		try {
			Map<String,Integer> keys = keys(rs);
			column.setScale(BasicUtil.parseInt(string(keys, "DECIMAL_DIGITS", rs), null));
			column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
			column.setAutoIncrement(BasicUtil.parseBoolean(string(keys, "IS_AUTOINCREMENT", rs), false));
			column.setGenerated(BasicUtil.parseBoolean(string(keys, "IS_GENERATEDCOLUMN", rs), false));
			column.setComment(string(keys, "REMARKS", rs));
			column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
			if (BasicUtil.isEmpty(column.getDefaultValue())) {
				column.setDefaultValue(string(keys, "COLUMN_DEF", rs));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return column;
	}

	/**
	 * 获取ResultSet中的列
	 * @param rs rs
	 * @return list
	 * @throws Exception Exception
	 */
	protected Map<String, Integer> keys(ResultSet set) throws Exception{
		ResultSetMetaData rsmd = set.getMetaData();
		Map<String, Integer> keys = new HashMap<>();
		if(null != rsmd){
			for (int i = 1; i < rsmd.getColumnCount(); i++) {
				keys.put(rsmd.getColumnName(i).toUpperCase(), i);
			}
		}
		return keys;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTagRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set) throws Exception
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryTagRunSQL(Table table, boolean metadata)",37));
		return null;
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set)",37));
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set)",37));
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, ResultSet set)",37));
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryIndexRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, ResultSet set) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryIndexRunSQL(Table table, boolean metadata) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryIndexRunSQL(Table table, boolean metadata)",37));
		return null;
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception
	 */
	@Override
	public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set)",37));
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set)",37));
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, ResultSet set) throws Exception{
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		Map<String, Integer> keys = keys(set);
		LinkedHashMap<String, Column> columns = null;
		while (set.next()) {
			String name = string(keys, "INDEX_NAME", set);
			if(null == name){
				continue;
			}
			Index index = indexs.get(name.toUpperCase());
			if(null == index){
				if(create){
					index = new Index();
					indexs.put(name.toUpperCase(), index);
				}else{
					continue;
				}
				index.setName(string(keys, "INDEX_NAME", set));
				index.setType(integer(keys, "TYPE", set, null));
				index.setUnique(!bool(keys, "NON_UNIQUE", set, false));
				index.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), table.getCatalog()));
				index.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), table.getSchema()));
				index.setTable(string(keys, "TABLE_NAME", set));
				indexs.put(name, index);
				columns = new LinkedHashMap<>();
				index.setColumns(columns);
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
		return indexs;
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildQueryConstraintRunSQL(Table table, boolean metadata)",37));
		return null;
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param constraint 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的，这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws exception
	 */
	@Override
	public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set)",37));
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set)",37));
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set)",37));
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}






	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区有
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/
	
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(Table table);
	 * public String buildAlterRunSQL(Table table);
     * public String buildRenameRunSQL(Table table);
	 * public String buildChangeCommentRunSQL(Table table);
	 * public String buildDropRunSQL(Table table);
	 * public StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * public StringBuilder primary(StringBuilder builder, Table table)
	 * public StringBuilder comment(StringBuilder builder, Table table)
	 * public StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public String buildCreateRunSQL(Table table) throws Exception{
		StringBuilder builder = new StringBuilder();
		table.setCreater(this);
		builder.append("CREATE ").append(table.getKeyword()).append(" ");
		checkTableExists(builder, false);
		name(builder, table);
		LinkedHashMap columMap = table.getColumns();
		if(null != columMap){
			Collection<Column> columns = columMap.values();
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
				primary(builder, table);
				builder.append(")");
			}
		}
		comment(builder, table);
		return builder.toString();
	}

	@Override
	public String buildAlterRunSQL(Table table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAlterRunSQL(Table table)",37));
		return null;
	}
	/**
	 * 修改表名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildRenameRunSQL(Table table)",37));
		return null;
	}

	@Override
	public String buildChangeCommentRunSQL(Table table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeCommentRunSQL(Table table)",37));
		return null;
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Table table) throws Exception{
		table.setCreater(this);

		StringBuilder builder = new StringBuilder();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		builder.append("DROP ").append(table.getKeyword()).append(" ");
		checkTableExists(builder, true);
		name(builder, table);
		return builder.toString();
	}

	/**
	 * 创建或删除表时检测表是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return
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
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				idx ++;
			}
			builder.append(")");
		}
		return builder;
	}


	/**
	 * 备注
	 * 子类实现
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
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(MasterTable table);
	 * public String buildAlterRunSQL(MasterTable table);
	 * public String buildDropRunSQL(MasterTable table);
	 * public String buildRenameRunSQL(MasterTable table);
	 * public String buildChangeCommentRunSQL(MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildCreateRunSQL(MasterTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildCreateRunSQL(MasterTable table)",37));
		return null;
	}
	@Override
	public String buildAlterRunSQL(MasterTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAlterRunSQL(MasterTable table)",37));
		return null;
	}
	@Override
	public String buildDropRunSQL(MasterTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildDropRunSQL(MasterTable table)",37));
		return null;
	}
	@Override
	public String buildRenameRunSQL(MasterTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildRenameRunSQL(MasterTable table)",37));
		return null;
	}
	@Override
	public String buildChangeCommentRunSQL(MasterTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeCommentRunSQL(MasterTable table)",37));
		return null;
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(PartitionTable table);
	 * public String buildAlterRunSQL(PartitionTable table);
	 * public String buildDropRunSQL(PartitionTable table);
	 * public String buildRenameRunSQL(PartitionTable table);
	 * public String buildChangeCommentRunSQL(PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildCreateRunSQL(PartitionTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildCreateRunSQL(PartitionTable table)",37));
		return null;
	}
	@Override
	public String buildAlterRunSQL(PartitionTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAlterRunSQL(PartitionTable table)",37));
		return null;
	}
	@Override
	public String buildDropRunSQL(PartitionTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildDropRunSQL(PartitionTable table)",37));
		return null;
	}
	@Override
	public String buildRenameRunSQL(PartitionTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildRenameRunSQL(PartitionTable table)",37));
		return null;
	}
	@Override
	public String buildChangeCommentRunSQL(PartitionTable table) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeCommentRunSQL(PartitionTable table)",37));
		return null;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String alterColumnKeyword()
	 * public String buildAddRunSQL(Column column)
	 * public List<String> buildAlterRunSQL(Column column)
	 * public String buildDropRunSQL(Column column)
	 * public String buildRenameRunSQL(Column column)
	 * public List<String> buildChangeTypeRunSQL(Column column)
	 * public String buildChangeDefaultRunSQL(Column column)
	 * public String buildChangeNullableRunSQL(Column column)
	 * public String buildChangeCommentRunSQL(Column column)
	 * public StringBuilder define(StringBuilder builder, Column column)
	 * public StringBuilder type(StringBuilder builder, Column column)
	 * public StringBuilder nullable(StringBuilder builder, Column column)
	 * public StringBuilder charset(StringBuilder builder, Column column)
	 * public StringBuilder defaultValue(StringBuilder builder, Column column)
	 * public StringBuilder increment(StringBuilder builder, Column column)
	 * public StringBuilder onupdate(StringBuilder builder, Column column)
	 * public StringBuilder position(StringBuilder builder, Column column)
	 * public StringBuilder comment(StringBuilder builder, Column column)
	 * public StringBuilder checkColumnExists(StringBuilder builder, boolean exists)
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
	public String buildAddRunSQL(Column column) throws Exception{
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		//Column update = column.getUpdate();
		//if(null == update){
		//添加列
		builder.append(" ADD ").append(column.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, column);
		//}
		return builder.toString();
	}


	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column) throws Exception{
		List<String> sqls = new ArrayList<>();

		Column update = column.getUpdate();
		if(null != update){
			column.setCreater(this);
			update.setCreater(this);

			//修改列名
			String name = column.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
				String sql = buildRenameRunSQL(column);
				if(null != sql){
					sqls.add(sql);
				}
			}
			column.setName(uname);
			//修改数据类型
			String type = type2type(column.getTypeName());
			String utype = type2type(update.getTypeName());
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<String> list = buildChangeTypeRunSQL(column);
				if(null != list){
					sqls.addAll(list);
				}
			}
			//修改默认值
			Object def = column.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				String sql = buildChangeDefaultRunSQL(column);
				if(null != sql){
					sqls.add(sql);
				}
			}
			//修改非空限制
			int nullable = column.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				String sql = buildChangeNullableRunSQL(column);
				if(null != sql){
					sqls.add(sql);
				}
			}
			//修改备注
			String comment = column.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				String sql = buildChangeCommentRunSQL(column);
				if(null != sql){
					sqls.add(sql);
				}
			}
		}

		return sqls;
	}


	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Column column) throws Exception{
		if(column instanceof Tag){
			Tag tag = (Tag)column;
			return buildDropRunSQL(tag);
		}

		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" DROP ").append(column.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}

	/**
	 * 修改列名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) throws Exception{
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" RENAME ").append(column.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" ");
		SQLUtil.delimiter(builder, column.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}


	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildChangeTypeRunSQL(Column column)",37));
		return null;
	}
	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Column column) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeDefaultRunSQL(Column column)",37));
		return null;
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Column column) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeNullableRunSQL(Column column)",37));
		return null;
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeCommentRunSQL(Column column)",37));
		return null;
	}




	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(StringBuilder builder, Column column){
		//数据类型
		type(builder, column);
		// 编码
		charset(builder, column);
		//默认值
		defaultValue(builder, column);
		//递增列
		increment(builder, column);
		//非空
		nullable(builder, column);
		//更新行事件
		onupdate(builder, column);
		//备注
		comment(builder, column);
		//位置
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

		builder.append(type2type(column.getTypeName()));
		//精度
		Integer precision = column.getPrecision();
		Integer scale = column.getScale();
		if(null != precision) {
			if (precision > 0) {
				builder.append("(").append(precision);
				if (null != scale && scale > 0) {
					builder.append(",").append(scale);
				}
				builder.append(")");
			} else if (precision == -1) {
				builder.append("(max)");
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
		String charset = column.getCharset();
		if(BasicUtil.isNotEmpty(charset)){
			builder.append(" CHARACTER SET ").append(charset);
			String collate = column.getCollate();
			if(BasicUtil.isNotEmpty(collate)){
				builder.append(" COLLATE ").append(collate);
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
				String value = buildInValue((SQL_BUILD_IN_VALUE)def);
				if(null != value){
					builder.append(value);
				}
			}else {
				format(builder, def);
			}
		}
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 StringBuilder increment(StringBuilder builder, Column column)",37));
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 StringBuilder onupdate(StringBuilder builder, Column column)",37));
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 StringBuilder position(StringBuilder builder, Column column)",37));
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 StringBuilder comment(StringBuilder builder, Column column)",37));
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 checkColumnExists(StringBuilder builder, boolean exists)",37));
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Tag tag)
	 * public List<String> buildAlterRunSQL(Tag tag)
	 * public String buildDropRunSQL(Tag tag)
	 * public String buildRenameRunSQL(Tag tag)
	 * public String buildChangeDefaultRunSQL(Tag tag)
	 * public String buildChangeNullableRunSQL(Tag tag)
	 * public String buildChangeCommentRunSQL(Tag tag)
	 * public List<String> buildChangeTypeRunSQL(Tag tag)
	 * public StringBuilder checkTagExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Tag tag) throws Exception{
		tag.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = tag.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		//Tag update = tag.getUpdate();
		//if(null == update){
		//添加标签
		builder.append(" ADD TAG ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, tag);
		//}
		return builder.toString();
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Tag tag) throws Exception{
		List<String> sqls = new ArrayList<>();

		Tag update = tag.getUpdate();
		if(null != update){
			tag.setCreater(this);
			update.setCreater(this);

			//修改标签名
			String name = tag.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith("_TMP_UPDATE_TYPE")){
				String sql = buildRenameRunSQL(tag);
				if(null != sql){
					sqls.add(sql);
				}
			}
			tag.setName(uname);
			//修改数据类型
			String type = type2type(tag.getTypeName());
			String utype = type2type(update.getTypeName());
			if(!BasicUtil.equalsIgnoreCase(type, utype)){
				List<String> list = buildChangeTypeRunSQL(tag);
				if(null != list){
					sqls.addAll(list);
				}
			}
			//修改默认值
			Object def = tag.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)){
				String sql = buildChangeDefaultRunSQL(tag);
				if(null != sql){
					sqls.add(sql);
				}
			}
			//修改非空限制
			int nullable = tag.isNullable();
			int unullable = update.isNullable();
			if(nullable != unullable){
				String sql = buildChangeNullableRunSQL(tag);
				if(null != sql){
					sqls.add(sql);
				}
			}
			//修改备注
			String comment = tag.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)){
				String sql = buildChangeCommentRunSQL(tag);
				if(null != sql){
					sqls.add(sql);
				}
			}
		}

		return sqls;
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Tag tag) throws Exception{
		tag.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = tag.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" DROP ").append(tag.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}


	/**
	 * 修改标签名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Tag tag) throws Exception{
		tag.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = tag.getTable();
		builder.append("ALTER ").append(table.getKeyword()).append(" ");
		name(builder, table);
		builder.append(" RENAME ").append(tag.getKeyword()).append(" ");
		SQLUtil.delimiter(builder, tag.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" ");
		SQLUtil.delimiter(builder, tag.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}

	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Tag tag) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeDefaultRunSQL(Tag tag)",37));
		return null;
	}

	/**
	 * 修改非空限制
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Tag tag) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeNullableRunSQL(Tag tag)",37));
		return null;
	}
	/**
	 * 修改备注
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Tag tag) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildChangeCommentRunSQL(Tag tag)",37));
		return null;
	}

	/**
	 * 修改数据类型
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Tag tag) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildChangeTypeRunSQL(Tag tag)",37));
		return null;
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
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 StringBuilder checkTagExists(StringBuilder builder, boolean exists)",37));
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Index index) throws Exception
	 * public List<String> buildAlterRunSQL(Index index) throws Exception
	 * public String buildDropRunSQL(Index index) throws Exception
	 * public String buildRenameRunSQL(Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	public String buildAddRunSQL(Index index) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAddRunSQL(Index index)",37));
		return null;
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Index index) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAddRunSQL(Index index)",37));
		return null;
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	public String buildDropRunSQL(Index index) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAddRunSQL(Index index)",37));
		return null;
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	public String buildRenameRunSQL(Index index) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAddRunSQL(Index index)",37));
		return null;
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Constraint constraint) throws Exception
	 * public List<String> buildAlterRunSQL(Constraint constraint) throws Exception
	 * public String buildDropRunSQL(Constraint constraint) throws Exception
	 * public String buildRenameRunSQL(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildAddRunSQL(Constraint constraint) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildAddRunSQL(Constraint constraint)",37));
		return null;
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Constraint constraint) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 List<String> buildAlterRunSQL(Constraint constraint)",37));
		return null;
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	public String buildDropRunSQL(Constraint constraint) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildDropRunSQL(Constraint constraint)",37));
		return null;
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	public String buildRenameRunSQL(Constraint constraint) throws Exception{
		log.warn(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.jdbc.config.db.impl.","") + ")未实现 String buildRenameRunSQL(Constraint constraint)",37));
		return null;
	}


	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * public boolean isBooleanColumn(Column column)
	 * public  boolean isNumberColumn(Column column)
	 * public boolean isCharColumn(Column column)
	 * public String buildInValue(SQL_BUILD_IN_VALUE value)
	 * public String type2type(String type)
	 * public String type2class(String type)
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
			//如果没有同步法数据库，直接生成column可能只设置了type Name
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
					clazz.contains("int")
							|| clazz.contains("integer")
							|| clazz.contains("long")
							|| clazz.contains("decimal")
							|| clazz.contains("float")
							|| clazz.contains("double")
							|| clazz.contains("timestamp")
							//|| clazz.contains("bit")
							|| clazz.contains("short")
			){
				return true;
			}
		}else{
			//如果没有同步法数据库，直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type){
				type = type.toLowerCase();
				if(type.contains("int")
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
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		return null;
	}

	@Override
	public String type2type(String type){
		return type;
	}
	@Override
	public String type2class(String type){
		return type;
	}

	/**
	 * 先检测rs中是否包含当前key 如果包含再取值, 取值时按keys中的大小写为准
	 * @param keys keys
	 * @param key key
	 * @param set ResultSet
	 * @return String
	 * @throws Exception
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
		if(null != index){
			//db2 直接用 set.getObject(String) 可能发行 参数无效：未知列名 String
			return set.getObject(index);
		}
		return def;
	}
	protected Object value(Map<String, Integer> keys, String key, ResultSet set) throws Exception{
		return value(keys, key, set, null);
	}

}
