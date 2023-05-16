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
 *
 *
 */


package org.anyline.util;

import org.anyline.entity.Compare;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component("anyline.config")
public class ConfigTable {
	private static final Logger log = LoggerFactory.getLogger(ConfigTable.class);
	private static Environment environment;
	private static Map<String,Long> listener_files = new Hashtable<>(); // 监听文件更新<文件名,最后加载时间>
	protected static String root;		// 项目根目录 如果是jar文件运行表示jar文件所在目录
	protected static String webRoot;
	protected static String classpath;
	protected static String libpath;
	protected static Hashtable<String,Object> configs;
	protected static long lastLoadTime 					= 0					;	// 最后一次加载时间
	protected static int reload 						= 0					;	// 重新加载间隔
	protected static final String version 				= "8.6.3-SNAPSHOT"	;	// 版本号
	protected static final String minVersion 			= "0007"			;	// 版本号
	protected static boolean isLoading 					= false				;	// 是否加载配置文件中
	private static boolean listener_running 			= false				;	// 监听是否启动

	// 对应配置文件key 如果集成了spring boot环境则与spring配置文件 anyline.*对应
	public static boolean IS_DEBUG 										= true			;	// DEBUG状态会输出更多日志
	public static boolean IS_SHOW_SQL									= true			;	// 执行SQL时是否输出日志
	public static long SLOW_SQL_MILLIS									= 0				; 	// 慢SQL,如果配置了>0的毫秒数,在SQL执行超出时限后会输出日志,并调用DMListener.slow
	public static boolean IS_SHOW_SQL_PARAM								= true			;	// 执行SQL时是否输出日志
	public static boolean IS_SHOW_SQL_WHEN_ERROR						= true			;	// 执行SQL异常时是否输出日志
	public static boolean IS_SHOW_SQL_PARAM_WHEN_ERROR					= true			;	// 执行SQL异常时是否输出日志
	public static boolean IS_SQL_DEBUG	 								= false			;	// 加载自定义SQL时是否输出日志
	public static boolean IS_HTTP_LOG 									= true			;	// 调用HTTP接口时是否出输出日志
	public static boolean IS_HTTP_PARAM_AUTO_TRIM						= true			;   // http参数值是否自动trim
	public static boolean IS_IGNORE_EMPTY_HTTP_KEY						= true			;	// AnylineController.entity(String ck)是否忽略http未提交的key
	public static int HTTP_PARAM_ENCODE									= 0				;   // http参数是否解码0:自动识别 1:确认编码 -1:确认未编码
	public static boolean IS_MULTIPLE_SERVICE							= true			;	// 如果有多数据源为每个数据源生成独立的service
	public static boolean IS_AUTO_CONVERT_BYTES							= true			;   // 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)
	public static boolean IS_AUTO_SPLIT_ARRAY							= true			;	// 更新数据库时，是把自动把数组/集合类型拆分
	public static boolean IS_UPPER_KEY 									= true			;	// DataRow是否自动转换成大写
	public static boolean IS_LOWER_KEY 									= false			;	// DataRow是否自动转换成小写
	public static boolean IS_KEY_IGNORE_CASE 							= true			;	// DataRow是否忽略大小写
	public static boolean IS_THROW_SQL_QUERY_EXCEPTION 					= true			;	// SQL查询异常时是否抛出
	public static boolean IS_THROW_SQL_UPDATE_EXCEPTION 				= true			;	// SQL执行异常时是否抛出
	public static boolean IS_UPDATE_NULL_COLUMN							= false			;	// DataRow是否更新nul值的列(针对DataRow)
	public static boolean IS_UPDATE_EMPTY_COLUMN						= false			;	// DataRow是否更新空值的列
	public static boolean IS_INSERT_NULL_COLUMN							= false			;	// DataRow是否更新nul值的列
	public static boolean IS_INSERT_EMPTY_COLUMN						= false			;	// DataRow是否更新空值的列
	public static boolean IS_UPDATE_NULL_FIELD							= false			;	// Entity是否更新nul值的属性(针对Entity)
	public static boolean IS_UPDATE_EMPTY_FIELD							= false			;	// Entity是否更新空值的属性
	public static boolean IS_INSERT_NULL_FIELD							= false			;	// Entity是否更新nul值的属性
	public static boolean IS_INSERT_EMPTY_FIELD							= false			;	// Entity是否更新空值的属性
	public static String LIST2STRING_FORMAT								= "concat"		;	// List/Array转换成String后的格式 concat:A,B,C json:["A","B","C"]
	public static boolean IS_REPLACE_EMPTY_NULL							= true			;   // 是否把""替换成null
	public static boolean IS_SQL_DELIMITER_OPEN 						= false			;	// 是否开启 界定符
	public static boolean IS_SQL_DELIMITER_PLACEHOLDER_OPEN 			= false			;	// 是否开启 界定符的占位符
	public static String SQL_DELIMITER_PLACEHOLDER						= "`"			;	// 界定符的占位符
	public static boolean IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL			= false			;	// service.query() DataSet.getRow()返回null时,是否替换成new DataRow(), new Entity()
	public static boolean IS_AUTO_CHECK_METADATA						= false			; 	// insert update 时是否自动检测表结构(删除表中不存在的属性)
	public static boolean IS_DISABLED_DEFAULT_ENTITY_ADAPTER			= false			; 	// 禁用默认的entity adapter
	public static boolean IS_REMOVE_EMPTY_HTTP_KEY						= true			;   // DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时,row中是否清空ID属性
	public static boolean IS_CACHE_DISABLED								= false			; 	// 是否禁用查询缓存
	public static String DEFAULT_PRIMARY_KEY							= "ID"			;	// 默认主键
	public static boolean IS_OPEN_PRIMARY_TRANSACTION_MANAGER 			= false			;	// 是否需要提供主事务管理器,多数据源时需要

