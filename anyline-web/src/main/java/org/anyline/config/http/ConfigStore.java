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
 *          
 */


package org.anyline.config.http; 
 
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.entity.PageNavi;
 
 
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
	public ConfigStore setValue(HttpServletRequest request); 
	public ConfigChain getConfigChain();
	public Config getConfig(String key);
	public ConfigStore removeConfig(String key);
	public ConfigStore removeConfig(Config config);
	public List<Object> getConfigValues(String key);
	public Object getConfigValue(String key);
	public Config getConfig(String key, SQL.COMPARE_TYPE compare);
	public ConfigStore removeConfig(String key, SQL.COMPARE_TYPE compare);
	public List<Object> getConfigValues(String key, SQL.COMPARE_TYPE compare);
	public Object getConfigValue(String key, SQL.COMPARE_TYPE compare);
	public ConfigStore addConditions(String key, Object value);
	public ConfigStore addCondition(String key, Object value);
	/**
	 * 
	 * @param key key
	 * @param var XML自定义SQL条件中指定变量赋值
	 * @param value value
	 * @param overCondition 覆盖相同key的条件
	 * @param overValue		覆盖条件value overValue		覆盖条件value
	 * @return return
	 */
	public ConfigStore addCondition(String key, String var, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(String key, Object value, boolean overCondition, boolean overValue);
	public ConfigStore addCondition(COMPARE_TYPE compare, String key, Object value);
	public ConfigStore addCondition(COMPARE_TYPE compare, String key, Object value, boolean overCondition, boolean overValue); 


	public ConfigStore and(String key, Object value);
	public ConfigStore and(COMPARE_TYPE compare, String key, Object value);
	/**
	 * 与ConfigStore中前一个条件合成or
	 * @param key key
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore or(String key, Object value);
	public ConfigStore or(COMPARE_TYPE compare, String key, Object value);
	/**
	 * 与ConfigStore中当前所有的条件合成or
	 * @param key key
	 * @param value value
	 * @return ConfigStore
	 */
	public ConfigStore ors(String key, Object value);
	public ConfigStore ors(COMPARE_TYPE compare, String key, Object value);
	/**
	 * XML自定义SQL条件中指定变量赋值
	 * @param key condition.id
	 * @param var condition.var
	 * @param value value
	 * @return return
	 */
	public ConfigStore addCondition(String key, String var, Object value);
	
	public ConfigStore addCondition(Config config);
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
 
 
