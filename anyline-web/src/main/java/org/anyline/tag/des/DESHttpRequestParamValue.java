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


package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * http request 请求参数值加密
 * @author Administrator
 *
 */
public class DESHttpRequestParamValue extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut();
			out.print(WebUtil.encryptHttpRequestParamValue(BasicUtil.nvl(value,body,"").toString()));
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
		value = null;
		body = null;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}