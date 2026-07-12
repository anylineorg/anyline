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
* COMPRESS 类函数定义
* 包含所有数据库的 COMPRESS 相关函数
*/
public enum CompressFunction implements META {
		COMPRESS("压缩", "expression"),
		DECOMPRESS("解压", "expression"),
		GTSQUERY_COMPRESS("索引压缩", DatabaseType.PostgreSQL),
		GTSVECTOR_COMPRESS("索引压缩", DatabaseType.PostgreSQL),
		GTSVECTOR_DECOMPRESS("索引解压", DatabaseType.PostgreSQL),
		UN_COMPRESS("解压缩压缩字符串", "expression", DatabaseType.MySQL),
		UNCOMPRESS("解压", "expression"),
		UNCOMPRESSED_LENGTH("压缩前字符串长度", "expression", DatabaseType.MySQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    CompressFunction(String title) {
        this(title, null, null);
    }
    CompressFunction(String title, String params) {
        this(title, params, null);
    }
    CompressFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    CompressFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public META.Category category() { return META.Category.COMPRESS; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}