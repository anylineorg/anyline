package org.anyline.mail.util; 
 
import java.io.File; 
import java.util.Hashtable; 
 
import org.anyline.util.BasicConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
public class MailConfig extends BasicConfig{ 
	 
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>(); 
	private static File configDir; 
	public String ACCOUNT; 
	public String PASSWORD; 
	public String USERNAME; 
	public String PROTOCOL = "smtp"; 
	public String HOST; 
	public String PORT; 
	public String ATTACHMENT_DIR = "";	//附件下载地址 
	public boolean AUTO_DOWNLOAD_ATTACHMENT = true; 
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
	public static MailConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static MailConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - MailConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			loadConfig(); 
		} 
		return (MailConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void loadConfig() { 
		loadConfig(instances, MailConfig.class, "anyline-mail.xml"); 
		MailConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
 
} 
 
