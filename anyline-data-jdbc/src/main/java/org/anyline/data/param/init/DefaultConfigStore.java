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
import org.anyline.data.prepare.Condition.EMPTY_VALUE_CROSS;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.init.DefaultGroup;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.encrypt.DESUtil;

import java.util.ArrayList;
import java.util.Collection;
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
	public ConfigStore ands(EMPTY_VALUE_CROSS cross, String var, Object ... values){
		return and(cross, Compare.IN, var, BeanUtil.array2list(values));
	}
	@Override
	public ConfigStore ands(String var, Object ... values){
		return ands(EMPTY_VALUE_CROSS.DEFAULT, var, values);
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, String var, Object value, boolean overCondition, boolean overValue){
		return and(cross, (String)null, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore and(String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_CROSS.DEFAULT, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore and(String text){
		Config conf = new DefaultConfig();
		conf.setText(text);
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, String prefix, String var, Object value, boolean overCondition, boolean overValue){
		Compare compare = compare(value);
		return and(cross, compare, prefix, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore and(String prefix, String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_CROSS.DEFAULT, prefix, var, value, overCondition, overValue);
	}

	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, Compare compare, String var, Object value) {
		return and(compare, var, value, false, false);
	}
	@Override
	public ConfigStore and(Compare compare, String var, Object value) {
		return and(EMPTY_VALUE_CROSS.DEFAULT, compare, var, value);
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, Compare compare, String id, String var, Object value) {
		return and(cross, compare, id, var, value, false, false);
	}
	@Override
	public ConfigStore and(Compare compare, String id, String var, Object value) {
		return and(EMPTY_VALUE_CROSS.DEFAULT, compare, id, var, value);
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, String id, String var, Object value) {
		return and(cross, id, var, value, false, false);
	}
	@Override
	public ConfigStore and(String id, String var, Object value) {
		return and(EMPTY_VALUE_CROSS.DEFAULT, id, var, value);
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		Config conf = null;
		boolean require = false;
		boolean strictRequired = false;
		if(null == compare){
			compare = Compare.AUTO;
		}
		int compareCode = compare.getCode();
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
		value = value(value);

		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				boolean first = true;
				for(Object item:list){
					if(first){
						and(compare, prefix, var, item, false, false);
						first = false;
					}else {
						or(compare, var, item);
					}
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				for(Object item:list){
					and(compare, prefix, var, item, false, false);
				}
			}
		}else{
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
		}
		return this;
	}
	@Override
	public ConfigStore and(Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_CROSS.DEFAULT, compare, prefix, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return and(compare, null, var, value, overCondition, overValue);
	}
	@Override
	public ConfigStore and(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_CROSS.DEFAULT, compare, var, var, overCondition, overValue);
	}
	@Override
	public ConfigStore and(Config conf) {
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_CROSS cross, String var, Object value){
		return and(cross, var, value, false, false);
	}
	@Override
	public ConfigStore and(String var, Object value){
		return and(EMPTY_VALUE_CROSS.DEFAULT, var, value);
	}
	@Override
	public ConfigStore param(EMPTY_VALUE_CROSS cross, String var, Object value){
		return and(cross, var, value);
	}
	@Override
	public ConfigStore param(String var, Object value){
		return and(EMPTY_VALUE_CROSS.DEFAULT, var, value);
	}
	@Override
	public ConfigStore param(EMPTY_VALUE_CROSS cross, String id, String var, Object value){
		return and(cross, id, var, value);
	}
	@Override
	public ConfigStore param(String id, String var, Object value){
		return and(EMPTY_VALUE_CROSS.DEFAULT, id, var, value);
	}
	@Override
	public ConfigStore or(EMPTY_VALUE_CROSS cross, String var, Object value){
		Compare compare = compare(value);
		return or(cross, compare, var, value);
	}
	@Override
	public ConfigStore or(String var, Object value){
		return or(EMPTY_VALUE_CROSS.DEFAULT, var, value);
	}
	@Override
	public ConfigStore or(EMPTY_VALUE_CROSS cross, Compare compare, String var, Object value) {
		List<Config> configs = chain.getConfigs();
		// 如果当前没有其他条件
		if(configs.size()==0){
			and(compare, var, value);
		}else{
			int compareCode = compare.getCode();
			value = value(value);
			if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
				List list = (List)value;
				if(compareCode == 60 || compareCode == 61){
					//FIND_IN_OR
					for(Object item:list){
						or(compare, var, item);
					}
				}else if(compareCode == 62){
					//FIND_IN_AND
					ConfigChain findChain = new DefaultConfigChain();
					findChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					for(Object item:list){
						Config conf = new DefaultConfig();
						conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
						conf.setCompare(compare);
						conf.setVariable(var);
						conf.setValue(item);
						findChain.addConfig(conf);
					}
					chain.addConfig(findChain);
				}
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

				Config conf = null;

				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
				conf.setCompare(compare);
				conf.setVariable(var);
				conf.setValue(value);

				orChain.addConfig(conf);
				chain.addConfig(orChain);
			}
		}
		return this;
	}

	@Override
	public ConfigStore or(Compare compare, String var, Object value){
		return or(EMPTY_VALUE_CROSS.DEFAULT, compare, var, value);
	}


	@Override
	public ConfigStore ors(EMPTY_VALUE_CROSS cross, String var, Object value){
		Compare compare = compare(value);
		return ors(compare, var, value);
	}
	@Override
	public ConfigStore ors(String var, Object value){
		return ors(EMPTY_VALUE_CROSS.DEFAULT, var, value);
	}
	@Override
	public ConfigStore ors(EMPTY_VALUE_CROSS cross, Compare compare, String var, Object value) {
		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);

		int compareCode = compare.getCode();
		value = value(value);
		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				for(Object item:list){
					Config conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					conf.setCompare(compare);
					conf.setVariable(var);
					conf.setValue(item);
					newChain.addConfig(conf);
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				ConfigChain findChain = new DefaultConfigChain();
				findChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
				for(Object item:list){
					Config conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
					conf.setCompare(compare);
					conf.setVariable(var);
					conf.setValue(item);
					findChain.addConfig(conf);
				}
				newChain.addConfig(findChain);
			}
		}else {
			Config conf = new DefaultConfig();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
			conf.setCompare(compare);
			conf.setVariable(var);
			conf.setValue(value);

			newChain.addConfig(conf);
		}
		
		chain = newChain;
		return this;
	}

	@Override
	public ConfigStore ors(Compare compare, String var, Object value) {
		return ors(EMPTY_VALUE_CROSS.DEFAULT, compare, var, value);
	}

	private Object value(Object value){
		if(value instanceof Object[]){
			value = BeanUtil.array2list((Object[])value);
		}
		if(value instanceof List){
			List list = (List)value;
			if(list.size() == 0){
				value = null;
			}else if(list.size() ==1){
				value = list.get(0);
			}
		}
		return value;
	}
	private Compare compare(Object value){
		Compare compare = Compare.EQUAL;
		if(null != value){
			if(value instanceof Collection){
				Collection col = (Collection) value;
				if(col.size()>1){
					compare = Compare.IN;
				}
			}else if(value instanceof Object[]){
				Object[] array = (Object[])value;
				if(array.length > 1){
					compare = Compare.IN;
				}
			}
		}
		return compare;
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
	public ConfigStore order(String column, Order.TYPE type, boolean override){
		return order(new DefaultOrder(column,type), override);
	}

	@Override
	public ConfigStore order(String column, Order.TYPE type){
		return order(column, type, true);
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
	public ConfigStore group(String column){
		return group(new DefaultGroup(column));
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