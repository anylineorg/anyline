package org.anyline.aliyun.oss.util;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;

public class OSSConfig extends BasicConfig{
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	private static File configDir;
	public String ACCESS_ID		= "";
	public String ACCESS_SECRET = "";
	public String ENDPOINT		= "";
	public String BUCKET		= "";
	public int EXPIRE_SECOND 	= 3600;
	
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}
	public static void setConfigDir(File dir){
		configDir = dir;
		init();
	}
	public static OSSConfig getInstance(){
		return getInstance("default");
	}
	public static OSSConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (OSSConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		try {
			File dir = configDir;
			if(null == dir){
				dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			}
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-aliyun-oss.xml".equals(file.getName())){
					parseFile(OSSConfig.class, file, instances);
				}
			}
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
