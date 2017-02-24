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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
 
 

@Controller("org.anyline.controller.impl.FileController")
@RequestMapping("/")
public class FileController extends AnylineController {


	@RequestMapping("ig")
	public String img(HttpServletRequest request, HttpServletResponse response){
		ServletContext sc = request.getSession().getServletContext();
		String table = getUploadTable(getRequest(),null);
		String pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
		DataRow row = service.cacheRow("static_1800",table, parseConfig("+"+pk+":"+pk.toLowerCase()));
		if(null == row){
			return null;
		}
		//D:/upload/anyline
		String uploadDir = ConfigTable.getString("UPLOAD_DIR");
		String fileServer = row.getString("SERVER_HOST");//ConfigTable.getString("FILE_SERVER");
		//转到到文件服务器(根据URL)
		if(BasicUtil.isNotEmpty(fileServer)){
			//D:\\upload\\anyline\\tmp\\894\\IMG_20160903_121158.jpg
			String url = row.getString("SUB_DIR") + row.getString("FILE_NAME");
			url = url.replace("\\", "/").replace(uploadDir, "");
			if(fileServer.endsWith("/") && url.startsWith("/")){
				url = url.substring(1,url.length());
				url = fileServer + url;
			}else if(fileServer.endsWith("/") || url.startsWith("/")){
				url = fileServer + url;
			}else{
				url = fileServer + "/" + url;
			}
			String redirect =  "redirect:"+url;
			log.info("[文件请求已转发][ID:"+row.getString(pk)+"][path:"+row.getString("PATH_ABS")+"][redirect:"+url+"]");
			return redirect;
		}
		//自处理(根据DIR+FILE_NAME)
		File file = null;
		if (row != null) {
			File dir = new File(new File(row.getString("DIR")), row.getString("SUB_DIR"));
			file = new File(dir, row.getString("FILE_NAME"));
		}
		FileInputStream in = null;
		OutputStream out = null;
		try {
			if (null != file && file.exists()) {
				String title = row.getString("TITLE");
				if(null == title){
					title = file.getName();
				}
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Location", title);
				response.setHeader("Content-Disposition", "attachment; filename=" + title);
				String mimeType = sc.getMimeType(file.getAbsolutePath());
				response.setContentType(mimeType);
				in = new FileInputStream(file);
				out = response.getOutputStream();
				byte[] buf = new byte[1024];
				int count = 0;
				if(ConfigTable.isDebug()){
					log.info("在正传输文件:" + file.getAbsolutePath() + ",请求来自" + request.getRequestURL() + "?" + request.getQueryString());
				}
				long fr = System.currentTimeMillis();
				while ((count = in.read(buf)) >= 0) {
					out.write(buf, 0, count);
				}
				if(ConfigTable.isDebug()){
					log.info("传输完成:" + file.getAbsolutePath() + "[耗时:"+(System.currentTimeMillis()-fr)+"],请求来自" + request.getRequestURL() + "?" + request.getQueryString());
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}