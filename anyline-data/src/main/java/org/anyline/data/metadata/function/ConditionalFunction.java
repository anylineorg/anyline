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
* CONDITIONAL 类函数定义
* 包含所有数据库的 CONDITIONAL 相关函数
*/
public enum ConditionalFunction implements META {
		ANY_VALUE("任意值", "expression"),
		BETWEEN_AND("否在值范围内"),
		CASE("条件分支CASE", "condition"),
		CHOOSE("选择值", "index"),
		DECODE("条件解码", "expression,search,result", DatabaseType.Oracle),
		DECODE_FUNC("条件解码", "expr"),
		EXISTS("结果是否包含任何行", "subquery"),
		GREATEST("最大值", "expression1,expression2"),
		IF("条件判断", "condition"),
		IF_ELSE("条件判断", "condition"),
		IIF("即时条件判断", "condition"),
		IN("值是否在一组值中", "expression,set"),
		INTERVAL("小于第一个参数索引", "expression,interval"),
		IS("针对布尔值测试值"),
		LEAST("最小值", "expression1,expression2"),
		LNNVL("逻辑条件取反", "condition", DatabaseType.Oracle),
		MEMBER_OF("一个值是否存在于数组中", "expression,set"),
		MERGE_ACTION("为当前行执行合并作命令"),
		PRESENTNNV("Present非空", "expression", DatabaseType.Oracle),
		PRESENTV("Present值", "expression", DatabaseType.Oracle),
		PREVIOUS("前一个值", "expression", DatabaseType.Oracle)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    ConditionalFunction(String title) {
        this(title, null, null);
    }
    ConditionalFunction(String title, String params) {
        this(title, params, null);
    }
    ConditionalFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    ConditionalFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.CONDITIONAL; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}