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
* NULL_HANDLE 类函数定义
* 包含所有数据库的 NULL_HANDLE 相关函数
*/
public enum NullFunction implements META {
    COALESCE("取首个非空", "expression1,expression2"),
    COUNT_NOT_NULL("非空参数的数量", "expression"),
    COUNT_NULL("空参数的数量", "expression"),
    IF_EQUAL_NULL_ELSE_FIRST("相等返回NULL", "expr1,expr2"),
    IF_NULL_C("是否NULL"),
    NAN_NVL("当NaN时返回默认", "expression,replacement"),
    NAN_VALUE("是否NAN", "expression,replacement"),
    NVL("空值替换", "expression,replacement"),
    NANVL("NaN值替换"),
    NULLIF("相等返回NULL", "expression1,expression2"),
    NUM_NON_NULLS("参数列表中非NULL值数量", "expression1"),
    NUM_NULLS("参数列表中NULL值数量", "expression1")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    NullFunction(String title) {
        this(title, null, null);
    }
    NullFunction(String title, String params) {
        this(title, params, null);
    }
    NullFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    NullFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.NULL_HANDLE; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}