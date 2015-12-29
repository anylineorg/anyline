
package org.anyline.config.db.impl;

import org.anyline.config.db.Group;


public class GroupImpl implements Group{
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
