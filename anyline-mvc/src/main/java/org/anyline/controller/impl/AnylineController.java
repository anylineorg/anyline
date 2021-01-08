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
 
package org.anyline.controller.impl; 
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.Constant;
import org.anyline.util.DESUtil;
import org.anyline.util.FileUtil;
import org.anyline.web.controller.AbstractBasicController;
import org.anyline.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
 
public class AnylineController extends AbstractBasicController { 
 
	@Autowired(required = false) 
	@Qualifier("anyline.service") 
	protected AnylineService service; 
	protected HttpServletRequest _request;
	protected HttpServletResponse _response;
	/** 
	 * 当前线程下的request 
	 *  
	 * @return return
	 */ 
	protected HttpServletRequest getRequest() {

		if(null == _request){
			_request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		} 
		return _request;
	} 
	@Autowired 
	protected void setRequest(HttpServletRequest request){ 
		this._request = request;
		try{ 
			this._request.setCharacterEncoding(ConfigTable.getString("HTTP_ENCODEING","UTF-8"));
		}catch(Exception e){ 
			 
		} 
	} 
	protected HttpServletResponse getResponse() { 
		if(null == _response){
			_response =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		} 
		return _response;
	} 
	@Autowired 
	public void setResponse(HttpServletResponse response){ 
		this._response = response;
		try{ 
			this._response.setCharacterEncoding(ConfigTable.getString("HTTP_ENCODEING","UTF-8"));
		}catch(Exception e){ 
			 
		} 
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

	@Deprecated
	public DataRow entityRow(DataRow.KEY_CASE keyCase,DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), keyCase, row, keyEncrypt, valueEncrypt, params);
	}

	@Deprecated
	public DataRow entityRow(DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, keyEncrypt, valueEncrypt, params);
	}
	@Deprecated
	public DataRow entityRow(DataRow.KEY_CASE keyCase,DataRow row, boolean keyEncrypt, String... params) {
		return entity(getRequest(),keyCase, row, keyEncrypt, false, params);
	}

	@Deprecated
	public DataRow entityRow(DataRow row, boolean keyEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, keyEncrypt, false, params);
	}
	@Deprecated
	public DataRow entityRow(DataRow.KEY_CASE keyCase, DataRow row, String... params) {
		return entity(getRequest(),keyCase, row, false, false, params);
	}

	@Deprecated
	public DataRow entityRow(DataRow row, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, false, false, params);
	}
	@Deprecated
	public DataRow entityRow(DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(),keyCase, null, keyEncrypt, valueEncrypt, params);
	}
	@Deprecated
	public DataRow entity(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, null, keyEncrypt, valueEncrypt, params);
	}

	@Deprecated
	public DataRow entity(DataRow.KEY_CASE keyCase, boolean keyEncrypt, String... params) {
		return entity(getRequest(),keyCase, null, keyEncrypt, false, params);
	}

	@Deprecated
	public DataRow entityRow(boolean keyEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, null, keyEncrypt, false, params);
	}

	@Deprecated
	public DataRow entityRow(DataRow.KEY_CASE keyCase, String... params) {
		return entity(getRequest(),keyCase, null, false, false, params);
	}
	@Deprecated
	public DataRow entityRow(String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, null, false, false, params);
	}

	@Deprecated
	public DataSet entitySet(DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitys(getRequest(),keyCase, keyEncrypt, valueEncrypt, params);
	}

	@Deprecated
	public DataSet entitySet(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, keyEncrypt, valueEncrypt, params);
	}
	@Deprecated
	public DataSet entitySet(DataRow.KEY_CASE keyCase, boolean keyEncrypt, String... params) {
		return entitys(getRequest(),keyCase, keyEncrypt, false, params);
	}
	@Deprecated
	public DataSet entitySet(boolean keyEncrypt, String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, keyEncrypt, false, params);
	}

	@Deprecated
	public DataSet entitySet(DataRow.KEY_CASE keyCase, String... params) {
		return entitys(getRequest(),keyCase, false, false, params);
	}
	@Deprecated
	public DataSet entitySet(String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, false, false, params);
	}

	protected ConfigStore parseConfig(boolean navi, String... configs) {
		return parseConfig(getRequest(), navi, configs);
	} 


	public DataRow entity(DataRow.KEY_CASE keyCase,DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), keyCase, row, keyEncrypt, valueEncrypt, params);
	}

	public DataRow entity(DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, keyEncrypt, valueEncrypt, params);
	}
	public DataRow entity(DataRow.KEY_CASE keyCase,DataRow row, boolean keyEncrypt, String... params) {
		return entity(getRequest(),keyCase, row, keyEncrypt, false, params);
	}

	public DataRow entity(DataRow row, boolean keyEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, keyEncrypt, false, params);
	}
	public DataRow entity(DataRow.KEY_CASE keyCase, DataRow row, String... params) {
		return entity(getRequest(),keyCase, row, false, false, params);
	}

	public DataRow entity(DataRow row, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, row, false, false, params);
	}
	public DataRow entity(DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entity(getRequest(),keyCase, null, keyEncrypt, valueEncrypt, params);
	}


	public DataRow entity(boolean keyEncrypt, String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, null, keyEncrypt, false, params);
	}

	public DataRow entity(DataRow.KEY_CASE keyCase, String... params) {
		return entity(getRequest(),keyCase, null, false, false, params);
	}
	public DataRow entity(String... params) {
		return entity(getRequest(), DataRow.KEY_CASE.CONFIG, null, false, false, params);
	}

	public DataSet entitys(DataRow.KEY_CASE keyCase, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitys(getRequest(),keyCase, keyEncrypt, valueEncrypt, params);
	}

	public DataSet entitys(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, keyEncrypt, valueEncrypt, params);
	}
	public DataSet entitys(DataRow.KEY_CASE keyCase, boolean keyEncrypt, String... params) {
		return entitys(getRequest(),keyCase, keyEncrypt, false, params);
	}
	public DataSet entitys(boolean keyEncrypt, String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, keyEncrypt, false, params);
	}

	public DataSet entitys(DataRow.KEY_CASE keyCase, String... params) {
		return entitys(getRequest(),keyCase, false, false, params);
	}
	public DataSet entitys(String... params) {
		return entitys(getRequest(), DataRow.KEY_CASE.CONFIG, false, false, params);
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
 
 
	protected ConfigStore condition(boolean navi, String... configs) { 
		return parseConfig(getRequest(), navi, configs); 
	} 
 
	protected ConfigStore condition(int vol, String... configs) { 
		return parseConfig(getRequest(), vol, configs); 
	} 
 
	protected ConfigStore condition(int fr, int to, String... configs) { 
		return parseConfig(getRequest(), fr, to, configs); 
	} 
 
	protected ConfigStore condition(String... conditions) { 
		return parseConfig(getRequest(), false, conditions); 
	} 
 
	 
 
	protected ConfigStore where(boolean navi, String... configs) { 
		return parseConfig(getRequest(), navi, configs); 
	} 
 
	protected ConfigStore where(int vol, String... configs) { 
		return parseConfig(getRequest(), vol, configs); 
	} 
 
	protected ConfigStore where(int fr, int to, String... configs) { 
		return parseConfig(getRequest(), fr, to, configs); 
	} 
 
	protected ConfigStore where(String... conditions) { 
		return parseConfig(getRequest(), false, conditions); 
	} 
	 
	 
	protected String getParam(String key, boolean keyEncrypt, boolean valueEncrypt, String ... defs) {
		return getParam(getRequest(), key, keyEncrypt, valueEncrypt, defs);
	} 
 
	protected String getParam(String key, boolean valueEncrypt, String ... defs) {
		return getParam(getRequest(), key, false, valueEncrypt, defs);
	} 
 
	protected String getParam(String key, String ... defs) {
		return getParam(getRequest(), key, false, false, defs);
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
 
 
	 
	protected String param(String key, boolean keyEncrypt, boolean valueEncrypt, String ... defs) {
		return getParam(getRequest(), key, keyEncrypt, valueEncrypt, defs);
	} 
 
	protected String param(String key, boolean valueEncrypt, String ... defs) {
		return getParam(getRequest(), key, false, valueEncrypt, defs);
	} 
 
	protected String param(String key, String ... defs) {
		return getParam(getRequest(), key, false, false, defs);
	} 
 
	protected List<Object> params(String key, boolean keyEncrypt, boolean valueEncrypt) { 
		return getParams(getRequest(), key, keyEncrypt, valueEncrypt); 
	} 
	protected List<Object> params(String key, boolean valueEncrypt) { 
		return getParams(getRequest(), key, false, valueEncrypt); 
	} 
 
	protected List<Object> params(String key) { 
		return getParams(getRequest(), key, false, false); 
	}





	protected int getInt(String key, boolean keyEncrypt, boolean valueEncrypt) throws Exception{
		return getInt(getRequest(), key, keyEncrypt, valueEncrypt);
	}

	protected int getInt(String key, boolean valueEncrypt) throws Exception{
		return getInt(getRequest(), key, valueEncrypt);
	}

	protected int getInt(String key) throws Exception{
		return getInt(getRequest(), key);
	}

	protected int getInt(String key, boolean keyEncrypt, boolean valueEncrypt, int def){
		return getInt(getRequest(), key, keyEncrypt, valueEncrypt, def);
	}

	protected int getInt(String key, boolean valueEncrypt, int def) {
		return getInt(getRequest(), key, valueEncrypt, def);
	}

	protected int getInt(String key, int def) {
		return getInt(getRequest(), key, def);
	}



	protected double getDouble(String key, boolean keyEncrypt, boolean valueEncrypt) throws Exception{
		return getDouble(getRequest(), key, keyEncrypt, valueEncrypt);
	}

	protected double getDouble(String key, boolean valueEncrypt) throws Exception{
		return getDouble(getRequest(), key, valueEncrypt);
	}

	protected double getDouble(String key) throws Exception{
		return getDouble(getRequest(), key);
	}

	protected double getDouble(String key, boolean keyEncrypt, boolean valueEncrypt, double def){
		return getDouble(getRequest(), key, keyEncrypt, valueEncrypt, def);
	}

	protected double getDouble(String key, boolean valueEncrypt, double def) {
		return getDouble(getRequest(), key, valueEncrypt, def);
	}

	protected double getDouble(String key, double def) {
		return getDouble(getRequest(), key, def);
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
	 * @param result  执行结果 
	 * @param data   返回数据 
	 * @param message  message
	 * @return return
	 */ 
	public String result(String code, boolean result, Object data, String message) {
		DataSet messages = (DataSet) getRequest().getAttribute(Constant.REQUEST_ATTR_MESSAGE); 
		if (null != messages) { 
			for (int i = 0; i < messages.size(); i++) { 
				DataRow msg = messages.getRow(i); 
				message = BasicUtil.nvl(message,"") + "\n" + msg.getString(Constant.MESSAGE_VALUE); 
			} 
			getRequest().removeAttribute(Constant.REQUEST_ATTR_MESSAGE); 
		} 
 
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
		map.put("code", code);
    	map.put("request_time", getRequest().getParameter("_anyline_request_time")); 
    	map.put("response_time_fr", getRequest().getAttribute("_anyline_response_time_fr")); 
    	map.put("response_time_to", System.currentTimeMillis()); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[controller return][result:{}][message:{}]",result,message); 
		} 
		return BeanUtil.map2json(map); 
	} 
	/** 
	 *  
	 * @param msg  msg
	 * @param encrypt	是否加密  encrypt	是否加密
	 * @return return
	 */ 
	protected String fail(String msg, boolean encrypt) { 
		if(encrypt){ 
			msg = DESUtil.encryptParamValue(msg); 
		} 
		return result("-1",false, null, msg);
	}
	protected String fail(String code, String msg, boolean encrypt) {
		if(encrypt){
			msg = DESUtil.encryptParamValue(msg);
		}
		return result(code,false, null, msg);
	}

	protected String fail(String msg) {
		return result("-1",false, null, msg);
	}
	protected String fail(String code, String msg) {
		return result(code,false, null, msg);
	}
	protected String fail() {
		String msg = null;
		return fail(msg);
	} 
 
	/** 
	 * 加密仅支持String类型 不支持对象加密 
	 * @param data  data
	 * @param encrypt  encrypt
	 * @return return
	 */ 
	protected String success(Object data, boolean encrypt) { 
		if(encrypt && null != data){ 
			return result("200",true,DESUtil.encryptParamValue(data.toString()),null);
		} 
		return result("200",true, data, null);
	}

	protected String success(Object data) {
		return result("200",true, data, null);
	}
	protected String success(Object ... data) {
		return result("200",true, data, null);
	}
	/** 
	 * AJAX分页时调用  
	 * 分数数据在服务器生成 
	 * @param adapt adapt
	 * @param request request
	 * @param response response
	 * @param data	数据 request.setAttribute("_anyline_navi_data", data); 
	 * @param page	生成分页数据的模板(与JSP语法一致)  page	生成分页数据的模板(与JSP语法一致)
	 * @param ext	扩展数据	  ext	扩展数据
	 * @return return
	 */ 
	public String navi(boolean adapt, HttpServletRequest request, HttpServletResponse response, DataSet data, String page, Object ext){
		 
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

		String clientType = "web";
		if (WebUtil.isWap(request)) {
			clientType = "wap";
		}

		if (null != page) {
			if (adapt) {
				page = page.replace("/web/", "/" + clientType + "/");
				page = page.replace("/wap/", "/" + clientType + "/");
			}

			page = page.replace("${client_type}", clientType);
			page = page.replace("${client}", clientType);
		}

		if (null != request) {
			Map<String, Object> map = (Map)request.getAttribute("anyline_template_variable");
			if (null == map) {
				map = (Map)request.getSession().getAttribute("anyline_template_variable");
			}

			if (null != map) {
				Iterator var4 = map.keySet().iterator();

				while(var4.hasNext()) {
					String key = (String)var4.next();
					Object value = map.get(key);
					if (null != value) {
						page = page.replace("${" + key + "}", value.toString());
					}
				}
			}
		}
		Map<String,Object> map = super.navi(request, response, data, navi, page, ext); 
		return success(map); 
	}
	public String navi(HttpServletRequest request, HttpServletResponse response, DataSet data, String page, Object ext){
		return navi(false,request, response, data, page, ext);
	}
	public String navi(HttpServletRequest request, HttpServletResponse response, DataSet data, String page){
		return navi(request, response, data, page ,null);
	}
	public String navi(boolean adapt, HttpServletRequest request, HttpServletResponse response, DataSet data, String page){
		return navi(adapt,request, response, data, page ,null);
	}
	public String navi(HttpServletResponse response, String page){ 
		return navi(null, response, null, page, null); 
	} 
	public String navi(HttpServletResponse response, DataSet data, String page){ 
		return navi(getRequest(), response, data, page, null); 
	} 
	/** 
	 * 上传文件 
	 * @param dir  dir
	 * @return return
	 * @throws IllegalStateException IllegalStateException
	 * @throws IOException  IOException
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
