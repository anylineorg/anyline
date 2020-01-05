package org.anyline.redis.util; 
 
import java.io.File; 
import java.util.Hashtable; 
import java.util.List; 


import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.util.FileUtil; 
 
 
public class RedisConfig extends AnylineConfig{ 
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
		parse(RedisConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		loadConfig(); 
	} 
	public static RedisConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static RedisConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		return (RedisConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void loadConfig() { 
		try { 
 
			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes"); 
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml"); 
			for(File file:files){ 
				if("anyline-Redis.xml".equals(file.getName())){ 
					parse(RedisConfig.class, file, instances); 
				} 
			} 
			 
		} catch (Exception e) { 
			log.error("[配置文件解析异常][msg:{}]",e); 
		} 
	} 
	 
	private static void debug(){} 
} 
