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
 
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Set extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data; 
	private String selector; 
	private String var; 
	private Integer index = null;
	private Integer begin = null;
	private Integer end = null;
	private Integer qty = null;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest(); 
		try { 
			if (null != data) { 
				if (data instanceof String) { 
					if (data.toString().endsWith("}")) { 
						data = data.toString().replace("{", "").replace("}", ""); 
					} else { 
						if ("servelt".equals(scope) || "application".equalsIgnoreCase(scope)) { 
							data = request.getSession().getServletContext().getAttribute(data.toString()); 
						} else if ("session".equals(scope)) { 
							data = request.getSession().getAttribute(data.toString()); 
						}  else if ("request".equals(scope)) { 
							data = request.getAttribute(data.toString()); 
						}else if ("page".equals(scope)){ 
							data = pageContext.getAttribute(data.toString()); 
						} 
					} 
				} 
				if(data instanceof Collection){ 
					Collection items = (Collection) data; 
					if(BasicUtil.isNotEmpty(selector)){
						items = BeanUtil.select(items,selector.split(","));
					} 
					if(index != null){
						int i = 0; 
						data = null; 
						for(Object item:items){ 
							if(index ==i){ 
								data = item; 
								break; 
							} 
							i ++; 
						} 
					}else{
						int[] range = BasicUtil.range(begin, end, qty, items.size());
						if(items instanceof DataSet){
							data = ((DataSet) items).cut(range[0], range[1]);
						}else {
							data = BeanUtil.cuts(items, range[0], range[1]);
						}
					} 
				} 
				 
				if ("servelt".equals(scope) || "application".equalsIgnoreCase(scope)) { 
					request.getSession().getServletContext().setAttribute(var,data); 
				} else if ("session".equals(scope)) { 
					request.getSession().setAttribute(var,data); 
				}  else if ("request".equals(scope)) { 
					request.setAttribute(var,data); 
				}  else if ("parent".equals(scope)) { 
					Tag parent = this.getParent(); 
					if(null != parent){ 
						BeanUtil.setFieldValue(parent, var, data); 
					} 
				}else { 
					pageContext.setAttribute(var,data); 
				} 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			release(); 
		} 
		return EVAL_PAGE; 
	} 
 
 
	public Object getData() { 
		return data; 
	} 
 
 
	public void setData(Object data) { 
		this.data = data; 
	} 
 
 
 
 
	@Override 
	public void release() { 
		super.release(); 
		scope = null; 
		data = null; 
		var = null; 
		selector = null; 
		index = null;
		begin = null;
		end = null;
		qty = null;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getBegin() {
		return begin;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}
}
