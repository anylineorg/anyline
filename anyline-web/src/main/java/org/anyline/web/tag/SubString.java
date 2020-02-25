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
 
 
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
 
 
public class SubString extends BaseBodyTag{ 
	private static final long serialVersionUID = 1554109844585627661L; 
	 
	private int begin = -1; 
	private int end = -1; 
	 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    } 
	 public int doEndTag() throws JspException { 
		//输出 
		JspWriter out = pageContext.getOut(); 
		String text = body; 
		if(null != text){ 
			if(begin < 0){ 
				begin = 0; 
			} 
			if(end > text.length() || end < 0){ 
				end = text.length(); 
			} 
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
    	begin = -1; 
    	end = -1;
    	value = null;
    	body = null;
    			 
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
	 
 
}
