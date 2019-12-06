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
 
import org.anyline.config.db.Order; 
import org.anyline.config.db.SQL;
import org.anyline.util.BasicUtil; 
 
public class OrderImpl implements Order{ 
	private static final long serialVersionUID = -765229283714551699L;
	private String column;									//排序列 
	private SQL.ORDER_TYPE type = SQL.ORDER_TYPE.ASC;		//排序方式 
	 
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
		if(typ.equalsIgnoreCase("ASC")){
			this.type = SQL.ORDER_TYPE.ASC;
		}else{
			this.type = SQL.ORDER_TYPE.DESC;
		} 
	}
	public OrderImpl(String column, SQL.ORDER_TYPE type){
		setColumn(column);
		setType(type);
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
	public SQL.ORDER_TYPE getType() { 
		return type; 
	} 
	public void setType(SQL.ORDER_TYPE type) { 
		this.type = type; 
	} 
	public Object clone() throws CloneNotSupportedException{ 
		OrderImpl clone = (OrderImpl)super.clone(); 
		return clone; 
	}
	@Override
	public void setType(String type) {
		if("DESC".equalsIgnoreCase(type)){
			this.type = SQL.ORDER_TYPE.DESC;
		}else{
			this.type = SQL.ORDER_TYPE.ASC;
		}
	}
	 
} 
