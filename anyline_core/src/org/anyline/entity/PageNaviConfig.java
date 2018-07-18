package org.anyline.entity;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PageNaviConfig extends BasicConfig{

	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();
	
	public String STYLE_FILE_PATH 				= ""		;	//样式文件路径
	public String SCRIPT_FILE_PATH 				= ""		;	//脚本文件路径
	//样式html
	public String STYLE_STAT_FORMAT				= "<div class='navi-summary'>共<span class='navi-total-row'>{totalRow}</span>条 第<span class='navi-cur-page'>{curPage}</span>/<span class='navi-total-page'>{totalPage}</span>页</div>";	//统计页数
	public String STYLE_BUTTON_FIRST			= "第一页"		;	//第一页
	public String STYLE_BUTTON_PREV				= "上一页"		;	//上一页
	public String STYLE_BUTTON_NEXT				= "下一页"		;	//下一页
	public String STYLE_BUTTON_LAST				= "最后页"		;	//最后页
	public String STYLE_BUTTON_JUMP				= "确定"			;	//跳转到
	public String STYLE_LABEL_JUMP				= "转到第"		;	//跳转到
	public String STYLE_LABEL_JUMP_PAGE			= "页"			;	//跳转到
	public String STYLE_LOAD_MORE_FORMAT		= "加载更多"		;	//加载更多
	
	//变量
	public int VAR_PAGE_RANGE					= 5			;	//下标数量
	public int VAR_PAGE_DEFAULT_VOL				= 10		;	//每页多少条
	public int VAR_PAGE_MAX_VOL					= 100		;	//每页最多多少条
	public boolean VAR_CLIENT_SET_VOL_ENABLE 	= false		; 	//前端是否可设置每页多少条
	public boolean VAR_SHOW_STAT				= false		;	//是否显示分布统计
	public boolean VAR_SHOW_JUMP				= false		;	//是否显示跳转
	public boolean VAR_SHOW_BUTTON				= true		;	//是否显示上一页下一页button
	public boolean VAR_SHOW_INDEX				= true		;	//是否显示下标
	
	//key
	public String KEY_PAGE_ROWS			= "_anyline_page_rows";
	public String KEY_PAGE_NO			= "_anyline_page";
	public String KEY_TOTAL_PAGE		= "_anyline_total_page";
	public String KEY_TOTAL_ROW			= "_anyline_total_row";
	public String KEY_SHOW_STAT			= "_anyline_navi_show_stat";
	public String KEY_SHOW_JUMP			= "_anyline_navi_show_jump";
	public String KEY_GUIDE				= "_anyline_navi_guide";
	static{
		init();
		debug();
	}
	public static void init() {
		//加载配置文件
		loadConfig();
	}

	public static PageNaviConfig getInstance(){
		return getInstance("default");
	}
	public static PageNaviConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		return (PageNaviConfig)instances.get(key);
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		adapt();
		try {
			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-navi.xml".equals(file.getName())){
					parseFile(PageNaviConfig.class, file, instances);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){}
	/**
	 * 兼容旧版本配置文件
	 */
	private static void adapt(){
		PageNaviConfig config = new PageNaviConfig();
		instances.put("default", config);
		SAXReader reader = new SAXReader();
		Map<String,String> keys = new HashMap<String,String>();
		keys.put("NAVI_TAG_FIRST", "STYLE_BUTTON_FIRST");
		keys.put("NAVI_TAG_PREV", "STYLE_BUTTON_PREV");
		keys.put("NAVI_TAG_NEXT", "STYLE_BUTTON_NEXT");
		keys.put("NAVI_TAG_LAST", "STYLE_BUTTON_LAST");
		keys.put("NAVI_TAG_GO", "STYLE_BUTTON_GO");
		keys.put("NAVI_LOAD_MORE_FORMAT", "STYLE_LOAD_MORE_FORMAT");
		keys.put("NAVI_STAT_FORMAT", "STYLE_STAT_FORMAT");
		keys.put("NAVI_STYLE_FILE_PATH", "STYLE_FILE_PATH");
		keys.put("NAVI_PAGE_RANGE", "VAR_PAGE_RANGE");
		keys.put("NAVI_SHOW_BUTTON", "VAR_SHOW_BUTTON");
		keys.put("NAVI_SHOW_INDEX", "VAR_SHOW_INDEX");
		keys.put("NAVI_SHOW_STAT", "VAR_SHOW_STAT");
		keys.put("NAVI_SHOW_JUMP", "VAR_SHOW_JUMP");
		keys.put("PAGE_DEFAULT_VOL", "VAR_PAGE_DEFAULT_VOL");
		keys.put("CLIENT_SET_PAGE_VOL_ENABLE", "VAR_CLIENT_SET_VOL_ENABLE");
		keys.put("NAVI_SCRIPT_FILE_PATH", "SCRIPT_FILE_PATH");
		keys.put("NAVI_STYLE_FILE_PATH", "STYLE_FILE_PATH");

		try{
			File dir = new File(ConfigTable.getWebRoot() , "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-config-navi.xml".equals(file.getName())){
					Document document = reader.read(file);
					Element root = document.getRootElement();
					for(Iterator<Element> item=root.elementIterator("property"); item.hasNext();){
						Element propertyElement = item.next();
						String key = propertyElement.attributeValue("key");
						String value = propertyElement.getTextTrim();
						key = keys.get(key);
						if(BasicUtil.isNotEmpty(key)){
							config.setValue(key,value);
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
