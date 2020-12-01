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
 
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.List;
 
 
public class Text extends BaseBodyTag{ 
	private static final long serialVersionUID = 1554109844585627661L; 
	 
	private Object data;
	private int index = -1; 
	private String property;
	private String selector;
	private String nvl = null;
	private String evl = null;
	private String var = null;
	 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    } 
	 public int doEndTag() throws JspException {
		//输出 
		JspWriter out = pageContext.getOut();
		try{
			Object result = null; 
			if(data instanceof DataSet){
				DataSet set = (DataSet)data;
				DataRow row = null;
				if(BasicUtil.isNotEmpty(selector)){
					set = set.getRows(selector.split(","));
				}
				if(index == -1){
					index = 0;
				}
				if(index<set.size()){
					row = set.getRow(index);
				}
				if(null != row){
					result = row.getString(property);
				}
			}else if(data instanceof List){
				@SuppressWarnings("rawtypes")
				List list = (List)data;
				Object item = null;
				if(index == -1){
					index = 0;
				}
				if(index<list.size()){
					item = list.get(index);
				}
				if(null != item) {
					result = BeanUtil.getValueByColumn(item, property);
				}
			}else if(null != property){
				result = BeanUtil.getValueByColumn(data,property);
			}else{
				result = data;
			}
			if(null == result){
				result = body;
			}
			if(null != nvl){
				result = BasicUtil.nvl(result,nvl); 
			}

			if(null != evl){
				result = BasicUtil.evl(result,evl); 
			}
			if(BasicUtil.isNotEmpty(result)) {
				if(null != var){
					pageContext.setAttribute(var, result);
				}else {
					out.print(result);
				}
			}
		}catch(Exception e){ 
		 
		}finally{ 
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
		selector = null;
		nvl = null;
		evl = null;
		var = null;
    }

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
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
	public String getNvl() {
		return nvl;
	}
	public void setNvl(String nvl) {
		this.nvl = nvl;
	}
	public String getEvl() {
		return evl;
	}
	public void setEvl(String evl) {
		this.evl = evl;
	} 
 
}
