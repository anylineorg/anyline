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


package org.anyline.tag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
/**
 * ajax形式分页
 * @author Administrator
 *
 */
public class Navi extends BodyTagSupport{
	public static final String CONFIG_FLAG_KEY = "_anyline_navi_conf_";
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Navi.class);
	private String url				;	//数据来源
	private String param			;	//参数收集函数
	private String container		;	//返回内容容器id
	private String type = "ajax"	;	//分页方式 ajax | jsp
	
	private String id				;	//一个页面内多个标签时需要id区分
	private Boolean intime = false	;	//实时执行,否则放入jqery.ready
	private String callback			;	//回调函数
	private String before			;	//渲染之前调用
	private String after			;	//渲染之后调用			
	private String bodyContainer	;	//如果body与page分开
	private String naviContainer	;	//如果body与page分开
	private String empty			;	//空数据显示内容

	public int doStartTag() throws JspException {
		try{
			StringBuilder builder = new StringBuilder();
			int idx = BasicUtil.parseInt((String)pageContext.getRequest().getAttribute("_anyline_navi_tag_idx"), 0);
			String flag = id;
			if(null == flag){
				flag = idx+"";
			}

			String confId = CONFIG_FLAG_KEY + flag;
			builder.append("<div id='_navi_border_"+confId+"'>");
			if(idx == 0){
				builder.append("<link rel=\"stylesheet\" href=\""+ConfigTable.getString("NAVI_STYLE_FILE_PATH")+"\" type=\"text/css\"/>\n");
				builder.append("<script type=\"text/javascript\" src=\""+ConfigTable.getString("NAVI_SCRIPT_FILE_PATH")+"\"></script>\n");
			}
			builder.append("<script>\n");
			builder.append("var " + confId + " = {");
			builder.append(CONFIG_FLAG_KEY).append(":'").append(flag).append("',");
			if(BasicUtil.isNotEmpty(url)){
				builder.append("url:'").append(url).append("',");
			}
			if(BasicUtil.isNotEmpty(param)){
				builder.append("param:").append(param).append(",");
			}
			if(BasicUtil.isNotEmpty(container)){
				builder.append("container:'").append(container).append("',");
			}
			if(BasicUtil.isNotEmpty(bodyContent)){
				builder.append("bodyContainer:'").append(bodyContainer).append("',");
			}
			if(BasicUtil.isNotEmpty(naviContainer)){
				builder.append("naviContainer:'" ).append(naviContainer).append("',");
			}
			if(BasicUtil.isNotEmpty(callback)){
				builder.append("callback:" ).append(callback).append(",");
			}
			if(BasicUtil.isNotEmpty(before)){
				builder.append("before:" ).append(before).append(",");
			}
			if(BasicUtil.isNotEmpty(after)){
				builder.append("after:" ).append(after).append(",");
			}
			if(null != empty){
				builder.append("empty:'" ).append(empty).append("',");
			}
			builder.append("type:'ajax'");
			builder.append("};\n");
			if(intime){
				builder.append("$('#_navi_border_"+confId+"').ready(function(){_navi_init("+confId+");});");
			}else{
				builder.append("$(function(){_navi_init("+confId+");});\n");
			}
			builder.append("</script>");
			builder.append("</div>");
			idx ++;
			pageContext.getRequest().setAttribute("_anyline_navi_tag_idx", idx + "");
			JspWriter out = pageContext.getOut();
			out.print(builder.toString());
		}catch(Exception e){
			log.error(e);
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
		container 		= null	;	//返回内容容器
		callback 		= null	;	//回调函数
		bodyContainer 	= null	;	//如果body与page分开
		naviContainer 	= null	;	//如果body与page分开
		empty 			= null	;	//空数据显示内容
		intime 			= false	;
		url 			= null	;
		id 				= null	;
		after			= null	;
		before			= null	;
		type			= "ajax";
	}
	
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	public String getBodyContainer() {
		return bodyContainer;
	}
	public void setBodyContainer(String bodyContainer) {
		this.bodyContainer = bodyContainer;
	}
	public String getNaviContainer() {
		return naviContainer;
	}
	public void setNaviContainer(String naviContainer) {
		this.naviContainer = naviContainer;
	}
	public String getEmpty() {
		return empty;
	}
	public void setEmpty(String empty) {
		this.empty = empty;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBefore() {
		return before;
	}
	public void setBefore(String before) {
		this.before = before;
	}
	public String getAfter() {
		return after;
	}
	public void setAfter(String after) {
		this.after = after;
	}

}
