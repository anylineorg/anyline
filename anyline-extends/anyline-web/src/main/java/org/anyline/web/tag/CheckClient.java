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
 
/**
 * 判断当前type是否包含在指定的type中
 * 参考 Webutil.clientType
 * app,wechat,qq,alipay,wap,web
 */ 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.web.util.WebUtil;
 
 
public class CheckClient extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L; 
	private String type = "";
	private Object elseValue;
	private boolean skip = false;//如果test=false时是否跳过body体(不再执行boyd中的子标签) skip=false时即使test=false标签体也会执行
	private boolean test = false;
	private boolean truncate = false; // 如果test=false;跳整个页面
	public int doStartTag() {
		type = (type+"").toLowerCase();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String curType = WebUtil.clientType(request).toLowerCase();
		if(type.contains(curType)) {
			test = true;
		}
		if(truncate && !test) {
			return SKIP_PAGE;
		}
		if(skip && !test) {
			return SKIP_BODY;
		}else {
			return EVAL_BODY_BUFFERED;
		}
	}
	public int doEndTag() throws JspException {
		try{
			if(test) {
				JspWriter out = pageContext.getOut();
				out.print(BasicUtil.nvl(value,body,""));
			}else if(null != elseValue) {
				JspWriter out = pageContext.getOut();
				out.print(elseValue);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			release();
		}
		return EVAL_PAGE ;
	}
 
 
	@Override 
	public void release() {
		super.release(); 
		value = null;
		body = null; 
		type = null;
		value = null;
		elseValue = null;
		skip = false;
		truncate = false;
		test = false;
	} 
	 
	 
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	}

	public void setElse(Object elseValue) {
		this.elseValue = elseValue;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public boolean isTruncate() {
		return truncate;
	}

	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}
}
