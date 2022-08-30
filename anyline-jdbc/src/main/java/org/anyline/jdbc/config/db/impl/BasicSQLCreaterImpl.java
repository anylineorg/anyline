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
import org.anyline.entity.adapter.KeyAdapter.KEY_CASE;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.run.impl.TableRunSQLImpl;
import org.anyline.jdbc.config.db.run.impl.TextRunSQLImpl;
import org.anyline.jdbc.config.db.run.impl.XMLRunSQLImpl;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
import org.anyline.jdbc.config.db.sql.auto.TextSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.jdbc.config.db.sql.xml.XMLSQL;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.exception.SQLException;
import org.anyline.jdbc.exception.SQLUpdateException;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


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
		/*if(!ConfigTable.IS_SQL_DELIMITER_OPEN){
			return "";
		}*/
		return this.delimiterFr;
	}
	@Override
	public String getDelimiterTo(){
		/*if(!ConfigTable.IS_SQL_DELIMITER_OPEN){
			return "";
		}*/
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
	public RunSQL createQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions){ 
		RunSQL run = null; 
		if(sql instanceof TableSQL){ 
			run = new TableRunSQLImpl(); 
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
	public RunSQL createExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions){
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
	public RunSQL createDeleteRunSQL(String table, String key, Object values){
		return createDeleteRunSQLFromTable(table, key, values);
	}
	@Override 
	public RunSQL createDeleteRunSQL(String dest, Object obj, String ... columns){ 
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
			run = new TableRunSQLImpl();
			run.setCreater(this);
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
		TableRunSQLImpl run = new TableRunSQLImpl();
		builder.append("DELETE FROM ").append(table).append(" WHERE ");
		if(values instanceof Collection){
			Collection cons = (Collection)values;
			BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(cons.size() > 1){
				builder.append(" IN(");
				int idx = 0;
				for(Object obj:cons){
					if(idx > 0){
						builder.append(",");
					}
					builder.append("'").append(obj).append("'");
					idx ++;
				}
				builder.append(")");
			}else if(cons.size() == 1){
				for(Object obj:cons){
					builder.append("=?");
					run.addValue(obj);
				}
			}else{
				throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
			}
		}else{

			BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			builder.append("=?");
			run.addValue(values);
		}
		run.setBuilder(builder);
		return run;
	}
	private RunSQL createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){
		TableRunSQLImpl run = new TableRunSQLImpl();
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

				BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ? ");
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
				run.addValue(value);
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
	public RunSQL createInsertTxt(String dest, Object obj, boolean checkParimary, String ... columns){ 
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
		RunSQL run = new TableRunSQLImpl();
		List<Object> values = new ArrayList<Object>();
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
			BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
			if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") && !BeanUtil.isJson(value)){
				String str = value.toString();
				value = str.substring(2, str.length()-1);
				if(value.toString().startsWith("${") && value.toString().endsWith("}")){
					//保存json时可以{json格式}最终会有两层:{{a:1}}8.5之前
					param.append("?");
					insertColumns.add(key);
					values.add(value);
				}else {
					param.append(value);
				}
			}else{
				param.append("?");
				insertColumns.add(key);
				if("NULL".equals(value)){
					values.add(null);
				}else{
					values.add(value);
				}
			}
			if(i<size-1){
				builder.append(",");
				param.append(",");
			}
		}
		param.append(")");
		builder.append(param);
		run.addValues(values);
		run.setBuilder(builder);
		run.setInsertColumns(insertColumns);
		return run;
	}
	private RunSQL createInsertTxtFromCollection(String dest, Collection list, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();

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
			BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
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
			BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
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
			format(builder, obj, keys.get(j));
			if(j<keySize-1){
				builder.append(",");
			}
		}
		builder.append(")");
	}
	public void format(StringBuilder builder, Object obj, String key){
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
		RunSQL run = new TableRunSQLImpl();
		StringBuilder builder = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
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

					BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(SQLCreater.BR_TAB);
				}else{
					BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(SQLCreater.BR_TAB);
					if("NULL".equals(value)){
						value = null;
					}
					updateColumns.add(key);
					values.add(value);
				}
				if(i<size-1){
					builder.append(",");
				}
			}
			builder.append(SQLCreater.BR);
			builder.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
			for(String pk:primaryKeys){
				builder.append(" AND ");
				BasicUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
				updateColumns.add(pk);
				if(AdapterProxy.hasAdapter()){
					Field field = AdapterProxy.field(obj.getClass(), pk);
					values.add(BeanUtil.getFieldValue(obj, field));
				}else {
					values.add(BeanUtil.getFieldValue(obj, pk));
				}
			}
			run.addValues(values);
		}
		run.setUpdateColumns(updateColumns);
		run.setBuilder(builder);
		return run;
	}
		private RunSQL createUpdateTxtFromDataRow(String dest, DataRow row, boolean checkParimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
		StringBuilder builder = new StringBuilder();
		List<Object> values = new ArrayList<Object>(); 
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
				Object value = row.get(KEY_CASE.SRC, key);
				if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") && !BeanUtil.isJson(value)){
					String str = value.toString();
					value = str.substring(2, str.length()-1);
					BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(SQLCreater.BR_TAB);
				}else{
					BasicUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(SQLCreater.BR_TAB);
					if("NULL".equals(value)){
						value = null;
					}
					updateColumns.add(key);
					values.add(value);
				} 
				if(i<size-1){
					builder.append(",");
				} 
			}
			builder.append(SQLCreater.BR);
			builder.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
			for(String pk:primaryKeys){
				builder.append(" AND ");
				BasicUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
				updateColumns.add(pk);
				values.add(row.get(pk)); 
			}
			run.addValues(values);
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
		List<String> metadatas = service.metadata(table);
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
			DataRow row = null;
			if(obj instanceof DataRow){
				row = (DataRow)obj;
				keys = row.keys();
			}else{
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
			//是否插入null及""列
			boolean isInsertNullColumn = ConfigTable.getBoolean("IS_INSERT_NULL_COLUMN",false); 
			boolean isInsertEmptyColumn = ConfigTable.getBoolean("IS_INSERT_EMPTY_COLUMN",false); 
			int size = keys.size(); 
			for(int i=size-1;i>=0; i--){ 
				String key = keys.get(i); 
				if(mastKeys.contains(key)){ 
					//必须插入 
					continue; 
				} 
				if(ignores.contains(key)){
					keys.remove(key); 
					continue; 
				}
				Object value = null;
				if(null != row) {
					value = row.get(KEY_CASE.SRC, key);
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
		List<String> mastKeys = row.getUpdateColumns();		//必须更新列 
		List<String> delimiters = new ArrayList<>();			//必须不更新列
		List<String> factKeys = new ArrayList<>();		//根据是否空值
		if(null != columns && columns.length>0){ 
			each = false; 
			keys = new ArrayList<>();
			for(String column:columns){ 
				if(BasicUtil.isEmpty(column)){ 
					continue; 
				} 
				if(column.startsWith("+")){ 
					column = column.substring(1, column.length()); 
					mastKeys.add(column); 
					each = true; 
				}else if(column.startsWith("-")){
					column = column.substring(1, column.length()); 
					delimiters.add(column);
					each = true; 
				}else if(column.startsWith("?")){
					column = column.substring(1, column.length()); 
					factKeys.add(column); 
					each = true; 
				} 
				keys.add(column); 
			} 
		}else if(null != mastKeys && mastKeys.size()>0){
			each = false;
			keys = mastKeys;
		}
		if(each){ 
			keys = row.keys();
			for(String k:mastKeys){
				if(!keys.contains(k)){
					keys.add(k);
				}
			}
			//是否更新null及""列 
			boolean isUpdateNullColumn = row.isUpdateNullColumn();//ConfigTable.getBoolean("IS_UPDATE_NULL_COLUMN",true); 
			boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();//ConfigTable.getBoolean("IS_UPDATE_EMPTY_COLUMN",true); 
			int size = keys.size(); 
			for(int i=size-1;i>=0; i--){ 
				String key = keys.get(i); 
				if(mastKeys.contains(key)){ 
					//必须更新 
					continue; 
				} 
				if(delimiters.contains(key)){
					keys.remove(key); 
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
		List<String> ignores = row.getIgnoreUpdateColumns();
		for(String key:ignores){
			keys.remove(key);
		}
		keys = checkMetadata(dest, keys);
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
			table = BasicUtil.delimiter(tmps[0],getDelimiterFr() , getDelimiterTo())
					+ "."
					+ BasicUtil.delimiter(tmps[1],getDelimiterFr() , getDelimiterTo());
		}else{
			table = BasicUtil.delimiter(table,getDelimiterFr() , getDelimiterTo());
		}
		return table;
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

} 
