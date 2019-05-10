package org.anyline.jpush.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;

public class JPushConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	public String APP_KEY ="";
	public String MASTER_SECRET ="";
		
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static JPushConfig getInstance(){
		return getInstance("default");
	}
	public static JPushConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (JPushConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 */
	private synchronized static void loadConfig() {
		try {
			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-jpush.xml".equals(file.getName())){
					parseFile(JPushConfig.class, file, instances);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}