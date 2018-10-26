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
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.apache.log4j.Logger;


public class DateFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DateFormat.class);
	private String format;
	private String lang;
	private Object nvl = false;	//如果value为空("",null) 是否显示当前时间,默认false

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
			Locale local = Locale.CHINA;
			if(BasicUtil.isNotEmpty(lang)){
				local = new Locale(lang);
			}
			if(BasicUtil.isEmpty(value) && BasicUtil.parseBoolean(nvl)){
				value = new Date();
				result = DateUtil.format(local,new Date(), format);
			}else if(value instanceof String){
				if(((String) value).contains(",")){
					value = value.toString().replace(",", "");
					result = DateUtil.format(local,BasicUtil.parseLong(value, 0L),format);
				}else{
					result = DateUtil.format(local,(String)value,format);
				}
			}else if(value instanceof Date){
				result = DateUtil.format(local,(Date)value,format);
			}else if(value instanceof Long){
				result = DateUtil.format(local,(Long)value,format);
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
		this.format = null;
		this.nvl = false;
		this.lang = null;
	}
	public Object getNvl() {
		return nvl;
	}


	public void setNvl(Object nvl) {
		this.nvl = nvl;
	}


	public String getLang() {
		return lang;
	}


	public void setLang(String lang) {
		this.lang = lang;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}