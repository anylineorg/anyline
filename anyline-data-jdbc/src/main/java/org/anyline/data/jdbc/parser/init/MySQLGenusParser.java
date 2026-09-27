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


import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL DDL 解析器。
 * <ul>
 *   <li>界定符分隔符：反引号 {@code `} 或双引号 {@code "}</li>
 *   <li>表选项：ENGINE + AUTO_INCREMENT</li>
 *   <li>额外信息：COMMENT、CHARSET/COLLATE、ON UPDATE</li>
 *   <li>自增列：AUTO_INCREMENT 关键字</li>
 * </ul>
 */

public class MySQLGenusParser extends AbstractGenusParser {
	{
		delimiterFr = "[`\"]?";
		delimiterTo = "[`\"]?";
		identClean  = "[`\"]";
	}

	// ======================== 接口方法 ========================

	/** {@inheritDoc} */
	@Override public DatabaseType database() { return DatabaseType.MySQL; }

	/** {@inheritDoc} */ @Override public Table parseTable(String ddl)            { return super.parseTable(ddl); }
	/** {@inheritDoc} */ @Override public View parseView(String ddl)              { return super.parseView(ddl); }
	/** {@inheritDoc} */ @Override public Index parseIndex(String ddl)            { return super.parseIndex(ddl); }
	/** {@inheritDoc} */ @Override public Constraint parseConstraint(String ddl)  { return super.parseConstraint(ddl); }
	/** {@inheritDoc} */ @Override public Function parseFunction(String ddl)      { return super.parseFunction(ddl); }
	/** {@inheritDoc} */ @Override public Procedure parseProcedure(String ddl)    { return super.parseProcedure(ddl); }
	/** {@inheritDoc} */ @Override public Trigger parseTrigger(String ddl)        { return super.parseTrigger(ddl); }
	/** {@inheritDoc} */ @Override public Sequence parseSequence(String ddl)      { return super.parseSequence(ddl); }
	/** {@inheritDoc} */ @Override public <T extends Metadata> T parse(String ddl) { return super.parse(ddl); }

	/**
	 * 提取 MySQL 表选项：ENGINE 存储引擎 + AUTO_INCREMENT 起始值。
	 * {@inheritDoc}
	 */
	@Override public void extractTableOptions(String ddl, Table table) {
		Matcher m = Pattern.compile("ENGINE\\s*=\\s*(\\w+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setEngineParameters(m.group(1));
		m = Pattern.compile("AUTO_INCREMENT\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setIncrement(Long.parseLong(m.group(1)));
	}

	// ======================== 自增检测 ========================

	/**
	 * MySQL 自增列通过 AUTO_INCREMENT 关键字识别。
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isAutoIncrement(String afterName) {
		return afterName.toUpperCase().contains("AUTO_INCREMENT");
	}

	// ======================== 列属性后置钩子 ========================

	/**
	 * 提取 MySQL 特有的 ON UPDATE 当前时间戳表达式。
	 * {@inheritDoc}
	 */
	@Override
	protected void afterExtractColumnAttributes(String afterName, Column column) {
		Matcher m = Pattern.compile("ON\\s+UPDATE\\s+(\\S+)", Pattern.CASE_INSENSITIVE).matcher(afterName);
		if (m.find()) column.setOnUpdate(m.group(1));

		m = Pattern.compile("CHARACTER\\s+SET\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(afterName);
		if (m.find()) column.setCharset(m.group(1));

		m = Pattern.compile("COLLATE\\s+([\\w\\-]+)", Pattern.CASE_INSENSITIVE).matcher(afterName);
		if (m.find()) column.setCollate(m.group(1));
	}

	// ======================== 表注释 ========================

	/**
	 * 提取 MySQL 表注释（COMMENT='xxx' 语法）。
	 */
	@Override
	protected String extractTableComment(String ddl) {
		String upper = ddl.toUpperCase();
		int idx = upper.indexOf("COMMENT=");
		if (idx > 0) {
			String after = ddl.substring(idx + 8).trim();
			char quote = after.charAt(0);
			if (quote == '\'' || quote == '"') {
				int end = after.indexOf(quote, 1);
				if (end > 0) return after.substring(1, end);
			}
		}
		return null;
	}

	// ======================== 表字符集与排序规则 ========================

	/**
	 * 提取 MySQL 表级字符集（DEFAULT CHARSET）与排序规则（COLLATE）。
	 */
	@Override
	protected void extractTableCharsetAndCollate(String ddl, Table table) {
		Matcher m = Pattern.compile("DEFAULT\\s+CHARSET\\s*=\\s*['\"]?(\\w+)['\"]?", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setCharset(m.group(1));
		m = Pattern.compile("COLLATE\\s*=\\s*['\"]?([\\w\\-]+)['\"]?", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setCollate(m.group(1));
	}
}