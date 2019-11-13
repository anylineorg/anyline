package org.anyline.entity;

import java.util.Hashtable;

import org.anyline.util.BasicConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

public class PageNaviConfig extends BasicConfig{

	
	public String STYLE_FILE_PATH 				= ""					; //样式文件路径
	public String SCRIPT_FILE_PATH 				= ""					; //脚本文件路径
	
	public String STYLE_BUTTON_FIRST			= "第一页"				; //第一页
	public String STYLE_BUTTON_PREV				= "上一页"				; //上一页
	public String STYLE_BUTTON_NEXT				= "下一页"				; //下一页
	public String STYLE_BUTTON_LAST				= "最后页"				; //最后页
	public String STYLE_BUTTON_JUMP				= "确定"					; //跳转到
	public String STYLE_LABEL_JUMP				= "转到第"				; //跳转到
	public String STYLE_LABEL_JUMP_PAGE			= "页"					; //跳转到
	public String STYLE_LOAD_MORE_FORMAT		= "加载更多"				; //加载更多
	public String STYLE_INDEX_ELLIPSIS			= "..."					; //下标省略符
	public String STYLE_DATA_EMPTY				= "没有更多内容了"			; //查询无数据
	public String STYLE_PAGE_OVER				= "最后一页了"				; //最后一页
	
	//变量
	public int VAR_PAGE_RANGE					= 5						; //下标数量
	public int VAR_PAGE_DEFAULT_VOL				= 10					; //每页多少条
	public int VAR_PAGE_MAX_VOL					= 100					; //每页最多多少条
	public boolean VAR_CLIENT_SET_VOL_ENABLE 	= false					; //前端是否可设置每页多少条
	public boolean VAR_SHOW_STAT				= false					; //是否显示分布统计
	public boolean VAR_SHOW_JUMP				= false					; //是否显示跳转
	public boolean VAR_SHOW_BUTTON				= true					; //是否显示上一页下一页button
	public boolean VAR_SHOW_INDEX				= true					; //是否显示下标
	public boolean VAR_SHOW_INDEX_ELLIPSIS		= false					; //是否显示下标省略符 (下标不含第2页或倒数第2页时显示省略号)1 .. 3 4 5 6 7 8 .. 10
	public String  VAR_FORM_METHOD				= "post"				;
	
	//key
	public String KEY_PAGE_ROWS			= "_anyline_page_rows"			; //设置每页显示多少条的key			
	public String KEY_PAGE_NO			= "_anyline_page"				; //设置当前第几页的key
	public String KEY_TOTAL_PAGE		= "_anyline_total_page"			; //显示一共多少页的key
	public String KEY_TOTAL_ROW			= "_anyline_total_row"			; //显示一共多少条的key
	public String KEY_SHOW_STAT			= "_anyline_navi_show_stat"		; //设置是否显示统计数据的key
	public String KEY_SHOW_JUMP			= "_anyline_navi_show_jump"		; //设置是否显示页数跳转key
	public String KEY_GUIDE				= "_anyline_navi_guide"			; //设置分页样式的key
	
	
	public int CONFIG_PAGE_VAL_SET_SORT = 1;	//1:VAR_PAGE_VOL_NUMBERS 2:STYLE_PAGE_VOL 
	//样式html
	public String STYLE_STAT_FORMAT				= "<div class='navi-summary'>共<span class='navi-total-row'>{totalRow}</span>条 第<span class='navi-cur-page'>{curPage}</span>/<span class='navi-total-page'>{totalPage}</span>页</div>";	//统计页数
	//用户选择每页多少条  STYLE_PAGE_VOL与STYLE_PAGE_VOL_NUMBERS 会相互覆盖 (以配置文件后设置的为准)
	//STYLE_PAGE_VOL_INDEX会引用STYLE_PAGE_VOL_CLASS所以要先设置STYLE_PAGE_VOL_CLASS
	public String STYLE_PAGE_VOL			= "<select class='navi-vol-set' id='navi_vol_set_{navi-conf-key}' onchange='_navi_change_vol({navi-conf-key})'><option value='10'>10 条/页</option><option value='20'>20 条/页</option><option value='30'>30 条/页</option><option value='40'>40 条/页</option><option value='50'>50 条/页</option><option value='100'>100 条/页</option></select>";
	public String VAR_PAGE_VOL_CLASS		= "navi-vol-set"	;
	public String VAR_PAGE_VOL_NUMBERS		= ""				; //用户选择每页多少条 10,20,30,40,50,100 如果设置了此属性将生成"<select class='navi-rows-set'><option value='10'>10 条/页</option>...</select>
		
	//位置分布 index:下标,stat:统计,jump:跳转到,vol:每页多少条
	public String VAR_COMPONENT_LAYOUT		= "{stat}{index}{vol}{jump}";
	
	public String EVENT_BEFORE				= "";
	public String EVENT_AFTER				= "";
	public String EVENT_REFRESH				= "";

	private static Hashtable<String,BasicConfig> instances = new Hashtable<String,BasicConfig>();

	//兼容上一版本 
	//最后一版key:倒数第二版key:倒数第三版key
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

		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - PageNaviConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载
			loadConfig();
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
	 */
	private synchronized static void loadConfig() {
		loadConfig(instances, PageNaviConfig.class, "anyline-navi.xml", compatibles);
		PageNaviConfig.lastLoadTime = System.currentTimeMillis();
	}

	protected void afterParse(String key, String value){

		if("VAR_PAGE_VOL_NUMBERS".equals(key) && BasicUtil.isNotEmpty(value)){
			CONFIG_PAGE_VAL_SET_SORT = 1;
		}
		if("STYLE_PAGE_VOL".equals(key) && BasicUtil.isNotEmpty(value)){
			CONFIG_PAGE_VAL_SET_SORT = 2;
		}
	}
	private static void debug(){
	}
}
