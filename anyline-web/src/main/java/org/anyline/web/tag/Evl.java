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


import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.apache.log4j.Logger;


/**
 * 第一个 !=null 并 != "" 的值
 */
public class Evl extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Evl.class);
	
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
		 if(null != paramList){
			try{
				for(Object result:paramList){
					if(null != result && !result.toString().equals("null") && !result.toString().trim().equals("")){
						JspWriter out = pageContext.getOut();
						out.print(result.toString());
						break;
					}
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
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}