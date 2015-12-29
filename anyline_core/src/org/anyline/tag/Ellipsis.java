
package org.anyline.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;

public class Ellipsis extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789,.?'_-=+!@#$%^&*() ";
	private Logger log = Logger.getLogger(this.getClass());
	private int length;					//结果长度
	private String replace = "...";		//替换字符

	public int doEndTag() {
		String src = BasicUtil.nvl(value,body,"").toString().trim();
		if("".equals(src)){
			return EVAL_BODY_INCLUDE;
		}

		JspWriter writer = null;
		String result = "";
		try {
			writer = pageContext.getOut();
			int size = length * 2;
			String chrs[] = src.split("");
			int cnt = 0;
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
			if(result.length() < src.length()){
				result += replace;
				result = "<label title='"+src+"'>" + result + "</label>";
			}
			writer.print(result);
		} catch (IOException e) {
			log.error(e);
		}finally{
			release();
		}
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容
	}

	@Override
	public void release() {
		super.release();
		value = null;
		length = 0;
		replace = "...";
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}
}
