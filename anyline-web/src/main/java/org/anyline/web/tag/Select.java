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
 
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.*;
 
public class Select extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data; 
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID"); 
	private String textKey = "NM"; 
	private String head; 
	private String headValue; 
	private String type = "select"; // 如果type=text则只显示选中项的text而不生成<select> 
	private String multiple = null;
	private int size = 1;
 
	public String getHead() { 
		return head; 
	} 
 
	public void setHead(String head) { 
		this.head = head; 
	} 
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest(); 
//		valueKey = DataRow.keyCase(valueKey); 
//		textKey = DataRow.keyCase(textKey); 
		String html = ""; 
 
		if (data instanceof String) { 
			if (data.toString().endsWith("}")) { 
				data = data.toString().replace("{", "").replace("}", ""); 
			} else { 
				if ("servelt".equals(scope) || "application".equalsIgnoreCase(scope)) { 
					data = request.getSession().getServletContext().getAttribute(data.toString()); 
				} else if ("session".equals(scope)) { 
					data = request.getSession().getAttribute(data.toString()); 
				} else { 
					data = request.getAttribute(data.toString()); 
				} 
			} 
		} 
		if (data instanceof String) { 
			String items[] = data.toString().split(","); 
			List list = new ArrayList(); 
			for (String item : items) { 
				Map map = new HashMap(); 
				String ks[] = BeanUtil.parseKeyValue(item); 
				map.put(valueKey, ks[0]); 
				map.put(textKey, ks[1]); 
				if(ks.length>2){ 
					map.put("CHK", ks[2]); 
				} 
				list.add(map); 
			} 
			data = list; 
		} 
		Collection items = (Collection) data; 
		try { 
			if ("text".equals(type)) { 
				if (null != items) { 
					for (Object item : items) { 
						String val = BeanUtil.parseRuntimeValue(item, valueKey, encrypt);
						String text = BeanUtil.parseRuntimeValue(item, textKey);
						if (null != val && null != this.value && val.equals(value.toString())) { 
							html = text; 
						} 
					} 
				} 
			} else {
				StringBuffer builder = new StringBuffer();
				builder.append("<select ");
				attribute(builder);
				if(BasicUtil.isNotEmpty(multiple)){
					builder.append(" multiple=\"multiple\"");
				}
				if(size>1){
					builder.append(" size=\""+size+"\"");
				}
				builder.append(">");
				if (null == headValue) { 
					headValue = ""; 
				} 
				if (null != head) {
					builder.append("<option value=\"" + headValue + "\">" + head + "</option>");
				}
				if (null != body) {
					builder.append(body);
				}
				if (null != items) { 
					for (Object item : items) { 
						String val = BeanUtil.parseRuntimeValue(item, valueKey, encrypt);
						String text = BeanUtil.parseRuntimeValue(item, textKey);
						builder.append("<option value=\"").append(val).append("\"");
						if (null != val && null != this.value && val.equals(value.toString())) {
							builder.append(" selected=\"selected\"");
						}
						crateExtraData(builder, item);
						builder.append(">").append(text).append("</option>");
					} 
				}
				builder.append("</select>");
				html = builder.toString();
			} 
			JspWriter out = pageContext.getOut(); 
			out.print(html); 
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
	} 
 
	public String getValueKey() { 
		return valueKey; 
	} 
 
	public void setValueKey(String valueKey) { 
		this.valueKey = valueKey; 
	} 
 
	public String getTextKey() { 
		return textKey; 
	} 
 
	public void setTextKey(String textKey) { 
		this.textKey = textKey; 
	} 
 
	@Override 
	public void release() { 
		super.release(); 
		scope = null; 
		data = null; 
		scope = null; 
		head = null;
		size = 1;
		headValue = null; 
		type = "select"; 
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID"); 
		textKey = "NM"; 
		multiple = null; 
 
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type; 
	} 
 
	public void setType(String type) { 
		this.type = type; 
	} 
 
	public String getHeadValue() { 
		return headValue; 
	} 
 
	public void setHeadValue(String headValue) { 
		this.headValue = headValue; 
	} 
 
	public String getScope() { 
		return scope; 
	} 
 
	public void setScope(String scope) { 
		this.scope = scope; 
	} 
 
	public String getMultiple() { 
		return multiple; 
	} 
 
	public void setMultiple(String multiple) { 
		this.multiple = multiple; 
	} 
	 
}
