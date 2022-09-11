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


package org.anyline.jdbc.config.db.impl;


import org.anyline.dao.PrimaryCreater;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.RunValue;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.run.impl.TableRunSQLImpl;
import org.anyline.jdbc.config.db.run.impl.TextRunSQLImpl;
import org.anyline.jdbc.config.db.run.impl.XMLRunSQLImpl;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
import org.anyline.jdbc.config.db.sql.auto.TextSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.jdbc.config.db.sql.xml.XMLSQL;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.jdbc.exception.SQLException;
import org.anyline.jdbc.exception.SQLUpdateException;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class BasicSQLCreaterImpl implements SQLCreater{
	protected static final Logger log = LoggerFactory.getLogger(BasicSQLCreaterImpl.class);

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
	/** 
	 * 创建查询SQL 
	 */ 
	@Override 
	public RunSQL buildQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions){
		RunSQL run = null; 
		if(sql instanceof TableSQL){ 
			run = new TableRunSQLImpl(this,sql.getTable());
		}else if(sql instanceof XMLSQL){ 
			run = new XMLRunSQLImpl();
		}else if(sql instanceof TextSQL){ 
			run = new TextRunSQLImpl(); 
		} 
		if(null != run){
			run.setStrict(sql.isStrict()); 
			run.setCreater(this); 
			run.setSql(sql);
			run.setConfigStore(configs); 
			run.addConditions(conditions);
			run.init();
			run.createRunQueryTxt();
		} 
		return run; 
	}
	@Override
	public RunSQL buildExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions){
		RunSQL run = null;
		if(sql instanceof XMLSQL){
			run = new XMLRunSQLImpl();
		}else if(sql instanceof TextSQL){
			run = new TextRunSQLImpl();
		}
		if(null != run){
			run.setCreater(this);
			run.setSql(sql);
			run.setConfigStore(configs);
			run.addConditions(conditions);
			run.init();
		}
		return run;
	}
	@Override
	public RunSQL buildDeleteRunSQL(String table, String key, Object values){
		return createDeleteRunSQLFromTable(table, key, values);
	}
	@Override 
	public RunSQL buildDeleteRunSQL(String dest, Object obj, String ... columns){
		if(null == obj){ 
			return null; 
		}
		RunSQL run = null; 
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
			run = new TableRunSQLImpl(this,dest);
			SQL sql = new TableSQLImpl();
			sql.setDataSource(dest);
			run.setSql(sql);
			run.setConfigStore((ConfigStore)obj);
			run.addConditions(columns);
			run.init();
			run.createRunDeleteTxt();
		}else{
			run = createDeleteRunSQLFromEntity(dest, obj, columns);
		}
		return run; 
	}
	@SuppressWarnings("rawtypes")
	private RunSQL createDeleteRunSQLFromTable(String table, String key, Object values){
		if(null == table || null == key || null == values){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		TableRunSQLImpl run = new TableRunSQLImpl(this,table);
		builder.append("DELETE FROM ").append(table).append(" WHERE ");
		if(values instanceof Collection){
			Collection cons = (Collection)values;
			SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(cons.size() > 1){
				builder.append(" IN(");
				int idx = 0;
				for(Object obj:cons){
					if(idx > 0){
						builder.append(",");
					}
					//builder.append("'").append(obj).append("'");
					builder.append("?");
					idx ++;
				}
				builder.append(")");
			}else if(cons.size() == 1){
				for(Object obj:cons){
					builder.append("=?");
				}
			}else{
				throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
			}
		}else{
			SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			builder.append("=?");
		}
		run.addValues(key, values);
		run.setBuilder(builder);

		return run;
	}
	private RunSQL createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){
		TableRunSQLImpl run = new TableRunSQLImpl(this,dest);
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ").append(parseTable(dest)).append(" WHERE ");
		List<String> keys = new ArrayList<>();
		if(null != columns && columns.length>0){
			for(String col:columns){
				keys.add(col);
			}
		}else{
			if(obj instanceof DataRow){
				keys = ((DataRow)obj).getPrimaryKeys();
			}else{
				if(AdapterProxy.hasAdapter()){
					keys = AdapterProxy.primaryKeys(obj.getClass());
				}
			}
		}
		int size = keys.size();
		if(size >0){
			for(int i=0; i<size; i++){
				if(i > 0){
					builder.append("\nAND ");
				}
				String key = keys.get(i);

				SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ? ");
				Object value = null;
				if(obj instanceof DataRow){
					value = ((DataRow)obj).get(key);
				}else{
					if(AdapterProxy.hasAdapter()){
						value = BeanUtil.getFieldValue(obj,AdapterProxy.field(obj.getClass(), key));
					}else{
						value = BeanUtil.getFieldValue(obj, key);
					}
				}
				run.addValues(key,value);
			}
		}else{
			throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
		}
		run.setBuilder(builder);

		return run; 
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
	 * 基础查询SQL 
	 * RunSQL 反转调用 
	 */ 
	@Override 
	public String parseBaseQueryTxt(RunSQL run){ 
		return run.getBuilder().toString();
	} 
	/** 
	 * 求总数SQL 
	 * RunSQL 反转调用 
	 * @param run  run
	 * @return String
	 */
	@Override
	public String parseTotalQueryTxt(RunSQL run){
		String sql = "SELECT COUNT(0) AS CNT FROM (\n" + run.getBuilder().toString() +"\n) F";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
		return sql;
	}

	@Override
	public String parseExistsTxt(RunSQL run){
		String sql = "SELECT EXISTS(\n" + run.getBuilder().toString() +"\n)  IS_EXISTS";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
		return sql;
	}
	 
	 
 

 
	@Override 
	public RunSQL buildInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns){
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

	private RunSQL createInsertTxtFromEntity(String dest, Object obj, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl(this,dest);
		//List<Object> values = new ArrayList<Object>();
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(dest)){
			throw new SQLException("未指定表");
		}
		StringBuilder param = new StringBuilder();
		DataRow row = null;
		if(obj instanceof DataRow){
			row = (DataRow)obj;
			if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
				String pk = row.getPrimaryKey();
				if(null == pk){
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
				}
				row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
			}
		}else{
			String pk = null;
			Object pv = null;
			if(AdapterProxy.hasAdapter()){
				pk = AdapterProxy.primaryKey(obj.getClass());
				pv = AdapterProxy.primaryValue(obj);
				AdapterProxy.createPrimaryValue(obj);
			}else{
				pk = DataRow.DEFAULT_PRIMARY_KEY;
				pv = BeanUtil.getFieldValue(obj, pk);
				if(null != primaryCreater && null == pv){
					pv = primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null);
					BeanUtil.setFieldValue(obj, pk, pv);
				}
			}
		}

		/*确定需要插入的列*/
		
		List<String> keys = confirmInsertColumns(dest, obj, columns);
		if(null == keys || keys.size() == 0){
			throw new SQLException("未指定列(DataRow或Entity中没有需要更新的属性值)["+obj.getClass().getName()+":"+BeanUtil.object2json(obj)+"]");
		}
		builder.append("INSERT INTO ").append(parseTable(dest));
		builder.append("(");
		param.append(") VALUES (");
		List<String> insertColumns = new ArrayList<>();
		int size = keys.size();
		for(int i=0; i<size; i++){
			String key = keys.get(i);
			Object value = null;
			if(null != row){
				value = row.get(key);
			}else{
				if(AdapterProxy.hasAdapter()){
					value = BeanUtil.getFieldValue(obj, AdapterProxy.field(obj.getClass(), key));
				}else{
					value = BeanUtil.getFieldValue(obj, key);
				}
			}
			SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") && !BeanUtil.isJson(value)){
				String str = value.toString();
				value = str.substring(2, str.length()-1);
				if(value.toString().startsWith("${") && value.toString().endsWith("}")){
					//保存json时可以{json格式}最终会有两层:{{a:1}}8.5之前
					param.append("?");
					insertColumns.add(key);
					//values.add(value);
					run.addValues(key, value);
				}else {
					param.append(value);
				}
			}else{
				param.append("?");
				insertColumns.add(key);
				if("NULL".equals(value)){
					//values.add(null);
					run.addValues(key, null);
				}else{
					//values.add(value);
					run.addValues(key, value);
				}
			}
			if(i<size-1){
				builder.append(",");
				param.append(",");
			}
		}
		param.append(")");
		builder.append(param);
		//run.addValues(values);
		run.setBuilder(builder);
		run.setInsertColumns(insertColumns);

		return run;
	}
	private RunSQL createInsertTxtFromCollection(String dest, Collection list, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl(this,dest);
		StringBuilder builder = new StringBuilder();
		if(null == list || list.size() ==0){
			throw new SQLException("空数据");
		}
		Object first = null;
		if(list instanceof DataSet){
			DataSet set = (DataSet)list;
			first = set.getRow(0);
			if(BasicUtil.isEmpty(dest)){
				dest = DataSourceHolder.parseDataSource(dest,set);
			}
			if(BasicUtil.isEmpty(dest)){
				dest = DataSourceHolder.parseDataSource(dest,first);
			}
		}else{
			first = list.iterator().next();
			if(AdapterProxy.hasAdapter()){
				dest = AdapterProxy.table(first.getClass());
			}
		}
		if(BasicUtil.isEmpty(dest)){
			throw new SQLException("未指定表");
		}
		/*确定需要插入的列*/
		List<String> keys = confirmInsertColumns(dest, first, columns);
		if(null == keys || keys.size() == 0){
			throw new SQLException("未指定列(DataRow或Entity中没有需要更新的属性值)["+first.getClass().getName()+":"+BeanUtil.object2json(first)+"]");
		}
		createInsertsTxt(builder, dest, list, keys);
		run.setBuilder(builder);

		return run;
	}

	@Override
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set,  List<String> keys){
		builder.append("INSERT INTO ").append(parseTable(dest));
		builder.append("(");

		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(i<keySize-1){
				builder.append(",");
			}
		}
		builder.append(") VALUES ");
		int dataSize = set.size();
		for(int i=0; i<dataSize; i++){
			DataRow row = set.getRow(i);
			if(null == row){
				continue;
			}
			if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
				String pk = row.getPrimaryKey();
				if(null == pk){
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
				}
				row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
			}
			insertValue(builder, row, keys);
			if(i<dataSize-1){
				builder.append(",");
			}
		}
	}

	//@Override
	public void createInsertsTxt(StringBuilder builder, String dest, Collection list,  List<String> keys){
		builder.append("INSERT INTO ").append(parseTable(dest));
		builder.append("(");

		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(i<keySize-1){
				builder.append(",");
			}
		}
		builder.append(") VALUES ");
		int dataSize = list.size();
		int idx = 0;
		for(Object obj:list){
			if(obj instanceof DataRow) {
				DataRow row = (DataRow)obj;
				if (row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())) {
					String pk = row.getPrimaryKey();
					if (null == pk) {
						pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
					}
					row.put(pk, primaryCreater.createPrimary(type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
				}
				insertValue(builder, row, keys);
			}else{
				String pk = null;
				Object pv = null;
				if(AdapterProxy.hasAdapter()){
					pk = AdapterProxy.primaryKey(obj.getClass());
					pv = AdapterProxy.primaryValue(obj);
					AdapterProxy.createPrimaryValue(obj);
				}else{
					pk = DataRow.DEFAULT_PRIMARY_KEY;
					pv = BeanUtil.getFieldValue(obj, pk);
					if(null != primaryCreater && null == pv){
						pv = primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null);
						BeanUtil.setFieldValue(obj, pk, pv);
					}
				}
				insertValue(builder, obj, keys);
			}
			if(idx<dataSize-1){
				builder.append(",");
			}
			idx ++;
		}
	}

	/**
	 * 生成insert sql的value部分
	 * @param builder builder
	 * @param obj obj
	 * @param keys keys
	 */
	protected void insertValue(StringBuilder builder, Object obj, List<String> keys){
		int keySize = keys.size();
		builder.append("(");
		for(int j=0; j<keySize; j++){
			value(builder, obj, keys.get(j));
			if(j<keySize-1){
				builder.append(",");
			}
		}
		builder.append(")");
	}
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
	private void format(StringBuilder builder, Object value){
		if(null == value || "NULL".equals(value)){
			builder.append("null");
		}else if(value instanceof String){
			String str = value.toString();
			if(str.startsWith("${") && str.endsWith("}") && !BeanUtil.isJson(value)){
				str = str.substring(2, str.length()-1);
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
	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkParimary, String ... columns){ 
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

	private RunSQL createUpdateTxtFromObject(String dest, Object obj, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl(this,dest);
		StringBuilder builder = new StringBuilder();
		//List<Object> values = new ArrayList<Object>();
		List<String> keys = null;
		List<String> primaryKeys = null;
		if(null != columns && columns.length >0 ){
			keys = BeanUtil.array2list(columns);
		}else{
			if(AdapterProxy.hasAdapter()){
				keys = AdapterProxy.columns(obj.getClass());
			}
		}
		if(AdapterProxy.hasAdapter()){
			primaryKeys = AdapterProxy.primaryKeys(obj.getClass());
		}else{
			primaryKeys = new ArrayList<>();
			primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
		}
		//不更新主键
		keys.removeAll(primaryKeys);

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/
		int size = keys.size();
		if(size > 0){
			builder.append("UPDATE ").append(parseTable(dest));
			builder.append(" SET").append(SQLCreater.BR_TAB);
			for(int i=0; i<size; i++){
				String key = keys.get(i);
				Object value = null;
				if(AdapterProxy.hasAdapter()){
					Field field = AdapterProxy.field(obj.getClass(), key);
					value = BeanUtil.getFieldValue(obj, field);
				}else {
					value = BeanUtil.getFieldValue(obj, key);
				}
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") && !BeanUtil.isJson(value)){
					String str = value.toString();
					value = str.substring(2, str.length()-1);

					SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(SQLCreater.BR_TAB);
				}else{
					SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(SQLCreater.BR_TAB);
					if("NULL".equals(value)){
						value = null;
					}
					updateColumns.add(key);
					//values.add(value);
					run.addValues(key, value);
				}
				if(i<size-1){
					builder.append(",");
				}
			}
			builder.append(SQLCreater.BR);
			builder.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
			for(String pk:primaryKeys){
				builder.append(" AND ");
				SQLUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
				updateColumns.add(pk);
				if(AdapterProxy.hasAdapter()){
					Field field = AdapterProxy.field(obj.getClass(), pk);
					//values.add(BeanUtil.getFieldValue(obj, field));
					run.addValues(pk, BeanUtil.getFieldValue(obj, field));
				}else {
					//values.add(BeanUtil.getFieldValue(obj, pk));
					run.addValues(pk, BeanUtil.getFieldValue(obj, pk));
				}
			}
			//run.addValues(values);
		}
		run.setUpdateColumns(updateColumns);
		run.setBuilder(builder);

		return run;
	}
	private RunSQL createUpdateTxtFromDataRow(String dest, DataRow row, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl(this,dest);
		StringBuilder builder = new StringBuilder();
		//List<Object> values = new ArrayList<Object>();
		/*确定需要更新的列*/ 
		List<String> keys = confirmUpdateColumns(dest, row, columns);
		List<String> primaryKeys = row.getPrimaryKeys();
		if(primaryKeys.size() == 0){
			throw new SQLUpdateException("[更新更新异常][更新条件为空,update方法不支持更新整表操作]");
		}

		//不更新主键
		keys.removeAll(primaryKeys);

		List<String> updateColumns = new ArrayList<>();
		/*构造SQL*/
		int size = keys.size();
		if(size > 0){
			builder.append("UPDATE ").append(parseTable(dest));
			builder.append(" SET").append(SQLCreater.BR_TAB);
			for(int i=0; i<size; i++){
				String key = keys.get(i);
				Object value = row.get(key);
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") && !BeanUtil.isJson(value)){
					String str = value.toString();
					value = str.substring(2, str.length()-1);
					SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(SQLCreater.BR_TAB);
				}else{
					SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(SQLCreater.BR_TAB);
					if("NULL".equals(value)){
						value = null;
					}
					updateColumns.add(key);
					//values.add(value);
					run.addValues(key, value);
				} 
				if(i<size-1){
					builder.append(",");
				} 
			}
			builder.append(SQLCreater.BR);
			builder.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
			for(String pk:primaryKeys){
				builder.append(" AND ");
				SQLUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
				updateColumns.add(pk);
				//values.add(row.get(pk));
				run.addValues(pk, row.get(pk));
			}
			//run.addValues(values);
		}
		run.setUpdateColumns(updateColumns);
		run.setBuilder(builder);

		return run; 
	}

	/**
	 * 过滤掉表结构中不存在的列
	 * @param table table
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
	 * 确认需要插入的列 
	 * @param obj  obj
	 * @param columns  columns
	 * @return List
	 */ 
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
				isInsertNullColumn = row.isUpdateNullColumn();
				isInsertEmptyColumn = row.isUpdateEmptyColumn();

			}else{
				isInsertNullColumn = ConfigTable.getBoolean("IS_INSERT_NULL_COLUMN",false);
				isInsertEmptyColumn = ConfigTable.getBoolean("IS_INSERT_EMPTY_COLUMN",false);
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
	 * 确认需要更新的列 
	 * @param dest  dest
	 * @param row  row
	 * @param columns  columns
	 * @return List
	 */ 
	private List<String> confirmUpdateColumns(String dest, DataRow row, String ... columns){
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
			LinkedHashMap<String, Column> columns = service.metadata().columns(catalog, schema, table, true);
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
	 * @param column column
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
			}
			// 根据Java类
			// 不要解析String 许多不识别的类型会对应String 交给子类解析
			// 不要解析Boolean类型 有可能是 0,1
			else if(clazz.contains("Integer")){
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
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
	/* ************** 拼接字符串 *************** */
	protected String concatFun(String ... args){
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

	protected String concatOr(String ... args){
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
	protected String concatAdd(String ... args){
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


	public String buildDropRunSQL(Table table){
		table.setCreater(this);
		StringBuilder builder = new StringBuilder();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		builder.append("DROP TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, table.getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}
	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column column
	 * @return String
	 */
	public String buildDropRunSQL(Column column){
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		String catalog = column.getCatalog();
		String schema = column.getSchema();
		builder.append("ALTER TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, column.getTable(), getDelimiterFr(), getDelimiterTo());
		builder.append(" DROP COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}

	/**
	 * 修改列 ALTER TABLE   HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column
	 * @return
	 */
	@Override
	public String buildAlterRunSQL(Column column){
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		String catalog = column.getCatalog();
		String schema = column.getSchema();
		builder.append("ALTER TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, column.getTable(), getDelimiterFr(), getDelimiterTo());
		Column update = column.getUpdate();
		if(null != update){
			builder.append(" CHANGE ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			SQLUtil.delimiter(builder, update.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			define(builder, update);
		}
		return builder.toString();
	}
	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column){
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		String catalog = column.getCatalog();
		String schema = column.getSchema();
		builder.append("ALTER TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, column.getTable(), getDelimiterFr(), getDelimiterTo());
		Column update = column.getUpdate();
		if(null == update){
			//添加列
			builder.append(" ADD COLUMN ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			define(builder, column);
		}
		return builder.toString();
	}

	@Override
	public String buildAlterRunSQL(Table table){
		return null;
	}
	@Override
	public String buildCreateRunSQL(Table table){
		table.setCreater(this);
		StringBuilder builder = new StringBuilder();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		builder.append("CREATE TABLE ");
		if(BasicUtil.isNotEmpty(catalog)){
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		if(BasicUtil.isNotEmpty(schema)){
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, table.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append("(");
		Collection<Column> columns = table.getColumns().values();
		int idx = 0;
		for(Column column:columns){
			if(idx > 0){
				builder.append(",");
			}
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			define(builder, column);
			idx ++;
		}
		primary(builder, table);
		builder.append(")");

		return builder.toString();
	}

	/**
	 * 主键
	 * @param builder builder
	 * @param table table
	 */
	@Override
	public void primary(StringBuilder builder, Table table){
		List<Column> pks = table.getPrimaryKeys();
		if(pks.size()>0){
			builder.append(",PRIMARY KEY (");
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
			}
			builder.append(")");
		}
	}

	/**
	 * 定义列
	 * @param builder builder
	 * @param column column
	 */
	@Override
	public void define(StringBuilder builder, Column column){
		//数据类型
		type(builder, column);
		// 编码
		charset(builder, column);
		//默认值
		defaultValue(builder, column);
		//非空
		if (!column.isNullable()) {
			builder.append(" NOT NULL");
		}
		//自增长列
		increment(builder, column);
		//更新行事件
		onupdate(builder, column);
		//备注
		comment(builder, column);
		//位置
		position(builder, column);
	}

	/**
	 * 数据类型
	 * @param builder builder
	 * @param column column
	 */
	public void type(StringBuilder builder, Column column){

		builder.append(column.getTypeName());
		//精度
		int precision = column.getPrecision();
		Integer scale = column.getScale();
		if(precision > 0){
			builder.append("(").append(precision);
			if(null != scale){
				builder.append(",").append(scale);
			}
			builder.append(")");
		}else if(precision == -1){
			builder.append("(max)");
		}
	}
	/**
	 * 编码
	 * @param builder builder
	 * @param column column
	 */
	public void charset(StringBuilder builder, Column column){
		// CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
		String charset = column.getCharset();
		if(BasicUtil.isNotEmpty(charset)){
			builder.append(" CHARACTER SET ").append(charset);
			String collate = column.getCollate();
			if(BasicUtil.isNotEmpty(collate)){
				builder.append(" COLLATE ").append(collate);
			}
		}
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column column
	 */
	public void defaultValue(StringBuilder builder, Column column){
		Object def = column.getDefaultValue();
		builder.append(" default ");
		boolean isCharColumn = isCharColumn(column);
		if(isCharColumn){
			builder.append("'");
		}
		builder.append(def);
		if(isCharColumn){
			builder.append("'");
		}
	}
	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column column
	 */
	public void onupdate(StringBuilder builder, Column column){
		if(column.isOnUpdate()){
			builder.append(" ON UPDATE CURRENT_TIMESTAMP");
		}
	}
	/**
	 * 自增长列
	 * @param builder builder
	 * @param column column
	 */
	public void increment(StringBuilder builder, Column column){
		if(column.isAutoIncrement()){
			builder.append(" AUTO_INCREMENT");
		}
	}

	/**
	 * 位置
	 * @param builder builder
	 * @param column column
	 */
	public void position(StringBuilder builder, Column column){
		Integer position = column.getPosition();
		if(null != position && position == 0){
			builder.append(" FIRST");
		}else{
			String after = column.getAfter();
			if(BasicUtil.isNotEmpty(after)){
				builder.append(" AFTER").append(after);
			}
		}
	}

	/**
	 * 备注
	 * @param builder builder
	 * @param column column
	 */
	public void comment(StringBuilder builder, Column column){
		String comment = column.getComment();
		if(BasicUtil.isNotEmpty(comment)){
			builder.append(" COMMENT '").append(comment).append("'");
		}
	}

	/**
	 * 是否同数字
	 * @param column column
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
		}
		return false;
	}

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
	@Override
	public boolean isCharColumn(Column column) {
		return !isNumberColumn(column) && !isBooleanColumn(column);
	}
}
