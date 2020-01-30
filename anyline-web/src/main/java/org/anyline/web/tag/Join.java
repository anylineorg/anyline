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
	private int index = -1;
	private int begin = -1;
	private int end = -1;
	private int qty = -1;
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
						 data = BeanUtil.select(items,selector.split(","));
					 }
					 if(index !=-1){
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
						 if(begin != -1){
							 if(qty != -1){
								 end = begin + qty;
							 }
							 data = BeanUtil.cuts((Collection)data, begin, end);
						 }
					 }
				 }
				 String html ="";
				 Collection list = (Collection)data;
				 for(Object item:list){
					 String val = parseRuntimeValue(item, property, encrypt);
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
		index = -1;
		begin = -1;
		end = -1;
		qty = -1;
		selector = null;
		property = null;
		split = ",";
    }
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getSelector() {
		return selector;
	}
	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}
 
}
