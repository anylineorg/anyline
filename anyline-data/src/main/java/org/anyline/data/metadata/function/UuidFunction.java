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

import org.anyline.metadata.SystemFunction.META;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* UUID 类函数定义
* 包含所有数据库的 UUID 相关函数
*/
public enum UuidFunction implements META {
		BINARY_TO_UUID("二进制UUID转换为字符串", "expression"),
		GENERATE_UNIQUE("生成唯一值"),
		NEW_ID("全球唯一界定符"),
		NEWID("新GUID"),
		NEWSEQUENTIALID("新顺序GUID"),
		SYS_GUID("全球唯一界定符", DatabaseType.Oracle),
		UUID("UUID生成"),
		UUID_CMP("比较", DatabaseType.PostgreSQL),
		UUID_EQ("相等判断", DatabaseType.PostgreSQL),
		UUID_EXTRACT_TIMESTAMP("提取时间戳", DatabaseType.PostgreSQL),
		UUID_EXTRACT_VERSION("UUID中提取版本", DatabaseType.PostgreSQL),
		UUID_GE("大于等于判断", DatabaseType.PostgreSQL),
		UUID_GT("大于判断", DatabaseType.PostgreSQL),
		UUID_IN("输入转换", DatabaseType.PostgreSQL),
		UUID_LE("小于等于判断", DatabaseType.PostgreSQL),
		UUID_LT("小于判断", DatabaseType.PostgreSQL),
		UUID_NE("不等判断", DatabaseType.PostgreSQL),
		UUID_OUT("输出转换", DatabaseType.PostgreSQL),
		UUID_RECV("二进制接收", DatabaseType.PostgreSQL),
		UUID_SEND("二进制发送", DatabaseType.PostgreSQL),
		UUID_SHORT("短UUID", DatabaseType.MySQL),
		UUID_SORTSUPPORT("排序支持", DatabaseType.PostgreSQL),
		UUID_TO_BIN("UUID转二进制", "uuid", DatabaseType.MySQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    UuidFunction(String title) {
        this(title, null, null);
    }
    UuidFunction(String title, String params) {
        this(title, params, null);
    }
    UuidFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    UuidFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.UUID; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}