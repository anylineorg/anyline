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
* RANGE 类函数定义
* 包含所有数据库的 RANGE 相关函数
*/
public enum RangeFunction implements META {
		ANYCOMPATIBLEMULTIRANGE_IN("输入转换", DatabaseType.PostgreSQL),
		ANYCOMPATIBLEMULTIRANGE_OUT("输出转换", DatabaseType.PostgreSQL),
		ANYMULTIRANGE_IN("输入转换", DatabaseType.PostgreSQL),
		ANYMULTIRANGE_OUT("输出转换", DatabaseType.PostgreSQL),
		BINARY_UPGRADE_SET_NEXT_MULTIRANGE_PG_TYPE_OID("设置值", DatabaseType.PostgreSQL),
		ELEM_CONTAINED_BY_MULTIRANGE("是否被包含于", DatabaseType.PostgreSQL),
		ELEM_CONTAINED_BY_RANGE("是否被包含于", DatabaseType.PostgreSQL),
		ELEMENT_CONTAINED_BY_MULTIRANGE("元素是否被多区间包含", "element", DatabaseType.PostgreSQL),
		ELEMENT_CONTAINED_BY_RANGE("元素是否被单区间包含", "element", DatabaseType.PostgreSQL),
		IN_RANGE("值是否在范围内", DatabaseType.PostgreSQL),
		LOWER_INC("范围下限是否包含", DatabaseType.PostgreSQL),
		LOWER_INF("范围是否有下限", DatabaseType.PostgreSQL),
		LOWER_LIMIT("取字符串与其最近下限"),
		MULTI_RANGE("仅包含给定范围多范围", DatabaseType.PostgreSQL),
		MULTIRANGE("multirange操作符", DatabaseType.PostgreSQL),
		MULTIRANGE_ADJACENT_MULTIRANGE("是否相邻", DatabaseType.PostgreSQL),
		MULTIRANGE_ADJACENT_RANGE("是否相邻", DatabaseType.PostgreSQL),
		MULTIRANGE_AFTER_MULTIRANGE("是否在后面", DatabaseType.PostgreSQL),
		MULTIRANGE_AFTER_RANGE("是否在后面", DatabaseType.PostgreSQL),
		MULTIRANGE_BEFORE_MULTIRANGE("是否在前面", DatabaseType.PostgreSQL),
		MULTIRANGE_BEFORE_RANGE("是否在前面", DatabaseType.PostgreSQL),
		MULTIRANGE_CMP("比较", DatabaseType.PostgreSQL),
		MULTIRANGE_CONTAINED_BY_MULTIRANGE("是否被包含于", DatabaseType.PostgreSQL),
		MULTIRANGE_CONTAINED_BY_RANGE("是否被包含于", DatabaseType.PostgreSQL),
		MULTIRANGE_CONTAINS_ELEM("是否包含", DatabaseType.PostgreSQL),
		MULTIRANGE_CONTAINS_MULTIRANGE("是否包含", DatabaseType.PostgreSQL),
		MULTIRANGE_CONTAINS_RANGE("是否包含", DatabaseType.PostgreSQL),
		MULTIRANGE_EQ("相等判断", DatabaseType.PostgreSQL),
		MULTIRANGE_GE("大于等于判断", DatabaseType.PostgreSQL),
		MULTIRANGE_GIST_COMPRESS("索引压缩", DatabaseType.PostgreSQL),
		MULTIRANGE_GIST_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		MULTIRANGE_GT("大于判断", DatabaseType.PostgreSQL),
		MULTIRANGE_IN("输入转换", DatabaseType.PostgreSQL),
		MULTIRANGE_INTERSECT("输入转换", DatabaseType.PostgreSQL),
		MULTIRANGE_INTERSECT_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		MULTIRANGE_LE("小于等于判断", DatabaseType.PostgreSQL),
		MULTIRANGE_LT("小于判断", DatabaseType.PostgreSQL),
		MULTIRANGE_MINUS("差集计算", DatabaseType.PostgreSQL),
		MULTIRANGE_NE("不等判断", DatabaseType.PostgreSQL),
		MULTIRANGE_OUT("输出转换", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERLAPS_MULTIRANGE("是否重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERLAPS_RANGE("是否重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERLEFT_MULTIRANGE("是否左侧重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERLEFT_RANGE("是否左侧重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERRIGHT_MULTIRANGE("是否右侧重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_OVERRIGHT_RANGE("是否右侧重叠", DatabaseType.PostgreSQL),
		MULTIRANGE_RECV("二进制接收", DatabaseType.PostgreSQL),
		MULTIRANGE_SEND("二进制发送", DatabaseType.PostgreSQL),
		MULTIRANGE_TYPANALYZE("类型分析", DatabaseType.PostgreSQL),
		MULTIRANGE_UNION("索引合并", DatabaseType.PostgreSQL),
		MULTIRANGESEL("multirange操作符", DatabaseType.PostgreSQL),
		NUMMULTIRANGE("数值多范围", DatabaseType.PostgreSQL),
		NUMRANGE("数值范围", DatabaseType.PostgreSQL),
		NUMRANGE_SUBDIFF("子类型差异计算", DatabaseType.PostgreSQL),
		RANGE_ADJACENT("是否相邻", DatabaseType.PostgreSQL),
		RANGE_ADJACENT_MULTIRANGE("是否相邻", DatabaseType.PostgreSQL),
		RANGE_AFTER("是否在后面", DatabaseType.PostgreSQL),
		RANGE_AFTER_MULTIRANGE("是否在后面", DatabaseType.PostgreSQL),
		RANGE_AGG("非空输入值并集", DatabaseType.PostgreSQL),
		RANGE_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		RANGE_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		RANGE_BEFORE("是否在前面", DatabaseType.PostgreSQL),
		RANGE_BEFORE_MULTIRANGE("是否在前面", DatabaseType.PostgreSQL),
		RANGE_CMP("比较", DatabaseType.PostgreSQL),
		RANGE_CONTAINED_BY("是否被包含于", DatabaseType.PostgreSQL),
		RANGE_CONTAINED_BY_MULTIRANGE("是否被包含于", DatabaseType.PostgreSQL),
		RANGE_CONTAINS("是否包含", DatabaseType.PostgreSQL),
		RANGE_CONTAINS_ELEM("是否包含", DatabaseType.PostgreSQL),
		RANGE_CONTAINS_MULTIRANGE("是否包含", DatabaseType.PostgreSQL),
		RANGE_EQ("相等判断", DatabaseType.PostgreSQL),
		RANGE_GE("大于等于判断", DatabaseType.PostgreSQL),
		RANGE_GIST_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		RANGE_GIST_PENALTY("索引代价惩罚", DatabaseType.PostgreSQL),
		RANGE_GIST_PICKSPLIT("索引分裂策略", DatabaseType.PostgreSQL),
		RANGE_GIST_SAME("索引相等性检查", DatabaseType.PostgreSQL),
		RANGE_GIST_UNION("索引合并", DatabaseType.PostgreSQL),
		RANGE_GT("大于判断", DatabaseType.PostgreSQL),
		RANGE_IN("输入转换", DatabaseType.PostgreSQL),
		RANGE_INTERSECT("输入转换", DatabaseType.PostgreSQL),
		RANGE_INTERSECT_AGG("非空输入值交集", DatabaseType.PostgreSQL),
		RANGE_INTERSECT_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		RANGE_LE("小于等于判断", DatabaseType.PostgreSQL),
		RANGE_LT("小于判断", DatabaseType.PostgreSQL),
		RANGE_MERGE("包括整个多范围最小范围", DatabaseType.PostgreSQL),
		RANGE_MINUS("差集计算", DatabaseType.PostgreSQL),
		RANGE_NE("不等判断", DatabaseType.PostgreSQL),
		RANGE_OUT("输出转换", DatabaseType.PostgreSQL),
		RANGE_OVERLAPS("是否重叠", DatabaseType.PostgreSQL),
		RANGE_OVERLAPS_MULTIRANGE("是否重叠", DatabaseType.PostgreSQL),
		RANGE_OVERLEFT("是否左侧重叠", DatabaseType.PostgreSQL),
		RANGE_OVERLEFT_MULTIRANGE("是否左侧重叠", DatabaseType.PostgreSQL),
		RANGE_OVERRIGHT("是否右侧重叠", DatabaseType.PostgreSQL),
		RANGE_OVERRIGHT_MULTIRANGE("是否右侧重叠", DatabaseType.PostgreSQL),
		RANGE_RECV("二进制接收", DatabaseType.PostgreSQL),
		RANGE_SEND("二进制发送", DatabaseType.PostgreSQL),
		RANGE_TYPANALYZE("类型分析", DatabaseType.PostgreSQL),
		RANGE_UNION("索引合并", DatabaseType.PostgreSQL),
		TSMULTIRANGE("时间戳多范围", DatabaseType.PostgreSQL),
		TSRANGE("时间戳范围", DatabaseType.PostgreSQL),
		TSRANGE_SUBDIFF("子类型差异计算", DatabaseType.PostgreSQL),
		UPPER_INC("范围上限是否包含", DatabaseType.PostgreSQL),
		UPPER_INF("范围是否有上限", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    RangeFunction(String title) {
        this(title, null, null);
    }
    RangeFunction(String title, String params) {
        this(title, params, null);
    }
    RangeFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    RangeFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.RANGE; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}