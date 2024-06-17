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

 


package org.anyline.data.jdbc.influxdb;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Component("anyline.data.jdbc.adapter.influxdb")
public class InfluxAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
	
	public DatabaseType type() {
		return DatabaseType.InfluxDB;
	} 
	public InfluxAdapter() {
		delimiterFr = "\"";
		delimiterTo = "\"";
	}
	
	private String delimiter;

	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
	@Override 
	public String mergeFinalQuery(DataRuntime runtime, Run run) {
		return super.pageLimitOffset(runtime, run);
	}

	public String concat(DataRuntime runtime, String ... args) {
		return concatOr(runtime, args);
	}

	/**
	 * 创建 insert Run
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj 实体
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	@Override
	public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
		Run run = null;
		if(null != obj) {
			StringBuilder builder = new StringBuilder();
			run = new TableRun(runtime, dest);
			if(obj instanceof DataRow) {
				DataRow row = (DataRow)obj;
				LinkedHashMap<String, Column> cols = confirmInsertColumns(runtime, dest, obj, configs, columns, false);
				// insert al, tag1=value1 qty=1, name=5
				builder.append("insert ");
				name(runtime, builder, dest);
				builder.append(" ");
				Map<String, Object> tags = row.getTags();
				for(String tag:tags.keySet()) {
					builder.append(",").append(tag).append("=").append(tags.get(tag));
				}
				int qty = 0;
				for(Column column:cols.values()) {
					String col = column.getName();
					Object value = row.get(col);
					if(null == value) {
						continue;
					}
					if(qty>0) {
						builder.append(",");
					}
					builder.append(col).append("=");
					if(BasicUtil.isNumber(value) || BasicUtil.isBoolean(value)) {
						builder.append(value);
					}else{
						builder.append("\"").append(value).append("\"");
					}
					qty ++;
				}
				builder.append(" ").append(row.getNanoTime());
				run.setBuilder(builder);
			}
		}
		return run;
	}
} 
