
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
	Logger log = Logger.getLogger(FileResult.class);
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
				log.info("在正传输文件:"+file.getAbsolutePath()+",请求来自"+request.getRequestURL()+"?"+request.getQueryString());
				while ((count = in.read(buf)) >= 0) {
					out.write(buf, 0, count);
				}
				log.info("传输完成:"+file.getAbsolutePath()+",请求来自"+request.getRequestURL()+"?"+request.getQueryString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null != in)
				in.close();
			if(null != out)
				out.close();
		}
	}

}
