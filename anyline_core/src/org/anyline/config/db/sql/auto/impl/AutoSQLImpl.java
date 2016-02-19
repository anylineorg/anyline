

package org.anyline.config.db.sql.auto.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.KeyValueEncryptConfig;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.Order;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.BasicSQL;
import org.anyline.config.db.sql.auto.AutoSQL;
import org.anyline.util.BasicUtil;

public class AutoSQLImpl extends BasicSQL implements AutoSQL{
	protected String author;
	protected String table;
	protected List<String> columns;
	protected String distinct = "";
	
	public AutoSQLImpl(){
		super();
		chain = new AutoConditionChainImpl();
	}
	public SQL init(){
		return this;
	}
	/**
	 * 设置数据源
	 */
	public SQL setDataSource(String table){
		if(null == table){
			return this;
		}
		table = table.toUpperCase();
		if(table.contains("(")){
			//指定列名
			setTable(table.substring(0,table.indexOf("(")));
			int colIdx0 = table.indexOf("(");
			int colIdx1 = table.lastIndexOf(")");
			String columns = table.substring(colIdx0+1,colIdx1);
			if(columns.contains("DISTINCT")){
				//distinct
				columns = columns.replace("DISTINCT","");
				columns = columns.trim();
				distinct = "DISTINCT";
			}
			addColumn(columns);
		}else{
			setTable(table);
		}
		return this;
	}
	
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
	public SQL addCondition(boolean requried, String column, Object value, int compare){
		if(null == chain){
			chain = new AutoConditionChainImpl();
		}
		Condition condition = new AutoConditionImpl(requried,column, value, compare);
		chain.addCondition(condition);
		return this;
	}

	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}
		if(condition.contains(":")){
			KeyValueEncryptConfig conf = new KeyValueEncryptConfig(condition);
			addCondition(conf.isRequired(),conf.getField(),conf.getKey(),SQL.COMPARE_TYPE_EQUAL);
		}else{
			Condition con = new AutoConditionImpl(condition);
			chain.addCondition(con);
		}
		return this;
	}
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
	/**
	 * 添加列 
	 * CD
	 * CD,NM
	 * @param columns
	 */
	public void addColumn(String columns){
		if(BasicUtil.isEmpty(columns)){
			return;
		}
		if(null == this.columns){
			this.columns = new ArrayList<String>();
		}
		if(columns.contains(",")){
			//多列
			String[] cols = columns.split(",");
			for(String col:cols){
				this.columns.add(col.trim());
			}
		}else{
			//单列
			this.columns.add(columns);
		}
	}
	public String getDataSource(){
		return table;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	@Override
	public String getTable() {
		return table;
	}
	@Override
	public void setTable(String table) {
		this.table = table;
	}
	@Override
	public SQL order(Order order) {
		return this;
	}
	@Override
	public ConditionChain getConditionChain() {
		return this.chain;
	}
	@Override
	public void createRunText(StringBuilder builder) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getDistinct() {
		return this.distinct;
	}
	@Override
	public List<String> getColumns() {
		return this.columns;
	}
	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<SQLVariable> getSQLVariables() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
