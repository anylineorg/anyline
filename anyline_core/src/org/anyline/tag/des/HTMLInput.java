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

package org.anyline.tag.des;

import org.anyline.tag.ComponentTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;
/**
 * 加密
 * @author Administrator
 *
 */
public class HTMLInput extends ComponentTag{
	protected Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;

	public void createHead(Object obj){
		setEncrypt(true);
		builder.append("\t\t\t<input ");
		createAttribute();
		createValue(null);
		builder.append(">");
	}
	public void createBody(Object obj){
		
	}
	public void createEnd(){
		builder.append("</input>");
		if("checkbox".equalsIgnoreCase(type) || "radio".equals(type)){
			if(BasicUtil.isNotEmpty(body)){
				builder.append("<label for=\"");
				builder.append(WebUtil.encryptHttpRequestParamKey(id));
				builder.append("\">");
				builder.append(body);
				builder.append("</label>");
			}
		}
	}
	private void createValue(Object data){
		if(null != data && null != property){
			try{
				Object v = BeanUtil.getFieldValue(data, property);
				if(null != v){
					value = v.toString();
				}
			}catch(Exception e){
				log.error(e);
			}
		}
		if(!"text".equalsIgnoreCase(type)){
			value = WebUtil.encryptHttpRequestParamValue(value);
		}
		value = BasicUtil.nvl(value,"").toString();
		builder.append(" value=\"").append(value).append("\"");
		
	}
}