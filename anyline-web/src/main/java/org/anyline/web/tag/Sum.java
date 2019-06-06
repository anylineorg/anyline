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

import java.math.BigDecimal;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BeanUtil;
import org.apache.log4j.Logger;

public class Sum extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Sum.class);
	private String scope;
	private Object data;
	private String key;

	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String html = "";
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
						} else {
							data = request.getAttribute(data.toString());
						}
					}
				}
				if(!(data instanceof Collection)){
					return EVAL_PAGE;
				}
				Collection items = (Collection) data;
				BigDecimal result = new BigDecimal(0);
				if (null != items){
					for (Object item : items) {
						if(null == item){
							continue;
						}
						Object val = null;
						if(item instanceof Number){
							val = item;
						}else{
							val = BeanUtil.getFieldValue(item, key);
						}
						if(null != val){
							result = result.add(new BigDecimal(val.toString()));
						}
					}
					html = result.toString();
				}
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
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


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	@Override
	public void release() {
		super.release();
		scope = null;
		data = null;
		key = null;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}