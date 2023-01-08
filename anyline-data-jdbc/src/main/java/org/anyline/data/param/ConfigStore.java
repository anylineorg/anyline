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


package org.anyline.data.param;

import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.entity.Compare;

import java.util.List;
import java.util.Map;
 
 
/** 
 * 查询参数 
 * @author zh 
 * 
 */ 
public interface ConfigStore {
	/**
	 * 解析查询配置参数 
	 * @param config "COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]" 
	 * @return Config
	 */ 
	public Config parseConfig(String config); 
	public ConfigStore setPageNavi(PageNavi navi);
	public ConfigStore copyPageNavi(PageNavi navi);
	public ConfigStore addParam(String key, String value); 
	public ConfigStore setValue(Map<String,Object> values); 
	public ConfigChain getConfigChain();
	public Config getConfig(String key);
	public ConfigStore removeConfig(String var);
	public ConfigStore removeConfig(Config config);
	public List<Object> getConfigValues(String var);
	public Object getConfigValue(String var);
	public Config getConfig(String key, Compare compare);
	public ConfigStore removeConfig(String var, Compare compare);
	public List<Object> getConfigValues(String var, Compare compare);
	public Object getConfigValue(String var, Compare compare);
	public ConfigStore addConditions(String var, Object ... values);
	public ConfigStore addCondition(String var, Object value);
	/**
	 * 
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或列名
	 * @param value value
	 * @param overCondition 覆盖相同key的条件
	 * @param overValue		覆盖条件value overValue		覆盖条件value
	 * @return ConfigStore
	 */
	public ConfigStore addCondition(String id, String var, Object value, boolean overCondition, boolean overValue);

	/**
	 *
	 * @param var XML自定义SQL条件中指定变量赋值或列名
	 * @param value value
	 * @param overCondition 覆盖相同key的条件
	 * @param overValue		覆盖条件value overValue		覆盖条件value
	 * @return ConfigStore
	 */
	public ConfigStore addCondition(String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(String text);
	public ConfigStore addCondition(Compare compare, String id, Object value);
	public ConfigStore addCondition(Compare compare, String id, String var, Object value);
	public ConfigStore addCondition(Compare compare, String id, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);

	/**
	 * XML自定义SQL条件中指定变量赋值
	 * @param id condition.id或表名
	 * @param var condition.var
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore addCondition(String id, String var, Object value);
	public ConfigStore addCondition(Config config);
	
	public ConfigStore conditions(String var, Object value);
	public ConfigStore condition(String var, Object value);
	public ConfigStore condition(String id, String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(Compare compare, String var, Object value);
	public ConfigStore condition(Compare compare, String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(String id, String var, Object value);
	public ConfigStore condition(Config config);
	

	public ConfigStore and(String var, Object value);
	public ConfigStore and(Compare compare, String var, Object value);
	/**
	 * 与ConfigStore中前一个条件合成or
	 * @param key key
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore or(String key, Object value);
	public ConfigStore or(Compare compare, String var, Object value);
	/**
	 * 与ConfigStore中当前所有的条件合成or
	 * @param var var
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore ors(String var, Object value);
	public ConfigStore ors(Compare compare, String var, Object value);
	/** 
	 * 添加排序 
	 * @param order order
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	public ConfigStore order(Order order, boolean override);
	public ConfigStore order(Order order);


	public ConfigStore order(String column, Order.TYPE type, boolean override);
	public ConfigStore order(String column, Order.TYPE type);
	public ConfigStore order(String column, String type, boolean override);
	public ConfigStore order(String column, String type);
	public ConfigStore order(String order, boolean override);
	public ConfigStore order(String order);
	public OrderStore getOrders() ;
	public ConfigStore setOrders(OrderStore orders) ; 
	/** 
	 * 添加分组 
	 * @param group  group
	 * @return ConfigStore
	 */ 
	public ConfigStore group(Group group); 
 
	public ConfigStore group(String group); 
	public GroupStore getGroups() ; 
	public ConfigStore setGroups(GroupStore groups) ; 
	public PageNavi getPageNavi();
	/**
	 * 提取部分查询条件
	 * @param keys keys
	 * @return ConfigStore
	 */
	public ConfigStore fetch(String ... keys);
	
	public String toString();
	/**
	 * 开启记录总数懒加载 
	 * @param ms 缓存有效期(毫秒)
	 * @return ConfigStore
	 */
	public ConfigStore setTotalLazy(long ms); 
} 
 
 
