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
package org.anyline.plugin.springmvc;

import org.anyline.util.WebUtil;
import org.springframework.web.servlet.ModelAndView;

public class TemplateModelAndView extends ModelAndView{
	public static final String DATA_URL 		= "anyline_template_data_url";			//加载数据URL 
	public static final String STYLE_URL	 	= "anyline_template_style_url"; 	//加载数据模板文件URL
	public static final String DATA_PARSER 		= "anyline_template_data_parser";  		//模板文件解析配置
	public static final String CONTENT_URL 		= "anyline_template_content_url"; 		//内容页面加载URL
	public static final String VPT_PAGE_TITLE	= "VPT_PAGE_TITLE";						//页面标题
	
	private String fromClass = "";	//创建视图的类
	
	public TemplateModelAndView setTitle(String title){
		addObject(VPT_PAGE_TITLE, title);
		return this;
	}
	public TemplateModelAndView setData(String url){
		if(null != url && !url.startsWith("/")){
			String base = (String)getModel().get(CONTENT_URL);
			base = base.substring(0,base.lastIndexOf("/"));
			if(null != base){
				if(base.endsWith("/")){
					url = base + url;
				}else{
					url = base + "/" + url;
				}
			}
		}
		this.addObject(DATA_URL, url);
		return this;
	}
	/**
	 * 数据模板目录
	 * 文件名:方法名(String)
	 * 或文件名:类名.方法名(String)
	 * @param template
	 * @return
	 */
	public TemplateModelAndView setStyle(String template){
		
		try{
			String data_template= createFullTemplatePath(template);
			addObject(TemplateView.STYLE_TEMPLATE_DES, WebUtil.encrypt(data_template));
		}catch(Exception e){
			e.printStackTrace();
		}
		return this;
	}
	/**
	 * 构造完整path
	 * @param path
	 * @return
	 */
	public String createFullTemplatePath(String path){
		String viewName = this.getViewName();
		String data_template = viewName.substring(0,viewName.lastIndexOf("/")+1).replace("/page/", "/template/style/")+path;
		int idx = data_template.indexOf(":");
		if(idx > 0){
			String method = data_template.substring(idx+1);
			if(method.indexOf(".") == -1){
				data_template = data_template.replace(method, fromClass+"."+ method);
			}
		}
		return data_template;
	}
	public TemplateModelAndView setDataParser(String ... parser){
		String str = "{";
		int size = parser.length;
		for(int i=0; i<size; i++){
			String p =parser[i];
			str += "'"+p.replace(":", "':'")+"'";
			if(i<size-1){
				str += ",";
			}
		}
		str += "}";
		this.addObject(DATA_PARSER, str);
		return this;
	}
	public String getFromClass() {
		return fromClass;
	}
	public void setFromClass(String fromClass) {
		this.fromClass = fromClass;
	}
	
	
}
