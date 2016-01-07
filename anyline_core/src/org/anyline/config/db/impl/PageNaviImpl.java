/* 
 * Copyright 2006-2015 the original author or authors.
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

package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;


public class PageNaviImpl implements PageNavi{
	private Logger log = Logger.getLogger(this.getClass());
//
//	public static final String PAGE_VOL				= "pageRows"							;
//	public static final String PAGE_NO				= "pageNo"								;
	public static final String PAGE_ROWS			= "_anyline_page_rows"					;
	public static final String PAGE_NO				= "_anyline_page"						;
	
	private int totalRow;					//记录总数
	private int totalPage; 					//最大页数
	private int curPage=1;					//当前页数
	
	private int pageRange=10;				//显示多少个分页下标
	private int pageRows=10;				//每页多少条
	private int displayPageFirst = 0;		//显示的第一页标签
	private int displayPageLast = 0;		//显示的最后页标签
	private String baseLink;				//基础URL
	private OrderStore orders;				//排序依据(根据 orderCol 排序分页)
	private int calType = 0;				//分页计算方式(0-按页数 1-按开始结束数)
	private int firstRow = 0;				//第一行
	private int lastRow = 0;				//最后一行
	
//	private String statFormat = "共<i class='blue'> {totalRow} </i>条记录，当前显示第&nbsp;<i class='blue'>{curPage}&nbsp;</i>页";
//	private String tagFirst = "<span class=\"first\">&nbsp;第一页&nbsp;</span>";
//	private String tagPrev = "<span class=\"navi-sign\">&lt;&lt;</span>上一页";
//	private String tagNext = "<span class=\"next\">下一页<span class=\"navi-sign\">&gt;&gt;</span></span>";
//	private String tagLast = "<span class=\"last\">&nbsp;最后页&nbsp;</span>";
//	private String scriptFile = "/common/web/common/script/navi.js";
//	private String styleFile = "/common/web/common/style/navi.css";
	
	private Map<String,List<Object>> params;	//查询参数
	
	private static final String BR = "\n";
	//private static final String TAB = "\t";
	private static final String BR_TAB = "\n\t";
	
	private StringBuilder builder = new StringBuilder();		//分页HTML
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
		setTotalPage((totalRow - 1) / pageRows + 1);					//总页数
		setDisplayPageFirst(curPage - pageRange/2);				//显示的第一页
		if(displayPageFirst > totalPage - pageRange){
			setDisplayPageFirst(totalPage - pageRange + 1);
		}
		if(displayPageFirst < 1){ 
			setDisplayPageFirst(1);
		}
		
		setDisplayPageLast(displayPageFirst + pageRange - 1);		//显示的最后页
		if (displayPageLast > totalPage)
			setDisplayPageLast(totalPage);
	}
	//创建隐藏参数
	private String createHidParams(){
		String html = "";
		try{
			if(null != params)
			for(Iterator<String> itrKey=params.keySet().iterator(); itrKey.hasNext();){
				String key = itrKey.next();
				Object values = params.get(key);
				html += createHidParam(key,values);
			}
		}catch(Exception e){
			log.error(e);
		}
		return html;
	}
	
	/**
	 * 第一行
	 * @return
	 */
	public int getFirstRow(){
		if(calType == 0){
			if(curPage <= 0) return 0;
			return (curPage-1) * pageRows;
		}else{
			return firstRow;
		}
	}
	/**
	 * 最后一行
	 * @return
	 */
	public int getLastRow(){
		if(calType == 0){
			if(curPage == 0) return pageRows -1;
			return curPage * pageRows - 1;
		}else{
			return lastRow;
		}
	}
	/**
	 * 页面显示的第一页
	 * @return
	 */
	public int getDisplayPageFirst() {
		return displayPageFirst;
	}
	/**
	 * 设置页面显示的第一页
	 * @param displayPageFirst
	 */
	public void setDisplayPageFirst(int displayPageFirst) {
		this.displayPageFirst = displayPageFirst;
	}
	/**
	 * 页面显示的最后一页
	 * @return
	 */
	public int getDisplayPageLast() {
		return displayPageLast;
	}
	/**
	 * 设置页面显示的最后一页
	 * @param displayPageLast
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
		return getOrderText(require, null);
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
			result = "ORDER BY CD";
		}
		return result;
	}
	/**
	 * 设置排序方式
	 * @param order
	 * @return
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
	 * @param order
	 * @param type
	 * @return
	 */
	public PageNavi order(String order, String type){
		return order(new OrderImpl(order, type));
	}
	public PageNavi order(String order){
		return order(new OrderImpl(order));
	}
	
	/**
	 * 设置总行数
	 * @param totalRow
	 */
	public void setTotalRow(int totalRow) {
		this.totalRow = totalRow;
	}
	/**
	 * 设置最后一页
	 * @param totalPage
	 */
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	/**
	 * 设置当前页
	 * @param curPage
	 */
	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}
	/**
	 * 设置每页显示的行数
	 * @param pageRows
	 */
	public void setPageRows(int pageRows) {
		if(pageRows > 0){
			this.pageRows = pageRows;
		}
	}
	public int getTotalRow() {
		return totalRow;
	}
	
	public int getTotalPage() {
		return totalPage;
	}

	public int getCurPage() {
		return curPage;
	}
	
	public int getPageRows() {
		return pageRows;
	}
	
	public String getBaseLink() {
		return baseLink;
	}
	public void setBaseLink(String baseLink) {
		this.baseLink = baseLink;
	}
