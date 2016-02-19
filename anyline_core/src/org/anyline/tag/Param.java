

package org.anyline.tag;


import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.log4j.Logger;

/**
 * 为上级标签添加参数list 或map格式
 * @author Administrator
 *
 */
public class Param extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Param.class);
	private String key;
	
	 public int doEndTag() throws JspException {
		try{
			Tag parent = this.getParent();
			if(null != parent){
				Method method = parent.getClass().getMethod("addParam",String.class, Object.class);
				if(null != method){
					method.invoke(parent, key, body);
				}
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