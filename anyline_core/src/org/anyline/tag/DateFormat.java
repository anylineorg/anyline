
package org.anyline.tag;


import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.DateUtil;


public class DateFormat extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());
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
			if(null == format){
				format = "yyyy-MM-dd";
			}
			if(null == value){
				value = body;
			}
			if(null == value){
				value = new Date();
				result = DateUtil.format(new Date(), format);
			}else if(value instanceof String){
				result = DateUtil.format((String)value,format);
			}else if(value instanceof Date){
				result = DateUtil.format((Date)value,format);
			}
			JspWriter out = pageContext.getOut();
			out.print(result);
		}catch(Exception e){
			log.error(e);
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