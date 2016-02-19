

package org.anyline.config.db;


public interface Order extends Cloneable{
	public static final String ORDER_TYPE_ASC = "ASC";
	public static final String ORDER_TYPE_DESC = "DESC";
	public String getColumn() ;
	public void setColumn(String column) ;
	public String getType() ;
	public void setType(String type) ;
}
