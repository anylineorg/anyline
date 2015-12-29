
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
import org.apache.log4j.Logger;


public class PageNaviImpl implements PageNavi{
	private Logger log = Logger.getLogger(this.getClass());
//
//	public static final String PAGE_VOL				= "pageRows"								;
//	public static final String PAGE_NO				= "pageNo"								;
	public static final String PAGE_ROWS			= "_anyline_page_rows"								;
	public static final String PAGE_NO				= "_anyline_page"								;
	
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
//	
	private Map<String,List<Object>> params;	//查询参数
	
	private static final String BR = "\n";
	//private static final String TAB = "\t";
	private static final String BR_TAB = "\n\t";
	
	private StringBuilder builderNavi = new StringBuilder();		//分页HTML

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
	private void createHidParams(){
		try{
			if(null != params)
			for(Iterator<String> itrKey=params.keySet().iterator(); itrKey.hasNext();){
				String key = itrKey.next();
				Object values = params.get(key);
				createHidParam(key,values);
			}
			//createHidParam(Constant.REQUEST_PARAM_PAGE_NO, curPage+"");
			//createHidParam(Constant.REQUEST_PARAM_PAGE_VOL, pageRows+"");
		}catch(Exception e){
			log.error(e);
		}
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
	public void createHidParam(String name, Object values) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getOrderText(boolean require, OrderStore store) {
		// TODO Auto-generated method stub
		return null;
	}

}