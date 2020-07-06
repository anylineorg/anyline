package org.anyline.ldap.util;

import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class LdapConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-ldap.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	public String KEY = "default";
	public String ADDRESS;
	public int PORT = 389;
	public String DOMAIN;
	public String ROOT;
	public String SECURITY_AUTHENTICATION;
	public String URL; // ldap:{ADDRESS}:{PORT}
	static{ 
		init(); 
		debug();
}
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(LdapConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load();
	} 
 
	public static LdapConfig getInstance(){
		return getInstance("default"); 
	} 
	public static LdapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - LdapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载 
			load(); 
		}
		LdapConfig config = (LdapConfig)instances.get(key);
		if(null == config.URL){
			config.URL = "ldap://" + config.ADDRESS + ":" + config.PORT;
		}
		return config;
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, LdapConfig.class, CONFIG_NAME);
		LdapConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){ 
	} 
}
