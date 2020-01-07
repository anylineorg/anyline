package org.anyline.qq.mp.util; 
 
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class QQMPConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-qq-mp.xml";
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
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(QQMPConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载配置文件 
		load(); 
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
			load(); 
		} 
		return (QQMPConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 * 首先加载anyline-config.xml 
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置 
	 */ 
	private synchronized static void load() { 
		load(instances, QQMPConfig.class, CONFIG_NAME);
		QQMPConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
