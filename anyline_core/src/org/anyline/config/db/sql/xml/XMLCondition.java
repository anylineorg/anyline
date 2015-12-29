
package org.anyline.config.db.sql.xml;

import org.anyline.config.db.Condition;
import org.anyline.config.db.SQLVariable;


/**
 * 通过XML定义的参数
 * @author Administrator
 *
 */
public interface XMLCondition extends Condition{
	
	public void init();
	/**
	 * 赋值
	 * @param variable
	 * @param values
	 */
	public void setValue(String variable, Object values);


	public String getId() ;

	public void setId(String id) ;

	public String getText() ;

	

	public String getRunText() ;
	public SQLVariable getVariable(String key) ;
}