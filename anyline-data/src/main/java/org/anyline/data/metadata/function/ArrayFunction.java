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
* ARRAY 类函数定义
* 包含所有数据库的 ARRAY 相关函数
*/
public enum ArrayFunction implements META {
		ANYARRAY_IN("输入转换", DatabaseType.PostgreSQL),
		ANYARRAY_OUT("输出转换", DatabaseType.PostgreSQL),
		ANYARRAY_RECV("二进制接收", DatabaseType.PostgreSQL),
		ANYARRAY_SEND("二进制发送", DatabaseType.PostgreSQL),
		ANYCOMPATIBLEARRAY_IN("输入转换", DatabaseType.PostgreSQL),
		ANYCOMPATIBLEARRAY_OUT("输出转换", DatabaseType.PostgreSQL),
		ANYCOMPATIBLEARRAY_RECV("二进制接收", DatabaseType.PostgreSQL),
		ANYCOMPATIBLEARRAY_SEND("二进制发送", DatabaseType.PostgreSQL),
		ARRAY_AGG("数组聚合", "expression"),
		ARRAY_AGG_ARRAY_FINALFN("数组聚合终结", DatabaseType.PostgreSQL),
		ARRAY_AGG_ARRAY_TRANSFN("数组聚合过渡", DatabaseType.PostgreSQL),
		ARRAY_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		ARRAY_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		ARRAY_APPEND("元素附加到数组末尾", "expression,element", DatabaseType.PostgreSQL),
		ARRAY_CAT("连接两个数组", "array1,array2", DatabaseType.PostgreSQL),
		ARRAY_DIMS("数组维度文本表示形式", "expression", DatabaseType.PostgreSQL),
		ARRAY_EQ("相等判断", DatabaseType.PostgreSQL),
		ARRAY_FILL("填充数组", "expression,dimensions", DatabaseType.PostgreSQL),
		ARRAY_GE("大于等于判断", DatabaseType.PostgreSQL),
		ARRAY_GT("大于判断", DatabaseType.PostgreSQL),
		ARRAY_IN("输入转换", DatabaseType.PostgreSQL),
		ARRAY_LARGER("取较大值", DatabaseType.PostgreSQL),
		ARRAY_LE("小于等于判断", DatabaseType.PostgreSQL),
		ARRAY_LENGTH("数组维度长度", "expression,dimension", DatabaseType.PostgreSQL),
		ARRAY_LOWER("数组维度下限", "expression,dimension", DatabaseType.PostgreSQL),
		ARRAY_LT("小于判断", DatabaseType.PostgreSQL),
		ARRAY_NDIMS("数组的维度数", "expression", DatabaseType.PostgreSQL),
		ARRAY_NE("不等判断", DatabaseType.PostgreSQL),
		ARRAY_OUT("输出转换", DatabaseType.PostgreSQL),
		ARRAY_POSITION("数组中第二个参数首次出现下标", "expression,element", DatabaseType.PostgreSQL),
		ARRAY_POSITIONS("查找所有下标", "array,expression", DatabaseType.PostgreSQL),
		ARRAY_PREPEND("在数组开头前加一个元素", "expression,array", DatabaseType.PostgreSQL),
		ARRAY_RECV("二进制接收", DatabaseType.PostgreSQL),
		ARRAY_REMOVE("移除数组元素", "expression,element", DatabaseType.PostgreSQL),
		ARRAY_REPLACE("替换数组元素", "expression,search,replace", DatabaseType.PostgreSQL),
		ARRAY_REVERSE("数组第一维度反转", "expression", DatabaseType.PostgreSQL),
		ARRAY_SAMPLE("随机取N个元素", "expression,n", DatabaseType.PostgreSQL),
		ARRAY_SEND("二进制发送", DatabaseType.PostgreSQL),
		ARRAY_SHUFFLE("随机打乱数组第一个维度", "expression", DatabaseType.PostgreSQL),
		ARRAY_SMALLER("取较小值", DatabaseType.PostgreSQL),
		ARRAY_SORT("对数组第一个维度进行排序", "expression", DatabaseType.PostgreSQL),
		ARRAY_SUBSCRIPT_HANDLER("处理器", DatabaseType.PostgreSQL),
		ARRAY_TO_JSON("数组转JSON", "expression", DatabaseType.PostgreSQL),
		ARRAY_TO_STRING("数组转文本", "expression,delimiter", DatabaseType.PostgreSQL),
		ARRAY_TO_TSVECTOR("文本数组转词位", "expression", DatabaseType.PostgreSQL),
		ARRAY_TYPANALYZE("数组类型分析", DatabaseType.PostgreSQL),
		ARRAY_UNNEST_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		ARRAY_UPPER("数组维度上限", "expression,dimension", DatabaseType.PostgreSQL),
		ARRAYCONTAINED("数组是否被包含", DatabaseType.PostgreSQL),
		ARRAYCONTAINS("数组是否包含", DatabaseType.PostgreSQL),
		ARRAYCONTJOINSEL("数组包含连接率", DatabaseType.PostgreSQL),
		ARRAYCONTSEL("数组包含选择率", DatabaseType.PostgreSQL),
		ARRAYOVERLAP("数组是否重叠", DatabaseType.PostgreSQL),
		BINARY_UPGRADE_SET_NEXT_ARRAY_PG_TYPE_OID("设置值", DatabaseType.PostgreSQL),
		BINARY_UPGRADE_SET_NEXT_MULTIRANGE_ARRAY_PG_TYPE_OID("设置值", DatabaseType.PostgreSQL),
		CARDINALITY("数组中的元素总数", "array"),
		GENERATE_SERIES("生成序列", DatabaseType.PostgreSQL),
		GENERATE_SERIES_INT4_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		GENERATE_SERIES_INT8_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		GENERATE_SUBSCRIPTS("生成下标", DatabaseType.PostgreSQL),
		OIDVECTOREQ("OID向量等于", DatabaseType.PostgreSQL),
		OIDVECTORGE("OID向量大于等于", DatabaseType.PostgreSQL),
		OIDVECTORGT("OID向量大于", DatabaseType.PostgreSQL),
		OIDVECTORIN("输入", DatabaseType.PostgreSQL),
		OIDVECTORLE("OID向量小于等于", DatabaseType.PostgreSQL),
		OIDVECTORLT("OID向量小于", DatabaseType.PostgreSQL),
		OIDVECTORNE("OID向量不等于", DatabaseType.PostgreSQL),
		OIDVECTOROUT("输出", DatabaseType.PostgreSQL),
		OIDVECTORRECV("接收", DatabaseType.PostgreSQL),
		OIDVECTORSEND("发送", DatabaseType.PostgreSQL),
		OIDVECTORTYPES("OID向量类型", DatabaseType.PostgreSQL),
		RAW_ARRAY_SUBSCRIPT_HANDLER("处理器", DatabaseType.PostgreSQL),
		TRIM_ARRAY("修剪数组尾部", "array", DatabaseType.PostgreSQL),
		UNNEST("a扩展为一组行，每个词位一个")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    ArrayFunction(String title) {
        this(title, null, null);
    }
    ArrayFunction(String title, String params) {
        this(title, params, null);
    }
    ArrayFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    ArrayFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.ARRAY; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}