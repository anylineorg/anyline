package org.anyline.nacos.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executor;

public class NacosUtil {
	private static Logger log = LoggerFactory.getLogger(NacosUtil.class);
	private NacosConfig config = null;

	private static Hashtable<String, NacosUtil> instances = new Hashtable<String, NacosUtil>();


	public static NacosUtil getInstance(){
		return getInstance("default");
	}
	public static NacosUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		NacosUtil util = instances.get(key);
		if(null == util){
			util = new NacosUtil();
			NacosConfig config = NacosConfig.getInstance(key);
			util.config = config;
			instances.put(key, util);
		}
		return util;
	}
	public boolean config(String group, String data, final Class<? extends AnylineConfig> T) throws NacosException{
		boolean result = false;
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, config.ADDRESS+":"+config.PORT);
		ConfigService configService = NacosFactory.createConfigService(properties);
		String content = configService.getConfig(data, group, 5000);
		parse(T, content);
		configService.addListener(data, group, new Listener() {
			@Override
			public void receiveConfigInfo(String content) {
				parse(T, content);
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});
		result = true;
		return result;
	}

	public static void parse(Class<? extends AnylineConfig> T, String content) {
		try{
		    Class<?> clazz = Class.forName(T.getName());
		    Method method = clazz.getMethod("parse", String.class);
		    method.invoke(null, content);
		}catch(Exception e){
			
		}
	}
}
