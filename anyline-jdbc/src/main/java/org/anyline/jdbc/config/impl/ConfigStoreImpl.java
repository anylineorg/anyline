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


package org.anyline.jdbc.config.impl; 
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigChain;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.Group;
import org.anyline.jdbc.config.db.GroupStore;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQL.COMPARE_TYPE;
import org.anyline.jdbc.config.db.impl.GroupImpl;
import org.anyline.jdbc.config.db.impl.GroupStoreImpl;
import org.anyline.jdbc.config.db.impl.OrderImpl;
import org.anyline.jdbc.config.db.impl.OrderStoreImpl;
import org.anyline.util.BasicUtil;
import org.anyline.util.DESUtil;
 
 
/** 
 * 查询参数 
 * @author zh 
 * 
 */ 
public class ConfigStoreImpl implements ConfigStore, Serializable {
	private static final long serialVersionUID = -2098827041540802313L;
	protected ConfigChain chain;
	protected PageNavi navi;
	protected OrderStore orders;		//排序依据
	protected GroupStore groups;

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
	 * "NM:nmEn|NM:nmCn" 生成 NM={nmEn} OR NM = {nmCn}
	 * "NM:nmEn|nmCn" 生成 NM={nmEn} OR NM = {nmCn} nmEn为空时当前条件不生效
	 * "NM:nmEn|{1}" 
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
	public ConfigStore addConditions(String var, Object values){
		return addCondition(COMPARE_TYPE.IN, var, values);
//		Config conf = chain.getConfig(null,var,SQL.COMPARE_TYPE.IN);
//		if(null == conf){
//			conf = new ConfigImpl();
//			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
//			conf.setCompare(SQL.COMPARE_TYPE.IN);
//		}
//		conf.setVariable(var);
//		if(null != values && !(values instanceof Collection)){
//			String s = values.toString();
//			if(s.startsWith("[") && s.endsWith("]")){
//				s = s.substring(1,s.length()-1);
//				String[] ss = s.split(",");
//				if(null != ss){
//					List<Object> list = new ArrayList<Object>();
//					for(String item:ss){
//						list.add(item.trim());
//					}
//					values = list;
//				}
//			}
//		}
//		conf.addValue(values);
//		chain.addConfig(conf);
//		return this;
	}
	@Override
	public ConfigStore addCondition(String var, Object value, boolean overCondition, boolean overValue){
		return addCondition((String)null, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore addCondition(String prefix, String var, Object value, boolean overCondition, boolean overValue){
		return addCondition(COMPARE_TYPE.EQUAL, prefix, var, value, overCondition, overValue);
//		Config conf = null;
//		if(overCondition){
//			conf = chain.getConfig(prefix,var,SQL.COMPARE_TYPE.EQUAL);
//		}
//		if(null == conf){
//			conf = new ConfigImpl();
//			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
//			conf.setCompare(SQL.COMPARE_TYPE.EQUAL);
//			chain.addConfig(conf);
//		}
//		conf.setPrefix(prefix);
//		if(BasicUtil.isNotEmpty(var)){
//			conf.setVariable(var);
//		}
//		if(overValue){
//			conf.setValue(value);
//		}else{
//			conf.addValue(value);
//		}
//		return this;
	}

	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String var, Object value) {
		return addCondition(compare, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String id, String var, Object value) {
		return addCondition(compare, id, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(String id, String var, Object value) {
		return addCondition(id, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		Config conf = null;
		boolean require = false;
		boolean strictRequired = false;
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1,var.length());
		}
		if(overCondition){
			conf = chain.getConfig(prefix,var, compare);
		}
		if(null != var){
			if(var.startsWith("++")){
				strictRequired = true;
				var = var.substring(2);
			}else if(var.startsWith("+")){
				require = true;
				var = var.substring(1);
			}
		}
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(compare);
			chain.addConfig(conf);
		}
		conf.setPrefix(prefix);
		conf.setVariable(var);
		conf.setRequire(require);
		conf.setStrictRequired(strictRequired);
		if(overValue){
			conf.setValue(value);
		}else{
			conf.addValue(value);
		}
		return this;
	}

	@Override
	public ConfigStore addCondition(COMPARE_TYPE compare, String var, Object value, boolean overCondition, boolean overValue) {
		return addCondition(compare, null, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore addCondition(Config conf) {
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore addCondition(String var, Object value){
		return addCondition(var, value, false, false);
	} 
	@Override
	public ConfigStore and(String var, Object value){
		return addCondition(var, value, false, false);
	} 
	@Override
	public ConfigStore and(COMPARE_TYPE compare, String var, Object value) {
		return addCondition(compare, var, value, false, false);
	}

	@Override
	public ConfigStore or(String var, Object value){
		return or(COMPARE_TYPE.EQUAL, var, value);
	} 
	@Override
	public ConfigStore or(COMPARE_TYPE compare, String var, Object value) {
		List<Config> configs = chain.getConfigs();
		//如果当前没有其他条件
		if(configs.size()==0){
			and(compare, var, value);
		}else{
			ConfigChain orChain = new ConfigChainImpl();
			Config last = configs.get(configs.size()-1);
			configs.remove(last);
			
			if(last instanceof ConfigChain){
				ConfigChain lastChain = (ConfigChain)last;
				List<Config> lastItems = lastChain.getConfigs();
				for(Config lastItem:lastItems){
					orChain.addConfig(lastItem);
				}
			}else{
				orChain.addConfig(last);
			}
			Config conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
			conf.setCompare(compare);
			conf.setVariable(var);
			conf.setValue(value);
			
			orChain.addConfig(conf);
			chain.addConfig(orChain);
		}
		return this;
	}
	
	@Override
	public ConfigStore conditions(String var, Object value) {
		return addConditions(var, value);
	}

	@Override
	public ConfigStore condition(String var, Object value) {
		return addCondition(var, value);
	}

	@Override
	public ConfigStore condition(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return addCondition(id, var, value, overCondition, overValue);
	}

	@Override
	public ConfigStore condition(String var, Object value, boolean overCondition, boolean overValue) {
		return addCondition(var, value, overCondition, overValue);
	}

	@Override
	public ConfigStore condition(COMPARE_TYPE compare, String var, Object value) {
		return addCondition(compare, var, value);
	}

	@Override
	public ConfigStore condition(COMPARE_TYPE compare, String var, Object value, boolean overCondition, boolean overValue) {
		return addCondition(compare, var, value, overCondition, overValue);
	}

	@Override
	public ConfigStore condition(String id, String var, Object value) {
		return addCondition(id, var, value);
	}

	@Override
	public ConfigStore condition(Config config) {
		return addCondition(config);
	}

	@Override
	public ConfigStore ors(String var, Object value){
		return ors(COMPARE_TYPE.EQUAL, var, value);
	} 
	@Override
	public ConfigStore ors(COMPARE_TYPE compare, String var, Object value) {
		ConfigChain newChain = new ConfigChainImpl();
		newChain.addConfig(chain);
		
		Config conf = new ConfigImpl();
		conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		conf.setCompare(compare);
		conf.setVariable(var);
		conf.setValue(value);
		
		newChain.addConfig(conf);
		
		chain = newChain;
		return this;
	}
	
	/** 
	 * 把httpRequest中的参数存放到navi 
	 */ 
	protected void setNaviParam(){
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
				key = DESUtil.encryptParamKey(key); 
			} 
			if(config.isValueEncrypt() && null != srcValues){ 
				for(Object value:srcValues){ 
					if(null != value){ 
						value = DESUtil.encryptParamValue(value.toString()); 
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
	public Config getConfig(String var){
		return chain.getConfig(null,var);
	}
	public ConfigStore removeConfig(String var){
		Config config = getConfig(var);
		return removeConfig(config);
	}
	@Override
	public ConfigStore removeConfig(Config config){
		chain.removeConfig(config);
		return this;
	}
	@Override
	public List<Object> getConfigValues(String var){
		Config config = chain.getConfig(null, var);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String var){
		Config config = chain.getConfig(null,var);
		if(null != config){
			List<Object> values = config.getValues();
			if(null != values && values.size() > 0){
				return values.get(0);
			}
		}
		return null;
	}

	@Override
	public Config getConfig(String var, SQL.COMPARE_TYPE compare){
		return chain.getConfig(null,var,compare);
	}
	@Override
	public ConfigStore removeConfig(String var, SQL.COMPARE_TYPE compare){
		Config config = getConfig(var, compare);
		return removeConfig(config);
	}
	@Override
	public List<Object> getConfigValues(String var, SQL.COMPARE_TYPE compare){
		Config config = chain.getConfig(null, var,compare);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String var, SQL.COMPARE_TYPE compare){
		Config config = chain.getConfig(null, var,compare);
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
			if(BasicUtil.contains(keys, config.getPrefix())){
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

	@Override 
	public ConfigStore setValue(Map<String, Object> values) {
		if(null == chain || null == values){ 
			return this; 
		} 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			config.setValue(values); 
		} 
		setNaviParam();
		return this; 
	}
} 