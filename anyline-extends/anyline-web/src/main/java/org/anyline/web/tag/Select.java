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
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.*;
 
public class Select extends BaseBodyTag {
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data;
	private String selector;
	private String valueKey = ConfigTable.DEFAULT_PRIMARY_KEY; 
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
 
	@SuppressWarnings({"rawtypes", "unchecked" })
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest(); 
//		valueKey = DataRow.keyCase(valueKey); 
//		textKey = DataRow.keyCase(textKey);
		textKey = textKey.replace("{","${");
		valueKey = valueKey.replace("{","${");
		String html = ""; 
 
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
		if (data instanceof String) {
			String items[] = data.toString().split(","); 
			List list = new ArrayList(); 
			for (String item : items) {
				Map map = new HashMap(); 
				String ks[] = BeanUtil.parseKeyValue(item); 
				map.put(valueKey, ks[0]); 
				map.put(textKey, ks[1]); 
				if(ks.length>2) {
					map.put("CHK", ks[2]); 
				} 
				list.add(map); 
			} 
			data = list; 
		} 
		Collection items = (Collection) data;
		if(BasicUtil.isNotEmpty(selector) && data instanceof Collection) {
			items = BeanUtil.select(items,selector.split(","));
		}
		try {
			if ("text".equals(type)) {
				if (null != items) {
					if(items instanceof Map) {
						Map map = (Map)items;
						Object item = map.get(value);
						if(null != item) {
							if(ClassUtil.isPrimitiveClass(item) || item instanceof String) {
								html = item.toString();
							}else{
								html = BeanUtil.parseRuntimeValue(item, textKey);
							}
						}
					}else {
						for (Object item : items) {
							String val = BeanUtil.parseRuntimeValue(item, valueKey, encrypt);
							if (null != val && null != this.value && val.equals(value.toString())) {
								html =  BeanUtil.parseRuntimeValue(item, textKey);
							}
						}
					}
				} 
			} else {
				StringBuffer builder = new StringBuffer();
				builder.append("<select ");
				attribute(builder);
				if(BasicUtil.isNotEmpty(multiple)) {
					builder.append(" multiple=\"multiple\"");
				}
				if(size>1) {
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
					boolean first = true;
					for (Object item : items) {
						String val = BeanUtil.parseRuntimeValue(item, valueKey, encrypt);
						String text = BeanUtil.parseRuntimeValue(item, textKey);
						if(first){
							if(BasicUtil.isEmpty(text)) {
								text = BeanUtil.parseRuntimeValue(item, "NAME");
								if(BasicUtil.isNotEmpty(text)){
									textKey = "NAME";
								}
							}
						}
						first = false;
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

	@SuppressWarnings({"rawtypes", "unchecked" })
	public void addParam(String key, Object value) {
//		if(null == value || "".equals(value.toString().trim())) {
//			return ;
//		}
		if(null == key) {
			if(null == paramList) {
				paramList = new ArrayList<Object>();
			}
			if(value instanceof Collection) {
				paramList.addAll((Collection)value);
			}else{
				paramList.add(value);
			}
		}else{
			if(null == paramMap) {
				paramMap = new HashMap<String,Object>();
			}
			paramMap.put(key.trim(), value);
		}
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
		valueKey = ConfigTable.DEFAULT_PRIMARY_KEY; 
		textKey = "NM"; 
		multiple = null;
		selector = null;

	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
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
