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


import org.anyline.util.BasicUtil;
import org.anyline.web.util.WebUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;


public class Client extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private boolean out = true;
	public int doStartTag(){
			return EVAL_BODY_BUFFERED;
	}
	public int doEndTag() throws JspException {
		try{
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			String curType = WebUtil.clientType(request).toLowerCase();
			if(BasicUtil.isNotEmpty(var)){
				request.setAttribute(var,curType);
			}
			if(out){
				JspWriter out = pageContext.getOut();
				out.print(curType);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
		return EVAL_PAGE ;
	}
 
 
	@Override 
	public void release() { 
		super.release(); 
		var = null;
		out = true;
	} 
	 
	 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public boolean isOut() {
		return out;
	}

	public void setOut(boolean out) {
		this.out = out;
	}
}
