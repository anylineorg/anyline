/*
 * Copyright 2006-2023 www.anyline.org
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
 */




package org.anyline.web.tag;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.MoneyUtil;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

public class Concat extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private Object data;
	private String split = ",";
 
	public int doEndTag() {
		JspWriter writer = null;
		String result = "";
		try {
			if(data instanceof String){
				String[] items = data.toString().split(",");
				result = BeanUtil.concat(items, split);
			}
			writer = pageContext.getOut();
			writer.print(result);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			release();
		}
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容 
	} 
 
	@Override 
	public void release() {
		super.release();
		data = null;
		split = ",";
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}
}
