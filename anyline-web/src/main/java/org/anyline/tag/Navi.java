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
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.entity.PageNaviConfig;
import org.anyline.util.BasicUtil;
import org.apache.log4j.Logger;
/**
 * ajax形式分页
 * @author Administrator
 *
 */
public class Navi extends BodyTagSupport{
	public static final String CONFIG_FLAG_KEY = "_anyline_navi_conf_";
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Navi.class);
	private String url				;	//数据来源
	private String param			;	//参数收集函数
	private String container		;	//返回内容显示容器
	private String body				;	//返回内容显示容器class或id(如果body与page分开)
	private String page				;	//返回内容显示容器class或id(如果body与page分开)
	private String bodyContainer	;	//如果body与page分开(兼容上一版本)
	private String naviContainer	;	//如果body与page分开(兼容上一版本)
	private String creater = "ajax"	;	//分页方式 ajax | html
	private String scroll			;   //自动翻页时 监听的滚动事件源 默认window
	private String method = "post"	;
	private String id				;	//一个页面内多个标签时需要id区分
	private String function			;	//指定function后,需主动调用function后加载数据,查询条件发生变化时可调用些function
	private Boolean intime = false	;	//实时执行
	private Boolean auto = false	;	//是否加载下一页内容(wap加载更多typ=1时 划屏到底部自动加载)
	private String callback			;	//回调函数
	private String before			;	//渲染之前调用
	private String after			;	//渲染之后调用		
	private String guide			;   //加载更多文本
	
	private String empty			;	//空数据显示内容
	private String style = "default"; 	//样式标记对应anyline-navi.xml中的config.key
	private Boolean stat = false	;	//是否显示统计
	private Boolean jump = false	;	//是否显示跳转
	
	private int type = 0			;	//分页方式(0:下标 1:加载更多)

	
	public int doStartTag() throws JspException {
		try{
			PageNaviConfig config = PageNaviConfig.getInstance(style);
			StringBuilder builder = new StringBuilder();
			int idx = BasicUtil.parseInt((String)pageContext.getRequest().getAttribute("_anyline_navi_tag_idx"), 0);
			String flag = id;
			if(null == flag){
				flag = idx+"";
			}
			String confId = CONFIG_FLAG_KEY + flag;
			builder.append("<div id='_navi_border_"+flag+"'>");
			if(idx == 0){
				builder.append("<link rel=\"stylesheet\" href=\"" + config.STYLE_FILE_PATH + "\" type=\"text/css\"/>\n");
				builder.append("<script type=\"text/javascript\" src=\"" + config.SCRIPT_FILE_PATH + "\"></script>\n");
			}
			builder.append("<script>\n");
			builder.append("var " + confId + " = {");
			builder.append(CONFIG_FLAG_KEY).append(":'").append(flag).append("',");
			if(BasicUtil.isNotEmpty(url)){
				builder.append("url:'").append(url).append("',");
			}
			if(BasicUtil.isNotEmpty(param)){
				String sign = "'";
				if(param.contains(")")){
					//构成成String 每次运行时解析实时value
					if(param.contains("'")){
						sign = "\"";
					}
					builder.append("param:").append(sign).append(param).append(sign).append(",");
				}else{
					builder.append("param:").append(param).append(",");
				}
			}
			if(BasicUtil.isNotEmpty(container)){
				builder.append("container:'").append(container).append("',");
			}
			body = (String)BasicUtil.evl(body, bodyContainer);
			page = (String)BasicUtil.evl(page, naviContainer);
			if(BasicUtil.isNotEmpty(body)){
				builder.append("body:'").append(body).append("',");
				builder.append("bodyContainer:'").append(body).append("',");
			}
			if(BasicUtil.isNotEmpty(page)){
				builder.append("page:'" ).append(page).append("',");
				builder.append("naviContainer:'" ).append(page).append("',");
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
			if(null != guide){
				builder.append("guide:'" ).append(guide).append("',");
			}
			builder.append("auto:").append(auto).append(",");
			builder.append("type:").append(type).append(",");
			builder.append("style:'").append(style).append("',");
			if(empty == null){
				empty = "没有更多内容了";
			}
			if(null != empty){
				builder.append("empty:'" ).append(empty).append("',");
			}

			builder.append("jump:").append(jump).append(",");
			builder.append("stat:").append(stat).append(",");
			builder.append("creater:'").append(creater).append("'");
			builder.append("};\n");
			if(BasicUtil.isNotEmpty(function)){
				builder.append("function ").append(function).append("(clear,hold){\n");
				builder.append("if(clear){").append(confId).append("['clear'] = 1;}\n");
				builder.append("var _cur_page = 1;\n");
				builder.append("if(hold){_cur_page = $('#hid_cur_page_"+confId+"').val()}\n");
				builder.append("_navi_init("+confId+",_cur_page);\n");
				builder.append("if(clear){").append(confId).append("['clear'] = 0;}\n");
				builder.append("}\n");
				if(intime){
					builder.append(function).append("(true);\n");
				}
			}else{
				builder.append("_navi_init("+confId+");\n");
			}
			//自动加载
			String scrollEventSrc = "window";
			if(BasicUtil.isNotEmpty(scroll)){
				scrollEventSrc = "'" + scroll + "'";
			}
			builder.append("var scroll = $('"+scroll+"');\nif(scroll.length==0){scroll = window}else{scroll=scroll[0]}\n");
			builder.append("$("+scrollEventSrc+").scroll(function(){_navi_auto_load(").append(confId).append(",scroll").append(");});\n");
			builder.append("</script>");
			builder.append("</div>");
			idx ++;
			pageContext.getRequest().setAttribute("_anyline_navi_tag_idx", idx + "");
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
		container 		= null	;	//返回内容容器
		callback 		= null	;	//回调函数
		body			= null	;
		page			= null	;
		bodyContainer 	= null	;	//如果body与page分开
		naviContainer 	= null	;	//如果body与page分开
		empty 			= null	;	//空数据显示内容
		intime 			= false	;
		url 			= null	;
		id 				= null	;
		after			= null	;
		before			= null	;
		guide			= null	;
		function		= null	;
		type			= 0		;
		creater			= "ajax";
		stat			= false	;
		jump			= false	;
		auto			= false	;
		style			= "default"	;
		scroll 			= null;
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
	public String getGuide() {
		return guide;
	}
	public void setGuide(String guide) {
		this.guide = guide;
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
	public String getCreater() {
		return creater;
	}
	public void setCreater(String creater) {
		this.creater = creater;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public Boolean getStat() {
		return stat;
	}
	public void setStat(Boolean stat) {
		this.stat = stat;
	}
	public Boolean getJump() {
		return jump;
	}
	public void setJump(Boolean jump) {
		this.jump = jump;
	}
	public Boolean getAuto() {
		return auto;
	}
	public void setAuto(Boolean auto) {
		this.auto = auto;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getScroll() {
		return scroll;
	}
	public void setScroll(String scroll) {
		this.scroll = scroll;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}

}
