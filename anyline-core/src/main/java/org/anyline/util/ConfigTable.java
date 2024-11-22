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

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.entity.generator.GeneratorConfig;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConfigTable {
	private static final Log log = LogProxy.get(ConfigTable.class);
	public static EnvironmentWorker environment;
	private static boolean IS_LOG = false;
	private static final Map<String, Long> listener_files = new Hashtable<>(); // 监听文件更新<文件名, 最后加载时间>
	protected static String root;		// 项目根目录 如果是jar文件运行表示jar文件所在目录
	protected static String webRoot;
	protected static String classpath;
	protected static String libpath;
	protected static Hashtable<String, Object> configs = new Hashtable<>()	;
	protected static long lastLoadTime 					= 0					;	// 最后一次加载时间
	protected static int reload 						= 0					;	// 重新加载间隔
	protected static final String version 				= "8.7.2-SNAPSHOT"	;	// 版本号
	protected static final String minVersion 			= "000"				;	// 版本号
	protected static boolean isLoading 					= false				;	// 是否加载配置文件中
	private static boolean listener_running 			= false				;	// 监听是否启动

	public static String CONFIG_NAME = "anyline-config.xml";

	// 对应配置文件key 如果集成了spring boot环境则与spring配置文件 anyline.*对应
	public static String ENVIRONMENT_CONFIG_FILE_NAMES 					= "application.properties,application.yml";   // 配置文件
	public static Class DEFAULT_JDBC_ENTITY_CLASS						= DataRow.class ;
	public static Class DEFAULT_MONGO_ENTITY_CLASS						= DataRow.class ;
	public static Class DEFAULT_ELASTIC_SEARCH_ENTITY_CLASS				= DataRow.class ;
	public static Class DEFAULT_NEO4J_ENTITY_CLASS						= DataRow.class ;
	public static boolean IS_DEBUG 										= true			;	// DEBUG状态会输出更多日志
	/**
	 * 同一个数据源是否保持相同的adapter
	 * 1: 全部保持
	 * 0: 全部不保持 每次操作数据库都检测一次
	 * 2: 由DataSourceMonitor接口实现
	 * 如果项目中出现 一个数据源对应多类数据库的情况, 同时出现 一个数据源只对应一类数据库的情况 这时才需要设置为2或0
	 * 这里设置了false后每个
	 */
	public static int KEEP_ADAPTER										= 1				;	// 同一个数据源是否保持相同的adapter
	public static int  DEBUG_LVL										= 0				;   //
	public static boolean IS_DISABLED_DEFAULT_LOG						= false			;   // 是否禁用默认日志(anyline-log)
	public static boolean IS_LOG_SQL									= true			;	// 执行SQL时是否输出日志
	public static boolean IS_LOG_SLOW_SQL								= true			;	// 执行慢SQL时是否输出日志
	public static boolean IS_LOG_SQL_TIME								= true			;	// 执行SQL时是否输出耗时日志
	public static boolean IS_LOG_QUERY_RESULT							= false			;	// 查询结果输出日志
	public static boolean IS_LOG_QUERY_RESULT_EXCLUDE_METADATA			= true			;	// 查询结果输出日志时 是否过滤元数据查询结果
	public static String LOG_QUERY_RESULT_ALT							= "(中文对齐请设置字体)";	// 查询结果提示
	public static int LOG_QUERY_RESULT_ROWS								= 10			;	// 查询结果日志显示行数(-1表示全部)
	public static int LOG_QUERY_RESULT_TABLE_MAX_WIDTH					= 120			;   // 查询结果日志表格最大宽度(中文占2个宽度)如果小于实际内容宽度按实际跨度显示
	public static int LOG_QUERY_RESULT_CUT_WIDTH						= 30			;   // 查询结果日志超长列截断后宽度
	public static boolean IS_LOG_ADAPTER_MATCH							= false			;	// 是否输出adapter匹配过程日志

	public static boolean IS_LOG_CONVERT_EXCEPTION						= false			;   // 是否记录convert异常提示()
	public static boolean IS_THROW_CONVERT_EXCEPTION					= false			;   // 是否抛出convert异常提示()
	public static boolean IS_PRINT_EXCEPTION_STACK_TRACE				= false			;   // 捕捉但未抛出的异常是否显示详细信息
	public static long SLOW_SQL_MILLIS									= 0				; 	// 慢SQL, 如果配置了>0的毫秒数, 在SQL执行超出时限后会输出日志, 并调用DMListener.slow
	public static boolean IS_LOG_SQL_PARAM								= true			;	// 执行SQL时是否输出参数日志
	public static boolean IS_LOG_BATCH_SQL_PARAM						= false 		;   // 执行批量SQL是是否输出参数日志
	public static boolean IS_LOG_SQL_WHEN_ERROR							= true			;	// 执行SQL异常时是否输出日志
	public static boolean IS_LOG_SQL_WARN								= true			;	// 执行SQL WARN
	public static boolean IS_LOG_SQL_PARAM_WHEN_ERROR					= true			;	// 执行SQL异常时是否输出参数日志
	public static boolean IS_SQL_LOG_PLACEHOLDER						= true			;   // SQL日志 是否显示占位符
	public static boolean IS_ENABLE_PLACEHOLDER_REGEX_EXT				= true			;	// 是否启用 ::KEY 格式的占位符解析,在PG环境中会与 ::INT8 格式冲突 需要禁用
	public static boolean IS_ENABLE_SQL_DATATYPE_CONVERT				= false			;	// 是否开启SQL数据类型转换 pg.json 不用占位符时String正常执行 使用占位符时需要转换 ?::json(jsonb)
	public static boolean IS_SQL_DEBUG	 								= false			;	// 加载自定义SQL时是否输出日志
	public static boolean IS_HTTP_LOG 									= true			;	// 调用HTTP接口时是否出输出日志
	public static boolean IS_HTTP_PARAM_AUTO_TRIM						= true			;   // http参数值是否自动trim
	public static boolean IS_IGNORE_EMPTY_HTTP_KEY						= true			;	// AnylineController.entity(String ck)是否忽略http未提交的key
	public static int HTTP_PARAM_ENCODE									= 0				;   // http参数是否解码0:自动识别 1:确认编码 -1:确认未编码
	public static boolean IS_MULTIPLE_SERVICE							= true			;	// 如果有多数据源为每个数据源生成独立的service
	public static boolean IS_ENABLE_COMMON_JDBC_ADAPTER					= false			;   // 是否开启通用的jdbc adapter(仅支持部分标准SQL)遇到没有实现adapter的数据库时可以开启
	public static boolean IS_ENABLE_ADAPTER_FAST_MATCH					= false			;   // 环境中只有一个adapter时快速匹配所有数据源(不检测数据库特征)
	public static boolean IS_ENABLE_COLUMN_POSITION						= true			;	// 是否开启通用的position(仅支持部分标准SQL)遇到没有实现adapter的数据库时可以开启
	public static boolean IS_AUTO_CONVERT_BYTES							= true			;   // 否将数据库中与Java bytes[]对应的类型自动转换如Point > double[](返回DataRow时受此开关景程)
	public static boolean IS_AUTO_SPLIT_ARRAY							= true			;	// 更新数据库时，是把自动把数组/集合类型拆分
	public static boolean IS_METADATA_IGNORE_CASE						= true			;   // 查询元数据时忽略大小写
	public static boolean IS_UPPER_KEY 									= true			;	// DataRow是否自动转换成大写
	public static boolean IS_LOWER_KEY 									= false			;	// DataRow是否自动转换成小写
	public static boolean IS_KEY_IGNORE_CASE 							= true			;	// DataRow是否忽略大小写
	public static boolean IS_THROW_SQL_QUERY_EXCEPTION 					= true			;	// SQL查询异常时是否抛出
	public static boolean IS_THROW_SQL_UPDATE_EXCEPTION 				= true			;	// 命令执行异常时是否抛出
	public static boolean IS_UPDATE_NULL_COLUMN							= false			;	// DataRow是否更新nul值的列(针对DataRow)
	public static boolean IS_UPDATE_EMPTY_COLUMN						= false			;	// DataRow是否更新空值的列
	public static boolean IS_INSERT_NULL_COLUMN							= false			;	// DataRow是否插入nul值的列
	public static boolean IS_INSERT_EMPTY_COLUMN						= false			;	// DataRow是否插入空值的列
	public static boolean IS_UPDATE_NULL_FIELD							= false			;	// Entity是否更新nul值的属性(针对Entity)
	public static boolean IS_UPDATE_EMPTY_FIELD							= false			;	// Entity是否更新空值的属性
	public static boolean IS_INSERT_NULL_FIELD							= false			;	// Entity是否更新nul值的属性
	public static boolean IS_INSERT_EMPTY_FIELD							= false			;	// Entity是否更新空值的属性
	public static boolean IS_CHECK_ALL_INSERT_COLUMN					= false			;	// 插入集合时是否检测所有条目的列(默认只检测第一行)
	public static boolean IS_CHECK_ALL_UPDATE_COLUMN					= false			;	// 更新集合时是否检测所有条目的列(默认只检测第一行)
	public static String LIST2STRING_FORMAT								= "concat"		;	// List/Array转换成String后的格式 concat:A, B, C json:["A","B","C"]
	public static boolean IS_REPLACE_EMPTY_NULL							= true			;   // 是否把""替换成null
	public static boolean IS_SQL_DELIMITER_OPEN 						= false			;	// 是否开启 界定符(注意开启后大部分数据库会区分大小写)
	public static boolean IS_AUTO_CHECK_KEYWORD							= false			;   // 自动检测关键字
	public static boolean IS_AUTO_CHECK_EL_VALUE						= true			;   // 自动检测el值
	public static boolean IS_SQL_DELIMITER_PLACEHOLDER_OPEN 			= false			;	// 是否开启 界定符的占位符(用来实现自定义SQL根据不同的数据库添加不同的界定符, 写SQL时统一写成SQL_DELIMITER_PLACEHOLDER)
	public static String SQL_DELIMITER_PLACEHOLDER						= "`"			;	// 界定符的占位符
	public static boolean IS_RETURN_EMPTY_STRING_REPLACE_NULL			= false			;  // DataRow.getString返回null时替换成""
	public static boolean IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL			= false			;	// service.query() DataSet.getRow()返回null时, 是否替换成new DataRow(), new Entity()
	public static boolean IS_AUTO_CHECK_METADATA						= false			; 	// insert update 时是否自动检测表结构(删除表中不存在的属性,转换数据类型)以及查询条件中的数据类型
	public static int METADATA_CACHE_SCOPE								= 9				;   // 0:不缓存 1:当前线程 9:整个应用
	public static String METADATA_CACHE_KEY								= ""			;	// 表结构缓存key
	public static int METADATA_CACHE_SECOND								= 3600*24		;	// 表结构缓存时间(-1:表示永不失效)
	public static int CHECK_METADATA_SAMPLE								= 1000			;   // 检测元数据的样本数量(-1:全部 0:不检测)(mongo当前版本中没有提供元数据查询方法)
	public static boolean IS_CHECK_EMPTY_SET_METADATA					= false			;   // 查询返回空DataSet时，是否检测元数据信息
	public static boolean IS_DISABLED_DEFAULT_ENTITY_ADAPTER			= false			; 	// 禁用默认的entity adapter
	public static boolean IS_REMOVE_EMPTY_HTTP_KEY						= true			;   // DataRow row = entity("ID:id") 如果参数(如request)中未提供id参数时, row中是否清空ID属性
	public static boolean IS_CACHE_DISABLED								= false			; 	// 是否禁用查询缓存
	public static String DEFAULT_PRIMARY_KEY							= "ID"			;	// 默认主键
	public static boolean IS_OPEN_TRANSACTION_MANAGER 					= true			;	// 是否需要提供事务管理器, 会为每个数据源生成相应的事务管理器
	public static boolean IS_OPEN_PRIMARY_TRANSACTION_MANAGER 			= false			;	// 是否需要设置一个主事务管理器, 多数据源时为注解事务指定一个事务管理器
	public static String ALTER_COLUMN_TYPE_SUFFIX						= "___"			;	// ORACLE系修改数据类型过程中，需要中间列，这里指定中间列名称的后缀
	public static int AFTER_ALTER_COLUMN_EXCEPTION_ACTION				= 1000			;   // DDL修改列异常后 -1：抛出异常 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
	public static int SQL_QUERY_TIMEOUT									= -1			;	// 等待 查询SQL 语句完成的最大时间(s), 超过则抛出异常
	public static int SQL_UPDATE_TIMEOUT								= -1			;	// 等待 更新SQL 语句完成的最大时间(s), 超过则抛出异常
	public static boolean IS_DDL_AUTO_DROP_COLUMN						= false			;   // DDL执行时是否自动删除定义中不存在的列
	public static boolean IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY			= false			;   // 查询列时，是否自动检测主键标识
	public static String SQL_STORE_DIR									= null			;	// 自定义SQL目录(包括MyBatis) 默认${classpath}/sql .表示项目根目录 ${classpath}表示classes目录
	public static boolean IS_OPEN_PARSE_MYBATIS							= true			; 	// 是否开始解析mybatis定义的SQL
	public static String ENTITY_FIELD_COLUMN_MAP						= "camel_"  	;	// 实体属性 与数据库表列名对照时 默认属性小驼峰转下划线 userName > USER_NAME
	public static String ENTITY_CLASS_TABLE_MAP							= "Camel_"  	;	// 实体类名 与数据库表名对照时 默认属性大驼峰转下划线 CrmUser > CRM_USER
	public static String ENTITY_TABLE_ANNOTATION						= null			;   // 表名注解(逗号分隔, 不区分大小写, 支持正则匹配)
	public static String ENTITY_COLUMN_ANNOTATION						= null			;	// 列名注解(逗号分隔, 不区分大小写, 支持正则匹配)column.name, column.value, TableField.name, TableField.value, tableId.name, tableId.value, Id.name, Id.value
	public static String ENTITY_PRIMARY_KEY_ANNOTATION					= null			;   // 主键注解(逗号分隔, 不区分大小写, 支持正则匹配) tableId.value, Id.name, Id(如果不指定注解属性名则依次按name, value解析)
	public static int ENTITY_FIELD_SELECT_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:查询属性关联表
	public static int ENTITY_FIELD_INSERT_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:插入属性关联表
	public static int ENTITY_FIELD_UPDATE_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:更新属性关联表
	public static int ENTITY_FIELD_DELETE_DEPENDENCY					= 0				;   // 实体类属性依赖层级 > 0:删除属性关联表
	public static Compare ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE		= Compare.EQUAL ;	// 实体类属性依赖查询方式 EQUAL:逐行查询 IN:一次查询
	public static int IGNORE_GRAPH_QUERY_RESULT_TOP_KEY					= 0				;   // 是否忽略查询结果中顶层的key,可能返回多个结果集(0-不忽略 1-忽略 2-如果1个结果集则忽略 多个则保留)
	public static int IGNORE_GRAPH_QUERY_RESULT_TABLE 					= 0				;   // 是否忽略查询结果中的表名,数据可能存在于多个表中(0-不忽略 CRM_USER.id 1-忽略 id 2-如果1个表则忽略 多个表则保留)
	public static int MERGE_GRAPH_QUERY_RESULT_TABLE 					= 0				;	// 是否合并查询结果中的表,合并后会少一层表名被合并到key中(如果不忽略表名)(0-不合并 1-合并 2-如果1个表则合并 多个表则不合并)
	// 0{"HR_USER":{"name":"n22","id":22},"CRM_USER":{"name":"n22","id":22}}
	// 1{"HR_USER.name":"n22","HR_USER.id":22,"CRM_USER.name":"n22","CRM_USER.id":22}}
	public static String HTTP_PARAM_KEY_CASE							= "camel"		;	// http参数格式 camel:小驼峰 Camel:大驼峰 lower:小写 upper:大写  service.column2param会把 USER_NAME 转成userName
	public static String MIX_DEFAULT_SEED								= "al"			;   // MixUti.mix默认seed
	public static String EL_ATTRIBUTE_PREFIX							= "al"			;

	//主键生成器
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
	public static String GENERATOR_TABLES								= "*"			;   // 主键生成器适用的表
	public final static GeneratorConfig GENERATOR 						= new GeneratorConfig();
	static{
		prepare();
		try {
			loadEnvironment();
		}catch (Exception e) {
			log.error("load environment exception:", e);
		}
		init();
	}
	public ConfigTable() {
		debug();
	}

	public static void setEnvironment(EnvironmentWorker environment) {
		ConfigTable.environment = environment;
	}

	public static EnvironmentWorker environment() {
		if(null == environment) {
			log.error("未注入EnvironmentWorker(8.7.2+)(基础Java环境调用DefaultEnvironmentWorker.start(),其他环境添加依赖如spring环境:anyline-environment-spring-data-jdbc)");
		}
		return environment;
	}
	private synchronized static void listener() {
		if(listener_running || listener_files.isEmpty()) {
			return;
		}
		listener_running = true;
		log.info("[启动监听][{}]", BeanUtil.concat(listener_files.keySet()));
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

					if(getInt("RELOAD", 0) != 0) {
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

	public static void addConfig(String content) {
		loadConfig(content);
	}
	public static void addConfig(File ... files) {
		if(null == files) {
			return;
		}
		for(File file:files) {
			loadConfig(file);
		}
	}
	public static String version() {
		return version + "-" + minVersion;
	}
	public static Hashtable<String, Object> getConfigs() {
		return configs;
	}
	public static String getWebRoot() {
		return webRoot;
	}
	public static void setWebRoot(String webRoot) {
		ConfigTable.webRoot = webRoot;
		init();
	}
	public static String getRoot() {
		return root;
	}
	public static void setRoot(String root) {
		ConfigTable.root = root;
		init();
	}
	public static String getWebClassPath() {
		String result = webRoot + File.separator + "WEB-INF" + File.separator + "classes" + File.separator;
		return result;
	}
	public static String getClassPath() {
		return classpath;
	}
	public static String getLibPath() {
		return libpath;
	}
	public static void init() {
		init("anyline");
	}

	/**
	 * 当前项目目录类型
	 * @return String
	 */
	public static String getProjectProtocol() {
		URL url = ConfigTable.class.getResource("/");
		if(null != url) {
			return url.getProtocol();
		}
		return null;
	}
	public static String path(String path) {
		// return new File(path).getAbsolutePath();
		// path=nested:/usr/local/A.jar!/BOOT-INF/lib/B.jar
		// path=file:/D:/develop/web/A.jar!/BOOT-INF/classes!/				(windows jar)
		// path=/D:/develop/web/sso/WEB-INF/classes/ 						(windows tomcat)
		// path=/D:/develop/git/sso/target/classes/							(windows IDE)
		// path=/D:/develop/git/sso/bin/classes/							(windows IDE)
		// path=file:/D:/develop/git/sso/bin/classes/						(windows IDE)
		// path=file:/usr/local/web/sso/A.jar!/BOOT-INF/classes!/			(linux jar)
		// path=/usr/local/web/sso/WEB-INF/classes/							(linux tomcat)
		if(null == path) {
			return null;
		}
		path = path.replace("nested:", "");
		Properties props=System.getProperties();
		String os = props.getProperty("os.name");
		if(null != os && os.toUpperCase().contains("WINDOWS")) {
			if(path.startsWith("/")) {
				path = path.substring(1);
			}
			path =path.replace("file:/","");
		}
		path = path.replace("file:","");//jar项目
		if(path.contains("!")) {
			path = path.substring(0, path.indexOf("!"));
		}

		return path;
	}
	public static void prepare() {
		if(isLoading) {
			return;
		}
		lastLoadTime = System.currentTimeMillis();
		isLoading = true;
		String path =  null;
		try{
			URL url = ConfigTable.class.getResource("/"); //项目外的lib目录获取不到url
			if(null != url) {
				path = url.getPath();
			}
			if(null == path) {
				File tmp = new File(".").getAbsoluteFile();
				if(null != tmp) {
					path = tmp.getParent();
				}
			}
			if(null == path) {
				path = System.getProperty("user.dir");
			}
		}catch(Exception e) {
			log.error("prepare exception:", e);
		}
		log.debug("path={}", path);
		path = path(path);
		// log.debug("root={}", root);
		if(null == root && null != path) {
			root = path;
			if(root.contains(".jar")) {
				root = root.substring(0, root.indexOf(".jar"));
				root = root.substring(0, root.lastIndexOf("/"));
			}
			if(path.indexOf("bin") > 0) {
				root = path.substring(0, path.indexOf("bin")-1);
			}
			if(path.indexOf("target") > 0) {
				root = path.substring(0, path.indexOf("target")-1);
			}
		}
		// log.debug("root={}", root);
		if(null == webRoot && null != path) {
			webRoot = path;
			if(path.indexOf("WEB-INF") > 0) {
				webRoot = path.substring(0, path.indexOf("WEB-INF")-1);
			}
			/*
			if(path.indexOf("!/BOOT-INF") > 0) {
				webRoot = path.substring(0, path.indexOf("!/BOOT-INF"));
			}
			if(path.indexOf("bin") > 0) {
				webRoot = path.substring(0, path.indexOf("bin")-1);
			}
			if(path.indexOf("target") > 0) {
				webRoot = path.substring(0, path.indexOf("target")-1);
			}*/
		}
		if(null == path) {
			path = root;
		}
		if(path.contains("classes")) {
			classpath = path;
		}else{
			if(path.contains("WEB-INF")) {
				classpath = webRoot + File.separator + "WEB-INF" + File.separator + "classes" + File.separator;
			}else{
				classpath = root + File.separator + "bin" + File.separator + "classes" + File.separator;
				if(!new File(classpath).exists()) {
					classpath = root;
				}
			}
		}
		libpath = new File(new File(classpath).getParent(), "lib").getAbsolutePath();
	}
	public static void init(String flag) {
		// 加载配置文件
		loadConfig(flag);
	}

	/**
	 * 加载项目配置文件
	 */
	public static void loadEnvironment() throws Exception {
		String[] items = ENVIRONMENT_CONFIG_FILE_NAMES.split(",");
		if("jar".equals(getProjectProtocol())) {
			log.debug("[load environment][type:jar]");
			if (FileUtil.getPathType(AnylineConfig.class) == 0) {
				// 遍历jar
				JarFile jar = new JarFile(System.getProperty("java.class.path"));
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if(check(name)) {
						InputStream in = jar.getInputStream(entry);
						String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
						parseEnvironment(txt, name);
					}
				}
			}else{
				for(String item:items) {
					InputStream in = ConfigTable.class.getClassLoader().getResourceAsStream("/"+item);
					String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
					parseEnvironment(txt, item);
				}
			}
			// 加载jar文件同目录的config
			File dir = new File(FileUtil.merge(root, "config"));
			for(String item:items) {
				String txt = FileUtil.read(new File(dir, item), StandardCharsets.UTF_8).toString();
				parseEnvironment(txt, item);
			}
		}else{
			// classpath根目录
			File dir = new File(classpath);
			for(String item:items) {
				String txt = FileUtil.read(new File(dir, item), StandardCharsets.UTF_8).toString();
				parseEnvironment(txt, item);
			}
		}
		map2field();
	}
	public static boolean check(String name) {
		String[] items = ENVIRONMENT_CONFIG_FILE_NAMES.split(",");
		for(String item:items) {
			if(name.endsWith(item)) {
				return true;
			}
		}
		return false;
	}
	public static void loadEnvironment(String path) {
		try {
			log.debug("[load environment][path:{}]", path);
			ClassLoader loader = ConfigTable.class.getClassLoader();
			Enumeration<URL> urls = loader.getResources(path);
			while (urls.hasMoreElements()) {
				parseEnvironment(urls.nextElement());
			}
		}catch (Exception e) {
			log.error("load environment 异常:", e);
		}
	}
	public static void parseEnvironment(URL url) {
		try {
			log.debug("[parse environment][url:{}]", url);
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			String path = url.getPath();
			InputStream in = url.openStream();
			String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
			if(path.contains(".yml")) {
				map = parseYml(txt);
			}else if(path.contains("properties")) {
				map = parseProperty(txt);
			}
			for(String key:map.keySet()) {
				put(key.trim().toUpperCase(), map.get(key));
			}
		}catch (Exception e) {
			log.error("parse environment exception:", e);
		}
	}

	public static void parseEnvironment(String txt, String name) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		if(name.endsWith(".properties")) {
			map = parseProperty(txt);
		}else if(name.endsWith(".yml")) {
			map = parseYml(txt);
		}
		for(String key:map.keySet()) {
			put(key.trim().toUpperCase(), map.get(key));
		}
	}
	public static LinkedHashMap<String, Object> parseYml(String txt) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		Map<Integer, String> heads = new HashMap<>();
		String[] lines = txt.split("\n");
		for(String line:lines) {
			int idx = line.indexOf(":");
			if(idx == -1) {
				continue;
			}
			if(line.trim().startsWith("#")) {
				continue;
			}
			String key = line.substring(0, idx);
			String val = line.substring(idx+1).trim();
			Integer lvl = lvl(key);
			key = key.trim();
			if(line.trim().endsWith(":")) {
				heads.put(lvl, key.trim());
			}else{
				String head = head(heads, lvl);
				if(head.isEmpty()) {
					head = key;
				}else{
					head = head+ "." + key;
				}
				map.put(head, val);
			}
		}
		return map;
	}
	public static LinkedHashMap<String, Object> parseProperty(String txt) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		String[] lines = txt.split("\n");
		for(String line:lines) {
			line = line.trim();
			if(line.startsWith("#")) {
				continue;
			}
			if(line.contains("=")) {
				int idx = line.indexOf("=");
				String key = line.substring(0, idx);
				String val = line.substring(idx+1);
				map.put(key, val);
			}
		}
		return map;
	}
	private static String head(Map<Integer, String> headers, int lvl) {
		StringBuilder head = new StringBuilder();
		for(int i=0; i<lvl; i++) {
			if(i>0) {
				head.append(".");
			}
			head.append(headers.get(i));
		}
		return head.toString();
	}
	private static Integer lvl(String key) {
		//按2个空格1级
		Integer lvl = 0;
		int length = key.length() - key.trim().length();
		lvl = length/2;
		return lvl;
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 * @param flag 配置文件前缀 如anyline-config.xml中的anyline
	 */
	protected synchronized static void loadConfig(String flag) {
		try {
			if(null == configs) {
				configs = new Hashtable<>();
			}
			if(null != root) {
				configs.put("HOME_DIR", root);
			}

			if("jar".equals(getProjectProtocol())) {
				log.debug("[加载配置文件][type:jar][file:{}]", flag+"-config.xml");
				InputStream in;
				if (FileUtil.getPathType(AnylineConfig.class) == 0) {
					// 遍历jar
					JarFile jar = new JarFile(System.getProperty("java.class.path"));
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.endsWith(flag+"-config.xml")) {
							in = jar.getInputStream(entry);
							parse(in);
						}
						if(name.contains(flag+"-config") && !name.endsWith(flag+"-config.xml")) {
							in = jar.getInputStream(entry);
							parse(in);
						}
					}
				}else{
					in = ConfigTable.class.getClassLoader().getResourceAsStream("/"+flag+"-config.xml");
					String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
					parseXML(txt);
				}
				// 加载jar文件同目录的config
				File dir = new File(FileUtil.merge(root, "config"));
				loadConfigDir(dir, flag);
				//加载当前目录下的 config.xml
				loadConfig(new File(root, flag+"-config.xml"));
			}else{
				// classpath根目录
				File dir = new File(classpath);
				loadConfigDir(dir, flag);
			}
		} catch (Exception e) {
			log.error("log config exception:", e);
		}
		lastLoadTime = System.currentTimeMillis();
		reload = getInt("RELOAD");
		String isUpper = getString("IS_UPPER_KEY");
		if(null != isUpper) {
			if("false".equals(isUpper.toLowerCase()) || "0".equals(isUpper)) {
				IS_UPPER_KEY = false;
			}
		}
	}
	protected synchronized static void loadConfigDir(File dir, String flag) {
		log.debug("[加载配置文件][dir:{}]", dir.getAbsolutePath());
		List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
		for(File f:files) {
			String name = f.getName();
			if((flag+"-config.xml").equals(name)) {
				loadConfig(f);
			}
		}
		for(File f:files) {
			String name = f.getName();
			if(name.startsWith(flag+"-config") && !(flag+"-config.xml").equals(name)) {
				loadConfig(f);
			}
		}
		log.debug("[加载配置文件完成]");
	}
	public static LinkedHashMap<String, Object> parse(File file) {
		LinkedHashMap<String, Object> maps = parseXML(FileUtil.read(file).toString());
		listener_files.put(file.getAbsolutePath(), System.currentTimeMillis());
		return maps;
	}
	public static LinkedHashMap<String, Object> parseXML(String xml) {
		LinkedHashMap<String, Object> maps = new LinkedHashMap<>();
		try {
			if(BasicUtil.isEmpty(xml)) {
				return maps;
			}
			Document document = DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			for (Iterator<Element> itrProperty = root.elementIterator("property"); itrProperty.hasNext(); ) {
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				put(key.toUpperCase().trim(), value);
				maps.put(key,value);
				if (IS_DEBUG) {
					log.debug("[解析配置文件][{}={}]", key, value);
				}
			}
			map2field();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return maps;
	}
	public static void parse(InputStream is) {
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
					log.debug("[解析配置文件][{}={}]", key, value);
				}
			}
			map2field();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	//同步maps与静态属性值
	public static void map2field() {
		Field[] fields = ConfigTable.class.getDeclaredFields();
		for(Field field:fields) {
			String name = field.getName();
			if(configs.containsKey(name)) {
				BeanUtil.setFieldValue(null, field, configs.get(name));
			}
		}
	}
	protected static void loadConfig(File file) {
		try{
			if(IS_DEBUG) {
				log.debug("[加载配置文件] [file:{}]", file);
			}
			if(null != file && !file.exists()) {
				log.debug("[配置文件不存在] [file:{}]", file.getAbsolutePath());
				return;
			}
			if(file.isDirectory()) {
				List<File> files = FileUtil.getAllChildrenDirectory(file);
				for(File f:files) {
					loadConfig(f);
				}
			}else {
				parse(file);
				// 如果未设置重新加载时间, 则实现监听文件更新
				if(BasicUtil.parseInt(configs.get("RELOAD"), 0) == 0) {
					listener();
				}
			}

		}catch(Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}

	public static Object get(String key) {
		if(null == key) {
			return null;
		}
		Object val = null;
		if(reload > 0 && (System.currentTimeMillis() - lastLoadTime)/1000 > reload) {
			// 重新加载
			isLoading = false;
			init();
		}
		val = configs.get(key.toUpperCase().trim());
		if(null == val && null != environment) {
			val = environment.get(key);
		}
		return val;
	}
	public static String getString(String key) {
		Object val = get(key);
		if(null != val) {
			return val.toString();
		}
		return null;
	}
	public static String getString(String key, String def) {
		String val = getString(key);
		if(null == val) {
			val = def;
		}
		return val;
	}
	public static Object get(String key, Object def) {
		Object val = get(key);
		if(null == val) {
			val = def;
		}
		return val;
	}
	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	public static boolean getBoolean(String key, boolean def) {
		return BasicUtil.parseBoolean(get(key), def);
	}
	public static int getInt(String key) {
		return BasicUtil.parseInt(get(key), 0);
	}
	public static int getInt(String key, int def) {
		return BasicUtil.parseInt(get(key), def);
	}
	public static void put(String key, String value) {
		configs.put(key, value);
		if(IS_DEBUG) {
			log.debug("[ConfigTable动态更新][{}={}]", key, value);
		}
	}
	public static void put(String key, Object value) {
		configs.put(key, value);
		if(value instanceof Collection) {
			Collection cols = (Collection) value;
			int i = 0;
			for(Object col:cols) {
				configs.put(key + "[" + i++ + "]", col);
				configs.put(key + "." + i++, col);
			}
		}
		if(value instanceof Map) {
			Map map = (Map)value;
			for(Object k: map.keySet()) {
				configs.put(key + "[" + k + "]", map.get(k));
				configs.put(key + "." + k, map.get(k));
			}
		}
		if(IS_DEBUG) {
			log.debug("[ConfigTable动态更新][{}={}]", key, value);
		}
	}
	public static String getVersion() {
		return version;
	}
	public static String getMinVersion() {
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

	protected static void line(String src, String chr, int append, boolean center) {
		int len = 80 + append;
		String line = "";
		if(center) {
			int fillLeft = (len - src.length() -2)/2;
			int fillRight = (len - src.length() -2)/2;
			if((len - src.length())%2!=0) {
				fillRight ++;

			}
			line = "*"+BasicUtil.fillChar("", chr, fillLeft) + src +BasicUtil.fillChar("", chr, fillRight) +"*";
		}else{
			int fill = len - src.length() - 2;
			line = "*" + src + BasicUtil.fillChar("", chr, fill)+"";
		}
		System.out.println(line);
	}

	protected static void debug() {
		if(!IS_DEBUG) {
			return;
		}
		if(IS_LOG) {
			return;
		}
		IS_LOG = true;
		try{
			String time = null;
			String version = ConfigTable.version;
			String project = null;
			try{
				String path = ConfigTable.class.getResource("").getPath();
				path = path(path);
				if(path.contains("/WEB-INF")) {
					project = path.substring(0, path.indexOf("/WEB-INF"));
				}

				File file = new File(path);
				try {
					String anylineJarPath = ConfigTable.class.getProtectionDomain().getCodeSource().getLocation().getFile();
					String anylineJarName = new File(anylineJarPath).getName();
					if(anylineJarName.endsWith("jar") || anylineJarName.endsWith("jar!")) {
						if(anylineJarName.contains("-")) {
							version = anylineJarName.replace("anyline-","").replace(".jar","").replace("!","");
							version = version.substring(version.indexOf("-")+1);
						}
					}
					file = new File(anylineJarPath);
				}catch (Exception ignored) {}
				time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date(file.lastModified()));
			}catch(Exception e) {

			}

			System.out.println();
			line("","*", 0, true);
			line("Anyline Core [" + version + "]"," ", 0, true);
			line("http://doc.anyline.org "," ", 0, true);
			line(""," ", 0, true);
			if(null != time && time.startsWith("2")) {
				line("Last Modified " + "[" + time +"] "," ", 0, true);
			}else{
				line("MinVersion " +  "[" + minVersion + "]"," ", 0, true);
			}
			line(""," ", 0, true);
			line("","*", 0, true);
			//line("","*", 0, true);
			if(null != project) {
			//	line(" project root > " + project, "", 0, false);
			}
			//line(" debug status > anyline-config.xml:<property key=\"DEBUG\">boolean</property>","", 0, false);
			//line(" =================== 生产环境请务必修改密钥文件key.xml ========================","", 0, false);
			//line("","*", 0, true);
			System.out.println();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void setUpperKey(boolean bol) {
		IS_UPPER_KEY = bol;
	}
	public static void setLowerKey(boolean bol) {
		IS_LOWER_KEY = bol;
	}
	public boolean  IS_DEBUG() {
		return IS_DEBUG;
	}

	public boolean  IS_LOG_SQL() {
		return IS_LOG_SQL;
	}

	public boolean  IS_THROW_CONVERT_EXCEPTION() {
		return IS_THROW_CONVERT_EXCEPTION;
	}

	public long SLOW_SQL_MILLIS() {
		return SLOW_SQL_MILLIS;
	}

	public boolean  IS_LOG_SQL_PARAM() {
		return IS_LOG_SQL_PARAM;
	}

	public boolean IS_LOG_BATCH_SQL_PARAM() {return IS_LOG_BATCH_SQL_PARAM;}

	public boolean  IS_LOG_SQL_WHEN_ERROR() {
		return IS_LOG_SQL_WHEN_ERROR;
	}

	public boolean  IS_LOG_SQL_PARAM_WHEN_ERROR() {
		return IS_LOG_SQL_PARAM_WHEN_ERROR;
	}

	public boolean  IS_SQL_DEBUG() {
		return IS_SQL_DEBUG;
	}

	public boolean  IS_HTTP_LOG() {
		return IS_HTTP_LOG;
	}

	public boolean  IS_HTTP_PARAM_AUTO_TRIM() {
		return IS_HTTP_PARAM_AUTO_TRIM;
	}

	public boolean  IS_IGNORE_EMPTY_HTTP_KEY() {
		return IS_IGNORE_EMPTY_HTTP_KEY;
	}

	public int HTTP_PARAM_ENCODE() {
		return HTTP_PARAM_ENCODE;
	}

	public boolean  IS_MULTIPLE_SERVICE() {
		return IS_MULTIPLE_SERVICE;
	}

	public boolean IS_ENABLE_COMMON_JDBC_ADAPTER() {
		return IS_ENABLE_COMMON_JDBC_ADAPTER;
	}
	public boolean  IS_AUTO_CONVERT_BYTES() {
		return IS_AUTO_CONVERT_BYTES;
	}

	public boolean  IS_AUTO_SPLIT_ARRAY() {
		return IS_AUTO_SPLIT_ARRAY;
	}

	public boolean  IS_UPPER_KEY() {
		return IS_UPPER_KEY;
	}

	public boolean  IS_LOWER_KEY() {
		return IS_LOWER_KEY;
	}

	public boolean  IS_KEY_IGNORE_CASE() {
		return IS_KEY_IGNORE_CASE;
	}

	public boolean  IS_THROW_SQL_QUERY_EXCEPTION() {
		return IS_THROW_SQL_QUERY_EXCEPTION;
	}

	public boolean  IS_THROW_SQL_UPDATE_EXCEPTION() {
		return IS_THROW_SQL_UPDATE_EXCEPTION;
	}

	public boolean  IS_UPDATE_NULL_COLUMN() {
		return IS_UPDATE_NULL_COLUMN;
	}

	public boolean  IS_UPDATE_EMPTY_COLUMN() {
		return IS_UPDATE_EMPTY_COLUMN;
	}

	public boolean  IS_INSERT_NULL_COLUMN() {
		return IS_INSERT_NULL_COLUMN;
	}

	public boolean  IS_INSERT_EMPTY_COLUMN() {
		return IS_INSERT_EMPTY_COLUMN;
	}

	public boolean  IS_UPDATE_NULL_FIELD() {
		return IS_UPDATE_NULL_FIELD;
	}

	public boolean  IS_UPDATE_EMPTY_FIELD() {
		return IS_UPDATE_EMPTY_FIELD;
	}

	public boolean  IS_INSERT_NULL_FIELD() {
		return IS_INSERT_NULL_FIELD;
	}

	public boolean  IS_INSERT_EMPTY_FIELD() {
		return IS_INSERT_EMPTY_FIELD;
	}

	public String LIST2STRING_FORMAT() {
		return LIST2STRING_FORMAT;
	}

	public boolean  IS_REPLACE_EMPTY_NULL() {
		return IS_REPLACE_EMPTY_NULL;
	}

	public boolean  IS_SQL_DELIMITER_OPEN() {
		return IS_SQL_DELIMITER_OPEN;
	}
	public boolean IS_AUTO_CHECK_KEYWORD() {
		return IS_AUTO_CHECK_KEYWORD;
	}
	public boolean IS_AUTO_CHECK_EL_VALUE() {return IS_AUTO_CHECK_EL_VALUE;}

	public boolean  IS_SQL_DELIMITER_PLACEHOLDER_OPEN() {
		return IS_SQL_DELIMITER_PLACEHOLDER_OPEN;
	}

	public String SQL_DELIMITER_PLACEHOLDER() {
		return SQL_DELIMITER_PLACEHOLDER;
	}

	public boolean  IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL() {
		return IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL;
	}

	public boolean  IS_AUTO_CHECK_METADATA() {
		return IS_AUTO_CHECK_METADATA;
	}
	public boolean  IS_CHECK_EMPTY_SET_METADATA() {
		return IS_CHECK_EMPTY_SET_METADATA;
	}

	public boolean  IS_DISABLED_DEFAULT_ENTITY_ADAPTER() {
		return IS_DISABLED_DEFAULT_ENTITY_ADAPTER;
	}

	public boolean  IS_REMOVE_EMPTY_HTTP_KEY() {
		return IS_REMOVE_EMPTY_HTTP_KEY;
	}

	public boolean  IS_CACHE_DISABLED() {
		return IS_CACHE_DISABLED;
	}

	public String DEFAULT_PRIMARY_KEY() {
		return DEFAULT_PRIMARY_KEY;
	}

	public boolean  IS_OPEN_PRIMARY_TRANSACTION_MANAGER() {
		return IS_OPEN_PRIMARY_TRANSACTION_MANAGER;
	}

	public boolean  IS_OPEN_TRANSACTION_MANAGER() {
		return IS_OPEN_TRANSACTION_MANAGER;
	}

	public boolean PRIMARY_GENERATOR_UUID_ACTIVE() {
		return PRIMARY_GENERATOR_UUID_ACTIVE;
	}

	public boolean PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE() {
		return PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE;
	}

	public boolean PRIMARY_GENERATOR_RANDOM_ACTIVE() {
		return PRIMARY_GENERATOR_RANDOM_ACTIVE;
	}

	public boolean PRIMARY_GENERATOR_TIMESTAMP_ACTIVE() {
		return PRIMARY_GENERATOR_TIMESTAMP_ACTIVE;
	}

	public boolean PRIMARY_GENERATOR_TIME_ACTIVE() {
		return PRIMARY_GENERATOR_TIME_ACTIVE;
	}

	public int PRIMARY_GENERATOR_WORKER_ID() {
		return PRIMARY_GENERATOR_WORKER_ID;
	}

	public String PRIMARY_GENERATOR_PREFIX() {
		return PRIMARY_GENERATOR_PREFIX;
	}

	public int PRIMARY_GENERATOR_RANDOM_LENGTH() {
		return PRIMARY_GENERATOR_RANDOM_LENGTH;
	}

	public boolean PRIMARY_GENERATOR_UPPER() {
		return PRIMARY_GENERATOR_UPPER;
	}

	public boolean PRIMARY_GENERATOR_LOWER() {
		return PRIMARY_GENERATOR_LOWER;
	}

	public String PRIMARY_GENERATOR_TIME_FORMAT() {
		return PRIMARY_GENERATOR_TIME_FORMAT;
	}

	public int PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH() {
		return PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH;
	}

	public String SNOWFLAKE_TWEPOCH() {
		return SNOWFLAKE_TWEPOCH;
	}

	public int AFTER_ALTER_COLUMN_EXCEPTION_ACTION() {
		return AFTER_ALTER_COLUMN_EXCEPTION_ACTION;
	}
	public int SQL_QUERY_TIMEOUT() {
		return SQL_QUERY_TIMEOUT;
	}
	public int SQL_UPDATE_TIMEOUT() {
		return SQL_UPDATE_TIMEOUT;
	}
	public boolean  IS_DDL_AUTO_DROP_COLUMN() {
		return IS_DDL_AUTO_DROP_COLUMN;
	}

	public String SQL_STORE_DIR() {
		return SQL_STORE_DIR;
	}

	public boolean  IS_OPEN_PARSE_MYBATIS() {
		return IS_OPEN_PARSE_MYBATIS;
	}

	public String ENTITY_FIELD_COLUMN_MAP() {
		return ENTITY_FIELD_COLUMN_MAP;
	}

	public String ENTITY_CLASS_TABLE_MAP() {
		return ENTITY_CLASS_TABLE_MAP;
	}

	public String ENTITY_TABLE_ANNOTATION() {
		return ENTITY_TABLE_ANNOTATION;
	}

	public String ENTITY_COLUMN_ANNOTATION() {
		return ENTITY_COLUMN_ANNOTATION;
	}

	public String ENTITY_PRIMARY_KEY_ANNOTATION() {
		return ENTITY_PRIMARY_KEY_ANNOTATION;
	}

	public int ENTITY_FIELD_SELECT_DEPENDENCY() {
		return ENTITY_FIELD_SELECT_DEPENDENCY;
	}

	public int ENTITY_FIELD_INSERT_DEPENDENCY() {
		return ENTITY_FIELD_INSERT_DEPENDENCY;
	}

	public int ENTITY_FIELD_UPDATE_DEPENDENCY() {
		return ENTITY_FIELD_UPDATE_DEPENDENCY;
	}

	public int ENTITY_FIELD_DELETE_DEPENDENCY() {
		return ENTITY_FIELD_DELETE_DEPENDENCY;
	}

	public Compare ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE() {
		return ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE;
	}

	public String HTTP_PARAM_KEY_CASE() {
		return HTTP_PARAM_KEY_CASE;
	}

	public String METADATA_CACHE_KEY() {
		return METADATA_CACHE_KEY;
	}

	public int METADATA_CACHE_SECOND() {
		return METADATA_CACHE_SECOND;
	}

	public String MIX_DEFAULT_SEED() {
		return MIX_DEFAULT_SEED;
	}

	public String EL_ATTRIBUTE_PREFIX() {
		return EL_ATTRIBUTE_PREFIX;
	}
	public String GENERATOR_TABLES() {
		return GENERATOR_TABLES;
	}
	public int IGNORE_GRAPH_QUERY_RESULT_TOP_KEY() {
		return IGNORE_GRAPH_QUERY_RESULT_TOP_KEY;
	}
	public int IGNORE_GRAPH_QUERY_RESULT_TABLE() {
		return IGNORE_GRAPH_QUERY_RESULT_TABLE;
	}
	public int MERGE_GRAPH_QUERY_RESULT_TABLE() {
		return MERGE_GRAPH_QUERY_RESULT_TABLE;
	}

	public static void closeAllSqlLog() {
		put("IS_LOG_SQL", false);
		put("IS_LOG_SQL_WHEN_ERROR", false);
		put("IS_LOG_SQL_TIME", false);
		put("IS_LOG_SQL_PARAM", false);
		put("IS_LOG_SQL_PARAM_WHEN_ERROR", false);
		put("IS_LOG_SLOW_SQL", false);

		IS_LOG_SQL = false;
		IS_LOG_SQL_WHEN_ERROR = false;
		IS_LOG_SQL_TIME = false;
		IS_LOG_SQL_PARAM = false;
		IS_LOG_SQL_PARAM_WHEN_ERROR = false;
		IS_LOG_SLOW_SQL = false;
	}
	public static void openAllSqlLog() {
		put("IS_LOG_SQL", true);
		put("IS_LOG_SQL_WHEN_ERROR", true);
		put("IS_LOG_SQL_TIME", true);
		put("IS_LOG_SQL_PARAM", true);
		put("IS_LOG_SQL_PARAM_WHEN_ERROR", true);
		put("IS_LOG_SLOW_SQL", true);

		IS_LOG_SQL = true;
		IS_LOG_SQL_WHEN_ERROR = true;
		IS_LOG_SQL_TIME = true;
		IS_LOG_SQL_PARAM = true;
		IS_LOG_SQL_PARAM_WHEN_ERROR = true;
		IS_LOG_SLOW_SQL = true;
	}
}
