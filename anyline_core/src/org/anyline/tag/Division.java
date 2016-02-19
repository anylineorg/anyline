

package org.anyline.tag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;
/**
 * 除法运算 主要处理0被除数异常 及格式化
 * @author Administrator
 *
 */
public class Division extends BodyTagSupport{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Division.class);
	private String divisor;		//除数
	private String dividend;	//被除数
	private String format;
	private String defaultValue = "";

	 public int doStartTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut();
			Double _divisor = BasicUtil.parseDouble(divisor, null);
			Double _dividend = BasicUtil.parseDouble(dividend, null);
			if(BasicUtil.isNotEmpty(_divisor) && BasicUtil.isNotEmpty(_dividend) && 0 != _dividend){
				double result = _dividend/_divisor;
				if(null != format){
					defaultValue = BasicUtil.formatNumber(result, format);
				}else{
					defaultValue = result +"";
				}
			}
			out.print(defaultValue);
		}catch(Exception e){
			LOG.error(e);
		}finally{
			release();
		}
        return EVAL_BODY_INCLUDE;
    }   
	public int doEndTag() throws JspException {   
	        return EVAL_PAGE;   
	}
	@Override
	public void release() {
		super.release();
		defaultValue = "";
		divisor = null;
		dividend = null;
		format = null;
	}

	public String getDivisor() {
		return divisor;
	}

	public void setDivisor(String divisor) {
		this.divisor = divisor;
	}

	public String getDividend() {
		return dividend;
	}

	public void setDividend(String dividend) {
		this.dividend = dividend;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
