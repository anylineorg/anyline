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



package org.anyline.data.prepare.init;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.Column;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractRunPrepare implements RunPrepare{

	protected static final Logger log     = LoggerFactory.getLogger(AbstractRunPrepare.class);
	protected String id 										;
	protected String text										;
	protected ConditionChain chain								; // 查询条件
	protected OrderStore orders									; // 排序
	protected GroupStore groups									; // 分组条件
	protected String having										; // 分组过滤条件
	protected PageNavi navi										; // 分页
	protected List<String> primaryKeys     = new ArrayList<>()	; // 主键
	protected List<String> fetchKeys       = new ArrayList<>()	; // 最终需要封装的列
	protected boolean valid 		       = true				;
	protected String alias										;
	protected int batch = 0;
	protected boolean multiple		       = false				;
	protected boolean strict		       = false				; // 严格格式 不能追加条件
	protected String runtime		       = null				; //
	protected EMPTY_VALUE_SWITCH swt       = EMPTY_VALUE_SWITCH.IGNORE;
	protected LinkedHashMap<String,Column> columns = new LinkedHashMap<>();	//查询列
	protected List<String> excludes = new ArrayList<>();  //不查询列
	protected ConfigStore condition;
	protected boolean unionAll = false;
	protected List<RunPrepare> unions = new ArrayList<>();

	// 运行时参数值
	protected Vector<Object> runValues;

	public int getVersion() {
		return 0; 
	} 
 
	public AbstractRunPrepare() {
	} 
	protected void initRunValues() {
		if(null == runValues) {
			runValues = new Vector<Object>(); 
		}else{
			runValues.clear(); 
		} 
	} 
	 
 
	/** 
	 * 添加排序条件, 在之前的基础上添加新排序条件, 有重复条件则覆盖
	 * @param order  order
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	public RunPrepare order(Order order) {
		if(null == orders) {
			orders = new DefaultOrderStore();
		} 
		orders.order(order); 
		return this; 
	} 
	 
	public RunPrepare order(String order) {
		if(null == orders) {
			orders = new DefaultOrderStore();
		} 
		orders.order(order); 
		return this; 
	}
	public RunPrepare order(String col, Order.TYPE type) {
		if(null == orders) {
			orders = new DefaultOrderStore();
		}
		orders.order(col, type);
		return this;
	}
 
	protected String getOrderText(String delimiter) {
		if(null != orders) {
			return orders.getRunText(delimiter);
		} 
		return ""; 
	} 
	/** 
	 * 添加分组条件, 在之前的基础上添加新分组条件, 有重复条件则覆盖
	 * @param group  group
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	public RunPrepare group(String group) {
		/*避免添加空条件*/ 
		if(BasicUtil.isEmpty(group)) {
			return this; 
		} 
		 
		if(null == groups) {
			groups = new DefaultGroupStore();
		} 
		group = group.trim().toUpperCase(); 
		groups.group(group); 
		 
		return this; 
	}
	public RunPrepare having(String having) {
		this.having = having;
		return this;
	}
	/** 
	 * 添加运行时参数值 
	 * @param runValue  runValue
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	protected RunPrepare addRunValue(Object runValue) {
		if(null == runValues) {
			runValues = new Vector<>();
		} 
		if(runValue instanceof Collection) {
			Collection collection = (Collection)runValue; 
			runValues.addAll(collection); 
		}else{
			runValues.add(runValue); 
		} 
		return this; 
	} 
	/** 
	 * 运行时参数值 
	 * @return List
	 */ 
	public List<Object> getRunValues() {
		return runValues; 
	} 
	 
	public RunPrepare setConditionChain(ConditionChain chain) {
		this.chain = chain; 
		return this; 
	} 
	/** 
	 * 添加标准查询条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	public RunPrepare addCondition(Condition condition) {
		chain.addCondition(condition); 
		return this; 
	} 
 
	 
	/** 
	 * 设置查询条件变量值(XML定义) 
	 * @param condition	 条件ID 
	 * @param variable 变量key 
	 * @param value  值 
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	public RunPrepare setConditionValue(String condition, String variable, Object value) {
		return this; 
	} 
	/** 
	 * 添加查询条件(自动生成) 
	 * @param column  列名
	 * @param value  值
	 * @param compare  比较方式
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	public RunPrepare addCondition(Compare compare, String column, Object value) {
		Condition condition = new DefaultAutoCondition(Compare.EMPTY_VALUE_SWITCH.IGNORE, compare, null, column, value) ;
		addCondition(condition);
		return this;
	}
	public RunPrepare addCondition(Compare.EMPTY_VALUE_SWITCH swt, Compare compare, String column, Object value) {
		Condition condition = new DefaultAutoCondition(swt, compare, null, column, value) ;
		addCondition(condition);
		return this;
	}
	public RunPrepare addCondition(String column, Object value) {
		Compare compare = Compare.EQUAL;
		if(value instanceof Collection) {
			compare = Compare.IN;
		}
		return addCondition(compare, column, value);
	}

	@Override
	public RunPrepare setText(String text) {
		this.text = text;
		return this; 
	} 
	 
	public void setPageNavi(PageNavi navi) {
		this.navi = navi; 
	} 
	public PageNavi getPageNavi() {
		return navi; 
	}

	public GroupStore getGroups() {
		return groups;
	}
	public String getHaving() {
		return having;
	}
	public OrderStore getOrders() {
		return orders; 
	} 
 
	public void setOrders(OrderStore orders) {
		if(null != orders) {
			List<Order> list = orders.getOrders(); 
			for(Order order:list) {
				this.order(order); 
			} 
			for(Order order:orders.getOrders()) {
				orders.order(order); 
			} 
		}else{
			this.orders.clear();
		} 
	} 
	 
	public ConditionChain getConditionChain() {
		return this.chain; 
	}
	
	
	public RunPrepare addPrimaryKey(String ... primaryKeys) {
		if(null != primaryKeys) {
			List<String> list = new ArrayList<>();
			for(String pk:primaryKeys) {
				list.add(pk);
			}
			return addPrimaryKey(list);
		}
		return this;
	}
	public RunPrepare addPrimaryKey(Collection<String> primaryKeys) {
		if(BasicUtil.isEmpty(primaryKeys)) {
			return this;
		}
		
		/*没有处于容器中时, 设置自身主键*/
		if(null == this.primaryKeys) {
			this.primaryKeys = new ArrayList<>();
		}
		for(String item:primaryKeys) {
			if(BasicUtil.isEmpty(item)) {
				continue;
			}
			item = item.toUpperCase();
			if(!this.primaryKeys.contains(item)) {
				this.primaryKeys.add(item);
			}
		}
		return this;
	}

	/**
	 * 设置主键 先清空之前设置过和主键
	 * 当前对象处于容器中时, 设置容器主键, 否则设置自身主键
	 * @param primaryKeys primaryKeys
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	public RunPrepare setPrimaryKey(String ... primaryKeys) {
		if(null != primaryKeys) {
			List<String> list = new ArrayList<>();
			for(String pk:primaryKeys) {
				list.add(pk);
			}
			return setPrimaryKey(list);
		}
		return this;
	}
	public RunPrepare setPrimaryKey(Collection<String> primaryKeys) {
		if(BasicUtil.isEmpty(primaryKeys)) {
			return this;
		}
		
		/*没有处于容器中时, 设置自身主键*/
		if(null == this.primaryKeys) {
			this.primaryKeys = new ArrayList<>();
		}else{
			this.primaryKeys.clear();
		}
		this.addPrimaryKey(primaryKeys);
		return this;
	}

	/**
	 * 读取主键
	 * 主键为空时且容器有主键时, 读取容器主键, 否则返回默认主键
	 * @return List
	 */
	public List<String> getPrimaryKeys() {
		return primaryKeys;
	}
	public String getPrimaryKey() {
		List<String> keys = getPrimaryKeys();
		if(null != keys && !keys.isEmpty()) {
			return keys.get(0); 
		}
		return null;
	}

	/**
	 * 自身是否有主键
	 * @return boolean
	 */
	public boolean hasPrimaryKeys() {
		if(null != primaryKeys && !primaryKeys.isEmpty()) {
			return true;
		}else{
			return false;
		}
	}
	
	

	public RunPrepare addFetchKey(String ... fetchKeys) {
		if(null != fetchKeys) {
			List<String> list = new ArrayList<>();
			for(String pk:fetchKeys) {
				list.add(pk);
			}
			return addFetchKey(list);
		}
		return this;
	}
	public RunPrepare addFetchKey(Collection<String> fetchKeys) {
		if(BasicUtil.isEmpty(fetchKeys)) {
			return this;
		}
		
		if(null == this.fetchKeys) {
			this.fetchKeys = new ArrayList<>();
		}
		for(String item:fetchKeys) {
			if(BasicUtil.isEmpty(item)) {
				continue;
			}
			item = item.toUpperCase();
			if(!this.fetchKeys.contains(item)) {
				this.fetchKeys.add(item);
			}
		}
		return this;
	}
	public RunPrepare setFetchKey(String ... fetchKeys) {
		if(null != fetchKeys) {
			List<String> list = new ArrayList<>();
			for(String pk:fetchKeys) {
				list.add(pk);
			}
			return setFetchKey(list);
		}
		return this;
	}
	public RunPrepare setFetchKey(Collection<String> fetchKeys) {
		if(BasicUtil.isEmpty(fetchKeys)) {
			return this;
		}
		
		if(null == this.fetchKeys) {
			this.fetchKeys = new ArrayList<>();
		}else{
			this.fetchKeys.clear();
		}
		this.addFetchKey(fetchKeys);
		return this;
	}

	public List<String> getFetchKeys() {
		return fetchKeys;
	}

	public RunPrepare join(Join join) {
		return this;
	}
	public RunPrepare join(Join.TYPE type, String table, String condition) {
		return this;
	}
	public RunPrepare inner(String table, String condition) {
		return this;
	}
	public RunPrepare left(String table, String condition) {
		return this;
	}
	public RunPrepare right(String table, String condition) {
		return this;
	}
	public RunPrepare full(String table, String condition) {
		return this;
	}
	public List<Join> getJoins() {
		return null;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	@Override
	public RunPrepare setMultiple(boolean multiple) {
		this.multiple = multiple;
		return this;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public RunPrepare setId(String id) {
		this.id = id;
		return this;
	}

	@Override
	public String getRuntime() {
		return runtime;
	}

	@Override
	public RunPrepare setRuntime(String runtime) {
		this.runtime = runtime;
		return this;
	}

	public boolean isStrict() {
		return strict;
	}
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	@Override
	public RunPrepare setQueryColumns(String... columns) {
		if(null != columns) {
			setQueryColumns(BeanUtil.array2list(columns));
		}
		return this;
	}

	@Override
	public RunPrepare setQueryColumns(List<String> columns) {
		this.columns = new LinkedHashMap<>();
		for(String column:columns) {
			this.columns.put(column.toUpperCase(), new Column(column));
		}
		return this;
	}

	@Override
	public LinkedHashMap<String,Column> getColumns() {
		return this.columns;
	}

	@Override
	public List<String> getExcludes() {
		return excludes;
	}

	@Override
	public RunPrepare setExcludeColumns(List<String> excludeColumn) {
		this.excludes = excludeColumn;
		return this;
	}

	@Override
	public RunPrepare setExcludeColumns(String... columns) {
		if(null != columns) {
			this.excludes = BeanUtil.array2list(columns);
		}
		return this;
	}

	@Override
	public int getBatch() {
		return batch;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	@Override
	public ConfigStore condition() {
		return condition;
	}

	@Override
	public RunPrepare condition(ConfigStore condition) {
		this.condition = condition;
		return this;
	}

	@Override
	public RunPrepare setUnionAll(boolean all) {
		this.unionAll = all;
		return this;
	}@Override
	public boolean isUnionAll() {
		return unionAll;
	}
	@Override
	public RunPrepare union(RunPrepare prepare, boolean all) {
		prepare.setUnionAll(all);
		unions.add(prepare);
		return this;
	}

	@Override
	public RunPrepare union(RunPrepare prepare) {
		unions.add(prepare);
		return this;
	}

	@Override
	public RunPrepare union(List<RunPrepare> unions, boolean all) {
		for(RunPrepare union:unions) {
			union(union, all);
		}
		return this;
	}

	@Override
	public RunPrepare union(List<RunPrepare> unions) {
		for(RunPrepare union:unions) {
			union(union);
		}
		return this;
	}

	@Override
	public List<RunPrepare> getUnions() {
		return  unions;
	}
}
