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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import com.opensymphony.xwork2.ActionInvocation;

public class JSONResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());
	private boolean result = true;
	private Object data = null;
	private String message = null;
	
	
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
    	response.setContentType("text/json; charset=UTF-8");
    	response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        try {
        	//提取Action的传入值
        	data = invocation.getStack().findValue("data");
        	result = BasicUtil.parseBoolean(invocation.getStack().findValue("result"),true);
        	message = invocation.getStack().findString("msg");
        	//转换成JSON格式
        	//JsonConfig config = new JsonConfig();
        	String dataType   = null; 	//数据类型
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
			}else if (data instanceof DataRow) {
				dataType = "map";
			}else if(data instanceof Map){
				dataType = "map";
			}else if(data instanceof String){
				dataType = "string";
				data = BasicUtil.convertJSONChar(data.toString());
			}else if(data instanceof Number){
				dataType = "number";
				data = BasicUtil.convertJSONChar(data.toString());
			}else{
				dataType = "map";
			}
        	Map<String,Object> map = new HashMap<String,Object>();
        	map.put("type", dataType);
        	map.put("result", result);
        	map.put("message", message);
        	map.put("data", data);
        	map.put("success", result);
        	
        	JSON json = JSONObject.fromObject(map);
        	writer.print(json.toString());
        }catch(Exception e){
        	log.error(e);
        }finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
	}
	
}
