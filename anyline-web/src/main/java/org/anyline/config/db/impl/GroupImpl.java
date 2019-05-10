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


package org.anyline.config.db.impl;

import org.anyline.config.db.Group;


public class GroupImpl implements Group{
	private static final long serialVersionUID = 5820480420021701152L;
	private String column;						//排序列
	
	public GroupImpl(){}
	public GroupImpl(String column){
		setColumn(column);
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		if(null != column){
			this.column = column.trim();
		}
	}
	public Object clone() throws CloneNotSupportedException{
		GroupImpl clone = (GroupImpl)super.clone();
		return clone;
	}
}
