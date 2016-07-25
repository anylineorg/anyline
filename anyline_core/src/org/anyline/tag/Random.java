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
	
	public int doEndTag() {

		JspWriter writer = null;
		String result = "";
		try {
			int size = BasicUtil.parseInt(length, 0);
			if(size>0){
				writer = pageContext.getOut();
				result = BasicUtil.getRandomNumberString(size);
				writer.print(result);
			}
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
	}


	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}
	
}
