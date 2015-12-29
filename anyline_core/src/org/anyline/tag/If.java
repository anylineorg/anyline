
package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;


public class If extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());
	
	private boolean test;
	private Object elseValue;

	 public int doEndTag() throws JspException {
		try{
			if(test){
				JspWriter out = pageContext.getOut();
				out.println(BasicUtil.nvl(value,body,""));
			}else if(null != elseValue){
				JspWriter out = pageContext.getOut();
				out.println(elseValue);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}


	@Override
	public void release() {
		super.release();
		value = null;
		elseValue = null;
	}
	
	public void setTest(boolean test) {
		this.test = test;
	}
	public void setElse(Object elseValue) {
		this.elseValue = elseValue;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}