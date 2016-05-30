/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.util;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class ConfigTable {
	private static Logger LOG = Logger.getLogger(ConfigTable.class);
	private static String webRoot;
	private static Hashtable<String,String> configs;
	
	static{
		init();
	}

	private ConfigTable() {}
	
	
	public static String getWebRoot() {
		return webRoot;
	}

	public static void init() {
		String path =  "";
		try{
			path = ConfigTable.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		}catch(Exception e){
			LOG.error(e);
		}
		Properties props=System.getProperties(); //获得系统属性集    
		String osName = props.getProperty("os.name"); //操作系统名称    
		if(null != osName && osName.toUpperCase().contains("WINDOWS") && path.startsWith("/")){
			path = path.substring(1);
		}
		webRoot = path.substring(0,path.indexOf("WEB-INF")-1);
		
		//加载配置文件
		loadConfig();
	}
	/**
	 * 加载配置文件
	 */
	@SuppressWarnings("unchecked")
	private static void loadConfig() {
		try {
			if(null == configs){
				configs = new Hashtable<String,String>();
			}
			configs.put("HOME_DIR", webRoot);
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(webRoot , "/WEB-INF/classes/anyline-config.xml"));
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				configs.put(key, value);
			}
		} catch (Exception e) {
			LOG.error("配置文件解析异常:"+e);
		}
	}
	public static String get(String key){
		return configs.get(key);
	}
	public static String getString(String key) {
		return get(key);
	}
	public static String getString(String key, String def){
		String val = getString(key);
		if(BasicUtil.isEmpty(val)){
			val = def;
		}
		return val;
	}
	public static boolean getBoolean(String key){
		return getBoolean(key,false);
	}
	public static boolean getBoolean(String key, boolean def){
		return BasicUtil.parseBoolean(get(key), def);
	}
	public static int getInt(String key) {
		return BasicUtil.parseInt(get(key),0);
	}
	public static int getInt(String key, int def){
		return BasicUtil.parseInt(get(key), def);
	}
	public static boolean isLocal(){
		String flag = getString("IS_LOCAL");
		boolean result = false;
		if(null == flag){
			 List<String>ips = BasicUtil.getLocalIpsAddress();
			 String localServerIp = ConfigTable.getString("LOCAL_IP_PREFIX");
			 for(String ip:ips){
				 if(ip.startsWith(localServerIp)){
					 	result = true;
						if(null == configs){
							configs = new Hashtable<String,String>();
						}
						configs.put("IS_LOCAL", "1");
					 break;
				 }
			 }
		}else{
			if("1".equals(flag)){
				result = true;
			}
		}
		return result;
	}
}
