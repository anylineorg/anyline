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


package org.anyline.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BeanUtil;
import org.apache.log4j.Logger;

public class Contains extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Contains.class);
	private Object data;		
	private String key;
	private String scope;
	private Object elseValue;
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String html = "";
		try{
			boolean contains = false;
			if(null != data && null != value){
				if(data instanceof String){
					if(data.toString().endsWith("}")){
						data = data.toString().replace("{", "").replace("}", "");
					}else{
						if("servelt".equals(scope) || "application".equalsIgnoreCase(scope)){
							data = request.getSession().getServletContext().getAttribute(data.toString());
						}else if("session".equals(scope)){
							data = request.getSession().getAttribute(data.toString());
						}else{
							data = request.getAttribute(data.toString());
						}
					}
				}
				if(data instanceof String){
					String items[] = data.toString().split(",");
					List list = new ArrayList();
					for(String item:items){
						list.add(item);
					}
					data = list;
				}
				if(data instanceof Collection){
					Collection cons = (Collection)data;
					for(Object item:cons){
						if(null == item){
							continue;
						}else if(item instanceof String || item instanceof Number || item instanceof Boolean || item instanceof Date) {
							if(value.equals(item)){
								contains = true;
								break;
							}
						}else{
							Object v = BeanUtil.getFieldValue(item, key);
							if(value.equals(v)){
								contains = true;
								break;
							}
						}
						
					}
				}else{
					Object v = BeanUtil.getFieldValue(data, key);
					if(value.equals(v)){
						contains = true;
					}
				}
			}
			if(contains){
				html = body;
			}else if(null != elseValue){
				html = elseValue.toString();
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
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
		this.data = null;
		this.value = null;
		this.key = null;
		this.scope = null;
		this.elseValue = null;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public Object getElseValue() {
		return elseValue;
	}
	public void setElseValue(Object elseValue) {
		this.elseValue = elseValue;
	}
	public Object getElse() {
		return elseValue;
	}
	public void setElse(Object elseValue) {
		this.elseValue = elseValue;
	}
	
}