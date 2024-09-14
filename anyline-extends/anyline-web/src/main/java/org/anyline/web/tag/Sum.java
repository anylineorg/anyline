/*
 * Copyright 2006-2023 www.anyline.org
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
 
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.NumberUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.math.BigDecimal;
import java.util.Collection;
 
public class Sum extends BaseBodyTag {
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data;
	private String selector; 
	private String property; 
	private String format;
	private String nvl;
	private Object min;
	private Object max;
	private String def; // 默认值
	private Integer scale;//小数位
	private Integer round; // 参考BigDecimal.ROUND_UP;
	private String hide; // 隐藏span.class
	private String echo; // 显示位置 span.class
 
	@SuppressWarnings("rawtypes")
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest(); 
		String html = "";
		if(null != var) {
			pageContext.removeAttribute(var);
		}
		try {
			JspWriter out = pageContext.getOut();
			if (null != data) {
				if (data instanceof String) {
					if (data.toString().endsWith("}")) {
						data = data.toString().replace("{", "").replace("}", "");
					} else {
						if ("servlet".equals(scope) || "application".equalsIgnoreCase(scope)) {
							data = request.getSession().getServletContext().getAttribute(data.toString()); 
						} else if ("session".equals(scope)) {
							data = request.getSession().getAttribute(data.toString()); 
						} else {
							data = request.getAttribute(data.toString()); 
						} 
					} 
				} 
				if(!(data instanceof Collection)) {
					return EVAL_PAGE;
				}
				Collection items = (Collection) data;
				if(BasicUtil.isNotEmpty(selector) && data instanceof DataSet) {
					items = BeanUtil.select(items,selector.split(","));
				}

				BigDecimal sum = new BigDecimal(0);
				if (null != items) {
					for (Object item : items) {
						if(null == item) {
							continue;
						}
						Object val = null;
						if(item instanceof Number) {
							val = item;
						}else{
							val = BeanUtil.getFieldValue(item, property);
						}
						if(null != val) {
							sum = sum.add(new BigDecimal(val.toString()));
						}
					}

					if(BasicUtil.isNotEmpty(min)) {
						BigDecimal minNum = new BigDecimal(min.toString());
						if(minNum.compareTo(sum) > 0) {
							log.warn("[number sum][value:{}][小于最小值:{}]", sum,min);
							sum = minNum;
						}
					}
					if(BasicUtil.isNotEmpty(max)) {
						BigDecimal maxNum = new BigDecimal(max.toString());
						if(maxNum.compareTo(sum) < 0) {
							log.warn("[number sum][value:{}][超过最大值:{}]",sum, max);
							sum = maxNum;
						}
					}
					if(null != scale) {
						if(null != round) {
							sum = sum.setScale(scale, round);
						}else {
							sum = sum.setScale(scale);
						}
					}
					if(BasicUtil.isNotEmpty(format)) {
						html = NumberUtil.format(sum,format);
					}else{
						html = sum.toString();
					}

					if(BasicUtil.isNotEmpty(hide)) {
						out.print("<span class='"+hide+"' style='display:none;'>"+sum+"</span>");
					}
				}

			}
			if(BasicUtil.isEmpty(html) && BasicUtil.isNotEmpty(nvl)) {
				html = nvl;
			}
			if(null == var) {
				out.print(html);
			}else{
				pageContext.setAttribute(var, html);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		} finally {
			release(); 
		} 
		return EVAL_PAGE; 
	} 
 
 
	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}	public String getProperty() {
		return property;
	}


	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public String getNvl() {
		return nvl;
	}

	public void setNvl(String nvl) {
		this.nvl = nvl;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	@Override
	public void release() {
		super.release(); 
		scope = null; 
		data = null;
		nvl = null;
		property = null;
		selector = null; 
		format = null;
		var = null;
		value = null;
		body = null;
		def = null;
		min = null;
		max = null;
		evl = null;
		scale = null;
		round = null;
		hide = null;
	} 
 
	public String getScope() {
		return scope; 
	} 
 
	public void setScope(String scope) {
		this.scope = scope; 
	}


	public String getSelector() {
		return selector;
	}


	public void setSelector(String selector) {
		this.selector = selector;
	}


	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
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

	public String getEcho() {
		return echo;
	}

	public void setEcho(String echo) {
		this.echo = echo;
	}
}
