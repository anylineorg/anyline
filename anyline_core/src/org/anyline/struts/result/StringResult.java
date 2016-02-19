

package org.anyline.struts.result;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class StringResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(StringResult.class);
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
        	LOG.error(e);
        }finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
	}

}
