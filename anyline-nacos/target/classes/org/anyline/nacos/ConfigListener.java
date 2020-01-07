package org.anyline.nacos;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.anyline.util.AnylineConfig;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

public class ConfigListener {
	public boolean reg(String addr, String group, String data, final Class<? extends AnylineConfig> T) throws NacosException{
		boolean result = false;
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, addr);
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
