package org.anyline.jpush.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
public class JPushConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

	public static String DEFAULT_APP_KEY 		= "" ;
	public static String DEFAULT_MASTER_SECRET 	= "" ;

	public String APP_KEY 		= DEFAULT_APP_KEY		;
	public String MASTER_SECRET = DEFAULT_MASTER_SECRET	;


	public static String CONFIG_NAME = "anyline-jpush.xml";

	static{
		init();
		debug();
	}
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(JPushConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
 
	public static JPushConfig getInstance(){ 
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static JPushConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - JPushConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		} 
		return (JPushConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, JPushConfig.class, CONFIG_NAME);
		JPushConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
}
