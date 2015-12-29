
package org.anyline.config.db;

import java.util.List;



public interface Condition extends Cloneable{
	public static String CONDITION_JOIN_TYPE_AND		= " AND ";	//拼接方式 AND
	public static String CONDITION_JOIN_TYPE_OR			= " OR ";	//拼接方式 OR
	//参数变量类型
	public static final int VARIABLE_FLAG_TYPE_INDEX	= 0;		//按下标区分
	public static final int VARIABLE_FLAG_TYPE_KEY		= 1;		//按KEY区分
	public static final int VARIABLE_FLAG_TYPE_NONE		= 2;		//没有变量
	
	/**
	 * 运行时文本
	 * @return
	 */
	public String getRunText(String disKey);
	/**
	 * 运行时参数值
	 * @return
	 */
	public List<Object> getRunValues();
	/**
	 * 拼接方式
	 * @return
	 */
	public String getJoin();
	public Condition setJoin(String join);
	/**
	 * 当前条件所处容器
	 * @return
	 */
	public ConditionChain getContainer();
	public boolean hasContainer();
	public boolean isContainer();
	/**
	 * 设置当前条件所处容器
	 * @param chain
	 */
	public Condition setContainer(ConditionChain chain);
	
	/**
	 * 初始化
	 */
	public void init();
	public void initRunValue();
	public boolean isActive();
	public int getVariableType();
	/*************************************************************************************************************
	 * 
	 * 													 自动生成
	 *
	 ************************************************************************************************************/
	
	
	/*************************************************************************************************************
	 * 
	 * 													 XML定义
	 *
	 ************************************************************************************************************/
	public String getId();
	public Object clone()throws CloneNotSupportedException;
	public void setValue(String key, Object value);
}
