/*
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.jdbc.config.db.sql.auto.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ParseResult;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLVariable;
import org.anyline.jdbc.config.db.impl.BasicSQL;
import org.anyline.jdbc.config.db.sql.auto.AutoSQL;
import org.anyline.util.BasicUtil;

public class AutoSQLImpl extends BasicSQL implements AutoSQL{
	protected String datasoruce;
	protected String schema;
	protected String table;
	protected String distinct = "";
	protected String alias;
	protected List<String> columns = new ArrayList<>();
	protected List<Join> joins = new ArrayList<Join>();//关联表



	public AutoSQLImpl(){
		super();
		chain = new AutoConditionChainImpl();
	}
	public SQL init(){
		return this;
	}
	/**
	 * 设置数据源
	 * table(c1,c2)[pk1,pk2]
	 * @param table table
	 * @return return
	 */
	public SQL setDataSource(String table){
		if(null == table){
			return this;
		}
		this.table = table;
		parseTable();
		return this;
	}
	/* ******************************************************************************************
	 *
	 * 										添加条件
	 *
	 * *******************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	required 是否必须
	 * @param strictRequired 是否严格验证 如果缺少严格验证的条件 整个SQL不执行
	 * @param	column  列名
	 * @param	value 值
	 * @param	compare  比较方式
	 * @return return
	 */
	public SQL addCondition(boolean required, boolean strictRequired, String column, Object value, COMPARE_TYPE compare){
		if(null == chain){
			chain = new AutoConditionChainImpl();
		}
		Condition condition = new AutoConditionImpl(required, strictRequired, null, column, value, compare);
		chain.addCondition(condition);
		return this;
	}
	public SQL addCondition(boolean required, String column, Object value, COMPARE_TYPE compare){
		return addCondition(required, false, column, value, compare);
	}
	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}
		if(condition.contains(":")){
			ParseResult parser = ConfigParser.parse(condition, false);
			Object value = ConfigParser.getValues(parser);
			addCondition(parser.isRequired(),parser.isStrictRequired(),parser.getVar(),value,parser.getCompare());
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
	 * @param columns  columns
	 */
	public void addColumn(String columns){
		if(BasicUtil.isEmpty(columns)){
			return;
		}
		if(null == this.columns){
			this.columns = new ArrayList<>();
		}
		if(columns.contains(",")){
			//多列
			parseMultColumns(columns);
		}else{
			//单列
			this.columns.add(columns);
		}
	}
	/**
	 * 解析多列
	 * @param src src
	 */
	protected void parseMultColumns(String src){
		List<String> cols = new ArrayList<>();
		//拆分转义字段({}) CD, {ISNULL(NM,'') AS NM}, {CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE
		while(src.contains("{")){
			src = src.trim();
			int fr = src.indexOf("{");
			String tmp = "";
			if(0 == fr){
				tmp = src.substring(0, src.indexOf("}")+1);
				src = src.substring(src.indexOf("}")+1);
			}else{
				tmp = src.substring(0, fr);
				src = src.substring(fr);
			}
			cols.add(tmp);
		}
		cols.add(src);
		//二次拆分
		for(String c:cols){
			if(c.contains("{")){
				this.columns.add(c);
			}else{
				String[] cs = c.split(",");
				for(String item:cs){
					item = item.trim();
					if(item.length()>0)
						this.columns.add(item);
				}
			}
		}
	}
	/**
	 * 解析name
	 * 支持的格式(以下按先后顺序即可)
	 * user
	 * user(id,nm)
	 * user as u
	 * user as u(id,nm)
	 * &lt;ds_hr&gt;user as u(id,nm)
	 */
	protected void parseTable(){
		if(null != table){
			if(table.startsWith("<")){
				datasoruce = table.substring(1,table.indexOf(">"));
				table = table.substring(table.indexOf(">")+1);
			}

			String tag = " as ";
			String lower = table.toLowerCase();
			int tagIdx = lower.lastIndexOf(tag);
			if(tagIdx > 0){
				if(table.substring(tagIdx+tag.length()).contains(")")){
					//列别名中的as
				}else{
					alias = table.substring(tagIdx+tag.length()).trim();
					table = table.substring(0,tagIdx).trim();
				}
			}
			if(table.contains("(")){
				String colStr = table.substring(table.indexOf("(")+1, table.lastIndexOf(")")).trim();
				if(colStr.toLowerCase().startsWith("distinct")){
					distinct = "distinct";
					colStr = colStr.substring(9).trim();
				}
				parseColumn(colStr);
				table = table.substring(0,table.indexOf("("));
			}
			if(null != table && table.contains(".")){
				String[] tbs = table.split("\\.");
				table = tbs[1];
				schema = tbs[0];
			}
			if(table.contains(" ")){
				String[] tmps = table.split(" ");
				if(tmps[0].contains("(")){
					//列中的空格
				}else {
					table = tmps[0];
					alias = tmps[1];
				}
			}
		}
	}
	private void parseColumn(String sql){
		if(BasicUtil.isEmpty(sql)){
			return;
		}
		if(sql.contains("{")) {
			while (sql.contains("{")) {
				String pre = sql.substring(0, sql.indexOf("{"));
				if (BasicUtil.isNotEmpty(pre)) {
					String[] pres = pre.split(",");
					for (String item : pres) {
						item = item.trim();
						if (BasicUtil.isNotEmpty(item) && !columns.contains(item)) {
							columns.add(item);
						}
					}
				}
				int fr = sql.indexOf("{");
				int to = sql.indexOf("}");
				String col = sql.substring(fr + 1, to).trim();
				if (!columns.contains(col)) {
					columns.add(col);
				}
				sql = sql.substring(sql.indexOf("}") + 1).trim();
			}
		}else{
			String[] cols = sql.split(",");
			for (String item : cols) {
				item = item.trim();
				if (BasicUtil.isNotEmpty(item) && !columns.contains(item)) {
					columns.add(item);
				}
			}
		}
	}
	public String getDataSource(){
		return table;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
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
		return null;
	}
	@Override
	public List<SQLVariable> getSQLVariables() {
		return null;
	}
	@Override
	public void setStrict(boolean strict) {}
	@Override
	public boolean isStrict() {
		return false;
	}

	@Override
	public List<Join> getJoins(){
		return joins;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
