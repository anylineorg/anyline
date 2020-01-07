package org.anyline.easemob.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class EasemobConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-easemob.xml";
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
		parse(EasemobConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
	public static EasemobConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static EasemobConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - EasemobConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		} 
		return (EasemobConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, EasemobConfig.class, CONFIG_NAME);
		EasemobConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	 
	private static void debug(){ 
	} 
} 
