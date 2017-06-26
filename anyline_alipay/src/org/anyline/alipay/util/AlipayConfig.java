package org.anyline.alipay.util;

import java.io.File;
import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;


public class AlipayConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	public String APP_PRIVATE_KEY = "";
	public String ALIPAY_PUBLIC_KEY = "";
	public String APP_ID = "";
	public String DATA_FORMAT = "json";
	public String ENCODE = "utf-8";
	public String SIGN_TYPE = "RSA";
	public String RETURN_URL= "";
	public String NOTIFY_URL= "";
	static{
		init();
		debug();
	}
	public static AlipayConfig getInstance(){
		return getInstance("default");
	}
	public static AlipayConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (AlipayConfig)instances.get(key);
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		try {

			File file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline-alipay.xml");
			parseFile(AlipayConfig.class, file, instances);
			
			file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline/anyline-alipay.xml");
			parseFile(AlipayConfig.class, file, instances);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	public String getString(String key){
		return kvs.get(key);
	}
	private static void debug(){
	}
}
