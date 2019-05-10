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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class StringResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(StringResult.class);
	private Object data = null;


    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
    	response.setContentType("text/plain; charset=UTF-8");
    	response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        try {
        	//提取Action的传入值
        	data = invocation.getStack().findValue("data");
        	if(null == data){
        		data = "";
        	}
        	writer.print(data.toString());
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
	}

}
