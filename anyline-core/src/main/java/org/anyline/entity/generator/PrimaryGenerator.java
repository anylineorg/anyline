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
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other) {
				return new RandomGenerator().create(entity, type, table, pks, inserts, other);
			}
		},
		SNOWFLAKE{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other) {
				return new SnowflakeGenerator().create(entity, type, table, pks, inserts, other);
			}
		},
		UUID{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other) {
				return new UUIDGenerator().create(entity, type, table, pks, inserts, other);
			}
		},
		TIME{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other) {
				return new TimeGenerator().create(entity, type, table, pks, inserts, other);
			}
		},
		TIMESTAMP{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other) {
				return new TimestampGenerator().create(entity, type, table, pks, inserts, other);
			}
		}
		;
	};

	/**
	 * 生成主键值并完成赋值
	 * @param entity entity或DataRow
	 * @param type 数据库类型
	 * @param table 表
	 * @param pks 主键,有可能是空,可以通过EntityAdapterProxy提取
	 * @param inserts 需要插入的列,成功生成主键后,需要把主键key添加到inserts中,注意要检测一下inserts中是否已包含了主键key
	 * @param other 其他参数
	 * @return 是否成功创建
	 */
	public boolean create(Object entity, DatabaseType type, String table, List<String> pks, List<String> inserts, String other);
} 
