package org.anyline.nacos.util;

import org.anyline.entity.DataRow;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Hashtable;

@Component()
public class NacosConfig extends AnylineConfig{
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();

	public static String DEFAULT_ADDRESS			= null						;
	public static int DEFAULT_PORT 					= 8848						;
	public static int DEFAULT_TIMEOUT 				= 3000						;
	public static String DEFAULT_NAMESPACE 			= ""						;   // 注意这里的命名空间要写ID而不是NAME,如果用默认的public写成空白不要写public
	public static String DEFAULT_GROUP 				= "DEFAULT_GROUP"			;
	public static boolean DEFAULT_AUTO_SCAN 		= true						;
	public static String DEFAULT_SCAN_PACKAGE		= "org.anyline,org.anyboot"	;
	public static String DEFAULT_SCAN_CLASS			= ""						;



	public String ADDRESS		= DEFAULT_ADDRESS 		;
	public int PORT 			= DEFAULT_PORT			;
	public int TIMEOUT 			= DEFAULT_TIMEOUT		;
	public String NAMESPACE 	= DEFAULT_NAMESPACE		;   // 注意这里的命名空间要写ID而不是NAME,如果用默认的public写成空白不要写public
	public String GROUP 		= DEFAULT_GROUP			;
	public boolean AUTO_SCAN 	= DEFAULT_AUTO_SCAN		;
	public String SCAN_PACKAGE	= DEFAULT_SCAN_PACKAGE	;
	public String SCAN_CLASS	= DEFAULT_SCAN_CLASS	;


	@Value("${anyline.nacos.scan.packages:org.anyline,org.anyboot}")
	public String scanPackpage;
	@Value("${anyline.nacos.scan.types:}")
	public String scanClass;

	// boot
	@Value("${nacos.config.server-addr:}")
	public String bootAddress;

	@Value("${nacos.config.namespace:}")
	public String bootNamespace;

	@Value("${nacos.config.group:DEFAULT_GROUP}")
	public String bootGroup;

	// cloud
	@Value("${spring.cloud.nacos.config.server-addr:}")
	public String cloudAddress;

	@Value("${spring.cloud.nacos.config.namespace:}")
	public String cloudNamespace;

	@Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
	public String cloudGroup;


	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	static{
		init(); 
		debug(); 
	}
	public NacosConfig(){
		auto();
	}
	public void auto(){
		if(BasicUtil.isNotEmpty(bootAddress)){
			register("boot", bootAddress, 8848, bootGroup, bootNamespace, true, scanPackpage, scanClass);
		}
		if(BasicUtil.isNotEmpty(cloudAddress)){
			register("cloud", cloudAddress, 8848, cloudGroup, cloudNamespace, true, scanPackpage, scanClass);
		}
	}
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(NacosConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() { 
		// 加载配置文件 
		load();
	} 
 
	public static NacosConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static NacosConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - NacosConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载 
			load(); 
		}
		NacosConfig config = (NacosConfig)instances.get(key);
		return config;
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() { 
		load(instances, NacosConfig.class, "anyline-nacos.xml");
		NacosConfig.lastLoadTime = System.currentTimeMillis();
	}

	/**
	 * 注册nacos实例
	 * @param instance “default”
	 * @param address nacos 地址
	 * @param port 端口
	 * @param group group
	 * @param namespace namespace
	 * @param auto 是否自动扫描项目下的类
	 * @param pack 扫描的包名
	 * @param clazz 扫描clazz的子类
	 * @return NacosConfig
	 */
	public static NacosConfig register(String instance, String address, int port, String group, String namespace, boolean auto, String pack, String clazz) {
		DataRow row = new DataRow();
		row.put("ADDRESS", address);
		row.put("PORT", port);
		row.put("GROUP", group);
		row.put("NAMESPACE", namespace);
		row.put("AUTO_SCAN", auto);
		row.put("SCAN_PACKAGE", pack);
		row.put("SCAN_CLASS", clazz);
		NacosConfig config = parse(NacosConfig.class, instance, row, instances, compatibles);
		NacosUtil util = NacosUtil.getInstance(instance);
		if(null != util) {
			util.scan();
		}
		return config;
	}

	public static NacosConfig register(String instance, String address, int port, String group, String namespace) {
		return register(instance, address, port, group, namespace, DEFAULT_AUTO_SCAN, null, null);
	}
	public static NacosConfig register(String instance, String address, int port) {
		return register(instance, address, port, DEFAULT_GROUP, DEFAULT_NAMESPACE, DEFAULT_AUTO_SCAN, null, null);
	}
	public static NacosConfig register(String address, int port) {
		return register(DEFAULT_GROUP, address, port, DEFAULT_GROUP, DEFAULT_NAMESPACE, DEFAULT_AUTO_SCAN, null, null);
	}
	public static NacosConfig register(String address, int port, String pack, String clazz) {
		return register(DEFAULT_GROUP, address, port, DEFAULT_GROUP, DEFAULT_NAMESPACE, DEFAULT_AUTO_SCAN, pack, clazz);
	}
	private static void debug(){ 
	} 
}
