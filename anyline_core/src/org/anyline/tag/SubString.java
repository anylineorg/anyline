

package org.anyline.tag;


import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.I18NUtil;


public class SubString extends BaseBodyTag{
	private static final long serialVersionUID = 1554109844585627661L;
	
	private int begin = -1;
	private int end = -1;
	
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	 public int doEndTag() throws JspException {
		//输出
		JspWriter out = pageContext.getOut();
		String text = body;
		if(null != text){
			if(begin < 0){
				begin = 0;
			}
			if(end > text.length() || end < 0){
				end = text.length();
			}
			text = text.substring(begin,end);
			try{
				out.print(text);
			}catch(Exception e){
	
			}finally{
				release();
			}
		}
        return EVAL_PAGE;   
	}
	@Override
    public void release(){
		super.release();
    	begin = -1;
    	end = -1;
    }
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	

}