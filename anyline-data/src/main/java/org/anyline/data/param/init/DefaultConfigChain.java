/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.param.init;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OriginRow;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultConfigChain extends DefaultConfig implements ConfigChain {
	private List<Config> configs = new ArrayList<>();
	 
	public DefaultConfigChain() {}
	public String toString() {
		String str = null;
		if(null != configs) {
			for(Config conf:configs) {
				if(null == conf) {
					continue;
				}
				if(null == str) {
					str = conf.toString();
				}else{
					str += "," + conf;
				}
			}
		}
		return "["+str+"]";
	}

	public DataRow map() {
		return map(true);
	}
	public DataRow map(boolean empty) {
		DataRow row = new OriginRow();
		Condition.JOIN join = getJoin();
		if(null != join) {
			row.put("join", join);
		}
		if((null != configs && !configs.isEmpty()) || empty) {
			DataSet<DataRow> set = new DataSet();
			for (Config config : configs) {
				set.add(config.map(empty));
			}
			row.put("items", set);
		}
		return row;
	}
	public String json() {
		return map().json();
	}
	public String cacheKey() {
		String str = null;
		if(null != configs) {
			for(Config conf:configs) {
				if(null == conf) {
					continue;
				}
				if(null == str) {
					str = conf.cacheKey();
				}else{
					str += "," + conf.cacheKey();
				}
			}
		}
		return str;
	} 
	public DefaultConfigChain(String config) {
		if(null == config) {
			return; 
		} 
		String[] configs = config.split("\\|"); 
		int size = configs.length;
		for(int i=0; i<size; i++) {
			String item = configs[i];
			if(i+1 <size) {
				String next = configs[i+1];
				if(!next.contains(":")) {
					// NM:nm|title
					item = item+"|"+next;
					i ++;
				}
			}
			DefaultConfig conf = new DefaultConfig(item);
			conf.setJoin(Condition.JOIN.OR);
			//只是设置了key还没有设置value
			//if(!conf.isEmpty()) {
				this.configs.add(conf);
			//}
		} 
	}
	public Config getConfig(String id, String var) {
		List<Config> list = getConfigs(id, var);
		if(!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	public List<Config> getConfigs(String id, String var) {
		List<Config> list = new ArrayList<>();
		if(BasicUtil.isEmpty(id, var)) {
			return list;
		}
		for(Config conf: configs) {
			if(conf instanceof ConfigChain) {
				ConfigChain chain = (ConfigChain) conf;
				list.addAll(chain.getConfigs(id, var));
			}
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			if(BasicUtil.isEmpty(id)) {
				// 只提供列名,不提供表名
				if(var.equalsIgnoreCase(confVar)) {
					list.add(conf);
				}
			}else if(BasicUtil.isEmpty(var)) {
				// 只提供查询条件id不提供变量名
				if(id.equalsIgnoreCase(confId)) {
					list.add(conf);
				}
			}else{
				if(id.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar)) {
					list.add(conf);
				}
			}
		}
		return list;
	}
	public List<Config> getConfigs(String prefix, String var, Compare type) {
		List<Config> list = new ArrayList<>();
		if(BasicUtil.isEmpty(prefix, var)) {
			return list;
		}
		for(Config conf: configs) {
			if(conf instanceof ConfigChain) {
				ConfigChain chain = (ConfigChain)conf;
				list.addAll(chain.getConfigs(prefix, var, type));
			}
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			Compare confType = conf.getCompare();

			if(BasicUtil.isEmpty(prefix)) {
				// 只提供列名,不提供表名
				if(var.equalsIgnoreCase(confVar) && type == confType) {
					list.add(conf);
				}
			}else if(BasicUtil.isEmpty(var)) {
				// 只提供查询条件id不提供变量名
				if(prefix.equalsIgnoreCase(confId) && type == confType) {
					list.add(conf);
				}
			}else{
				if(prefix.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar) && type == confType) {
					list.add(conf);
				}
			}
		}
		return list;
	}
	public Config getConfig(String prefix, String var, Compare type) {
		if(BasicUtil.isEmpty(prefix, var)) {
			return null;
		}
		List<Config> list = getConfigs(prefix, var, type);
		if(!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public ConfigChain removeConfig(String prefix, String var) {
		List<Config> configs = getConfigs(prefix, var);
		return removeConfig(configs);
	}
	public ConfigChain removeConfig(String key, String var, Compare type) {
		List<Config> configs = getConfigs(key, var, type);
		return removeConfig(configs);
	}
	public ConfigChain removeConfig(Config config) {
		if(null != config) {
			configs.remove(config);
			for(Config item:configs) {
				if(item instanceof ConfigChain) {
					ConfigChain chain = ((ConfigChain)item);
					chain.removeConfig(config);
				}
			}
		}
		return this;
	}

	public ConfigChain removeConfig(List<Config> list) {
		if(null != list) {
			for(Config config:list) {
				removeConfig(config);
			}
		}
		return this;
	}

	public void addConfig(Config config) {
		if(null != config) {
			if(config instanceof ConfigChain){
				ConfigChain chain = (ConfigChain) config;
				List<Config> items = chain.getConfigs();
				//拆分时注意有OR的情况
				Condition.JOIN join = chain.getJoin();
				if(items.size() ==1){
					//只有一个条件的情况 拆开多余的括号
					Config item = items.get(0);
					item.setJoin(config.getJoin());
					this.configs.add(item);
				}else{
					this.configs.add(config);
				}
			}else{
				this.configs.add(config);
			}

		}
	} 

	public List<Object> getValues() {
		List<Object> values = new ArrayList<Object>();
		for(Config config:configs) {
			List<Object> vs = config.getValues();
			if(null != vs) {
				values.addAll(vs);
			}
		}
		return values;
	} 
	public List<Config> getConfigs() {
		return configs; 
	} 
	public ConditionChain createAutoConditionChain() {
		ConditionChain chain = new DefaultAutoConditionChain();
		chain.setJoin(this.getJoin());
		for(Config config:configs) {
			Condition condition = config.createAutoCondition(chain); 
			if(null != condition) {
				if(condition instanceof ConditionChain) {
					ConditionChain itemChain = (ConditionChain) condition;
					List<Condition> items = itemChain.getConditions();
					if(items.size() == 1) {
						//只有一个条件的 去除多余() 但join需要用外层的
						Condition first = items.get(0);
						first.setJoin(itemChain.getJoin());
						chain.addCondition(first);
					}else if(items.size() > 1) {
						chain.addCondition(condition);
					}
				}else {
					chain.addCondition(condition);
				}
			} 
		}
		chain.integrality(integrality);
		return chain; 
	}
	@Override
	public void setValue(Map<String, Object> values) {
		for(Config config:configs) {
			config.setValue(values);
		}
	}

	public boolean isEmpty() {
		if(null != configs) {
			for(Config config:configs) {
				if(null != config && !config.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}
	public DefaultConfigChain clone() {
		DefaultConfigChain clone = null;
		try{
			clone = (DefaultConfigChain) super.clone();
			clone.configs = new ArrayList<>();
		}catch (Exception e) {
			clone = new DefaultConfigChain();
		}
		List<Config> configs = this.getConfigs();
		for(Config config:configs) {
			if(null == config) {
				continue;
			}
			clone.configs.add(config.clone());
		}
		return clone;
	}
}
