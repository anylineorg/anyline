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


import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImpalaGenusParser extends AbstractGenusParser {
	{
		delimiterFr = "[`\"]?";
		delimiterTo = "[`\"]?";
		identClean  = "[`\"]";
	}

	@Override public DatabaseType database() { return DatabaseType.Cloudera; }

	@Override public Table parseTable(String ddl) { return super.parseTable(ddl); }

	@Override
	public void extractTableOptions(String ddl, Table table) {
		Matcher m = Pattern.compile("STORED\\s+AS\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) {
			table.setEngineParameters(m.group(1));
		}

		m = Pattern.compile("LOCATION\\s+['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) {
			table.setLocation(m.group(1));
		}

		extractPartitionColumns(ddl, table);
	}

	protected void extractPartitionColumns(String ddl, Table table) {
		Matcher m = Pattern.compile("PARTITIONED\\s+BY\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) {
			String partitionStr = m.group(1);
			String[] parts = partitionStr.split(",");
			String[] columns = new String[parts.length];
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i].trim();
				String[] tokens = part.split("\\s+");
				if (tokens.length >= 1) {
					columns[i] = tokens[0];
					Column col = new Column(tokens[0]);
					if (tokens.length >= 2) {
						col.setType(tokens[1]);
					}
					table.addColumn(col);
				}
			}
			table.partitionBy(Table.Partition.TYPE.LIST, columns);
		}
	}

	@Override
	protected boolean isAutoIncrement(String afterName) {
		return afterName.toUpperCase().contains("SERIAL") || afterName.toUpperCase().contains("IDENTITY");
	}

	@Override
	protected void afterExtractColumnAttributes(String afterName, Column column) {
	}

	@Override
	protected String extractTableComment(String ddl) {
		Matcher m = Pattern.compile("COMMENT\\s+['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE).matcher(ddl);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	@Override
	protected void extractTableCharsetAndCollate(String ddl, Table table) {
	}
}