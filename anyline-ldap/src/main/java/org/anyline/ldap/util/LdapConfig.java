package org.anyline.ldap.util;

import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class LdapConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-ldap.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();


	public static String DEFAULT_ADDRESS					;
	public static int DEFAULT_PORT = 389					;
	public static String DEFAULT_DOMAIN						;
	public static String DEFAULT_ROOT						;
	public static String DEFAULT_SECURITY_AUTHENTICATION	;
	public static String DEFAULT_URL						; // ldap:{ADDRESS}:{PORT}
	public static int DEFAULT_CONNECT_TIMEOUT = 0			;
	public static int DEFAULT_READ_TIMEOUT = 0				;



	public String ADDRESS					= DEFAULT_ADDRESS 					;
	public int PORT 						= DEFAULT_PORT						;
	public String DOMAIN					= DEFAULT_DOMAIN					;
	public String ROOT						= DEFAULT_ROOT						;
	public String SECURITY_AUTHENTICATION	= DEFAULT_SECURITY_AUTHENTICATION	;
	public String URL						= DEFAULT_URL						; // ldap:{ADDRESS}:{PORT}
	public int CONNECT_TIMEOUT 				= DEFAULT_CONNECT_TIMEOUT			;
	public int READ_TIMEOUT 				= DEFAULT_READ_TIMEOUT				;

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
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static LdapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = DEFAULT_INSTANCE_KEY;
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
