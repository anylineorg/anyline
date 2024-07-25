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
import org.anyline.data.prepare.init.AbstractRunPrepare;
import org.anyline.entity.Compare;
import org.anyline.entity.Join;
import org.anyline.entity.Order;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class DefaultAutoPrepare extends AbstractRunPrepare implements AutoPrepare {
	protected String datasoruce;
	protected Catalog catalog;
	protected Schema schema;
	protected Table table;
	protected String distinct = "";
	protected String alias;
	protected List<Join> joins = new ArrayList<Join>();//关联表

	public DefaultAutoPrepare() {
		super();
		chain = new DefaultAutoConditionChain();
	}
	public RunPrepare init() {
		return this;
	}

	/**
	 * 设置数据源
	 * table(c1, c2)[pk1, pk2]
	 * @param table 表
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	public RunPrepare setDest(String table) {
		if(null == table) {
			return this;
		}
		this.table = new Table(table);
		parseTable(this.table);
		return this;
	}
	public RunPrepare setDest(Table table) {
		if(null == table) {
			return this;
		}
		this.table = table;
		parseTable(this.table);
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
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	public RunPrepare addCondition(Compare.EMPTY_VALUE_SWITCH swt, Compare compare, String column, Object value) {
		if(null == chain) {
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
		if(BasicUtil.isEmpty(condition)) {
			return this;
		}
		if(condition.contains(":")) {
			ParseResult parser = ConfigParser.parse(condition, false);
			Object value = ConfigParser.getValues(parser);
			addCondition(parser.getSwt(), parser.getCompare(), parser.getVar(), value);
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
	 * CD, NM
	 * @param columns  columns
	 */
	@Override
	public RunPrepare addColumn(String columns) {
		if(BasicUtil.isEmpty(columns)) {
			return this;
		}
		if(null == this.columns) {
			this.columns = new LinkedHashMap<>();
		}
		if(columns.contains(",")) {
			// 多列
			parseMultiColumns(false, columns);
		}else{
			// 单列
			if(columns.startsWith("!")) {
				excludeColumn(columns.substring(1));
			}else {
				this.columns.put(columns.toUpperCase(), new Column(columns));
			}
		}
		return this;
	}
	@Override
	public RunPrepare addColumn(Column column) {
		if(null == this.columns) {
			this.columns = new LinkedHashMap<>();
		}
		columns.put(column.getName().toUpperCase(), column);
		return this;
	}

	@Override
	public RunPrepare excludeColumn(String columns) {
		if(BasicUtil.isEmpty(columns)) {
			return this;
		}
		if(null == this.excludes) {
			this.excludes = new ArrayList<>();
		}
		if(columns.contains(",")) {
			// 多列
			parseMultiColumns(true, columns);
		}else{
			// 单列
			if(!excludes.contains(columns)) {
				this.excludes.add(columns);
			}
		}

		return this;
	}

	/**
	 * 解析多列
	 * @param src src
	 */
	protected void parseMultiColumns(boolean exclude, String src) {
		List<String> cols = new ArrayList<>();
		// 拆分转义字段(${}) CD, ${ISNULL(NM, '') AS NM}, ${CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE
		while(src.contains("${")) {
			src = src.trim();
			int fr = src.indexOf("${");
			String tmp = "";
			if(0 == fr) {
				tmp = src.substring(0, src.indexOf("}")+1);
				src = src.substring(src.indexOf("}")+1);
			}else{
				tmp = src.substring(0, fr);  // 先把 ${之前的部分拆出: CD,
				src = src.substring(fr);     // 剩余部分: ${ISNULL(NM, '') AS NM}, ${CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE
			}
			cols.add(tmp);
		}
		cols.add(src);
		// 二次拆分
		for(String c:cols) {
			if(c.contains("${")) {
				if(exclude) {
					excludeColumn(c);
				}else{
					addColumn(c);
				}
			}else{
				String[] cs = c.split(",");
				for(String item:cs) {
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
	 * user(id, nm)
	 * user as u
	 * user as u(id, nm)
	 * &lt;ds_hr&gt;user as u(id, nm)
	 */
	public void parseTable(Table table) {
		if(null != table) {
			String catalog = null;
			String schema = null;
			String name = table.getName();
			if(name.startsWith("<")) {
				datasoruce = name.substring(1, name.indexOf(">"));
				name = name.substring(name.indexOf(">")+1);
			}

			String tag = " as ";
			String lower = name.toLowerCase();
			int tagIdx = lower.lastIndexOf(tag);
			if(tagIdx > 0) {
				if(name.substring(tagIdx+tag.length()).contains(")")) {
					// 列别名中的as
				}else{
					alias = name.substring(tagIdx+tag.length()).trim();
					name = name.substring(0, tagIdx).trim();
				}
			}
			if(name.contains("(")) {
				String colStr = name.substring(name.indexOf("(")+1, name.lastIndexOf(")")).trim();
				if(colStr.toLowerCase().startsWith("distinct")) {
					distinct = "distinct";
					colStr = colStr.substring(9).trim();
				}
				parseColumn(colStr);
				name = name.substring(0, name.indexOf("("));
			}
			if(null != name && name.contains(".")) {
				String[] tbs = name.split("\\.");
				if(tbs.length == 2) {
					schema = tbs[0];
					name = tbs[1];
				}else if(tbs.length == 3) {
					catalog = tbs[0];
					schema = tbs[1];
					name = tbs[2];
				}
			}
			if(name.contains(" ")) {
				String[] tmps = name.split(" ");
				if(tmps[0].contains("(")) {
					// 列中的空格
				}else {
					name = tmps[0];
					alias = tmps[1];
				}
			}
			if(null != catalog) {
				table.setCatalog(catalog);
				this.catalog = new Catalog(catalog);
			}
			if(null != schema) {
				table.setSchema(schema);
				this.schema = new Schema(schema);
			}
			if(BasicUtil.isNotEmpty(alias)) {
				table.setAlias(alias);
			}
			if(BasicUtil.isNotEmpty(name)) {
				table.setName(name);
			}
		}
	}
	private void parseColumn(String sql) {
		if(BasicUtil.isEmpty(sql)) {
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
	public String getDest() {
		String dest = null;
		String catalogName = getCatalogName();
		String schemaName = getSchemaName();
		String tableName = getTableName();
		if(BasicUtil.isNotEmpty(catalogName)) {
			dest = catalogName;
		}
		if(BasicUtil.isNotEmpty(schemaName)) {
			if(null == dest) {
				dest = schemaName;
			}else{
				dest += "." + schemaName;
			}
		}
		if(BasicUtil.isNotEmpty(tableName)) {
			if(null == dest) {
				dest = tableName;
			}else{
				dest += "." + tableName;
			}
		}
		return dest;
	}

	@Override
	public RunPrepare setCatalog(Catalog catalog) {
		this.catalog = catalog;
		return this;
	}

	@Override
	public RunPrepare setCatalog(String catalog) {
		if(BasicUtil.isNotEmpty(catalog)) {
			this.catalog = new Catalog(catalog);
		}else{
			this.catalog = null;
		}
		return this;
	}

	@Override
	public RunPrepare setSchema(Schema schema) {
		this.schema = schema;
		return this;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public String getCatalogName() {
		if(null != catalog) {
			return catalog.getName();
		}
		return null;
	}
	public Schema getSchema() {
		return schema;
	}
	public String getSchemaName() {
		if(null != schema) {
			return schema.getName();
		}
		return null;
	}
	public RunPrepare setSchema(String schema) {
		if(BasicUtil.isNotEmpty(schema)) {
			this.schema = new Schema(schema);
		}else{
			this.schema = null;
		}
		return this;
	}
	@Override
	public Table getTable() {
		return table;
	}
	@Override
	public String getTableName() {
		if(null != table) {
			return table.getName();
		}
		return null;
	}

	@Override
	public RunPrepare setTable(String table) {
		if(BasicUtil.isNotEmpty(table)) {
			this.table = new Table(table);
			parseTable(this.table);
		}else{
			this.table = null;
		}
		return this;
	}
	@Override
	public RunPrepare setTable(Table table) {
		this.table = table;
		parseTable(this.table);
		return this;
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
	public List<Join> getJoins() {
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
