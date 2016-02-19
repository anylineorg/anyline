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
 */


package org.anyline.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.entity.DataSet;

public class Power extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(Power.class);
	private Object powers;		//权限范围
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		DataSet pws = null;
		try {
			if(null == powers){
				pws = (DataSet)request.getSession().getAttribute("SESSION_ATTR_POWER");
			}else{
				pws = (DataSet)powers;
			}
			boolean result = false;
			if(null != pws){
				int size = pws.getSize();
				for(int i=0; i<size; i++){
					String cd = pws.getString(i, "CD");
					if(null == cd){
						cd = pws.getString(i,"POWER_CD");
					}
					if(null != cd && cd.equals(value)){
						result = true;
						break;
					}
				}
			}
			JspWriter out = pageContext.getOut();
			if(result){				
				out.print(body);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			release();
		}
		return EVAL_PAGE;
	}
	public Object getPowers() {
		return powers;
	}
	public void setPowers(Object powers) {
		this.powers = powers;
	}
}