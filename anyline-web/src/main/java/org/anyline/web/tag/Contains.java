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
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Contains extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private Object data;
	private String scope;
	private String property;
	private Object elseValue;
	private boolean skip = false;


	private boolean contains = false;
	public int doStartTag(){
		String valueProperty = property;
		String dataProperty = property;
		if(property != null && property.contains(":")){
			String[] tmps = property.split(":");
			dataProperty = tmps[0];
			valueProperty = tmps[1];
		}
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String html = "";
		try{
			if(null != data && null != value){
				if(null != valueProperty && ClassUtil.isWrapClass(value) && !(value instanceof String)){
					value = BeanUtil.getFieldValue(value, valueProperty);
				}
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
							if(value.toString().equals(item.toString())){
								contains = true;
								break;
							}
						}else{
							Object v = BeanUtil.getFieldValue(item, dataProperty)+"";
							if(value.toString().equals(v)){
								contains = true;
								break;
							}
						}

					}
				}else{
					Object v = BeanUtil.getFieldValue(data, dataProperty);
					if(value.equals(v)){
						contains = true;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
		if(skip && !contains){
			return SKIP_BODY;
		}else {
			return EVAL_BODY_BUFFERED;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int doEndTag() throws JspException {
		try{
			String html = "";
			if(contains){
				html = body;
			}else if(null != elseValue){
				html = elseValue.toString();
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
		}catch(Exception e){

		}
        return EVAL_PAGE;    
	}
	@Override
	public void release() {
		super.release();
		this.data = null;
		this.value = null;
		this.scope = null;
		this.elseValue = null;
		this.property = null;
		this.skip = false;
		contains = false;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
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

	public String getProperty() {
		return property;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void setProperty(String property) {
		this.property = property;
	}
}
