/* 
 * Copyright 2006-2020 www.anyline.org
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
 *          
 */


package org.anyline.web.tag; 
 
 
import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Replace extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private String from;
	private String separate;
	private String to;

	 public int doEndTag() throws JspException {
		 String src = BasicUtil.nvl(value,body,"").toString().trim();
		 if(BasicUtil.isEmpty(src)){
			 return EVAL_BODY_INCLUDE;
		 }
		 if(null == from || from.length()==0){
			 return EVAL_BODY_INCLUDE;
		 }
		if(BasicUtil.isEmpty(to)){
			to = "";
		}
		JspWriter writer = null;
		try {
			writer = pageContext.getOut();
			String result = "";
			if(null!= separate){
				/**
				 * 以separate分隔from，每个条目换成to,如果to中也有separate并且条目长度与from一致则按顺序替换
				 * value = "ABC123"  from = "ABC" to="a" result = "a123"
				 * value = "ABC123" from = "A,B,C"  separate="," to = "a" result = "aaa123"
				 * value = "ABC123" from = "A,B,C"  separate="," to = "a,b,c" result = "abc123"
				 */
				String froms[] = from.split(separate,-1);
				String tos[] = to.split(separate,-1);

				result = src;
				if(tos.length == froms.length){
					int len = froms.length;
					for(int i=0; i<len; i++){
						result = result.replace(froms[i], tos[i]);
					}
				}else {
					for (String item : froms) {
						result = result.replace(item, to);
					}
				}
			}else {
				result = src.replace(from, to);
			}
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
		from = null;
		to = null;
		separate = null;
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	}

	public String getSeparate() {
		return separate;
	}

	public void setSeparate(String separate) {
		this.separate = separate;
	}

	public String getFrom() {
		return from;
	}


	public void setFrom(String from) {
		this.from = from;
	}


	public String getTo() {
		return to;
	}


	public void setTo(String to) {
		this.to = to;
	}

}
