package org.anyline.mail.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class MailConfig extends AnylineConfig{

	public static String CONFIG_NAME = "anyline-mail.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	private static File configDir;



	public static String DEFAULT_ACCOUNT					= null 	;
	public static String DEFAULT_PASSWORD					= null 	;
	public static String DEFAULT_USERNAME					= null	;
	public static String DEFAULT_PROTOCOL 					= "smtp";
	public static String DEFAULT_HOST						= null	;
	public static String DEFAULT_PORT						= null 	;
	public static String DEFAULT_ATTACHMENT_DIR 			= ""	;	// 附件下载地址
	public static boolean DEFAULT_SSL_FLAG 					= false	;  // 是否需要ssl验证  具体看服务商情况  smtp  25不需要  465需要
	public static boolean DEFAULT_AUTO_DOWNLOAD_ATTACHMENT 	= true	;


	public String ACCOUNT					= DEFAULT_ACCOUNT 					;
	public String PASSWORD					= DEFAULT_PASSWORD					;
	public String USERNAME					= DEFAULT_USERNAME					;
	public String PROTOCOL 					= DEFAULT_PROTOCOL					;
	public String HOST						= DEFAULT_HOST						;
	public String PORT						= DEFAULT_PORT						;
	public String ATTACHMENT_DIR 			= DEFAULT_ATTACHMENT_DIR			;	// 附件下载地址
	public boolean SSL_FLAG 				= DEFAULT_SSL_FLAG					;  // 是否需要ssl验证  具体看服务商情况  smtp  25不需要  465需要
	public boolean AUTO_DOWNLOAD_ATTACHMENT = DEFAULT_AUTO_DOWNLOAD_ATTACHMENT	;

	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	static{
		init();
		debug();
	}
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(MailConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		// 加载默认配置文件
		load();
	}
	public static void setConfigDir(File dir){
		configDir = dir;
		init();
	}
	public static MailConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	}
	public static MailConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		}
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - MailConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
			load();
		}
		return (MailConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void load() {
		load(instances, MailConfig.class, CONFIG_NAME);
		MailConfig.lastLoadTime = System.currentTimeMillis();
	}
	private static void debug(){
	}
	public static MailConfig register(String instance, DataRow row){
		MailConfig config = parse(MailConfig.class, instance, row, instances, compatibles);
		MailUtil.getInstance(instance);
		Pop3Util.getInstance(instance);
		return config;
	}
	public static MailConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
 
