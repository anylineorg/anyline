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
 *          
 */

package org.anyline.struts.result;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.WebUtil;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;

public class FileResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(FileResult.class);
	private Object data = null;

	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
		HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
		HttpServletRequest request = (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);

		try {

			File file = null;
			String title = null;
			data = invocation.getStack().findValue("data");
			if(null == data){
				log.warn("\n\t[文件下载][文件不存在][URL:{}?{}",request.getRequestURL(),request.getQueryString());
				return;
			}
			if (data instanceof File) {
				file = (File)data;
				WebUtil.download(response, file, title);
			} else if (data instanceof DataRow) {
				DataRow row = (DataRow) data;
				String fileServer = row.getString("SERVER_HOST");
				//转到到文件服务器(根据URL)
				if(BasicUtil.isNotEmpty(fileServer)){
					String url = FileUtil.mergePath(row.getString("SUB_DIR"), row.getString("FILE_NAME"));
					//注意http:\\中的\
					url = url.replace("\\", "/").replace("//", "/");
					url = FileUtil.mergePath(fileServer, url);
					log.info("[文件请求已转发][id:{}][redirect:{}]",row.getPrimaryKey(),url);
					response.sendRedirect(url);
				}else{
					String path = FileUtil.mergePath(row.getString("ROOT_DIR"), row.getString("SUB_DIR"), row.getString("FILE_NAME"));
					file = new File(path);
					title = row.getString("TITLE");
					WebUtil.download(response, file, title);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
