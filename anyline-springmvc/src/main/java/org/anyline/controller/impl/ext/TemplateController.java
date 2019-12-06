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
package org.anyline.controller.impl.ext; 
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.controller.impl.AnylineController;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationContextUtils;
  
  

@Controller("org.anyline.controller.impl.ext.TemplateController")
@RequestMapping("/al/tmp")
public class TemplateController extends AnylineController { 

	/**
	 * 加载服务器端文件
	 * path必须以密文提交 &lt;al:des&gt;/WEB-INF/template/a.jsp &lt;/al:des&gt;
	 * 以WEB-INF为相对目录根目录
	 * al.template('/WEB-INF/template/a.jsp',function(result,data,msg){alert(data)});
	 * al.template({path:'template/a.jsp', id:'1'},function(result,data,msg){});
	 * 模板文件中以${param.id}的形式接收参数
	 * 
	 * 对于复杂模板(如解析前需要查询数据)需要自行实现解析方法js中 通过{parser:'/al/tmp/load1.do'}形式指定
	 * @param request request
	 * @param response response
	 * @return return
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
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("加载模板文件开始:"+path);
			}
			html = WebUtil.parseJsp(request, response, path);
			html = BasicUtil.escape(html);
		}catch(Exception e){
			log.warn("加载模板文件失败:"+path+e.getMessage());
		}
		return success(html);
	}
	/**
	 * 加载样式模板
	 * 与load区别是{KEY}形式的变量KEY会加密
	 * 并有可能调用controller方法为模板准备数据(参考parseTemplate)
	 * @param request request
	 * @param response response
	 * @return return
	 */
	@RequestMapping("load_style")
	@ResponseBody
	public String loadDataTemplate(HttpServletRequest request, HttpServletResponse response){
		String template_path = getParam("path", false , true);
		if(null == template_path || template_path.isEmpty()){
			template_path = "default.jsp";
		}
		if(template_path.endsWith("/")){
			template_path = template_path+"default.jsp";
		}
		String content = parseTemplate(request, response,template_path);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("样式模板:"+template_path);
		}
		try{
			List<List<String>> vars= RegularUtil.fetch(content, "{([\\w.]+)}");//RegularUtil.REGEX_VARIABLE
			for(List<String> var:vars){
				String fullVar = var.get(0);
				String simVar = var.get(1).toUpperCase().trim();
				if("ROW_NUMBER".equals(simVar)){
					continue;
				}
				if(simVar.contains(".")){
					String pre = simVar.substring(0,simVar.lastIndexOf("."));
					String suf = simVar.substring(simVar.lastIndexOf(".")+1);
					simVar = pre + "." + WebUtil.encryptKey(suf).toUpperCase(); 
				}else{
					simVar = WebUtil.encryptKey(simVar).toUpperCase();
				}
				content = content.replace(fullVar, "{"+simVar+"}");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		content = BasicUtil.escape(content);
		return success(content);
	}
	
	private String parseTemplate(HttpServletRequest request, HttpServletResponse response, String path){
		String result = "";
		 //构造填充数据的Map 
		if(null == path){
			return result;
		}
		Map map = new HashMap();
		int idx = path.indexOf(":");
		if(idx > 0){
			String cm = path.substring(idx+1);
			path = path.substring(0, idx);
			idx = cm.lastIndexOf(".");
			String clazz = cm.substring(0,idx);
			String method = cm.substring(idx+1);
			String arg = method.substring(method.indexOf("(")+1, method.indexOf(")"));
			method = method.substring(0, method.indexOf("("));
			try{
				Class c = Class.forName(clazz);
				ApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
				
				Object instance = ac.getBean(c);
				Method m = c.getMethod(method, String.class);
				if(null != m){
					map = (Map)m.invoke(instance, arg);
					if(null != map){
						Set keys = map.keySet();
						for(Object key:keys){
							request.setAttribute(key.toString(), map.get(key));
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
        try{
        	File dir = new File(ConfigTable.getWebRoot());
        	File file = new File(dir, path);
        	if(!file.exists()){
        		//文件不存在
        		return result;
        	}
        	result = WebUtil.parseJsp(request, response, path);
        }catch(Exception e){
        	e.printStackTrace();
        }
		return result;
	}
	 
}
