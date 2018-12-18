/* 
 * Copyright 2006-2015 www.anyline.org
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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.db.run.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.ConfigParser;
import org.anyline.config.ParseResult;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.db.run.RunSQL;
import org.anyline.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.config.db.sql.xml.impl.XMLConditionImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;


public abstract class BasicRunSQLImpl implements RunSQL {
	protected static final Logger log = Logger.getLogger(BasicRunSQLImpl.class);
	protected SQL sql;
	protected List<Object> values;
	protected PageNavi pageNavi;
	protected StringBuilder builder = new StringBuilder();
	protected ConditionChain conditionChain;			//查询条件
	protected ConfigStore configStore;
	protected OrderStore orderStore;
	protected GroupStore groupStore;
	protected String having;
	protected List<SQLVariable> variables;
	protected boolean strict = false;
	protected boolean valid = true;
	
	
	protected SQLCreater creater;
	protected String disKeyFr;
	protected String disKeyTo;
	
	public void setCreater(SQLCreater creater){
		this.creater = creater;
	}
	public void init(){
		this.disKeyFr = creater.getDisKeyFr();
		this.disKeyTo = creater.getDisKeyTo();
		

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
		return this;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>();
		}
		values.add(value);
	}
	public PageNavi getPageNavi() {
		return pageNavi;
	}
	public void setPageNavi(PageNavi pageNavi) {
		this.pageNavi = pageNavi;
	}
	public StringBuilder getBuilder() {
		return builder;
	}
	public void setBuilder(StringBuilder builder) {
		this.builder = builder;
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
	public String getDisKeyFr() {
		return disKeyFr;
	}
	public void setDisKeyFr(String disKeyFr) {
		this.disKeyFr = disKeyFr;
	}
	public String getDisKeyTo() {
		return disKeyTo;
	}
	public void setDisKeyTo(String disKeyTo) {
		this.disKeyTo = disKeyTo;
	}
	public SQLCreater getCreater() {
		return creater;
	}

	@Override
	public RunSQL setConditionValue(boolean required, boolean strictRequired,  String condition, String variable, Object value, SQL.COMPARE_TYPE compare) {
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
		return creater.parseFinalQueryTxt(this);
	}
	@Override
	public String getTotalQueryTxt() {
		return creater.parseTotalQueryTxt(this);
	}
	@Override
	public String getExistsTxt(){
		return creater.parseExistsTxt(this);
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
	 * @param	required
	 * 			是否必须
	 * @param	column
	 * 			列名
	 * @param	value
	 * 			值
	 * @param	compare
	 * 			比较方式
	 */
	public RunSQL addCondition(boolean requried, boolean strictRequired, String column, Object value, COMPARE_TYPE compare){
		if(this instanceof XMLRunSQLImpl){
			((XMLRunSQLImpl)this).addCondition(column, column, value);
		}else{
			Condition condition = new AutoConditionImpl(requried,strictRequired,column, value, compare);
			if(null == conditionChain){
				conditionChain = new AutoConditionChainImpl();
			}
			if(condition.isActive()){
				conditionChain.addCondition(condition);
			}
		}
		return this;
	}
	public RunSQL addCondition(boolean requried, String column, Object value, COMPARE_TYPE compare){
		return addCondition(requried, false, column, value, compare);
	}
	/**
	 * 添加静态文本查询条件
	 */
	public RunSQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}

		if(condition.startsWith("{") && condition.endsWith("}")){
			//原生SQL  不处理
			Condition con = new AutoConditionImpl(condition.substring(1, condition.length()-1));
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
				addCondition(parser.isRequired(), parser.isStrictRequired(), parser.getId(),value,parser.getCompare());
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
				this.addCondition(condition);
			}
		}
		return this;
	}
	
	public String getDeleteTxt(){
		return this.builder.toString();
	}
	public String getInsertTxt(){
		return this.builder.toString();
	}
	public String getUpdateTxt(){
		return this.builder.toString();
	}
	
	public RunSQL addVariable(SQLVariable var){
		if(null == variables){
			variables = new ArrayList<SQLVariable>();
		}
		variables.add(var);
		return this;
	}
	
	protected boolean hasWhere(String txt){
		boolean where = false;
		txt = txt.toUpperCase();
		int fr = txt.lastIndexOf("WHERE");
		if(fr > 0){
			txt = txt.substring(fr);
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
					where = true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return where;
	}
	@Override
	public String getExecuteTxt(){
		return builder.toString();
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
						result = creater.getDisKeyFr() + col + creater.getDisKeyTo();
					}else{
						result += "," + creater.getDisKeyFr() + col + creater.getDisKeyTo();
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
		return valid;
	}
}


