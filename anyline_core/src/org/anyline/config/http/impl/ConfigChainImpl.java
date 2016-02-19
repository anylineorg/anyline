

package org.anyline.config.http.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.sql.auto.impl.AutoConditionChainImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.apache.log4j.Logger;

public class ConfigChainImpl extends ConfigImpl implements ConfigChain{
	private static Logger LOG = Logger.getLogger(ConfigChainImpl.class);
	private List<Config> configs = new ArrayList<Config>();
	
	public ConfigChainImpl(){}
	public String toString(){
		String str = "";
		if(null != configs){
			for(Config conf:configs){
				str += "." + conf.toString();
			}
		}
		return str;
	}
	public ConfigChainImpl(String config){
		if(null == config){
			return;
		}
		String[] configs = config.split("\\|");
		for(String item:configs){
			ConfigImpl conf = new ConfigImpl(item);
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
			this.configs.add(conf);
		}
	}
	public Config getConfig(String key){
		for(Config conf: configs){
			if(conf.getId().equals(key)){
				return conf;
			}
		}
		return null;
	}
	public void addConfig(Config config){
		configs.add(config);
	}

	/**
	 * 赋值
	 * @param request
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
		ConditionChain chain = new AutoConditionChainImpl();
		for(Config config:configs){
			Condition condition = config.createAutoCondition(chain);
			if(null != condition){
				chain.addCondition(condition);
			}
		}
		return chain;
	}
}