/*  
 * Copyright 2006-2020 www.anyline.org
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

import javax.servlet.http.HttpServletResponse;

import org.anyline.entity.DataRow;
import org.anyline.web.util.WebUtil;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
 
public class ImageResult extends StrutsResultSupport { 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(ImageResult.class); 
	private Object data = null; 
 
	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception { 
		HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE); 
		try { 
 
			File file = null; 
			String title = null; 
			data = invocation.getStack().findValue("data"); 
			if (data instanceof File) { 
				file = (File) data; 
				title = file.getName(); 
			} else if (data instanceof DataRow) { 
				DataRow row = (DataRow) data; 
				file = new File(row.getString("PATH")); 
				title = row.getString("NM"); 
			} 
			if (null != file && file.exists()) { 
				WebUtil.download(response, file, title); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
 
} 
