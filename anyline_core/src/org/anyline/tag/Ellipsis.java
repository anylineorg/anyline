/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.tag;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;

public class Ellipsis extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789,.?'_-=+!@#$%^&*() ";
	private static Logger log = Logger.getLogger(Ellipsis.class);
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
