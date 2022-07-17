package org.anyline.mimio.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.File;
import java.util.Hashtable;

public class MinioConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir;

	public static String DEFAULT_ACCESS_KEY		= ""	;
	public static String DEFAULT_ACCESS_SECRET	= ""	;
	public static String DEFAULT_ENDPOINT		= ""	;
	public static String DEFAULT_BUCKET			= ""	;
	public static int DEFAULT_PART_SIZE			= 1		;
	public static String DEFAULT_DIR			= ""	;
	public static int DEFAULT_EXPIRE_SECOND 	= 3600	;


	public String ACCESS_KEY	= DEFAULT_ACCESS_KEY	;
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET	;
	public String ENDPOINT		= DEFAULT_ENDPOINT		;
	public String BUCKET		= DEFAULT_BUCKET		;
	public int PART_SIZE		= DEFAULT_PART_SIZE		;
	public String DIR			= DEFAULT_DIR			;
	public int EXPIRE_SECOND 	= DEFAULT_EXPIRE_SECOND	;



	public static String CONFIG_NAME = "anyline-minio.xml";




	static{ 
		init(); 
		debug(); 
	} 
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(MinioConfig.class, content, instances ,compatibles); 
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
	public static MinioConfig getInstance(){ 
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static MinioConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - MinioConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			load(); 
		} 
		return (MinioConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() { 
		load(instances, MinioConfig.class, CONFIG_NAME);
		MinioConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
