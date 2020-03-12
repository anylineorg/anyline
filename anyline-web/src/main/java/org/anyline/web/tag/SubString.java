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
 
 
import org.anyline.util.BasicUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
 
 
public class SubString extends BaseBodyTag{ 
	private static final long serialVersionUID = 1554109844585627661L; 
	 
	private Integer begin = null;
	private Integer qty = null;
	private Integer end = null; //负数表示取最后end个
	 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    } 
	 public int doEndTag() throws JspException { 
		//输出 
		JspWriter out = pageContext.getOut(); 
		String text = body; 
		if(null != text){
			int range[] = BasicUtil.range(begin, end, qty, text.length());
			text = text.substring(begin,end); 
			try{ 
				out.print(text); 
			}catch(Exception e){ 
	 
			}finally{ 
				release(); 
			} 
		} 
        return EVAL_PAGE;    
	} 
	@Override 
    public void release(){ 
		super.release(); 
    	begin = null;
    	end = null;
    	qty = null;
    	value = null;
    	body = null;
    			 
    }

	public Integer getBegin() {
		return begin;
	}

	public void setBegin(Integer begin) {
		this.begin = begin;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}
}
