/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.web.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.encrypt.DESUtil;
import org.anyline.web.tag.BaseBodyTag;
/** 
 * http request 请求参数名加密 
 * @author zh 
 * 
 */ 
public class DESHttpRequestParamKey extends BaseBodyTag{
	private static final long serialVersionUID = 1L; 
	private String value;		// 被加密数据 
 
	public int doEndTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut(); 
			out.print(DESUtil.encryptParamKey(BasicUtil.nvl(value,body,"").toString())); 
		}catch(Exception e) {
			e.printStackTrace(); 
		}finally{
			release(); 
		}    
		return EVAL_PAGE;    
	} 
	@Override 
	public void release() {
		super.release();
		body = null; 
		value = null; 
	} 
 
	public String getValue() {
		return value; 
	} 
 
	public void setValue(String value) {
		this.value = value; 
	} 
 
}
