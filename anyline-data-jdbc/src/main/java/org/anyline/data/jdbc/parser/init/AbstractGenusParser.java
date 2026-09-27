/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.jdbc.parser.init;


import org.anyline.data.adapter.function.Parser;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DDL 解析器抽象父类，基于模板方法模式统一各数据库方言的解析流程。
 * <p>
 * 子类只需提供界定符分隔符（{@code delimiterFr/delimiterTo/identClean}）、
 * 表级选项提取（{@link #extractTableOptions}）以及自增列检测（{@link #isAutoIncrement}）。
 * 其他解析逻辑（表名、主键、索引、外键等）均由本类基于分隔符参数统一实现。
 * </p>
 */

public abstract class AbstractGenusParser implements Parser {

	/** 界定符左分隔符正则，如 Oracle/PG: {@code \"?} MySQL: {@code [`\"]?} MSSQL: {@code \[?} */
	protected String delimiterFr = "";
	/** 界定符右分隔符正则，如 Oracle/PG: {@code \"?} MySQL: {@code [`\"]?} MSSQL: {@code \]?} */
	protected String delimiterTo = "";
	/** 界定符引号清理正则，用于 {@code replaceAll} 去掉已提取名称中的引号 */
	protected String identClean = "";

	// ======================== 接口方法 ========================

	/**
	 * 解析 DDL 语句并返回对应的元数据对象。
	 * <p>支持 CREATE TABLE / CREATE OR REPLACE VIEW / CREATE UNIQUE INDEX / ALTER TABLE ADD CONSTRAINT 等变体。</p>
	 *
	 * @param ddl DDL 语句
	 * @return 解析出的元数据对象，无法识别时返回 null
	 */
	@Override
	public <T extends Metadata> T parse(String ddl) {
		String upper = ddl.toUpperCase().trim();
		upper = upper.replaceAll("\\s+", " ");
		if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?(?:TEMPORARY\\s+)?(?:EXTERNAL\\s+)?TABLE[\\s\\(]+.*")) {
			return (T) parseTable(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?(?:MATERIALIZED\\s+)?VIEW[\\s\\(]+.*")) {
			return (T) parseView(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:UNIQUE\\s+)?(?:FULLTEXT\\s+)?(?:SPATIAL\\s+)?INDEX[\\s\\(]+.*")) {
			return (T) parseIndex(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?FUNCTION[\\s\\(]+.*")) {
			return (T) parseFunction(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?PROCEDURE[\\s\\(]+.*")) {
			return (T) parseProcedure(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?TRIGGER[\\s\\(]+.*")) {
			return (T) parseTrigger(ddl);
		} else if (upper.matches("CREATE[\\s]+(?:OR\\s+REPLACE\\s+)?SEQUENCE[\\s\\(]+.*")) {
			return (T) parseSequence(ddl);
		} else if (upper.matches("ALTER[\\s]+TABLE[\\s]+.*[\\s]+ADD[\\s]+CONSTRAINT[\\s]+.*")) {
			return (T) parseConstraint(ddl);
		}
		return null;
	}

	// ======================== 数据库类型绑定钩子 ========================

	/**
	 * 设置表的数据库类型。
	 * <p>默认行为：当 {@link #database()} 返回 {@link DatabaseType} 时自动设置，
	 * MSSQL 等返回其他类型时跳过。</p>
	 *
	 * @param table 待设置的 Table 对象
	 */
	protected void initTableDatabase(Table table) {
		if (database() instanceof DatabaseType) {
			table.setDatabaseType((DatabaseType) database());
		}
	}

	/**
	 * 设置列的数据库类型。
	 *
	 * @param column 待设置的 Column 对象
	 */
	protected void initColumnDatabase(Column column) {
		if (database() instanceof DatabaseType) {
			column.setDatabaseType((DatabaseType) database());
		}
	}

	/**
	 * 设置索引的数据库类型。
	 *
	 * @param index 待设置的 Index 对象
	 */
	protected void initIndexDatabase(Index index) {
		if (database() instanceof DatabaseType) {
			index.setDatabaseType((DatabaseType) database());
		}
	}

	/**
	 * 设置约束的数据库类型。
	 *
	 * @param constraint 待设置的 Constraint 对象
	 */
	protected void initConstraintDatabase(Constraint constraint) {
		if (database() instanceof DatabaseType) {
			constraint.setDatabaseType((DatabaseType) database());
		}
	}

	// ======================== 子类必须实现 ========================

	/**
	 * 提取数据库特有的表级 DDL 选项。
	 * <ul>
	 *   <li>Oracle: TABLESPACE + LOGGING/NOLOGGING</li>
	 *   <li>PostgreSQL: TABLESPACE</li>
	 *   <li>MySQL: ENGINE + AUTO_INCREMENT</li>
	 *   <li>MSSQL: ON filegroup</li>
	 * </ul>
	 *
	 * @param ddl   DDL 语句
	 * @param table 待填充的 Table 对象
	 */
	@Override public abstract void extractTableOptions(String ddl, Table table);

	// ======================== 模板方法：建表解析 ========================

	/**
	 * 解析 CREATE TABLE DDL，按固定流程调用各子步骤。
	 *
	 * @param ddl CREATE TABLE 语句
	 * @return 解析后的 Table 对象，无法解析时返回 null
	 */
	@Override
	public Table parseTable(String ddl) {
		if (BasicUtil.isEmpty(ddl)) {
			return null;
		}
		if (!ddl.trim().toUpperCase().startsWith("CREATE TABLE")) {
			return null;
		}

		Table table = new Table();
		initTableDatabase(table);

		table.setName(extractTableName(ddl));

		String comment = extractTableComment(ddl);
		if (comment != null) {
			table.setComment(comment);
		}
		extractTableCharsetAndCollate(ddl, table);
		extractTableOptions(ddl, table);

		for (Column column : extractColumns(ddl, table)) {
			table.addColumn(column);
		}

		PrimaryKey primaryKey = extractPrimaryKey(ddl);
		if (primaryKey != null) {
			table.setPrimaryKey(primaryKey);
		}

		for (Index index : extractIndexes(ddl, table).values()) {
			table.add(index);
		}
		for (Constraint constraint : extractConstraints(ddl, table).values()) {
			table.add(constraint);
		}

		return table;
	}

	// ======================== 子步骤：列解析 ========================

	/**
	 * 提取列通用属性：非空、自增、默认值。
	 * <p>各数据库差异通过 {@link #isAutoIncrement} 和 {@link #afterExtractColumnAttributes} 处理。</p>
	 *
	 * @param afterName 列名之后的部分（类型 + 约束）
	 * @param column    待填充的 Column 对象
	 */
	protected void extractColumnAttributes(String afterName, Column column) {
		String upper = afterName.toUpperCase();
		if (upper.contains("NOT NULL")) {
			column.setNullable(false);
		} else if (upper.contains("NULL")) {
			column.setNullable(true);
		}
		if (isAutoIncrement(afterName)) {
			column.setAutoIncrement(true);
		}
		Pattern defaultPattern = Pattern.compile(
			"DEFAULT\\s+(.+?)(?:\\s+(?:NOT\\s+NULL|NULL|PRIMARY|UNIQUE|KEY|INDEX|CHECK|REFERENCES|CONSTRAINT|COMMENT|AUTO_INCREMENT|ON\\s+UPDATE|IDENTITY|GENERATED|$)|,|$)",
			Pattern.CASE_INSENSITIVE);
		Matcher defaultMatcher = defaultPattern.matcher(afterName);
		if (defaultMatcher.find()) {
			String defaultValue = defaultMatcher.group(1).trim();
			if (defaultValue.endsWith(",")) {
				defaultValue = defaultValue.substring(0, defaultValue.length() - 1);
			}
			column.setDefaultValue(defaultValue);
		}
		afterExtractColumnAttributes(afterName, column);
	}

	/**
	 * 判断列是否为自增列，子类按方言覆盖。
	 * <ul>
	 *   <li>Oracle: GENERATED AS IDENTITY</li>
	 *   <li>PostgreSQL: SERIAL 或 IDENTITY</li>
	 *   <li>MySQL: AUTO_INCREMENT</li>
	 *   <li>MSSQL: IDENTITY</li>
	 * </ul>
	 *
	 * @param afterName 列名之后的部分
	 * @return 是否为自增列，默认 false
	 */
	protected abstract boolean isAutoIncrement(String afterName);

	/**
	 * 列属性提取后置钩子，MySQL 用于提取 ON UPDATE 当前时间戳。
	 *
	 * @param afterName 列名之后的部分
	 * @param column    已填充的 Column 对象
	 */
	protected abstract void afterExtractColumnAttributes(String afterName, Column column);

	// ======================== 子步骤：其他可选信息 ========================

	/**
	 * 提取建表 DDL 中的 COMMENT 信息。
	 * <p>PostgreSQL 和 MySQL 可覆盖此方法。</p>
	 *
	 * @param ddl DDL 语句
	 * @return 表注释文本，无注释时返回 null
	 */
	protected abstract String extractTableComment(String ddl);

	/**
	 * 提取建表 DDL 中的字符集与排序规则。
	 * <p>仅 MySQL 覆盖此方法。</p>
	 *
	 * @param ddl   DDL 语句
	 * @param table 待填充的 Table 对象
	 */
	protected abstract void extractTableCharsetAndCollate(String ddl, Table table);

	// ======================== 子步骤：基于分隔符的通用解析 ========================

	/**
	 * 从 DDL 中提取表名，支持 IF NOT EXISTS 前缀。
	 *
	 * @param ddl DDL 语句
	 * @return 表名，无法提取时返回 null
	 */
	protected String extractTableName(String ddl) {
		Pattern pattern = Pattern.compile(
			"CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?" + delimiterFr + "(\\w+)" + delimiterTo,
			Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(ddl);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * 解析单个列定义（如 {@code id INT NOT NULL AUTO_INCREMENT}）。
	 *
	 * @param columnDef 列定义文本
	 * @param table     所属表
	 * @return 解析后的 Column 对象，无法解析时返回 null
	 */
	protected Column parseColumnDefinition(String columnDef, Table table) {
		columnDef = columnDef.trim();
		if (columnDef.isEmpty()) return null;

		Column column = new Column();
		initColumnDatabase(column);
		column.setTable(table);

		Pattern namePattern = Pattern.compile("^" + delimiterFr + "(\\w+)" + delimiterTo, Pattern.CASE_INSENSITIVE);
		Matcher nameMatcher = namePattern.matcher(columnDef);
		if (nameMatcher.find()) {
			column.setName(nameMatcher.group(1));
		} else {
			return null;
		}

		String afterName = columnDef.substring(nameMatcher.end()).trim();
		column.setType(extractDataType(afterName));
		extractTypeLengthAndPrecision(afterName, column);
		extractColumnAttributes(afterName, column);

		String comment = extractColumnComment(afterName);
		if (comment != null) {
			column.setComment(comment);
		}
		return column;
	}

	/**
	 * 提取主键定义（PRIMARY KEY 内联或表级约束）。
	 *
	 * @param ddl DDL 语句
	 * @return PrimaryKey 对象，无主键时返回 null
	 */
	protected PrimaryKey extractPrimaryKey(String ddl) {
		Pattern pattern = Pattern.compile("PRIMARY\\s+KEY\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(ddl);
		if (matcher.find()) {
			PrimaryKey primaryKey = new PrimaryKey();
			for (String columnName : matcher.group(1).split(",")) {
				primaryKey.addColumn(new Column(columnName.trim().replaceAll(identClean, "")));
			}
			return primaryKey;
		}
		return null;
	}

	/**
	 * 提取表级索引定义（KEY / INDEX）。
	 *
	 * @param ddl   DDL 语句
	 * @param table 所属表
	 * @return 索引名 → Index 映射
	 */
	protected Map<String, Index> extractIndexes(String ddl, Table table) {
		Map<String, Index> indexes = new LinkedHashMap<>();
		Pattern pattern = Pattern.compile(
			"(KEY|INDEX)\\s+" + delimiterFr + "(\\w+)" + delimiterTo + "\\s*\\(([^)]+)\\)",
			Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(ddl);
		while (matcher.find()) {
			Index index = new Index();
			initIndexDatabase(index);
			index.setTable(table);
			index.setName(matcher.group(2));
			int pos = 1;
			for (String columnName : matcher.group(3).split(",")) {
				Column column = new Column(columnName.trim().replaceAll(identClean, ""));
				column.setPosition(pos++);
				index.addColumn(column);
			}
			indexes.put(index.getName(), index);
		}
		return indexes;
	}

	/**
	 * 提取外键约束定义（CONSTRAINT ... FOREIGN KEY ... REFERENCES ...）。
	 *
	 * @param ddl   DDL 语句
	 * @param table 所属表
	 * @return 约束名 → Constraint 映射
	 */
	protected Map<String, Constraint> extractConstraints(String ddl, Table table) {
		Map<String, Constraint> constraints = new LinkedHashMap<>();
		Pattern fkPattern = Pattern.compile(
			"CONSTRAINT\\s+" + delimiterFr + "(\\w+)" + delimiterTo
			+ "\\s+FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s+REFERENCES\\s+"
			+ delimiterFr + "(\\w+)" + delimiterTo + "\\s*\\(([^)]+)\\)",
			Pattern.CASE_INSENSITIVE);
		Matcher fkMatcher = fkPattern.matcher(ddl);
		while (fkMatcher.find()) {
			Constraint constraint = new Constraint();
			initConstraintDatabase(constraint);
			constraint.setTable(table);
			constraint.setName(fkMatcher.group(1));
			constraint.setType("FOREIGN KEY");
			for (String columnName : fkMatcher.group(2).split(",")) {
				constraint.addColumn(new Column(columnName.trim().replaceAll(identClean, "")));
			}
			constraints.put(constraint.getName(), constraint);
		}
		return constraints;
	}

	// ======================== 公共工具方法 ========================

	/**
	 * 从 DDL 中解析所有列定义。
	 *
	 * @param ddl   DDL 语句
	 * @param table 所属表
	 * @return 按位置排列的列列表
	 */
	protected List<Column> extractColumns(String ddl, Table table) {
		List<Column> columns = new ArrayList<>();
		String columnDefs = extractColumnDefinitions(ddl);
		if (columnDefs == null) {
			return columns;
		}
		List<String> columnDefList = splitByComma(columnDefs);
		int position = 1;
		for (String columnDef : columnDefList) {
			columnDef = columnDef.trim();
			if (isConstraintDefinition(columnDef)) {
				continue;
			}
			Column column = parseColumnDefinition(columnDef, table);
			if (column != null) {
				column.setPosition(position++);
				columns.add(column);
			}
		}
		return columns;
	}

	/**
	 * 从 DDL 中截取列定义区域（第一个 '(' 到最后一个 ')' 之间）。
	 *
	 * @param ddl DDL 语句
	 * @return 列定义区域文本，无法截取时返回 null
	 */
	protected String extractColumnDefinitions(String ddl) {
		int start = ddl.indexOf("(");
		int end = ddl.lastIndexOf(")");
		if (start > 0 && end > start) {
			return ddl.substring(start + 1, end);
		}
		return null;
	}

	/**
	 * 按逗号分割列定义，正确处理括号嵌套（包括 {@code ()[]{} }）。
	 *
	 * @param str 列定义区域文本
	 * @return 分割后的列定义列表
	 */
	protected List<String> splitByComma(String str) {
		List<String> result = new ArrayList<>();
		int bracketCount = 0;
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '(' || c == '[' || c == '{') {
				bracketCount++;
				current.append(c);
			} else if (c == ')' || c == ']' || c == '}') {
				bracketCount--;
				current.append(c);
			} else if (c == ',' && bracketCount == 0) {
				result.add(current.toString());
				current = new StringBuilder();
			} else {
				current.append(c);
			}
		}
		if (current.length() > 0) {
			result.add(current.toString());
		}
		return result;
	}

	/**
	 * 判断列定义行是否为约束定义（PRIMARY KEY / UNIQUE / FOREIGN KEY 等）。
	 *
	 * @param def 列定义文本
	 * @return 是否为约束定义
	 */
	protected boolean isConstraintDefinition(String def) {
		String upper = def.trim().toUpperCase();
		return upper.startsWith("PRIMARY KEY") ||
			   upper.startsWith("UNIQUE") ||
			   upper.startsWith("KEY ") ||
			   upper.startsWith("INDEX ") ||
			   upper.startsWith("CONSTRAINT ") ||
			   upper.startsWith("FOREIGN KEY");
	}

	/**
	 * 从列定义文本中提取数据类型（如 INT、VARCHAR、DOUBLE PRECISION）。
	 *
	 * @param afterName 列名之后的部分
	 * @return 数据类型字符串，无法提取时返回 null
	 */
	protected String extractDataType(String afterName) {
		Pattern typePattern = Pattern.compile("^(\\w+(?:\\s+\\w+)?)", Pattern.CASE_INSENSITIVE);
		Matcher typeMatcher = typePattern.matcher(afterName);
		if (typeMatcher.find()) {
			return typeMatcher.group(1);
		}
		return null;
	}

	/**
	 * 从列定义中提取类型长度与精度（如 {@code VARCHAR(255)}、{@code DECIMAL(10,2)}）。
	 *
	 * @param afterName 列名之后的部分
	 * @param column    待填充的 Column 对象
	 */
	protected void extractTypeLengthAndPrecision(String afterName, Column column) {
		Pattern pattern = Pattern.compile("\\((\\d+)(?:\\s*,\\s*(\\d+))?\\)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(afterName);
		if (matcher.find()) {
			column.type().setLength(Integer.parseInt(matcher.group(1)));
			if (matcher.group(2) != null) {
				column.type().setPrecision(Integer.parseInt(matcher.group(2)));
			}
		}
	}

	/**
	 * 从列定义中提取行内注释（COMMENT 'xxx'）。
	 * <p>PostgreSQL 和 MySQL 的 DDL 中存在此语法。</p>
	 *
	 * @param afterName 列名之后的部分
	 * @return 列注释文本，无注释时返回 null
	 */
	protected String extractColumnComment(String afterName) {
		Pattern commentPattern = Pattern.compile("COMMENT\\s+['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE);
		Matcher commentMatcher = commentPattern.matcher(afterName);
		if (commentMatcher.find()) {
			return commentMatcher.group(1);
		}
		return null;
	}

	// ======================== 其他 parse 方法（暂未实现） ========================

	/**
	 * 解析视图 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public View parseView(String ddl) { return null; }
	/**
	 * 解析索引 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Index parseIndex(String ddl) { return null; }
	/**
	 * 解析约束 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Constraint parseConstraint(String ddl) { return null; }
	/**
	 * 解析函数 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Function parseFunction(String ddl) { return null; }
	/**
	 * 解析存储过程 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Procedure parseProcedure(String ddl) { return null; }
	/**
	 * 解析触发器 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Trigger parseTrigger(String ddl) { return null; }
	/**
	 * 解析序列 DDL（暂未实现）。
	 * @param ddl DDL 语句
	 * @return always null
	 */
	@Override public Sequence parseSequence(String ddl) { return null; }
}