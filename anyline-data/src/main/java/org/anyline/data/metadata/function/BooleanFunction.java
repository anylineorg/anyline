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
* BOOLEAN 类函数定义
* 包含所有数据库的 BOOLEAN 相关函数
*/
public enum BooleanFunction implements META {
		AND("逻辑与"),
		BLOB_EQUAL("相等判断"),
		BOOL("布尔类型转换", DatabaseType.PostgreSQL),
		BOOL_ACCUM("累加器", DatabaseType.PostgreSQL),
		BOOL_ACCUM_INV("布尔反向累加", DatabaseType.PostgreSQL),
		BOOL_ALLTRUE("所有布尔值为真", DatabaseType.PostgreSQL),
		BOOL_ANYTRUE("任一布尔值为真", DatabaseType.PostgreSQL),
		BOOLAND_STATEFUNC("布尔与状态", DatabaseType.PostgreSQL),
		BOOLEQ("布尔相等", DatabaseType.PostgreSQL),
		BOOLGE("布尔大于等于", DatabaseType.PostgreSQL),
		BOOLGT("布尔大于", DatabaseType.PostgreSQL),
		BOOLIN("布尔输入", DatabaseType.PostgreSQL),
		BOOLLE("布尔小于等于", DatabaseType.PostgreSQL),
		BOOLLT("布尔小于", DatabaseType.PostgreSQL),
		BOOLNE("布尔不等", DatabaseType.PostgreSQL),
		BOOLOR_STATEFUNC("布尔或状态", DatabaseType.PostgreSQL),
		BOOLOUT("布尔输出", DatabaseType.PostgreSQL),
		BOOLRECV("布尔接收", DatabaseType.PostgreSQL),
		BOOLSEND("布尔发送", DatabaseType.PostgreSQL),
		CASH_CMP("比较", DatabaseType.PostgreSQL),
		CASH_EQ("相等判断", DatabaseType.PostgreSQL),
		CASH_GE("大于等于判断", DatabaseType.PostgreSQL),
		CASH_GT("大于判断", DatabaseType.PostgreSQL),
		CASH_LE("小于等于判断", DatabaseType.PostgreSQL),
		CASH_LT("小于判断", DatabaseType.PostgreSQL),
		CASH_NE("不等判断", DatabaseType.PostgreSQL),
		ENUM_CMP("比较", DatabaseType.PostgreSQL),
		ENUM_EQ("相等判断", DatabaseType.PostgreSQL),
		ENUM_GE("大于等于判断", DatabaseType.PostgreSQL),
		ENUM_GT("大于判断", DatabaseType.PostgreSQL),
		ENUM_LE("小于等于判断", DatabaseType.PostgreSQL),
		ENUM_LT("小于判断", DatabaseType.PostgreSQL),
		ENUM_NE("不等判断", DatabaseType.PostgreSQL),
		GREAT("最大值", "expression1"),
		IFNULL("非空判断", "expression1"),
		IS_EMPTY("范围是否空"),
		IS_FALSE("一个bool值是否为FALSE"),
		IS_IPV4("是否为IPv4"),
		IS_IPV6("是否为IPv6"),
		IS_NOT("按位非"),
		IS_NOT_FALSE("一个bool值是否不为FALSE"),
		IS_NOT_NULL("按位非", "expression"),
		IS_NOT_TRUE("一个bool值是否不为TRUE"),
		IS_NULL("是否为NULL", "expression"),
		IS_TRUE("一个bool值是否为TRUE"),
		IS_UUID("是否为UUID", "expression"),
		ISNULL("判断是否为空", "expression"),
		ISNUMERIC("判断是否为数值"),
		NOT("逻辑非"),
		NULL_EQU("相等判断"),
		NVL2("双参数空值替换", "expression,not_null,null"),
		OR("逻辑或"),
		RECORD_EQ("相等判断", DatabaseType.PostgreSQL),
		RECORD_GE("大于等于判断", DatabaseType.PostgreSQL),
		RECORD_GT("大于判断", DatabaseType.PostgreSQL),
		RECORD_LE("小于等于判断", DatabaseType.PostgreSQL),
		RECORD_LT("小于判断", DatabaseType.PostgreSQL),
		RECORD_NE("不等判断", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    BooleanFunction(String title) {
        this(title, null, null);
    }
    BooleanFunction(String title, String params) {
        this(title, params, null);
    }
    BooleanFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    BooleanFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.BOOLEAN; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}