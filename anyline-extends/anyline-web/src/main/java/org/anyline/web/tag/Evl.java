/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.web.tag;
 
 
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import java.util.ArrayList;
import java.util.Arrays;
 

/**
 * 第一个 !=null 并 != "" 的值
 */ 
public class Evl extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private String scope;
	 
	 public int doEndTag() throws JspException {
	 	if(BasicUtil.isNotEmpty(var)){
			 pageContext.getRequest().removeAttribute(var);
		 }
		 if(null == paramList || paramList.size()==0){
		 	value = BasicUtil.evl(value,body);
			 if(BasicUtil.isNotEmpty(value)){
				 String str = value.toString();
				 if(str.contains(",")){
					 paramList = BeanUtil.array2list(str.split(","));
				 }
			 }
		 }
		 if(null != paramList && paramList.size()>0){
			try{
				String result = "";
				for(Object param:paramList){
					if(null != param && !param.toString().equals("null") && !param.toString().trim().equals("")){

						result = param.toString();
						break; 
					} 
				}

				Object evl = BasicUtil.evl(result,body,"");
				if(null != evl){
					result = evl.toString();
				}
				if(BasicUtil.isEmpty(var)){
					JspWriter out = pageContext.getOut();
					out.print(result);
				}else{
					if(null == scope){
						pageContext.getRequest().setAttribute(var, result);
					}else if("parent".equalsIgnoreCase(scope)){
						Tag parent = this.getParent();
						BeanUtil.setFieldValue(parent, var, result);
					}else if("page".equalsIgnoreCase(scope)){
						pageContext.setAttribute(var, result);
					}else if("request".equalsIgnoreCase(scope)){
						pageContext.getRequest().setAttribute(var, result);
					}else if("session".equalsIgnoreCase(scope)){
						((HttpServletRequest)pageContext.getRequest()).getSession().setAttribute(var, result);
					}else if("servlet".equalsIgnoreCase(scope) || "application".equalsIgnoreCase(scope)){
						pageContext.getRequest().getServletContext().setAttribute(var, result);
					}
				}
			}catch(Exception e){
				e.printStackTrace(); 
			}finally{
				release(); 
			}
		 } 
        return EVAL_PAGE;    
	} 
 
 
	@Override 
	public void release() {
		super.release();
		paramList = null;
		value = null;
		scope = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	}
 	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
