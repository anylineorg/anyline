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
import org.anyline.util.FileUtil;
import org.anyline.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
 
 

@Controller("org.anyline.controller.impl.FileController")
@RequestMapping("/")
public class FileController extends AnylineController {

	@RequestMapping("ig")
	public String img(HttpServletRequest request, HttpServletResponse response){
		return file(request, response);
	}

	@RequestMapping("fl")
	public String file(HttpServletRequest request, HttpServletResponse response){
		String table = getUploadTable(request,null);
		String pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
		if(BasicUtil.isEmpty(pk)){
			pk = "id";
		}
		DataRow row = service.cacheRow("static_1800",table, parseConfig("+"+pk+":"+pk.toLowerCase()));
		if(null == row){
			log.info("[文件请求][文件不存在]["+pk+":"+request.getParameter(pk.toLowerCase())+"]");
			return null;
		}
		//D:/upload/anyline
		String fileServer = row.getString("SERVER_HOST");
		//转到到文件服务器(根据URL)
		if(BasicUtil.isNotEmpty(fileServer)){
			String url = FileUtil.mergePath(row.getString("SUB_DIR"), row.getString("FILE_NAME"));
			//注意http:\\中的\
			url = url.replace("\\", "/").replace("//", "/");
			url = FileUtil.mergePath(fileServer, url);
			String redirect =  "redirect:"+url;
			log.info("[文件请求已转发][ID:"+row.getString(pk)+"][path:"+row.getString("PATH_ABS")+"][redirect:"+url+"]");
			return redirect;
		}
		//自处理(根据DIR+FILE_NAME)
		File file = null;
		if (row != null) {
			String path = FileUtil.mergePath(row.getString("ROOT_DIR"), row.getString("SUB_DIR"), row.getString("FILE_NAME"));
			file = new File(path);
		}
		if (null != file && file.exists()) {
			String title = row.getString("TITLE");
			if(null == title){
				title = file.getName();
			}
			WebUtil.writeFile(response, file, title);
		}
		return null;
	}

}