
package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.RegularUtil;
/**
 * 整体加密
 * @author Administrator
 *
 */
public class HtmlAs extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());

	public int doEndTag() throws JspException {
		try{
			String value = body;
			if(null != value && !"".equals(value.trim())){
				value = value.trim();
				JspWriter out = pageContext.getOut();
				out.print(WebUtil.encryptHtmlTagA(value));
			}
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
