package org.anyline.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BasicConfig {
	protected static Logger log = Logger.getLogger(BasicConfig.class);
	protected Map<String,String> kvs = new HashMap<String,String>();
	protected static Hashtable<String,BasicConfig> parseFile(Class T, File file, Hashtable<String,BasicConfig> instances){
		SAXReader reader = new SAXReader();
		try{
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrConfig=root.elementIterator("config"); itrConfig.hasNext();){
				BasicConfig config = (BasicConfig)T.newInstance();
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
					log.info("[解析配置文件][file="+file.getName()+"][key = " + configKey + "] [" + key + " = " + value+"]");
					kvs.put(key, value);
					config.setValue(key, value);
				}
				config.kvs = kvs;
				instances.put(configKey, config);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return instances;
	}
//	public String getString(String key){
//		return kvs.get(key);
//	}
	private void setValue(String key, String value){
		try{
			Field field = this.getClass().getDeclaredField(key);
			this.setValue(field, value);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void setValue(Field field, String value){
		if(null != field){
			try{
				if(field.isAccessible()){
					field.set(this, value);
				}else{
					field.setAccessible(true);
					field.set(this, value);
					field.setAccessible(false);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
