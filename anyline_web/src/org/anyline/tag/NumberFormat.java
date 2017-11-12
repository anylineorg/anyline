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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.NumberUtil;
import org.apache.log4j.Logger;


public class NumberFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(NumberFormat.class);
	private String format;
	private String def;

	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


	public int doEndTag() throws JspException {
		try{
			String result = "";
			if(null == value){
				value = body;
			}
			if(value instanceof String){
				result = NumberUtil.format((String)value,format);
			}else if(value instanceof Number){
				result = NumberUtil.format((Number)value,format);
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
		value = null;
		format = null;
		body = null;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}