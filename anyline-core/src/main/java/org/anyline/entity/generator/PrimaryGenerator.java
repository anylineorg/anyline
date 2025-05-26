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

package org.anyline.entity.generator;

import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.entity.generator.init.*;

import java.util.LinkedHashMap;
import java.util.List;

public interface PrimaryGenerator {
	enum GENERATOR implements PrimaryGenerator{
		DISABLE{ //不生成主键，由数据库决定
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return false;
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return false;
			}
		}, 
		AUTO{ //不设置，按配置文件
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return false;
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return false;
			}
		}, 
		RANDOM{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return new RandomGenerator().create(entity, type, table, pks, other);
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return new RandomGenerator().create(entity, type, table, pks, other);
			}
		}, 
		SNOWFLAKE{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return new SnowflakeGenerator().create(entity, type, table, pks, other);
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return new SnowflakeGenerator().create(entity, type, table, pks, other);
			}
		}, 
		UUID{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return new UUIDGenerator().create(entity, type, table, pks, other);
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return new UUIDGenerator().create(entity, type, table, pks, other);
			}
		}, 
		TIME{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return new TimeGenerator().create(entity, type, table, pks, other);
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column>  pks, String other) {
				return new TimeGenerator().create(entity, type, table, pks, other);
			}
		}, 
		TIMESTAMP{
			@Override
			public boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other) {
				return new TimestampGenerator().create(entity, type, table, pks, other);
			}
			@Override
			public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> pks, String other) {
				return new TimestampGenerator().create(entity, type, table, pks, other);
			}
		}
		;
	};

	/**
	 * 生成主键值并完成赋值
	 * @param entity entity或DataRow
	 * @param type 数据库类型
	 * @param table 表
	 * @param pks 主键, 有可能是空, 可以通过EntityAdapterProxy提取
	 * @param other 其他参数
	 * @return 是否成功创建
	 */
	boolean create(Object entity, DatabaseType type, String table, List<String> pks, String other);
	boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> pks, String other);
} 