	public static boolean PRIMARY_GENERATOR_UUID_ACTIVE					= false			;	// 是否开启默认的主键生成器(UUID)
	public static boolean PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE			= false			;	// 是否开启默认的主键生成器(雪花)
	public static boolean PRIMARY_GENERATOR_RANDOM_ACTIVE				= false			;	// 是否开启默认的主键生成器(随机)
	public static boolean PRIMARY_GENERATOR_TIMESTAMP_ACTIVE			= false			;	// 是否开启默认的主键生成器(时间戳)
	public static boolean PRIMARY_GENERATOR_TIME_ACTIVE					= false			;	// 是否开启默认的主键生成器(年月日时分秒毫秒)

	public static int PRIMARY_GENERATOR_WORKER_ID						= 1				;	// 主键生成器机器ID
	public static String PRIMARY_GENERATOR_PREFIX						= ""			;	// 主键前缀(随机主键)
	public static int PRIMARY_GENERATOR_RANDOM_LENGTH					= 32			;	// 主随机主键总长度
	public static boolean PRIMARY_GENERATOR_UPPER						= true			;	// 生成主键大写
	public static boolean PRIMARY_GENERATOR_LOWER						= false			;	// 生成主键小写
	public static String PRIMARY_GENERATOR_TIME_FORMAT					= null			;	// 生成主键日期格式(默认yyyyMMddHHmmssSSS)
	public static int PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH				= 3				;   // 生成主键TIME/TIMESTAMP后缀随机数长度
	public static String SNOWFLAKE_TWEPOCH								= "2000-01-01"	;	// 雪花算法开始日期

