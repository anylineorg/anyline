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
package org.anyline.plugin.springmvc; 
 
import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.JstlView;
 
public class TemplateView extends JstlView {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	public static final String ANYLINE_TEMPLATE_NAME				= "template_name";
	public static final String ANYLINE_STYLE_TEMPLATE_DES			= "style_template_des";
	public static final String ANYLINE_TEMPLATE_NAME_DEFAULT		= "default";
	public static final String ANYLINE_TEMPLATE_CONTENT_PATH 		= "anyline_template_content_path";
	
	
	private String template;
	private String content;

	public TemplateView(){
		super();
	}
	public TemplateView(String name){
		super(name);
	}
	public TemplateView(String content, String template){
		super(content);
		this.content = content;
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}



	public void setTemplate(String template) {
		this.template = template;
	}



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void render(Map model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		

		String template = null;
		if(null != model){
			//通过ModelAndView.add(TEMPLATE_NAME,"default");形式设置模板
			template = (String)model.get(ANYLINE_TEMPLATE_NAME);
		}
		if(null == template){
			//通过 new TemplateView(url,template);设置模板
			template = getTemplate();
		}
		if(null != template){
			String content_url = getContent();//模板文件中引用的实际内容路径
			if(null == content_url || content_url.trim().equals("")){
				content_url = getUrl();
			}
			if(null == content_url || content_url.trim().equals("")){
				content_url = getBeanName();
			}
			String template_url = "";
			String prefix = content_url.substring(0, content_url.indexOf(getBeanName()));
			if(!template.contains(prefix) && !template.startsWith("/")){
				template_url = prefix + template;
			}else{
				template_url = template;
			}
			setUrl(template_url);
			setContent(content_url);
			File file = new File(ConfigTable.getWebRoot(), content_url);
			if(!file.exists()){
				//内容文件未指定  或 不存在 
				log.error("[文件不存在]:[url:{}]", content_url);
				content_url = ConfigTable.getString("DEFAULT_CONTENT_PAGE_PATH");
			}
			request.setAttribute(ANYLINE_TEMPLATE_CONTENT_PATH, content_url);
			request.setAttribute("template_content_path", content_url);
		}
		super.render(model, request, response);
	} 
} 
