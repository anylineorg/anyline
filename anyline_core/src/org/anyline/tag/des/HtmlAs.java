

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
	private static Logger LOG = Logger.getLogger(HtmlAs.class);

	public int doEndTag() throws JspException {
		try{
			String value = body;
			if(null != value && !"".equals(value.trim())){
				value = value.trim();
				JspWriter out = pageContext.getOut();
				out.print(WebUtil.encryptHtmlTagA(value));
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
