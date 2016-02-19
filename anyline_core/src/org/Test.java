package org;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class Test {
	public static void main(String args[]) throws ServletException, IOException {
		String url = "";
		ServletContext sc = null;
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		String file_name = "D:\\t.jsp";// 你要访问的jsp文件名,如index，不包括扩展名
		// 则你访问这个servlet时加参数.如http://localhost/test/toHtml?file_name=index
		url = "/" + file_name + ".jsf";// 你要生成的页面的文件名。我的扩展名为jsf .
		// ConfConstants.CONTEXT_PATH为你的应用的上下文路径。
		RequestDispatcher rd = sc.getRequestDispatcher(url);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final ServletOutputStream stream = new ServletOutputStream() {
			public void write(byte[] data, int offset, int length) {
				os.write(data, offset, length);
			}

			public void write(int b) throws IOException {
				os.write(b);
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setWriteListener(WriteListener arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		final PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		HttpServletResponse rep = new HttpServletResponseWrapper(response) {
			public ServletOutputStream getOutputStream() {
				return stream;
			}

			public PrintWriter getWriter() {
				return pw;
			}
		};
		rd.include(request, rep);
		pw.flush();
		FileOutputStream fos = new FileOutputStream("D:\\t.html"); // 把jsp输出的内容写到xxx.htm
		os.writeTo(fos);
		fos.close();
		PrintWriter ōut = response.getWriter();
	}
}
