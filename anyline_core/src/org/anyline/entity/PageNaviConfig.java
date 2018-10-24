package org.anyline.entity;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;

public class PageNaviConfig extends BasicConfig{

	
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
	

	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();

	//兼容上一版本 最后一版key:倒数第二版key:倒数第三版key
	protected static String[] compatibles = {
		"STYLE_BUTTON_FIRST:NAVI_TAG_FIRST"
		,"STYLE_BUTTON_PREV:NAVI_TAG_PREV"
		,"STYLE_BUTTON_NEXT:NAVI_TAG_NEXT"
		,"STYLE_BUTTON_LAST:NAVI_TAG_LAST"
		,"STYLE_BUTTON_GO:NAVI_TAG_GO"
		,"STYLE_LOAD_MORE_FORMAT:NAVI_LOAD_MORE_FORMAT"
		,"STYLE_STAT_FORMAT:NAVI_STAT_FORMAT"
		,"STYLE_FILE_PATH:NAVI_STYLE_FILE_PATH"
		,"VAR_PAGE_RANGE:NAVI_PAGE_RANGE"
		,"VAR_SHOW_BUTTON:NAVI_SHOW_BUTTON"
		,"VAR_SHOW_INDEX:NAVI_SHOW_INDEX"
		,"VAR_SHOW_STAT:NAVI_SHOW_STAT"
		,"VAR_SHOW_JUMP:NAVI_SHOW_JUMP"
		,"VAR_PAGE_DEFAULT_VOL:PAGE_DEFAULT_VOL"
		,"VAR_CLIENT_SET_VOL_ENABLE:CLIENT_SET_PAGE_VOL_ENABLE"
		,"SCRIPT_FILE_PATH:NAVI_SCRIPT_FILE_PATH"
		,"STYLE_FILE_PATH:NAVI_STYLE_FILE_PATH"};
	 

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

	public static PageNaviConfig parse(String key, DataRow row){
		return parse(PageNaviConfig.class, key, row, instances,compatibles);
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
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void loadConfig() {
		try {
			File dir = new File(ConfigTable.getWebRoot(), "WEB-INF/classes");
			List<File> files = FileUtil.getAllChildrenFile(dir, "xml");
			for(File file:files){
				if("anyline-navi.xml".equals(file.getName())){
					parseFile(PageNaviConfig.class, file, instances,compatibles);
				}
			}
			
		} catch (Exception e) {
			log.error("配置文件解析异常:"+e);
		}
	}
	private static void debug(){
	}
}
