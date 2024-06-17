/*
 * Copyright 2006-2023 www.anyline.org
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class AnylineConfig {
	protected static long lastLoadTime = 0;    // 最后一次加载时间
	protected static final Logger log = LoggerFactory.getLogger(AnylineConfig.class);
	protected Map<String, String> kvs = new HashMap<>();
	protected static String[] compatibles = {};

	public static final String DEFAULT_INSTANCE_KEY = "default";
	public String INSTANCE_KEY							= "";
	protected void afterParse(String key, String value) {

	}

	public static void parse(String content) {
	}

	/**
	 * 加载配置文件
	 *
	 * @param instances   配置实例
	 * @param clazz       子类
	 * @param fileName    文件名
	 * @param compatibles 兼容配置
	 */
	protected synchronized static void load(Hashtable<String, AnylineConfig> instances, Class<? extends AnylineConfig> clazz, String fileName, String... compatibles) {
		try {

			int configSize = 0;
			if ("jar".equals(ConfigTable.getProjectProtocol())) {
				InputStream in = null;
				log.info("[加载配置文件][type:jar][file:{}]", fileName);
				if (FileUtil.getPathType(AnylineConfig.class) == 0) {
					// 遍历jar
					JarFile jar = new JarFile(System.getProperty("java.class.path"));
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.endsWith(fileName)) {
							in = jar.getInputStream(entry);
							parse(clazz, in, instances, compatibles);
							configSize++;
						}
					}
				} else {
					in = ConfigTable.class.getClassLoader().getResourceAsStream("/" + fileName);
					String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
					parse(txt);
				}
				// 加载同目录下config目录
				File dir = new File(FileUtil.merge(ConfigTable.getRoot(), "config"));
				if (null != dir && !dir.exists()) {
					dir = new File(ConfigTable.getWebRoot());
				}
				List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
				for (File file : files) {
					if (fileName.equals(file.getName())) {
						parse(clazz, file, instances, compatibles);
						configSize++;
					}
				}
			} else {
				// File dir = new File(ConfigTable.getWebRoot(), "WEB-INF/classes");
				File dir = new File(ConfigTable.getClassPath());
				if (!dir.exists()) {
					dir = new File(ConfigTable.getWebRoot());
				}
				List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
				for (File file : files) {
					if (fileName.equals(file.getName())) {
						parse(clazz, file, instances, compatibles);
						configSize++;
					}
				}
			}
			log.info("[解析配置文件][文件:{}][数量:{}/{}][请参考:http://doc.anyline.org或源码中resources/{}]", fileName, configSize, instances.size(), fileName);

		} catch (Exception e) {
			log.error("[解析配置文件][file:{}][配置文件解析异常:{}]", fileName, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends AnylineConfig> T parse(Class<? extends AnylineConfig> T, String instance, DataRow row, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		T config = null;
		try {
			config = (T) T.newInstance();
			List<Field> fields = ClassUtil.getFields(T, false, false);
			Map<String, String> kvs = new HashMap<>();
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
						log.info("[解析配置文件][{}={}]", nm, value);
						kvs.put(nm, value);
						config.afterParse(nm, value);
					} catch (Exception e) {
						log.error("解析配置文件异常:", e);
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
							if (BasicUtil.isNotEmpty(kvs.get(newKey))) {
								break;
							}
							if (row.containsKey(oldKey)) {
								String val = row.getString(oldKey);
								kvs.put(newKey, val);
								config.setValue(newKey, val);
								config.afterParse(newKey, val);
								log.info("[解析配置文件][版本兼容][last key:{}][old key:{}][value:{}]", newKey, oldKey, val);
							}
						}
					}
				}
			}
			config.INSTANCE_KEY = instance;
			instances.put(instance, config);
		} catch (Exception e) {
			log.error("解析配置文件异常:", e);
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
			for (Iterator<Element> itrConfig = root.elementIterator("config"); itrConfig.hasNext(); ) {
				Element configElement = itrConfig.next();
				String configKey = configElement.attributeValue("key");
				if (BasicUtil.isEmpty(configKey)) {
					configKey = DEFAULT_INSTANCE_KEY;
				}

				AnylineConfig config = instances.get(configKey);
				if (null == config) {
					config = (AnylineConfig) T.newInstance();
				}
				Map<String, String> kvs = new HashMap<>();
				Iterator<Element> elements = configElement.elementIterator("property");
				while (elements.hasNext()) {
					Element element = elements.next();
					String key = element.attributeValue("key");
					String value = element.getTextTrim();
					log.info("[解析配置文件][key:{}][{}={}]", configKey, key, value);
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
									log.info("[解析配置文件][版本兼容][laster key:{}][old key:{}][value:{}]", newKey, oldKey, val);
								}
							}
						}
					}
				}
				// 加载时间
				config.kvs = kvs;
				config.INSTANCE_KEY = configKey;
				instances.put(configKey, config);
			}
		} catch (Exception e) {
			log.error("parse dom exception:", e);
		}
		return instances;
	}

	protected static Hashtable<String, AnylineConfig> parse(Class<?> T, String content, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		try {
			if(BasicUtil.isNotEmpty(content)) {
				Document document = DocumentHelper.parseText(content);
				instances = parse(T, document, instances, compatibles);
			}
		} catch (Exception e) {
			log.error("parse dom exception:", e);
		}
		return instances;
	}

	private static boolean listener_running = false;	// 监听是否启动
	protected static Map<String, Map<String, Object>> listener_files = new Hashtable<>(); // 监听文件更新<文件名, 最后加载时间>

	private synchronized static void listener() {
		if(listener_running) {
			return;
		}
		listener_running = true;
		log.info("[启动本地配置监听]");
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					for (Map.Entry<String, Map<String, Object>> item : listener_files.entrySet()) {
						File file = new File(item.getKey());
						Map<String, Object> params = item.getValue();
						Class<?> T = (Class<?>)params.get("CLAZZ");
						Hashtable<String, AnylineConfig> instances = (Hashtable<String, AnylineConfig>)params.get("INSTANCES");
						String[] compatibles = (String[])params.get("COMPATIBLES");
						Long lastLoad = (Long)params.get("LAST_LOAD");

						Long lastModify = file.lastModified();

						if (lastLoad == 0 || lastModify > lastLoad) {
							String txt = FileUtil.read(file).toString();
							parse(T, txt, instances, compatibles);
							params.put("LAST_LOAD", System.currentTimeMillis());
							listener_files.put(file.getAbsolutePath(), params);
						}
					}

					if(ConfigTable.getInt("RELOAD", 0) != 0) {
						listener_running = false;
						break;
					}
					try {
						Thread.sleep(5000);
					} catch (Exception ignored) {
					}
				}
			}
		}).start();
	}
	// 兼容上一版本 最后一版key:倒数第二版key:倒数第三版key
	protected static Hashtable<String, AnylineConfig> parse(Class<?> T, File file, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		log.info("[解析配置文件][file:{}]", file);
		if (null == file || !file.exists()) {
			log.warn("[解析配置文件][文件不存在][file:{}]", file);
			return instances;
		}
		addListener(file, T, instances, compatibles);
		String txt = FileUtil.read(file).toString();
		parse(T, txt, instances, compatibles);
		if(ConfigTable.getInt("RELOAD", 0) == 0) {
			listener();
		}
		return instances;
	}
	public static Map<String, Map<String, Object>> getListeners() {
		return listener_files;
	}
	public static Map<String, Object> addListener(File file, Class clazz, Hashtable<String, AnylineConfig> instances, String[] compatibles) {
		Map<String, Object> params = new HashMap<>();
		params.put("CLAZZ", clazz);
		params.put("INSTANCES", instances);
		params.put("COMPATIBLES", compatibles);
		params.put("LAST_LOAD", System.currentTimeMillis());
		listener_files.put(file.getAbsolutePath(), params);
		return params;
	}
	protected static Hashtable<String, AnylineConfig> parse(Class<?> T, InputStream is, Hashtable<String, AnylineConfig> instances, String... compatibles) {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(is);
			instances = parse(T, document, instances, compatibles);
		} catch (Exception e) {
			log.error("parser stream exception:", e);
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
				if (typeName.startsWith("int") || typeName.contains("Integer")) {
					val = BasicUtil.parseInt(value, 0);
				}else if (typeName.contains("long") || typeName.contains("Long")) {
					val = BasicUtil.parseLong(value, 0L);
				} else if (typeName.contains("double") || typeName.contains("Double")) {
					val = BasicUtil.parseDouble(value, 0D);
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
				log.error("set field value exception:", e);
			}
		}
	}
}
