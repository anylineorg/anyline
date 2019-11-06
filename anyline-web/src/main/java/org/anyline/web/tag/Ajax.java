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


package org.anyline.web.tag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * ajax形式分页
 * @author Administrator
 *
 */
public class Ajax extends BodyTagSupport{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Ajax.class);
	private String url				;	//数据来源
	private String param			;	//参数收集函数
	private Boolean intime = false	;	//实时执行,否则放入jqery.ready
	private String callback			;	//回调函数
	private boolean async = true		;

	public int doStartTag() throws JspException {
		try{
			StringBuilder builder = new StringBuilder();
			builder.append("<script>\n");
			if(!intime){
				builder.append("$(function(){\n");
			}
			builder.append("al.ajax({");
			builder.append("url:'").append(url).append("',");
			if(BasicUtil.isNotEmpty(param)){
				builder.append("data:");
				builder.append(param).append(",");
			}
			if(BasicUtil.isNotEmpty(callback)){
				builder.append("callback:").append(callback).append(",");
			}
			builder.append("async:").append(async);
			builder.append("});\n");
			if(!intime){
				builder.append("});\n");
			}
			builder.append("</script>");
			JspWriter out = pageContext.getOut();
			out.print(builder.toString());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return EVAL_BODY_INCLUDE;
    }   
	public int doEndTag() throws JspException {   
	        return EVAL_PAGE;   
	}
	@Override
	public void release() {
		super.release();
		param 			= null	;	//参数收集函数
		callback 		= null	;	//回调函数
		intime 			= false	;
		url 			= null	;
		id 				= null	;
		async			= true  ;
	}
	
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	public Boolean getIntime() {
		return intime;
	}
	public void setIntime(Boolean intime) {
		this.intime = intime;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
