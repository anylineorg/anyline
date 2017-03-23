package org.anyline.sms;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SMSConfig {
	private static Logger log = Logger.getLogger(SMSConfig.class);
	//服务器配置
	public static String ACCESS_KEY= "";
	public static String ACCESS_SECRET = "";
	public static String SMS_SIGN = "";
	//客户端配置
	public static String SMS_SERVER = "";
	public static String CLIENT_APP= "";
	public static String CLIENT_SECRET= "";
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

			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-sms.xml".equals(file.getName())){
					loadConfig(file);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void loadConfig(File file){
		try{
			if(ConfigTable.isDebug()){
				log.info("[加SMS配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				if("ACCESS_KEY".equalsIgnoreCase(key)){
					ACCESS_KEY = value;
				}else if("ACCESS_SECRET".equalsIgnoreCase(key)){
					ACCESS_SECRET = value;
				}else if("SMS_SIGN".equalsIgnoreCase(key)){
					SMS_SIGN = value;
				}else if("SMS_SERVER".equalsIgnoreCase(key)){
					SMS_SERVER = value;
				}else if("CLIENT_APP".equalsIgnoreCase(key)){
					CLIENT_APP = value;
				}else if("CLIENT_SECRET".equalsIgnoreCase(key)){
					CLIENT_SECRET = value;
				}
				
				if(ConfigTable.isDebug()){
					log.info("[解析SMS配置文件] [" + key + " = " + value+"]");
				}
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
