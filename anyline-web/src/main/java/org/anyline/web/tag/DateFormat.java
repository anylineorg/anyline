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
 
 
import java.util.Date;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class DateFormat extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private String format;
	private String lang;
	private int add;	//在原来基础上增加add单位
	private String part = "d"; // y M d h m s
	private String function; //对应DateUtil函数
	private Object def;		//默认值 value,body,nvl都未指定时取def

 
	public String getFormat() { 
		return format; 
	} 
 
 
	public void setFormat(String format) { 
		this.format = format; 
	} 
 
 
	public int doEndTag() throws JspException { 
		try{
			Date date = null;
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
			date = parse(value);
			if(null == date){
				if(BasicUtil.isNotEmpty(nvl)) {
					if ("true".equalsIgnoreCase(nvl.toString()) || (nvl instanceof Boolean && (Boolean)nvl)){
						date = new Date();
					}
				}
			}
			if(null == date){
				date = parse(def);
			}
			if(null != date) {
				if (add != 0) {
					if("y".equals(part)){
						date = DateUtil.addYear(date,add);
					}else if("d".equals(part)){
						date = DateUtil.addDay(date, add);
					}else if("M".equals(part)){
						date = DateUtil.addMonth(date,add);
					}else if("h".equalsIgnoreCase(part) || "hh".equals(part)){
						date = DateUtil.addHour(date,add);
					}
				}
			}
			if(null != date){
				result = DateUtil.format(local,date,format);
			}
			if(BasicUtil.isEmpty(result)){
				if(null !=nvl && !"false".equalsIgnoreCase(nvl.toString()) && !(nvl instanceof Boolean)){
					result = nvl.toString();
				}
			}
			if(null != result) {
				if(BasicUtil.isNotEmpty(var)){
					pageContext.getRequest().setAttribute(var, result);
				}else {
					JspWriter out = pageContext.getOut();
					out.print(result);
				}
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
		this.value = null;
		this.body = null;
		this.format = null;
		this.lang = null;
		this.nvl = false; //如果value为空("",null) 是否显示当前时间,默认false
	}
	private Date parse(Object value){
		Date date = null;
		if(BasicUtil.isEmpty(value)){
			return null;
		}
		if(value instanceof String){
			if(((String) value).contains(",") || BasicUtil.isNumber(value)){
				value = value.toString().replace(",", "");
				date = new Date(BasicUtil.parseLong(value, 0L));
			}else{
				date = DateUtil.parse((String)value);
			}
		}else if(value instanceof Date){
			date = (Date)value;
		}else if(value instanceof Long){
			date = new Date((Long)value);
		}
		return date;
	}

	public String getLang() {
		return lang;
	}


	public void setLang(String lang) {
		this.lang = lang;
	}

	public Integer getAdd() {
		return add;
	}

	public void setAdd(Integer add) {
		this.add = add;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public void setAdd(int add) {
		this.add = add;
	}

	public Object getDef() {
		return def;
	}

	public void setDef(Object def) {
		this.def = def;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
