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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.tag.seo;


import javax.servlet.jsp.JspException;

import org.anyline.tag.BaseBodyTag;
import org.apache.log4j.Logger;

/**
 * 
 * @author Administrator
 * 客户端回复确认(JS形式)
 * 
 */
public class ClientReply extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(ClientReply.class);

	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	public int doEndTag() throws JspException {
		try{
			String js = "<script type=\"text/javascript\" src=\"/iccr?c="+pageContext.getRequest().hashCode()+"\"></script>";
			pageContext.getOut().write(js);
		}catch(Exception e){
			LOG.error(e);
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}

	@Override
	public void release() {
		super.release();
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private String getParamValue(String key){
		if(paramMap != null){
			return (String)paramMap.get(key);
		}
		return null;
	}
}