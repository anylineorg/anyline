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
 *          
 */


package org.anyline.web.tag; 
 
 
import org.anyline.util.BasicUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
 
 
public class If extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	 
	private boolean test; 
	private boolean skip = false;//如果test=false时是否跳过body体(不再执行boyd中的子标签) skip=false时即使test=false标签体也会执行
	private Object elseValue; 

	public int doStartTag(){
		if(skip && !test){
            return SKIP_BODY;
		}else {
            return EVAL_BODY_BUFFERED;
        }
	} 
	 public int doEndTag() throws JspException { 
		try{
			if(test){
				JspWriter out = pageContext.getOut(); 
				out.print(BasicUtil.nvl(value,body,"")); 
			}else if(null != elseValue){ 
				JspWriter out = pageContext.getOut(); 
				out.print(elseValue); 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		}finally{ 
			release(); 
		}
		return EVAL_PAGE ; 
	} 
 
 
	@Override 
	public void release() { 
		super.release(); 
		value = null;
		body = null;
		test = false; 
		elseValue = null; 
		skip = false;
	} 
	 
	public void setTest(boolean test) { 
		this.test = test; 
	} 
	public void setElse(Object elseValue) { 
		this.elseValue = elseValue; 
	}
	public boolean getTest(){
		return test;
	} 
	 
	public boolean isSkip() {
		return skip;
	}
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
