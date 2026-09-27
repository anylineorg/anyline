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
import org.anyline.metadata.type.DatabaseOrigin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL Server DDL 解析器。
 * <ul>
 *   <li>界定符分隔符：方括号 {@code []}</li>
 *   <li>表选项：ON filegroup</li>
 *   <li>自增列：IDENTITY 关键字</li>
 * </ul>
 */

public class MSSQLGenusParser extends AbstractGenusParser {
	{
		delimiterFr = "\\[?";
		delimiterTo = "\\]?";
		identClean  = "[\\[\\]]";
	}

	// ======================== 接口方法 ========================

	/** {@inheritDoc} */
	@Override public Object database() { return DatabaseOrigin.MSSQL; }

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
	 * 提取 SQL Server 表选项：ON filegroup。
	 * {@inheritDoc}
	 */
	@Override public void extractTableOptions(String ddl, Table table) {
		Matcher m = Pattern.compile("ON\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setEngineParameters(m.group(1));
	}

	// ======================== 自增检测 ========================

	/**
	 * SQL Server 自增列通过 IDENTITY 关键字识别。
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isAutoIncrement(String afterName) {
		return afterName.toUpperCase().contains("IDENTITY");
	}

	/** MSSQL 无 ON UPDATE 语法。 */ @Override protected void afterExtractColumnAttributes(String afterName, Column column) { }
	/** MSSQL CREATE TABLE 无表级 COMMENT 语法。 */ @Override protected String extractTableComment(String ddl) { return null; }
	/** MSSQL 无表级 CHARSET/COLLATE。 */ @Override protected void extractTableCharsetAndCollate(String ddl, Table table) { }
}