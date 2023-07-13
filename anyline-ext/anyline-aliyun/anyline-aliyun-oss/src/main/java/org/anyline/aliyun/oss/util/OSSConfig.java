package org.anyline.aliyun.oss.util; 
 
import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import java.io.File;
import java.util.Hashtable;
 
public class OSSConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir;

	public static String DEFAULT_ACCESS_ID		= ""	;
	public static String DEFAULT_ACCESS_SECRET 	= ""	;
	public static String DEFAULT_ENDPOINT		= ""	;
	public static String DEFAULT_BUCKET			= ""	;
	public static String DEFAULT_DIR			= ""	;
	public static int DEFAULT_EXPIRE_SECOND 	= 3600	;

	public String ACCESS_ID		= DEFAULT_ACCESS_ID		;
	public String ACCESS_SECRET = DEFAULT_ACCESS_SECRET	;
	public String ENDPOINT		= DEFAULT_ENDPOINT		;
	public String BUCKET		= DEFAULT_BUCKET		;
	public String DIR			= DEFAULT_DIR			;
	public int EXPIRE_SECOND 	= DEFAULT_EXPIRE_SECOND	;

	public static String CONFIG_NAME = "anyline-aliyun-oss.xml";


	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
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
		// 加载配置文件
		load(); 
	} 
	public static void setConfigDir(File dir){
		configDir = dir; 
		init(); 
	} 
	public static OSSConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static OSSConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - OSSConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
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


	public static OSSConfig register(String instance, String id, String secret, String endpoint, String bucket, String dir, int expire) {
		DataRow row = new DataRow();
		row.put("ACCESS_ID", id);
		row.put("ACCESS_SECRET", secret);
		row.put("ENDPOINT", endpoint);
		row.put("BUCKET", bucket);
		row.put("DIR", dir);
		row.put("EXPIRE_SECOND", expire);
		OSSConfig config = parse(OSSConfig.class, instance, row, instances, compatibles);
		OSSUtil.getInstance(instance);
		return config;
	}

	public static OSSConfig register(String instance, String secret, String endpoint, String bucket, String dir, int expire) {
		return register(DEFAULT_INSTANCE_KEY, instance, secret, endpoint, bucket, dir, expire);
	}

} 
