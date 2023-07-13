package org.anyline.aliyun.sms.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;

import java.util.Hashtable;
 
public class SMSConfig extends AnylineConfig {
	private static Hashtable<String, AnylineConfig> instances = new Hashtable<String, AnylineConfig>();

	public static String DEFAULT_ACCESS_KEY 	= "";
	public static String DEFAULT_ACCESS_SECRET  = "";
	public static String DEFAULT_SIGN	 		= "";


	// 服务器配置
	public String ACCESS_KEY 	= DEFAULT_ACCESS_KEY		;
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET		;
	public String SIGN 			= DEFAULT_SIGN				;

	public static String CONFIG_NAME = "anyline-aliyun-sms.xml";

	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	static {
		init();
		debug();
	}


	/**
	 * 解析配置文件内容
	 *
	 * @param content 配置文件内容
	 */
	public static void parse(String content) {
		parse(SMSConfig.class, content, instances, compatibles);
	}

	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载配置文件
		load();
	}

	public static SMSConfig getInstance() {
		return getInstance(DEFAULT_INSTANCE_KEY);
	}

	public static SMSConfig getInstance(String key) {
		if (BasicUtil.isEmpty(key)) {
			key = DEFAULT_INSTANCE_KEY;
		}
		return (SMSConfig) instances.get(key);
	}

	/**
	 * 加载配置文件
	 */
	private synchronized static void load() {
		load(instances, SMSConfig.class, CONFIG_NAME);
	}

	private static void debug() {
	}

	public static SMSConfig register(String instance, DataRow row) {
		SMSConfig config = parse(SMSConfig.class, instance, row, instances, compatibles);
		return config;
	}
	public static SMSConfig register(String instance, String key, String secret) {
		DataRow row = new DataRow();
		row.put("ACCESS_KEY", key);
		row.put("ACCESS_SECRET", secret);
		SMSConfig config = parse(SMSConfig.class, instance, row, instances, compatibles);
		SMSUtil.getInstance(instance);
		return config;
	}
	public static SMSConfig register(String key, String secret) {
		return register(DEFAULT_INSTANCE_KEY, key, secret);
	}
}
