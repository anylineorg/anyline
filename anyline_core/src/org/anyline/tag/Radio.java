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
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;


public class Radio extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Radio.class);
	private String scope;
	private Object data;
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
	private String textKey = "NM";


	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String html = "<span "+tag() + ">";
		if(null != body){
			html += body;
		}
		try{
			if(BasicUtil.isEmpty(name)){
				name = BasicUtil.getRandomLowerString(10);
			}
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
					Object value = BeanUtil.getFieldValue(item, valueKey);
					String id = name +"_"+ value;
					html += "<input type='radio' value='" + value + "' name='"+name+"' id='"+id+"'";
					if(null != value && value.toString().equals(this.value)){
						html += " checked='checked'";
					}
					String text = "";
					if(textKey.contains("{")){
						text = textKey;
						List<String> keys =RegularUtil.fetch(textKey, "\\{\\w+\\}",2,0);
						for(String key:keys){
							Object v = BeanUtil.getFieldValue(item,key.replace("{", "").replace("}", ""));
							if(null == v){
								v = "";
							}
							text = text.replace(key, v.toString());
						}
					}else{
						Object v = BeanUtil.getFieldValue(item, textKey);
						if(null != v){
							text = v.toString();
						}
					}
					html += "></input>" + "<label for='"+id+"'>"+text+"</label>";
				}
			}
			html += "</span>";
			JspWriter out = pageContext.getOut();
			out.print(html);
		}catch(Exception e){
			LOG.error(e);
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

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}