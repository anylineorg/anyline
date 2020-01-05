package org.anyline.aliyun.oss.util; 
 
import java.io.File; 
import java.util.Hashtable; 
 
import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
public class OSSConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	private static File configDir; 
	public String ACCESS_ID		= ""; 
	public String ACCESS_SECRET = ""; 
	public String ENDPOINT		= ""; 
	public String BUCKET		= ""; 
	public String DIR			= ""; 
	public int EXPIRE_SECOND 	= 3600; 
	 
	static{ 
		init(); 
		debug(); 
	} 
	public static void init() { 
		//加载配置文件 
		loadConfig(); 
	} 
	public static void setConfigDir(File dir){ 
		configDir = dir; 
		init(); 
	} 
	public static OSSConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static OSSConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - OSSConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			loadConfig(); 
		} 
		return (OSSConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void loadConfig() { 
		loadConfig(instances, OSSConfig.class, "anyline-aliyun-oss.xml"); 
		OSSConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
