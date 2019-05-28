package org.anyline.amap.util;

import java.io.File;
import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

public class AmapConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	public static String KEY= "";
	public static String PRIVATE_KEY = "";
	public static String TABLE_ID = "";
	private static File configDir;
	
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	public static void setConfigDir(File dir){
		configDir = dir;
		init();
	}
	public static AmapConfig getInstance(){
		return getInstance("default");
	}
	public static AmapConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}

		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - AmapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载
			loadConfig();
		}
		return (AmapConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, AmapConfig.class, "anyline-amap.xml");
		AmapConfig.lastLoadTime = System.currentTimeMillis();
	}
	private static void debug(){
	}
}
