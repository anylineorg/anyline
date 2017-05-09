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
import org.apache.log4j.Logger;
/**
 * JS切图并上传
 *
 */
public class ImgClip extends BodyTagSupport{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ImgClip.class);

	private String url				;	//上传url
	private int width = 200;
	private int height = 200;
	private String callback;
	private String flag = "";
	public int doStartTag() throws JspException {
		try{
			StringBuilder builder = new StringBuilder();
			int idx = BasicUtil.parseInt((String)pageContext.getRequest().getAttribute("_anyline_imgclip_tag_idx"), 0);

			if(BasicUtil.isEmpty(flag)){
				flag = idx+"";
			}
			if(idx == 0){
				builder.append("<script src=\"http://source.deepbit.cn/plugin/photoclip/js/iscroll-zoom.js\"></script>\n");
				builder.append("<script src=\"http://source.deepbit.cn/plugin/photoclip/js/hammer.js\"></script>\n");
				builder.append("<script src=\"http://source.deepbit.cn/plugin/photoclip/js/jquery.photoClip.js\"></script>\n");
			}
			builder.append("<div id=\"clip_area"+flag+"\" style=\"height: "+(height+100)+"px;\"></div>\n");
			builder.append("<input type=\"file\" id=\"clip_file"+flag+"\" style=\"display:none;\">\n");
			builder.append("<label for=\"clip_file"+flag+"\" class=\"clip_btn btn\" style=\"margin: 20px;\">选择图片</label>\n");
			builder.append("<button id=\"clip_btn"+flag+"\" class=\"clip_btn btn\" style=\"margin: 20px;\">上传图片</button>\n");
			builder.append("<div id=\"clip_view"+flag+"\"></div>\n");
			builder.append("<script>\n");
			builder.append("$(\"#clip_area"+flag+"\").photoClip({\n");
			builder.append("\twidth: "+width+",\n");
			builder.append("\theight: "+height+",\n");
			builder.append("\tfile: \"#clip_file"+flag+"\",\n");
			builder.append("\tview: \"#clip_view"+flag+"\",\n");
			builder.append("\tok: \"#clip_btn"+flag+"\",\n");
			builder.append("\tloadStart: function() {console.log(\"图片读取中\");},\n");
			builder.append("\tloadComplete: function() {console.log(\"图片读取完成\");},\n");
			builder.append("\tclipFinish: function(dataURL) {\n");
			builder.append("\t\tal.ajax({\n");
			builder.append("\t\t\turl:'"+url+"',\n");
			builder.append("\t\t\tdata:{str:dataURL},\n");
			builder.append("\t\t\tcallback:function(result,data,msg){\n");
			builder.append("\t\t\t\t"+callback+"(result,data,msg);\n");
			builder.append("\t\t\t}\n");
			builder.append("\t\t});\n");
			builder.append("\t}\n");
			builder.append("});\n");
			builder.append("\t</script>\n");
			
			idx ++;
			pageContext.getRequest().setAttribute("_anyline_imgclip_tag_idx", idx + "");
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
		url = null;
		width = 200;
		height = 200;
		flag = null;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}

}
