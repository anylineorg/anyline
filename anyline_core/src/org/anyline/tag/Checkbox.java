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
 */


package org.anyline.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.anyline.util.regular.RegularUtil;

public class Checkbox extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Checkbox.class);
	private String scope;
	private Object data;
	private String valueKey = "CD";
	private String textKey = "NM";
	private Object checked;	//已选中项 1或{1,2}或List
	private String checkKey;

	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();
		String html = "";
		try {
			if (null != data) {
				if (data instanceof String) {
					if (data.toString().endsWith("}")) {
						data = data.toString().replace("{", "")
								.replace("}", "");
					} else {
						if ("servelt".equals(scope)
								|| "application".equalsIgnoreCase(scope)) {
							data = request.getSession().getServletContext()
									.getAttribute(data.toString());
						} else if ("session".equals(scope)) {
							data = request.getSession().getAttribute(
									data.toString());
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
						String tmp[] = item.split(":");
						map.put(valueKey, tmp[0]);
						map.put(textKey, tmp[1]);
						list.add(map);
					}
					data = list;
				}

				if (null != checked) {
					if (checked instanceof String) {
						if (checked.toString().endsWith("}")) {
							checked = checked.toString().replace("{", "").replace(
									"}", "");
						}
					}
					if (checked instanceof String) {
						String items[] = checked.toString().split(",");
						List list = new ArrayList();
						for (String item : items) {
							list.add(item);
						}
						checked = list;
					}else if(checked instanceof Collection){
						List list = new ArrayList();
						Collection cols = (Collection)checked;
						if(null == checkKey){
							checkKey = valueKey;
						}
						for(Object item:cols){
							Object val = null;
							if(item instanceof Map){
								val = ((Map)item).get(checkKey);
							}
							list.add(val);
						}
						checked = list;
					}
				}
				Collection<Map> items = (Collection<Map>) data;
				Collection chks = (Collection)checked;
				if (null != items)
					for (Map item : items) {
						Object val = item.get(valueKey);
						String id = name +"_"+ value;
						html += "<input type='checkbox' value='" + val + "' id='" + id+"_"+val+"'";
						Object chk = item.get("CHECKED")+"";
						if("1".equals(chk) || checked(chks,val) ) {
							html += " checked='checked'";
						}
						html +=tag()+ "/>";
						String label = "<label for='"+id+"_"+val+"'>";
						String text = "";
						if (textKey.contains("{")) {
							text = textKey;
							List<String> keys = RegularUtil.fetch(textKey,
									"\\{\\w+\\}", 2, 0);
							for (String key : keys) {
								Object v = item.get(key.replace("{", "")
										.replace("}", ""));
								if (null != v) {
									text = text.replace(key, v.toString());
								}
							}
						} else {
							Object v = item.get(textKey);
							if (null != v) {
								text = v.toString();
							}
						}
						label += text +"</label>";
						html += label;
					}
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			release();
		}
		return EVAL_PAGE;
	}
	private boolean checked(Collection<Object> chks, Object value){
		if(null != chks){
			for(Object chk:chks){
				if(null != chk && chk.equals(value)){
					return true;
				}
			}
		}
		return false;
	}
	public Object getData() {
		return data;
	}

	public Object getChecked() {
		return checked;
	}
	public void setChecked(Object checked) {
		this.checked = checked;
	}
	public String getCheckKey() {
		return checkKey;
	}
	public void setCheckKey(String checkKey) {
		this.checkKey = checkKey;
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
		scope = null;
		data = null;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}