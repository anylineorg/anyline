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
package org.anyline.controller.impl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.plugin.springmvc.TemplateModelAndView;
import org.anyline.plugin.springmvc.TemplateView;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
 
 

@Controller("org.anyline.controller.impl.TemplateController")
@RequestMapping("/al/tmp")
public class TemplateController extends AnylineController {

	/**
	 * 加载服务器端文件
	 * path必须以密文提交 <al:des>/WEB-INF/template/a.jsp</al:des>
	 * 以WEB-INF为相对目录根目录
	 * al.template('/WEB-INF/template/a.jsp',function(result,data,msg){alert(data)});
	 * al.template({path:'template/a.jsp', id:'1'},function(result,data,msg){});
	 * 模板文件中以${param.id}的形式接收参数
	 * 
	 * 对于复杂模板(如解析前需要查询数据)需要自行实现解析方法js中 通过{parser:'/al/tmp/load1.do'}形式指定
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("load")
	@ResponseBody
	public String load(HttpServletRequest request, HttpServletResponse response){
		String path = getParam("path", false, true);
		if(null != path && !path.startsWith("/")){
			path = "/WEB-INF/" + path;
		}
		String html = "";
		try{
			html = WebUtil.parseJsp(request, response, path);
		}catch(Exception e){
			
		}
		html = HttpUtil.escape(html);
		return success(html);
	}
	/**
	 * 创建显示视图(page目录下)
	 * @param name
	 * @param template
	 * @return
	 */
	protected ModelAndView template(String name, String template){
		return createView(name, template);
	}

	private String buildDir(){
		String result = "";
		String dir = (String)BeanUtil.getFieldValue(this, "dir");
		String superDir = null;
		try {
			superDir = (String)BeanUtil.getFieldValue(getClass().getSuperclass().newInstance(), "dir");
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != dir){
			result = dir;
			if(!result.startsWith("/")){
				if(null != superDir){
					if(superDir.endsWith("/")){
						result = superDir + result;
					}else{
						result = superDir + "/" + result;
					}	
				}
			}
		}else{
			if(null != superDir){
				result = superDir;
			}
		}
		if(!result.endsWith("/")){
			result = result + "/";
		}
		return result;
	}
	protected TemplateModelAndView createView(String name, String template){
		TemplateModelAndView tv = new TemplateModelAndView();
		if(null != name && !name.startsWith("/")){
			//相对目录
			name = buildDir() + name;
		}
		String content_template = "";
		if(null != template){
			if(!template.endsWith(".jsp")){
				template += ".jsp";
			}
			if(!template.startsWith("/")){
				if(name.contains("/page/")){
					content_template = name.substring(0, name.indexOf("/page/")) + "/template/layout/" + template;
				}else{
					content_template = buildDir() + "template/layout/" + template;
				}
			}
		}
		tv.setViewName(name);
		tv.addObject(TemplateView.TEMPLATE_NAME, content_template);
		tv.addObject(TemplateModelAndView.CONTENT_URL,getRequest().getRequestURI());
		String style_template = name.substring(0,name.lastIndexOf("/")+1).replace("/page/", "/template/style/");
		try{
			tv.addObject(TemplateView.STYLE_TEMPLATE_DES, DESUtil.getInstance().encrypt(style_template));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String clazz = this.getClass().getName();
		tv.setFromClass(clazz);
		return tv;
	}

	protected TemplateModelAndView createView(String name){
		return createView(name,TemplateView.TEMPLATE_NAME_DEFAULT);
	}
	protected TemplateModelAndView template(String name){
		return createView(name);
	}
	
	/**
	 * 加载数据 数据模板中的数据
	 * @param objects
	 * @return
	 */
	protected String createTemplateData(Object obj, String ... keys){
		BasicUtil.toUpperCaseKey(obj, keys);
		WebUtil.encryptKey(obj, keys);
		return success(obj);
	}
	protected String loadData(Object obj, String ...keys){
		return createTemplateData(obj, keys);
	}
	
	protected ModelAndView error(String ... msgs ){
		return errorView(msgs);
	}
	protected ModelAndView errorView(String ... msgs){
		String message ="";
		String bak_url = getRequest().getHeader("Referer");
		if(null != msgs){
			for(String msg:msgs){
				message += "<br/>"+ msg;
			}
		}
		
		ModelAndView view = new ModelAndView(ConfigTable.getString("ERROR_PAGE_PATH"));
		view.addObject("msg", message);
		view.addObject("bak_url",bak_url);
		return view;
	}
	protected ModelAndView emptyView(){
		ModelAndView view = new ModelAndView(ConfigTable.getString("EMPTY_PAGE_PATH"));
		return view;
	}
}