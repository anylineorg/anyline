

package org.anyline.tag.seo;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SeoUtil;
import org.anyline.util.WebUtil;

/**
 * 随机插入关键词
 * 按星期(一年中的第几个星期)计算插入位置
 * @author Administrator
 *
 */
public class Keywords extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Keywords.class);
	//<seo:keyword key="java" count="3"/>
	//<ic:param key ="" count="3"/>

	
	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	 public int doEndTag() throws JspException {
		JspWriter out = null;
		try{
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			boolean insert = false;	//是否插入关键词
			insert = ConfigTable.getBoolean("SEO_INSERT_KEYWORDS",insert);
			insert = ConfigTable.getBoolean("SEO_INSERT_KEYWORDS_"+request.getServerName(),insert);
			
			if(insert && WebUtil.isSpider(request)){
				List<String> keys = new ArrayList<String>();
				if(null != paramList){
					for(Object item:paramList){
						if(null != item){
							keys.add(item.toString().trim());
						}
					}
				}
				body = SeoUtil.insertKeyword(body, keys);
			}
			out = pageContext.getOut();
			out.print(body);
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
		body = null;
	}

	public BodyContent getBodyContent() {
		return super.getBodyContent();
	}

	public void setBodyContent(BodyContent b) {
		super.setBodyContent(b);
	}

}