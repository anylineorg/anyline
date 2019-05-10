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

package org.anyline.controller.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.anyline.config.http.ConfigStore;
import org.anyline.controller.AbstractBasicController;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.Constant;
import org.anyline.util.FileUtil;
import org.anyline.util.JSONDateFormatProcessor;
import org.anyline.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class AnylineController extends AbstractBasicController {

	@Autowired(required = false)
	@Qualifier("anylineService")
	protected AnylineService service;

	/**
	 * 当前线程下的request
	 * 
	 * @return
	 */
	protected HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		try{
			request.setCharacterEncoding(ConfigTable.getString("HTTP_ENCODEING","UTF-8"));
		}catch(Exception e){
			
		}
		return request;
	}
	protected HttpServletResponse getResponse() {
		HttpServletResponse response =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		try{
			response.setCharacterEncoding(ConfigTable.getString("HTTP_ENCODEING","UTF-8"));
		}catch(Exception e){
			
		}
		return response;
	}

	protected HttpSession getSession() {
		return getRequest().getSession();
	}

	protected ServletContext getServlet() {
		return getSession().getServletContext();
	}

	public <T> T entity(Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), clazz, keyEncrypt, valueEncrypt, params);
	}

	public <T> T entity(Class<T> clazz, boolean keyEncrypt, String... params) {
		return entity(getRequest(), clazz, keyEncrypt, false, params);
	}

	public <T> T entity(Class<T> clazz, String... params) {
		return entity(getRequest(), clazz, false, false, params);
	}

	public DataRow entityRow(DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entityRow(getRequest(), row, keyEncrypt, valueEncrypt, params);
	}

	public DataRow entityRow(DataRow row, boolean keyEncrypt, String... params) {
		return entityRow(getRequest(), row, keyEncrypt, false, params);
	}

	public DataRow entityRow(DataRow row, String... params) {
		return entityRow(getRequest(), row, false, false, params);
	}

	public DataRow entityRow(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entityRow(getRequest(), null, keyEncrypt, valueEncrypt, params);
	}

	public DataRow entityRow(boolean keyEncrypt, String... params) {
		return entityRow(getRequest(), null, keyEncrypt, false, params);
	}

	public DataRow entityRow(String... params) {
		return entityRow(getRequest(), null, false, false, params);
	}

	public DataSet entitySet(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitySet(getRequest(), keyEncrypt, valueEncrypt, params);
	}

	public DataSet entitySet(boolean keyEncrypt, String... params) {
		return entitySet(getRequest(), keyEncrypt, false, params);
	}

	public DataSet entitySet(String... params) {
		return entitySet(getRequest(), false, false, params);
	}

	protected ConfigStore parseConfig(boolean navi, String... configs) {
		return parseConfig(getRequest(), navi, configs);
	}

	protected ConfigStore parseConfig(int vol, String... configs) {
		return parseConfig(getRequest(), vol, configs);
	}

	protected ConfigStore parseConfig(int fr, int to, String... configs) {
		return parseConfig(getRequest(), fr, to, configs);
	}

	protected ConfigStore parseConfig(String... conditions) {
		return parseConfig(getRequest(), false, conditions);
	}

	protected String getParam(String key, boolean keyEncrypt, boolean valueEncrypt) {
		return getParam(getRequest(), key, keyEncrypt, valueEncrypt);
	}

	protected String getParam(String key, boolean valueEncrypt) {
		return getParam(getRequest(), key, false, valueEncrypt);
	}

	protected String getParam(String key) {
		return getParam(getRequest(), key, false, false);
	}

	protected List<Object> getParams(String key, boolean keyEncrypt, boolean valueEncrypt) {
		return getParams(getRequest(), key, keyEncrypt, valueEncrypt);
	}
	protected List<Object> getParams(String key, boolean valueEncrypt) {
		return getParams(getRequest(), key, false, valueEncrypt);
	}

	protected List<Object> getParams(String key) {
		return getParams(getRequest(), key, false, false);
	}

	protected boolean checkRequired(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return checkRequired(getRequest(), keyEncrypt, valueEncrypt, params);
	}

	protected boolean checkRequired(String... params) {
		return checkRequired(getRequest(), false, false, params);
	}

	protected boolean isAjaxRequest() {
		return isAjaxRequest(getRequest());
	}


	protected void setRequestMessage(String key, Object value, String type) {
		setRequestMessage(getRequest(), key, value, type);
	}

	protected void setRequestMessage(String key, Object value) {
		setRequestMessage(getRequest(), key, value, null);
	}

	protected void setRequestMessage(Object value) {
		setRequestMessage(getRequest(), BasicUtil.getRandomLowerString(10), value, null);
	}

	protected void setMessage(String key, Object value, String type) {
		setRequestMessage(getRequest(), key, value, type);
	}

	protected void setMessage(String key, Object value) {
		setMessage(getRequest(), key, value, null);
	}

	protected void setMessage(Object value) {
		setMessage(getRequest(), BasicUtil.getRandomLowerString(10), value);
	}

	protected void setSessionMessage(String key, Object value, String type) {
		setSessionMessage(getRequest().getSession(), key, value, type);
	}

	protected void setSessionMessage(String key, Object value) {
		setSessionMessage(getRequest().getSession(), key, value, null);
	}

	protected void setSessionMessage(Object value) {
		setSessionMessage(getRequest().getSession(), BasicUtil.getRandomLowerString(10), value, null);
	}

	protected boolean hasReffer() {
		return hasReffer(getRequest());
	}

	protected boolean isSpider() {
		return !hasReffer(getRequest());
	}

	protected boolean isWap() {
		return WebUtil.isWap(getRequest());
	}

	/******************************************************************************************************************
	 * 
	 * 返回执行结果路径
	 * 
	 *******************************************************************************************************************/
	/**
	 * 返回执行路径
	 * 
	 * @param result
	 *            执行结果
	 * @param data
	 *            返回数据
	 * @param message
	 * 
	 */
	public String result(boolean result, Object data, String message) {
		DataSet messages = (DataSet) getRequest().getAttribute(Constant.REQUEST_ATTR_MESSAGE);
		if (null != messages) {
			for (int i = 0; i < messages.size(); i++) {
				DataRow msg = messages.getRow(i);
				message = BasicUtil.nvl(message,"") + "\n" + msg.getString(Constant.MESSAGE_VALUE);
			}
			getRequest().removeAttribute(Constant.REQUEST_ATTR_MESSAGE);
		}
		// 转换成JSON格式
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Date.class, new JSONDateFormatProcessor());  
		config.registerJsonValueProcessor(Timestamp.class, new JSONDateFormatProcessor());

		Map<String, Object> map = new HashMap<String, Object>();
		String dataType = null; // 数据类型
		if (null == data) {
			message = (String) BasicUtil.nvl(message, "没有返回数据");
			data = "";
		} else if (data instanceof DataSet) {
			DataSet set = (DataSet) data;
			message += (String) BasicUtil.nvl(message, set.getMessage());
			dataType = "list";
			data = set.getRows();
			map.put("navi", set.getNavi());
		} else if (data instanceof Iterable) {
			dataType = "list";
		} else if (data instanceof DataRow) {
			dataType = "map";
		} else if (data instanceof Map) {
			dataType = "map";
		} else if (data instanceof String) {
			dataType = "string";
			// data = BasicUtil.convertJSONChar(data.toString());
			data = data.toString();
		} else if (data instanceof Number) {
			dataType = "number";
			data = data.toString();
		} else {
			dataType = "map";
		}
		if (!result && null != data) {
			message += data.toString();
		}
		map.put("type", dataType);
		map.put("result", result);
		map.put("message", message);
		map.put("data", data);
		map.put("success", result);
		map.put("code", "200");
    	map.put("request_time", getRequest().getParameter("_anyline_request_time"));
    	map.put("response_time_fr", getRequest().getAttribute("_anyline_response_time_fr"));
    	map.put("response_time_to", System.currentTimeMillis());
		if(ConfigTable.isDebug()){
			log.warn("[Controller Return][result:"+result+"][message:"+message+"]");
		}
		JSON json = JSONObject.fromObject(map,config);
		return json.toString();
	}
	/**
	 * 
	 * @param msg
	 * @param encrypt	是否加密
	 * @return
	 */
	protected String fail(String msg, boolean encrypt) {
		if(encrypt){
			msg = WebUtil.encryptHttpRequestParamValue(msg);
		}
		return result(false, null, msg);
	}

	protected String fail(String msg) {
		return result(false, null, msg);
	}
	protected String fail() {
		return fail(null);
	}

	/**
	 * 加密仅支持String类型 不支持对象加密
	 * @param data
	 * @param encrypt
	 * @return
	 */
	protected String success(Object data, boolean encrypt) {
		if(encrypt && null != data){
			return result(true,WebUtil.encryptHttpRequestParamValue(data.toString()),null);
		}
		return result(true, data, null);
	}

	protected String success(Object ... data) {
		if(null != data && data.length ==1){
			return result(true, data[0], null);
		}
		return result(true, data, null);
	}
	/**
	 * AJAX分页时调用 
	 * 分数数据在服务器生成
	 * @param data	数据 request.setAttribute("_anyline_navi_data", data);
	 * @param page	生成分页数据的模板(与JSP语法一致)
	 * @param ext	扩展数据	
	 * @return
	 */
	public String navi(HttpServletRequest request, HttpServletResponse response, DataSet data, String page, Object ext){
		
		if(null == request){
			request = getRequest();
		}
		if(null == response){
			response = getResponse();
		}
		if(null == data){
			data = (DataSet)request.getAttribute("_anyline_navi_data");
		}else{
			request.setAttribute("_anyline_navi_data", data);
		}
		PageNavi navi = null;
		if(null != data){
			navi = data.getNavi();
		}
		if(page != null && !page.startsWith("/")){
			page = "/WEB-INF/"+page;
		}
		Map<String,Object> map = super.navi(request, response, data, navi, page, ext);
		return success(map);
	}
	public String navi(HttpServletRequest request, HttpServletResponse response, DataSet data, String page){
		return navi(request, response, data, page ,null);
	}
	public String navi(HttpServletResponse response, String page){
		return navi(null, response, null, page, null);
	}
	public String navi(HttpServletResponse response, DataSet data, String page){
		return navi(getRequest(), response, data, page, null);
	}
	/**
	 * 上传文件
	 * @param request
	 * @param dir
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public List<File> upload(File dir) throws IllegalStateException, IOException {
		List<File> result = new ArrayList<File>();
		HttpServletRequest request = getRequest();
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (multipartResolver.isMultipart(request)) {
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得request中的所有文件名
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				// 取得上传文件
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					// 取得当前上传文件的文件名称
					String fileName = file.getOriginalFilename();
					// 如果名称不为"",说明该文件存在，否则说明该文件不存在
					if (BasicUtil.isNotEmpty(fileName)) {
						// 重命名上传后的文件名
						String sufName = FileUtil.getSuffixFileName(fileName);
						// 定义上传路径
						File localFile = new File(dir,BasicUtil.getRandomLowerString(10)+"."+sufName);
						file.transferTo(localFile);
						result.add(localFile);
					}
				}
			}

		}
		return result;
	}


}
