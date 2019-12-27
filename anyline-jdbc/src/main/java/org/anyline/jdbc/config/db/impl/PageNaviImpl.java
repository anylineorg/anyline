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
 
 
package org.anyline.jdbc.config.db.impl; 
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.PageNavi;
import org.anyline.entity.PageNaviConfig;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class PageNaviImpl implements PageNavi, Serializable{ 
	protected static final long serialVersionUID = 3593100423479113410L; 
	protected static final Logger log = LoggerFactory.getLogger(PageNaviImpl.class); 
 
	protected static final String BR 					= "\n"; 
	protected static final String TAB 					= "\t"; 
	protected static final String BR_TAB 				= "\n\t"; 
	 
	protected int totalRow					= 0			; //记录总数 
	protected int totalPage					= 0 		; //最大页数 
	protected int curPage					= 1 		; //当前页数 
	 
	protected int pageRange					= 10		; //显示多少个分页下标 
	protected int pageRows					= 10		; //每页多少条 
	protected int displayPageFirst 			= 0			; //显示的第一页标签 
	protected int displayPageLast 			= 0			; //显示的最后页标签 
	protected String baseLink				= null		; //基础URL 
	protected OrderStore orders				= null 		; //排序依据(根据 orderCol 排序分页) 
	protected int calType 					= 0			; //分页计算方式(0-按页数 1-按开始结束数) 
	protected int firstRow 					= 0			; //第一行 
	protected int lastRow 					= -1		; //最后一行 
	protected boolean lazy 					= false		; 
	protected String flag  					= ""		; //一个jsp中有多个分页时用来区分 
	protected long lazyPeriod 				= 0			; //总条数懒加载时间间隔(秒) 
	protected String lazyKey 				= null		; //懒加载 
	protected int type 						= 0			; //分页方式(0:下标 1:流式 2:根据浏览器状态 web:0,wap:1) 
	protected Map<String,List<Object>> params= null		; //查询参数 
	protected String method					= "post"	; 
	protected String style					= ""		; //样式标记对应anyline-navi.xml中的config.key 
	 
 
	protected boolean showStat = false; 
	protected boolean showJump = false; 
	protected boolean showVol = true; 
	protected String loadMoreFormat = ""; 
	 
	public PageNaviImpl(int totalRow, int curPage, int pageRows, String baseLink) { 
		this.totalRow = totalRow; 
		this.curPage = curPage; 
		setPageRows(pageRows); 
		this.baseLink = baseLink; 
	} 
	public PageNaviImpl(int curPage,int pageRows, String baseLink){ 
		this.curPage = curPage; 
		setPageRows(pageRows); 
		this.baseLink = baseLink; 
	} 
	public PageNaviImpl(String baseLink){ 
		this.curPage = 1; 
		this.baseLink = baseLink; 
	} 
	public static PageNavi parse(DataRow row){ 
		if(null == row){ 
			return null; 
		} 
		PageNavi navi = row.entity(PageNaviImpl.class); 
		return navi; 
	} 
	public PageNaviImpl(){} 
 
	/** 
	 * 分页计算方式 
	 * @param type	0-按页数 1-按开始结束记录数 
	 */ 
	public void setCalType(int type){ 
		this.calType = type; 
	} 
	public int getCalType(){ 
		return calType; 
	} 
	/** 
	 * 计算分页变量 
	 */ 
	public void calculate() { 
		int totalPage = (totalRow - 1) / pageRows + 1; 
		//当前页是否超出总页数 
		if(curPage > totalPage){ 
			curPage = totalPage; 
		} 
		setTotalPage(totalPage);					//总页数 
		setDisplayPageFirst(curPage - pageRange/2);				//显示的第一页 
		if(displayPageFirst > totalPage - pageRange){ 
			setDisplayPageFirst(totalPage - pageRange + 1); 
		} 
		if(displayPageFirst < 1){  
			setDisplayPageFirst(1); 
		} 
		 
		setDisplayPageLast(displayPageFirst + pageRange - 1);		//显示的最后页 
		if (displayPageLast > totalPage){ 
			setDisplayPageLast(totalPage); 
		} 
	} 
	 
	/** 
	 * 第一行 
	 * @return return
	 */ 
	public int getFirstRow(){ 
		if(calType == 0){ 
			if(curPage <= 0) { 
				return 0; 
			} 
			return (curPage-1) * pageRows; 
		}else{ 
			return firstRow; 
		} 
	} 
	/** 
	 * 最后一行 
	 * @return return
	 */ 
	public int getLastRow(){ 
		if(calType == 0){ 
			if(curPage == 0) { 
				return pageRows -1; 
			} 
			return curPage * pageRows - 1; 
		}else{ 
			return lastRow; 
		} 
	} 
	/** 
	 * 页面显示的第一页 
	 * @return return
	 */ 
	public int getDisplayPageFirst() { 
		return displayPageFirst; 
	} 
	/** 
	 * 设置页面显示的第一页 
	 * @param displayPageFirst  displayPageFirst
	 */ 
	public void setDisplayPageFirst(int displayPageFirst) { 
		this.displayPageFirst = displayPageFirst; 
	} 
	/** 
	 * 页面显示的最后一页 
	 * @return return
	 */ 
	public int getDisplayPageLast() { 
		return displayPageLast; 
	} 
	/** 
	 * 设置页面显示的最后一页 
	 * @param displayPageLast  displayPageLast
	 */ 
	public void setDisplayPageLast(int displayPageLast) { 
		this.displayPageLast = displayPageLast; 
	} 
 
	@SuppressWarnings("unchecked") 
	public void addParam(String key, Object value){ 
		if(null == key || null == value){ 
			return; 
		} 
		if(null == this.params){ 
			this.params = new HashMap<String,List<Object>>(); 
		} 
		List<Object> values = params.get(key); 
		if(null == values){ 
			values = new ArrayList<Object>(); 
		} 
		if(value instanceof Collection){ 
			values.addAll((Collection)value); 
		}else{ 
			values.add(value); 
		} 
		params.put(key, values); 
	} 
	public Object getParams(String key){ 
		Object result = null; 
		if(null != params){ 
			result = params.get(key); 
		} 
		return result; 
	} 
	@SuppressWarnings("unchecked") 
	public Object getParam(String key){ 
		Object result = null; 
		if(null != params){ 
			Object values = getParams(key); 
			if(null != values && values instanceof List){ 
				result = ((List)values).get(0); 
			}else{ 
				result = values; 
			} 
		} 
		return result; 
	} 
	public String getOrderText(boolean require){ 
		//return getOrderText(require, null); 
		return null; 
	} 
	public String getOrderText(boolean require, OrderStore store, String disKey){ 
		String result = ""; 
		if(null == orders){ 
			orders = store; 
		}else{ 
			if(null != store){ 
				for(Order order:store.getOrders()){ 
					orders.order(order); 
				} 
			} 
		} 
		if(null != orders){ 
			result = orders.getRunText(disKey); 
		} 
		if(require && result.length() == 0){ 
			result = "ORDER BY " + ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID"); 
		} 
		return result; 
	} 
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @return return
	 */ 
	public PageNavi order(Order order){ 
		if(null == orders){ 
			orders = new OrderStoreImpl(); 
		} 
		orders.order(order); 
		return this; 
	} 
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @param type  type
	 * @return return
	 */ 
	@Override 
	public PageNavi order(String order, String type){ 
		return order(new OrderImpl(order, type)); 
	} 
	@Override 
	public PageNavi order(String order){ 
		return order(new OrderImpl(order)); 
	} 
	 
	/** 
	 * 设置总行数 
	 * @param totalRow  totalRow
	 */ 
	@Override 
	public PageNavi setTotalRow(int totalRow) { 
		this.totalRow = totalRow; 
		calculate(); 
		return this; 
	} 
	/** 
	 * 设置最后一页 
	 * @param totalPage  totalPage
	 */ 
	@Override 
	public PageNavi setTotalPage(int totalPage) { 
		this.totalPage = totalPage; 
		return this; 
	} 
	/** 
	 * 设置当前页 
	 * @param curPage  curPage
	 */ 
	@Override 
	public PageNavi setCurPage(int curPage) { 
		this.curPage = curPage; 
		return this; 
	} 
	/** 
	 * 设置每页显示的行数 
	 * @param pageRows  pageRows
	 */ 
	@Override 
	public PageNavi setPageRows(int pageRows) { 
		if(pageRows > 0){ 
			this.pageRows = pageRows; 
		} 
		return this; 
	} 
	@Override 
	public int getTotalRow() { 
		return totalRow; 
	} 
 
	@Override 
	public int getTotalPage() { 
		return totalPage; 
	} 
 
	@Override 
	public int getCurPage() { 
		return curPage; 
	} 
 
	@Override 
	public int getPageRows() { 
		return pageRows; 
	} 
 
	@Override 
	public String getBaseLink() { 
		return baseLink; 
	} 
	@Override 
	public PageNavi setBaseLink(String baseLink) { 
		this.baseLink = baseLink; 
		return this; 
	} 
	@Override 
	public PageNavi setFirstRow(int firstRow) { 
		this.firstRow = firstRow; 
		return this; 
	} 
	@Override 
	public PageNavi setLastRow(int lastRow) { 
		this.lastRow = lastRow; 
		return this; 
	} 
	 
	@Override 
	public boolean isLazy() { 
		return this.lazy; 
	} 
	@Override 
	public long getLazyPeriod() { 
		return this.lazyPeriod; 
	} 
	@Override 
	public PageNavi setLazy(long ms) { 
		this.lazy = true; 
		this.lazyPeriod = ms; 
		return this; 
	} 
	@Override 
	public PageNavi setLazyPeriod(long ms){ 
		this.lazy = true; 
		this.lazyPeriod = ms; 
		return this; 
	} 
	 
	@Override 
	public PageNavi setLazyKey(String key) { 
		this.lazyKey = key; 
		return this; 
	} 
	@Override 
	public String getLazyKey() { 
		return this.lazyKey; 
	} 
	 
	public String getFlag() { 
		return flag; 
	} 
	public PageNavi setFlag(String flag) { 
		this.flag = flag; 
		return this; 
	} 
	public boolean isShowStat() { 
		return showStat; 
	} 
	public PageNavi setShowStat(boolean showStat) { 
		this.showStat = showStat; 
		return this; 
	} 
	public boolean isShowJump() { 
		return showJump; 
	} 
	public PageNavi setShowJump(boolean showJump) { 
		this.showJump = showJump; 
		return this; 
	} 
	public int getType() { 
		return type; 
	} 
	public PageNavi setType(int type) { 
		this.type = type; 
		return this; 
	} 
	public String getStyle() { 
		return style; 
	} 
	public PageNavi setStyle(String style) { 
		this.style = style; 
		return this; 
	} 
	public String getGuide() { 
		return loadMoreFormat; 
	} 
	public PageNavi setGuide(String guide) { 
		this.loadMoreFormat = guide; 
		return this; 
	} 
	public String getMethod() { 
		return method; 
	} 
	public PageNavi setMethod(String method) { 
		this.method = method; 
		return this; 
	}
	public boolean isShowVol() {
		return showVol;
	}
	public PageNavi setShowVol(boolean showVol) {
		this.showVol = showVol;
		return this;
	}
 
	/** 
	 *  @param creater creater
	 * @return return
	 */ 
	public String html(String creater){ 
		PageNaviConfig config = PageNaviConfig.getInstance(style); 
		calculate(); 
		StringBuilder navi = new StringBuilder(); 
		StringBuilder layout = new StringBuilder(); 
		StringBuilder index = new StringBuilder(); 
		StringBuilder stat = new StringBuilder(); 
		StringBuilder vol = new StringBuilder(); 
		StringBuilder jump = new StringBuilder(); 
		String configVarKey = ""; 
		if(null == flag){ 
			flag = ""; 
		} 
		if("ajax".equals(creater)){ 
			configVarKey = config.KEY_ID_FLAG + flag;	//_anyline_navi_conf_0 
		} 
		if("html".equals(creater)){ 
			navi.append("<link rel=\"stylesheet\" href=\"" + config.STYLE_FILE_PATH + "\" type=\"text/css\"/>\n"); 
			navi.append("<script type=\"text/javascript\" src=\"" + config.SCRIPT_FILE_PATH + "\"></script>\n"); 
		} 
		if("html".equals(creater)){ 
			navi.append("<form class=\"form\" action=\"" + baseLink + "\" method=\"post\">\n"); 
		} 
		if("ajax".equals(creater)){ 
			navi.append("<div class=\"form\">\n"); 
		} 
		//当前页 
		navi.append("<input type='hidden' id='hid_cur_page_"+flag+"' name='"+config.KEY_PAGE_NO+"' class='_anyline_navi_cur_page' value='"+curPage+"'/>\n"); 
		//共多少页 
		navi.append("<input type='hidden' id='hid_total_page_"+flag+"' name='"+config.KEY_TOTAL_PAGE+"' class='_anyline_navi_total_page' value='"+totalPage+"'/>\n"); 
		//共多少条 
		navi.append("<input type='hidden' id='hid_total_row_"+flag+"' name='"+config.KEY_TOTAL_ROW+"' class='_anyline_navi_total_row' value='"+totalRow+"'/>\n"); 
		//每页显示多少条 
		if(config.VAR_CLIENT_SET_VOL_ENABLE){ 
			navi.append("<input type='hidden' id='hid_page_rows_key_"+flag+"'  class='_anyline_navi_page_rows_key' value='"+config.KEY_PAGE_ROWS+"'/>\n"); 
			navi.append("<input type='hidden' id='hid_page_rows_"+flag+"' name='"+config.KEY_PAGE_ROWS+"' class='_anyline_navi_page_rows' value='"+pageRows+"'/>\n"); 
		} 
		if("ajax".equals(creater)){ 
			navi.append("<input type='hidden' class='"+config.KEY_ID_FLAG+"' value='" + flag + "'/>"); 
		} 
		navi.append(createHidParams(config)); 
		navi.append("<div class=\"anyline_navi\">\n"); 
		//数据统计 
		String statFormat = config.STYLE_STAT_FORMAT;  
		statFormat = statFormat.replace("{totalRow}", totalRow+"").replace("{curPage}", curPage+"").replace("{totalPage}", totalPage+""); 
		statFormat = statFormat.replace("{total-row}", totalRow+"").replace("{cur-page}", curPage+"").replace("{total-page}", totalPage+""); 
		if(showStat){ 
			stat.append(statFormat).append("\n"); 
		} 
		int range = config.VAR_PAGE_RANGE; 
		int fr = NumberUtil.max(1,curPage - range/2); 
		int to = fr + range - 1; 
		boolean match = false; 
		if(totalPage > range && curPage>range/2){ 
			match = true; 
		} 
		if(match){ 
			to = curPage + range/2; 
		} 
		if(totalPage - curPage < range/2){ 
			fr = totalPage - range; 
		} 
		fr = NumberUtil.max(fr, 1); 
		to = NumberUtil.min(to, totalPage); 
		 
		if(type ==0){ //下标导航 
			//每页多少条 
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[vol set][enable:{}][vol:{}][sort:{}]",config.VAR_CLIENT_SET_VOL_ENABLE,pageRows,config.CONFIG_PAGE_VAL_SET_SORT); 
			} 
			if(config.VAR_CLIENT_SET_VOL_ENABLE){ 
				if(config.CONFIG_PAGE_VAL_SET_SORT == 2){ 
					vol.append(config.STYLE_PAGE_VOL.replace("{navi-conf}", configVarKey).replace("{navi-conf-key}", flag)); 
				}else{ 
					String[] nums = config.VAR_PAGE_VOL_NUMBERS.split(","); 
					String clazz = config.VAR_PAGE_VOL_CLASS; 
					if(BasicUtil.isEmpty(clazz)){ 
						clazz = "navi-rows-set"; 
					} 
					vol.append("<select class='").append(clazz).append("' id='navi_val_set_").append(flag).append("' onchange='_navi_change_vol(").append(configVarKey).append(")'>"); 
					for(String num:nums){ 
						vol.append("<option value='").append(num).append("' id='navi_val_set_").append(flag).append("_item_").append(num).append("'"); 
						if(pageRows == BasicUtil.parseInt(num, 0)){ 
							vol.append(" selected=\"selected\""); 
						} 
						vol.append(">").append(num).append(" 条/页</option>\n"); 
					} 
					vol.append("</select>"); 
				} 
 
			} 
			 
			//config.VAR_SHOW_INDEX_ELLIPSIS;是否显示下标省略号(不显示��2页或倒数第2页时显示省略号) 
			//1 .. 3 4 5 6 7 8 .. 10 
			if(config.VAR_SHOW_INDEX_ELLIPSIS){ 
				if(config.VAR_SHOW_BUTTON){ 
					createPageTag(index, "navi-button navi-prev-button", config.STYLE_BUTTON_PREV, NumberUtil.max(curPage-1,1), configVarKey); 
				} 
				//下标 
				if(config.VAR_SHOW_INDEX){ 
					if(fr<2){ 
						fr = 2; 
					} 
					if(to>totalPage-1){ 
						to = totalPage-1; 
					} 
					index.append("<div class='navi-num-border'>\n"); 
					createPageTag(index, "navi-num-item", "1", 1, configVarKey); 
					if(fr > 2){ 
						createPageTag(index, "navi-num-item", config.STYLE_INDEX_ELLIPSIS, 0, configVarKey); 
					} 
					for(int i=fr; i<=to; i++){ 
						createPageTag(index, "navi-num-item", i + "", i, configVarKey); 
					} 
					if(to < totalPage-1){ 
						createPageTag(index, "navi-num-item", config.STYLE_INDEX_ELLIPSIS, 0, configVarKey); 
					} 
					if(totalPage >1){//不是只有一页 
						createPageTag(index, "navi-num-item", totalPage+"", totalPage, configVarKey); 
					} 
					index.append("</div>\n"); 
				} 
				//下一页 最后页 
				if(config.VAR_SHOW_BUTTON){ 
					createPageTag(index, "navi-button navi-next-button", config.STYLE_BUTTON_NEXT, (int)NumberUtil.min(curPage+1, totalPage), configVarKey); 
				} 
			}else{ 
				//上一页  第一页 
				if(config.VAR_SHOW_BUTTON){ 
					createPageTag(index, "navi-button navi-first-button", config.STYLE_BUTTON_FIRST, 1, configVarKey); 
					createPageTag(index, "navi-button navi-prev-button", config.STYLE_BUTTON_PREV, NumberUtil.max(curPage-1,1), configVarKey); 
				} 
				//下标 
				if(config.VAR_SHOW_INDEX){ 
					index.append("<div class='navi-num-border'>\n"); 
					for(int i=fr; i<=to; i++){ 
						createPageTag(index, "navi-num-item", i + "", i, configVarKey); 
					} 
					index.append("</div>\n"); 
				} 
				//下一页 最后页 
				if(config.VAR_SHOW_BUTTON){ 
					createPageTag(index, "navi-button navi-next-button", config.STYLE_BUTTON_NEXT, (int)NumberUtil.min(curPage+1, totalPage), configVarKey); 
					createPageTag(index, "navi-button navi-last-button", config.STYLE_BUTTON_LAST, totalPage, configVarKey); 
				} 
			} 
			//VOL位置:下标之后 
//			if("page".equalsIgnoreCase(config.VAR_PAGE_VOL_INDEX)){ 
//				builder.append(vol); 
//			} 
			//跳转到 
			if(showJump){ 
				jump.append(config.STYLE_LABEL_JUMP) 
				.append("<input type='text' value='") 
				.append(curPage) 
				.append("' class='navi-go-txt _anyline_jump_txt' id='hid_jump_txt_"+flag+"' onkeydown='_navi_jump_enter("+configVarKey+")'/>") 
				.append(config.STYLE_LABEL_JUMP_PAGE) 
				.append("<span class='navi-go-button' onclick='_navi_jump("+configVarKey+")'>") 
				.append(config.STYLE_BUTTON_JUMP).append("</span>\n"); 
			} 
			//VOL位置:最后 
//			if("last".equalsIgnoreCase(config.VAR_PAGE_VOL_INDEX)){ 
//				builder.append(vol); 
//			} 
		}else if(type == 1){ 
			//加载更多 
			createPageTag(index, "navi-more-button", loadMoreFormat, (int)NumberUtil.min(curPage+1, totalPage+1), configVarKey); 
		} 
		 
		 
		String layout_html = config.VAR_COMPONENT_LAYOUT; 
		if(null == layout_html){ 
			layout_html = "{navi-stat}{navi-index}{navi-vol}{navi-jump}"; 
		} 
		layout_html = layout_html.replace("{stat}", stat.toString()); 
		layout_html = layout_html.replace("{index}", index.toString()); 
		layout_html = layout_html.replace("{vol}", vol.toString()); 
		layout_html = layout_html.replace("{jump}", jump.toString()); 
 
		layout_html = layout_html.replace("{navi-stat}", stat.toString()); 
		layout_html = layout_html.replace("{navi-index}", index.toString()); 
		layout_html = layout_html.replace("{navi-vol}", vol.toString()); 
		layout_html = layout_html.replace("{navi-jump}", jump.toString()); 
		navi.append(layout_html); 
		navi.append("</div>"); 
 
		if("html".equals(creater)){ 
			navi.append("</form>\n"); 
		} 
		if("ajax".equals(creater)){ 
			navi.append("</div>\n"); 
		} 
		return navi.toString(); 
	} 
	/** 
	 *  
	 * @param builder  builder
	 * @param clazz   clazz
	 * @param tag 显示内容 
	 * @param page 跳到第几页 
	 * @param configFlag  configFlag
	 */ 
	private void createPageTag(StringBuilder builder, String clazz, String tag, int page, String configFlag){ 
		builder.append("<span class ='").append(clazz); 
		if(page == curPage && 0 == type){ 
			if(clazz.contains("navi-num-item")){//下标 
				builder.append(" navi-num-item-cur"); 
			}else{//btn 
				builder.append(" navi-disabled"); 
			} 
			builder.append("'"); 
		}else{ 
			builder.append("'"); 
			if(page>0){ 
				builder.append(" onclick='_navi_go(").append(page); 
				if(BasicUtil.isNotEmpty(configFlag)){ 
					builder.append(",").append(configFlag); 
				} 
				builder.append(")'"); 
			} 
		} 
		builder.append(">"); 
		builder.append(tag).append("</span>\n"); 
	} 
	//创建隐藏参数 
	private String createHidParams(PageNaviConfig config){ 
		String html = ""; 
		try{ 
			if(null != params){ 
				for(Iterator<String> itrKey=params.keySet().iterator(); itrKey.hasNext();){ 
					String key = itrKey.next(); 
					Object values = params.get(key); 
					html += createHidParam(key,values); 
				} 
			} 
			html += createHidParam(config.KEY_SHOW_STAT,showStat); 
			html += createHidParam(config.KEY_SHOW_JUMP,showJump); 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		return html; 
	} 
	 
	public String createHidParam(String name, Object values) { 
		String html = ""; 
		if(null == values){ 
			html = "<input type='hidden' name='"+name+"' value=''>\n"; 
		}else{ 
			if(values instanceof Collection<?>){ 
				Collection<?> list = (Collection<?>)values; 
				for(Object obj:list){ 
					html += "<input type='hidden' name='"+name+"' value='"+obj+"'>\n"; 
				} 
			}else{ 
				html += "<input type='hidden' name='"+name+"' value='"+values+"'>\n"; 
			} 
		} 
		return html; 
	} 
	 
	public String toString(){ 
		return html("html"); 
	} 
}
