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


package org.anyline.jdbc.run;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.OrderStoreImpl;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.adapter.JDBCAdapter;
import org.anyline.jdbc.param.ConfigParser;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.param.ParseResult;
import org.anyline.jdbc.prepare.*;
import org.anyline.jdbc.prepare.init.SimpleGroupStore;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoCondition;
import org.anyline.jdbc.prepare.sql.auto.init.SimpleAutoConditionChain;
import org.anyline.jdbc.run.sql.XMLRunSQL;
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


public abstract class BasicRun implements Run {
	protected static final Logger log = LoggerFactory.getLogger(BasicRun.class);
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
	 
	public void setAdapter(JDBCAdapter adapter){
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
	 * @param required 是否必须
	 * @param strictRequired 是否必须
	 * @param prefix 表名
	 * @param var 列名
	 * @param value 值
	 * @param compare 比较方式
	 */
	public Run addCondition(boolean required, boolean strictRequired, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare){
		Condition condition = new SimpleAutoCondition(required,strictRequired,prefix,var, value, compare);
		if(null == conditionChain){
			conditionChain = new SimpleAutoConditionChain();
		}
		if(condition.isActive()){
			conditionChain.addCondition(condition);
		}
		return this;
	}
	public Run addCondition(boolean required, String prefix, String var, Object value, RunPrepare.COMPARE_TYPE compare){
		return addCondition(required, false, prefix, var, value, compare);
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

	@Override
	public String getExecuteTxt(){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(prepare.getText(), delimiterFr, delimiterTo);
		}
		return prepare.getText();
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
 
 
