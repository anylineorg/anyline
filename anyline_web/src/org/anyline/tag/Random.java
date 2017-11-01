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

import org.anyline.util.BasicUtil;

public class Random extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private String length;
	private String begin;
	private String end;
	
	public int doEndTag() {

		JspWriter writer = null;
		String result = "";
		try {
			int _begin = BasicUtil.parseInt(begin, 0);
			int _end = BasicUtil.parseInt(end, 0);
			if(_begin != _end){
				result = BasicUtil.getRandomNumber(_begin, _end)+"";
			}else{
				int size = BasicUtil.parseInt(length, 0);
				if(size>0){
					result = BasicUtil.getRandomNumberString(size);
				}
			}
			writer = pageContext.getOut();
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
		length = "0";
		begin = null;
		end = null;
	}


	public String getBegin() {
		return begin;
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}
	
}
