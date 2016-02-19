
package org.anyline.struts.result;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.anyline.util.ConfigTable;
import org.anyline.util.WebUtil;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
public class TemplateResult extends StrutsResultSupport {
	private static final long serialVersionUID = 0xe4a6cfd5319c8fc5L;
	private String contentPage = null;

	public TemplateResult() {
		super();
	}

	public TemplateResult(String location) {
		super(location);
	}

	public void doExecute(String finalLocation, ActionInvocation invocation)
			throws Exception {
		this.contentPage = finalLocation;
		Result result = invocation.getResult();
		if (!contentPage.startsWith("/")) {
			String dir = (String) invocation.getStack().findValue("dir");
			if (null != dir) {
				if (!dir.endsWith("/")) {
					dir = dir + "/";
				}
				contentPage = dir + contentPage;
			}
		}
		
		HttpServletRequest request = ServletActionContext.getRequest();
		String template = ""; 
		int idx = contentPage.indexOf("/page/");
		if(idx > 0){
			template = contentPage.substring(0,idx)+"/template/default.jsp";
		}
		if(null == template){
			template = ConfigTable.getString("TEMPLET_FILE_PATH_WEB");
			if (WebUtil.isWap(request)) {
				template = ConfigTable.getString("TEMPLET_FILE_PATH_WAP");
			}
			if (null == template) {
				template = ConfigTable.getString("TEMPLET_FILE_PATH");
			}
		}
		PageContext pageContext = ServletActionContext.getPageContext();
		if (pageContext != null) {
			pageContext.include(template);
		} else {
			HttpServletResponse response = ServletActionContext.getResponse();
			RequestDispatcher dispatcher = request.getRequestDispatcher(template);
			request.setAttribute("content_page", contentPage);
			if (dispatcher == null) {
				response.sendError(404,
						(new StringBuilder()).append("result '")
								.append(template).append("' not found")
								.toString());
				return;
			}
			if (!response.isCommitted()
					&& request
							.getAttribute("javax.servlet.include.servlet_path") == null) {
				request.setAttribute("struts.view_uri", template);
				request.setAttribute("struts.request_uri",
						request.getRequestURI());
				dispatcher.forward(request, response);
			} else {
				dispatcher.include(request, response);
			}
		}
	}
}
