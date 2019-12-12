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


package org.anyline.config.http.impl; 
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Condition;
import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQL.COMPARE_TYPE;
import org.anyline.config.db.impl.GroupImpl;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
 
 
/** 
 * 查询参数 
 * @author zh 
 * 
 */ 
public class ConfigStoreImpl implements ConfigStore{ 
	private ConfigChain chain; 
	private PageNavi navi; 
	private OrderStore orders;		//排序依据 
	private GroupStore groups; 

	@Override
	public String toString(){
		String str = "";
		if(null != chain){
			str += chain.toString();
		}
		if(null != navi){
			str += "." + navi.getFirstRow() + "." + navi.getLastRow() + "." + navi.getCurPage();
		}
		if(null != orders){
			str += "." + orders.getRunText("");
		}
		if(null != groups){
			str += "." + groups.getRunText("");
		}
		return str;
	} 
	 
	/** 
	 * 解析查询配置参数 
	 * @param config	  configs
	 * 			"COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]"
	 * "NM:nmEn% | NM:nmCn%" 生成 NM={nmEn} OR NM = {nmCn}
	 * "NM:nmEn:nmCn" 根据参数值生成NM = {nmEn}或生成 NM={nmCn}   
	 * @return return
	 */
	@Override 
	public Config parseConfig(String config){ 
		if(null == config){ 
			return null; 
		} 
		ConfigImpl conf = null; 
		if(config.indexOf("|") != -1){ 
			conf = new ConfigChainImpl(config); 
		}else{ 
			conf = new ConfigImpl(config); 
		} 
		return conf; 
	}
	@Override
	public ConfigStore setPageNavi(PageNavi navi){
		this.navi = navi;
		return this;
	}
	@Override
	public ConfigStore copyPageNavi(PageNavi navi){
		if(null == this.navi){
			this.navi = navi;
		}else{
			this.navi.setBaseLink(navi.getBaseLink());
			this.navi.setCalType(navi.getCalType());
			this.navi.setCurPage(navi.getCurPage());
			this.navi.setDisplayPageFirst(navi.getDisplayPageFirst());
			this.navi.setDisplayPageLast(navi.getDisplayPageLast());
			this.navi.setFirstRow(navi.getFirstRow());
			this.navi.setLastRow(navi.getLastRow());
			this.navi.setPageRows(navi.getPageRows());
			this.navi.setTotalPage(navi.getTotalPage());
			this.navi.setTotalRow(navi.getTotalRow());
		}
		return this;
	}
	
	public ConfigStoreImpl(String ... configs){ 
		configs = BasicUtil.compressionSpace(configs); 
		chain = new ConfigChainImpl(); 
		for(String config:configs){ 
			chain.addConfig(parseConfig(config)); 
		} 
	}        

