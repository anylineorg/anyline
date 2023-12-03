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


package org.anyline.data.run;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.*;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.ColumnType;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public abstract class BasicRun implements Run {
	protected static final Logger log = LoggerFactory.getLogger(BasicRun.class);
	protected StringBuilder builder = new StringBuilder();
	protected int batch;
	protected int vol;//每行多少个值
	protected RunPrepare prepare;
	protected String catalog;
	protected String schema;
	protected String table;
	protected List<String> keys;
	protected List<RunValue> values;
	protected List<RunValue> batchValues;
	protected PageNavi pageNavi;
	protected ConditionChain conditionChain;			// 查询条件
	protected ConfigStore configStore;
	protected OrderStore orderStore; 
	protected GroupStore groupStore;
	protected String having;
	protected List<Variable> variables;

	protected Object filter;
	protected Object update;
	protected Object value;

	protected EMPTY_VALUE_SWITCH swt = EMPTY_VALUE_SWITCH.IGNORE;
	protected boolean valid = true;
	protected LinkedHashMap<String, Column> insertColumns = null;
	protected LinkedHashMap<String, Column> updateColumns;
	protected List<String> queryColumns;	//查询列
	protected List<String> excludeColumn;  //不查询列
	protected int from = 1;
	protected boolean supportBr = true;

	protected DataRuntime runtime;
	protected String delimiterFr;
	protected String delimiterTo;

	protected String action;
	public DriverAdapter adapter() {
		if(null != runtime) {
			return runtime.getAdapter();
		}
		return null;
	}

	@Override
	public void setRuntime(DataRuntime runtime){
		this.runtime = runtime;
	}

	@Override
	public int getFrom() {
		return from;
	}

	@Override
	public void setFrom(int from) {
		this.from = from;
	}

	@Override
	public void init(){
		if(null != runtime) {
			this.delimiterFr = runtime.getAdapter().getDelimiterFr();
			this.delimiterTo = runtime.getAdapter().getDelimiterTo();
		}

		if(null != configStore){
			setPageNavi(configStore.getPageNavi()); 
			/*OrderStore orderStore = configStore.getOrders();
			List<Order> orders = null;
			if (null != orderStore) {
				orders = orderStore.getOrders();
			}
			if (null != orders) {
				for (Order order : orders) {
					orderStore.order(order);
				}
			}*/
		} 
		 
	}
	@Override
	public String getTable(){
		return table;
	}

	@Override
	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	@Override
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setTable(String table) {
		this.table = table;
	}

	@Override
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
	@Override
	public Run group(String group){
		/*避免添加空条件*/ 
		if(BasicUtil.isEmpty(group)){
			return this; 
		} 
		 
		if(null == groupStore){
			groupStore = new DefaultGroupStore();
		} 
 
		group = group.trim().toUpperCase(); 
 
		 
		/*添加新分组条件*/ 
		if(!groupStore.getGroups().contains(group)){
			groupStore.group(group); 
		} 
		 
		return this; 
	}
	@Override
	public Run order(String order){
		if(null == orderStore){
			orderStore = new DefaultOrderStore();
		} 
		orderStore.order(order); 
		return this; 
	}
	@Override
	public RunPrepare getPrepare() {
		return prepare;
	}
	@Override
	public Run setPrepare(RunPrepare prepare) {
		this.prepare = prepare;
		this.table = prepare.getTable();
		return this; 
	}
	@Override
	public List<RunValue> getRunValues() {
		if(null != batchValues){
			return batchValues;
		}
		return values;
	}
	@Override
	public List<Object> getValues() {
		List<Object> list = new ArrayList<>();
		if(null != batchValues){
			for(RunValue value:batchValues){
				list.add(value.getValue());
			}
		}else  if(null != values){
			for(RunValue value:values){
				list.add(value.getValue());
			}
		}
		return list;
	}
	@Override
	public void setValues(String key, List<Object> values) {
		if(null != values){
			if(null == this.values){
				this.values = new ArrayList<>();
			}
			for(Object value:values){
				this.values.add(new RunValue(key, value));
			}
		}
	}

	/**
	 * 添加参数值
	 * @param compare  compare
	 * @param obj  obj
	 * @param column  column
	 * @param split  遇到集合/数组类型是否拆分处理(DataRow 并且Column不是数组类型)
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	@Override
	public RunValue addValues(Compare compare, Column column, Object obj, boolean split){
		RunValue rv = null;
		if(null != obj){
			// from:1-DataRow 2-Entity
			if(split && (null == column || !column.isArray()) && getFrom() != 2){
				/**/
				boolean json = false;
				if(null != column){
					String type = column.getTypeName();
					if(null != type){
						if(type.toUpperCase().contains("JSON") || type.toUpperCase().contains("BSON")){
							json = true;
						}
					}
				}
				if(obj.getClass().isArray()) {
					if(obj instanceof Object[] && !json){
						Object[] list = (Object[]) obj;
						for(Object item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof double[] && !json){
						double[] list = (double[]) obj;
						for(double item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof long[] && !json){
						long[] list = (long[]) obj;
						for(long item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof int[] && !json){
						int[] list = (int[]) obj;
						for(int item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof float[] && !json){
						float[] list = (float[]) obj;
						for(float item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof short[] && !json){
						short[] list = (short[]) obj;
						for(short item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}else if(obj instanceof Object[] && !json){
						Object[] list = (Object[]) obj;
						for(Object item:list){
							rv = new RunValue(column, item);
							addValues(rv);
							if(Compare.EQUAL == compare){
								break;
							}
						}
					}
				}else if(obj instanceof Collection && !json){
					Collection list = (Collection)obj;
					for(Object item:list){
						rv = new RunValue(column, item);
						addValues(rv);
						if(Compare.EQUAL == compare){
							break;
						}
					}
				}else{
					rv = new RunValue(column, obj);
					addValues(rv);
				}
			}
			else{
				rv = new RunValue(column, obj);
				addValues(rv);
			}

		}else{
			rv = new RunValue(column, obj);
			addValues(rv);
		}
		return rv;
	}


	/**
	 * 添加参数值
	 * @param run  run
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	public Run addValues(RunValue run){
		if(null == values){
			values = new ArrayList<>();
		}
		values.add(run);
		return this;
	}
	public Run addValues(List<RunValue> values){
		for(RunValue value:values){
			addValues(value);
		}
		return this;
	}
	public Run setRunValues(List<RunValue> values){
		this.values = values;
		return this;
	}
	@Override
	public PageNavi getPageNavi() {
		return pageNavi; 
	}
	@Override
	public void setPageNavi(PageNavi pageNavi) {
		this.pageNavi = pageNavi; 
	}
	@Override
	public ConfigStore getConfigStore() {
		return configStore; 
	}
	@Override
	public void setConfigStore(ConfigStore configStore) {
		this.configStore = configStore; 
	}

	@Override
	public OrderStore getOrderStore() {
		return orderStore; 
	}
	@Override
	public void setOrderStore(OrderStore orderStore) {
		this.orderStore = orderStore; 
	}
	@Override
	public GroupStore getGroupStore() {
		return groupStore; 
	}
	@Override
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
	public DriverAdapter getAdapter() {
		return runtime.getAdapter();
	}
 
	@Override 
	public Run setConditionValue(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String variable, Object value) {
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
	public String getFinalQuery(boolean placeholder) {
		String text = runtime.getAdapter().mergeFinalQuery(runtime,this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		if(!placeholder){
			text = replace(text);
		}
		return text;
	} 
	@Override 
	public String getTotalQuery(boolean placeholder) {
		String text = runtime.getAdapter().mergeFinalTotal(runtime,this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}
	@Override
	public String getFinalExists(boolean placeholder){
		String text =  runtime.getAdapter().mergeFinalExists(runtime,this);
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}
	@Override 
	public String getBaseQuery(boolean placeholder) {
		String text = builder.toString();
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}
	@Override
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
	@Override
	public Run addOrder(Order order){
		this.orderStore.order(order); 
		return this; 
	}
	public Run addValue(RunValue value){
		if(null == values){
			values = new ArrayList<>();
		}
		values.add(value);
		return this;
	}

	@Override
	public Run setConditionChain(ConditionChain chain){
		this.conditionChain = chain; 
		return this; 
	}
	@Override
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
	 * @param swt 遇到空值处理方式
	 * @param prefix 表名
	 * @param var 列名
	 * @param value 值
	 * @param compare 比较方式
	 */
	@Override
	public Run addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value){
		Condition condition = new DefaultAutoCondition(swt, compare, prefix,var, value);
		if(null == conditionChain){
			conditionChain = new DefaultAutoConditionChain();
		}
		if(condition.isActive()){
			conditionChain.addCondition(condition);
		}else{
			if(swt == EMPTY_VALUE_SWITCH.BREAK) {
				conditionChain.setValid(false);
			}
		}
		return this;
	}
	@Override
	public Run addCondition(Condition condition) {
		if(null != conditionChain){
			conditionChain.addCondition(condition); 
		} 
		return this; 
	}


	@Override
	public Condition getCondition(String name){
		for(Condition con:conditionChain.getConditions()){
			if(null != con && null != con.getId() && con.getId().equalsIgnoreCase(name)){
				return con;
			}
		}
		return null;
	}

	@Override
	public List<Condition> getConditions(String name){
		List<Condition> list = new ArrayList<>();
		for(Condition con:conditionChain.getConditions()){
			if(null != con && null != con.getId() && con.getId().equalsIgnoreCase(name)){
				list.add(con);
			}
		}
		return list;
	}

	@Override
	public String getFinalDelete(boolean placeholder){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		String text = builder.toString();
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}
	@Override
	public String getFinalInsert(boolean placeholder){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		String text = builder.toString();
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}
	@Override
	public String getFinalUpdate(boolean placeholder){
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return  SQLUtil.placeholder(builder.toString(), delimiterFr, delimiterTo);
		}
		String text = builder.toString();
		if(!placeholder){
			text = replace(text);
		}
		return text;
	}

	public Run addVariable(Variable var){
		if(null == variables){
			variables = new ArrayList<Variable>();
		} 
		variables.add(var); 
		return this; 
	} 

	@Override
	public String getFinalExecute(boolean placeholder){
		String text = builder.toString();
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			text = SQLUtil.placeholder(text, delimiterFr, delimiterTo);
		}
		if(!placeholder){
			text = replace(text);
		}
		if(!supportBr()){
			text = text.replace("\r\n"," ").replace("\n"," ");
		}
		return text;
	}

	public boolean supportBr(){
		return supportBr;
	}
	public void supportBr(boolean support){
		this.supportBr = support;
	}


	@Override
	public EMPTY_VALUE_SWITCH getStrict() {
		return swt;
	}

	@Override
	public void setSwitch(EMPTY_VALUE_SWITCH swt) {
		this.swt = swt;
	}
	@Override
	public boolean isValid(){
		if(!valid){
			return false;
		}
		valid = checkValid();
		return valid;
	}

	@Override
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public void setBuilder(StringBuilder builder){
		this.builder = builder;
	}
	@Override
	public StringBuilder getBuilder(){
		return this.builder;
	}

	@Override
	public LinkedHashMap<String, Column> getInsertColumns(boolean metadata) {
		return insertColumns;
	}

	@Override
	public List<String> getInsertColumns() {
		List<String> keys = new ArrayList<>();
		if(null != insertColumns){
			for(Column column:insertColumns.values()){
				keys.add(column.getName());
			}
		}
		return keys;
	}

	@Override
	public Run setInsertColumns(List<String> columns) {
		if(null != columns){
			if(null == insertColumns){
				insertColumns = new LinkedHashMap<>();
			}
			for(String column:columns){
				insertColumns.put(column.toUpperCase(), new Column(column));
			}
		}
		return this;
	}
	@Override
	public Run setInsertColumns(LinkedHashMap<String, Column> columns) {
		this.insertColumns = columns;
		return this;
	}

	@Override
	public LinkedHashMap<String, Column> getUpdateColumns(boolean metadata) {
		return updateColumns;
	}

	@Override
	public List<String> getUpdateColumns() {
		List<String> keys = new ArrayList<>();
		if(null != updateColumns){
			for(Column column:updateColumns.values()){
				keys.add(column.getName());
			}
		}
		return keys;
	}

	@Override
	public Run setUpdateColumns(List<String> columns) {
		if(null != columns){
			if(null == updateColumns){
				updateColumns = new LinkedHashMap<>();
			}
			for(String column:columns){
				updateColumns.put(column.toUpperCase(), new Column(column));
			}
		}
		return this;
	}
	@Override
	public Run setUpdateColumns(LinkedHashMap<String, Column> columns) {
		this.updateColumns = columns;
		return this;
	}

	/**
	 * 添加条件
	 * @param conditions  简单过滤条件 ORDER GROUP 等
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run addCondition(String ... conditions) {
		/*添加查询条件*/
		if(null != conditions){
			for(String condition:conditions){
				if(null == condition){
					continue;
				}
				condition = condition.trim();
				String up = condition.toUpperCase().replaceAll("\\s+", " ").trim();

				if(up.startsWith("ORDER BY")){
					// 排序条件
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
					// 分组条件
					String groupStr = condition.substring(up.indexOf("GROUP BY") + "GROUP BY".length()).trim();
					String groups[] = groupStr.split(",");
					for(String item:groups){
						if(null == groupStore){
							groupStore = new DefaultGroupStore();
						}
						groupStore.group(item);
					}
					continue;
				}else if(up.startsWith("HAVING")){
					// 分组过滤
					String haveStr = condition.substring(up.indexOf("HAVING") + "HAVING".length()).trim();
					this.having = haveStr;
					continue;
				}
//				if(up.contains(" OR ") && !(condition.startsWith("(") && condition.endsWith(")"))){
//					condition = "(" + condition + ")";
//				}


				if(condition.startsWith("${") && condition.endsWith("}")){
					// 原生SQL  不处理
					Condition con = new DefaultAutoCondition(condition.substring(2, condition.length()-1));
					addCondition(con);
					continue;
				}

				if(condition.contains(":")){
					// :符号是否表示时间
					boolean isTime = false;
					int idx = condition.indexOf(":");
					// ''之内
					if(condition.indexOf("'")<idx && condition.indexOf("'", idx+1) > 0){
						isTime = true;
					}
					if(!isTime){
						// 需要解析的SQL
						ParseResult parser = ConfigParser.parse(condition,false);
						Object value = ConfigParser.getValues(parser);
						addCondition(parser.getSwitch(),parser.getCompare(), parser.getPrefix(),parser.getVar(),value);
						continue;
					}
				}
				//原生SQL
				Condition con = new DefaultAutoCondition(condition);
				addCondition(con);
			}
		}
		return this;
	}
	protected static boolean endWithWhere(String txt){
		/*boolean result = false;
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
		return result;*/
		txt = txt.replaceAll("\\s"," ")
				.replaceAll("'[\\S\\s]*?'","{}")
				.replaceAll("\\([^\\(\\)]+?\\)","{}")
				.replaceAll("\\([^\\(\\)]+?\\)","{}")
				.replaceAll("\\([^\\(\\)]+?\\)","{}")
				.toUpperCase();
		if(txt.contains("UNION")){
			boolean result = false;
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
		}else {
			return txt.contains("WHERE");
		}
	}


	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}



	public boolean isSetValue(String condition, String variable){
		Condition con = getCondition(condition);
		if(null == con){
			Variable var = con.getVariable(variable);
			if(null != var){
				return var.isSetValue();
			}
		}
		return false;
	}
	public boolean isSetValue(String variable){
		Variable var = getVariable(variable);
		if(null != var){
			return var.isSetValue();
		}
		return false;
	}

	public Variable getVariable(String var) {
		if(null == variables || null == var){
			return null;
		}
		for(Variable variable:variables){
			if(null == variable){
				continue;
			}
			if(var.equalsIgnoreCase(variable.getKey())){
				return variable;
			}
		}
		return null;
	}

	@Override
	public void setFilter(Object filter) {
		this.filter = filter;
	}

	@Override
	public Object getFilter() {
		return filter;
	}

	@Override
	public Object getUpdate() {
		return update;
	}

	@Override
	public void setUpdate(Object update) {
		this.update = update;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}
