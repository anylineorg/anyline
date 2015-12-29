
package org.anyline.struts.action;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Procedure;
import org.anyline.config.db.impl.ProcedureImpl;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;

@ParentPackage("def-web")
@Namespace("/")
@Scope("prototype")
public class DefaultAction extends AnylineAction{
	private static final long serialVersionUID = 1L;
	public String dir = "WEB-INF/offical/web/home/page";
	@Actions({
			@Action(value = "", results = { @Result(location = "/${dir}/index.jsp") }),
			@Action(value = "index", results = { @Result(location = "/${dir}/index.jsp") })
	})
	public String index(){
		DataSet set ;
//		//DataSet set = service.querySQL("SELECT * FROM ABOUTUS","atp_id:30","aus_url:0");
//		set = service.query("aboutus", "atp_id:30","aus_id:5");
//		//set = service.query(null,"hr.member:test", parseConfig(true,"bor_id:bosr"));
		List<String> list = new ArrayList<String>();
		list.add("123");
		list.add("343");
//		set = service.query("hr.member:test", 
//				parseConfig("bor_id.bor:[bor]").addCondition("cd", list),
//				"atp_id:22");
		set = service.query("test");
	//	System.out.println(set);
//		String sql = "UPDATE ABOUTUS SET AUS_DESCRIPTION ='TESddddddddddddddddd7T' WHERE aus_Id= :id";
//		service.execute(sql,"id:5");
//		Procedure proc = new ProcedureImpl("pro_io").addInput("1234").regOutput();
//		List<Object> result = service.executeProcedure(proc);
//		System.out.println("result:"+result.get(0));
		//set = service.queryProcedure("pro_rs");
		

//		DataRow row = new DataRow();
//		row.addPrimaryKey("CD");
//		row.put("TITLE", "TITLE");
//		service.save("test1", row);
		
		return success();
	}
	/*
	 * xml定义 in
	 * <condition id="CD"> CD IN(:CD)</condtion>parseConfig("CD:[cd]")
	 * <condition id="CD"> CD IN(:CD1)</condtion> parseConfig("CD.CD1:[cd]")
	 * parseConfig("CD:[cd]")
	 * parseConfig().addConditions("CD", List);
	 * 
	 * Table in
	 * parseConfig("CD:[cd]")
	 * parseConfig().addConditions("CD", List);
	 * 
	 * TABLE >= 
	 * parseConfig("CD:>=cd")
	 * 
	 * 
	 * 动态添加集合条件
	 * parseConfig().addCondition(key,value);
	 * 添加集合条件时经常用到 where cd in()
	 * */
	
	
	/**
	 * 部门列表
	 * @return
	 */
	@Action(value = "list", results = { @Result(location = "/${dir}/list.jsp") })
	public String list() {
		DataSet set;
//		set = service.query(null, "aboutus", parseConfig(true,"+atp_id:atp").order("ATP_ID"),"aus_savePersonId:0");
//		System.out.println(set.size()+":"+set.toString());
		set = service.query(null,"hr.member:test", parseConfig(true,"aus_id:atp"));
		System.out.println(set.size()+":"+set.toString());
//		set = service.query(null,"hr.member:test",null);
//		System.out.println(set.size()+":"+set.toString());
		request.setAttribute("set", set);
		com.opensymphony.xwork2.config.providers.XmlConfigurationProvider s;
		return success();
	}
	/**
	 * 成长历程
	 * @return
	 */
	@Action(value="history", results={@Result(location="history")})
	public String history(){
		return success();
	}
	/**
	 * 下载
	 * @return
	 */
	@Action(value="download", results={@Result(location="history")})
	public String download(){
		return success();
	}
	/**
	 * 擅长于-适用场景
	 * @return
	 */
	@Action(value="apt", results={@Result(location="history")})
	public String apt(){
		return success();
	}
	/**
	 * License
	 * @return
	 */
	@Action(value="license", results={@Result(type="template",location="/${dir}/license.jsp")})
	public String license(){
		return success();
	}
	/**
	 * About
	 * @return
	 */
	@Action(value="about", results={@Result(type="template",location="/${dir}/about.jsp")})
	public String about(){
		return success();
	}
}
