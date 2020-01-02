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

import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class SelectText extends BaseBodyTag{ 
	private static final long serialVersionUID = 1L; 
	private String scope; 
	private Object data; 
	private String valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD"); 
	private String textKey = "NM"; 
 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest(); 
		String html = "";
//		valueKey = DataRow.keyCase(valueKey);
//		textKey = DataRow.keyCase(textKey); 
		try{ 
			if(null == data){ 
				return EVAL_PAGE; 
			} 
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
			if(null != value){ 
				for(Map item:items){
					Object tmp = item.get(valueKey); 
					if(null != tmp && value.toString().equals(tmp.toString())){ 
						String text = "";
						if(textKey.contains("{")){
							text = BeanUtil.parseFinalValue(item, textKey);
						}else{
							Object v = BeanUtil.getFieldValue(item, textKey);
							if(null != v){
								text = v.toString();
							}
						}
						html += text; 
					} 
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
		valueKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
		textKey = "NM";
		value = null; 
	} 
 
 
	public String getScope() { 
		return scope; 
	} 
 
	public void setScope(String scope) { 
		this.scope = scope; 
	} 
}
