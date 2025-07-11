/*
 * Copyright 2006-2025 www.anyline.org
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
 */

package org.anyline.web.tag;

import org.anyline.util.BasicUtil;
import org.anyline.util.NumberUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.math.BigDecimal;

/**
 * 除法运算 主要处理0被除数异常 及格式化 
 * @author zh 
 * 
 */ 
public class Divide extends BodyTagSupport{
	private static final long serialVersionUID = 1L; 
	private String divisor;		// 除数 
	private String dividend;	// 被除数 
	private String format; 
	private String defaultValue = "";
	private Integer scale = 10;
	private int round = 0;

	 public int doStartTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut(); 
			BigDecimal _divisor = BasicUtil.parseDecimal(divisor, 0); 
			BigDecimal _dividend = BasicUtil.parseDecimal(dividend, 0);
			if(null == scale) {
				if (null != format) {
					if(format.indexOf(".") >=0) {
						scale = format.length() - format.indexOf(".")-1;
					}else{
						scale = 1;
					}
				}else{
					scale = 10;
				}
			}
			if(_divisor.compareTo(BigDecimal.ZERO) != 0) {
				BigDecimal result = _dividend.divide(_divisor,scale, round);
				if(null != format) {
					defaultValue = NumberUtil.format(result, format); 
				}else{
					defaultValue = result +""; 
				} 
			} 
			out.print(defaultValue); 
		}catch(Exception e) {
			e.printStackTrace(); 
		}finally{
			release(); 
		} 
        return EVAL_BODY_INCLUDE; 
    }    
	public int doEndTag() throws JspException {   
	 	return EVAL_PAGE;
	} 
	@Override 
	public void release() {
		super.release(); 
		defaultValue = ""; 
		divisor = null; 
		dividend = null; 
		format = null;
		scale = null;
		round = 0;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public String getDivisor() {
		return divisor; 
	} 
 
	public void setDivisor(String divisor) {
		this.divisor = divisor; 
	} 
 
	public String getDividend() {
		return dividend; 
	} 
 
	public void setDividend(String dividend) {
		this.dividend = dividend; 
	} 
 
	public String getDefault() {
		return defaultValue; 
	} 
 
	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue; 
	} 
 
	public String getFormat() {
		return format; 
	} 
 
	public void setFormat(String format) {
		this.format = format; 
	} 
} 
