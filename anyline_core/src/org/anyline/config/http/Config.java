

package org.anyline.config.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;

public interface Config {
	//从request 取值方式
	public static int FETCH_REQUEST_VALUE_TYPE_SINGLE = 1;	//单值
	public static int FETCH_REQUEST_VALUE_TYPE_MULIT  = 2;	//数组

	/**
	 * 参数加密配置 默认不加密
	 * @param config 参数名　参数名是否加密　参数值是否加密
	 * 只设置一项时　默认为设置参数名加密状态
	 * @return
	 */
	public String parseEncrypt();

	/**
	 * 赋值
	 * @param request
	 */
	public void setValue(HttpServletRequest request);
	public List<Object> getValues() ;
	public void addValue(Object value);

	/**
	 * 
	 * @param chain 容器
	 * @return
	 */
	public Condition createAutoCondition(ConditionChain chain);
	public String getId() ;

	public void setId(String id) ;

	public String getVariable() ;

	public void setVariable(String variable) ;

	public String getKey() ;

	public void setKey(String key) ;

	public int getCompare() ;

	public void setCompare(int compare) ;

	public boolean isEmpty() ;

	public void setEmpty(boolean empty) ;

	public boolean isRequire() ;

	public void setRequire(boolean require) ;

	public String getJoin() ;

	public void setJoin(String join) ;

	public boolean isKeyEncrypt() ;

	public boolean isValueEncrypt();
	
	public Object clone();
	public String toString();
}