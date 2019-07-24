package org.anyline.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public abstract class BasicConfig {
	protected static long lastLoadTime 	= 0;	//最后一次加载时间
	protected static final Logger log = Logger.getLogger(BasicConfig.class);
	protected Map<String, String> kvs = new HashMap<String, String>();
	
	protected void afterParse(String key, String value){
		
	}
	
	protected synchronized static void loadConfig(Hashtable<String,BasicConfig> instances, Class<? extends BasicConfig> clazz, String fileName, String ... compatibles) {
		try {
			File dir = new File(ConfigTable.getWebRoot(), "WEB-INF/classes");
			if(null != dir &&  !dir.exists()){
				dir = new File(ConfigTable.getWebRoot());
			}
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			int configSize = 0;
			for(File file:files){
				if(fileName.equals(file.getName())){
					parseFile(clazz, file, instances,compatibles);
					configSize ++;
				}
			}
			if(configSize ==0){
				log.warn("[解析配置文件][未加载配置文件:"+fileName+"][配置文件模板请参考:http://api.anyline.org/config或源文件中src/main/resources/"+fileName+"]");
			}
			
		} catch (Exception e) {
			log.error("[解析配置文件][file:"+fileName+"][配置文件解析异常:"+e+"]");
		}
	}
	@SuppressWarnings("unchecked")
	public static <T extends BasicConfig> T parse(Class<? extends BasicConfig> T, String key, DataRow row, Hashtable<String, BasicConfig> instances, String... compatibles) {
		T config = null;
		try {
			config = (T) T.newInstance();
			List<Field> fields = BeanUtil.getFields(T);
			Map<String, String> kvs = new HashMap<String, String>();
			config.kvs = kvs;
			for (Field field : fields) {
				String nm = field.getName();
				if (!Modifier.isFinal(field.getModifiers()) 
						&& !Modifier.isPrivate(field.getModifiers()) 
						&& !Modifier.isStatic(field.getModifiers())
						&& "String".equals(field.getType().getSimpleName()) 
						&& nm.equals(nm.toUpperCase())) {
					try {
						String value = row.getString(nm);
						config.setValue(nm, value);
						log.info("[解析配置文件][" + nm + " = " + value + "]");
						kvs.put(nm, value);
						config.afterParse(nm, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// 兼容旧版本
			if (null != compatibles) {
				for (String compatible : compatibles) {
					String[] keys = compatible.split(":");
					if (keys.length > 1) {
						String newKey = keys[0];
						for (int i = 1; i < keys.length; i++) {
							String oldKey = keys[i];
							if (kvs.containsKey(newKey)) {
								break;
							}
							if (row.containsKey(oldKey)) {
								String val = row.getString(oldKey);
								kvs.put(newKey, val);
								config.setValue(newKey, val);
								config.afterParse(newKey, val);
								log.warn("[解析配置文件][版本兼容][laster key:" + newKey + "][old key:" + oldKey + ":" + val + "]");
							}
						}
					}
				}
			}
			instances.put(key, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config;
	}

	public static Hashtable<String, BasicConfig> parse(Class<? extends BasicConfig> T, String column, DataSet set, Hashtable<String, BasicConfig> instances, String... compatibles) {
		for (DataRow row : set) {
			String key = row.getString(column);
			parse(T, key, row, instances, compatibles);
		}
		return instances;
	}

	/**
	 * 解析配置文件
	 * 
	 * @param T
	 * @param file
	 * @param instances
	 * @param compatible
	 *            兼容上一版本 最后一版key:倒数第二版key:倒数第三版key
	 * @return
	 */
	protected static Hashtable<String, BasicConfig> parseFile(Class<?> T, File file, Hashtable<String, BasicConfig> instances, String... compatibles) {
		if (null == file || !file.exists()) {
			log.warn("[解析配置文件][文件不存在][file=" + file.getName() + "]");
			return instances;
		}
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for (Iterator<Element> itrConfig = root.elementIterator("config"); itrConfig.hasNext();) {
				Element configElement = itrConfig.next();
				String configKey = configElement.attributeValue("key");
				if (BasicUtil.isEmpty(configKey)) {
					configKey = "default";
				}

				BasicConfig config = instances.get(configKey);
				if(null == config){
					config = (BasicConfig) T.newInstance();
				}
				Map<String, String> kvs = new HashMap<String, String>();
				Iterator<Element> elements = configElement.elementIterator("property");
				while (elements.hasNext()) {
					Element element = elements.next();
					String key = element.attributeValue("key");
					String value = element.getTextTrim();
					log.info("[解析配置文件][file=" + file.getName() + "][key = " + configKey + "] [" + key + " = " + value + "]");
					kvs.put(key, value);
					config.setValue(key, value);
				}
				// 兼容旧版本
				if (null != compatibles) {
					for (String compatible : compatibles) {
						String[] keys = compatible.split(":");
						if (keys.length > 1) {
							String newKey = keys[0];
							for (int i = 1; i < keys.length; i++) {
								String oldKey = keys[i];
								if (kvs.containsKey(newKey)) {
									break;
								}
								Element element = configElement.element(oldKey);
								if (null != element) {
									String val = element.getTextTrim();
									kvs.put(newKey, val);
									log.warn("[解析配置文件][版本兼容][laster key:" + newKey + "][old key:" + oldKey + ":" + val + "]");
								}
							}
						}
					}
				}
				//加载时间
				config.kvs = kvs;
				instances.put(configKey, config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instances;
	}

	protected void setValue(String key, String value) {
		Field field = null;
		for (Class<?> clazz = this.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				field = clazz.getDeclaredField(key);
				if (null != field) {
					this.setValue(field, value);
					break;
				}
			} catch (Exception e) {

			}
		}
	}

	private void setValue(Field field, String value) {
		if (null != field) {

			try {
				Object val = value;
				Type type = field.getGenericType();
				String typeName = type.getTypeName();
				if (typeName.contains("int") || typeName.contains("Integer")) {
					val = BasicUtil.parseInt(value, 0);
				} else if (typeName.contains("boolean") || typeName.contains("Boolean")) {
					val = BasicUtil.parseBoolean(value);
				}
				if (field.isAccessible()) {
					field.set(this, val);
				} else {
					field.setAccessible(true);
					field.set(this, val);
					field.setAccessible(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
