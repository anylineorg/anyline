/* 
 * Copyright 2006-2015 the original author or authors.
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


public class Select extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Select.class);
	private String scope;
	private Object data;
	private String valueKey = "CD";
	private String textKey = "NM";
	private String head;
	private String headValue;
	
	public String getHead() {
		return head;
	}


	public void setHead(String head) {
		this.head = head;
	}


	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String html = "<select "+tag() + ">";
		if(null != body){
			html += body;
		}
		if(null == headValue){
			headValue = "";
		}
		if(null != head){
			html += "<option value='"+headValue+"'>"+head+"</option>";
		}
		try{
			if(null != data){
				if(data instanceof String){
					if(data.toString().endsWith("}")){
						data = data.toString().replace("{", "").replace("}", "");
					}else{
						if("servelt".equals(scope) || "application".equalsIgnoreCase(scope)){
							data = request.getSession().getServletContext().getAttribute(data.toString());
						}else if("session".equals(scope)){
							data = request.getSession().getAttribute(data.toString());
						}else{
							data = request.getAttribute(data.toString());
						}
					}
				}
				if(data instanceof String){
					String items[] = data.toString().split(",");
					List list = new ArrayList();
					for(String item:items){
						Map map = new HashMap();
						String tmp[] = item.split(":");
						map.put(valueKey, tmp[0]);
						map.put(textKey, tmp[1]);
						list.add(map);
					}
					data = list;
				}
				Collection<Map> items = (Collection<Map>)data;
				if(null != items)
				for(Map item:items){
					html += "<option value='"+item.get(valueKey)+"'";
					if(null != value && value.toString().equals(item.get(valueKey))){
						html += " selected='selected'";
					}
					String text = "";
					if(textKey.contains("{")){
						text = textKey;
						List<String> keys =RegularUtil.fetch(textKey, "\\{\\w+\\}",2,0);
						for(String key:keys){
							Object v = item.get(key.replace("{", "").replace("}", ""));
							if(null == v){
								v = "";
							}
							text = text.replace(key, v.toString());
						}
					}else{
						Object v = item.get(textKey);
						if(null != v){
							text = v.toString();
						}
					}
					html += ">" + text+ "</option>";
				}
			}
			html += "</select>";
				JspWriter out = pageContext.getOut();
				out.print(html);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
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
		scope = null;
		data = null;
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
}