package org.anyline.easemob;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class EasemobConfig {
	private static Logger log = Logger.getLogger(EasemobConfig.class);
	public static String CLIENT_ID ="";
	public static String CLIENT_SECRET ="";
	public static String ORG_NAME ="";
	public static String APP_NAME ="";
		
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

			File file = new File(ConfigTable.getWebRoot() , "/WEB-INF/classes/anyline-easemob.xml");
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载环信配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("CLIENT_ID".equalsIgnoreCase(key)){
					EasemobConfig.CLIENT_ID = value;
				}else if("APP_SECRECT".equalsIgnoreCase(key)){
					EasemobConfig.CLIENT_SECRET = value;
				}else if("ORG_NAME".equalsIgnoreCase(key)){
					EasemobConfig.ORG_NAME = value;
				}else if("APP_NAME".equalsIgnoreCase(key)){
					EasemobConfig.APP_NAME = value;
				}
				if(ConfigTable.isDebug()){
					log.info("[解析环信配置文件] [" + key + " = " + value+"]");
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
