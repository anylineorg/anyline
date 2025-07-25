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
import java.math.BigDecimal;
 
 
public class NumberFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L; 
	private String format;
	private Object min;
	private Object max; 
	private String def; // 默认值
	private Integer scale;//小数位
	private Integer round; // 参考BigDecimal.ROUND_UP;
	private String hide; // 隐藏span.class
	private String echo; // 显示位置 span.class
	private Boolean factor = null;//正数时 是否带正负号

 
 
	public int doEndTag() throws JspException {
		try{
			if(BasicUtil.isNotEmpty(var)) {
				pageContext.getRequest().removeAttribute(var);
			}
			String result = null;
			if(null == value) {
				value = body;
			}
			if(BasicUtil.isEmpty(value)) {
				value = def;
			}
			BigDecimal num = null;
			if(BasicUtil.isNotEmpty(value)) {
				num = new BigDecimal(value.toString());
				if(BasicUtil.isNotEmpty(min)) {
					BigDecimal minNum = new BigDecimal(min.toString());
					if(minNum.compareTo(num) > 0) {
						log.warn("[number format][value:{}][小于最小值:{}]", num,min);
						num = minNum;
					}
				}
				if(BasicUtil.isNotEmpty(max)) {
					BigDecimal maxNum = new BigDecimal(max.toString());
					if(maxNum.compareTo(num) < 0) {
						log.warn("[number format][value:{}][超过最大值:{}]",num, max);
						num = maxNum;
					}
				}
				if(null != scale) {
					if(null != round) {
						num = num.setScale(scale, round);
					}else {
						num = num.setScale(scale);
					}
				}
				if(BasicUtil.isNotEmpty(format)) {
					result = NumberUtil.format(num, format);
				}else{
					result = num.toString();
				}
				if(num.compareTo(BigDecimal.ZERO) > 0) {
					if(null != factor && factor && !result.startsWith("+")) {
						result = "+" + result;
					}
				}
			}else{
				if(null == result && null != nvl) {
					result = nvl.toString();
				}
				if(BasicUtil.isEmpty(result) && null != evl) {
					result = evl.toString();
				}
			}

			if(null != result) {
				if(BasicUtil.isNotEmpty(var)) {
					pageContext.getRequest().setAttribute(var, result);
				}else {
					JspWriter out = pageContext.getOut();
					if(BasicUtil.isNotEmpty(echo) && null != num) {
						out.print("<span class='" + echo+"'>"+result+"</span>");
					}else {
						out.print(result);
					}
					if(BasicUtil.isNotEmpty(hide) && null != num) {
						out.print("<span class='"+hide+"' style='display:none;'>"+num+"</span>");
					}
				}
			}
		}catch(Exception e) {
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
		evl = null;
		scale = null;
		round = null;
		hide = null;
		factor = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	}

	public Boolean getFactor() {
		return factor;
	}

	public void setFactor(Boolean factor) {
		this.factor = factor;
	}

	public String getEcho() {
		return echo;
	}

	public void setEcho(String echo) {
		this.echo = echo;
	}

	public Object getMin() {
		return min;
	}

	public void setMin(Object min) {
		this.min = min;
	}

	public Object getMax() {
		return max;
	}

	public void setMax(Object max) {
		this.max = max;
	}

	public String getDef() {
		return def;
	}

	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	public String getHide() {
		return hide;
	}

	public void setHide(String hide) {
		this.hide = hide;
	}
}
