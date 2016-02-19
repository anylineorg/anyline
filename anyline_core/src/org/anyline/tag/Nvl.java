

package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;


public class Nvl extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Nvl.class);
	
	 public int doEndTag() throws JspException {
		try{
			for(Object result:paramList){
				if(null != result){
					JspWriter out = pageContext.getOut();
					out.println(result.toString());
					break;
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
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}