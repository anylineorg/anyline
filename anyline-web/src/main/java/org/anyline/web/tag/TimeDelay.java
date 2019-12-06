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
 
 
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * 从value|body起到现在经过多少时间
 * @author zh
 *
 */ 
public class TimeDelay extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(TimeDelay.class); 
	private Object nvl = false;	//如果value为空("",null) 是否显示当时间,默认false 
 
	public int doEndTag() throws JspException { 
		try{ 
			String result = ""; 
			if(null == value){ 
				value = body; 
			} 
			if(BasicUtil.isEmpty(value) && BasicUtil.parseBoolean(nvl)){ 
				value = new Date(); 
			}else if(value instanceof String){ 
				value = DateUtil.parse((String)value); 
			}
			Date date = (Date)value;
			long fr = date.getTime()/1000;
			long to = new Date().getTime()/1000;
			long dif = to - fr;
			if(dif < 60){
				result = dif + "秒";
			}else if(dif < 60 * 60){
				result = dif/60 + "分钟";
			}else if(dif <60*60*24){
				result = dif/60/60 + "小时";
			}else if(dif <60*60*24*7){
				result = dif/60/60/24 + "天";
			}else if(dif <60*60*24*30){
				result = dif/60/60/24/7 + "周";
			}else if(dif <60*60*24*365){
				result = dif/60/60/24/30 + "月";
			}else{
				result = dif/60/60/24/365 + "年";
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
		this.nvl = true; 
	} 
	public Object getNvl() {
		return nvl;
	}


	public void setNvl(Object nvl) {
		this.nvl = nvl;
	}


	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
