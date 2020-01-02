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
 
 
import java.math.BigDecimal;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class NumberFormat extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private String format;
	private Object min;
	private Object max; 
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
			if(BasicUtil.isEmpty(value)){
				value = def;
			}
			if(BasicUtil.isNotEmpty(value)){
				BigDecimal num = new BigDecimal(value.toString());
				if(BasicUtil.isNotEmpty(min)){
					BigDecimal minNum = new BigDecimal(min.toString());
					if(minNum.compareTo(num) > 0){
						num = minNum;
						log.warn("[number format][超过最小值:{}]", min);
					}
				}
				if(BasicUtil.isNotEmpty(max)){
					BigDecimal maxNum = new BigDecimal(max.toString());
					if(maxNum.compareTo(num) < 0){
						num = maxNum;
						log.warn("[number format][超过最大值:{}]", max);
					}
				} 
				result = NumberUtil.format(num,format); 
				JspWriter out = pageContext.getOut(); 
				out.print(result);
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
		value = null;
		format = null;
		body = null;
		def = null;
		min = null;
		max = null; 
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
