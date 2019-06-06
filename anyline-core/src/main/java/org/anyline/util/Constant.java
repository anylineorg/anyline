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


package org.anyline.util;

public class Constant {
	public static final String SERVLET_ATTR_TOKEN_CONFIG			= "SERVLET_ATTR_TOKEN_CONFIG"			;//TOKEN配置
	public static final String SERVLET_ATTR_MESSAGE					= "SERVLET_ATTR_MESSAGE"				;//开发级消息
	public static final String SERVLET_ATTR_REQUIRED_TOKEN_URI		= "SERVLET_ATTR_REQUIRED_TOKEN_URI";	//必须验证token的URI
	public static final String SERVLET_ATTR_REQUIRED_TOKEN_REFER	= "SERVLET_ATTR_REQUIRED_TOKEN_REFER";	//必须验证token的REFFER
	//session变量

	public static final String SESSION_ATTR_MESSAGE					= "SESSION_ATTR_MESSAGE"				;//session信息
	public static final String SESSION_ATTR_TOKEN_VALUE_EMPTY		= "SESSION_ATTR_TOKEN_VALUE_EMPTY"		;//TOKEN空值(已被验证过一次)
	public static final String SESSION_ATTR_ERROR_MESSAGE			= "SESSION_ATTR_ERROR_MESSAGE";
	//request变量及参数
	public static final String REQUEST_ATTR_HTTP_CLIENT_CD 			= "REQUEST_HTTP_CLIENT_CD"				;//操作客户端CD
	public static final String REQUEST_ATTR_HTTP_CLIENT				= "REQUEST_HTTP_CLIENT"					;//操作客户端cn.ecool.entity.HttpClient
	public static final String REQUEST_ATTR_ERROR_MESSAGE_KEY		= "REQUEST_ERROR_MESSAGE_KEY"			;//错误信息KEY
	public static final String REQUEST_ATTR_REFER_KEY				= "REQUEST_REFER_KEY"					;//来源路径KEY
	public static final String REQUEST_ATTR_NAVI_LINK_KEY			= "REQUEST_PAGE_NAVI_LINK_KEY"			;//分页基础RUI KEY
	public static final String REQUEST_ATTR_CURRENT_ACTION			= "REQUEST_ATTR_CURRENT_ACTION"			;//当前请求的action(String)
	public static final String REQUEST_ATTR_CURRENT_POWER			= "REQUEST_ATTR_CURRENT_POWER"			;//当前请求的action(Power)
	public static final String REQUEST_ATTR_CURRENT_POWER_CD		= "REQUEST_ATTR_CURRENT_POWER_CD"		;//当前请求的action(String)
	public static final String REQUEST_PARAM_PAGE_VOL				= "pageVol"								;
	public static final String REQUEST_PARAM_PAGE_NO				= "pageNo"								;
	public static final String REQUEST_ATTR_TEMPLATE_LAYOUT_PATH	= "REQUEST_ATTR_TEMPLATE_LAYOUT_PATH"	;//模板路径(布局)
	public static final String REQUEST_ATTR_TEMPLATE_STYLE_PATH		= "REQUEST_ATTR_TEMPLATE_STYLE_PATH"	;//模板路径(样式)
	public static final String REQUEST_ATTR_TEMPLATE_DATA_PATH		= "REQUEST_ATTR_TEMPLATE_DATA_PATH"		;//模板路径(数据)
	public static final String REQUEST_ATTR_MESSAGE					= "REQUEST_ATTR_MESSAGE"				;//
	public static final String REQUEST_ATTR_ACTION_URI				= "REQUEST_ATTR_ACTION_URI"				;
	
	public static final String HTML_NAME_TOKEN_VALUE				= "MVCTV"								;//token 值
	public static final String HTML_NAME_TOKEN_KEY					= "MCTK"								;//token 键
	public static final String HTML_NAME_TOKEN_KEY_PREFIX			= "T"									;//token键 前缀
	public static final String HTML_POSITION						= "HTML_POSITION"						;//当前位置
	
	public static final String MESSAGE_KEY							= "key"									;//消息KEY
	public static final String MESSAGE_VALUE						= "value"								;//消息VALUE
	public static final String MESSAGE_TYPE							= "type"								;//消息类型
	public static final String MESSAGE_TYPE_ERROR					= "error"								;//消息类型error
	public static final String MESSAGE_TYPE_INFO					= "info"								;//消息类型info

	
}