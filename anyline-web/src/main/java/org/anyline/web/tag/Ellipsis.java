/* 
 * Copyright 2006-2022 www.anyline.org
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Ellipsis extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789,.?'_-=+!@#$%^&*() "; 
	private int length;					//结果长度 
	private String replace = "...";		//替换字符
	private boolean toggle = false; 
 
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
				if(toggle){
					//点击显示全部
					String random = BasicUtil.getRandomLowerString(10);
					String all = "<span style='display:none;' id='tga_" + random + "'>" + src + "</span>";
					String sub = "<span id='tgs_" + random + "'>"+result+"<span style='display:inline;' onclick=\"$('#tgs_"+random+"').hide();$('#tga_"+random+"').show();\">" + replace + "</span></span>";
					result = all + sub;
				}else{ 
					result += replace; 
					result = "<label title=\""+src+"\">" + result + "</label>";
				} 
			}else{
				result = "<label title=\""+src+"\">" + src + "</label>";
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
		length = 0; 
		replace = "...";
		toggle = false; 
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

	public boolean isToggle() {
		return toggle;
	}

	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}
	 
} 
