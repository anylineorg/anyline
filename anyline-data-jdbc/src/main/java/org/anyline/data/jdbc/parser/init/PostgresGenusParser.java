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
 * PostgreSQL DDL 解析器。
 * <ul>
 *   <li>界定符分隔符：双引号 {@code "}（与 Oracle 相同）</li>
 *   <li>表选项：TABLESPACE 表空间</li>
 *   <li>自增列：SERIAL 或 IDENTITY 关键字</li>
 * </ul>
 */

public class PostgresGenusParser extends AbstractGenusParser {
	{
		delimiterFr = "\"?";
		delimiterTo = "\"?";
		identClean  = "\"";
	}

	// ======================== 接口方法 ========================

	/** {@inheritDoc} */
	@Override public DatabaseType database() { return DatabaseType.PostgreSQL; }

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
	 * 提取 PostgreSQL 表选项：TABLESPACE 表空间。
	 * {@inheritDoc}
	 */
	@Override public void extractTableOptions(String ddl, Table table) {
		Matcher m = Pattern.compile("TABLESPACE\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) table.setEngineParameters(m.group(1));
	}

	// ======================== 表注释 ========================

	/**
	 * 提取 PostgreSQL 表注释（COMMENT = 'xxx' 语法）。
	 */
	@Override
	protected String extractTableComment(String ddl) {
		Matcher m = Pattern.compile("COMMENT\\s*=\\s*['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE).matcher(ddl);
		return m.find() ? m.group(1) : null;
	}

	// ======================== 自增检测 ========================

	/**
	 * PostgreSQL 自增列通过 SERIAL 或 IDENTITY 关键字识别。
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isAutoIncrement(String afterName) {
		String upper = afterName.toUpperCase();
		return upper.contains("SERIAL") || upper.contains("IDENTITY");
	}

	/** PostgreSQL 无 ON UPDATE 语法。 */ @Override protected void afterExtractColumnAttributes(String afterName, Column column) { }
	/** PostgreSQL 无表级 CHARSET/COLLATE。 */ @Override protected void extractTableCharsetAndCollate(String ddl, Table table) { }
}