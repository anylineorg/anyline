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
 */


package org.anyline.tag;


import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.I18NUtil;


public class Text extends BodyTagSupport{
	private static final long serialVersionUID = 1554109844585627661L;
	
	private Object data;
	private String lang;			//语言
	private String key;
	private HttpServletRequest request; 
	
	public int doStartTag() throws JspException {
		request = (HttpServletRequest)pageContext.getRequest();
		checkLang();
        return EVAL_BODY_BUFFERED;
    }
	 public int doEndTag() throws JspException {
		 String text = "";
		 if(BasicUtil.isNotEmpty(data)){
			 if(data instanceof DataRow){
				 text = ((DataRow)data).getString(key);
			 }else if(data instanceof Map){
				 Object val = ((Map)data).get(key);
				 if(null != val){
					 text = val.toString();
				 }
			 }else{
				 Object val = BeanUtil.getFieldValue(data, key);
				 if(null != val){
					 text = val.toString();
				 }
			 }
		 }else{
			 text = I18NUtil.get(lang, key);
		 }
		//输出
		JspWriter out = pageContext.getOut();
		try{
			out.print(text);
		}catch(Exception e){

		}finally{
			release();
		}
        return EVAL_PAGE;   
	}
	@Override
    public void release(){
		super.release();
    	key = null;
    	lang = null;
    }
	/**
	 * 确认语言环境
	 */
	private void checkLang(){
		if(BasicUtil.isEmpty(lang)){
			lang = (String)request.getSession().getAttribute(ConfigTable.getString("I18N_MESSAGE_SESSION_KEY"));
		}
		if(BasicUtil.isEmpty(lang)){
			//配置文件默认
			lang = ConfigTable.getString("I18N_MESSAGE_DEFAULT_LANG");
		}
		if(BasicUtil.isEmpty(lang)){
			//struts
			lang = (String)request.getSession().getAttribute("WW_TRANS_I18N_LOCALE");
		}
		if(BasicUtil.isEmpty(lang)){
			//Local
			lang = Locale.getDefault().getCountry().toLowerCase();
		}
		if(BasicUtil.isEmpty(lang)){
			lang = I18NUtil.defaultLang;
		}
	}

	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

}