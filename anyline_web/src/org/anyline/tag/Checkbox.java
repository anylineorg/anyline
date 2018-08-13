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

import org.apache.log4j.Logger;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.RegularUtil;
/**
 * 
 * 是否选中 一类的单个复选
 * <al:checkbox name="role${item.CODE }" value="1" data="" head="" headValue="${item.CODE }"></al:checkbox>
 * value:选中值 
 * value="{1,2,3,4,5}"	 item.get(valueKey)是在1,3,4,5集合中时选中
 * value="${list}" property="ID"	item.get(valueKey)是在list.items.property 集合中时选中
 * property="CHK" data.item.CHK  = true或1时选中
 *
 */
public class Checkbox extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Checkbox.class);
	private String scope;
	private Object data;
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
	private String textKey = "NM";
	//private Object checked;	//
	private String property;
	private String head;
	private String headValue;
	private boolean checked = false;

	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();
		String html = "";
		try {
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
						String tmp[] = item.split(":");
						map.put(valueKey, tmp[0]);
						if(tmp.length>1){
							map.put(textKey, tmp[1]);
						}else{
							map.put(textKey, "");
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
						if(null == property){
							property = valueKey;
						}
						for(Object item:cols){
							Object val = null;
							if(item instanceof Map){
								val = ((Map)item).get(property);
							}
							list.add(val);
						}
						this.value = list;
					}
				}
				Collection<Map> items = (Collection<Map>) data;
				Collection<?> chks = (Collection<?>)this.value;
				

				if(null == headValue){
					headValue = "";
				}
				if(null != head){
					String id = this.id;
					if(BasicUtil.isEmpty(id)){
						id = name +"_"+ headValue; 
					}
					html += "<div class=\"al-chk-item-border\"><input type=\"checkbox\"";
					if((null != headValue && headValue.equals(value)) || checked){
						html += " checked = \"checked\"";
					}
					html += " name=\""+name+"\" value=\"" + headValue + "\" id=\"" + id + "\"";
					if(null != clazz){
						html += " class=\"" + clazz + "\"";
					}
					if(null != style){
						html += " style=\"" + style + "\"";
					}
					if(null != onclick){
						html += " onclick=\"" + onclick + "\"";
					}
					if(null != onchange){
						html += " onchange=\"" + onchange + "\"";
					}
					if(null != onblur){
						html += " onblur=\"" + onblur + "\"";
					}
					if(null != onfocus){
						html += " onfocus=\"" + onfocus + "\"";
					}
					if(null != disabled){
						html += " disabled=\"" + disabled + "\"";
					}
					html +="/>" + "<label for=\""+id+ "\">" + head + "</label></div>\n";
				}
				
				
				if (null != items)
					for (Map item : items) {
						Object val = item.get(valueKey);
						if(this.encrypt){
							val = WebUtil.encryptValue(val+"");
						}
						String id = this.id;
						if(BasicUtil.isEmpty(id)){
							id = name +"_"+ val; 
						}
						html += "<div class=\"al-chk-item-border\"><input type=\"checkbox\" value=\"" + val + "\" id=\"" + id + "\"";
						Object chk = null;
						if(BasicUtil.isNotEmpty(property)){
							chk = item.get(property);
						}
						if(BasicUtil.parseBoolean(chk+"") || "checked".equals(chk) || checked(chks,item.get(valueKey)) ) {
							html += " checked=\"checked\"";
						}
						
						html += tag() + crateExtraData(item) + "/>";
						String label = "<label for=\""+id+ "\">";
						String text = "";
						if (textKey.contains("{")) {
							text = textKey;
							List<String> keys = RegularUtil.fetch(textKey, "\\{\\w+\\}", 2, 0);
							for (String key : keys) {
								Object v = item.get(key.replace("{", "").replace("}", ""));
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
						label += text +"</label></div>\n";
						html += label;
					}
			}
			JspWriter out = pageContext.getOut();
			out.print(html);
		} catch (Exception e) {
			e.printStackTrace();
			if(ConfigTable.isDebug()){
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
				if(null != chk && null != value && chk.equals(value.toString())){
					return true;
				}
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
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
		textKey = "NM";
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
	
}