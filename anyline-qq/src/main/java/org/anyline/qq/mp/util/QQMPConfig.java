package org.anyline.qq.mp.util; 
 
import java.util.Hashtable; 
 
import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
 
public class QQMPConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	/** 
	 * 服务号相关信息 
	 */ 
	public String APP_ID = ""				; //AppID(应用ID) 
	public String API_KEY = ""				; //APPKEY(应用密钥) 
	public String OAUTH_REDIRECT_URL		; //登录成功回调URL 
	static{ 
		init(); 
		debug(); 
	} 
	public static void init() { 
		//加载配置文件 
		loadConfig(); 
	} 
 
	public static QQMPConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static QQMPConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - QQMPConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			loadConfig(); 
		} 
		return (QQMPConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void loadConfig() { 
		loadConfig(instances, QQMPConfig.class, "anyline-qq-mp.xml"); 
		QQMPConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
