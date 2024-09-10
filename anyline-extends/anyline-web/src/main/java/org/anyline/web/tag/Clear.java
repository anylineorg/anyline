/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.web.tag;


import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;


public class Clear extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private int len = 0;

	 public int doEndTag() throws JspException {
		 String src = BasicUtil.nvl(value,body,"").toString().trim();
		 if(BasicUtil.isEmpty(src)){
			 return EVAL_BODY_INCLUDE;
		 }
		 if(BasicUtil.isNotEmpty(var)){
			 pageContext.getRequest().removeAttribute(var);
		 }
		try {
			String result = RegularUtil.removeTag(src);
			result = result.replaceAll("[^\u2E80-\uFE4F0-9]","")
					.replaceAll("[　、％（〈）〉《》＋,。／【０１】２３〔４—〕５６７８９“＜”＞？•\"#%'()+,-./［］>]", "")
					.replace(" ", "")
					.replace("\n", "")
					.replace("\r", "")
					.replace("”", "")
					.replace("“", "");
			if(null != result) {
				if(len >= 0 && len<result.length()){
					result = result.substring(0, len);
				}
				if(BasicUtil.isNotEmpty(var)){
					pageContext.getRequest().setAttribute(var, result);
				}else {
					JspWriter out = pageContext.getOut();
					out.print(result);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			release();
		}
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容
	} 
 
 
	@Override 
	public void release() {
		super.release();
		value = null;
		len = 0;
		var = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}
}
