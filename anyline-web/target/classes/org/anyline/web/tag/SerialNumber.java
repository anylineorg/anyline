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
 
 
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class SerialNumber extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private Object data;		//分页数据 DataSete 或PageNavi
	private int index;	//当前页下标 varStatus.index 
 
 
	public int doEndTag() throws JspException { 
		try{ 
			String result = "";
			PageNavi navi = null;
			if(null != data){
				if(data instanceof DataSet){
					navi = ((DataSet)data).getNavi();
				}
				if(data instanceof PageNavi){
					navi = (PageNavi)data;
				}
			}
			if(null == navi){
				Object tmp = pageContext.getAttribute("navi");
				if(null != tmp && tmp instanceof PageNavi){
					navi = (PageNavi)tmp;
				}
			}
			if(null == navi){
				Object tmp = pageContext.getRequest().getAttribute("navi");
				if(null != tmp && tmp instanceof PageNavi){
					navi = (PageNavi)tmp;
				}
			}
			if(null != navi){
				index += navi.getPageRows() * (navi.getCurPage()-1);
			}
			index ++;
			result = index+""; 
			JspWriter out = pageContext.getOut(); 
			out.print(result); 
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
		this.index = 0;
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


	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
