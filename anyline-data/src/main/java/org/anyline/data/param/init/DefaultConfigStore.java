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
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.init.DefaultGroup;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.entity.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.encrypt.DESUtil;

import java.util.*;


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
	protected List<String> columns = new ArrayList<>();
	protected Object values; //保存values后续parse用到

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
	 * @param first 起
	 * @param last 止
	 */
	public DefaultConfigStore(long first, long last){
		chain = new DefaultConfigChain();
		scope(first, last);
	}
	public DefaultConfigStore(List<String> configs){
		configs = BasicUtil.compress(configs);
		chain = new DefaultConfigChain();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}

	/**
	 * 起止行 下标从0开始
	 * @param first 起
	 * @param last 止
	 * @return ConfigStore
	 */
	public ConfigStore scope(long first, long last){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.scope(first, last);
		navi.setTotalRow(last-first+1);
		this.setPageNavi(navi);
		return this;
	}
	/**
	 * 起止行 下标从0开始
	 * @param offset 指定第一个返回记录行的偏移量（即从哪一行开始返回） 初始行的偏移量为0
	 * @param rows 返回具体行数
	 * @return ConfigStore
	 */
	public ConfigStore limit(long offset, int rows){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.setFirstRow(offset);
		navi.setLastRow(offset+rows);
		navi.setCalType(1);
		navi.setTotalRow(rows);
		this.setPageNavi(navi);
		return this;
	}

	/**
	 * 设置分页
	 * @param page 第page页 下标从1开始
	 * @param rows 每页rows行
	 * @return ConfigStore
	 */
	public ConfigStore page(long page, int rows){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.setCurPage(page);
		navi.setPageRows(rows);
		navi.setCalType(0);
		this.setPageNavi(navi);
		return this;
	}

	@Override
	public ConfigStore and(EMPTY_VALUE_SWITCH swt, String text){
		Config conf = new DefaultConfig();
		conf.setText(text);
		chain.addConfig(conf);
		return this;
	}

	@Override
	public ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		if(null == compare){
			compare = Compare.AUTO;
		}
		int compareCode = compare.getCode();
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}
		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			if (null != var) {
				if (var.startsWith("++")) {
					swt = EMPTY_VALUE_SWITCH.BREAK;
					var = var.substring(2);
				} else if (var.startsWith("+")) {
					swt = EMPTY_VALUE_SWITCH.NULL;
					var = var.substring(1);
				}
			}
		}
		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			swt = EMPTY_VALUE_SWITCH.IGNORE;
		}

		value = value(value);

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 and id = 2 and id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}

		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if (overValue) {
				chain.removeConfig(olds);
			}
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				boolean first = true;
				for(Object item:list){
					if(first){
						and(swt, compare, prefix, var, item, false, false);
						first = false;
					}else {
						or(compare, var, item);
					}
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				for(Object item:list){
					and(swt, compare, prefix, var, item, false, false);
				}
			}
		}else{
			if(null == conf){
				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
				conf.setCompare(compare);
				conf.setPrefix(prefix);
				conf.setVariable(var);
				conf.setSwitch(swt);
				conf.setValue(value);
				chain.addConfig(conf);
			}else{
				conf.setOverCondition(overCondition);
				conf.setOverValue(overValue);
				if (overValue) {
					conf.setValue(value);
				} else {
					conf.addValue(value);
				}
			}
		}
		return this;
	}


	@Override
	public ConfigStore and(Config conf) {
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore and(ConfigStore configs) {
		chain.addConfig(configs.getConfigChain());
		return this;
	}

	@Override
	public ConfigStore or(ConfigStore configs) {
		ConfigChain orChain = configs.getConfigChain();
		orChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		chain.addConfig(orChain);
		return this;
	}
	@Override
	public ConfigStore or(Config config) {
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		chain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore or(EMPTY_VALUE_SWITCH swt, String text){
		Config config = new DefaultConfig();
		config.setText(text);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		chain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore ors(ConfigStore configs) {
		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		ConfigChain orChain = configs.getConfigChain();
		orChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		newChain.addConfig(orChain);
		return this;
	}

	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, String text){
		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		Config config = new DefaultConfig();
		config.setText(text);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		newChain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, Config config) {
		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		newChain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix,  String var, Object value, boolean overCondition, boolean overValue) {
		List<Config> configs = chain.getConfigs();
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 or id = 2 or id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}
		// 如果当前没有其他条件
		if(configs.size()==0){
			and(swt, compare, prefix, var, value, overCondition, overValue);
		}else{
			int compareCode = compare.getCode();
			value = value(value);
			if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
				List list = (List)value;
				if (overValue) {
					chain.removeConfig(olds);
				}
				if(compareCode == 60 || compareCode == 61){
					//FIND_IN_OR
					for(Object item:list){
						or(swt, compare, prefix, var, item);
					}
				}else if(compareCode == 62){
					//FIND_IN_AND
					ConfigChain findChain = new DefaultConfigChain();
					findChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					for(Object item:list){
						conf = new DefaultConfig();
						conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
						conf.setCompare(compare);
						conf.setPrefix(prefix);
						conf.setVariable(var);
						conf.setValue(item);
						findChain.addConfig(conf);
					}
					chain.addConfig(findChain);
				}
			}else{
				//覆盖原条件(不要新加)
				if(null != conf){
					if(overValue){
						conf.setValue(value);
					}else {
						conf.addValue(value);
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
					conf = new DefaultConfig();
					orChain.addConfig(conf);
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					conf.setCompare(compare);
					conf.setVariable(var);
					conf.setPrefix(prefix);
					conf.setValue(value);

					chain.addConfig(orChain);
				}
			}
		}
		return this;
	}
	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}
		int compareCode = compare.getCode();

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 or id = 2 or id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}

		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		value = value(value);
		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if (overValue) {
				chain.removeConfig(olds);
			}
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				for(Object item:list){
					conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					conf.setPrefix(prefix);
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
					conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
					conf.setCompare(compare);
					conf.setPrefix(prefix);
					conf.setVariable(var);
					conf.setValue(item);
					findChain.addConfig(conf);
				}
				newChain.addConfig(findChain);
			}
		}else {
			if(null != conf) {
				if(overValue){
					conf.setValue(value);
				}else{
					conf.addValue(value);
				}
			}else{
				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
				conf.setCompare(compare);
				conf.setPrefix(prefix);
				conf.setVariable(var);
				conf.setValue(value);
				newChain.addConfig(conf);
			}

		}
		
		chain = newChain;
		return this;
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

	@Override 
	public ConfigStore setValue(Map<String, Object> values) {
		this.values = values;
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

	/**
	 * 设置城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	public ConfigStore columns(String ... columns){
		if(null != columns){
			for(String column:columns){
				this.columns.add(column);
			}
		}
		return this;
	}
	public List<String> columns(){
		return columns;
	}
	@Override
	public boolean isValid() {
		if(null != chain){
			for(Config config:chain.getConfigs()){
				if(config.getSwitch() == EMPTY_VALUE_SWITCH.BREAK && config.isEmpty()){
					return false;
				}
			}
		}
		return true;
	}

	public ConfigStore clone(){
		ConfigStore store = new DefaultConfigStore();
		return store;
	}

	public ConfigStore condition(String join, Compare compare, String key, Object ... values){
		if("or".equalsIgnoreCase(join)){
			or(compare, key, values);
		}else{
			and(compare, key, values);
		}
		return this;
	}
	public ConfigStore condition(String join, String compare, String key, String value){
		return this;
	}
}