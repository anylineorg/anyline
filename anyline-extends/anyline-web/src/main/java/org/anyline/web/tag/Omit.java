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

import org.anyline.util.BasicUtil;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
 
public class Omit extends BaseBodyTag {
	private static final long serialVersionUID = 1L; 
	private Integer right;
	private Integer left;
	private String ellipsis = "*";
	private Integer max;//最大长度(不小于 right+left+1)
	private Integer min;//最小长度(不小于 right+left+1)
	private Integer vol = 0; //个段最大长度,超出 vol 的拆成多段(vol大于1时有效)
	private String value;
	 
	public int doEndTag() {
		String src = BasicUtil.nvl(value, body, "").trim();
		if(BasicUtil.isEmpty(src)) {
			return EVAL_BODY_INCLUDE; 
		} 
 
		JspWriter writer = null; 
		String result = ""; 
		try {
			writer = pageContext.getOut();
			if(null == vol){
				vol = 0;
			}
			int len = src.length();
			if(null == max || max < 0 || max>len) {
				max = len;
			}

			if(null == left || left<0) {
				left = 0;
			}
			if(left > len) {
				left = len;
			}
            if(null == right || right<0) {
				right = 0;
			}

			if(null == min ) {
				min = 0;
			}
			if(min < left+right) {
				min = left+right+1;
			}
 			if(src.length() > max) {
				 src = src.substring(0, max);
			}
			result = BasicUtil.omit(src, vol, left, right, ellipsis);
			if(result.length() < min) {
				result = BasicUtil.fillChar(result, min-result.length());
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
		body = null;
		left = null;
		right = null;
		max = null;
		min = null;
		vol = 0;
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

	public Integer getVol() {
		return vol;
	}

	public void setVol(Integer vol) {
		this.vol = vol;
	}
}
