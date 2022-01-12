/* 
 * Copyright 2006-2022 www.anyline.org
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
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigChain;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.util.BasicUtil;

public class ConfigChainImpl extends ConfigImpl implements ConfigChain{ 
	private List<Config> configs = new ArrayList<Config>(); 
	 
	public ConfigChainImpl(){}
	public String toString(){
		String str = null;
		if(null != configs){
			for(Config conf:configs){
				if(null == conf){
					continue;
				}
				if(null == str){
					str = conf.toString();
				}else{
					str += "," + conf.toString();
				}
			}
		}
		return "["+str+"]";
	}
	public String cacheKey(){
		String str = null;
		if(null != configs){
			for(Config conf:configs){
				if(null == conf){
					continue;
				}
				if(null == str){
					str = conf.cacheKey();
				}else{
					str += "," + conf.cacheKey();
				}
			}
		}
		return str;
	} 
	public ConfigChainImpl(String config){ 
		if(null == config){ 
			return; 
		} 
		String[] configs = config.split("\\|"); 
		int size = configs.length;
		for(int i=0; i<size; i++){
			String item = configs[i];
			if(i+1 <size){
				String next = configs[i+1];
				if(!next.contains(":")){
					//NM:nm|title
					item = item+"|"+next;
					i ++;
				}
			}
			
			ConfigImpl conf = new ConfigImpl(item);
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
			if(!conf.isEmpty()){ 
				this.configs.add(conf);
			}
		} 
	}
	public Config getConfig(String id, String var){
		if(BasicUtil.isEmpty(id, var)){
			return null;
		}
		for(Config conf: configs){
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			if(BasicUtil.isEmpty(id)){
				//只提供列名，不提供表名
				if(var.equalsIgnoreCase(confVar)){
					return conf;
				}
			}else if(BasicUtil.isEmpty(var)){
				//只提供查询条件id不提供变量名
				if(id.equalsIgnoreCase(confId)){
					return conf;
				}
			}else{
				if(id.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar)){
					return conf;
				}
			}
		}
		return null;
	}
	public Config getConfig(String prefix, String var, SQL.COMPARE_TYPE type){
		if(BasicUtil.isEmpty(prefix, var)){
			return null;
		}
		for(Config conf: configs){
			String confId = conf.getPrefix();
			String confVar = conf.getVariable();
			SQL.COMPARE_TYPE confType = conf.getCompare();
			if(BasicUtil.isEmpty(prefix)){
				//只提供列名，不提供表名
				if(var.equalsIgnoreCase(confVar) && type == confType){
					return conf;
				}
			}else if(BasicUtil.isEmpty(var)){
				//只提供查询条件id不提供变量名
				if(prefix.equalsIgnoreCase(confId) && type == confType){
					return conf;
				}
			}else{
				if(prefix.equalsIgnoreCase(confId) && var.equalsIgnoreCase(confVar) && type == confType){
					return conf;
				}
			}
		}
		return null;
	}
	

	public ConfigChain removeConfig(String key, String var){
		Config config = getConfig(key, var);
		return removeConfig(config);
	}
	public ConfigChain removeConfig(String key, String var, SQL.COMPARE_TYPE type){
		Config config = getConfig(key, var, type);
		return removeConfig(config);
	}
	public ConfigChain removeConfig(Config config){
		if(null != config){
			configs.remove(config);
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
	public List<Config> getConfigs(){ 
		return configs; 
	} 
	public ConditionChain createAutoConditionChain(){ 
		ConditionChain chain = new AutoConditionChainImpl(); 
		for(Config config:configs){ 
			Condition condition = config.createAutoCondition(chain); 
			if(null != condition){ 
				chain.addCondition(condition); 
			} 
		} 
		return chain; 
	}
	@Override
	public void setValue(Map<String, Object> values) {
		for(Config config:configs){
			config.setValue(values);
		}
	} 
}
