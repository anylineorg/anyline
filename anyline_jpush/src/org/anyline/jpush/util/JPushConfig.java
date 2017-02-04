package org.anyline.jpush.util;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class JPushConfig {
	private static Logger log = Logger.getLogger(JPushConfig.class);
	public static String APP_KEY ="";
	public static String MASTER_SECRET ="";
		
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
	 */
	private synchronized static void loadConfig() {
		try {

			File file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline-jpush.xml");
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载极光推送配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("APP_KEY".equalsIgnoreCase(key)){
					JPushConfig.APP_KEY = value;
				}else if("MASTER_SECRET".equalsIgnoreCase(key)){
					JPushConfig.MASTER_SECRET = value;
				}
				if(ConfigTable.isDebug()){
					log.info("[解析极光推送配置文件] [" + key + " = " + value+"]");
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