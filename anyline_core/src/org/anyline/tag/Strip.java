

package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.regular.RegularUtil;
/**
 * 清除html标签
 * @author Administrator
 *
 */

public class Strip extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Strip.class);

	private int length = -1;
	private String ellipsis="...";
	private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789,.?'_-=+!@#$%^&*() ";

	public int doEndTag() throws JspException {
		try{
			String result = "";
			if(null != value){
				result = value.toString();
			}else if(null != body){
				result = body;
			}
			result = RegularUtil.removeAllHtmlTag(result);
			if(length != -1){
				int size = length * 2;
				String chrs[] = result.split("");
				int cnt = 0;
				String tmp = result;
				result = "";
				for(String chr:chrs){
					if(cnt >= size){
						break;
					}
					if(SINGLE_CHAR.contains(chr.toLowerCase())){
						cnt += 1;
					}else{
						cnt += 2;
					}
					result += chr;
				}
				if(result.length() < tmp.length()){
					result += ellipsis;
				}
			}
			JspWriter out = pageContext.getOut();
			out.println(result);
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
		length = -1;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
 

	public int getLength() {
		return length;
	}


	public void setLength(int length) {
		this.length = length;
	}


	public String getEllipsis() {
		return ellipsis;
	}


	public void setEllipsis(String ellipsis) {
		this.ellipsis = ellipsis;
	}

}