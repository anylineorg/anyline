/* 
 * Copyright 2006-2022 www.anyline.org
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
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Message extends BodyTagSupport{ 
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private String key; 
	private boolean clean = true;		//显示后清除 
 
	 public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest(); 
		DataSet messages = null; 
		String message = ""; 
		try{ 
			if("servelt".equals(scope) || "application".equalsIgnoreCase(scope)){ 
				messages = (DataSet)request.getSession().getServletContext().getAttribute(Constant.SERVLET_ATTR_MESSAGE); 
			}else if("session".equals(scope)){ 
				messages = (DataSet)request.getSession().getAttribute(Constant.SESSION_ATTR_MESSAGE); 
			}else{ 
				messages = (DataSet)request.getAttribute(Constant.REQUEST_ATTR_MESSAGE); 
			} 
			if(null != messages){ 
				if(BasicUtil.isNotEmpty(key)){ 
					DataRow row = messages.getRow(Constant.MESSAGE_KEY,key); 
					if(null != row){ 
						message = row.getString(Constant.MESSAGE_VALUE); 
						if(clean){ 
							messages.remove(row); 
						} 
					} 
				}else{ 
					for(int i=0; i<messages.size(); i++){ 
						message += messages.getString(i, Constant.MESSAGE_VALUE); 
						if(i >0) message += "<br/>"; 
					} 
				} 
	 
				JspWriter out = pageContext.getOut(); 
				out.print(message); 
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
		scope = null; 
		key = null; 
		clean = true; 
	} 
 
	public boolean isClean() { 
		return clean; 
	} 
 
	public void setClean(boolean clean) { 
		this.clean = clean; 
	} 
 
	public String getScope() { 
		return scope; 
	} 
 
	public void setScope(String scope) { 
		this.scope = scope; 
	} 
 
	public String getKey() { 
		return key; 
	} 
 
	public void setKey(String key) { 
		this.key = key; 
	} 
 
}
