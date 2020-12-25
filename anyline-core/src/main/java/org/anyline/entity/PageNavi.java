/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.entity; 

import java.io.Serializable;
 
 
 
 
public interface PageNavi extends Serializable{ 
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
	 * @return return
	 */ 
	public int getFirstRow(); 
	/** 
	 * 最后一行 
	 * @return return
	 */ 
	public int getLastRow(); 
	/** 
	 * 页面显示的第一页 
	 * @return return
	 */ 
	public int getDisplayPageFirst() ; 
	/** 
	 * 设置页面显示的第一页 
	 * @param displayPageFirst  displayPageFirst
	 */ 
	public void setDisplayPageFirst(int displayPageFirst); 
	/** 
	 * 页面显示的最后一页 
	 * @return return
	 */ 
	public int getDisplayPageLast() ; 
	/** 
	 * 设置页面显示的最后一页 
	 * @param displayPageLast  displayPageLast
	 */ 
	public void setDisplayPageLast(int displayPageLast) ; 
 
	public void addParam(String key, Object value); 
	public Object getParams(String key); 
	public Object getParam(String key); 
	public String getOrderText(boolean require); 
	//public String getOrderText(boolean require, OrderStore store); 
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @return return
	 */ 
	//public PageNavi order(Order order); 
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @param type  type
	 * @return return
	 */ 
	public PageNavi order(String order, String type); 
	public PageNavi order(String order); 
	 
	/** 
	 * 设置总行数 
	 * @param totalRow  totalRow
	 * @return return
	 */ 
	public PageNavi setTotalRow(int totalRow) ; 
	/** 
	 * 设置最后一页 
	 * @param totalPage  totalPage
	 * @return return
	 */ 
	public PageNavi setTotalPage(int totalPage) ; 
	/** 
	 * 设置当前页 
	 * @param curPage  curPage
	 * @return return
	 */ 
	public PageNavi setCurPage(int curPage) ; 
	/** 
	 * 设置每页显示的行数 
	 * @param pageRows  pageRows
	 * @return return
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
	 * @return return
	 */
	public boolean isLazy();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @return return
	 */
	public long getLazyPeriod();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param ms ms
	 * @return return
	 */
	public PageNavi setLazyPeriod(long ms);
	public PageNavi setLazyKey(String key);
	public String getLazyKey();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param ms ms
	 * @return return
	 */
	public PageNavi setLazy(long ms);
	public String html(String creater);
	public PageNavi setFlag(String flag);
	public String getFlag();
	public PageNavi setType(int type);
	public int getType();
	/**
	 * 是否显示跳到指定页
	 * @param showJump 是否
	 * @return PageNavi
	 */
	public PageNavi setShowJump(boolean showJump);
	public boolean isShowJump();
	/**
	 * 是否显示每页多少条设置
	 * @param showVol 是否
	 * @return PageNavi
	 */
	public PageNavi setShowVol(boolean showVol);
	public boolean isShowVol();
	
	public PageNavi setShowStat(boolean showStat);
	public boolean isShowStat();
	/**
	 * 样式分组
	 * @param style style
	 * @return return
	 */
	public PageNavi setStyle(String style);
	public String getStyle();
	/**
	 * 加载更多样式
	 * @param guide guide
	 * @return return
	 */
	public PageNavi setGuide(String guide);
	public String getGuide();
	public PageNavi setMethod(String method);
	public String getMethod();
	public String getHtml();
}
