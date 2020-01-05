package org.anyline.weixin.mp.util; 
 
import java.util.Hashtable;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.weixin.util.WXConfig;
 
 
public class WXMPConfig extends WXConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	 
	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(WXMPConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
	public static WXMPConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static WXMPConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WXMPConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		} 
		return (WXMPConfig)instances.get(key); 
	} 
 
	public static WXMPConfig reg(String key, DataRow row){ 
		return parse(WXMPConfig.class, key, row, instances,compatibles); 
	} 
	public static WXMPConfig parse(String key, DataRow row){ 
		return parse(WXMPConfig.class, key, row, instances,compatibles); 
	} 
	public static Hashtable<String,AnylineConfig> parse(String column, DataSet set){ 
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
	private synchronized static void load() { 
		load(instances, WXMPConfig.class, "anyline-weixin-mp.xml",compatibles); 
		WXMPConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
