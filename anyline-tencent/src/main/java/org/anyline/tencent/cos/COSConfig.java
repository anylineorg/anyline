package org.anyline.tencent.cos; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class COSConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir; 
	public String ACCESS_ID		= ""; 
	public String ACCESS_SECRET = ""; 
	public String ENDPOINT		= ""; 
	public String BUCKET		= ""; 
	public String DIR			= ""; 
	public int EXPIRE_SECOND 	= 3600;
	public static String CONFIG_NAME = "anyline-tencent-cos.xml";
	 
	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(COSConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
	public static void setConfigDir(File dir){ 
		configDir = dir; 
		init(); 
	} 
	public static COSConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static COSConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - COSConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		} 
		return (COSConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() { 
		load(instances, COSConfig.class, CONFIG_NAME);
		COSConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
