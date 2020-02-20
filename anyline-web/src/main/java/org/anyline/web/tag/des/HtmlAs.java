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


package org.anyline.web.tag.des; 
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.DESUtil;
import org.anyline.web.tag.BaseBodyTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** 
 * 整体加密 
 * @author zh 
 * 
 */ 
public class HtmlAs extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
 
	public int doEndTag() throws JspException { 
		try{ 
			String value = body; 
			if(null != value && !"".equals(value.trim())){ 
				value = value.trim(); 
				JspWriter out = pageContext.getOut(); 
				out.print(DESUtil.encryptHtmlTagA(value)); 
			} 
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
		body = null; 
	} 
 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
} 
