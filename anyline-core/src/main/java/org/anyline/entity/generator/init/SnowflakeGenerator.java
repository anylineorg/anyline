/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SnowflakeWorker;

import java.util.LinkedHashMap;
import java.util.List;

public class SnowflakeGenerator implements PrimaryGenerator {
	private static SnowflakeWorker worker = null;
	@Override
	public boolean create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
		if(null == columns) {
			if(entity instanceof DataRow) {
				columns = ((DataRow)entity).getPrimaryKeys();
			}else{
				columns = EntityAdapterProxy.primaryKeys(entity.getClass(), true);
			}
		}
		if(null == worker) {
			worker = newInstance();
		}
		for(String column:columns) {
			if(null != BeanUtil.getFieldValue(entity, column, true)) {
				continue;
			}
			create(entity, type, table, column, other);
		}
		return true;
	}

	@Override
	public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> columns, String other) {
		if(null == columns) {
			if(entity instanceof DataRow) {
				columns = ((DataRow)entity).getPrimaryColumns();
			}else{
				columns = EntityAdapterProxy.primaryKeys(entity.getClass());
			}
		}
		if(null == worker) {
			worker = newInstance();
		}
		for(Column column:columns.values()) {
			//检测主键值
			if(null != BeanUtil.getFieldValue(entity, column.getName(), true)) {
				continue;
			}
			create(entity, type, table, column.getName(), other);
		}
		return true;
	}
	public boolean create(Object entity, DatabaseType type, String table, String column, String other) {
		Long value = worker.next();
		BeanUtil.setFieldValue(entity, column, value, true);
		return true;
	}
	private SnowflakeWorker newInstance() {
		int workerId = ConfigTable.PRIMARY_GENERATOR_WORKER_ID;
		return new SnowflakeWorker(workerId);
	}
} 
