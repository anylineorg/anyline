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


package org.anyline.web.tag.seo;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.web.tag.BaseBodyTag;
import org.apache.log4j.Logger;

/**
 * 
 * @author Administrator
 * 头信息
 * pagecontext > attribute > param
 */
public class Head extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Head.class);

	private String[] vars;		
	private String title;
	private String keywords;
	private String description;
	private String var;		//变量 以,分开 titte keywords description中以{0}{1}引用
	
	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	public int doEndTag() throws JspException {
		try{			
			String _title = (String)pageContext.getAttribute("title");
			if(_title == null || _title.trim().equals("")){
				_title = title;
			}
			if(_title == null || _title.trim().equals("")){
				_title = getParamValue("title");
			}
			
			String _keywords = (String)pageContext.getAttribute("keywords");
			if(_keywords == null || _keywords.trim().equals("")){
				_keywords = keywords;
			}
			if(_keywords == null || _keywords.trim().equals("")){
				_keywords = getParamValue("keywords");
			}
			
			String _description = (String)pageContext.getAttribute("description");
			if(_description == null || _description.trim().equals("")){
				_description = description;
			}
			if(_description == null || _description.trim().equals("")){
				_description = getParamValue("description");
			}
			

			String _var = (String)pageContext.getAttribute("var");
			if(_var == null || _var.trim().equals("")){
				_var = var;
			}
			if(_var == null || _var.trim().equals("")){
				_var = getParamValue("var");
			}
			if(null != _var && !_var.equals("")){
				vars = _var.split(",");
			}
			
			if(null != vars){
				int len = vars.length;
				for(int i=0; i<len; i++){
					if(null != _title && !_title.equals("")){
						_title = _title.trim().replace("{"+i+"}", vars[i]);					
					}
					if(null != _keywords && !_keywords.equals("")){
						_keywords = _keywords.trim().replace("{"+i+"}", vars[i]);					
					}
					if(null != _description && !_description.equals("")){
						_description = _description.trim().replace("{"+i+"}", vars[i]);					
					}
				}
			}
			
			JspWriter out = pageContext.getOut();
			if(_title != null && !_title.trim().equals("")){
				out.println("\t<title>" + _title + "</title>");
			}
			if(_keywords != null && !_keywords.trim().equals("")){
				out.println("\t<meta name=\"keywords\" content=\"" + _keywords + "\" />");
			}
			if(_description != null && !_description.trim().equals("")){
				out.println("\t<meta name=\"description\" content=\"" + _description + "\" />");
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}

	@Override
	public void release() {
		super.release();
		vars = null;	
		title = null;
		keywords = null;
		description = null;
		var = null;
		
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private String getParamValue(String key){
		if(paramMap != null){
			return (String)paramMap.get(key);
		}
		return null;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getVar() {
		return var;
	}
	public void setVar(String var) {
		this.var = var;
	}
	
}