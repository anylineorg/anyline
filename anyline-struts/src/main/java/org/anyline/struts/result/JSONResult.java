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
 
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.apache.struts2.dispatcher.StrutsResultSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
 
public class JSONResult extends StrutsResultSupport { 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(JSONResult.class); 
	private boolean result = true; 
	private Object data = null; 
	private String message = null;
	private String url = null;
	private String code = null; 
	 
	 
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        HttpServletRequest request = (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE); 
    	response.setContentType("text/json; charset=UTF-8"); 
    	response.setCharacterEncoding("UTF-8"); 
        PrintWriter writer = response.getWriter(); 
        try { 
        	//提取Action的传入值 
        	data = invocation.getStack().findValue("data"); 
        	result = BasicUtil.parseBoolean(invocation.getStack().findValue("result"),true);
        	message = invocation.getStack().findString("msg");
        	url = invocation.getStack().findString("url");
        	code = invocation.getStack().findString("code");
        	 
        	//转换成JSON格式 
        	//JsonConfig config = new JsonConfig(); 
        	String dataType   = null; 	//数据类型
        	Map<String,Object> map = new HashMap<String,Object>(); 
        	if(null == data){ 
        		message = (String)BasicUtil.nvl(message, "没有返回数据"); 
        		data = false; 
        	}else if(data instanceof Iterable){ 
				dataType = "list"; 
        	}else if (data instanceof DataSet) { 
        		DataSet set = (DataSet)data; 
        		result = set.isSuccess(); 
        		message = (String)BasicUtil.nvl(message,set.getMessage()); 
				dataType = "list"; 
				data = set.getRows();
				map.put("navi", set.getNavi()); 
			}else if (data instanceof DataRow) { 
				dataType = "map"; 
			}else if(data instanceof Map){ 
				dataType = "map"; 
			}else if(data instanceof String){ 
				dataType = "string"; 
				data = convertJSONChar(data.toString()); 
			}else if(data instanceof Number){ 
				dataType = "number"; 
				data = convertJSONChar(data.toString()); 
			}else{ 
				dataType = "map"; 
			} 
        	map.put("type", dataType); 
        	map.put("result", result); 
        	map.put("message", message);
        	map.put("data", data);
        	map.put("url", url); 
        	map.put("success", result);
    		map.put("code", code);
        	map.put("request_time", request.getParameter("_anyline_request_time"));
        	map.put("response_time_fr", request.getAttribute("_anyline_response_time_fr"));
        	map.put("response_time_to", System.currentTimeMillis());
    		String str = BeanUtil.map2json(map);
        	writer.print(str); 
        }catch(Exception e){
        	e.printStackTrace(); 
        }finally { 
            if (writer != null) { 
                writer.flush(); 
                writer.close(); 
            } 
        } 
	}	
    private String convertJSONChar(String value) {
		if (null != value) {
			value = value.replace("\\", "\\\\").replace("\"", "\\\"");
		}
		return value;
	} 
	 
} 
