/*
 * Copyright 2006-2025 www.anyline.org
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

 

package org.anyline.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.NumberUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.beans.Transient;
import java.io.Serializable;
import java.util.*;

public class DefaultPageNavi implements PageNavi, Serializable, Cloneable {
	protected static final long serialVersionUID = 3593100423479113410L;
	protected static final Log log = LogProxy.get(DefaultPageNavi.class);
 
	protected static final String BR 					= "\n"; 
	protected static final String TAB 					= "\t"; 
	protected static final String BR_TAB 				= "\n\t"; 

	protected int dataSize					= 0			; // 查询结果行数
	protected long totalRow					= 0			; // 记录总数 (rows)
	protected long totalPage				= 0 		; // 最大页数 (pages)
	protected long curPage					= 1 		; // 当前页数
	 
	protected int pageRange					= 10		; // 显示多少个分页下标
	protected int pageRows					= 10		; // 每页多少条
	protected long displayPageFirst 		= 0			; // 显示的第一页标签
	protected long displayPageLast 			= 0			; // 显示的最后页标签

	protected Boolean requiredTotal			= null		; // map方法时默认不需要总行数
	protected Boolean autoCount 			= PageNaviConfig.IS_AUTO_COUNT;
	protected String baseLink				= null		; // 基础URL
	protected OrderStore orders				= null 		; // 排序依据(根据 orderCol 排序分页)
	protected int calType 					= 0			; // 分页计算方式(0-按页数 1-按开始结束数)
	protected long firstRow 				= 0			; // 第一行
	protected long lastRow 					= -1		; // 最后一行
	protected boolean lazy 					= false		; // 是否懒加载
	protected String flag  					= ""		; // 一个jsp中有多个分页时用来区分
	protected long lazyPeriod 				= 0			; // 总条数懒加载时间间隔(秒)
	protected String lazyKey 				= null		; // 懒加载
	protected int type 						= 0			; // 分页方式(0:下标 1:流式 2:根据浏览器状态 web:0, wap:1)
	protected Map<String, List<Object>> params= null		; // 查询参数
	protected String method					= "post"	; 
	protected String style					= ""		; // 样式标记对应anyline-navi.xml中的config.key
 
	protected boolean showStat 				= true		;
	protected boolean showJump 				= true		;
	protected boolean showVol 				= true		;
	protected String loadMoreFormat 		= ""		;
	protected Long maxPage					= null		; //最大页数，超出后显示maxPage

	public DefaultPageNavi() {}

	/**
	 * Page
	 * @param page 当前第几页
	 */
	public DefaultPageNavi(long page) {
		this.curPage = page;
	}

	/**
	 * Page
	 * @param page 当前第几页
	 * @param vol 每页多少行
	 */
	public DefaultPageNavi(long page, int vol) {
		this.curPage = page;
		this.pageRows = vol;
	}
	public DefaultPageNavi(long totalRow, long curPage, int pageRows, String baseLink) {
		this.totalRow = totalRow;
		this.curPage = curPage;
		setPageRows(pageRows);
		this.baseLink = baseLink;
	}
	public DefaultPageNavi(long curPage, int pageRows, String baseLink) {
		this.curPage = curPage;
		setPageRows(pageRows);
		this.baseLink = baseLink;
	}
	public DefaultPageNavi(String baseLink) {
		this.curPage = 1; 
		this.baseLink = baseLink; 
	}
	public static PageNavi parse(DataRow row) {
		if(null == row) {
			return null; 
		} 
		PageNavi navi = row.entity(DefaultPageNavi.class);
		return navi; 
	}

	public PageNavi scope(long first, long last) {
		setFirstRow(first);
		setLastRow(last);
		setCalType(1);
		autoCount(false);
		//setTotalRow(last-first+1);
		return this;
	}
	public PageNavi limit(long offset, int rows) {
		setFirstRow(offset);
		setLastRow(offset + rows - 1);
		setCalType(1);
		autoCount(false);
		return this;
	}

	public PageNavi autoCount(Boolean auto) {
		this.autoCount = auto;
		return this;
	}
	public Boolean autoCount() {
		return autoCount;
	}
	@Override
	public PageNavi setDataSize(int size) {
		this.dataSize = size;
		return this;
	}

	@Override
	public int getDataSize() {
		return this.dataSize;
	}

	/**
	 * 分页计算方式 
	 * @param type	0-按页数 1-按开始结束记录数 
	 */ 
	public PageNavi setCalType(int type) {
		this.calType = type;
		return this;
	} 
	public int getCalType() {
		return calType; 
	} 
	/** 
	 * 计算分页变量 
	 */ 
	public PageNavi calculate() {
		long totalPage = (totalRow - 1) / pageRows + 1;
		long curPage = getCurPage();
		// 当前页是否超出总页数 
		if(curPage > totalPage) {
			PageNaviConfig config = PageNaviConfig.getInstance(style);
			if(null != config &&  config.VAR_LIMIT_SCOPE) {
				curPage = totalPage; // 超出范围 查最后一页
			}
		}

		setTotalPage(totalPage);					// 总页数 
		setDisplayPageFirst(NumberUtil.min(curPage, totalPage) - pageRange/2);				// 显示的第一页
		if(displayPageFirst > totalPage - pageRange) {
			setDisplayPageFirst(totalPage - pageRange + 1); 
		} 
		if(displayPageFirst < 1) {
			setDisplayPageFirst(1); 
		} 
		 
		setDisplayPageLast(displayPageFirst + pageRange - 1);		// 显示的最后页 
		if (displayPageLast > totalPage) {
			setDisplayPageLast(totalPage); 
		}
		return this;
	} 
	 
	/** 
	 * 第一行 
	 * @return int
	 */ 
	public long getFirstRow() {
		if(calType == 0) {
			if(getCurPage() <= 0) {
				return 0; 
			} 
			return (getCurPage()-1) * pageRows;
		}else{
			return firstRow; 
		} 
	} 
	/** 
	 * 最后一行 
	 * @return int
	 */ 
	public long getLastRow() {
		if(calType == 0) { //0-按页数 1-按开始结束数
			if(getCurPage() == 0) {
				return pageRows -1; 
			} 
			return getCurPage() * pageRows - 1;
		}else{
			return lastRow; 
		} 
	} 
	/** 
	 * 页面显示的第一页 
	 * @return int
	 */ 
	public long getDisplayPageFirst() {
		return displayPageFirst; 
	} 
	/** 
	 * 设置页面显示的第一页 
	 * @param displayPageFirst  displayPageFirst
	 */ 
	public PageNavi setDisplayPageFirst(long displayPageFirst) {
		this.displayPageFirst = displayPageFirst;
		return this;
	} 
	/** 
	 * 页面显示的最后一页 set
	 * @return int
	 */ 
	public long getDisplayPageLast() {
		return displayPageLast; 
	}

	/** 
	 * 设置页面显示的最后一页 
	 * @param displayPageLast  displayPageLast
	 */ 
	public PageNavi setDisplayPageLast(long displayPageLast) {
		this.displayPageLast = displayPageLast;
		return this;
	} 
 
	@SuppressWarnings({"unchecked","rawtypes" })
	public PageNavi addParam(String key, Object value) {
		if(null == key || null == value) {
			return this;
		} 
		if(null == this.params) {
			this.params = new LinkedHashMap<>();
		} 
		List<Object> values = params.get(key); 
		if(null == values) {
			values = new ArrayList<Object>(); 
		} 
		if(value instanceof Collection) {
			values.addAll((Collection)value); 
		}else{
			values.add(value); 
		} 
		params.put(key, values);
		return this;
	} 
	public Object getParams(String key) {
		Object result = null; 
		if(null != params) {
			result = params.get(key); 
		} 
		return result; 
	} 
	@SuppressWarnings({"rawtypes" })
	public Object getParam(String key) {
		Object result = null; 
		if(null != params) {
			Object values = getParams(key); 
			if(null != values && values instanceof List) {
				result = ((List)values).get(0); 
			}else{
				result = values; 
			} 
		} 
		return result; 
	} 
	//public String getOrderText(boolean require) {
		// return getOrderText(require, null);
	//	return null;
