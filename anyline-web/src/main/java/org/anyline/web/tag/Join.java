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
 
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import java.util.*;


public class Join extends BaseBodyTag{
	private static final long serialVersionUID = 1554109844585627661L;
	private String scope;
	private Object data;
	private String selector;
	private String property;
	private Integer index;
	private Integer begin;
	private Integer end;
	private Integer qty;
	private String split = ",";
	 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    }
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
						 data = BeanUtil.cuts(items, range[0], range[1]);
					 }
				 }
				 String html ="";
				 Collection list = (Collection)data;
				 for(Object item:list){
					 String val = BeanUtil.parseRuntimeValue(item, property, encrypt);
					 if(BasicUtil.isNotEmpty(val)){
					 	if("".equals(html)){
					 		html = val;
						}else{
					 		html = html + split + val;
						}
					 }
				 }
				 JspWriter out = pageContext.getOut();
				 out.print(html);
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 } finally {
			 release();
		 }
		 return EVAL_PAGE;
	} 
	@Override 
    public void release(){ 
		super.release();
		data = null;
		property = null;
		index = null;
		begin = null;
		end = null;
		qty = null;
		selector = null;
		property = null;
		split = ",";
    }

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getScope() {
		return scope;
	}

	public Object getData() {
		return data;
	}

	public String getSelector() {
		return selector;
	}

	public String getProperty() {
		return property;
	}

	public Integer getIndex() {
		return index;
	}

	public Integer getBegin() {
		return begin;
	}

	public Integer getEnd() {
		return end;
	}

	public Integer getQty() {
		return qty;
	}

	public String getSplit() {
		return split;
	}
}
