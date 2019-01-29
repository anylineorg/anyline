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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.entity;

import java.io.Serializable;




public interface PageNavi extends Serializable{
//	public static final String PAGE_ROWS			= "_anyline_page_rows"	;
//	public static final String PAGE_NO				= "_anyline_page"		;
//	public static final String TOTAL_PAGE			= "_anyline_total_page"		;
//	public static final String TOTAL_ROW			= "_anyline_total_row"		;
//	public static final String SHOW_STAT			= "_anyline_navi_show_stat"	;
//	public static final String SHOW_JUMP			= "_anyline_navi_show_jump"	;
//	public static final String GUIDE				= "_anyline_navi_guide"		;
	
	/**
	 * 分页计算方式
	 * @param type	0-按页数 1-按开始结束记录数
	 */
	public void setCalType(int type);
	public int getCalType();
	/**
	 * 计算分页变量
	 */
	public void calculate() ;
	public String createHidParam(String name, Object values);

	/**
	 * 第一行
	 * @return
	 */
	public int getFirstRow();
	/**
	 * 最后一行
	 * @return
	 */
	public int getLastRow();
	/**
	 * 页面显示的第一页
	 * @return
	 */
	public int getDisplayPageFirst() ;
	/**
	 * 设置页面显示的第一页
	 * @param displayPageFirst
	 */
	public void setDisplayPageFirst(int displayPageFirst);
	/**
	 * 页面显示的最后一页
	 * @return
	 */
	public int getDisplayPageLast() ;
	/**
	 * 设置页面显示的最后一页
	 * @param displayPageLast
	 */
	public void setDisplayPageLast(int displayPageLast) ;

	public void addParam(String key, Object value);
	public Object getParams(String key);
	public Object getParam(String key);
	public String getOrderText(boolean require);
	//public String getOrderText(boolean require, OrderStore store);
	/**
	 * 设置排序方式
	 * @param order
	 * @return
	 */
	//public PageNavi order(Order order);
	/**
	 * 设置排序方式
	 * @param order
	 * @param type
	 * @return
	 */
	public PageNavi order(String order, String type);
	public PageNavi order(String order);
	
	/**
	 * 设置总行数
	 * @param totalRow
	 */
	public PageNavi setTotalRow(int totalRow) ;
	/**
	 * 设置最后一页
	 * @param totalPage
	 */
	public PageNavi setTotalPage(int totalPage) ;
	/**
	 * 设置当前页
	 * @param curPage
	 */
	public PageNavi setCurPage(int curPage) ;
	/**
	 * 设置每页显示的行数
	 * @param pageRows
	 */
	public PageNavi setPageRows(int pageRows) ;
	public int getTotalRow() ;
	
	public int getTotalPage() ;

	public int getCurPage() ;
	
	public int getPageRows() ;
	
	public String getBaseLink() ;
	public PageNavi setBaseLink(String baseLink) ;
	public PageNavi setFirstRow(int firstRow) ;
	public PageNavi setLastRow(int lastRow) ;
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @return
	 */
	public boolean isLazy();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @return
	 */
	public long getLazyPeriod();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param period
	 * @return
	 */
	public PageNavi setLazyPeriod(long ms);
	public PageNavi setLazyKey(String key);
	public String getLazyKey();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param period
	 * @return
	 */
	public PageNavi setLazy(long ms);
	public String ajaxPage();
	public String jspPage();
	public PageNavi setFlag(String flag);
	public String getFlag();
	public PageNavi setType(int type);
	public int getType();

	public PageNavi setShowJump(boolean showJump);
	public boolean isShowJump();
	
	public PageNavi setShowStat(boolean showStat);
	public boolean isShowStat();
	/**
	 * 样式分组
	 * @param style
	 * @return
	 */
	public PageNavi setStyle(String style);
	public String getStyle();
	/**
	 * 加载更多样式
	 * @param guide
	 * @return
	 */
	public PageNavi setGuide(String guide);
	public String getGuide();
	public PageNavi setMethod(String method);
	public String getMethod();
}