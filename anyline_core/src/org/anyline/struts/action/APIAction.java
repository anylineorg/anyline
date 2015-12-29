
package org.anyline.struts.action;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;

@ParentPackage("def-web")
@Namespace("/api")
@Scope("prototype")
public class APIAction extends AnylineAction{
	private static final long serialVersionUID = 1L;
	public String dir = "/WEB-INF/offical/web/home/page/api/";
	@Actions({
		@Action(value = "", results = { @Result(type="template",location = "index.jsp") }),
		@Action(value = "index", results = { @Result(type="template", location = "index.jsp") })
	})
	public String index(){
		DataSet set = service.query("oc.API(CD,TITLE)");
		DataRow row = service.query("oc.API", parseConfig("CD:cd++")).getRow(0);
		String cd = getParam("cd",true,true);
		request.setAttribute("row", row);
		request.setAttribute("set", set);
		return success();
	}

	@Action(value = "a", results = { @Result(type="template", location = "info.jsp") })
	public String add(){
		return success();
	}
	@Action(value = "l", results = { @Result(type="template", location = "list.jsp") })
	public String list(){
		DataSet set = service.query("oc.API(CD,TITLE)");
		request.setAttribute("set", set);
		return success();
	}

	@Action(value = "u", results = { @Result(type="template", location = "info.jsp") })
	public String info(){
		DataRow row = service.query("oc.api", parseConfig("+CD:cd++")).getRow(0);
		request.setAttribute("row", row);
		return success();
	}

	@Action(value = "s", results = { @Result(type="template", location = "info.jsp") })
	public String save(){
		String content = getParam("content");
		DataRow row = entityRow("CD:cd","BASE_CD:base","TITLE:title","IDX:idx");
		if(null != content){
			content = content.replace("<", "&lt;").replace(">", "&gt;");
		}
		row.put("CONTENT", content);
		service.save("oc.API", row);
		
		row.setIsNew(true);
		request.setAttribute("row", row);
		return success();
	}


}
