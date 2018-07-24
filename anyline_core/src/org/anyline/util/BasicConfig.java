package org.anyline.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
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
		if(null == file || !file.exists()){
			log.warn("[解析配置文件][文件不存在][file="+file.getName()+"]");
			return instances;
		}
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
	protected void setValue(String key, String value){
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
				Object val = value;
				Type type = field.getGenericType();
				String typeName = type.getTypeName();
				if(typeName.contains("int") || typeName.contains("Integer")){
					val = BasicUtil.parseInt(value, 0);
				}else if(typeName.contains("boolean") || typeName.contains("Boolean")){
					val = BasicUtil.parseBoolean(value);
				}
				if(field.isAccessible()){
					field.set(this, val);
				}else{
					field.setAccessible(true);
					field.set(this, val);
					field.setAccessible(false);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
