package org.anyline.alipay.util;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class AlipayConfig {
	private static Logger log = Logger.getLogger(AlipayConfig.class);
	private static Hashtable<String,AlipayConfig> instances = new Hashtable<String,AlipayConfig>();
	private Map<String,String> kvs = new HashMap<String,String>();
//	public static String APP_PRIVATE_KEY = "";
//	public static String ALIPAY_PUBLIC_KEY = "";
//	public static String APP_ID = "";
//	public static String DATA_FORMAT = "json";
//	public static String ENCODE = "utf-8";
//	public static String SIGN_TYPE = "RSA";
//	public static String NOTIFY_URL= "";
	
	static{
		init();
		debug();
	}
	public static AlipayConfig getInstance(String key){
		return instances.get(key);
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
			loadConfig(file);
			
			file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline/anyline-alipay.xml");
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载支付宝配置文件] [file:" + file.getName() + "]");
			}
			if(!file.exists()){
				if(ConfigTable.isDebug()){
					log.info("[加载支付宝配置文件][文件不存在][file:" + file.getName() + "]");
				}
				return;
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrConfig=root.elementIterator("config"); itrConfig.hasNext();){
				AlipayConfig config = new AlipayConfig();
				Element configElement = itrConfig.next();
				String configKey = configElement.attributeValue("key");
				if(BasicUtil.isEmpty(configKey)){
					configKey = "default";
				}
				Map<String,String> kvs = new HashMap<String,String>();
				for(Iterator<Element> itrProperty=configElement.elementIterator("property"); itrProperty.hasNext();){
					Element propertyElement = itrProperty.next();
					String key = propertyElement.attributeValue("key");
					String value = propertyElement.getTextTrim();
					log.info("[解析支付宝配置文件][key = " + configKey + "] [" + key + " = " + value+"]");
					kvs.put(key, value);
				}
				config.kvs = kvs;
				instances.put(configKey, config);
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
	public static void main(String args[]){
		debug();
	}
	public String getString(String key){
		return kvs.get(key);
	}
}
