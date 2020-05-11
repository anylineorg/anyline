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


package org.anyline.web.tag; 
 
 
import javax.servlet.jsp.JspException;

import org.anyline.util.BasicUtil;
 
 
public class Split extends BaseBodyTag{ 
	private static final long serialVersionUID = 1554109844585627661L; 
	 
	private String regex; 
	private String var;
	private String scope = "page"; 
	private String text = null;
	 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    } 
	 public int doEndTag() throws JspException {
		if(BasicUtil.isEmpty(text)){ 
			text = body;
		} 
		if(null != text && null != var){ 
			try{ 
				String[] result = text.split(regex);
				if ("servelt".equals(scope) || "application".equalsIgnoreCase(scope)) {
					pageContext.getServletContext().setAttribute(var, result);
				} else if ("session".equals(scope)) {
					pageContext.getSession().setAttribute(var, result);
				}  else if ("request".equals(scope)) {
					pageContext.getRequest().setAttribute(var, result);
				}else if ("page".equals(scope)){
					pageContext.setAttribute(var, result);
				}
			}catch(Exception e){ 
	 
			}finally{ 
				release(); 
			} 
		} 
        return EVAL_PAGE;    
	} 
	@Override 
    public void release(){ 
		super.release(); 
		regex = null;
		var = null;
    	value = null;
    	body = null;
    	scope = "page";
    	text = null;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public String getVar() {
		return var;
	}
	public void setVar(String var) {
		this.var = var;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	} 
 
}
