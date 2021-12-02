/* 
 * Copyright 2006-2020 www.anyline.org
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
import org.anyline.util.DESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * 是否选中 一类的单个复选
 * &lt;al:checkbox name="role${item.CODE }" value="1" data="" head="" headValue="${item.CODE }"&gt;&lt;/al:checkbox&gt;
 * value:选中值 
 * value="{1,2,3,4,5}"	 item.get(valueKey)是在1,3,4,5集合中时选中
 * value="${list}" property="ID"	item.get(valueKey)是在list.items.property 集合中时选中
 * rely="CHK" data.item.CHK  = true或1时选中 根据rely列的值 判断是否选中
 *
 * text 支持多列 {ID}-{NM}
 */ 
public class Checkbox extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data; 
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID"); 
	private String textKey = "NM"; 
	//private Object checked;	// 
	private String property;
	private String rely;
	private String head;
	private String headValue;
	private String checkedValue = "";
	private boolean checked = false;
	private String border = "true";//条目border(内部包含checkox,label) true, false, div, li, dd
	private String borderClazz = "al-chk-item-border";//
	private String labelClazz = "al-chk-item-label";
	private String label = "";//label标签体，如果未定义label则生成默认label标签体{textKey}
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest(); 
		StringBuilder html = new StringBuilder();
//		valueKey = DataRow.keyCase(valueKey);
//		textKey = DataRow.keyCase(textKey);
		try {

			if(null == rely){
				rely = property;
			}
			if(null == rely){
				rely = valueKey;
			}
			 
			if (null != data) { 
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
				//选中值 
				if (null != this.value) {
					if(!(this.value instanceof String || this.value instanceof Collection)){
						this.value = this.value.toString();
					} 
					if (this.value instanceof String) { 
						if (this.value.toString().endsWith("}")) { 
							this.value = this.value.toString().replace("{", "").replace("}", ""); 
						} 
					}
					if (this.value instanceof String) { 
						String items[] = this.value.toString().split(","); 
						List list = new ArrayList(); 
						for (String item : items) { 
							list.add(item); 
						}
						this.value = list; 
					}else if(this.value instanceof Collection){ 
						List list = new ArrayList(); 
						Collection cols = (Collection)this.value; 
						for(Object item:cols){ 
							Object val = item; 
							if(item instanceof Map){ 
								val = ((Map)item).get(rely); 
							} 
							list.add(val); 
						} 
						this.value = list; 
					} 
				} 
				Collection<Map> items = (Collection<Map>) data; 
				Collection<?> chks = (Collection<?>)this.value;
				
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
				if(null != head){
					String id = this.id;
					if(BasicUtil.isEmpty(id)){
						id = name +"_"+ headValue; 
					}
					html.append(itemBorderStartTag);
					html.append("<input type=\"checkbox\"");
					if(null != headValue){
						if(checked || checkedValue.equals(headValue) || "true".equalsIgnoreCase(headValue) || "checked".equalsIgnoreCase(headValue) || checked(value,headValue) ) {
							html.append(" checked=\"checked\"");
						}
					}
					
					Map<String,String> map = new HashMap<String,String>();
					map.put(valueKey, headValue);
					html.append(attribute() + crateExtraData(map) + "/>");
					html.append("<label for=\"").append(id).append("\" class=\""+labelClazz+"\">").append(head).append("</label>\n");

					html.append(itemBorderEndTag);
				}
				
				 
				if (null != items) 
					for (Map item : items) { 
						Object val = item.get(valueKey);
						if(this.encrypt){
							val = DESUtil.encryptValue(val+"");
						}
						String id = this.id;
						if(BasicUtil.isEmpty(id)){
							id = name +"_"+ val; 
						}
						html.append(itemBorderStartTag);
						html.append("<input type=\"checkbox\" value=\"").append(val).append("\" id=\"").append(id).append("\"");
						Object chk = null;
						if(BasicUtil.isNotEmpty(rely)){
							chk = item.get(rely);
							if(null != chk){
								chk = chk.toString().trim();
							}
						} 
						if(checkedValue.equals(chk) || "true".equalsIgnoreCase(chk+"") || "checked".equalsIgnoreCase(chk+"") || checked(chks,item.get(valueKey)) ) { 
							html.append(" checked=\"checked\""); 
						}
						 
						html.append(attribute()).append(crateExtraData(item)).append("/>");
						if(BasicUtil.isEmpty(label)){ 
							String labelHtml = "<label for=\""+id+ "\" class=\""+labelClazz+"\">"; 
							String labelBody = ""; 
							if (textKey.contains("{")) { 
								labelBody = BeanUtil.parseRuntimeValue(item,textKey);
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
								labelHtml = BeanUtil.parseRuntimeValue(item,labelHtml);
							}
							html.append(labelHtml);
						}
						html.append(itemBorderEndTag);
					} 
			} 
			JspWriter out = pageContext.getOut(); 
			out.print(html.toString()); 
		} catch (Exception e) {
			e.printStackTrace();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				e.printStackTrace();
			} 
		} finally { 
			release(); 
		} 
		return EVAL_PAGE; 
	}
	private boolean checked(Collection<?> chks, Object value){
		if(null != chks){
			for(Object chk:chks){
				if(null != chk && null != value && chk.toString().equals(value.toString())){
					return true;
				}
			}
		}
		return false;
	}
	@SuppressWarnings("rawtypes")
	private boolean checked(Object chks, Object value){
		if(null != chks){
			if(chks instanceof Collection){
				return checked((Collection)chks, value);
			}else{
				return chks.equals(value);
			}
		}
		return false;
	} 
	public Object getData() { 
		return data; 
	} 
 
	public String getProperty() { 
		return property; 
	} 
	public void setCheckKey(String property) { 
		this.property = property; 
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
		property = null;
		head= null;
		headValue="";
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID");
		textKey = "NM";
		rely = null;
		checked = false;
		border = "true";
		borderClazz = "al-chk-item-border";
		labelClazz = "al-chk-item-label";
		label = "";
	} 
 
	public String getBorder() {
		return border;
	}
	public void setBorder(String border) {
		this.border = border;
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
	public String getScope() { 
		return scope; 
	} 
 
	public void setScope(String scope) { 
		this.scope = scope; 
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getRely() {
		return rely;
	}
	public void setRely(String rely) {
		this.rely = rely;
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
	public String getCheckedValue() {
		return checkedValue;
	}
	public void setCheckedValue(String checkedValue) {
		this.checkedValue = checkedValue;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
