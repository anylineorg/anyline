package org.anyline.nacos.util;

import org.anyline.entity.DataRow;
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
	public String NAMESPACE = "public";
	public String GROUP = "DEFAULT_GROUP";
	public boolean AUTO_SCAN = true;
	public String SCAN_PACKAGE="org.anyline,org.anyboot";
	public String SCAN_CLASS="";
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
	public static NacosConfig register(String id, String address, int port, String group, String namespace, boolean auto, String pack, String clazz) {
		DataRow row = new DataRow();
		row.put("ADDRESS", address);
		row.put("PORT", port);
		row.put("GROUP", group);
		row.put("NAMESPACE", namespace);
		row.put("AUTO_SCAN", auto);
		row.put("SCAN_PACKAGE", pack);
		row.put("SCAN_CLASS", clazz);
		NacosConfig config = parse(NacosConfig.class, id, row, instances, compatibles);
		NacosUtil util = NacosUtil.getInstance(id);
		if(null != util) {
			util.scan();
		}
		return config;
	}

	public static NacosConfig register(String id, String address, int port, String group, String namespace) {
		return register(id, address, port, group, namespace, true, null, null);
	}
	public static NacosConfig register(String id, String address, int port) {
		return register(id, address, port, "DEAULT_GROUP", "public", true, null, null);
	}
	public static NacosConfig register(String address, int port) {
		return register("default", address, port, "DEAULT_GROUP", "public", true, null, null);
	}
	private static void debug(){ 
	} 
}
