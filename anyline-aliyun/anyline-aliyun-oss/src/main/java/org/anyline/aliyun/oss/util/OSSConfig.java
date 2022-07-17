package org.anyline.aliyun.oss.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Hashtable;
 
public class OSSConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir;

	public static String DEFAULT_ACCESS_ID		= "";
	public static String DEFAULT_ACCESS_SECRET 	= "";
	public static String DEFAULT_ENDPOINT		= "";
	public static String DEFAULT_BUCKET			= "";
	public static String DEFAULT_DIR			= "";

	public String ACCESS_ID		= DEFAULT_ACCESS_ID		;
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET	;
	public String ENDPOINT		= DEFAULT_ENDPOINT		;
	public String BUCKET		= DEFAULT_BUCKET		;
	public String DIR			= DEFAULT_DIR			;
	public int EXPIRE_SECOND 	= 3600;
	public static String CONFIG_NAME = "anyline-aliyun-oss.xml";




	static{ 
		init(); 
		debug(); 
	}


	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(OSSConfig.class, content, instances ,compatibles); 
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
	public static OSSConfig getInstance(){ 
		return getInstance(DEFAULT_KEY); 
	} 
	public static OSSConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = DEFAULT_KEY; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - OSSConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		}
		OSSConfig instance = (OSSConfig)instances.get(key);
		return instance;
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() { 
		load(instances, OSSConfig.class, CONFIG_NAME);
		OSSConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
