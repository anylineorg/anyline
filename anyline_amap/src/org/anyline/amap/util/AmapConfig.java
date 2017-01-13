package org.anyline.amap.util;

import java.io.File;
import java.util.Iterator;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class AmapConfig {
	private static Logger log = Logger.getLogger(AmapConfig.class);

	public static String KEY= "";
	public static String PRIVATE_KEY = "";
	public static String TABLE_ID = "";
	
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
			String filePath = ConfigTable.getString("AMAP_CONFIG_FILE");
			if(BasicUtil.isEmpty(filePath)){
				filePath = ConfigTable.getWebRoot() + "/WEB-INF/classes/anyline-amap.xml";
			}
			File file = new File(filePath);
			loadConfig(file);
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加载高德云图配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("KEY".equalsIgnoreCase(key)){
					KEY = value;
				}else if("PRIVATE_KEY".equalsIgnoreCase(key)){
					PRIVATE_KEY = value;
				}else if("TABLE_ID".equalsIgnoreCase(key)){
					TABLE_ID = value;
				}
				
				if(ConfigTable.isDebug()){
					log.info("[解析高德云图配置文件] [" + key + " = " + value+"]");
				}
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
