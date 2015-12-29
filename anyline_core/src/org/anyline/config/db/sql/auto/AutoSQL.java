
package org.anyline.config.db.sql.auto;

import java.util.List;

import org.anyline.config.db.SQL;

public interface AutoSQL extends SQL{
	public SQL init();

	/**
	 * 设置数据源
	 */
	public SQL setDataSource(String table);
	
	/*******************************************************************************************
	 * 
	 * 										添加条件
	 * 
	 ********************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	required
	 * 			是否必须
	 * @param	column
	 * 			列名
	 * @param	value
	 * 			值
	 * @param	compare
	 * 			比较方式
	 */
	public SQL addCondition(boolean requried, String column, Object value, int compare);

	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) ;
	 /*******************************************************************************************
	 * 
	 * 										赋值
	 * 
	 ********************************************************************************************/
	
	
	/********************************************************************************************
	 * 
	 * 										生成 SQL
	 * 
	 ********************************************************************************************/
	
	public void createRunText(StringBuilder builder);
/* ******************************************* END SQL *********************************************************** */
	/**
	 * 添加列 
	 * CD
	 * CD,NM
	 * @param columns
	 */
	public void addColumn(String columns);
	public String getDataSource();
	public String getAuthor() ;
	public void setAuthor(String author) ;
	public String getTable() ;
	public void setTable(String table) ;
	public String getDistinct();
	public List<String> getColumns();
}
