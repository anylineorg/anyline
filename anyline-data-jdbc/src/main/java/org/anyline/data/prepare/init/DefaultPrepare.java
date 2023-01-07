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


package org.anyline.data.prepare.init;

import org.anyline.data.entity.Join;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
 
public abstract class DefaultPrepare implements RunPrepare{

	protected static final Logger log = LoggerFactory.getLogger(DefaultPrepare.class);
	protected ConditionChain chain							; // 查询条件
	protected OrderStore orders								; // 排序
	protected GroupStore groups								; // 分组条件
	protected PageNavi navi									; // 分页
	protected List<String> primaryKeys = new ArrayList<>()	; // 主键
	protected List<String> fetchKeys   = new ArrayList<>()	; // 最终需要封装的列
	protected boolean valid 		   = true				;
	protected String alias									;


	// 运行时参数值 
	protected Vector<Object> runValues; 
	public int getVersion(){ 
		return 0; 
	} 
 
	public DefaultPrepare(){
	} 
	protected void initRunValues(){ 
		if(null == runValues){ 
			runValues = new Vector<Object>(); 
		}else{ 
			runValues.clear(); 
		} 
	} 
	 
 
	/** 
	 * 添加排序条件,在之前的基础上添加新排序条件,有重复条件则覆盖 
	 * @param order  order
	 * @return RunPrepare
	 */ 
	public RunPrepare order(Order order){
		if(null == orders){ 
			orders = new DefaultOrderStore();
		} 
		orders.order(order); 
		return this; 
	} 
	 
	public RunPrepare order(String order){
		if(null == orders){ 
			orders = new DefaultOrderStore();
		} 
		orders.order(order); 
		return this; 
	}
	public RunPrepare order(String col, Order.TYPE type){
		if(null == orders){
			orders = new DefaultOrderStore();
		}
		orders.order(col, type);
		return this;
	}
 
