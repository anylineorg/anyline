package org.anyline.wechat.programe;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.util.WechatConfig;

import java.util.Hashtable;


public class WechatProgrameConfig extends WechatConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	public static String CONFIG_NAME = "anyline-wechat-programe.xml";
	 
	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(WechatProgrameConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
	public static WechatProgrameConfig getInstance(){
		return getInstance("default"); 
	} 
	public static WechatProgrameConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WechatProgrameConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载 
			load(); 
		} 
		return (WechatProgrameConfig)instances.get(key);
	} 
 
	public static WechatProgrameConfig reg(String key, DataRow row){
		return parse(WechatProgrameConfig.class, key, row, instances,compatibles);
	} 
	public static WechatProgrameConfig parse(String key, DataRow row){
		return parse(WechatProgrameConfig.class, key, row, instances,compatibles);
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
		load(instances, WechatProgrameConfig.class, CONFIG_NAME,compatibles);
		WechatProgrameConfig.lastLoadTime = System.currentTimeMillis();
	} 
	private static void debug(){ 
	} 
} 
