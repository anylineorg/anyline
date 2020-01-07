package org.anyline.nacos.util;

import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;

public class NacosConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	public String KEY = "default";
	public String ADDRESS;
	public int PORT = 8848;
	public int TIMEOUT = 5000;
	public String NAMESAPCE = "public";
	public String GROUP = "DEFAULT_GROUP";
	public boolean AUTO_SCAN = true;
	static{
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(NacosConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load();
	} 
 
	public static NacosConfig getInstance(){
		return getInstance("default"); 
	} 
	public static NacosConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - NacosConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载 
			load(); 
		}
		NacosConfig config = (NacosConfig)instances.get(key);
		return config;
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, NacosConfig.class, "anyline-nacos.xml");
		NacosConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){ 
	} 
}
