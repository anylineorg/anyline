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


package org.anyline.data.param.init;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.init.DefaultGroup;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.entity.*;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
import org.anyline.util.encrypt.DESUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
 
/** 
 * 查询参数 
 * @author zh 
 * 
 */ 
public class DefaultConfigStore implements ConfigStore {
	private static final long serialVersionUID = -2098827041540802313L;
	protected ConfigChain chain;
	protected PageNavi navi;
	protected OrderStore orders;		// 排序依据
	protected GroupStore groups;

	public DefaultConfigStore init(){
		return new DefaultConfigStore();
	}
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
	 * @return Config
	 */
	@Override 
	public Config parseConfig(String config){
		if(null == config){ 
			return null; 
		} 
		DefaultConfig conf = null;
		if(config.indexOf("|") != -1){ 
			conf = new DefaultConfigChain(config);
		}else{ 
			conf = new DefaultConfig(config);
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

	public DefaultConfigStore(String ... configs){
		configs = BasicUtil.compress(configs);
		chain = new DefaultConfigChain();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}

	/**
	 * 按起止行数查询
	 * @param fr 起
	 * @param to 止
	 */
	public DefaultConfigStore(int fr, int to){
		chain = new DefaultConfigChain();
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		navi.setTotalRow(to-fr+1);
		this.setPageNavi(navi);
	}
	public DefaultConfigStore(List<String> configs){
		configs = BasicUtil.compress(configs);
		chain = new DefaultConfigChain();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}


	@Override
	public ConfigStore addConditions(String var, Object ... values){
		return addCondition(Compare.IN, var, values);
//		Config conf = chain.getConfig(null,var,Compare.IN);
//		if(null == conf){
//			conf = new DefaultConfig();
//			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
//			conf.setCompare(Compare.IN);
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
	public ConfigStore addCondition(String text){
		Config conf = new DefaultConfig();
		conf.setText(text);
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore addCondition(String prefix, String var, Object value, boolean overCondition, boolean overValue){
		return addCondition(Compare.EQUAL, prefix, var, value, overCondition, overValue);
//		Config conf = null;
//		if(overCondition){
//			conf = chain.getConfig(prefix,var,Compare.EQUAL);
//		}
//		if(null == conf){
//			conf = new DefaultConfig();
//			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
//			conf.setCompare(Compare.EQUAL);
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
	public ConfigStore addCondition(Compare compare, String var, Object value) {
		return addCondition(compare, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(Compare compare, String id, String var, Object value) {
		return addCondition(compare, id, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(String id, String var, Object value) {
		return addCondition(id, var, value, false, false);
	}
	@Override
	public ConfigStore addCondition(Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
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
			conf = new DefaultConfig();
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
	public ConfigStore addCondition(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
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
	public ConfigStore and(Compare compare, String var, Object value) {
		return addCondition(compare, var, value, false, false);
	}

	@Override
	public ConfigStore or(String var, Object value){
		return or(Compare.EQUAL, var, value);
	} 
	@Override
	public ConfigStore or(Compare compare, String var, Object value) {
		List<Config> configs = chain.getConfigs();
		// 如果当前没有其他条件
		if(configs.size()==0){
			and(compare, var, value);
		}else{
			ConfigChain orChain = new DefaultConfigChain();
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
			Config conf = new DefaultConfig();
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
	public ConfigStore condition(Compare compare, String var, Object value) {
		return addCondition(compare, var, value);
	}

	@Override
	public ConfigStore condition(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
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
		return ors(Compare.EQUAL, var, value);
	} 
	@Override
	public ConfigStore ors(Compare compare, String var, Object value) {
		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		
		Config conf = new DefaultConfig();
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
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	@Override 
	public ConfigStore order(Order order, boolean override){
		if(null == orders){ 
			orders = new DefaultOrderStore();
		} 
		orders.order(order, override);
		if(null != navi){ 
			navi.order(order.getColumn(), order.getType().getCode(), override);
		}
		return this; 
	}
	@Override
	public ConfigStore order(Order order){
		return order(order, true);
	}
 
	@Override 
	public ConfigStore order(String column, String type, boolean override){
		return order(new DefaultOrder(column,type), override);
	}

	@Override
	public ConfigStore order(String column, String type){
		return order(column, type, true);
	}
	@Override
	public ConfigStore order(String order, boolean override){
		return order(new DefaultOrder(order), override);
	}
	@Override 
	public ConfigStore order(String order){
		return order(order, true);
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
	 * @return ConfigStore
	 */
	@Override 
	public ConfigStore group(Group group){
		if(null == groups){ 
			groups = new DefaultGroupStore();
		} 
		groups.group(group); 
		return this; 
	} 

	@Override 
	public ConfigStore group(String group){ 
		return group(new DefaultGroup(group));
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
	public Config getConfig(String var, Compare compare){
		return chain.getConfig(null,var,compare);
	}
	@Override
	public ConfigStore removeConfig(String var, Compare compare){
		Config config = getConfig(var, compare);
		return removeConfig(config);
	}
	@Override
	public List<Object> getConfigValues(String var, Compare compare){
		Config config = chain.getConfig(null, var,compare);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String var, Compare compare){
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
		DefaultConfigStore result = new DefaultConfigStore();
		result.setOrders(this.getOrders());
		result.setGroups(this.getGroups());
		result.setPageNavi(this.getPageNavi());
		ConfigChain chain = new DefaultConfigChain();
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
	 * @return ConfigStore
	 */
	public ConfigStore setTotalLazy(long ms){
		if(null != navi){
			navi.setLazy(ms);
		}
		return this;
	}
	public ConfigStore clone(){
		ConfigStore store = new DefaultConfigStore();
//		private ConfigChain chain;
//		private PageNavi navi;
//		private OrderStore orders;		// 排序依据
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