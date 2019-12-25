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
	private static final long serialVersionUID = 3593100423479113410L; 
	private static final Logger log = LoggerFactory.getLogger(PageNaviImpl.class); 
// 
//	public static final String PAGE_VOL				= "pageRows"							; 
//	public static final String PAGE_NO				= "pageNo"								; 
 
	private static final String BR 					= "\n"; 
	private static final String TAB 				= "\t"; 
	private static final String BR_TAB 				= "\n\t"; 
	 
	private int totalRow					= 0			;	//记录总数 
	private int totalPage					= 0 		;	//最大页数 
	private int curPage						= 1 		;	//当前页数 
	 
	private int pageRange					= 10		;	//显示多少个分页下标 
	private int pageRows					= 10		;	//每页多少条 
	private int displayPageFirst 			= 0			;	//显示的第一页标签 
	private int displayPageLast 			= 0			;	//显示的最后页标签 
	private String baseLink					= null		;	//基础URL 
	private OrderStore orders				= null 		;	//排序依据(根据 orderCol 排序分页) 
	private int calType 					= 0			;	//分页计算方式(0-按页数 1-按开始结束数) 
	private int firstRow 					= 0			;	//第一行 
	private int lastRow 					= -1		;	//最后一行 
	private boolean lazy 					= false		; 
	private String flag  					= ""		;	//一个jsp中有多个分页时用来区分 
	private long lazyPeriod 				= 0			;	//总条数懒加载时间间隔(秒) 
	private String lazyKey 					= null		;	//懒加载 
	private int type 						= 0			;	//分页方式(0:下标 1:流式 2:根据浏览器状态 web:0,wap:1) 
	private Map<String,List<Object>> params	= null		;	//查询参数 
	private String method					= "post"	; 
	private String style		= ""	; //样式标记对应anyline-navi.xml中的config.key 
	 
 
	private boolean showStat = false; 
	private boolean showJump = false; 
	private boolean volEnable = false; 
	private String loadMoreFormat = ""; 
	 
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
 
	public String ajaxPage(){ 
		return html("ajax"); 
	} 
	public String jspPage(){ 
		return html("html"); 
	} 
	/** 
	 *  @param creater creater
	 * @return return
	 */ 
	public String html(String creater){ 
		return ""; 
	} 
	 
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
	@Override 
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
 
}
