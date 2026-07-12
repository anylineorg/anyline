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
* BIT_OP 类函数定义
* 包含所有数据库的 BIT_OP 相关函数
*/
public enum BitFunction implements META {
		BIT_AND("按位与聚合", "expression1,expression2"),
		BIT_COUNT("位数", "expression"),
		BIT_IN("输入转换", DatabaseType.PostgreSQL),
		BIT_LENGTH("位长度", "expression"),
		BIT_OR("按位或聚合", "expression1,expression2"),
		BIT_OUT("输出转换", DatabaseType.PostgreSQL),
		BIT_RECV("二进制接收", DatabaseType.PostgreSQL),
		BIT_SEND("二进制发送", DatabaseType.PostgreSQL),
		BIT_XOR("按位异或聚合", "expression1,expression2"),
		BITAND("按位与", "expression1", DatabaseType.PostgreSQL),
		BITCAT("位串连接", DatabaseType.PostgreSQL),
		BITCMP("位串比较", DatabaseType.PostgreSQL),
		BITEQ("位串相等", DatabaseType.PostgreSQL),
		BITGE("位串大于等于", DatabaseType.PostgreSQL),
		BITGT("位串大于", DatabaseType.PostgreSQL),
		BITLE("位串小于等于", DatabaseType.PostgreSQL),
		BITLT("位串小于", DatabaseType.PostgreSQL),
		BITNE("位串不等", DatabaseType.PostgreSQL),
		BITNOT("按位取反", "expression", DatabaseType.PostgreSQL),
		BITOR("按位或", "expression1", DatabaseType.PostgreSQL),
		BITSHIFTLEFT("位左移", DatabaseType.PostgreSQL),
		BITSHIFTRIGHT("位右移", DatabaseType.PostgreSQL),
		BITTYPMODIN("位串类型修饰输入", DatabaseType.PostgreSQL),
		BITTYPMODOUT("位串类型修饰输出", DatabaseType.PostgreSQL),
		BITXOR("按位异或", "expression1", DatabaseType.PostgreSQL),
		GET_BIT("提取第n位", "expression"),
		GET_BYTE("提取第n个字节"),
		RID_BIT("rid位"),
		SET_BIT("二进制字符串中第n位", "expression"),
		SET_BYTE("二进制字符串中第n字节"),
		VARBIT("变长位串类型转换", DatabaseType.PostgreSQL),
		VARBIT_IN("输入转换", DatabaseType.PostgreSQL),
		VARBIT_OUT("输出转换", DatabaseType.PostgreSQL),
		VARBIT_RECV("二进制接收", DatabaseType.PostgreSQL),
		VARBIT_SEND("二进制发送", DatabaseType.PostgreSQL),
		VARBIT_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		VARBITCMP("变长位串比较", DatabaseType.PostgreSQL),
		VARBITEQ("变长位串等于", DatabaseType.PostgreSQL),
		VARBITGE("变长位串大于等于", DatabaseType.PostgreSQL),
		VARBITGT("变长位串大于", DatabaseType.PostgreSQL),
		VARBITLE("变长位串小于等于", DatabaseType.PostgreSQL),
		VARBITLT("变长位串小于", DatabaseType.PostgreSQL),
		VARBITNE("变长位串不等于", DatabaseType.PostgreSQL),
		VARBITTYPMODIN("变长位串类型修饰输入", DatabaseType.PostgreSQL),
		VARBITTYPMODOUT("变长位串类型修饰输出", DatabaseType.PostgreSQL),
		XOR("逻辑异或")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    BitFunction(String title) {
        this(title, null, null);
    }
    BitFunction(String title, String params) {
        this(title, params, null);
    }
    BitFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    BitFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.BIT_OP; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}