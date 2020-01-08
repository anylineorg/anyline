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
 
 
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
 
 
public class Text extends BaseBodyTag{ 
	private static final long serialVersionUID = 1554109844585627661L; 
	 
	private Object data;
	private int index = -1; 
	private String property;
	private String selector;
	private String nvl = null;
	private String evl = null; 
	 
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
				if(index > -1 && index<set.size()){
					DataRow row = set.getRow(index);
					result = row.get(property);
				}else if(BasicUtil.isNotEmpty(selector)){
					DataRow row = set.getRow(selector.split(","));
					if(null != row){
						result = row.get(property);
					}
				}
			}else if(data instanceof List){
				@SuppressWarnings("rawtypes")
				List list = (List)data;
				if(index > -1 && index<list.size()){
					result = BeanUtil.getValueByColumn(list.get(index), property);
				}
			}else{
				result = BeanUtil.getValueByColumn(data,property);
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
			
			out.print(result);
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
