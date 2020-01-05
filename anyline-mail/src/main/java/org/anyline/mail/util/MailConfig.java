package org.anyline.mail.util; 
 
import java.io.File; 
import java.util.Hashtable; 
 
import org.anyline.util.AnylineConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
 
public class MailConfig extends AnylineConfig{ 
	 
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>(); 
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
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(MailConfig.class, content, instances ,compatibles); 
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		//加载默认配置文件 
		load(); 
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
			load(); 
		} 
		return (MailConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, MailConfig.class, "anyline-mail.xml"); 
		MailConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
 
} 
 
