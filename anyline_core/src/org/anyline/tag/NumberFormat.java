

package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;


public class NumberFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(NumberFormat.class);
	private String format;
	

	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


	public int doEndTag() throws JspException {
		try{
			String result = "";
			if(null == value){
				value = body;
			}
			if(value instanceof String){
				result = BasicUtil.formatNumber((String)value,format);
			}else if(value instanceof Number){
				result = BasicUtil.formatNumber((Number)value,format);
			}
			JspWriter out = pageContext.getOut();
			out.println(result);
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