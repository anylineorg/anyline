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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
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

import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;


public class Select extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Select.class);
	private String scope;
	private Object data;
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
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
		String html = "<select "+attribute() + ">";
		if(null != body){
			html += body;
		}
		if(null == headValue){
			headValue = "";
		}
		if(null != head){
			html += "<option value=\""+headValue+"\">"+head+"</option>";
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
				Collection items = (Collection)data;
				if(null != items)
				for(Object item:items){
					String val = null;
					if(valueKey.contains("{")){
						val = valueKey;
						List<String> keys =RegularUtil.fetch(valueKey, "\\{\\w+\\}",Regular.MATCH_MODE.CONTAIN,0);
						for(String key:keys){
							Object v = BeanUtil.getFieldValue(item,key.replace("{", "").replace("}", ""));
							if(null == v){
								v = "";
							}
							val = val.replace(key, v.toString());
						}
					}else{
						val = BeanUtil.getFieldValue(item, valueKey)+"";
						if(encrypt){
							val = WebUtil.encryptValue(val+"");
						}
					}
					
					String text = "";
					if(textKey.contains("{")){
						text = parseRuntimeValue(item,textKey);
					}else{
						Object v = BeanUtil.getFieldValue(item, textKey);
						if(null != v){
							text = v.toString();
						}
					}

					html += "<option value=\"" + val + "\"";
					if(null != val && null != this.value && val.equals(value.toString())){
						html += " selected=\"selected\"";
					}
					html += crateExtraData(item);
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
		super.release();
		scope = null;
		data = null;
		scope = null;
		head = null;
		headValue = null;
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
		textKey = "NM";
		
		
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