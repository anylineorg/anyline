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


import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;


public class Else extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Else.class);
	
	 public int doEndTag() throws JspException {
		try{
			Tag parent = this.getParent();
			if(null != parent){
				Method method = parent.getClass().getMethod("setElse", Object.class);
				if(null != method){
					method.invoke(parent, BasicUtil.nvl(value,body));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}


	@Override
	public void release() {
		super.release();
		value = null;
		body = null;
	}

}