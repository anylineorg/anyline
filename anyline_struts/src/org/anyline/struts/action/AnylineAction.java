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

package org.anyline.struts.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.anyline.config.http.ConfigStore;
import org.anyline.controller.AbstractBasicController;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.Constant;
import org.anyline.util.DESUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.WebUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AnylineAction extends AbstractBasicController implements ServletRequestAware, ServletResponseAware {
	protected static Logger LOG = Logger.getLogger(AnylineAction.class);
	public static int RESULT_TYPE_DEFAULT = 0;
	public static int RESULT_TYPE_JOSN = 1;
	
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session;
	protected ServletContext servlet;
	@Autowired(required = false)
	@Qualifier("anylineService")
	protected AnylineService service;

	protected Object data; // 返回数据
	protected boolean result = true; // 执行结果
	protected String msg; // 返回信息
	protected String url; // 动态跳转
	protected int result_type = RESULT_TYPE_DEFAULT;
	protected String code = "200";
	
	
	
	protected List<File> upload;
	protected List<String> uploadContentType;
	protected List<String> uploadFileName;

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
	 * @param result
	 * @return
	 */
	protected String success(HttpServletRequest request, Object ... data) {
		return success(null, request, data);
	}

	protected String success(int resultType, HttpServletRequest request, Object ... data) {
		if(null != data){
			if(data.length ==1){				
				this.data = data[0];
			}else{
				this.data = data;
			}
		}
		if (isAjaxRequest(request) || resultType == RESULT_TYPE_JOSN) {
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
			return success(RESULT_TYPE_JOSN,request, data);
		}else{
			return fail(RESULT_TYPE_JOSN,data);
		}
		
	}

	protected String json(boolean result, Object ... data) {
		return json(request, result,data);
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

	/**
	 * 执行失败
	 * 
	 * @return
	 */
	protected String fail(Object... msgs) {
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
		if(ConfigTable.isDebug()){
			log.warn("[Action Return][result:fail][message:"+msg+"]");
		}
		request.getSession().setAttribute(Constant.SESSION_ATTR_ERROR_MESSAGE, msg);
		if (isAjaxRequest(request) || RESULT_TYPE_JOSN == result_type) {
			return JSON;
		} else {
			return FAIL;
		}
	}
	public DataRow upload(File src, String srcName, String title,File dst){
		DataRow fileRow = new DataRow();
		try{
			FileUtils.copyFile(src, dst);
			fileRow.put("TITLE", title);
			fileRow.put("SRC_NAME", srcName);
			fileRow.put("ROOT", request.getSession().getServletContext().getRealPath("/"));
			fileRow.put("PATH_ABS", dst.getAbsolutePath());
			fileRow.put("PATH_REL", dst.getAbsolutePath());
			service.save(getUploadTable(null), fileRow);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return fileRow;
	}
	/**
	 * 上传文件 并保存到数据库中
	 * upload uploadFileName uploadContentType
	 * @param dir 
	 * @returnn
	 */
	public List<DataRow> upload(File dir){
		List<DataRow> result = new ArrayList<DataRow>();
		if(null == upload){
			return result;
		}
		// 把得到的文件的集合通过循环的方式读取并放在指定的路径下
		for (int i = 0; i < upload.size(); i++) {
			try {
				String dstName = DateUtil.format("yyyyMMddhhmmssms")+BasicUtil.getRandomLowerString(10)+"."+FileUtil.getSuffixFileName(uploadFileName.get(i));
				File dst = new File(new File(dir, "upload_img"), dstName);
				FileUtils.copyFile(upload.get(i), dst);
				DataRow fileRow = new DataRow();
				fileRow.put("TITLE", dst.getName());
				fileRow.put("SRC_NAME", uploadFileName.get(i));
				fileRow.put("ROOT", request.getSession().getServletContext().getRealPath("/"));
				fileRow.put("PATH_ABS", dst.getAbsolutePath());
				fileRow.put("PATH_REL", dst.getAbsolutePath().replace(dir.getAbsolutePath(),""));
				service.save(getUploadTable(null), fileRow);
				result.add(fileRow);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public DataRow uploadFile(File file){
		return uploadFile(file, null);
	}
	public DataRow uploadFile(File file, String name){
		DataRow row = null;
		if(null == file || !file.exists()){
			return row;
		}
		if(BasicUtil.isEmpty(name)){
			name = file.getName();
		}
		String tarName = DateUtil.format("yyyyMMddhhmmssms")+BasicUtil.getRandomLowerString(10)+"."+FileUtil.getSuffixFileName(name);
		File dir = new File(ConfigTable.getString("UPLOAD_DIR"));
		String dateFormat = ConfigTable.getString("UPLOAD_DIR_DATE_FORMAT");
		if(BasicUtil.isNotEmpty(dateFormat)){
			dir = new File(dir, DateUtil.format(dateFormat));
		}
		if(dir.exists()){
			dir.mkdirs();
		}
		File tar = new File(dir, tarName);
		try {
			FileUtils.copyFile(file, tar);
			row = new DataRow();
			row.put("TITLE", name);
			row.put("SRC_NAME", name);
			row.put("ROOT", request.getSession().getServletContext().getRealPath("/"));
			row.put("PATH_ABS", tar.getAbsolutePath());
			row.put("PATH_REL", tar.getAbsolutePath().replace(dir.getAbsolutePath(),""));
			service.save(getUploadTable(null), row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return row;
	}
	public List<DataRow> upload(String dir) {
		File root = new File(ConfigTable.getWebRoot()).getParentFile();
		
		if(!BasicUtil.isEmpty(dir)){
			root = new File(root,dir);
		}
		// 如果指定的路径没有就创建
		if (!root.exists()) {
			root.mkdirs();
		}
		return upload(root);
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
				if(ConfigTable.isDebug()){
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

	
	public String getUploadTable(String cf){
		return getUploadTable(request, cf);
	}
	public String getUploadDir(String cf){
		return getUploadDir(request, cf);
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

	public List<File> getUpload() {
		return upload;
	}

	public void setUpload(List<File> upload) {
		this.upload = upload;
	}

	public List<String> getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(List<String> uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public List<String> getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(List<String> uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	
}