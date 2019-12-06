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
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class Radio extends BaseBodyTag{ 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(Radio.class); 
	private String scope; 
	private Object data; 
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID"); 
	private String textKey = "NM"; 
	private String head;
	private String headValue;
	private String border = "true";//条目border(内部包含checkox,label) true, false, div, li, dd
	private String borderClazz = "al-radio-item-border";
	private String labelClazz = "al-radio-item-label";
	private String label = "";//label标签体，如果未定义label则生成默认label标签体{textKey} 
 
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest(); 
		StringBuilder html = new StringBuilder();
//		valueKey = DataRow.keyCase(valueKey);
//		textKey = DataRow.keyCase(textKey); 
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
						String ks[] = BeanUtil.parseKeyValue(item); 
						map.put(valueKey, ks[0]); 
						map.put(textKey, ks[1]); 
						list.add(map); 
					} 
					data = list; 
				}

				//条目边框
				String itemBorderTagName ="";
				String itemBorderStartTag = "";
				String itemBorderEndTag = "";
				if(BasicUtil.isNotEmpty(border) && !"false".equals(border)){
					if("true".equalsIgnoreCase(border)){
						itemBorderTagName = "div";
					}else{
						itemBorderTagName = border;
					}
					itemBorderStartTag = "<"+itemBorderTagName+" class=\""+borderClazz+"\">";
					itemBorderEndTag = "</"+itemBorderTagName+">";
				}
				if(null == headValue){
					headValue = "";
				}

				if(null == headValue){
					headValue = "";
				}
				if(null != head){
					String id = this.id;
					if(BasicUtil.isEmpty(id)){
						id = name +"_"+ headValue; 
					}
					html.append(itemBorderStartTag);
					html.append("<input type=\"radio\"");
					if((null != headValue && headValue.equals(value))){
						html.append(" checked = \"checked\"");
					}
					Map<String,String> map = new HashMap<String,String>();
					map.put(valueKey, headValue);
					html.append(attribute()).append(crateExtraData(map)).append("/>");
					html.append("<label for=\"").append(id).append("\" class=\"").append(labelClazz).append("\">").append(head).append("</label>\n");
					html.append(itemBorderEndTag);
				}
				
				
				
				 
				Collection<Map> items = (Collection<Map>)data;
				if(null != items) 
				for(Map item:items){
					Object srcValue = BeanUtil.getFieldValue(item, valueKey);
					Object value = srcValue;
					if(this.encrypt){
						value = WebUtil.encryptValue(value+"");
					}
					
					String id = name +"_"+ value;
					html.append(itemBorderStartTag);
					html.append("<input type=\"radio\" value=\"").append(value).append("\" id=\"").append(id).append("\"");
					if(null != srcValue && null != this.value && srcValue.toString().equals(this.value.toString())){ 
						html.append(" checked=\"checked\""); 
					}
					html.append(attribute()).append(crateExtraData(item)).append("/>");
					
					if(BasicUtil.isEmpty(label)){
						String labelHtml = "<label for=\""+id+ "\" class=\""+labelClazz+"\">";
						String labelBody = "";
						if (textKey.contains("{")) {
							labelBody = parseRuntimeValue(item,textKey);
						} else {
							Object v = item.get(textKey);
							if (null != v) {
								labelBody = v.toString();
							}
						}
						labelHtml += labelBody +"</label>\n";
						html.append(labelHtml);
					}else{//指定label文本
						String labelHtml = label;
						if(labelHtml.contains("{") && labelHtml.contains("}")){
							labelHtml = parseRuntimeValue(item,labelHtml);
						}
						html.append(labelHtml);
					}
					
					html.append(itemBorderEndTag);
				} 
			} 
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
 
 
	public String getBorder() {
		return border;
	}


	public void setBorder(String border) {
		this.border = border;
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
		value = null;
		body = null;
		head = null;
		headValue = null;
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID");
		textKey = "NM";
		border = "true";
		borderClazz = "al-radio-item-border";
		labelClazz = "al-radio-item-label";
		label = ""; 
	} 
 
	public String getScope() { 
		return scope; 
	} 
 
	public String getHead() {
		return head;
	}


	public void setHead(String head) {
		this.head = head;
	}


	public String getHeadValue() {
		return headValue;
	}


	public void setHeadValue(String headValue) {
		this.headValue = headValue;
	}


	public void setScope(String scope) { 
		this.scope = scope; 
	}


	public String getBorderClazz() {
		return borderClazz;
	}


	public void setBorderClazz(String borderClazz) {
		this.borderClazz = borderClazz;
	}


	public String getLabelClazz() {
		return labelClazz;
	}


	public void setLabelClazz(String labelClazz) {
		this.labelClazz = labelClazz;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}
	 
}
