package org.anyline.sms;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SMSConfig {
	private static Logger log = Logger.getLogger(SMSConfig.class);
	private static Hashtable<String, SMSConfig> instances = new Hashtable<String,SMSConfig>();
	private Map<String,String> kvs = new HashMap<String,String>();
	
	//服务器配置
	public String ACCESS_KEY= "";
	public String ACCESS_SECRET = "";
	public String SMS_SIGN = "";
	//客户端配置
	public String SMS_SERVER = "";
	public String CLIENT_APP= "";
	public String CLIENT_SECRET= "";
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static SMSConfig getInstance(){
		return instances.get("default");
	}
	public static SMSConfig getInstance(String key){
		return instances.get(key);
	}
	public String getString(String key){
		return kvs.get(key);
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

			for(Iterator<Element> itrConfig=root.elementIterator("config"); itrConfig.hasNext();){
				SMSConfig config = new SMSConfig();
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
					if(ConfigTable.isDebug()){
						log.info("[解析SMS配置文件][key = "  + key + "][" + key + " = " + value+"]");
					}
					kvs.put(key, value);
				}
				config.kvs = kvs;
				config.setFieldValue();
				instances.put(configKey, config);
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}

	private void setFieldValue(){
		Field[] fields = this.getClass().getDeclaredFields();
		for(Field field:fields){
			if(field.getType().getName().equals("java.lang.String")){
				String name = field.getName();
				try {
					String value = kvs.get(name);
					if(BasicUtil.isNotEmpty(value)){
						field.set(this, kvs.get(name));
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static void debug(){
	}
}
