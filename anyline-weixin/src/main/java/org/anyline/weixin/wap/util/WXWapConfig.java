package org.anyline.weixin.wap.util; 
 
import java.io.File; 
import java.util.Hashtable; 
import java.util.List; 
 
import org.anyline.entity.DataRow; 
import org.anyline.entity.DataSet; 
import org.anyline.util.BasicConfig; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.util.FileUtil; 
import org.anyline.weixin.open.util.WXOpenConfig; 
import org.anyline.weixin.util.WXConfig; 
 
 
public class WXWapConfig extends WXConfig{ 
	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>(); 
 
	static{ 
		init(); 
		debug(); 
	} 
	public static void init() { 
		//加载配置文件 
		loadConfig(); 
	} 
 
	public static WXWapConfig getInstance(){ 
		return getInstance("default"); 
	} 
	public static WXWapConfig getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WXWapConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){ 
			//重新加载 
			loadConfig(); 
		} 
		return (WXWapConfig)instances.get(key); 
	} 
 
	public static WXWapConfig parse(String key, DataRow row){ 
		return parse(WXWapConfig.class, key, row, instances, compatibles); 
	} 
	public static Hashtable<String,BasicConfig> parse(String column, DataSet set){ 
		for(DataRow row:set){ 
			String key = row.getString(column); 
			parse(key, row); 
		} 
		return instances; 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void loadConfig() { 
		loadConfig(instances, WXWapConfig.class, "anyline-weixin-wap.xml",compatibles); 
		WXWapConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){ 
	} 
} 