/*

	@Override
	public void setValues(List<Object> values) {
		this.batchValues = new ArrayList<>();
	}
*/


	@Override
	public Run setQueryColumns(String... columns) {
		if(null != columns) {
			this.queryColumns = BeanUtil.array2list(columns);
		}
		return this;
	}

	@Override
	public Run setQueryColumns(List<String> columns) {
		this.queryColumns = columns;
		return this;
	}

	@Override
	public List<String> getQueryColumns() {
		return this.queryColumns;
	}

	@Override
	public List<String> getExcludeColumns() {
		return excludeColumn;
	}

	@Override
	public Run setExcludeColumns(List<String> excludeColumn) {
		this.excludeColumn = excludeColumn;
		return this;
	}

	@Override
	public Run setExcludeColumns(String... columns) {
		if(null != columns) {
			this.queryColumns = BeanUtil.array2list(columns);
		}
		return this;
	}

	@Override
	public int getBatch() {
		return batch;
	}

	@Override
	public void setBatch(int batch) {
		this.batch = batch;
	}

	@Override
	public int getVol() {
		return vol;
	}

	@Override
	public String action(){
		return action;
	}
	@Override
	public void action(String action){
		this.action = action;
	}

	@Override
	public void setVol(int vol) {
		this.vol = vol;
	}

	/**
	 * 需要查询的列
	 * @return String
	 */
	@Override
	public String getQueryColumn(){
		String result = "*";
		if(null != prepare){
			List<String> cols = prepare.getFetchKeys();
			if(null != cols && cols.size()>0){
				result = null;
				for(String col:cols){
					if(null == result){

						result = SQLUtil.delimiter(col, runtime.getAdapter().getDelimiterFr() , runtime.getAdapter().getDelimiterTo());
					}else{
						result += "," + SQLUtil.delimiter(col, runtime.getAdapter().getDelimiterFr() , runtime.getAdapter().getDelimiterTo());
					}
				}
			}
		}
		return result;
	}

	/**
	 * 替换占位符
	 * @param sql sql
	 * @return String
	 */
	protected String replace(String sql){
		String result=sql;
		if(null != values){
			for(RunValue rv:values){
				Object value = rv.getValue();
				Column column = rv.getColumn();
				ColumnType columnType = null;
				if(null != column){
					columnType = column.getColumnType();
				}
				int index = result.indexOf("?");
				String replacement = null;
				if(null == value){
					value = "null";
				}
				DriverAdapter adapter = adapter();
				if(null != adapter){
					replacement = adapter.write(runtime, column, value, false)+"";
				}else {
					if (BasicUtil.isNumber(value) || "NULL".equalsIgnoreCase(value.toString())) {
						replacement = value.toString();
					} else {
						replacement = "'" + value + "'";
					}
				}
				result = result.substring(0, index) + replacement + result.substring(index+1);

			}
		}
		return result;
	}
	public String log(ACTION.DML action, boolean placeholder){
		StringBuilder builder = new StringBuilder();
		List<String> keys = null;
		builder.append("[sql:\n");
		if(action == ACTION.DML.SELECT){
			builder.append(getFinalQuery(placeholder));
		}else if(action == ACTION.DML.COUNT){
			builder.append(getTotalQuery(placeholder));
		}else if(action == ACTION.DML.UPDATE){
			keys = getUpdateColumns();
			builder.append(getFinalUpdate(placeholder));
		}else if(action == ACTION.DML.INSERT){
			keys = getInsertColumns();
			builder.append(getFinalInsert(placeholder));
		}else if(action == ACTION.DML.EXECUTE){
			builder.append(getFinalExecute(placeholder));
		}else if(action == ACTION.DML.DELETE){
			builder.append(getFinalDelete(placeholder));
		}else if(action == ACTION.DML.EXISTS){
			builder.append(getFinalExists(placeholder));
		}
		builder.append("\n]");
		if(placeholder){
			List<Object> values = getValues();
			if(null!= values && values.size() > 0) {
				builder.append("\n[param:");
				builder.append(LogUtil.param(keys, getValues()));
				builder.append("];");
			}
		}
		return builder.toString();
	}
}
 
 