	@Override
	public ConfigStore addConditions(String key, Object values){
		Config conf = chain.getConfig(key,SQL.COMPARE_TYPE.IN);
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(SQL.COMPARE_TYPE.IN);
		}
		conf.setId(key);
		if(null != values && !(values instanceof Collection)){
			String s = values.toString();
			if(s.startsWith("[") && s.endsWith("]")){
				s = s.substring(1,s.length()-1);
				String[] ss = s.split(",");
				if(null != ss){
					List<Object> list = new ArrayList<Object>();
					for(String item:ss){
						list.add(item.trim());
					}
					values = list;
				}
			}
		}
		conf.addValue(values);
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore addCondition(String key, Object value, boolean overCondition, boolean overValue){
		return addCondition(key, null, value, overCondition, overValue);
	}
	@Override
	public ConfigStore addCondition(String key, String var, Object value, boolean overCondition, boolean overValue){
		Config conf = null;
		if(overCondition){
			conf = chain.getConfig(key,SQL.COMPARE_TYPE.EQUAL);
		}
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(SQL.COMPARE_TYPE.EQUAL);
			chain.addConfig(conf);
		}
		conf.setId(key);
		if(BasicUtil.isNotEmpty(var)){
			conf.setVariable(var);
		}
		if(overValue){
			conf.setValue(value);
		}else{
			conf.addValue(value);
		}
		return this;
	}

	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String key, Object value) {
		return addCondition(compare, key, value, false, false);
	}
	@Override
	public ConfigStore addCondition(String key, String var, Object value) {
		return addCondition(key, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String key, Object value, boolean overCondition, boolean overValue) {
		Config conf = null;
		if(overCondition){
			conf = chain.getConfig(key, compare);
		}
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(compare);
			chain.addConfig(conf);
		}
		conf.setId(key);
		if(overValue){
			conf.setValue(value);
		}else{
			conf.addValue(value);
		}
		return this;
	}

	@Override
	public ConfigStore addCondition(String key, Object value){
		return addCondition(key, value, false, false);
	} 
	/** 
	 * 把httpRequest中的参数存放到navi 
	 */ 
	private void setNaviParam(){ 
		if(null == chain || null == navi){ 
			return; 
		} 
		 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			String key = config.getKey(); 
			List<Object> values = new ArrayList<Object>(); 
			List<Object> srcValues = config.getValues(); 
			if(config.isKeyEncrypt()){ 
				key = WebUtil.encryptHttpRequestParamKey(key); 
			} 
			if(config.isValueEncrypt()){ 
				for(Object value:srcValues){ 
					if(null != value){ 
						value = WebUtil.encryptHttpRequestParamValue(value.toString()); 
						values.add(value); 
					} 
				} 
			}else{ 
				values = srcValues; 
			} 
			navi.addParam(key, values); 
		} 
	}
	@Override 
	public ConfigStore addParam(String key, String value){ 
		if(null != navi){ 
			navi.addParam(key, value); 
		} 
		return this; 
	}
	@Override 
	public ConfigStore setValue(HttpServletRequest request){ 
		if(null == chain){ 
			return this; 
		} 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			config.setValue(request); 
		} 
		setNaviParam();
		return this; 
	}
	@Override 
	public ConfigChain getConfigChain(){ 
		return chain; 
	} 
	/** 
	 * 添加排序 
	 * @param order  order
	 * @return return
	 */
	@Override 
	public ConfigStore order(Order order){ 
		if(null == orders){ 
			orders = new OrderStoreImpl(); 
		} 
		orders.order(order); 
		if(null != navi){ 
			navi.order(order.getColumn(), order.getType().getCode()); 
		} 
		return this; 
	} 
 
	@Override 
	public ConfigStore order(String column, String type){ 
		return order(new OrderImpl(column,type)); 
	} 
	@Override 
	public ConfigStore order(String order){ 
		return order(new OrderImpl(order)); 
	} 
	@Override 
	public OrderStore getOrders() { 
		return orders; 
	} 
	@Override 
	public ConfigStore setOrders(OrderStore orders) { 
		this.orders = orders;
		return this; 
	} 
	/** 
	 * 添加分组 
	 * @param group  group
	 * @return return
	 */
	@Override 
	public ConfigStore group(Group group){ 
		if(null == groups){ 
			groups = new GroupStoreImpl(); 
		} 
		groups.group(group); 
		return this; 
	} 

	@Override 
	public ConfigStore group(String group){ 
		return group(new GroupImpl(group)); 
	} 
	public GroupStore getGroups() { 
		return groups; 
	} 
	public ConfigStore setGroups(GroupStore groups) { 
		this.groups = groups;
		return this; 
	} 
	@Override 
	public PageNavi getPageNavi() { 
		return navi; 
	}

	@Override
	public Config getConfig(String key){
		return chain.getConfig(key);
	}
	public ConfigStore removeConfig(String key){
		Config config = getConfig(key);
		return removeConfig(config);
	}
	@Override
	public ConfigStore removeConfig(Config config){
		chain.removeConfig(config);
		return this;
	}
	@Override
	public List<Object> getConfigValues(String key){
		Config config = chain.getConfig(key);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String key){
		Config config = chain.getConfig(key);
		if(null != config){
			List<Object> values = config.getValues();
			if(null != values && values.size() > 0){
				return values.get(0);
			}
		}
		return null;
	}

	@Override
	public Config getConfig(String key, SQL.COMPARE_TYPE compare){
		return chain.getConfig(key,compare);
	}
	@Override
	public ConfigStore removeConfig(String key, SQL.COMPARE_TYPE compare){
		Config config = getConfig(key, compare);
		return removeConfig(config);
	}
	@Override
	public List<Object> getConfigValues(String key, SQL.COMPARE_TYPE compare){
		Config config = chain.getConfig(key,compare);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String key, SQL.COMPARE_TYPE compare){
		Config config = chain.getConfig(key,compare);
		if(null != config){
			List<Object> values = config.getValues();
			if(null != values && values.size() > 0){
				return values.get(0);
			}
		}
		return null;
	}
	public ConfigStore fetch(String ... keys){
		ConfigStoreImpl result = new ConfigStoreImpl();
		result.setOrders(this.getOrders());
		result.setGroups(this.getGroups());
		result.setPageNavi(this.getPageNavi());
		ConfigChain chain = new ConfigChainImpl();
		List<Config> configs = getConfigChain().getConfigs();
		for(Config config:configs){
			if(null == config){
				continue;
			}
			if(BasicUtil.contains(keys, config.getId())){
				chain.addConfig((Config)config.clone());
			}
		}
		result.chain = chain;
		return result;
	}
	/**
	 * 开启记录总数懒加载 
	 * @param ms 缓存有效期(毫秒)
	 * @return return
	 */
	public ConfigStore setTotalLazy(long ms){
		if(null != navi){
			navi.setLazy(ms);
		}
		return this;
	}
	public ConfigStore clone(){
		ConfigStore store = new ConfigStoreImpl();
//		private ConfigChain chain;
//		private PageNavi navi;
//		private OrderStore orders;		//排序依据
//		private GroupStore groups;
		return store;
	} 
} 
 
 
