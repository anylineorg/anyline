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


package org.anyline.jdbc.run.sql;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.OrderStoreImpl;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.prepare.*;
import org.anyline.jdbc.param.ConfigParser;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.param.ParseResult;
import org.anyline.jdbc.prepare.init.SimpleGroupStore;
import org.anyline.jdbc.run.Run;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoConditionChain;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoCondition;
import org.anyline.jdbc.run.RunValue;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class BasicRunSQL implements Run {
	protected static final Logger log = LoggerFactory.getLogger(BasicRunSQL.class);
	protected StringBuilder builder = new StringBuilder();
	protected RunPrepare prepare;
	protected String catalog;
	protected String schema;
	protected String table;
	protected List<String> keys;
	protected List<RunValue> values;
	protected PageNavi pageNavi;
	protected ConditionChain conditionChain;			//查询条件
	protected ConfigStore configStore; 
	protected OrderStore orderStore; 
	protected GroupStore groupStore;
	protected String having;
	protected List<Variable> variables;
	protected boolean strict = false;
	protected boolean valid = true;
	protected List<String> insertColumns;
	protected List<String> updateColumns;
	 
	 
	protected JDBCAdapter adapter;
	protected String delimiterFr;
	protected String delimiterTo;
	protected static AnylineService service;
	 
	public void setCreater(JDBCAdapter adapter){
		this.adapter = adapter; 
	}

	public void init(){ 
		this.delimiterFr = adapter.getDelimiterFr();
		this.delimiterTo = adapter.getDelimiterTo();
		 
 
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

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getDataSource() {
		String ds = table;
		if (BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(schema)) {
			ds = schema + "." + ds;
		}
		if (BasicUtil.isEmpty(ds)) {
			ds = schema;
		}
		return ds;
	}
	public Run group(String group){
		/*避免添加空条件*/ 
		if(BasicUtil.isEmpty(group)){ 
			return this; 
		} 
		 
		if(null == groupStore){ 
			groupStore = new SimpleGroupStore();
		} 
 
		group = group.trim().toUpperCase(); 
 
		 
		/*添加新分组条件*/ 
		if(!groupStore.getGroups().contains(group)){ 
			groupStore.group(group); 
		} 
		 
		return this; 
	} 
	public Run order(String order){
		if(null == orderStore){ 
			orderStore = new OrderStoreImpl(); 
		} 
		orderStore.order(order); 
		return this; 
	} 
	public RunPrepare getPrepare() {
		return prepare;
	} 
	public Run setPrepare(RunPrepare prepare) {
		this.prepare = prepare;
		this.table = prepare.getTable();
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
	 * @return Run
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Run addValues(String key, Object obj){
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
	 * @return Run
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Run addValues(RunValue run){
		if(null == values){
			values = new ArrayList<>();
		}
		adapter.convert(getCatalog(), getSchema(), getTable(), run);
		values.add(run);
		return this;
	}
	public Run addValues(List<RunValue> values){
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
	public JDBCAdapter getAdapter() {
		return adapter;
	} 
 
	@Override 
	public Run setConditionValue(boolean required, boolean strictRequired, String prefix, String variable, Object value, RunPrepare.COMPARE_TYPE compare) {
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
		String text = adapter.parseFinalQueryTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	} 
	@Override 
	public String getTotalQueryTxt() {
		String text = adapter.parseTotalQueryTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	}
	@Override
	public String getExistsTxt(){
		String text =  adapter.parseExistsTxt(this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		return text;
	}
	@Override 
	public String getBaseQueryTxt() { 
		return builder.toString();
	} 
	public Run addOrders(OrderStore orderStore){
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
	public Run addOrder(Order order){
		this.orderStore.order(order); 
		return this; 
	} 
 
 
	public Run setConditionChain(ConditionChain chain){
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
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare){
		if(this instanceof XMLRunSQL){
			((XMLRunSQL)this).addCondition(prefix, var, value);
		}else{
			Condition condition = new SimpleAutoCondition(required,strictRequired,prefix,var, value, compare);
			if(null == conditionChain){
				conditionChain = new SimpleAutoConditionChain();
			}
			if(condition.isActive()){
				conditionChain.addCondition(condition);
			}
		}
		return this;
	}
	public Run addCondition(boolean required, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare){
		return addCondition(required, false, prefix, var, value, compare);
	}
	/**
	 * 添加静态文本查询条件
	 */
	public Run addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}

		if(condition.startsWith("${") && condition.endsWith("}")){
			//原生SQL  不处理
			Condition con = new SimpleAutoCondition(condition.substring(2, condition.length()-1));
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
		Condition con = new SimpleAutoCondition(condition);
		conditionChain.addCondition(con);
		
		return this;
	}
	public Run addCondition(Condition condition) {
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
	public Run addConditions(String[] conditions) {
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
							groupStore = new SimpleGroupStore();
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
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	public String getInsertTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	public String getUpdateTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		return builder.toString();
	} 
	 
	public Run addVariable(Variable var){
		if(null == variables){ 
			variables = new ArrayList<Variable>();
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
			return  SQLUtil.placeholder(prepare.getText(), delimiterFr, delimiterTo);
		}
		return prepare.getText();
	}
	//需要查询的列 
	public String getFetchColumns(){
		String result = "*";
		if(null != prepare){
			List<String> cols = prepare.getFetchKeys();
			if(null != cols && cols.size()>0){
				result = null;
				for(String col:cols){
					if(null == result){

						result = SQLUtil.delimiter(col, adapter.getDelimiterFr() , adapter.getDelimiterTo());
					}else{
						result += "," + SQLUtil.delimiter(col, adapter.getDelimiterFr() , adapter.getDelimiterTo());
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
	public Run setInsertColumns(List<String> insertColumns) {
		this.insertColumns = insertColumns;
		return this;
	}

	@Override
	public List<String> getUpdateColumns() {
		return updateColumns;
	}

	@Override
	public Run setUpdateColumns(List<String> updateColumns) {
		this.updateColumns = updateColumns;
		return this;
	}

}
 
 
