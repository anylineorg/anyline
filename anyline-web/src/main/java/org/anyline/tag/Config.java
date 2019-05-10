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


package org.anyline.tag;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
public class Config extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Config.class);
	
	private String key;
	private String value;
	private String defaultValue;
	 public int doEndTag() throws JspException {
		try{
			value = ConfigTable.get(key);
			if(BasicUtil.isEmpty(value)){
				value = defaultValue;
			}
			if(BasicUtil.isNotEmpty(value)){
				JspWriter out = pageContext.getOut();
				out.print(value);
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
		key = null;
		value = null;
		defaultValue = null;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}