	protected String getOrderText(String delimiter){
		if(null != orders){ 
			return orders.getRunText(delimiter);
		} 
		return ""; 
	} 
	/** 
	 * 添加分组条件,在之前的基础上添加新分组条件,有重复条件则覆盖 
	 * @param group  group
	 * @return RunPrepare
	 */ 
	public RunPrepare group(String group){
		/*避免添加空条件*/ 
		if(BasicUtil.isEmpty(group)){ 
			return this; 
		} 
		 
		if(null == groups){ 
			groups = new DefaultGroupStore();
		} 
		group = group.trim().toUpperCase(); 
		groups.group(group); 
		 
		return this; 
	} 
	/** 
	 * 添加运行时参数值 
	 * @param runValue  runValue
	 * @return RunPrepare
	 */ 
	@SuppressWarnings({ "unchecked", "rawtypes" }) 
	protected RunPrepare addRunValue(Object runValue){
		if(null == runValues){ 
			runValues = new Vector<Object>(); 
		} 
		if(runValue != null && runValue instanceof Collection){ 
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
	 
	public RunPrepare setConditionChain(ConditionChain chain){
		this.chain = chain; 
		return this; 
	} 
	/** 
	 * 添加标准查询条件
	 * @return RunPrepare
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
	 * @return RunPrepare
	 */ 
	public RunPrepare setConditionValue(String condition, String variable, Object value) {
		return this; 
	} 
	/** 
	 * 添加查询条件(自动生成) 
	 * @param column  列名
	 * @param value  值
	 * @param compare  比较方式
	 * @return RunPrepare
	 */
	public RunPrepare addCondition(String column, Object value, Compare compare) {
		Condition condition = new DefaultAutoCondition(false, false, null, column, value, compare) ;
		addCondition(condition);
		return this;
	}
	public RunPrepare addCondition(String column, Object value) {
		Compare compare = Compare.EQUAL;
		if(null != value && value instanceof Collection){
			compare = Compare.IN;
		}
		return addCondition(column, value, compare);
	}





	@Override 
	public RunPrepare setText(String text) {
		return this; 
	} 
	 
	public void setPageNavi(PageNavi navi){ 
		this.navi = navi; 
	} 
	public PageNavi getPageNavi(){ 
		return navi; 
	} 

	public GroupStore getGroups(){
		return groups;
	} 
	public OrderStore getOrders() { 
		return orders; 
	} 
 
	public void setOrders(OrderStore orders) { 
		if(null != orders){ 
			List<Order> list = orders.getOrders(); 
			for(Order order:list){ 
				this.order(order); 
			} 
			for(Order order:orders.getOrders()){				 
				orders.order(order); 
			} 
		}else{ 
			orders = this.orders; 
		} 
	} 
	 
	public ConditionChain getConditionChain(){ 
		return this.chain; 
	}
	
	
	public RunPrepare addPrimaryKey(String ... primaryKeys){
		if(null != primaryKeys){
			List<String> list = new ArrayList<>();
			for(String pk:primaryKeys){
				list.add(pk);
			}
			return addPrimaryKey(list);
		}
		return this;
	}
	public RunPrepare addPrimaryKey(Collection<String> primaryKeys){
		if(BasicUtil.isEmpty(primaryKeys)){
			return this;
		}
		
		/*没有处于容器中时,设置自身主键*/
		if(null == this.primaryKeys){
			this.primaryKeys = new ArrayList<>();
		}
		for(String item:primaryKeys){
			if(BasicUtil.isEmpty(item)){
				continue;
			}
			item = item.toUpperCase();
			if(!this.primaryKeys.contains(item)){
				this.primaryKeys.add(item);
			}
		}
		return this;
	}
	/**
	 * 设置主键 先清空之前设置过和主键
	 * 当前对象处于容器中时,设置容器主键,否则设置自身主键
	 * @param primaryKeys primaryKeys
	 * @return RunPrepare
	 */
	public RunPrepare setPrimaryKey(String ... primaryKeys){
		if(null != primaryKeys){
			List<String> list = new ArrayList<>();
			for(String pk:primaryKeys){
				list.add(pk);
			}
			return setPrimaryKey(list);
		}
		return this;
	}
	public RunPrepare setPrimaryKey(Collection<String> primaryKeys){
		if(BasicUtil.isEmpty(primaryKeys)){
			return this;
		}
		
		/*没有处于容器中时,设置自身主键*/
		if(null == this.primaryKeys){
			this.primaryKeys = new ArrayList<>();
		}else{
			this.primaryKeys.clear();
		}
		this.addPrimaryKey(primaryKeys);
		return this;
	}
	/**
	 * 读取主键
	 * 主键为空时且容器有主键时,读取容器主键,否则返回默认主键
	 * @return List
	 */
	public List<String> getPrimaryKeys(){
		return primaryKeys;
	}
	public String getPrimaryKey(){
		List<String> keys = getPrimaryKeys();
		if(null != keys && keys.size()>0){
			return keys.get(0); 
		}
		return null;
	}
	/**
	 * 自身是否有主键
	 * @return boolean
	 */
	public boolean hasPrimaryKeys(){
		if(null != primaryKeys && primaryKeys.size()>0){
			return true;
		}else{
			return false;
		}
	}
	
	

	public RunPrepare addFetchKey(String ... fetchKeys){
		if(null != fetchKeys){
			List<String> list = new ArrayList<>();
			for(String pk:fetchKeys){
				list.add(pk);
			}
			return addFetchKey(list);
		}
		return this;
	}
	public RunPrepare addFetchKey(Collection<String> fetchKeys){
		if(BasicUtil.isEmpty(fetchKeys)){
			return this;
		}
		
		if(null == this.fetchKeys){
			this.fetchKeys = new ArrayList<>();
		}
		for(String item:fetchKeys){
			if(BasicUtil.isEmpty(item)){
				continue;
			}
			item = item.toUpperCase();
			if(!this.fetchKeys.contains(item)){
				this.fetchKeys.add(item);
			}
		}
		return this;
	}
	public RunPrepare setFetchKey(String ... fetchKeys){
		if(null != fetchKeys){
			List<String> list = new ArrayList<>();
			for(String pk:fetchKeys){
				list.add(pk);
			}
			return setFetchKey(list);
		}
		return this;
	}
	public RunPrepare setFetchKey(Collection<String> fetchKeys){
		if(BasicUtil.isEmpty(fetchKeys)){
			return this;
		}
		
		if(null == this.fetchKeys){
			this.fetchKeys = new ArrayList<>();
		}else{
			this.fetchKeys.clear();
		}
		this.addFetchKey(fetchKeys);
		return this;
	}

	public List<String> getFetchKeys(){
		return fetchKeys;
	}
	public boolean isValid(){
		return this.valid;
	}
	public void setValid(boolean valid){
		this.valid = valid;
	}

	public RunPrepare join(Join join){
		return this;
	}
	public RunPrepare join(Join.TYPE type, String table, String condition){
		return this;
	}
	public RunPrepare inner(String table, String condition){
		return this;
	}
	public RunPrepare left(String table, String condition){
		return this;
	}
	public RunPrepare right(String table, String condition){
		return this;
	}
	public RunPrepare full(String table, String condition){
		return this;
	}
	public List<Join> getJoins(){
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
} 
