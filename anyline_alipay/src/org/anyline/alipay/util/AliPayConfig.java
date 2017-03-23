package org.anyline.alipay.util;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class AliPayConfig {
	private static Logger log = Logger.getLogger(AliPayConfig.class);

	public static String APP_PRIVATE_KEY = "";
	public static String ALIPAY_PUBLIC_KEY = "";
	public static String APP_ID = "";
	public static String DATA_FORMAT = "json";
	public static String ENCODE = "utf-8";
	public static String SIGN_TYPE = "RSA";
	public static String NOTIFY_URL= "";
	
	static{
		init();
		debug();
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
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("APP_PRIVATE_KEY".equalsIgnoreCase(key)){
					AliPayConfig.APP_PRIVATE_KEY = value;
				}else if("ALIPAY_PUBLIC_KEY".equalsIgnoreCase(key)){
					AliPayConfig.ALIPAY_PUBLIC_KEY = value;
				}else if("APP_ID".equalsIgnoreCase(key)){
					AliPayConfig.APP_ID = value;
				}else if("DATA_FORMAT".equalsIgnoreCase(key)){
					AliPayConfig.DATA_FORMAT = value;
				}else if("ENCODE".equalsIgnoreCase(key)){
					AliPayConfig.ENCODE = value;
				}else if("SIGN_TYPE".equalsIgnoreCase(key)){
					AliPayConfig.SIGN_TYPE = value;
				}else if("NOTIFY_URL".equalsIgnoreCase(key)){
					AliPayConfig.NOTIFY_URL = value;
				}
				if(ConfigTable.isDebug()){
					log.info("[解析支付宝配置文件] [" + key + " = " + value+"]");
				}
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
}
