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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.*;
public class BaseBodyTag extends BodyTagSupport implements Cloneable{
	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass()); 
 
	protected List<Object> paramList = null; 
	protected Map<String,Object> paramMap = null; 
	protected String body = null; 
	protected String id; 
	protected String name; 
	protected Object value;
	protected Object evl;
	protected Object nvl;
	protected String clazz; 
	protected String style; 
	protected String onclick; 
	protected String onchange; 
	protected String onblur; 
	protected String onfocus;
	protected String disabled;
	protected String readonly;
	protected String extra;
	protected String extraPrefix = "data-";
	protected Object extraData;
	protected String itemExtra;
	protected String var;
	protected boolean encrypt;	//是否加密 
	
	 
	public String getItemExtra() {
		return itemExtra;
	}
	public void setItemExtra(String itemExtra) {
		this.itemExtra = itemExtra;
	}
	public String getExtra() {
		return extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
	public String getDisabled() {
		return disabled;
	}
	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}
	protected void attribute(StringBuffer builder){
		if(null != id){
			builder.append(" id=\"").append(id).append("\"");
		} 
		if(null != name){
			builder.append(" name=\"").append(name).append("\"");
		} 
//		if(null != value){ 
//			html += " value=\"" + value + "\""; 
//		} 
		if(null != clazz){
			builder.append(" class=\"").append(clazz).append("\"");
		} 
		if(null != style){
			builder.append(" style=\"").append(style).append("\"");
		} 
		if(null != onclick){
			builder.append(" onclick=\"").append(onclick).append("\"");
		} 
		if(null != onchange){
			builder.append(" onchange=\"").append(onchange).append("\"");
		} 
		if(null != onblur){
			builder.append(" onblur=\"").append(onblur).append("\"");
		}
		if(null != onfocus){
			builder.append(" onfocus=\"").append(onfocus).append("\"");
		}
		if(BasicUtil.isNotEmpty(disabled) && !"false".equalsIgnoreCase(disabled)){
			builder.append(" disabled=\"").append(disabled).append("\"");
		}
		if(BasicUtil.isNotEmpty(readonly) && !"false".equalsIgnoreCase(readonly)){
			builder.append(" readonly=\"").append(readonly).append("\"");
		}
		crateExtraData(builder);
	}
	/**
	 * 条目data-*
	 * itemExtra = "ID:1"
	 * itemExtra = "ID:{ID}"
	 * itemExtra = "ID:{ID}-{NM}"
	 * @param obj obj
	 * @return return
	 */
	protected void crateExtraData(StringBuffer builder, Object obj){
		if(BasicUtil.isNotEmpty(itemExtra)){
			String[] list = itemExtra.split(",");
			for(String item:list){
				String[] tmps = item.split(":");
				if(tmps.length>=2){
					String id = tmps[0];
					String key = tmps[1];
					String value = BeanUtil.parseRuntimeValue(obj,key);
					if(null == value){
						value = "";
					}
					builder.append(extraPrefix).append(id).append("=\"").append(value).append("\"");
				}
			}
		}
	}
	protected void crateExtraData(StringBuffer builder){
		if(BasicUtil.isNotEmpty(extra)){
			if(extra.startsWith("{") && extra.endsWith("}")){
				//{id:1,nm:2} > data-id=1,data-nm=2 
				extra = extra.substring(1,extra.length()-1);
				String[] list = extra.split(",");
				for(String item:list){
					String[] tmps = item.split(":");
					if(tmps.length>=2){
						builder.append(extraPrefix).append(tmps[0]).append("=\"").append(tmps[1]).append("\"");
					}
				}
			}else{
				//id:ID,name:{NM}-{CODE} > data-id=extraData.get("ID"),data-NAME=extraData.get("NM")-extraData.get("CODE")
				String[] list = extra.split(",");
				for(String item:list){
					String[] tmps = item.split(":");
					if(tmps.length>=2){
						String value = BeanUtil.parseRuntimeValue(extraData, tmps[1]);
						builder.append(extraPrefix).append(tmps[0]).append("=\"").append(value).append("\"");
					}
				}
			}
		}
	} 
	public int doStartTag() throws JspException { 
        return EVAL_BODY_BUFFERED; 
    } 
	public int doAfterBody() throws JspException { 
		if(null != bodyContent){ 
			body = bodyContent.getString().trim(); 
		} 
		return super.doAfterBody(); 
	} 
	 public int doEndTag() throws JspException { 
        return EVAL_PAGE;    
	} 
 
 
	@Override 
	public void release() { 
		super.release(); 
		if(null != paramList){ 
			paramList.clear(); 
		} 
		if(null != paramMap){ 
			paramMap.clear(); 
		} 
		body = null;
		id = null;
		name = null;
		value = null;
		//evl = false;
		evl = null;
		nvl = null;
		clazz = null;
		style = null;
		onclick = null;
		onchange = null;
		onblur = null;
		onfocus = null;
		disabled = null;
		extra = null;
		itemExtra = null;
		readonly = null;
		encrypt = false;
		extraPrefix ="data-";
		extraData = null;
		var = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addParam(String key, Object value) { 
//		if(null == value || "".equals(value.toString().trim())){ 
//			return ; 
//		} 
		if(null == key){ 
			if(null == paramList){ 
				paramList = new ArrayList<Object>(); 
			} 
			if(value instanceof Collection){ 
				paramList.addAll((Collection)value); 
			}else{ 
				paramList.add(value); 
			} 
		}else{ 
			if(null == paramMap){ 
				paramMap = new HashMap<String,Object>(); 
			} 
			paramMap.put(key.trim(), value); 
		} 
	} 
	public BodyContent getBodyContent() { 
		return super.getBodyContent(); 
	} 
 
	public void setBodyContent(BodyContent b) { 
		super.setBodyContent(b); 
	} 
	public String getBody() { 
		return body; 
	} 
	public void setBody(String body) {
//		if(evl && BasicUtil.isNotEmpty(body)){
//			 String str = body.toString();
//			 if(str.contains(",")){
//				 String[] strs = str.split(",");
//				 body = (String)BasicUtil.nvl(strs);
//			 }
//		}
		this.body = body; 
	} 
	public String getId() { 
		return id; 
	} 
	public void setId(String id) { 
		this.id = id; 
	} 
	public String getName() { 
		return name; 
	} 
	public void setName(String name) { 
		this.name = name; 
	} 
 
	public Object getValue() { 
		return value; 
	} 
	public void setValue(Object value) {
//		if(evl && BasicUtil.isNotEmpty(value)){
//			 String str = value.toString();
//			 if(str.contains(",")){
//				 String[] strs = str.split(",");
//				 value = BasicUtil.nvl(strs);
//			 }
//		}
		this.value = value;
	}
	public String getClazz() { 
		return clazz; 
	} 
	public void setClazz(String clazz) { 
		this.clazz = clazz; 
	} 
	public String getStyle() { 
		return style; 
	} 
	public void setStyle(String style) { 
		this.style = style; 
	} 
	public String getOnclick() { 
		return onclick; 
	} 
	public void setOnclick(String onclick) { 
		this.onclick = onclick; 
	} 
	public String getOnchange() { 
		return onchange; 
	} 
	public void setOnchange(String onchange) { 
		this.onchange = onchange; 
	} 
	public String getOnblur() { 
		return onblur; 
	} 
	public void setOnblur(String onblur) { 
		this.onblur = onblur; 
	} 
	public String getOnfocus() { 
		return onfocus; 
	} 
	public void setOnfocus(String onfocus) { 
		this.onfocus = onfocus; 
	}
	public boolean isEncrypt() {
		return encrypt;
	}
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}
	public String getReadonly() {
		return readonly;
	}
	public void setReadonly(String readonly) {
		this.readonly = readonly;
	}
//	public boolean isEvl() {
//		return evl;
//	}
//	public void setEvl(boolean evl) {
//		this.evl = evl;
//	}

	public Object getEvl() {
		return evl;
	}

	public void setEvl(Object evl) {
		this.evl = evl;
	}

	public Object getNvl() {
		return nvl;
	}

	public void setNvl(Object nvl) {
		this.nvl = nvl;
	}

	public String getExtraPrefix() {
		return extraPrefix;
	}
	public void setExtraPrefix(String extraPrefix) {
		this.extraPrefix = extraPrefix;
	}
	public Object getExtraData() {
		return extraData;
	}
	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
}
