/*
 * Copyright 2006-2023 www.anyline.org
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

import java.io.Serializable;

public interface PageNavi extends Serializable{
	/**
	 * 查询结果行数
	 * @param size 查询结果行数
	 * @return this
	 */
	PageNavi setDataSize(int size);

	/**
	 * 查询结果行数
	 * @return 查询结果行数
	 */
	int getDataSize();
	/** 
	 * 分页计算方式 
	 * @param type	0-按页数 1-按开始结束记录数 
	 */ 
	PageNavi setCalType(int type); 
	int getCalType(); 
	/** 
	 * 计算分页变量 
	 */ 
	PageNavi calculate() ; 
	String createHidParam(String name, Object values);
 
	/** 
	 * 第一行 
	 * @return int
	 */ 
	long getFirstRow();
	/** 
	 * 最后一行 
	 * @return int
	 */ 
	long getLastRow();
	/** 
	 * 页面显示的第一页 
	 * @return int
	 */ 
	long getDisplayPageFirst() ;
	/** 
	 * 设置页面显示的第一页 
	 * @param displayPageFirst  displayPageFirst
	 */ 
	PageNavi setDisplayPageFirst(long displayPageFirst);
	/** 
	 * 页面显示的最后一页 
	 * @return int
	 */ 
	long getDisplayPageLast() ;
	/** 
	 * 设置页面显示的最后一页 
	 * @param displayPageLast  displayPageLast
	 */ 
	PageNavi setDisplayPageLast(long displayPageLast) ;
 
	PageNavi addParam(String key, Object value);
	Object getParams(String key); 
	Object getParam(String key); 
	String getOrderText(boolean require); 
	// String getOrderText(boolean require, OrderStore store);
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @return PageNavi
	 */ 
	// PageNavi order(Order order); 
	/** 
	 * 设置排序方式 
	 * @param order  order
	 * @param type  type
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return PageNavi
	 */
	PageNavi order(String order, String type, boolean override);
	PageNavi order(String order, String type);

	PageNavi order(String order, Order.TYPE type, boolean override);
	PageNavi order(String order, Order.TYPE type);

	PageNavi order(String order, boolean override);
	PageNavi order(String order);

	PageNavi order(Order order, boolean override);
	PageNavi order(Order order);
	OrderStore getOrders();

	/** 
	 * 设置总行数 
	 * @param totalRow  totalRow
	 * @return PageNavi
	 */
	PageNavi setTotalRow(long totalRow) ;
	/** 
	 * 设置最后一页 
	 * @param totalPage  totalPage
	 * @return PageNavi
	 */ 
	PageNavi setTotalPage(long totalPage) ;
	/** 
	 * 设置当前页 
	 * @param curPage  curPage
	 * @return PageNavi
	 */ 
	PageNavi setCurPage(long curPage) ;
	/** 
	 * 设置每页显示的行数 
	 * @param pageRows  pageRows
	 * @return PageNavi
	 */ 
	PageNavi setPageRows(int pageRows) ; 
	long getTotalRow() ;
	 
	long getTotalPage() ;
 
	long getCurPage() ;
	 
	int getPageRows() ; 
	 
	String getBaseLink() ; 
	PageNavi setBaseLink(String baseLink) ; 
	PageNavi setFirstRow(long firstRow) ;
	PageNavi setLastRow(long lastRow) ;
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @return boolean
	 */
	boolean isLazy();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @return long
	 */
	long getLazyPeriod();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param ms ms
	 * @return PageNavi
	 */
	PageNavi setLazyPeriod(long ms);
	PageNavi setLazyKey(String key);
	String getLazyKey();
	/**
	 * 总条数懒加载时间间隔(秒)
	 * @param ms ms
	 * @return PageNavi
	 */
	PageNavi setLazy(long ms);
	String html(String adapter);
	String form();
	String ajax();
	PageNavi setFlag(String flag);
	String getFlag();
	PageNavi setType(int type);
	int getType();
	/**
	 * 是否显示跳到指定页
	 * @param showJump 是否
	 * @return PageNavi
	 */
	PageNavi setShowJump(boolean showJump);
	boolean isShowJump();
	/**
	 * 是否显示每页多少条设置
	 * @param showVol 是否
	 * @return PageNavi
	 */
	PageNavi setShowVol(boolean showVol);
	boolean isShowVol();
	
	PageNavi setShowStat(boolean showStat);
	boolean isShowStat();
	/**
	 * 样式分组
	 * @param style style
	 * @return PageNavi
	 */
	PageNavi setStyle(String style);
	String getStyle();
	/**
	 * 加载更多样式
	 * @param guide guide
	 * @return PageNavi
	 */
	PageNavi setGuide(String guide);
	String getGuide();
	PageNavi setMethod(String method);
	String getMethod();
	String getHtml();
	String getForm();

	PageNavi scope(long first, long last);
	PageNavi limit(long offset, int rows);

	/**
	 * 设置是否需要是查询总行数<br/>
	 * maps国为性能考虑默认不查总行数，通过这个配置强制开启总行数查询，执行完成后会在page navi中存放总行数结果
	 * @param required 是否
	 * @return this
	 */
	PageNavi total(boolean required);
	Boolean requiredTotal();
}
