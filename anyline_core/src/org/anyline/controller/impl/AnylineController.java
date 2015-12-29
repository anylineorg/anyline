package org.anyline.controller.impl;

import java.util.HashMap;
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
import org.anyline.entity.ClientTrace;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.Constant;
import org.anyline.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public  class AnylineController extends AbstractBasicController{

	@Autowired(required=false)
	@Qualifier("anylineService")
	protected AnylineService service;
	
	/**
	 * 当前线程下的request
	 * @return
	 */
	protected HttpServletRequest getRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}
	protected HttpServletResponse getResponse(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
	}
	protected HttpSession getSession(){
		return getRequest().getSession();
	}
	protected ServletContext getServlet(){
		return getSession().getServletContext();
	}
	
	
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params){
		return entity(getRequest(), clazz, keyEncrypt, valueEncrypt, params);
	}
	
	
	public <T> T entity(Class<T> clazz, boolean keyEncrypt, String... params) {
		return entity(getRequest(),clazz, keyEncrypt, false, params);
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
		return entityRow(getRequest(),null, keyEncrypt, valueEncrypt, params);
	}


	public DataRow entityRow(boolean keyEncrypt, String... params) {
		return entityRow(getRequest(),null, keyEncrypt, false, params);
	}
	

	public DataRow entityRow(String... params) {
		return entityRow(getRequest(), null, false, false, params);
	}
	
	public DataSet entitySet(boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entitySet(getRequest(), keyEncrypt, valueEncrypt, params);
	}

	public DataSet entitySet(boolean keyEncrypt, String... params) {
		return entitySet(getRequest(),keyEncrypt, false, params);
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

	protected String getParam(String key, boolean keyEncrypt) {
		return getParam(getRequest(),key, keyEncrypt, false);
	}
	

	protected String getParam(String key) {
		return getParam(getRequest(), key, false, false);
	}


	protected List<Object> getParams(String key, boolean keyEncrypt) {
		return getParams(getRequest(), key, keyEncrypt, false);
	}


	protected List<Object> getParams(String key) {
		return getParams(getRequest(),key, false, false);
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
	
	
	

	protected ClientTrace currentClient() {
		return currentClient(getRequest());
	}
	


	protected String currentClientCd() {
		return currentClientCd(getRequest());
	}
	
	

	protected void setRequestMessage(String key, Object value, String type) {
		setRequestMessage(getRequest(), key, value, type);
	}
	
	protected void setRequestMessage(String key, Object value) {
		setRequestMessage(getRequest(),key, value, null);
	}

	protected void setRequestMessage(Object value) {
		setRequestMessage(getRequest(),BasicUtil.getRandomLowerString(10), value, null);
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
	protected String fail(Object... msgs) {
		return fail(getRequest(), msgs);
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
	 * @param success
	 *            执行成功时返回数据
	 * @param fail
	 *            执行失败时返回数据
	 */

	public String result(boolean result, Object obj){
		Object data = obj;
    	String message = "";

		DataSet messages = (DataSet) getRequest().getAttribute(Constant.REQUEST_ATTR_MESSAGE);
		if (null != messages) {
			for (int i = 0; i < messages.size(); i++) {
				DataRow msg = messages.getRow(i);
				message += "\n" + msg.getString(Constant.MESSAGE_VALUE);
			}
			getRequest().removeAttribute(Constant.REQUEST_ATTR_MESSAGE);
		}
    	//转换成JSON格式
    	JsonConfig config = new JsonConfig();
    	String dataType   = null; 	//数据类型
    	if(null == data){
    		message = (String)BasicUtil.nvl(message, "没有返回数据");
    		data = false;
    	}else if(data instanceof Iterable){
			dataType = "list";
    	}else if (data instanceof DataSet) {
    		DataSet set = (DataSet)data;
    		result = set.isSuccess();
    		message += (String)BasicUtil.nvl(message,set.getMessage());
			dataType = "list";
			data = set.getRows();
		}else if (data instanceof DataRow) {
			dataType = "map";
		}else if(data instanceof Map){
			dataType = "map";
		}else if(data instanceof String){
			dataType = "string";
			//data = BasicUtil.convertJSONChar(data.toString());
			data = data.toString();
		}else if(data instanceof Number){
			dataType = "number";
			data = data.toString();
		}else{
			dataType = "map";
		}
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("type", dataType);
    	map.put("result", result);
    	map.put("message", message);
    	map.put("data", data);
    	map.put("success", result);
    	
    	JSON json = JSONObject.fromObject(map);
    	return json.toString();
	}
	/**
	 * 执行失败
	 * 
	 * @return
	 */
	protected String fail(Object msg) {
		return result(false, msg);
	}
	protected String fail() {
		return result(false, null);
	}
	protected String success(Object msg) {
		return result(true, msg);
	}
	protected String success() {
		return result(true, null);
	}
}
