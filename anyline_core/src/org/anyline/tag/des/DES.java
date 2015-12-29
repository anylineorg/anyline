
package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.DESUtil;
/**
 * 加密
 * @author Administrator
 *
 */
public class DES extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DES.class);
	private String key;			//密钥
	private String value;		//被加密数据

	public int doEndTag() throws JspException {   
		try{
			value = BasicUtil.nvl(value,body,"").toString().trim();
			value = DESUtil.getInstance(key).encrypt(value);
			JspWriter out = pageContext.getOut();
			out.print(value);
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
		key = null;
		value = null;
	}
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}