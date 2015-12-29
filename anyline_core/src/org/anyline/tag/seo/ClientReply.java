
package org.anyline.tag.seo;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;

/**
 * 
 * @author Administrator
 * 客户端回复确认(JS形式)
 * 
 */
public class ClientReply extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());

	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	public int doEndTag() throws JspException {
		try{
			String js = "<script type=\"text/javascript\" src=\"/iccr?c="+pageContext.getRequest().hashCode()+"\"></script>";
			pageContext.getOut().write(js);
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

	private String getParamValue(String key){
		if(paramMap != null){
			return (String)paramMap.get(key);
		}
		return null;
	}
}