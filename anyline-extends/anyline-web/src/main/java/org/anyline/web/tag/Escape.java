/*
 * Copyright 2006-2025 www.anyline.org
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
 
import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
 
public class Escape extends BaseBodyTag {
	private static final long serialVersionUID = 1L; 
 
	public int doEndTag() {
		String src = BasicUtil.nvl(value,body,"").toString().trim(); 
		if(BasicUtil.isEmpty(src)) {
			return EVAL_BODY_INCLUDE; 
		} 
 
		JspWriter writer = null; 
		String result = BasicUtil.escape(src); 
		try {
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
		value = null;
		body = null; 
	} 
	 
} 
