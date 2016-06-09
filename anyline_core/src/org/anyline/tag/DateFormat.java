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


import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.DateUtil;


public class DateFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(DateFormat.class);
	private String format;
	

	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


	public int doEndTag() throws JspException {
		try{
			String result = "";
			if(null == format){
				format = "yyyy-MM-dd";
			}
			if(null == value){
				value = body;
			}
			if(null == value){
				value = new Date();
				result = DateUtil.format(new Date(), format);
			}else if(value instanceof String){
				result = DateUtil.format((String)value,format);
			}else if(value instanceof Date){
				result = DateUtil.format((Date)value,format);
			}
			JspWriter out = pageContext.getOut();
			out.print(result);
		}catch(Exception e){
			LOG.error(e);
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