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


package org.anyline.web.tag; 
 
/**
 * 判断当前type是否包含在指定的type中
 */ 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.web.util.WebUtil;
 
 
public class CheckClient extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private String type = "";
	private Object elseValue; 
	public int doEndTag() throws JspException { 
		try{
			type = (type+"").toLowerCase();
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String curType = WebUtil.clientType(request).toLowerCase();
			if(type.contains(curType)){
				JspWriter out = pageContext.getOut(); 
				out.print(BasicUtil.nvl(value,body,"")); 
			}else if(null != elseValue){
				JspWriter out = pageContext.getOut();
				out.print(elseValue);
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
		value = null;
		body = null; 
		type = null;
		value = null;
		elseValue = null; 
	} 
	 
	 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	}

	public void setElse(Object elseValue) {
		this.elseValue = elseValue;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	 
}
