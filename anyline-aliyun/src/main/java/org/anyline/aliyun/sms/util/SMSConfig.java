package org.anyline.aliyun.sms.util; 
 
import java.util.Hashtable; 
 
import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
 
public class SMSConfig extends AnylineConfig{ 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
	//服务器配置 
	public String ACCESS_KEY= ""; 
	public String ACCESS_SECRET = ""; 
	public String SMS_SIGN = ""; 
	//客户端配置 
	public String SMS_SERVER = ""; 
	public String CLIENT_APP= ""; 
	public String CLIENT_SECRET= ""; 
	static{ 
		init(); 
		debug(); 
	} 
	public static void init() { 
		//加载配置文件 
		load(); 
	} 
 
	public static SMSConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static SMSConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		return (SMSConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, SMSConfig.class, "anyline-aliyun-sms.xml"); 
	} 
	private static void debug(){ 
	} 
} 