//	}
	/*public String getOrderText(boolean require, OrderStore store, String delimiter) {
		String result = ""; 
		if(null == orders) {
			orders = store; 
		}else{
			if(null != store) {
				for(Order order:store.gets().values()) {
					orders.add(order);
				} 
			} 
		} 
		if(null != orders) {
			result = orders.getRunText(delimiter);
		} 
		if(require && result.length() == 0) {
			result = "ORDER BY " +ConfigTable.DEFAULT_PRIMARY_KEY; 
		} 
		return result; 
	} */
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return PageNavi
	 */
	@Override
	public PageNavi order(Order order, boolean override) {
		if(null == orders) {
			orders = new DefaultOrderStore();
		} 
		orders.add(order, override);
		return this; 
	}
	@Override
	public OrderStore getOrders() {
		return orders;
	}
	@Override
	public PageNavi order(Order order) {
		return order(order, true);
	}

	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @param type  type
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return PageNavi
	 */
	@Override
	public PageNavi order(String order, Order.TYPE type, boolean override) {
		return order(new DefaultOrder(order, type), override);
	}
	@Override
	public PageNavi order(String order, Order.TYPE type) {
		return order(order, type, true);
	}
	@Override
	public PageNavi order(String order, String type, boolean override) {
		return order(new DefaultOrder(order, type), override);
	}
	@Override
	public PageNavi order(String order, String type) {
		return order(order, type, true);
	}
	@Override
	public PageNavi order(String order, boolean override) {
		return order(new DefaultOrder(order), override);
	}
	@Override
	public PageNavi order(String order) {
		return order(order, true);
	}

	/** 
	 * 设置总行数 
	 * @param totalRow  totalRow
	 */
	@Override
	public PageNavi setTotalRow(long totalRow) {
		this.totalRow = totalRow;
		calculate();
		return this;
	}

	/** 
	 * 设置最后一页 
	 * @param totalPage  totalPage
	 */ 
	@Override 
	public PageNavi setTotalPage(long totalPage) {
		this.totalPage = totalPage; 
		return this; 
	} 
	/** 
	 * 设置当前页 
	 * @param curPage  curPage
	 */ 
	@Override 
	public PageNavi setCurPage(long curPage) {
		this.curPage = curPage; 
		return this; 
	} 
	/** 
	 * 设置每页显示的行数 
	 * @param pageRows  pageRows
	 */ 
	@Override 
	public PageNavi setPageRows(int pageRows) {
		if(pageRows > 0) {
			this.pageRows = pageRows; 
		} 
		return this; 
	} 
	@Override 
	public long getTotalRow() {
		return totalRow; 
	} 
 
	@Override 
	public long getTotalPage() {
		return totalPage; 
	} 
 
	@Override 
	public long getCurPage() {
		if(null != maxPage && curPage > maxPage){
			return maxPage;
		}
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
	public PageNavi setFirstRow(long firstRow) {
		this.firstRow = firstRow; 
		return this; 
	} 
	@Override 
	public PageNavi setLastRow(long lastRow) {
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
	public PageNavi setLazyPeriod(long ms) {
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
	public PageNavi setMaxPage(Long page){
		this.maxPage = page;
		return this;
	}
	public PageNavi setMaxPage(Integer page){
		if(null != page){
			this.maxPage = page.longValue();
		}else{
			this.maxPage = null;
		}
		return this;
	}
	public Long getMaxPage(){
		return maxPage;
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
	 *  @param adapter html/ajax
	 *  @param method get/post
	 * @return String
	 */ 
	public String html(String adapter, String method) {
		long curPage = getCurPage();
		PageNaviConfig config = PageNaviConfig.getInstance(style);
		if(null == config) {
			config = new PageNaviConfig();
		}else{
			config.VAR_PAGE_MAX_PAGE = PageNaviConfig.DEFAULT_VAL_PAGE_MAX_PAGE;
		}
		boolean get = false;
		if("get".equalsIgnoreCase(method)) {
			get = true;
		}
		calculate(); 
		StringBuilder navi = new StringBuilder(); 
		// StringBuilder layout = new StringBuilder(); 
		StringBuilder index = new StringBuilder(); 
		StringBuilder stat = new StringBuilder(); 
		StringBuilder vol = new StringBuilder(); 
		StringBuilder jump = new StringBuilder(); 
		String configVarKey = ""; 
		if(null == flag) {
			flag = ""; 
		}

		if(get) {
			String params = null;
			if(config.VAR_CLIENT_SET_VOL_ENABLE) {
				params = config.KEY_PAGE_ROWS + "=" + pageRows;
			}
			if(null != params) {
				if (baseLink.contains("?")) {
					baseLink += "&" + params;
				} else {
					baseLink += "?" + params;
				}
			}
		}
		String totalRowFormat = NumberUtil.format(totalRow, "###,##0");
		String totalPageFormat = NumberUtil.format(totalPage, "###,##0");
		String curPageFormat = NumberUtil.format(curPage, "###,##0");

		if("ajax".equals(adapter)) {
			configVarKey = config.KEY_ID_FLAG + flag;	// _anyline_navi_conf_0 
		} 
		if("html".equals(adapter)) {
			navi.append("<link rel=\"stylesheet\" href=\"" + config.STYLE_FILE_PATH + "\" type=\"text/css\"/>\n"); 
			navi.append("<script type=\"text/javascript\" src=\"" + config.SCRIPT_FILE_PATH + "\"></script>\n"); 
		} 
		if("html".equals(adapter)) {
			navi.append("<form class=\"form\" action=\"" + baseLink + "\" method=\"post\">\n"); 
		} 
		if("ajax".equals(adapter)) {
			navi.append("<div class=\"form\">\n"); 
		}
		// 当前页
		navi.append("<input type='hidden' id='hid_cur_page_"+flag+"' name='"+config.KEY_PAGE_NO+"' class='anyline-navi-cur-page' value='"+curPage+"'/>\n");
		// 共多少页 
		navi.append("<input type='hidden' id='hid_total_page_"+flag+"' name='"+config.KEY_TOTAL_PAGE+"' class='anyline-navi-total-page' value='"+totalPage+"'/>\n");
		// 共多少条 
		navi.append("<input type='hidden' id='hid_total_row_"+flag+"' name='"+config.KEY_TOTAL_ROW+"' class='anyline-navi-total-row' value='"+totalRow+"'/>\n");
		// 每页显示多少条 
		if(config.VAR_CLIENT_SET_VOL_ENABLE) {
			navi.append("<input type='hidden' id='hid_page_rows_key_"+flag+"'  class='anyline-navi-page-rows-key' value='"+config.KEY_PAGE_ROWS+"'/>\n");
			navi.append("<input type='hidden' id='hid_page_rows_"+flag+"' name='"+config.KEY_PAGE_ROWS+"' class='anyline-navi-page-rows' value='"+pageRows+"'/>\n");
		} 
		if("ajax".equals(adapter)) {
			navi.append("<input type='hidden' class='"+config.KEY_ID_FLAG+"' value='" + flag + "'/>"); 
		} 
		navi.append(createHidParams(config)); 
		navi.append("<div class=\"anyline-navi\">\n");
		// 数据统计 
		String statFormat = config.STYLE_STAT_FORMAT;  
		statFormat = statFormat.replace("${totalRow}", totalRowFormat).replace("${curPage}", curPageFormat).replace("${totalPage}", totalPageFormat);
		statFormat = statFormat.replace("${total-row}", totalRowFormat).replace("${cur-page}", curPageFormat).replace("${total-page}", totalPageFormat);
		if(showStat) {
			stat.append(statFormat).append("\n"); 
		} 
		int range = config.VAR_PAGE_RANGE;
		long fr = NumberUtil.max(1, curPage - range/2);
		long to = fr + range - 1;
		boolean match = false; 
		if(totalPage > range && curPage>range/2) {
			match = true; 
		} 
		if(match) {
			to = curPage + range/2;
		} 
		if(totalPage - curPage < range/2) {
			fr = totalPage - range; 
		} 
		fr = NumberUtil.max(fr, 1);
		to = NumberUtil.min(to, totalPage);
		 
		if(type ==0) {// 下标导航
			// 每页多少条 
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
				log.info("[vol set][enable:{}][vol:{}][sort:{}]", config.VAR_CLIENT_SET_VOL_ENABLE, pageRows, config.CONFIG_PAGE_VAL_SET_SORT);
			}
			if(isShowVol()){
				if(config.VAR_CLIENT_SET_VOL_ENABLE) {
					if(config.CONFIG_PAGE_VAL_SET_SORT == 2) {
						vol.append(config.STYLE_PAGE_VOL.replace("{navi-conf}", configVarKey).replace("{navi-conf-key}", flag));
					}else{
						String[] nums = config.VAR_PAGE_VOL_NUMBERS.split(",");
						String clazz = config.VAR_PAGE_VOL_CLASS;
						if(BasicUtil.isEmpty(clazz)) {
							clazz = "navi-rows-set";
						}
						vol.append("<select class='").append(clazz).append("' id='navi_val_set_").append(flag).append("' onchange='_navi_change_vol(").append(configVarKey).append(")'>");
						for(String num:nums) {
							vol.append("<option value='").append(num).append("' id='navi_val_set_").append(flag).append("_item_").append(num).append("'");
							if(pageRows == BasicUtil.parseInt(num, 0)) {
								vol.append(" selected=\"selected\"");
							}
							vol.append(">").append(num).append(" 条/页</option>\n");
						}
						vol.append("</select>");
					}

				}
			}
			 
			// config.VAR_SHOW_INDEX_ELLIPSIS;是否显示下标省略号(不显示第2页或倒数第2页时显示省略号)
			// 1 .. 3 4 5 6 7 8 .. 10 
			if(config.VAR_SHOW_INDEX_ELLIPSIS) {
				if(config.VAR_SHOW_BUTTON) {
					createPageTag(index, method, "navi-button navi-prev-button", config.STYLE_BUTTON_PREV, NumberUtil.max(curPage-1, 1), configVarKey);
				} 
				// 下标 
				if(config.VAR_SHOW_INDEX) {
					if(fr<2) {
						fr = 2; 
					} 
					if(to>totalPage-1) {
						to = totalPage-1; 
					} 
					index.append("<div class='navi-num-border'>\n"); 
					createPageTag(index, method, "navi-num-item","1", 1, configVarKey);
					if(fr > 2) {
						createPageTag(index, method, "navi-num-item", config.STYLE_INDEX_ELLIPSIS, 0, configVarKey);
					} 
					for(long i=fr; i<=to; i++) {
						createPageTag(index, method, "navi-num-item", i + "", i, configVarKey);
					} 
					if(to < totalPage-1) {
						createPageTag(index, method, "navi-num-item", config.STYLE_INDEX_ELLIPSIS, 0, configVarKey);
					} 
					if(totalPage >1) {//不是只有一页
						createPageTag(index, method, "navi-num-item", totalPage+"", totalPage, configVarKey);
					} 
					index.append("</div>\n"); 
				} 
				// 下一页 最后页 
				if(config.VAR_SHOW_BUTTON) {
					createPageTag(index, method, "navi-button navi-next-button", config.STYLE_BUTTON_NEXT, (int)NumberUtil.min(curPage+1, totalPage), configVarKey);
				} 
			}else{
				// 上一页  第一页 
				if(config.VAR_SHOW_BUTTON) {
					createPageTag(index, method, "navi-button navi-first-button", config.STYLE_BUTTON_FIRST, 1, configVarKey);
					createPageTag(index, method, "navi-button navi-prev-button", config.STYLE_BUTTON_PREV, NumberUtil.max(curPage-1, 1), configVarKey);
				} 
				// 下标 
				if(config.VAR_SHOW_INDEX) {
					index.append("<div class='navi-num-border'>\n"); 
					for(long i=fr; i<=to; i++) {
						createPageTag(index, method, "navi-num-item", i + "", i, configVarKey);
					} 
					index.append("</div>\n"); 
				} 
				// 下一页 最后页 
				if(config.VAR_SHOW_BUTTON) {
					createPageTag(index, method, "navi-button navi-next-button", config.STYLE_BUTTON_NEXT, (int)NumberUtil.min(curPage+1, totalPage), configVarKey);
					createPageTag(index, method, "navi-button navi-last-button", config.STYLE_BUTTON_LAST, totalPage, configVarKey);
				} 
			} 
			// VOL位置:下标之后 
//			if("page".equalsIgnoreCase(config.VAR_PAGE_VOL_INDEX)) {
//				builder.append(vol); 
//			} 
			// 跳转到 
			if(showJump) {
				jump.append(config.STYLE_LABEL_JUMP) 
				.append("<input type='text' value='") 
				.append(curPage) 
				.append("' class='navi-go-txt anyline-jump-txt' id='hid_jump_txt_"+flag+"' onkeydown='_navi_jump_enter("+configVarKey+")'/>")
				.append(config.STYLE_LABEL_JUMP_PAGE) 
				.append("<span class='navi-go-button' onclick='_navi_jump("+configVarKey+")'>") 
				.append(config.STYLE_BUTTON_JUMP).append("</span>\n"); 
			} 
			// VOL位置:最后 
//			if("last".equalsIgnoreCase(config.VAR_PAGE_VOL_INDEX)) {
//				builder.append(vol); 
//			} 
		}else if(type == 1) {
			// 加载更多
			if(curPage+1 <= totalPage) {
				createPageTag(index, method, "navi-more-button", loadMoreFormat, (int)NumberUtil.min(curPage+1, totalPage+1), configVarKey);
			}else{
				index.append(config.STYLE_PAGE_OVER);
			}
		} 
		 
		 
		String layout_html = config.VAR_COMPONENT_LAYOUT; 
		if(null == layout_html) {
			layout_html = "${navi-stat}${navi-index}${navi-vol}${navi-jump}";
		} 
		layout_html = layout_html.replace("${stat}", stat.toString());
		layout_html = layout_html.replace("${index}", index.toString());
		layout_html = layout_html.replace("${vol}", vol.toString());
		layout_html = layout_html.replace("${jump}", jump.toString());
 
		layout_html = layout_html.replace("${navi-stat}", stat.toString());
		layout_html = layout_html.replace("${navi-index}", index.toString());
		layout_html = layout_html.replace("${navi-vol}", vol.toString());
		layout_html = layout_html.replace("${navi-jump}", jump.toString());
		navi.append(layout_html); 
		navi.append("</div>"); 
 
		if("html".equals(adapter)) {
			navi.append("</form>\n"); 
		} 
		if("ajax".equals(adapter)) {
			navi.append("</div>\n"); 
		} 
		return navi.toString(); 
	} 
	/** 
	 *  
	 * @param builder  builder
	 * @param method   get/post
	 * @param clazz   clazz
	 * @param tag 显示内容 
	 * @param page 跳到第几页 
	 * @param configFlag  configFlag
	 */ 
	private void createPageTag(StringBuilder builder, String method, String clazz, String tag, long page, String configFlag) {
		long curPage = getCurPage();
		boolean get = false;
		if("get".equalsIgnoreCase(method)) {
			get = true;
		}
		builder.append("<span class ='").append(clazz); 
		if(page == curPage && 0 == type) {
			if(clazz.contains("navi-num-item")) {//下标
				builder.append(" navi-num-item-cur"); 
			}else{//btn 
				builder.append(" navi-disabled"); 
			} 
			builder.append("'"); 
		}else{
			builder.append("'"); 
			if(page>0) {
				if(!get) {//post
					builder.append(" onclick='_navi_go(").append(page);
					if (BasicUtil.isNotEmpty(configFlag)) {
						builder.append(", ").append(configFlag);
					}
					builder.append(")'");
				}
			} 
		} 
		builder.append(">");
		if(get) {//get
			PageNaviConfig config = PageNaviConfig.getInstance(style);
			if(null == config) {
				config = new PageNaviConfig();
			}else{
				config.VAR_PAGE_MAX_PAGE = PageNaviConfig.DEFAULT_VAL_PAGE_MAX_PAGE;
			}
			builder.append("<a href='").append(baseLink);
			if(baseLink.contains("?")) {
				builder.append("&");
			}else{
				builder.append("?");
			}
			builder.append(config.KEY_PAGE_NO).append("=").append(page).append("'>").append(tag).append("</a>");
		}else{//post
			builder.append(tag);
		}
		builder.append("</span>\n");
	} 
	// 创建隐藏参数 
	private String createHidParams(PageNaviConfig config) {
		String html = ""; 
		try{
			if(null != params) {
				for(Iterator<String> itrKey=params.keySet().iterator(); itrKey.hasNext();) {
					String key = itrKey.next(); 
					Object values = params.get(key); 
					html += createHidParam(key, values);
				} 
			} 
			html += createHidParam(config.KEY_SHOW_STAT, showStat);
			html += createHidParam(config.KEY_SHOW_JUMP, showJump);
		}catch(Exception e) {
			e.printStackTrace(); 
		} 
		return html; 
	} 
	 
	public String createHidParam(String name, Object values) {
		String html = ""; 
		if(null == values) {
			html = "<input type='hidden' name='"+name+"' value=''>\n"; 
		}else{
			if(values instanceof Collection<?>) {
				Collection<?> list = (Collection<?>)values; 
				for(Object obj:list) {
					html += "<input type='hidden' name='"+name+"' value='"+obj+"'>\n"; 
				} 
			}else{
				html += "<input type='hidden' name='"+name+"' value='"+values+"'>\n"; 
			} 
		} 
		return html; 
	}
	@Transient
	public String getHtml() {
		return html("html","get");
	}
	@Transient
	public String html() {
		return html("html","get");
	}
	@Transient
	public String html(String adapter) {
		return html(adapter, "get");
	}
	@Transient
	public String getForm() {
		return html("html","post");
	}
	@Transient
	public String form() {
		return html("html","post");
	}
	@Transient
	public String ajax() {
		return html("ajax","post");
	}

	public PageNavi clone() {
		PageNavi clone = null;
		try{
			clone = (PageNavi)super.clone();
		}catch (Exception e) {
			clone = new DefaultPageNavi();
		}
		return clone;
	}

	public DataRow map(boolean empty) {
		DataRow row = new OriginRow();
		row.put("page", getCurPage());
		row.put("vol", pageRows);
		row.put("total", totalRow);
		row.put("auto_count", autoCount);
		return row;
	}
}
