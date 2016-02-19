

package org.anyline.tag;


import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;


public class Else extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Else.class);
	
	 public int doEndTag() throws JspException {
		try{
			Tag parent = this.getParent();
			if(null != parent){
				Method method = parent.getClass().getMethod("setElse", Object.class);
				if(null != method){
					method.invoke(parent, BasicUtil.nvl(value,body));
				}
			}
		}catch(Exception e){
			LOG.error(e);
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}


	@Override
	public void release() {
		super.release();
		value = null;
	}

}