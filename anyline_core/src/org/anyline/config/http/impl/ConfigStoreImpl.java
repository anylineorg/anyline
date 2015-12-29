
package org.anyline.config.http.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Condition;
import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQL;
import org.anyline.config.db.impl.GroupImpl;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.config.http.ConfigStore;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;


/**
 * 查询参数
 * @author Administrator
 *
 */
public class ConfigStoreImpl implements ConfigStore{
	private ConfigChain chain;
	private PageNavi navi;
	private OrderStore orders;		//排序依据
	private GroupStore groups;
	
	
	/**
	 * 解析查询配置参数
	 * @param configs	
	 * 			"COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]"
	 * @return
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
	public ConfigStoreImpl(String ... configs){
		configs = BasicUtil.compressionSpace(configs);
		chain = new ConfigChainImpl();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}        

	@Override
	public ConfigStore addConditions(String key, Object values){
		Config conf = chain.getConfig(key);
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(SQL.COMPARE_TYPE_IN);
		}
		conf.setId(key);
		conf.addValue(values);
		chain.addConfig(conf);
		return this;
	}
	@Override
	public ConfigStore addCondition(String key, Object value){
		Config conf = chain.getConfig(key);
		if(null == conf){
			conf = new ConfigImpl();
			conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
			conf.setCompare(SQL.COMPARE_TYPE_EQUAL);
		}
		conf.setId(key);
		conf.addValue(value);
		chain.addConfig(conf);
		return this;
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
	 * @param order
	 * @return
	 */
	@Override
	public ConfigStore order(Order order){
		if(null == orders){
			orders = new OrderStoreImpl();
		}
		orders.order(order);
		if(null != navi){
			navi.order(order);
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
	 * @param group
	 * @return
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
	public ConfigStore fetch(String ... keys){
		ConfigStoreImpl result = new ConfigStoreImpl();
		result.setOrders(this.getOrders());
		result.setGroups(this.getGroups());
		result.setPageNavi(this.getPageNavi());
		ConfigChain chain = new ConfigChainImpl();
		List<Config> configs = getConfigChain().getConfigs();
		for(Config config:configs){
			if(BasicUtil.contains(keys, config.getId())){
				chain.addConfig((Config)config.clone());
			}
		}
		result.chain = chain;
		return result;
	}
}


