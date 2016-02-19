

package org.anyline.config.db.sql.auto;

import java.util.List;

import org.anyline.config.db.Condition;


/**
 * 自动生成的参数
 * @author Administrator
 *
 */
public interface AutoCondition extends  Condition{
	public Object getValue();
	public List<Object> getValues();
	public String getId();
	public String getColumn() ;
	public void setColumn(String column) ;
	public void setValues(Object values) ;
	public int getCompare() ;
	public void setCompare(int compare) ;
}