	public static int AFTER_ALTER_COLUMN_EXCEPTION_ACTION				= 1000			;   // DDL修改列异常后 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
	public static boolean IS_DDL_AUTO_DROP_COLUMN						= false			;   // DDL执行时是否自动删除定义中不存在的列
	public static String SQL_STORE_DIR									= null			;	// 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
	public static boolean IS_OPEN_PARSE_MYBATIS							= true			; 	// 是否开始解析mybatis定义的SQL
	public static String ENTITY_FIELD_COLUMN_MAP						= "camel_"  	;	// 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
	public static String ENTITY_CLASS_TABLE_MAP							= "Camel_"  	;	// 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
	public static String ENTITY_TABLE_ANNOTATION						= null			;   // 表名注解
	public static String ENTITY_COLUMN_ANNOTATION						= null			;	// 列名注解
	public static String ENTITY_PRIMARY_KEY_ANNOTATION					= null			;   // 主键注解(逗号分隔,不区分大小写,支持正则匹配) TableId.value,Id.name,Id(如果不指定注解属性名则依次按name,value解析)
	public static int ENTITY_FIELD_SELECT_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:查询属性关联表
	public static int ENTITY_FIELD_INSERT_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:插入属性关联表
	public static int ENTITY_FIELD_UPDATE_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:更新属性关联表
	public static int ENTITY_FIELD_DELETE_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:删除属性关联表
	public static Compare ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE		= Compare.EQUAL ;	// 实体类属性依赖查询方式 EQUAL:逐行查询 IN:一次查询

	public static String HTTP_PARAM_KEY_CASE							= "camel"		;	// http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
	public static String TABLE_METADATA_CACHE_KEY						= ""			;	// 表结构缓存key
	public static int TABLE_METADATA_CACHE_SECOND						= 3600*24		;	// 表结构缓存时间(没有设置缓存key的情况下生效)(-1:表示永不失效)
	public static String MIX_DEFAULT_SEED								= "al"			;   // MixUti.mix默认seed
	public static String EL_ATTRIBUTE_PREFIX							= "al"			;

	public static String CONFIG_NAME = "anyline-config.xml";

