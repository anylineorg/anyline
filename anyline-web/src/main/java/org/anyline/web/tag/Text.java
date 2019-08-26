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
	
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	 public int doEndTag() throws JspException {
		//输出
		JspWriter out = pageContext.getOut();
		try{
			Object result ="";
			if(data instanceof DataSet){
				DataSet set = (DataSet)data;
				if(index > -1 && index<set.size()){
					DataRow row = set.getRow(index);
					result = row.get(property);
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
			if(BasicUtil.isNotEmpty(result)){
				out.print(result);
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

}