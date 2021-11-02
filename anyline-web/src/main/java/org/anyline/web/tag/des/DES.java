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


package org.anyline.web.tag.des; 
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DESUtil;
import org.anyline.web.tag.BaseBodyTag;
/** 
 * 加密 
 * @author zh 
 * 
 */ 
public class DES extends BaseBodyTag{ 
	private static final long serialVersionUID = 1L; 
	private String key;			//密钥 
	private String value;		//被加密数据 
 
	public int doEndTag() throws JspException {    
		try{ 
			value = BasicUtil.nvl(value,body,"").toString().trim(); 
			value = DESUtil.getInstance(key).encrypt(value); 
			JspWriter out = pageContext.getOut(); 
			out.print(value); 
		}catch(Exception e){
			e.printStackTrace(); 
		}finally{ 
			release(); 
		} 
		return EVAL_PAGE;    
	} 
	@Override 
	public void release() { 
		super.release(); 
		key = null; 
		value = null; 
	} 
	public String getKey() { 
		return key; 
	} 
 
	public void setKey(String key) { 
		this.key = key; 
	} 
 
	public String getValue() { 
		return value; 
	} 
 
	public void setValue(String value) { 
		this.value = value; 
	} 
 
}
