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
 *
 *          
 */


package org.anyline.util;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class I18NUtil {
	static final Logger log = LoggerFactory.getLogger(I18NUtil.class);
	public static final String defaultLang = "cn";
	private static Map<String,Map<String,String>> messages;
	
	static{
		init();
	}
	private static void init(){
		List<File> files = FileUtil.getAllChildrenFile(new File(ConfigTable.getWebRoot(),ConfigTable.get("I18N_MESSAGE_DIR")), "xml");
		messages = new Hashtable<String,Map<String,String>>();
		SAXReader reader = new SAXReader();
		for(File file:files){
			try {
				Document document = reader.read(file);
				Element root = document.getRootElement();
				for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
					Element propertyElement = itrProperty.next();
					String key = propertyElement.attributeValue("key");
					for(Iterator<Element> itrItem=propertyElement.elementIterator("item"); itrItem.hasNext();){
						Element itemElement = itrItem.next();
						String lang = itemElement.attributeValue("lang");
						String value = itemElement.getTextTrim();
						Map<String,String> map = messages.get(lang);
						if(null == map){
							map = new Hashtable<String,String>();
							messages.put(lang, map);
						}
						map.put(key, value);
					}
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @param lang 语言
	 * @param key 
	 * @return
	 */
	public static String get(String lang, String key){
		Map<String,String> map = messages.get(lang);
		String value = null;
		if(null != map){
			value = map.get(key);
		}
		return value;
	}
}
