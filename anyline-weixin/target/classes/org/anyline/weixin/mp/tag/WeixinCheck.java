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


package org.anyline.weixin.mp.tag; 
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.ConfigTable;
import org.anyline.web.tag.BaseBodyTag;
import org.anyline.web.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 *是否支付微信JS
 *
 */ 
public class WeixinCheck extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		try{
			if(WebUtil.isWeixin(request)){
				JspWriter out = pageContext.getOut();
				out.println(body);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				e.printStackTrace();
			} 
		} finally { 
			release(); 
		} 
		return EVAL_PAGE; 
	}
	 
}
