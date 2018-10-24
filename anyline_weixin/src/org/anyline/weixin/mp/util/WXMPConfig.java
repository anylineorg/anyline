package org.anyline.weixin.mp.util;

import java.util.Hashtable;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.weixin.util.WXConfig;


public class WXMPConfig extends WXConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	public static WXMPConfig getInstance(){
		return getInstance("default");
	}
	public static WXMPConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (WXMPConfig)instances.get(key);
	}

	public static WXMPConfig parse(String key, DataRow row){
		return parse(WXMPConfig.class, key, row, instances,compatibles);
	}
	public static Hashtable<String,BasicConfig> parse(String column, DataSet set){
		for(DataRow row:set){
			String key = row.getString(column);
			parse(key, row);
		}
		return instances;
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, WXMPConfig.class, "anyline-weixin-mp.xml",compatibles);
	}
	private static void debug(){
	}
}
