package org.anyline.jpush.util; 
 
import java.util.Hashtable; 


import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
public class JPushConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	public String APP_KEY =""; 
	public String MASTER_SECRET =""; 
		 
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
		return getInstance("default"); 
	} 
	public static JPushConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
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
		load(instances, JPushConfig.class, "anyline-jpush.xml"); 
		JPushConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
}
