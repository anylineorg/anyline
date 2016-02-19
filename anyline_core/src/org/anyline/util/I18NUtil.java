

package org.anyline.util;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class I18NUtil {
	static Logger LOG = Logger.getLogger(I18NUtil.class);
	public static final String defaultLang = "cn";
	private static Map<String,Map<String,String>> messages;
	
	static{
		init();
	}
	@SuppressWarnings("unchecked")
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
				LOG.error(e);
			}catch(Exception e){
				LOG.error(e);
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
		String value = map.get(key);
		return value;
	}
}
