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


package org.anyline.jdbc.config.db.run.impl;

import org.anyline.entity.*;
import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.*;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.impl.GroupStoreImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.SpringContextUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;


public abstract class BasicRunSQLImpl implements RunSQL { 
	protected static final Logger log = LoggerFactory.getLogger(BasicRunSQLImpl.class);
	protected StringBuilder builder = new StringBuilder();
	protected SQL sql;
	protected String author;
	protected String table;
	protected List<String> keys;
	protected List<RunValue> values;
	protected PageNavi pageNavi;
	protected ConditionChain conditionChain;			//查询条件 
	protected ConfigStore configStore; 
	protected OrderStore orderStore; 
	protected GroupStore groupStore; 
	protected String having;
	protected List<SQLVariable> variables;
	protected boolean strict = false;
	protected boolean valid = true;
	protected List<String> insertColumns;
	protected List<String> updateColumns;
	 
	 
	protected SQLCreater creater; 
	protected String delimiterFr;
	protected String delimiterTo;
	protected static AnylineService service;
	 
	public void setCreater(SQLCreater creater){ 
		this.creater = creater; 
	}

	public void init(){ 
		this.delimiterFr = creater.getDelimiterFr();
		this.delimiterTo = creater.getDelimiterTo();
		 
 
		if(null != configStore){ 
			setPageNavi(configStore.getPageNavi()); 
			OrderStore orderStore = configStore.getOrders(); 
			List<Order> orders = null; 
			if(null != orderStore){ 
				orders = orderStore.getOrders(); 
			} 
			if(null != orders){ 
				for(Order order:orders){ 
					orderStore.order(order); 
				} 
			} 
		} 
		 
	}
	public String getTable(){
		return table;
	}
	public String getAuthor(){
		return author;
	}
	public String getDataSource() {
		String ds = table;
		if (BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(author)) {
			ds = author + "." + ds;
		}
		if (BasicUtil.isEmpty(ds)) {
			ds = author;
		}
		return ds;
	}
	public RunSQL group(String group){ 
		/*避免添加空条件*/ 
		if(BasicUtil.isEmpty(group)){ 
			return this; 
		} 
		 
		if(null == groupStore){ 
			groupStore = new GroupStoreImpl(); 
		} 
 
		group = group.trim().toUpperCase(); 
 
		 
		/*添加新分组条件*/ 
		if(!groupStore.getGroups().contains(group)){ 
			groupStore.group(group); 
		} 
		 
		return this; 
	} 
	public RunSQL order(String order){ 
		if(null == orderStore){ 
			orderStore = new OrderStoreImpl(); 
		} 
		orderStore.order(order); 
		return this; 
	} 
	public SQL getSql() { 
		return sql; 
	} 
	public RunSQL setSql(SQL sql) { 
		this.sql = sql;
		this.table = sql.getTable();
		return this; 
	}
	public List<RunValue> getRunValues() {
		return values;
	}
	public List<Object> getValues() {
		List<Object> list = new ArrayList<>();
		if(null != values){
			for(RunValue value:values){
				list.add(value.getValue());
			}
		}
		return list;
	}
	public void setValues(String key, List<Object> values) {
		for(Object value:values){
			values.add(new RunValue(key, value));
		}
	}

