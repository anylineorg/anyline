
package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * http request 请求参数名加密
 * @author Administrator
 *
 */
public class DESHttpRequestParamKey extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DESHttpRequestParamKey.class);
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut();
			out.print(WebUtil.encryptHttpRequestParamKey(BasicUtil.nvl(value,body,"").toString()));
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
		value = null;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}