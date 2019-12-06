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
 
 
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

/**
 * 第一个 !=null 并 != "" 的值
 */ 
public class Evl extends BaseBodyTag implements Cloneable{ 
	private static final long serialVersionUID = 1L; 
	private static final Logger log = LoggerFactory.getLogger(Evl.class);
	private String target = null; 
	 
	 public int doEndTag() throws JspException {
		 if(null == paramList || paramList.size()==0){
			 if(BasicUtil.isNotEmpty(value)){
				 String str = value.toString();
				 if(str.contains(",")){
					 String[] strs = str.split(",");
					 paramList = new ArrayList<Object>(); 
					 for(String item:strs){
						 paramList.add(item);
					 }
				 }
			 }
		 }
		 if(null != paramList && paramList.size()>0){ 
			try{
				String result = "";
				for(Object param:paramList){ 
					if(null != param && !param.toString().equals("null") && !param.toString().trim().equals("")){ 

						result = param.toString();
						break; 
					} 
				}

				result = BasicUtil.evl(result,body,"").toString();
				Tag parent = this.getParent();
				if("".equals(target) && null != parent){
					Method method = BeanUtil.getMethod(parent.getClass(), "setEvl", String.class);
					if(null != method){
						method.invoke(parent, result);
					}
				}else{
					JspWriter out = pageContext.getOut();
					out.print(result);
				}
				 
			}catch(Exception e){ 
				e.printStackTrace(); 
			}finally{ 
				release(); 
			}
		 } 
        return EVAL_PAGE;    
	} 
 
 
	@Override 
	public void release() { 
		super.release();
		paramList = null;
		value = null;
		target = null; 
	} 
	@Override 
	protected Object clone() throws CloneNotSupportedException { 
		return super.clone(); 
	} 
}
