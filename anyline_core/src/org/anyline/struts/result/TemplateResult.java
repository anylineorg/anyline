/* 
 * Copyright 2006-2015 the original author or authors.
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
 */
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
		String templet = ConfigTable.getString("TEMPLET_FILE_PATH_WEB");
		if (WebUtil.isWap(request)) {
			templet = ConfigTable.getString("TEMPLET_FILE_PATH_WAP");
		}
		if (null == templet) {
			templet = ConfigTable.getString("TEMPLET_FILE_PATH");
		}
		PageContext pageContext = ServletActionContext.getPageContext();
		if (pageContext != null) {
			pageContext.include(templet);
		} else {
			HttpServletResponse response = ServletActionContext.getResponse();
			RequestDispatcher dispatcher = request
					.getRequestDispatcher(templet);
			request.setAttribute("content_page", contentPage);
			if (dispatcher == null) {
				response.sendError(404,
						(new StringBuilder()).append("result '")
								.append(templet).append("' not found")
								.toString());
				return;
			}
			if (!response.isCommitted()
					&& request
							.getAttribute("javax.servlet.include.servlet_path") == null) {
				request.setAttribute("struts.view_uri", templet);
				request.setAttribute("struts.request_uri",
						request.getRequestURI());
				dispatcher.forward(request, response);
			} else {
				dispatcher.include(request, response);
			}
		}
	}
}
