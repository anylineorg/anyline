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


package org.anyline.entity.generator;


import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.init.*;

import java.util.List;

public interface PrimaryGenerator {
	public enum GENERATORS implements PrimaryGenerator{
		RANDOM{
			@Override
			public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
				return new RandomGenerator().create(entity, type, table, columns, other);
			}
		},
		SNOWFLAKE{
			@Override
			public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
				return new SnowflakeGenerator().create(entity, type, table, columns, other);
			}
		},
		UUID{
			@Override
			public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
				return new UUIDGenerator().create(entity, type, table, columns, other);
			}
		},
		TIME{
			@Override
			public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
				return new TimeGenerator().create(entity, type, table, columns, other);
			}
		},
		TIMESTAMP{
			@Override
			public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
				return new TimestampGenerator().create(entity, type, table, columns, other);
			}
		}
		;
	};
	public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other);
} 
