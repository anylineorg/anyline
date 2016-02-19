

package org.anyline.config.db.impl;

import org.anyline.config.db.Order;
import org.anyline.util.BasicUtil;

public class OrderImpl implements Order{
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
