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

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class FileResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(FileResult.class);
	private Object data = null;

	protected void doExecute(String finalLocation, ActionInvocation invocation)
			throws Exception {
		HttpServletResponse response = (HttpServletResponse) invocation
				.getInvocationContext().get(HTTP_RESPONSE);
		HttpServletRequest request = (HttpServletRequest) invocation
				.getInvocationContext().get(HTTP_REQUEST);
		ServletContext sc = (ServletContext) invocation.getInvocationContext()
				.get(SERVLET_CONTEXT);
		response.setCharacterEncoding("UTF-8");
		FileInputStream in = null;
		OutputStream out = null;
		try {
			data = invocation.getStack().findValue("data");
			File file = (File) data;
			if (null != file && file.exists()) {
				String mimeType = sc.getMimeType(file.getAbsolutePath());
				response.setContentType(mimeType);
				in = new FileInputStream(file);
				out = response.getOutputStream();
				byte[] buf = new byte[1024];
				int count = 0;
				LOG.info("在正传输文件:"+file.getAbsolutePath()+",请求来自"+request.getRequestURL()+"?"+request.getQueryString());
				while ((count = in.read(buf)) >= 0) {
					out.write(buf, 0, count);
				}
				LOG.info("传输完成:"+file.getAbsolutePath()+",请求来自"+request.getRequestURL()+"?"+request.getQueryString());
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			if(null != in)
				in.close();
			if(null != out)
				out.close();
		}
	}

}
