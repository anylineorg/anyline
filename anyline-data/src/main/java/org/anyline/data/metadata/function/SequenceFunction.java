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


package org.anyline.data.metadata.function;

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.SystemFunction.META;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* SEQUENCE 类函数定义
* 包含所有数据库的 SEQUENCE 相关函数
*/
public enum SequenceFunction implements META {
		CURR_VAL("序列值", "sequence_name"),
		CURRVAL("序列在当前会话中当前值", "sequence"),
		GET_SERIAL_SEQUENCE("与列关联序列名称", DatabaseType.PostgreSQL),
		IDENT_CURRENT("当前标识值"),
		IDENT_INCR("标识增量"),
		IDENT_SEED("标识种子"),
		IDENTITY("返回自身"),
		LAST_INSERT_ID("最后插入ID"),
		LAST_VAL("序列最后值"),
		LASTVAL("最后序列值", DatabaseType.PostgreSQL),
		NEXT_VAL("序列值"),
		NEXTVAL("下一序列值", "sequence", DatabaseType.PostgreSQL),
		NUMERIC_AVG_SERIALIZE("序列化", DatabaseType.PostgreSQL),
		NUMERIC_POLY_SERIALIZE("序列化", DatabaseType.PostgreSQL),
		NUMERIC_SERIALIZE("序列化", DatabaseType.PostgreSQL),
		SCOPE_IDENTITY("作用域标识"),
		SET_VAL("一个序列当前值"),
		SETVAL("设置序列值", "sequence", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    SequenceFunction(String title) {
        this(title, null, null);
    }
    SequenceFunction(String title, String params) {
        this(title, params, null);
    }
    SequenceFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    SequenceFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.SEQUENCE; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}