	static{
		init();
	}
	public ConfigTable(){
		debug();
	}
	private synchronized static void listener(){
		if(listener_running){
			return;
		}
		listener_running = true;
		log.info("[启动监听]");
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					for (Map.Entry<String, Long> item : listener_files.entrySet()) {
						File file = new File(item.getKey());
						Long lastLoad = item.getValue();
						Long lastModify = file.lastModified();
						if (lastLoad == 0 || lastModify > lastLoad) {
							parse(file);
						}
					}

					if(getInt("RELOAD",0) != 0){
						listener_running = false;
						break;
					}
					try {
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public static void addConfig(String content){
		loadConfig(content);
	}
	public static void addConfig(File ... files){
		if(null == files){
			return;
		}
		for(File file:files){
			loadConfig(file);
		}
	}
	public static Hashtable<String,Object> getConfigs(){
		return configs;
	}
	public static String getWebRoot() {
		return webRoot;
	}
	public static void setWebRoot(String webRoot){
		ConfigTable.webRoot = webRoot;
		init();
	}
	public static String getRoot(){
		return root;
	}
	public static void setRoot(String root){
		ConfigTable.root = root;
		init();
	}
	public static String getWebClassPath(){
		String result = webRoot + File.separator + "WEB-INF" + File.separator + "classes" + File.separator;
		return result;
	}
	public static String getClassPath(){
		return classpath;
	}
	public static String getLibPath(){
		return libpath;
	}
	public static void init(){
		init("anyline");
	}
	public static String getPackageType(){
		String type = "war";
		String path = ConfigTable.class.getResource("/").getPath();
		if(path.contains(".jar!")){
			type = "jar";
		}
		return type;
	}
	public static void init(String flag) {
		if(isLoading){
			return;
		}
		lastLoadTime = System.currentTimeMillis();
		isLoading = true;
		String path =  "";
		try{
			// path=file:/D:/develop/web/sso-0.0.2.jar!/BOOT-INF/classes!/		(windows jar)
			// path=/D:/develop/web/sso/WEB-INF/classes/ 									(windows tomcat)
			// path=/D:/develop/git/sso/target/classes/									(windows IDE)
			// path=/D:/develop/git/sso/bin/classes/										(windows IDE)
			// path=file:/usr/local/web/sso/sso-0.0.2.jar!/BOOT-INF/classes!/		(linux jar)
			// path=/usr/local/web/sso/WEB-INF/classes/									(linux tomcat)
			path = ConfigTable.class.getResource("/").getPath();
		}catch(Exception e){
			e.printStackTrace();
		}
		log.debug("path={}",path);
		Properties props=System.getProperties();
		String osName = props.getProperty("os.name");
		if(null != osName && osName.toUpperCase().contains("WINDOWS") && path.startsWith("/")){
			path = path.substring(1);
			path =path.replace("file:/", "");
		}
		path = path.replace("file:", "");//jar项目
		// file:/cse/java/cse-sso/qnlm-sso-0.0.2.jar!/BOOT-INF/classes!/
		// log.debug("root={}",root);
		if(null == root && null != path){
			root = path;
			if(root.contains(".jar")){
				root = root.substring(0, root.indexOf(".jar"));
				root = root.substring(0,root.lastIndexOf("/"));
			}
			if(path.indexOf("bin") > 0){
				root = path.substring(0,path.indexOf("bin")-1);
			}
			if(path.indexOf("target") > 0){
				root = path.substring(0,path.indexOf("target")-1);
			}
		}
		// log.debug("root={}",root);
		if(null == webRoot && null != path){
			webRoot = path;
			if(path.indexOf("WEB-INF") > 0){
				webRoot = path.substring(0,path.indexOf("WEB-INF")-1);
			}
			/*
			if(path.indexOf("!/BOOT-INF") > 0){
				webRoot = path.substring(0,path.indexOf("!/BOOT-INF"));
			}
			if(path.indexOf("bin") > 0){
				webRoot = path.substring(0,path.indexOf("bin")-1);
			}
			if(path.indexOf("target") > 0){
				webRoot = path.substring(0,path.indexOf("target")-1);
			}*/
		}
		if(path.contains("classes")){
			classpath = path;
		}else{
			if(path.contains("WEB-INF")){
				classpath = webRoot + File.separator + "WEB-INF" + File.separator + "classes" + File.separator;
			}else{
				classpath = root + File.separator + "bin" + File.separator + "classes" + File.separator;
			}
		}
		libpath = new File(new File(classpath).getParent(), "lib").getAbsolutePath();
		// 加载配置文件
		loadConfig(flag);
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 * @param flag flag
	 */
	protected synchronized static void loadConfig(String flag) {
		try {
			if(null == configs){
				configs = new Hashtable<String,Object>();
			}
			if(null != root){
				configs.put("HOME_DIR", root);
			}

			if("jar".equals(getPackageType())){
				log.info("[加载配置文件][type:jar][file:{}]",flag+"-config.xml");
				InputStream in;
				if (FileUtil.getPathType(AnylineConfig.class) == 0) {
					// 遍历jar
					List<JarEntry> list = new ArrayList<JarEntry>();
					JarFile jFile = new JarFile(System.getProperty("java.class.path"));
					Enumeration<JarEntry> jarEntrys = jFile.entries();
					while (jarEntrys.hasMoreElements()) {
						JarEntry entry = jarEntrys.nextElement();
						String name = entry.getName();
						if (name.endsWith(flag+"-config.xml")) {
							list.add(entry);
						}
					}
					while (jarEntrys.hasMoreElements()) {
						JarEntry entry = jarEntrys.nextElement();
						String name = entry.getName();
						if(name.contains(flag+"-config") && !name.endsWith(flag+"-config.xml")){
							list.add(entry);
						}
					}
					for (JarEntry jarEntry : list) {
						try {
							in = AnylineConfig.class.getClassLoader().getResourceAsStream(jarEntry.getName());
							parse(in);
						}catch (Exception e){
							//这里有可能遇到 多层jar
							//好几层jar的就不读取了 这个文件也极少用 需要配置放spring配置文件里就可以了
						}
					}
				}else{
					in = ConfigTable.class.getClassLoader().getResourceAsStream("/"+flag+"-config.xml");
					String txt = FileUtil.read(in, Charset.forName("UTF-8")).toString();
					parse(txt);
				}
				// 加载jar文件同目录的config
				File dir = new File(FileUtil.merge(root,"config"));
				loadConfigDir(dir,flag);
				//加载当前目录下的 config.xml
				loadConfig(new File(root, flag+"-config.xml"));
			}else{
				// classpath根目录
				File dir = new File(classpath);
				loadConfigDir(dir,flag);
			}
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
			e.printStackTrace();
		}
		lastLoadTime = System.currentTimeMillis();
		reload = getInt("RELOAD");
		String isUpper = getString("IS_UPPER_KEY");
		if(null != isUpper){
			if("false".equals(isUpper.toLowerCase()) || "0".equals(isUpper)){
				IS_UPPER_KEY = false;
			}
		}
	}
	protected synchronized static void loadConfigDir(File dir, String flag) {
		log.info("[加载配置文件][dir:{}]",dir.getAbsolutePath());
		List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
		for(File f:files){
			String name = f.getName();
			if((flag+"-config.xml").equals(name)){
				loadConfig(f);
			}
		}
		for(File f:files){
			String name = f.getName();
			if(name.startsWith(flag+"-config") && !(flag+"-config.xml").equals(name)){
				loadConfig(f);
			}
		}
		log.info("[加载配置文件完成]");
	}
	public static void parse(File file){
		parse(FileUtil.read(file).toString());
		listener_files.put(file.getAbsolutePath(), System.currentTimeMillis());
	}
	public static void parse(String xml){
		try {
			if(BasicUtil.isEmpty(xml)){
				return;
			}
			Document document = DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			for (Iterator<Element> itrProperty = root.elementIterator("property"); itrProperty.hasNext(); ) {
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				put(key.toUpperCase().trim(), value);
				if (IS_DEBUG) {
					log.debug("[解析配置文件][{}={}]", key, value);
				}
			}
			map2field();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void parse(InputStream is){
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(is);
			Element root = document.getRootElement();
			for (Iterator<Element> itrProperty = root.elementIterator("property"); itrProperty.hasNext(); ) {
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				configs.put(key.toUpperCase().trim(), value);
				if (IS_DEBUG) {
					log.info("[解析配置文件][{}={}]", key, value);
				}
			}
			map2field();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//同步maps与静态属性值
	protected static void map2field(){
		Field[] fields = ConfigTable.class.getDeclaredFields();
		for(Field field:fields){
			String name = field.getName();
			if(configs.containsKey(name)){
				BeanUtil.setFieldValue(null, field, configs.get(name));
			}
		}
	}
	protected static void loadConfig(File file){
		try{
			if(IS_DEBUG){
				log.info("[加载配置文件] [file:{}]",file);
			}
			if(null != file && !file.exists()){
				log.info("[配置文件不存在] [file:{}]",file.getAbsolutePath());
				return;
			}
			if(file.isDirectory()){
				List<File> files = FileUtil.getAllChildrenDirectory(file);
				for(File f:files){
					loadConfig(f);
				}
			}else {
				parse(file);
				// 如果未设置重新加载时间,则实现监听文件更新
				if(getInt("RELOAD",0) == 0){
					listener();
				}
			}

		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}

	public static Object get(String key){
		if(null == key){
			return null;
		}
		Object val = null;
		if(reload > 0 && (System.currentTimeMillis() - lastLoadTime)/1000 > reload){
			// 重新加载
			isLoading = false;
			init();
		}
		val = configs.get(key.toUpperCase().trim());
		if(null == val && null != environment){
			val = environment.getProperty(key);
			if(null == val){
				if(key.startsWith("anyline.")){
					key = key.replace("anyline.","");
				}else{
					key = "anyline." + key;
				}
				val = environment.getProperty(key);
				if(null != val){
					put(key, val);
				}
			}
		}
		return val;
	}
	public static String getString(String key) {
		Object val = get(key);
		if(null != val){
			return val.toString();
		}
		return null;
	}
	public static String getString(String key, String def){
		String val = getString(key);
		if(null == val){
			val = def;
		}
		return val;
	}
	public static Object get(String key, Object def){
		Object val = get(key);
		if(null == val){
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
	public static void put(String key, String value){
		configs.put(key, value);
		if(IS_DEBUG){
			log.info("[ConfigTable动态更新][{}={}]",key,value);
		}
	}
	public static void put(String key, Object value){
		configs.put(key, value);
		if(value instanceof Collection){
			Collection cols = (Collection) value;
			int i = 0;
			for(Object col:cols){
				configs.put(key + "[" + i++ + "]", col);
				configs.put(key + "." + i++ , col);
			}
		}
		if(value instanceof Map){
			Map map = (Map)value;
			for(Object k: map.keySet()){
				configs.put(key + "[" + k + "]", map.get(k));
				configs.put(key + "." + k , map.get(k));
			}
		}
		if(IS_DEBUG){
			log.info("[ConfigTable动态更新][{}={}]",key,value);
		}
	}
	public static String getVersion(){
		return version;
	}
	public static String getMinVersion(){
		return minVersion;
	}

	public static int getReload() {
		return reload;
	}
	public static boolean isDebug() {
		return IS_DEBUG;
	}
	public static boolean isSQLDebug() {
		return IS_SQL_DEBUG;
	}



	protected static void line(String src, String chr, int append, boolean center){
		int len = 80 + append;
		String line = "";
		if(center){
			int fillLeft = (len - src.length() -2)/2;
			int fillRight = (len - src.length() -2)/2;
			if((len - src.length())%2!=0){
				fillRight ++;

			}
			line = "*"+BasicUtil.fillChar("", chr, fillLeft) + src +BasicUtil.fillChar("", chr, fillRight) +"*";
		}else{
			int fill = len - src.length() - 2;
			line = "*" + src + BasicUtil.fillChar("", chr, fill)+"";
		}
		System.out.println(line);
	}

	protected static void debug(){
		if(!IS_DEBUG){
			return;
		}
		try{
			String time = null;
			String version = ConfigTable.version;
			String project = null;
			try{
				String path =ConfigTable.class.getResource("").getPath();
				if(path.startsWith("file:")){
					path = path.substring(path.indexOf(":")+1);
				}
				Properties props=System.getProperties(); // 获得系统属性集
				String osName = props.getProperty("os.name"); // 操作系统名称
				if(null != osName && osName.toUpperCase().contains("WINDOWS") && path.startsWith("/")){
					path = path.substring(1);
				}
				if(path.contains("!")){
					path = path.substring(0,path.indexOf("!"));
				}
				if(path.contains("/WEB-INF")){
					project = path.substring(0, path.indexOf("/WEB-INF"));
				}

				File file = new File(path);
				try {
					String anylineJarPath = ConfigTable.class.getProtectionDomain().getCodeSource().getLocation().getFile();
					String anylineJarName = new File(anylineJarPath).getName();
					if(anylineJarName.endsWith("jar") || anylineJarName.endsWith("jar!")){
						if(anylineJarName.contains("-")){
							version = anylineJarName.replace("anyline-","").replace(".jar","").replace("!","");
							version = version.substring(version.indexOf("-")+1);
						}
					}
					file = new File(anylineJarPath);
				}catch (Exception e){}
				time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date(file.lastModified()));
			}catch(Exception e){

			}

			System.out.println();
			line("","*", 0,true);
			line("Anyline Core [" + version + "]", " ",0, true);
			line("http://doc.anyline.org ", " ", 0, true);
			line(""," ", 0, true);
			if(null != time && time.startsWith("2")){
				line("Last Modified " + "[" + time +"] ", " ", 0, true);
			}else{
				line("MinVersion " +  "[" + minVersion + "]", " ", 0, true);
			}
			line(""," ", 0, true);
			line("","*", 0, true);
			line("","*", 0, true);
			if(null != project){
				line(" project root > " + project, "", 0, false);
			}
			line(" debug status > anyline-config.xml:<property key=\"DEBUG\">boolean</property>", "", 0, false);
			line(" =================== 生产环境请务必修改密钥文件key.xml ========================", "", 0, false);
			line("","*", 0, true);
			System.out.println();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void setUpperKey(boolean bol){
		IS_UPPER_KEY = bol;
	}
	public static void setLowerKey(boolean bol){
		IS_LOWER_KEY = bol;
	}
	public static void setEnvironment(Environment env){
		environment = env;
	}
}
