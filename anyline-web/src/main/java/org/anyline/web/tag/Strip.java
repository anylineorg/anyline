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

import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** 
 * 清除html标签 
 * @author zh 
 * 
 */ 
 
public class Strip extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(Strip.class); 
 
	private int length = -1; 
	private String ellipsis="..."; 
	private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789,.?'_-=+!@#$%^&*() "; 
 
	public int doEndTag() throws JspException { 
		try{ 
			String result = ""; 
			if(null != value){ 
				result = value.toString(); 
			}else if(null != body){ 
				result = body; 
			} 
			result = RegularUtil.removeAllHtmlTag(result); 
			if(length != -1){ 
				int size = length * 2; 
				String chrs[] = result.split(""); 
				int cnt = 0; 
				String tmp = result; 
				result = ""; 
				for(String chr:chrs){ 
					if(cnt >= size){ 
						break; 
					} 
					if(SINGLE_CHAR.contains(chr.toLowerCase())){ 
						cnt += 1; 
					}else{ 
						cnt += 2; 
					} 
					result += chr; 
				} 
				if(result.length() < tmp.length()){ 
					result += ellipsis; 
				} 
			} 
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
		length = -1;
		value = null;
		body = null;
		ellipsis="..."; 
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
  
 
	public int getLength() { 
		return length; 
	} 
 
 
	public void setLength(int length) { 
		this.length = length; 
	} 
 
 
	public String getEllipsis() { 
		return ellipsis; 
	} 
 
 
	public void setEllipsis(String ellipsis) { 
		this.ellipsis = ellipsis; 
	} 
 
}
