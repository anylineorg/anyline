

package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * http request 请求参数值加密
 * @author Administrator
 *
 */
public class DESHttpRequestParamValue extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut();
			out.print(WebUtil.encryptHttpRequestParamValue(BasicUtil.nvl(value,body,"").toString()));
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}