	/**
	 * 添加参数值
	 * @param obj  obj
	 * @return RunSQL
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RunSQL addValues(String key, Object obj){
		if(null == key){
			key = "none";
		}
		if(null != obj && obj instanceof Collection){
			Collection list = (Collection)obj;
			for(Object item:list){
				addValues(new RunValue(key, item));
			}

		}else{
			addValues(new RunValue(key, obj));
		}
		return this;
	}


	/**
	 * 添加参数值
	 * @param run  run
	 * @return RunSQL
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RunSQL addValues(RunValue run){
		if(null == values){
			values = new ArrayList<>();
		}
		creater.convert(getTable(), run);
		values.add(run);
		return this;
	}
	public RunSQL addValues(List<RunValue> values){
		for(RunValue value:values){
			addValues(value);
		}
		return this;
	}
	public PageNavi getPageNavi() { 
		return pageNavi; 
	} 
	public void setPageNavi(PageNavi pageNavi) { 
		this.pageNavi = pageNavi; 
	}
	public ConfigStore getConfigStore() { 
		return configStore; 
	} 
	public void setConfigStore(ConfigStore configStore) { 
		this.configStore = configStore; 
	} 
	 
	public OrderStore getOrderStore() { 
		return orderStore; 
	} 
	public void setOrderStore(OrderStore orderStore) { 
		this.orderStore = orderStore; 
	} 
	public GroupStore getGroupStore() { 
		return groupStore; 
	} 
	public void setGroupStore(GroupStore groupStore) { 
		this.groupStore = groupStore; 
	} 
	public String getDelimiterFr() {
		return delimiterFr;
	} 
	public void setDelimiterFr(String delimiterFr) {
		this.delimiterFr = delimiterFr;
	} 
	public String getDelimiterTo() {
		return delimiterTo;
	} 
	public void setDelimiterTo(String delimiterTo) {
		this.delimiterTo = delimiterTo;
	} 
	public SQLCreater getCreater() { 
		return creater; 
	} 
 
	@Override 
	public RunSQL setConditionValue(boolean required, boolean strictRequired,  String prefix, String variable, Object value, COMPARE_TYPE compare) {
		return this; 
	} 
	@Override 
	public void setOrders(String[] orders) { 
		if(null != orders){ 
			for(String order:orders){ 
				order(order); 
			} 
		} 
	} 
	@Override 
	public String getFinalQueryTxt() { 
		String text = creater.parseFinalQueryTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = BasicUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	} 
	@Override 
	public String getTotalQueryTxt() {
		String text = creater.parseTotalQueryTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = BasicUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	}
	@Override
	public String getExistsTxt(){
		String text =  creater.parseExistsTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = BasicUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	}
	@Override 
	public String getBaseQueryTxt() { 
		return builder.toString();
	} 
	public RunSQL addOrders(OrderStore orderStore){ 
		if(null == orderStore){ 
			return this; 
		} 
		List<Order> orders = orderStore.getOrders(); 
		if(null == orders){ 
			return this; 
		} 
		for(Order order:orders){ 
			this.orderStore.order(order); 
		} 
		return this; 
	} 
	public RunSQL addOrder(Order order){ 
		this.orderStore.order(order); 
		return this; 
	} 
 
 
	public RunSQL setConditionChain(ConditionChain chain){ 
		this.conditionChain = chain; 
		return this; 
	} 
	public ConditionChain getConditionChain() { 
		return this.conditionChain; 
	} 

	/*******************************************************************************************
	 * 
	 * 										添加条件
	 * 
	 ********************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	required 是否必须
	 * @param	strictRequired 是否必须
	 * @param	prefix 表名
	 * @param	var 列名
	 * @param	value 值
	 * @param	compare 比较方式
	 */
	public RunSQL addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, COMPARE_TYPE compare){
		if(this instanceof XMLRunSQLImpl){
			((XMLRunSQLImpl)this).addCondition(prefix, var, value);
		}else{
			Condition condition = new AutoConditionImpl(required,strictRequired,prefix,var, value, compare);
			if(null == conditionChain){
				conditionChain = new AutoConditionChainImpl();
			}
			if(condition.isActive()){
				conditionChain.addCondition(condition);
			}
		}
		return this;
	}
	public RunSQL addCondition(boolean required, String prefix, String var, Object value, COMPARE_TYPE compare){
		return addCondition(required, false, prefix, var, value, compare);
	}
	/**
	 * 添加静态文本查询条件
	 */
	public RunSQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}

		if(condition.startsWith("${") && condition.endsWith("}")){
			//原生SQL  不处理
			Condition con = new AutoConditionImpl(condition.substring(2, condition.length()-1));
			conditionChain.addCondition(con);
			return this;
		}
		if(condition.contains(":")){
			//:符号是否表示时间
			boolean isTime = false;
			int idx = condition.indexOf(":");
			//''之内
			if(condition.indexOf("'")<idx && condition.indexOf("'", idx+1) > 0){
				isTime = true;
			}
			if(!isTime){			
				//需要解析的SQL
				ParseResult parser = ConfigParser.parse(condition,false);
				Object value = ConfigParser.getValues(parser);
				addCondition(parser.isRequired(), parser.isStrictRequired(), parser.getPrefix(),parser.getVar(),value,parser.getCompare());
				return this;
			}
		}
		Condition con = new AutoConditionImpl(condition);
		conditionChain.addCondition(con);
		
		return this;
	}
	public RunSQL addCondition(Condition condition) { 
		if(null != conditionChain){ 
			conditionChain.addCondition(condition); 
		} 
		return this; 
	} 
 
 
	public Condition getCondition(String name){ 
		for(Condition con:conditionChain.getConditions()){ 
			if(null != con && null != con.getId() && con.getId().equalsIgnoreCase(name)){ 
				return con; 
			} 
		} 
		return null; 
	} 
	public RunSQL addConditions(String[] conditions) { 
		/*添加查询条件*/ 
		if(null != conditions){ 
			for(String condition:conditions){
				if(null == condition){
					continue;
				}
				condition = condition.trim();
				String up = condition.toUpperCase().replaceAll("\\s+", " ").trim(); 
				if(up.startsWith("ORDER BY")){ 
					String orderStr = condition.substring(up.indexOf("ORDER BY") + "ORDER BY".length()).trim();
					String orders[] = orderStr.split(","); 
					for(String item:orders){ 
						order(item); 
						if(null != configStore){ 
							configStore.order(item); 
						} 
						if(null != this.orderStore){ 
							this.orderStore.order(item); 
						} 
					} 
					continue; 
				}else if(up.startsWith("GROUP BY")){
					String groupStr = condition.substring(up.indexOf("GROUP BY") + "GROUP BY".length()).trim();
					String groups[] = groupStr.split(",");
					for(String item:groups){
						if(null == groupStore){
							groupStore = new GroupStoreImpl();
						}
						groupStore.group(item);
					}
					continue;
				}else if(up.startsWith("HAVING")){
					String haveStr = condition.substring(up.indexOf("HAVING") + "HAVING".length()).trim();
					this.having = haveStr;
					continue;
				}
//				if(up.contains(" OR ") && !(condition.startsWith("(") && condition.endsWith(")"))){
//					condition = "(" + condition + ")";
//				}
				this.addCondition(condition); 
			} 
		} 
		return this; 
	}
	 
	public String getDeleteTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  BasicUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	public String getInsertTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  BasicUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	public String getUpdateTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  BasicUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	 
	public RunSQL addVariable(SQLVariable var){ 
		if(null == variables){ 
			variables = new ArrayList<SQLVariable>(); 
		} 
		variables.add(var); 
		return this; 
	} 

	protected static boolean endwithWhere(String txt){ 
		boolean result = false;
		txt = txt.toUpperCase(); 
		int fr = 0;
		while((fr = txt.indexOf("WHERE")) > 0){ 
			txt = txt.substring(fr+5); 
			if(txt.indexOf("UNION") > 0){
				continue;
			}
			try{ 
				int bSize = 0;//左括号数据
				if(txt.contains(")")){
					bSize = RegularUtil.fetch(txt, "\\)").size();
				} 
				int eSize = 0;//右括号数量
				if(txt.contains("(")){
					eSize = RegularUtil.fetch(txt, "\\(").size();
				} 
				if(bSize == eSize){ 
					result = true;
					break;
				} 
			}catch(Exception e){ 
				e.printStackTrace(); 
			} 
		} 
		return result; 
	}
	@Override
	public String getExecuteTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  BasicUtil.placeholder(sql.getText(), delimiterFr, delimiterTo);
		}
		return sql.getText();
	}
	//需要查询的列 
	public String getFetchColumns(){
		String result = "*";
		if(null != sql){
			List<String> cols = sql.getFetchKeys();
			if(null != cols && cols.size()>0){
				result = null;
				for(String col:cols){
					if(null == result){

						result = BasicUtil.delimiter(col, creater.getDelimiterFr() , creater.getDelimiterTo());
					}else{
						result += "," + BasicUtil.delimiter(col, creater.getDelimiterFr() , creater.getDelimiterTo());
					}
				}
			}
		}
		return result;
	}


	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	public boolean isValid(){
		return this.valid;
	}

	public void createRunDeleteTxt(){

	}
	public void createRunQueryTxt(){

	}
	public void setBuilder(StringBuilder builder){
		this.builder = builder;
	}
	public StringBuilder getBuilder(){
		return this.builder;
	}

	@Override
	public List<String> getInsertColumns() {
		return insertColumns;
	}

	@Override
	public RunSQL setInsertColumns(List<String> insertColumns) {
		this.insertColumns = insertColumns;
		return this;
	}

	@Override
	public List<String> getUpdateColumns() {
		return updateColumns;
	}

	@Override
	public RunSQL setUpdateColumns(List<String> updateColumns) {
		this.updateColumns = updateColumns;
		return this;
	}

}
 
 
