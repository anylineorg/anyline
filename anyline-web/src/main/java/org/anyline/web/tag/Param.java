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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 为上级标签添加参数list 或map格式
 * @author Administrator
 *
 */
public class Param extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Param.class);
	private String property; //如果设置的property则调用父标签的setProperty(value)方法
	private String key; //未设置property的前提下 如果指定了key则添加到父标签的paramMap中 未指定则添加到父标签的paramList中
	
	 public int doEndTag() throws JspException {
		try{
			Tag parent = this.getParent();
			if(null != parent){
				value = BasicUtil.nvl(value,body);
				if(BasicUtil.isEmpty(property)){
					Method method = parent.getClass().getMethod("addParam",String.class, Object.class);
					if(null != method){
						method.invoke(parent, key, value);
						if(ConfigTable.isDebug()){
							log.warn("[set parent param map][key:"+key+"][value:"+value+"]");
						}
					}
				}else{
					BeanUtil.setFieldValue(parent, property, value);
					if(ConfigTable.isDebug()){
						log.warn("[set parent property][property:"+property+"][value:"+value+"]");
					}
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
		key = null;
		property = null;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}


	public String getProperty() {
		return property;
	}


	public void setProperty(String property) {
		this.property = property;
	}
	
	
}