
package org.anyline.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;

@ParentPackage("def-web")
@Namespace("/download")
@Scope("prototype")
public class DownloadAction extends AnylineAction{
	private static final long serialVersionUID = 1L;
	public String dir = "/WEB-INF/offical/web/home/page/download/";
	@Actions({
			@Action(value = "", results = { @Result(type="template",location = "index.jsp") }),
			@Action(value = "index", results = { @Result(type="template", location = "index.jsp") })
	})
	public String index(){
		return success();
	}
}
