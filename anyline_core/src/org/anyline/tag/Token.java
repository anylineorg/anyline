

package org.anyline.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.util.TokenUtil;

public class Token extends BodyTagSupport{
	private static final long serialVersionUID = 1L;
	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}

	 public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String token = TokenUtil.createToken(request);
		if(null != token){
			JspWriter out = pageContext.getOut();
			try{
				out.println(token);
			}catch(Exception e){
				
			}finally{
				
			}
		}
        return EVAL_BODY_INCLUDE;
    }   
	public int doEndTag() throws JspException {   
		 return EVAL_PAGE;   
	}
}