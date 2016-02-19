

package org.anyline.tag;


import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;

/**
 * 
 * @author Administrator
 * pageContext.setAttribute(key,value);
 */
public class PageContext extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(PageContext.class);

	private String key;
	
	
	public int doEndTag() throws JspException {
		try{
			if(null != key){
				pageContext.setAttribute(key, BasicUtil.nvl(value,body));
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
		key = null;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}