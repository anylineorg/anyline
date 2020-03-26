package org.anyline.util; 
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
 
public abstract class AnylineConfig implements Serializable {
	protected static long lastLoadTime 	= 0;	//最后一次加载时间 
	protected static final Logger log = LoggerFactory.getLogger(AnylineConfig.class); 
	protected Map<String, String> kvs = new HashMap<String, String>(); 
	protected static String[] compatibles = {};
	protected void afterParse(String key, String value){ 
		 
	} 
	public static void parse(String content){
	}
	/**
	 * 加载配置文件
	 * @param instances 配置实例
	 * @param clazz 子类
	 * @param fileName 文件名
	 * @param compatibles 兼容配置
	 */
	protected synchronized static void load(Hashtable<String,AnylineConfig> instances, Class<? extends AnylineConfig> clazz, String fileName, String ... compatibles) { 
		try { 

			if("jar".equals(ConfigTable.getPackageType())){
				log.warn("[加载配置文件][type:jar][file:{}]",fileName);
				InputStream in = ConfigTable.class.getClassLoader().getResourceAsStream("/"+fileName);
				String txt = FileUtil.read(in, "UTF-8").toString();
				parse(txt);
			}else{
				File dir = new File(ConfigTable.getWebRoot(), "WEB-INF/classes"); 
				if(null != dir &&  !dir.exists()){ 
					dir = new File(ConfigTable.getWebRoot()); 
				} 
				List<File> files = FileUtil.getAllChildrenFile(dir, "xml"); 
				int configSize = 0; 
				for(File file:files){ 
					if(fileName.equals(file.getName())){ 
						parse(clazz, file, instances,compatibles); 
						configSize ++; 
					} 
				} 
				if(configSize ==0){ 
					log.warn("[解析配置文件][未加载配置文件:{}][配置文件模板请参考:http://api.anyline.org/config或源文件中src/main/resources/{}]",fileName,fileName); 
				} 
			}
		} catch (Exception e) { 
			log.error("[解析配置文件][file:{}][配置文件解析异常:{}]",fileName,e); 
		} 
	} 
	
	@SuppressWarnings("unchecked") 
	public static <T extends AnylineConfig> T parse(Class<? extends AnylineConfig> T, String key, DataRow row, Hashtable<String, AnylineConfig> instances, String... compatibles) { 
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
						log.info("[解析配置文件][{}={}]",nm,value); 
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
								log.warn("[解析配置文件][版本兼容][laster key:{}][old key:{}][value:{}]",newKey,oldKey,val); 
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
	public static Hashtable<String, AnylineConfig> parse(Class<? extends AnylineConfig> T, String column, DataSet set, Hashtable<String, AnylineConfig> instances, String... compatibles) { 
		for (DataRow row : set) { 
			String key = row.getString(column); 
			parse(T, key, row, instances, compatibles); 
		} 
		return instances; 
	} 
	private static Hashtable<String, AnylineConfig> parse(Class<?> T, Document document, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		try { 
			Element root = document.getRootElement(); 
			for (Iterator<Element> itrConfig = root.elementIterator("config"); itrConfig.hasNext();) { 
				Element configElement = itrConfig.next(); 
				String configKey = configElement.attributeValue("key"); 
				if (BasicUtil.isEmpty(configKey)) { 
					configKey = "default"; 
				} 
 
				AnylineConfig config = instances.get(configKey); 
				if(null == config){ 
					config = (AnylineConfig) T.newInstance(); 
				} 
				Map<String, String> kvs = new HashMap<String, String>(); 
				Iterator<Element> elements = configElement.elementIterator("property"); 
				while (elements.hasNext()) { 
					Element element = elements.next(); 
					String key = element.attributeValue("key"); 
					String value = element.getTextTrim(); 
					log.info("[解析配置文件][key:{}][{}={}]", configKey, key,value); 
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
									log.warn("[解析配置文件][版本兼容][laster key:{}][old key:{}][value:{}]", newKey, oldKey, val); 
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
	protected static Hashtable<String, AnylineConfig> parse(Class<?> T, String content, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		try { 
			Document document = DocumentHelper.parseText(content); 
			instances = parse(T, document, instances, compatibles);
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return instances; 
	}
	//兼容上一版本 最后一版key:倒数第二版key:倒数第三版key 
	protected static Hashtable<String, AnylineConfig> parse(Class<?> T, File file, Hashtable<String, AnylineConfig> instances, String... compatibles) { 
		log.warn("[解析配置文件][file:{}]",file); 
		if (null == file || !file.exists()) { 
			log.warn("[解析配置文件][文件不存在][file:{}]",file); 
			return instances; 
		} 
		SAXReader reader = new SAXReader(); 
		try { 
			Document document = reader.read(file); 
			instances = parse(T, document, instances, compatibles);
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
