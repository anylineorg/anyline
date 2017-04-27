package org.anyline.jpush.util;

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

public class JPushConfig {
	private static Logger log = Logger.getLogger(JPushConfig.class);
	private static Hashtable<String, JPushConfig> instances = new Hashtable<String,JPushConfig>();
	private Map<String,String> kvs = new HashMap<String,String>();
	
	public String APP_KEY ="";
	public String MASTER_SECRET ="";
		
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	public static JPushConfig getInstance(){
		return instances.get("default");
	}
	public static JPushConfig getInstance(String key){
		return instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		try {


			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-jpush.xml".equals(file.getName())){
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
				log.info("[加载极光推送配置文件] [file:" + file.getName() + "]");
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrConfig=root.elementIterator("config"); itrConfig.hasNext();){
				JPushConfig config = new JPushConfig();
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
					log.info("[解析极光推送配置文件][key = " + configKey + "] [" + key + " = " + value+"]");
					kvs.put(key, value);
				}
				config.kvs = kvs;
				config.setFieldValue();
				instances.put(configKey, config);
				//设置属性值
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
	public String getString(String key){
		return kvs.get(key);
	}
	private static void debug(){
	}
	public static void main(String args[]){
		debug();
	}
}