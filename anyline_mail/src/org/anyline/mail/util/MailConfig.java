package org.anyline.mail.util;

import java.io.File;
import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;

public class MailConfig extends BasicConfig{
	
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	private static File configDir;
	public String ACCOUNT;
	public String PASSWORD;
	public String USERNAME;
	public String PROTOCOL = "smtp";
	public String HOST;
	public String PORT;
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
		return (MailConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, MailConfig.class, "anyline-mail.xml");
	}
	private static void debug(){
	}

}

