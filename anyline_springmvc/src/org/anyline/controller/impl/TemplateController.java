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
import org.anyline.plugin.springmvc.TemplateModelAndView;
import org.anyline.plugin.springmvc.TemplateView;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.WebUtil;
import org.springframework.web.servlet.ModelAndView;
 
 

public class TemplateController extends AnylineController {
	/**
	 * 根据dir构造文件目录(super.dir+this.dir)
	 * @return
	 */
	private String buildDir(){
		String result = "";
		try {
			Class clazz = getClass();
			while(null != clazz){
				String dir = (String)BeanUtil.getFieldValue(clazz.newInstance(), "dir", false);
				if(BasicUtil.isNotEmpty(dir)){
					result = HttpUtil.mergePath(dir, result);
				}
				if(result.startsWith("/")){
					break;
				}
				clazz = clazz.getSuperclass();
			}
		} catch (Exception e) {
		}
		if(!result.endsWith("/")){
			result = result + "/";
		}
		return result;
	}

	/**
	 * 创建显示视图
	 * @param adapt 是否识别并切换 移动 PC(web,wap)
	 * @param name
	 * @param template
	 * template(page,template) 与createView(page,template); 相同
		需要注意的是
		page有两种格式相对与绝对
		相对目录时,方法内部会将文件名与目录名拼接
		拼接时,先拼当前类的dir 再拼父类中的dir
		另外:template不指定时template(page)默认为default.jsp
		内容文件与模板文件 目录结构应该保持一致
	 * @return
	 */
	protected TemplateModelAndView template(boolean adapt, String name, String template){
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
		String clientType = "web";
		if(WebUtil.isWap(getRequest())){
			clientType = "wap";
		}
		if(null != name){
			if(adapt){
				name = name.replace("/web/", "/"+clientType+"/");
				name = name.replace("/wap/", "/"+clientType+"/");
			}
			name = name.replace("${client_type}", clientType);
		}
		if(null != content_template){
			if(adapt){
				content_template = content_template.replace("/web/", "/"+clientType+"/");
				content_template = content_template.replace("/wap/", "/"+clientType+"/");
			}
			content_template = content_template.replace("${client_type}", clientType);
		}
		if(ConfigTable.isDebug() && adapt){
			log.warn("[create view template][content path:" + content_template + "][template path:" + name + "]");
		}
		
		tv.setViewName(name);
		tv.addObject(TemplateView.ANYLINE_TEMPLATE_NAME, content_template);
		tv.addObject(TemplateModelAndView.CONTENT_URL,getRequest().getRequestURI());
		String style_template = name.substring(0,name.lastIndexOf("/")+1).replace("/page/", "/template/style/");
		try{
			tv.addObject(TemplateView.ANYLINE_STYLE_TEMPLATE_DES, DESUtil.getInstance().encrypt(style_template));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String clazz = this.getClass().getName();
		tv.setFromClass(clazz);
		return tv;
	}

	protected TemplateModelAndView template(boolean adapt, String name){
		return template(adapt, name, TemplateView.ANYLINE_TEMPLATE_NAME_DEFAULT);
	}

	protected TemplateModelAndView template(String name){
		return template(false, name, TemplateView.ANYLINE_TEMPLATE_NAME_DEFAULT);
	}

	protected TemplateModelAndView template(String name, String template){
		return template(false, name, template);
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
	protected ModelAndView emptyView(String ... msgs){
		String message ="";
		String bak_url = getRequest().getHeader("Referer");
		if(null != msgs){
			for(String msg:msgs){
				message += "<br/>"+ msg;
			}
		}
		
		ModelAndView view = new ModelAndView(ConfigTable.getString("EMPTY_PAGE_PATH"));
		view.addObject("msg", message);
		view.addObject("bak_url",bak_url);
		return view;
	}
	protected ModelAndView emptyView(){
		ModelAndView view = new ModelAndView(ConfigTable.getString("EMPTY_PAGE_PATH"));
		return view;
	}

}