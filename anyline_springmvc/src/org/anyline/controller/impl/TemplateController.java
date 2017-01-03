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
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.plugin.springmvc.TemplateModelAndView;
import org.anyline.plugin.springmvc.TemplateView;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESUtil;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationContextUtils;
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
			if(ConfigTable.isDebug()){
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
	 * @return
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
		if(ConfigTable.isDebug()){
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
	
	/**
	 * 创建显示视图
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
	protected ModelAndView template(String name, String template){
		return createView(name, template);
	}
	/**
	 * 根据dir构造文件目录(super.dir+this.dir)
	 * @return
	 */
	private String buildDir(){
		String result = "";
		String dir = (String)BeanUtil.getFieldValue(this, "dir");
		String superDir = null;
		try {
			superDir = (String)BeanUtil.getFieldValue(getClass().getSuperclass().newInstance(), "dir");
		} catch (InstantiationException | IllegalAccessException e) {
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
	/**
	 * 常用业务方法
	 */

	protected String table = null;
	protected String view = null;
	protected String[] columns = null;
	protected String[] conditions = null;
	protected String[] orders = null;
	protected int page = 0;
	private void init(){
		if(BasicUtil.isEmpty(table)){
			table = (String)BeanUtil.getFieldValue(this, "table");
		}
		if(BasicUtil.isEmpty(view)){
			view = (String)BeanUtil.getFieldValue(this, "view");
		}
		if(BasicUtil.isEmpty(columns)){
			String str = (String)BeanUtil.getFieldValue(this, "columns");
			if(BasicUtil.isNotEmpty(str)){
				columns = str.split(",");
			}
		}
		if(BasicUtil.isEmpty(conditions)){
			String str = (String)BeanUtil.getFieldValue(this, "conditions");
			if(BasicUtil.isNotEmpty(str)){
				conditions = str.split(",");
			}
		}
		if(BasicUtil.isEmpty(orders)){
			String str = (String)BeanUtil.getFieldValue(this, "orders");
			if(BasicUtil.isNotEmpty(str)){
				orders = str.split(",");
			}
		}
		if(page == 0){
			String str = (String)BeanUtil.getFieldValue(this, "page");
			if(BasicUtil.isEmpty(str)){
				page =  ConfigTable.getInt("PAGE_DEFAULT_VOL", 10);
			}else{
				if("false".equalsIgnoreCase(str)){
					page = -1;
				}else if("true".equalsIgnoreCase(str)){
					page =  ConfigTable.getInt("PAGE_DEFAULT_VOL", 10);
				}else{
					page = BasicUtil.parseInt(BeanUtil.getFieldValue(this, "page")+"", ConfigTable.getInt("PAGE_DEFAULT_VOL", 10));
				}
			}
		}
	}
	@RequestMapping("a")
	public ModelAndView add(){
		init();
		ModelAndView mv = template("info.jsp");
		return mv;
	}
	@RequestMapping("l")
	public ModelAndView list(){
		init();
		ModelAndView mv = template("list.jsp");
		ConfigStore config = parseConfig(true);
		if(BasicUtil.isNotEmpty(orders)){
			for(String item:orders){
				config.order(item);
			}
		}
		DataSet set = service.query(view,config);
		WebUtil.encrypt(set, DataRow.PRIMARY_KEY);
		mv.addObject("set",set);
		return mv;
	}
	@RequestMapping("u")
	public ModelAndView update(){
		init();
		ModelAndView mv = template("info.jsp");
		String pk = DataRow.PRIMARY_KEY.toLowerCase();
		DataRow row = service.queryRow(view, parseConfig("+"+pk+":"+pk+"-+"+":"+pk+"++"));
		if(null == row){
			return errorView("数据不存在");
		}
		WebUtil.encrypt(row, DataRow.PRIMARY_KEY);
		mv.addObject("row",row);
		return mv;
	}
	@RequestMapping("s")
	public ModelAndView save(){
		init();
		DataRow row = entityRow(columns);
		if(BasicUtil.isEmpty(row.get(DataRow.PRIMARY_KEY))){
			row.put(DataRow.PRIMARY_KEY, WebUtil.decryptHttpRequestParamValue(row.getString(DataRow.PRIMARY_KEY)));
		}
		service.save(table, row);
		String pk = DataRow.PRIMARY_KEY.toLowerCase();
		String param = pk+"="+row.getPrimaryValue();
		param = WebUtil.encryptRequestParam(param);
		ModelAndView mv = new ModelAndView("redirect:v?"+param);
		return mv;
	}
	@RequestMapping("v")
	public ModelAndView view(){
		init();
		ModelAndView mv = template("view.jsp");
		String pk = DataRow.PRIMARY_KEY.toLowerCase();
		DataRow row = service.queryRow(view, parseConfig("+"+pk+":"+pk+"-+"+":"+pk+"++"));
		if(null == row){
			return errorView("数据不存在");
		}
		WebUtil.encrypt(row, DataRow.PRIMARY_KEY);
		mv.addObject("row",row);
		return mv;
	}
	@RequestMapping("d")
	@ResponseBody
	public String delete(){
		init();
		String pk = DataRow.PRIMARY_KEY.toLowerCase();
		DataRow row = service.queryRow(table, parseConfig("+"+pk+":"+pk+"-+"+":"+pk+"++"));
		if(null == row){
			return fail("数据不存在");
		}
		service.delete(row);
		return success();
	}

}