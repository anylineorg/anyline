
package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * 整体加密url
 * @author Administrator
 *
 */
public class DESUrl extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			value = BasicUtil.nvl(value,body,"").toString().trim();
			if(null != value && !"".equals(value)){
				String result = "";
				String url = value;
				String split = "";
				String param = "";
				if(value.contains("?")){
					url = value.substring(0, value.indexOf("?"));
					param = value.substring(value.indexOf("?")+1);
					split = "?";
				}
				result = url + split + WebUtil.encryptRequestParam(param);
				JspWriter out = pageContext.getOut();
				out.print(result);
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
		value = null;
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
