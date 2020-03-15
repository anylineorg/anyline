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

import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
 
public class Omit extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private Integer right;
	private Integer left;
	private String ellipsis = "*";
	private Integer max;//最大长度(不小于 right+left+1)
	private Integer min;//最小长度(不小于 right+left+1)
	private String value;
	 
	public int doEndTag() { 
		String src = BasicUtil.nvl(value,body,"").toString().trim(); 
		if("".equals(src)){ 
			return EVAL_BODY_INCLUDE; 
		} 
 
		JspWriter writer = null; 
		String result = ""; 
		try {

			writer = pageContext.getOut();
			int len = src.length();
			if(null == max || max < 0 || max>len){
				max = len;
			}

			if(null == min || min < 0){
				min = len;
			}
			if(null == left || left<0){
				left = 0;
			}
			if(left > len){
				left = len;
			}
			if(null == left || left<0){
				left = 0;
			}
			int fill = max - left - right;
			if(fill < 0){
				fill = 0;
			}
			if(fill + left + right < min){
				fill = min - left - right;
			}
			if(left > max){
				left = max;
			}
			if(right > max - left){
				right = max - left;
			}
			String l = src.substring(0,left);
			String r = src.substring(src.length() - right);
			
			result = l+BasicUtil.fillRChar("", ellipsis, fill)+r; 
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
		left = null;
		right = null;
		max = null;
		min = null;
		ellipsis = "*"; 
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public String getEllipsis() {
		return ellipsis;
	}

	public void setEllipsis(String ellipsis) {
		this.ellipsis = ellipsis;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
