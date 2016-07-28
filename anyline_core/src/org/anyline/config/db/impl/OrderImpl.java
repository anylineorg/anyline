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


package org.anyline.config.db.impl;

import org.anyline.config.db.Order;
import org.anyline.util.BasicUtil;

public class OrderImpl implements Order{
	private static final long serialVersionUID = -765229283714551699L;
	public static final String ORDER_TYPE_ASC = "ASC";
	public static final String ORDER_TYPE_DESC = "DESC";
	private String column;						//排序列
	private String type = ORDER_TYPE_ASC;		//排序方式
	
	public OrderImpl(){}
	public OrderImpl(String str){
		if(null == str){
			return;
		}
		str = str.trim();
		String col = null;
		String typ = "ASC";
		if (str.contains(" ")) { // 指明正序或倒序
			String[] keys = str.split("\\s+");
			col = keys[0];
			if (keys.length > 1) {
				typ = keys[1];
			}
		} else {
			col = str;
		}
		this.column = col;
		this.type = typ;
	}
	public OrderImpl(String column, String type){
		setColumn(column);
		setType(type);
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		if(null != column){
			this.column = column.trim();
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = BasicUtil.nvl(type,this.type,"").toString().trim();
	}
	public Object clone() throws CloneNotSupportedException{
		OrderImpl clone = (OrderImpl)super.clone();
		return clone;
	}
}
