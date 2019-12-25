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
 
package org.anyline.struts.action; 
 
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
import org.anyline.jdbc.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.Constant;
import org.anyline.util.DESUtil;
import org.anyline.util.DESUtil;
import org.anyline.web.controller.AbstractBasicController;
import org.anyline.web.util.WebUtil;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 
public class AnylineAction extends AbstractBasicController implements ServletRequestAware, ServletResponseAware { 
	protected static final Logger log = LoggerFactory.getLogger(AnylineAction.class); 
	public static int RESULT_TYPE_DEFAULT = 0; 
	public static int RESULT_TYPE_JSON = 1; 
	 
	protected HttpServletRequest request; 
	protected HttpServletResponse response; 
	protected HttpSession session; 
	protected ServletContext servlet; 
	@Autowired(required = false) 
	@Qualifier("anyline.service") 
	protected AnylineService service; 
 
	protected Object data; // 返回数据 
	protected boolean result = true; // 执行结果 
	protected String msg; // 返回信息 
	protected String url; // 动态跳转 
	protected int result_type = RESULT_TYPE_DEFAULT; 
	protected String code = "200"; 
	 
 
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		return entity(request, clazz, keyEncrypt, valueEncrypt, params); 
	} 
 
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, String... params) { 
		return entity(request, clazz, keyEncrypt, false, params); 
	} 
 
	public <T> T entity(Class<T> clazz, String... params) { 
		return entity(request, clazz, false, false, params); 
	} 
 
	public DataRow entityRow(DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		return entityRow(request, row, keyEncrypt, valueEncrypt, params); 
	} 
 
	public DataRow entityRow(DataRow row, boolean keyEncrypt, String... params) { 
		return entityRow(request, row, keyEncrypt, false, params); 
	} 
 
	public DataRow entityRow(DataRow row, String... params) { 
		return entityRow(request, row, false, false, params); 
	} 
 
	public DataRow entityRow(boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		return entityRow(request, null, keyEncrypt, valueEncrypt, params); 
	} 
 
	public DataRow entityRow(boolean keyEncrypt, String... params) { 
		return entityRow(request, null, keyEncrypt, false, params); 
	} 
 
	public DataRow entityRow(String... params) { 
		return entityRow(request, null, false, false, params); 
	} 
 
	public DataSet entitySet(boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		return entitySet(request, keyEncrypt, valueEncrypt, params); 
	} 
 
	public DataSet entitySet(boolean keyEncrypt, String... params) { 
		return entitySet(request, keyEncrypt, false, params); 
	} 
 
	public DataSet entitySet(String... params) { 
		return entitySet(request, false, false, params); 
	} 
 
	protected ConfigStore parseConfig(boolean navi, String... configs) { 
		return parseConfig(request, navi, configs); 
	} 
 
	protected ConfigStore parseConfig(int vol, String... configs) { 
		return parseConfig(request, vol, configs); 
	} 
 
	protected ConfigStore parseConfig(int fr, int to, String... configs) { 
		return parseConfig(request, fr, to, configs); 
	} 
 
	protected ConfigStore parseConfig(String... conditions) { 
		return parseConfig(request, false, conditions); 
	} 
 
	protected String getParam(String key, boolean keyEncrypt, boolean valueEncrypt) { 
		return getParam(request, key, keyEncrypt, valueEncrypt); 
	} 
 
	protected String getParam(String key, boolean keyEncrypt) { 
		return getParam(request, key, keyEncrypt, false); 
	} 
 
	protected String getParam(String key) { 
		return getParam(request, key, false, false); 
	} 
 
	protected List<Object> getParams(String key,  boolean keyEncrypt, boolean valueEncrypt) { 
		return getParams(request, key, keyEncrypt, valueEncrypt); 
	} 
 
	protected List<Object> getParams(String key, boolean keyEncrypt) { 
		return getParams(request, key, keyEncrypt, false); 
	} 
 
	protected List<Object> getParams(String key) { 
		return getParams(request, key, false, false); 
	} 
 
	protected boolean checkRequired(boolean keyEncrypt, boolean valueEncrypt, String... params) { 
		return checkRequired(request, keyEncrypt, valueEncrypt, params); 
	} 
 
	protected boolean checkRequired(String... params) { 
		return checkRequired(request, false, false, params); 
	} 
 
	protected boolean isAjaxRequest() { 
		return isAjaxRequest(request); 
	} 
 
 
	protected void setRequestMessage(String key, Object value, String type) { 
		setRequestMessage(request, key, value, type); 
	} 
 
	protected void setRequestMessage(String key, Object value) { 
		setRequestMessage(request, key, value, null); 
	} 
 
	protected void setRequestMessage(Object value) { 
		setRequestMessage(request, BasicUtil.getRandomLowerString(10), value, null); 
	} 
 
	protected void setMessage(String key, Object value, String type) { 
		setRequestMessage(request, key, value, type); 
	} 
 
	protected void setMessage(String key, Object value) { 
		setMessage(request, key, value, null); 
	} 
 
	protected void setMessage(Object value) { 
		setMessage(request, BasicUtil.getRandomLowerString(10), value); 
	} 
 
	protected void setSessionMessage(String key, Object value, String type) { 
		setSessionMessage(request.getSession(), key, value, type); 
	} 
 
	protected void setSessionMessage(String key, Object value) { 
		setSessionMessage(request.getSession(), key, value, null); 
	} 
 
	protected void setSessionMessage(Object value) { 
		setSessionMessage(request.getSession(), BasicUtil.getRandomLowerString(10), value, null); 
	} 
 
	protected boolean hasReffer() { 
		return hasReffer(request); 
	} 
 
	protected boolean isSpider() { 
		return !hasReffer(request); 
	} 
 
	protected boolean isWap() { 
		return WebUtil.isWap(request); 
	} 
 
	public void setServletResponse(HttpServletResponse response) { 
		this.response = response; 
	} 
 
	public void setServletRequest(HttpServletRequest request) { 
		this.request = request; 
		this.session = request.getSession(); 
		this.servlet = this.session.getServletContext(); 
	} 
 
	/* ***************************************************************************************************************** 
	 *  
	 * 返回执行结果路径 
	 *  
	 * ******************************************************************************************************************/ 
	/** 
	 * 返回执行路径 
	 *  
	 * @param request request
	 * @param result 执行结果 
	 * @param success   执行成功时返回数据 
	 * @param fail   执行失败时返回数据 
	 * @return return
	 */ 
	protected String result(HttpServletRequest request, boolean result, Object success, Object fail) { 
		if (result) { 
			return success(request, success); 
		} else { 
			return fail(fail); 
		} 
	} 
 
	protected String result(boolean result, Object success, Object fail) { 
		return result(request, result, success, fail); 
	} 
	 
	/** 
	 * 执行成功 
	 *  
	 * @param request  request
	 * @param data  data
	 * @return return
	 */ 
	protected String success(HttpServletRequest request, Object ... data) { 
		return success(RESULT_TYPE_DEFAULT,request, data); 
	} 
	protected String success(int resultType, HttpServletRequest request, Object ... data) { 
		if(null != data){ 
			if(data.length ==1){				 
				this.data = data[0]; 
			}else{ 
				this.data = data; 
			} 
		} 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[result:success][url:{}][action return][action:{}]",request.getRequestURI(),getClass().getName()); 
		} 
		if (isAjaxRequest(request) || resultType == RESULT_TYPE_JSON) { 
			result = true; 
			return JSON; 
		} 
		return super.SUCCESS; 
	} 
	protected String success(Object ... data) { 
		return success(request, data); 
	} 
	protected String json(HttpServletRequest request, boolean result, Object ... data) { 
		this.result = result; 
		if(result){ 
			return success(RESULT_TYPE_JSON,request, data); 
		}else{ 
			return fail(RESULT_TYPE_JSON,data); 
		} 
		 
	} 
 
	protected String json(boolean result, Object ... data) { 
		return json(request, result,data); 
	} 
	/** 
	 * 加密仅支持String类型 不支持对象加密  
	 * @param data  data
	 * @param encrypt  encrypt
	 * @return return
	 */ 
	protected String success(Object data, boolean encrypt) { 
		if(encrypt && null != data){ 
			return result(true,DESUtil.encryptParamValue(data.toString()),null); 
		} 
		return success(request, data); 
	} 
	protected String success(HttpServletRequest request) { 
		return success(request, data); 
	} 
 
	protected String success() { 
		return success(request, data); 
	} 
	public String navi(DataSet data, String page, Object ext){ 
		if(null == data){ 
			data = (DataSet)request.getAttribute("_anyline_navi_data"); 
		}else{ 
			request.setAttribute("_anyline_navi_data", data); 
		} 
		PageNavi navi = null; 
		if(null != data){ 
			navi = data.getNavi(); 
		} 
		Map<String,Object> map = super.navi(request, response, data, navi, page, ext); 
		return success(map); 
	} 
	public String navi(DataSet data, String page){ 
		return navi(data, page, null); 
	} 
	protected String jsonFail(Object... msgs){ 
		return fail(RESULT_TYPE_JSON, msgs); 
	} 
	protected String fail(Object... msgs) { 
		return fail(RESULT_TYPE_DEFAULT, msgs); 
	} 
	/** 
	 * 执行失败 
	 * @param resultType resultType
	 * @param msgs msgs
	 * @return return
	 */ 
	protected String fail(int resultType, Object... msgs) { 
		result = false; 
		if (null != msgs && msgs.length > 0) { 
			for (Object msg : msgs) { 
				setMessage(request, msg); 
			} 
		} 
		String html = ""; 
		DataSet messages = (DataSet) request.getAttribute(Constant.REQUEST_ATTR_MESSAGE); 
		if (null != messages) { 
			for (int i = 0; i < messages.size(); i++) { 
				DataRow msg = messages.getRow(i); 
				html += "\n" + msg.getString(Constant.MESSAGE_VALUE); 
				messages.remove(msg); 
			} 
		} 
		msg = BasicUtil.nvl(msg, "").toString() + BasicUtil.nvl(html, "").toString().trim(); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[result:fail][message:{}][url:{}][action return][action:{}]",msg, request.getRequestURI(),getClass().getName()); 
		} 
		request.getSession().setAttribute(Constant.SESSION_ATTR_ERROR_MESSAGE, msg); 
		if (isAjaxRequest(request) || RESULT_TYPE_JSON == resultType) { 
			return JSON; 
		} else { 
			return FAIL; 
		} 
	} 
	 
 
	/** 
	 *  
	 * @param layout 布局path 
	 * @param style  样式path 
	 * @param data	  数据path 
	 */ 
	protected void setTemplate(String layout, String style, String data){ 
		request.setAttribute(Constant.REQUEST_ATTR_TEMPLATE_LAYOUT_PATH, layout); 
		try{ 
			if(null != style){ 
				if(style.startsWith("/")){ 
					//从根目录开始 
				}else{ 
					//根据内容页相对目录 
					int idx = this.getDir().indexOf("/page/"); 
					if(idx > 0){ 
						String styleDir = this.getDir().replace("/page/", "/template/style/"); 
						if(!styleDir.endsWith("/")){ 
							styleDir = styleDir + "/"; 
						} 
						style = styleDir + style; 
					} 
				} 
			}else{ 
				if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
					log.warn("[未设置样式模板] [原因:有可能需要数据url中通过parseJsp合成样式与数据]"); 
				} 
			} 
			request.setAttribute(Constant.REQUEST_ATTR_TEMPLATE_STYLE_PATH, DESUtil.getInstance().encrypt(style)); 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		request.setAttribute(Constant.REQUEST_ATTR_TEMPLATE_DATA_PATH, data); 
	} 
	protected void template(String layout, String style, String data){ 
		setTemplate(layout, style, data); 
	} 
	protected void template(String template){ 
		template(template, null, null); 
	} 
 
 
	 
	 
	public Object getData() { 
		return data; 
	} 
 
	public void setData(Object data) { 
		this.data = data; 
	} 
 
	public boolean isResult() { 
		return result; 
	} 
 
	public void setResult(boolean result) { 
		this.result = result; 
	} 
 
	public String getMsg() { 
		return msg; 
	} 
 
	public void setMsg(String message) { 
		this.msg = message; 
	} 
 
	public String getUrl() { 
		return url; 
	} 
 
	public void setUrl(String url) { 
		this.url = url; 
	} 
 
 
}
