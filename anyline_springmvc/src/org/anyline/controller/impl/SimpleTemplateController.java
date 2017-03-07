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
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
 

public class SimpleTemplateController extends TemplateController {


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