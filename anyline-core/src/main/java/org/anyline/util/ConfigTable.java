/* 
 * Copyright 2006-2015 www.anyline.org
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class ConfigTable {
	private static final Logger log = Logger.getLogger(ConfigTable.class);
	protected static String root;
	protected static String webRoot;
	protected static String classpath;
	protected static Hashtable<String,String> configs;
	protected static long lastLoadTime = 0;	//最后一次加载时间
	protected static int reload = 0;			//重新加载间隔
	protected static boolean debug = false;
	protected static boolean sqlDebug = false;
	protected static final String version = "8.1.5";
	protected static final String minVersion = "0007";
	protected static boolean isLoading = false;
	public static boolean  IS_UPPER_KEY = true;
	public static boolean  IS_LOWER_KEY = false;
	static{
		init();
		debug();
	}
	protected ConfigTable(){}
	public static void addConfig(String ... files){
		if(null == files){
			return;
		}
		for(String file:files){
			loadConfig(new File(file));
		}
	}
	public static void addConfig(File ... files){
		if(null == files){
			return;
		}
		for(File file:files){
			loadConfig(file);
		}
	}
	public static Hashtable<String,String> getConfigs(){
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
	public static void init(){
		init("anyline");
	}
	public static void init(String flag) {
		//log.warn("IS LOAD:"+isLoad);
		if(isLoading){
			return;
		}
		//中断递归
		lastLoadTime = System.currentTimeMillis();
		isLoading = true;
		String path =  "";
		try{
			//path = ConfigTable.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			path = ConfigTable.class.getResource("/").getPath();
		}catch(Exception e){
			e.printStackTrace();
		}
		Properties props=System.getProperties(); //获得系统属性集    
		String osName = props.getProperty("os.name"); //操作系统名称    
		if(null != osName && osName.toUpperCase().contains("WINDOWS") && path.startsWith("/")){
			path = path.substring(1);
		}

		if(null == root && null != path){
			root = path;
			if(path.indexOf("bin") > 0){
				root = path.substring(0,path.indexOf("bin")-1);	
			}
		}
		if(null == webRoot && null != path){
			webRoot = path;
			if(path.indexOf("WEB-INF") > 0){
				webRoot = path.substring(0,path.indexOf("WEB-INF")-1);	
			}
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
		//加载配置文件
		loadConfig(flag);
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	protected synchronized static void loadConfig(String flag) {
		try {
			if(null == configs){
				configs = new Hashtable<String,String>();
			}
			if(null != root){
				configs.put("HOME_DIR", root);
			}
			//classpath根目录
			File dir = new File(classpath);
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
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
			e.printStackTrace();
		}
		lastLoadTime = System.currentTimeMillis();
		reload = getInt("RELOAD");
		debug = getBoolean("DEBUG");
		sqlDebug = getBoolean("SQL_DEBUG");
		String isUpper = getString("IS_UPPER_KEY");
		if(null != isUpper){
			if("false".equals(isUpper.toLowerCase()) || "0".equals(isUpper)){
				IS_UPPER_KEY = false;
			}
		}
	}
	protected static void loadConfig(File file){
		try{
			if(isDebug()){
				log.info("[加载配置文件] [file:" + file.getAbsolutePath() + "]");
			}
			if(null != file && !file.exists()){
				log.info("[配置文件不存在] [file:" + file.getAbsolutePath() + "]");
				return;
			}
			if(file.isDirectory()){
				List<File> files = FileUtil.getAllChildrenDirectory(file);
				for(File f:files){
					loadConfig(f);
				}
			}
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				configs.put(key.toUpperCase().trim(), value);
				if(isDebug()){
					log.info("[解析配置文件] [" + key + " = " + value+"]");
				}
			}
		}catch(Exception e){
			log.error("配置文件解析异常:"+e);
		}
	}
	public static String get(String key){
		if(null == key){
			return null;
		}
		String val = null;
		if(reload > 0 && (System.currentTimeMillis() - lastLoadTime)/1000 > reload){
			//重新加载
			isLoading = false;
			init();
		}
		val = configs.get(key.toUpperCase().trim());
		return val;
	}
	public static String getString(String key) {
		return get(key);
	}
	public static String getString(String key, String def){
		String val = getString(key);
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
		if(isDebug()){
			log.warn("[ConfigTable动态更新]["+key+":"+value+"]");
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
	public static void setDebug(boolean bol){
		debug = bol;
	}
	public static boolean isDebug() {
		return debug;
	}
	public static boolean isSQLDebug() {
		return sqlDebug;
	}
	
	
	
	protected static void line(String src, String chr, boolean center){
		int len = 80;
		int fill = 0 ;
		String line = "";
		if(center){
			fill = (len - src.length() -2)/2;
			line = "*"+BasicUtil.fillChar("", chr, fill) + src +BasicUtil.fillChar("", chr, fill) +"*";
		}else{
			fill = len - src.length() - 2;
			line = "*" + src + BasicUtil.fillChar("", chr, fill)+"";
		}
		System.out.println(line);
	}
	
	protected static void debug(){
		if(!isDebug()){
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
				Properties props=System.getProperties(); //获得系统属性集    
				String osName = props.getProperty("os.name"); //操作系统名称
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
				String fileName = file.getName();
				if(fileName.endsWith("jar") && fileName.contains("-")){
					version = fileName.substring(fileName.lastIndexOf("-")+1, fileName.indexOf(".jar"));
				}
				time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date(file.lastModified()));
			}catch(Exception e){
				
			}
			
			line("","*", true);
			line("Anyline Core " + version, " ", true);
			line("anyline.org ", " ", true);
			line(""," ", true);
			if(null != time && time.startsWith("2")){
				line("Last Modified " + "[" + time +"] ", " ", true);
			}else{
				line("MinVersion " +  "[" + minVersion + "]", " ", true);
			}
			line(""," ", true);
			line("","*", true);
//			line(" github.con  git地址：https://github.com/anylineorg/anyline.git", "", false);
//			//line(" github.com  git帐号：public@anyline.org(anyline111111)", "", false);
//			line(" ", " ", false);
//			line(" oschina.net git地址：https://git.oschina.net/anyline/anyline.git", "", false);
//			line(" oschina.net svn地址：svn://git.oschina.net/anyline/anyline", "", false);
			//line(" oschina.net 帐号密码：public@anyline.org(111111)", "", false);
			line("","*", true);
			if(null != project){
				line(" project > " + project, "", false);
			}
			line(" debug状态 > anyline-config.xml:<property key=\"DEBUG\">boolean</property>", "", false);
			line(" =====================生产环境请务必修改密钥文件key.xml==========================", "", false);
			line("","*", true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
