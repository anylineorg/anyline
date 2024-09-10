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
 */

package org.anyline.web.param;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class WebConfigChain extends WebConfig implements  ConfigChain{
	private List<Config> configs = new ArrayList<>();


	public Config getConfig(String id, String var){
		List<Config> list = getConfigs(id, var);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	public List<Config> getConfigs(String id, String var){
		List<Config> list = new ArrayList<>();
		if(BasicUtil.isEmpty(id, var)){
			return list;
		}
		for(Config conf: configs){
			if(conf instanceof ConfigChain){
				ConfigChain chain = (ConfigChain)conf;
				list.addAll(chain.getConfigs(id, var));
			}
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			if(BasicUtil.isEmpty(id)){
				// 只提供列名,不提供表名
				if(var.equalsIgnoreCase(confVar)){
					list.add(conf);
				}
			}else if(BasicUtil.isEmpty(var)){
				// 只提供查询条件id不提供变量名
				if(id.equalsIgnoreCase(confId)){
					list.add(conf);
				}
			}else{
				if(id.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar)){
					list.add(conf);
				}
			}
		}
		return null;
	}

	public List<Config> getConfigs(String prefix, String var, Compare type){
		List<Config> list = new ArrayList<>();
		if(BasicUtil.isEmpty(prefix, var)){
			return list;
		}
		for(Config conf: configs){
			if(conf instanceof ConfigChain){
				ConfigChain chain = (ConfigChain)conf;
				list.addAll(chain.getConfigs(prefix, var, type));
			}
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			Compare confType = conf.getCompare();
			if(BasicUtil.isEmpty(prefix)){
				// 只提供列名,不提供表名
				if(var.equalsIgnoreCase(confVar) && type == confType){
					list.add(conf);
				}
			}else if(BasicUtil.isEmpty(var)){
				// 只提供查询条件id不提供变量名
				if(prefix.equalsIgnoreCase(confId) && type == confType){
					list.add(conf);
				}
			}else{
				if(prefix.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar) && type == confType){
					list.add(conf);
				}
			}
		}
		return list;
	}
	public Config getConfig(String prefix, String var, Compare type){
		if(BasicUtil.isEmpty(prefix, var)){
			return null;
		}
		List<Config> list = getConfigs(prefix, var, type);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}



	public ConfigChain removeConfig(String prefix, String var){
		List<Config> configs = getConfigs(prefix, var);
		return removeConfig(configs);
	}
	public ConfigChain removeConfig(String key, String var, Compare type){
		List<Config> configs = getConfigs(key, var, type);
		return removeConfig(configs);
	}
	public ConfigChain removeConfig(Config config){
		if(null != config){
			configs.remove(config);
			for(Config item:configs){
				if(item instanceof ConfigChain){
					ConfigChain chain = ((ConfigChain)item);
					chain.removeConfig(config);
				}
			}
		}
		return this;
	}

	public ConfigChain removeConfig(List<Config> list){
		if(null != list){
			for(Config config:list) {
				removeConfig(config);
			}
		}
		return this;
	}

	public void addConfig(Config config){
		configs.add(config);
	}

	public List<Object> getValues() {
		List<Object> values = new ArrayList<Object>();
		for(Config config:configs){
			values.addAll(config.getValues());
		}
		return values;
	}
	/** 
	 * 赋值 
	 * @param request  request
	 */ 
	public void setValue(HttpServletRequest request){
		for(Config config:configs){
			config.setValue(request); 
		} 
//		if(items.size()>0){
//			setCompare(items.get(0).getCompareType()); 
//		} 
	}
	public List<Config> getConfigs(){
		return configs;
	}
	public ConditionChain createAutoConditionChain(){
		ConditionChain chain = new DefaultAutoConditionChain();
		for(Config config:configs){
			Condition condition = config.createAutoCondition(chain);
			if(null != condition){
				chain.addCondition(condition);
			}
		}
		chain.integrality(integrality);
		return chain;
	}
	public WebConfigChain clone(){
		WebConfigChain clone = null;
		try {
			clone = (WebConfigChain) super.clone();
		}catch (Exception ignored){
			clone = new WebConfigChain();
		}
		if(null != this.configs){
			List<Config> configs = new ArrayList<>();
			for(Config config:this.configs){
				configs.add(config.clone());
			}
			clone.configs = configs;
		}
		return clone;
	}
}
