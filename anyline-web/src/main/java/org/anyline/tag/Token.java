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


package org.anyline.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.util.TokenUtil;

public class Token extends BodyTagSupport{
	private static final long serialVersionUID = 1L;
	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}

	 public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String token = TokenUtil.createToken(request);
		if(null != token){
			JspWriter out = pageContext.getOut();
			try{
				out.print(token);
			}catch(Exception e){
				
			}finally{
				
			}
		}
        return EVAL_BODY_INCLUDE;
    }   
	public int doEndTag() throws JspException {   
		 return EVAL_PAGE;   
	}
}