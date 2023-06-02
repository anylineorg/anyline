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
 *
 *          
 */


package org.anyline.entity.generator.init;

import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SnowflakeWorker;

import java.util.List;

public class SnowflakeGenerator implements PrimaryGenerator {
	private static SnowflakeWorker worker = null;
	public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other){
		if(null == worker){
			worker = newInstance();
		}
		for(String column:columns){
			Long value = worker.next();
			BeanUtil.setFieldValue(entity, column, value, false);
		}
		return entity;
	}
	private SnowflakeWorker newInstance(){
		int workerId = ConfigTable.PRIMARY_GENERATOR_WORKER_ID;
		return new SnowflakeWorker(workerId);
	}
} 
