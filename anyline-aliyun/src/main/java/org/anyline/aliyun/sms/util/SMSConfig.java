package org.anyline.aliyun.sms.util;

import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;

public class SMSConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	//服务器配置
	public String ACCESS_KEY= "";
	public String ACCESS_SECRET = "";
	public String SMS_SIGN = "";
	//客户端配置
	public String SMS_SERVER = "";
	public String CLIENT_APP= "";
	public String CLIENT_SECRET= "";
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static SMSConfig getInstance(){
		return getInstance("default");
	}
	public static SMSConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (SMSConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, SMSConfig.class, "anyline-sms.xml");
	}
	private static void debug(){
	}
}
