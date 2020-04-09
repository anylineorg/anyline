package org.anyline.redis.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;

import java.util.Hashtable;
 
 
public class RedisConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-redis.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	public String HOST = ""; 
	public String CLIENT_ID =""; 
	public String CLIENT_SECRET =""; 
	public String ORG_NAME =""; 
	public String APP_NAME =""; 
		 
	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(RedisConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load();
	} 
	public static RedisConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static RedisConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		return (RedisConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() {
		load(instances, RedisConfig.class, CONFIG_NAME);
		RedisConfig.lastLoadTime = System.currentTimeMillis();
	} 
	 
	private static void debug(){} 
} 
