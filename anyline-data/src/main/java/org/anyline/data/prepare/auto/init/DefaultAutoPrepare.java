
/*
 * Copyright 2006-2023 www.anyline.org
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
 */


package org.anyline.data.prepare.auto.init;

import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.AutoPrepare;
import org.anyline.data.prepare.init.DefaultPrepare;
import org.anyline.entity.Compare;
import org.anyline.entity.Order;
import org.anyline.entity.Join;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;

public class DefaultAutoPrepare extends DefaultPrepare implements AutoPrepare {
	protected String datasoruce;
	protected String schema;
	protected String table;
	protected String distinct = "";
	protected String alias;
	protected List<Join> joins = new ArrayList<Join>();//关联表



	public DefaultAutoPrepare(){
		super();
		chain = new DefaultAutoConditionChain();
	}
	public RunPrepare init(){
		return this;
	}
	/**
	 * 设置数据源
	 * table(c1,c2)[pk1,pk2]
	 * @param table 表
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	public RunPrepare setDataSource(String table){
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
	 * @param swt 空值处理方式
	 * @param column  列名
	 * @param value 值
	 * @param compare  比较方式
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	public RunPrepare addCondition(Compare.EMPTY_VALUE_SWITCH swt, Compare compare, String column, Object value){
		if(null == chain){
			chain = new DefaultAutoConditionChain();
		}
		Condition condition = new DefaultAutoCondition(swt, compare, null, column, value);
		chain.addCondition(condition);
		return this;
	}
	/**
	 * 添加静态文本查询条件
	 */
	public RunPrepare addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}
		if(condition.contains(":")){
			ParseResult parser = ConfigParser.parse(condition, false);
			Object value = ConfigParser.getValues(parser);
			addCondition(parser.getSwitch(), parser.getCompare(), parser.getVar(),value);
		}else{
			Condition con = new DefaultAutoCondition(condition);
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
	 * 										生成 RunPrepare
	 *
	 ********************************************************************************************/
	/**
	 * 添加列
	 * CD
	 * CD,NM
	 * @param columns  columns
	 */
	@Override
	public RunPrepare addColumn(String columns){
		if(BasicUtil.isEmpty(columns)){
			return this;
		}
		if(null == this.queryColumns){
			this.queryColumns = new ArrayList<>();
		}
		if(columns.contains(",")){
			// 多列
			parseMultColumns(false, columns);
		}else{
			// 单列
			if(columns.startsWith("!")){
				excludeColumn(columns.substring(1));
			}else {
				if (!queryColumns.contains(columns)) {
					queryColumns.add(columns);
				}
			}
		}
		return this;
	}

	@Override
	public RunPrepare excludeColumn(String columns) {
		if(BasicUtil.isEmpty(columns)){
			return this;
		}
		if(null == this.excludeColumns){
			this.excludeColumns = new ArrayList<>();
		}
		if(columns.contains(",")){
			// 多列
			parseMultColumns(true, columns);
		}else{
			// 单列
			if(!excludeColumns.contains(columns)) {
				this.excludeColumns.add(columns);
			}
		}

		return this;
	}


	/**
	 * 解析多列
	 * @param src src
	 */
	protected void parseMultColumns(boolean exclude, String src){
		List<String> cols = new ArrayList<>();
		// 拆分转义字段(${}) CD, ${ISNULL(NM,'') AS NM}, ${CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE
		while(src.contains("${")){
			src = src.trim();
			int fr = src.indexOf("${");
			String tmp = "";
			if(0 == fr){
				tmp = src.substring(0, src.indexOf("}")+1);
				src = src.substring(src.indexOf("}")+1);
			}else{
				tmp = src.substring(0, fr);  // 先把 ${之前的部分拆出: CD,
				src = src.substring(fr);     // 剩余部分: ${ISNULL(NM,'') AS NM}, ${CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE
			}
			cols.add(tmp);
		}
		cols.add(src);
		// 二次拆分
		for(String c:cols){
			if(c.contains("${")){
				if(exclude) {
					excludeColumn(c);
				}else{
					addColumn(c);
				}
			}else{
				String[] cs = c.split(",");
				for(String item:cs){
					item = item.trim();
					if(item.length()>0)
						addColumn(item);
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
	public void parseTable(){
		if(null != table){
			if(table.startsWith("<")){
				datasoruce = table.substring(1, table.indexOf(">"));
				table = table.substring(table.indexOf(">")+1);
			}

			String tag = " as ";
			String lower = table.toLowerCase();
			int tagIdx = lower.lastIndexOf(tag);
			if(tagIdx > 0){
				if(table.substring(tagIdx+tag.length()).contains(")")){
					// 列别名中的as
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
				table = table.substring(0, table.indexOf("("));
			}
			if(null != table && table.contains(".")){
				String[] tbs = table.split("\\.");
				table = tbs[1];
				schema = tbs[0];
			}
			if(table.contains(" ")){
				String[] tmps = table.split(" ");
				if(tmps[0].contains("(")){
					// 列中的空格
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
		if(sql.contains("${")) {
			while (sql.contains("${")) {
				sql = sql.trim();
				String pre = sql.substring(0, sql.indexOf("${"));
				if (BasicUtil.isNotEmpty(pre)) {
					String[] pres = pre.split(",");
					for (String item : pres) {
						item = item.trim();
						addColumn(item);
					}
				}
				int fr = sql.indexOf("${");
				int to = sql.indexOf("}");
				String col = sql.substring(fr + 2, to).trim();
				addColumn(col);
				sql = sql.substring(sql.indexOf("}") + 1).trim();
			}
		}else{
			String[] cols = sql.split(",");
			for (String item : cols) {
				item = item.trim();
				addColumn(item);
			}
		}
	}
	public String getDataSource(){
		String ds = table;
		if (BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(schema)) {
			ds = schema + "." + ds;
		}
		if (BasicUtil.isEmpty(ds)) {
			ds = schema;
		}
		return ds;
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
	public RunPrepare order(Order order) {
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
	public String getText() {
		return null;
	}
	@Override
	public List<Variable> getSQLVariables() {
		return null;
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
