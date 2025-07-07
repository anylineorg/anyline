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

package org.anyline.web.tag; 
 
 
import org.anyline.util.BasicUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;


public class Replace extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L; 
	private String target;
	private String separate;
	private String replacement;

	 public int doEndTag() throws JspException {
		 if(BasicUtil.isNotEmpty(var)) {
			 pageContext.getRequest().removeAttribute(var);
		 }
		 String src = BasicUtil.nvl(value,body,"").toString().trim();
		 if(BasicUtil.isEmpty(src)) {
			 return EVAL_BODY_INCLUDE;
		 }
		 if(null == target || target.isEmpty()) {
			 return EVAL_BODY_INCLUDE;
		 }
		if(BasicUtil.isEmpty(replacement)) {
			replacement = "";
		}
		try {
			String result = "";
			if(null!= separate) {
				/**
				 * 以separate分隔target,每个条目换成replacement,如果replacement中也有separate并且条目长度与 target 一致则按顺序替换
				 * value = "ABC123"  target = "ABC" to="a" result = "a123"
				 * value = "ABC123" target = "A,B,C"  separate="," replacement = "a" result = "aaa123"
				 * value = "ABC123" target = "A,B,C"  separate="," replacement = "a,b,c" result = "abc123"
				 */
				String froms[] = target.split(separate,-1);
				String tos[] = replacement.split(separate,-1);

				result = src;
				if(tos.length == froms.length) {
					int len = froms.length;
					for(int i=0; i<len; i++) {
						result = result.replace(froms[i], tos[i]);
					}
				}else {
					for (String item : froms) {
						result = result.replace(item, replacement);
					}
				}
			}else {
				result = src.replaceAll(target, replacement);
			}
			if(null != result) {
				if(BasicUtil.isNotEmpty(var)) {
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
		target = null;
		replacement = null;
		separate = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	}

	public String getSeparate() {
		return separate;
	}

	public void setSeparate(String separate) {
		this.separate = separate;
	}

	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}


	public String getReplacement() {
		return replacement;
	}


	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}
