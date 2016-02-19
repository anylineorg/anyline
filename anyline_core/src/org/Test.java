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
 */
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
