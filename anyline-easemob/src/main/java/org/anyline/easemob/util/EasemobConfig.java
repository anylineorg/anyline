package org.anyline.easemob.util; 
 
import java.util.Hashtable; 
 
import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
 
public class EasemobConfig extends AnylineConfig{ 
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
	public static void init() { 
		//加载配置文件 
		loadConfig(); 
	} 
	public static EasemobConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static EasemobConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - EasemobConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			loadConfig(); 
		} 
		return (EasemobConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void loadConfig() { 
		loadConfig(instances, EasemobConfig.class, "anyline-easemob.xml"); 
		EasemobConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	 
	private static void debug(){ 
	} 
} 