//	public String getTagFirst() {
//		return tagFirst;
//	}
//	public void setTagFirst(String tagFirst) {
//		this.tagFirst = tagFirst;
//	}
//	public String gettagPrev() {
//		return tagPrev;
//	}
//	public void settagPrev(String tagPrev) {
//		this.tagPrev = tagPrev;
//	}
//	public String gettagNext() {
//		return tagNext;
//	}
//	public void settagNext(String tagNext) {
//		this.tagNext = tagNext;
//	}
//	public String gettagLast() {
//		return tagLast;
//	}
//	public void settagLast(String tagLast) {
//		this.tagLast = tagLast;
//	}
//	
//	public String getScriptFile() {
//		return scriptFile;
//	}
//	public void setScriptFile(String scriptFile) {
//		this.scriptFile = scriptFile;
//	}
//	public String getStyleFile() {
//		return styleFile;
//	}
//	public void setStyleFile(String styleFile) {
//		this.styleFile = styleFile;
//	}
	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}
	public void setLastRow(int lastRow) {
		this.lastRow = lastRow;
	}
	@Override
	public String createHidParam(String name, Object values) {
		String html = "";
		if(null == values){
			html = "<inpu type='hidden' name='"+name+"' value=''>\n";
		}else{
			if(values instanceof Collection<?>){
				Collection<?> list = (Collection<?>)values;
				for(Object obj:list){
					html += "<inpu type='hidden' name='"+name+"' value='"+obj+"'>\n";
				}
			}else{
				html += "<inpu type='hidden' name='"+name+"' value='"+values+"'>\n";
			}
		}
		return html;
	}
	@Override
	public String getOrderText(boolean require, OrderStore store) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*  builder.append("<link rel=\"stylesheet\" href=\""+styleFile+"\" type=\"text/css\"/>");
		builder.append(BR);
		builder.append("<script type=\"text/javascript\" src=\""+scriptFile+"\"></script>");
		builder.append(BR);
		builder.append("<form id=\"__frmNavi\" action=\"" + baseLink + "\" method=\"post\">");
		builder.append(BR_TAB);
		builder.append("<input type=\"hidden\" name=\"pageNo\" id=\"__hidPageNo\"/>");
		builder.append(BR_TAB);
		builder.append("<div style='clear:both;'>\n</div><div class='navi'>");
		builder.append(BR_TAB);
		//合计信息
		builder.append("<span class=\"stat\">");
		builder.append(statFormat);
		builder.append("</span>");
		builder.append(BR_TAB);
		builder.append("<span class=\"idx\">");
		createFirstPage(); 		//第一页
		createPrevPage();		//上一页
		for (int j = displayPageFirst; j>0 && j <=displayPageLast; j++) {
			createLink(j,true);
		}
		createNextPage();		//下一页
		createLastPage();		//最后页
		builder.append(BR_TAB);
		builder.append("</span>");
		builder.append(BR_TAB);
		builder.append("</div>");
		createHidParams();
		builder.append(BR);
		builder.append("</form>");*/
	public String html(){
		calculate();
		StringBuilder builder = new StringBuilder();
		builder.append("<link rel=\"stylesheet\" href=\""+ConfigTable.getString("NAVI_STYLE_FILE_PATH")+"\" type=\"text/css\"/>\n");
		builder.append("<script type=\"text/javascript\" src=\""+ConfigTable.getString("NAVI_SCRIPT_FILE_PATH")+"\"></script>\n");
		builder.append("<form id=\"_navi_frm\" action=\"" + baseLink + "\" method=\"post\">\n");
		builder.append("<input type=\"hidden\" name=\""+PageNavi.PAGE_NO+"\" id=\"__hidPageNo\" value='"+curPage+"'/>\n");
		builder.append(createHidParams());
		builder.append("</form>\n");
		builder.append("<div class='anyline_navi'>\n");
		builder.append("<span class='navi-summary'>\n");
		builder.append("共<span class='navi-total-row'>").append(totalRow).append("</span>条\n");
		builder.append("第<span class='navi-cur-page'>").append(curPage)
		.append("</span>/<span class='navi-total-page'>").append(totalPage).append("</span>页\n");
		builder.append("<input type='button' class='navi-first-button' value='第一页' onclick='_navi_go(1)'/>\n");
		builder.append("<input type='button' class='navi-prev-button' value='上一页' onclick='_navi_go("+(int)BasicUtil.getMax(curPage-1,1)+")'/>\n");
		builder.append("<span class='navi-num-border'>\n");
		for(int i=1; i<=totalPage; i++){
			String cur = "";
			if(i ==curPage){
				cur = " navi-num-cur";
			}
			builder.append("<span class='navi-num-item"+cur+"' onclick='_navi_go("+i+")'>").append(i).append("</span>\n");
		}
		builder.append("</span>");
		builder.append("<input type='button' class='navi-next-button' value='下一页' onclick='_navi_go("+(int)BasicUtil.getMin(curPage+1, totalPage)+")'/>\n");
		builder.append("<input type='button' class='navi-last-button' value='最后页' onclick='_navi_go("+totalPage+")'/>\n");
		builder.append("</span>\n");
		builder.append("转到<input type='text' id='_anyline_go' value='"+curPage+"' class='navi-go-txt'/>页 &nbsp;<input type='button' value='确定' class='navi-go-button' onclick='_navi_go()'/>\n");
		builder.append("</div>");
		return builder.toString();
	}
	public String toString(){
		return html();
	}

}