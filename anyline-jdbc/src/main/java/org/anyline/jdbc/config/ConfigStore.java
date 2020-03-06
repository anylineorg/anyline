/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.jdbc.config; 
 
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.*;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;

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
	 * @return return
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
	public Config getConfig(String key, SQL.COMPARE_TYPE compare);
	public ConfigStore removeConfig(String var, SQL.COMPARE_TYPE compare);
	public List<Object> getConfigValues(String var, SQL.COMPARE_TYPE compare);
	public Object getConfigValue(String var, SQL.COMPARE_TYPE compare);
	public ConfigStore addConditions(String var, Object value);
	public ConfigStore addCondition(String var, Object value);
	/**
	 * 
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或列名
	 * @param value value
	 * @param overCondition 覆盖相同key的条件
	 * @param overValue		覆盖条件value overValue		覆盖条件value
	 * @return return
	 */
	public ConfigStore addCondition(String id, String var, Object value, boolean overCondition, boolean overValue);

	/**
	 *
	 * @param var XML自定义SQL条件中指定变量赋值或列名
	 * @param value value
	 * @param overCondition 覆盖相同key的条件
	 * @param overValue		覆盖条件value overValue		覆盖条件value
	 * @return return
	 */
	public ConfigStore addCondition(String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(COMPARE_TYPE compare, String id, Object value);
	public ConfigStore addCondition(COMPARE_TYPE compare, String id, String var, Object value);
	public ConfigStore addCondition(COMPARE_TYPE compare, String id, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(COMPARE_TYPE compare, String id, String var, Object value, boolean overCondition, boolean overValue);

	/**
	 * XML自定义SQL条件中指定变量赋值
	 * @param id condition.id或表名
	 * @param var condition.var
	 * @param value value
	 * @return return
	 */
	public ConfigStore addCondition(String id, String var, Object value);
	public ConfigStore addCondition(Config config);
	
	public ConfigStore conditions(String var, Object value);
	public ConfigStore condition(String var, Object value);
	public ConfigStore condition(String id, String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(COMPARE_TYPE compare, String var, Object value);
	public ConfigStore condition(COMPARE_TYPE compare, String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore condition(String id, String var, Object value);
	public ConfigStore condition(Config config);
	

	public ConfigStore and(String var, Object value);
	public ConfigStore and(COMPARE_TYPE compare, String var, Object value);
	/**
	 * 与ConfigStore中前一个条件合成or
	 * @param key key
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore or(String key, Object value);
	public ConfigStore or(COMPARE_TYPE compare, String var, Object value);
	/**
	 * 与ConfigStore中当前所有的条件合成or
	 * @param var var
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore ors(String var, Object value);
	public ConfigStore ors(COMPARE_TYPE compare, String var, Object value);
	/** 
	 * 添加排序 
	 * @param order  order
	 * @return return
	 */ 
	public ConfigStore order(Order order); 
 
	public ConfigStore order(String column, String type); 
	public ConfigStore order(String order); 
	public OrderStore getOrders() ; 
	public ConfigStore setOrders(OrderStore orders) ; 
	/** 
	 * 添加分组 
	 * @param group  group
	 * @return return
	 */ 
	public ConfigStore group(Group group); 
 
	public ConfigStore group(String group); 
	public GroupStore getGroups() ; 
	public ConfigStore setGroups(GroupStore groups) ; 
	public PageNavi getPageNavi();
	/**
	 * 提取部分查询条件
	 * @param keys keys
	 * @return return
	 */
	public ConfigStore fetch(String ... keys);
	
	public String toString();
	/**
	 * 开启记录总数懒加载 
	 * @param ms 缓存有效期(毫秒)
	 * @return return
	 */
	public ConfigStore setTotalLazy(long ms); 
} 
 
 
