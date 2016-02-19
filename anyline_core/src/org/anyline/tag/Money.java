

package org.anyline.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;

public class Money extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Money.class);

	public int doEndTag() {
		String src = BasicUtil.nvl(value,body,"").toString().trim();
		if("".equals(src)){
			return EVAL_BODY_INCLUDE;
		}

		JspWriter writer = null;
		String result = "";
		try {
			writer = pageContext.getOut();
			double d = BasicUtil.parseDouble(value, 0d);
			result = BasicUtil.moneyUpper(d);
			writer.print(result);
		} catch (IOException e) {
			LOG.error(e);
		}finally{
			release();
		}
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容
	}

	@Override
	public void release() {
		super.release();
		value = null;
	}
	
}
