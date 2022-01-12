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
 
 
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * 分钟数转换成HH:mm
 * @author zh
 *
 */ 
public class MinuteFormat extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L;
	public int doEndTag() throws JspException { 
		try{ 
			String result = ""; 
			if(null == value){ 
				value = body; 
			}
			if(BasicUtil.isNotEmpty(value)){
				int minute = BasicUtil.parseInt(value, 0);
				result = DateUtil.convertMinute(minute);
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
		this.value = null;
		this.body = null;
	} 


	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
