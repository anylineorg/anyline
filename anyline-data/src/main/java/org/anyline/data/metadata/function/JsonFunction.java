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
* JSON 类函数定义
* 包含所有数据库的 JSON 相关函数
*/
public enum JsonFunction implements META {
		BSON_TO_JSON("BSON转JSON", "bson"),
		JSON_AGG("JSON聚合"),
		JSON_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		JSON_AGG_STRICT("聚合为JSON数组", DatabaseType.PostgreSQL),
		JSON_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		JSON_ARRAY("构造JSON数组", "expression"),
		JSON_ARRAY_AGG("JSON数组聚合", "expression", DatabaseType.PostgreSQL),
		JSON_ARRAY_APPEND("JSON数组追加", "json,path,expression"),
		JSON_ARRAY_ELEMENT("获取元素", DatabaseType.PostgreSQL),
		JSON_ARRAY_ELEMENT_TEXT("获取元素", DatabaseType.PostgreSQL),
		JSON_ARRAY_ELEMENTS("获取元素", DatabaseType.PostgreSQL),
		JSON_ARRAY_ELEMENTS_TEXT("获取元素", DatabaseType.PostgreSQL),
		JSON_ARRAY_INSERT("JSON数组插入", "json,path,expression"),
		JSON_ARRAY_LENGTH("数组长度", DatabaseType.PostgreSQL),
		JSON_ARRAYAGG("JSON数组聚合"),
		JSON_BUILD_ARRAY("构造数组", DatabaseType.PostgreSQL),
		JSON_BUILD_OBJECT("构造对象", DatabaseType.PostgreSQL),
		JSON_CONTAINS("JSON包含", "json,expression"),
		JSON_CONTAINS_PATH("JSON路径包含", "json,path", DatabaseType.PostgreSQL),
		JSON_DATA_GUIDE("聚合过渡:json_data_guide", "json", DatabaseType.PostgreSQL),
		JSON_DEPTH("JSON深度", "json"),
		JSON_EACH("展开为键值对", DatabaseType.PostgreSQL),
		JSON_EACH_TEXT("展开为键值对", DatabaseType.PostgreSQL),
		JSON_EXISTS("JSON路径存在"),
		JSON_EXTRACT("提取JSON值", "json,path"),
		JSON_EXTRACT_PATH("按指定路径(VARIADIC)提取JSON值", DatabaseType.PostgreSQL),
		JSON_EXTRACT_PATH_TEXT("索引键提取", DatabaseType.PostgreSQL),
		JSON_IN("输入转换", DatabaseType.PostgreSQL),
		JSON_INSERT("插入JSON值", "json,path,expression"),
		JSON_KEYS("JSON键列表", "json"),
		JSON_LENGTH("JSON长度", "json"),
		JSON_MERGE("合并JSON，保留重复键", "json1,json2"),
		JSON_MERGE_PATCH("合并JSON(PATCH)", "json1,json2"),
		JSON_MERGE_PRESERVE("合并JSON(保留)", "json1,json2"),
		JSON_MODIFY("聚合过渡:json_modify"),
		JSON_OBJECT("构造JSON对象", "key,expression"),
		JSON_OBJECT_AGG("JSON对象聚合", "key,expression"),
		JSON_OBJECT_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		JSON_OBJECT_AGG_STRICT("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSON_OBJECT_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		JSON_OBJECT_AGG_UNIQUE("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSON_OBJECT_AGG_UNIQUE_STRICT("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSON_OBJECT_FIELD("聚合过渡:json_object_field", DatabaseType.PostgreSQL),
		JSON_OBJECT_FIELD_TEXT("聚合过渡:json_object_field_text", DatabaseType.PostgreSQL),
		JSON_OBJECT_KEYS("获取对象键", DatabaseType.PostgreSQL),
		JSON_OBJECTAGG("JSON对象聚合"),
		JSON_OUT("输出转换", DatabaseType.PostgreSQL),
		JSON_OVERLAPS("JSON重叠判断", "json1,json2"),
		JSON_PATH_EXISTS("判断键是否存在"),
		JSON_POPULATE_RECORD("填充记录", DatabaseType.PostgreSQL),
		JSON_POPULATE_RECORDSET("填充记录", DatabaseType.PostgreSQL),
		JSON_PRETTY("JSON美化", "json"),
		JSON_QUERY("JSON查询", "json,path"),
		JSON_QUOTE("JSON引号化", "expression"),
		JSON_RECV("二进制接收", DatabaseType.PostgreSQL),
		JSON_REMOVE("删除JSON值", "json,path"),
		JSON_REPLACE("替换JSON值", "json,path,expression"),
		JSON_SCALAR("聚合过渡:json_scalar"),
		JSON_SCHEMA_VALID("JSON模式验证", "schema,json"),
		JSON_SCHEMA_VALIDATION_REPORT("JSON模式验证报告", "schema,json", DatabaseType.PostgreSQL),
		JSON_SEARCH("JSON搜索", "json,path,search"),
		JSON_SEND("二进制发送", DatabaseType.PostgreSQL),
		JSON_SERIALIZE("序列化"),
		JSON_SET("设置JSON值", "json,path,expression"),
		JSON_STORAGE_FREE("JSON释放空间", "json"),
		JSON_STORAGE_SIZE("JSON存储大小", "json"),
		JSON_STRIP_NULLS("去除null字段", DatabaseType.PostgreSQL),
		JSON_TABLE("JSON表", "json,path"),
		JSON_TO_BSON("聚合过渡:json_to_bson"),
		JSON_TO_RECORD("转为记录", DatabaseType.PostgreSQL),
		JSON_TO_RECORDSET("转为记录", DatabaseType.PostgreSQL),
		JSON_TO_TS_VECTOR("文本规范化", DatabaseType.PostgreSQL),
		JSON_TYPE("JSON类型", "json"),
		JSON_TYPEOF("获取类型", DatabaseType.PostgreSQL),
		JSON_UNQUOTE("JSON去引号", "json"),
		JSON_VALID("JSON是否有效", "json"),
		JSON_VALUE("JSON值提取", "json,path"),
		JSONB_AGG("JSONB聚合", DatabaseType.PostgreSQL),
		JSONB_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		JSONB_AGG_STRICT("聚合为JSON数组", DatabaseType.PostgreSQL),
		JSONB_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		JSONB_ARRAY_ELEMENT("获取元素", DatabaseType.PostgreSQL),
		JSONB_ARRAY_ELEMENT_TEXT("获取元素", DatabaseType.PostgreSQL),
		JSONB_ARRAY_ELEMENTS("获取元素", DatabaseType.PostgreSQL),
		JSONB_ARRAY_ELEMENTS_TEXT("获取元素", DatabaseType.PostgreSQL),
		JSONB_ARRAY_LENGTH("数组长度", DatabaseType.PostgreSQL),
		JSONB_BUILD_ARRAY("构造数组", DatabaseType.PostgreSQL),
		JSONB_BUILD_OBJECT("构造对象", DatabaseType.PostgreSQL),
		JSONB_CMP("比较", DatabaseType.PostgreSQL),
		JSONB_CONCAT("连接", DatabaseType.PostgreSQL),
		JSONB_CONTAINED("被包含判断", DatabaseType.PostgreSQL),
		JSONB_CONTAINS("是否包含", DatabaseType.PostgreSQL),
		JSONB_DELETE("删除键", DatabaseType.PostgreSQL),
		JSONB_DELETE_PATH("删除键", DatabaseType.PostgreSQL),
		JSONB_EACH("展开为键值对", DatabaseType.PostgreSQL),
		JSONB_EACH_TEXT("展开为键值对", DatabaseType.PostgreSQL),
		JSONB_EQ("相等判断", DatabaseType.PostgreSQL),
		JSONB_EXISTS("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_EXISTS_ALL("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_EXISTS_ANY("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_EXTRACT_PATH("索引键提取", DatabaseType.PostgreSQL),
		JSONB_EXTRACT_PATH_TEXT("索引键提取", DatabaseType.PostgreSQL),
		JSONB_GE("大于等于判断", DatabaseType.PostgreSQL),
		JSONB_GT("大于判断", DatabaseType.PostgreSQL),
		JSONB_IN("输入转换", DatabaseType.PostgreSQL),
		JSONB_INSERT("输入转换", "json,path,expression", DatabaseType.PostgreSQL),
		JSONB_LE("小于等于判断", DatabaseType.PostgreSQL),
		JSONB_LT("小于判断", DatabaseType.PostgreSQL),
		JSONB_NE("不等判断", DatabaseType.PostgreSQL),
		JSONB_OBJECT("聚合过渡:jsonb_object", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG("JSONB对象聚合", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG_STRICT("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG_UNIQUE("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSONB_OBJECT_AGG_UNIQUE_STRICT("所有键/值对收集到JSON对象中", DatabaseType.PostgreSQL),
		JSONB_OBJECT_FIELD("聚合过渡:jsonb_object_field", DatabaseType.PostgreSQL),
		JSONB_OBJECT_FIELD_TEXT("聚合过渡:jsonb_object_field_text", DatabaseType.PostgreSQL),
		JSONB_OBJECT_KEYS("获取对象键", DatabaseType.PostgreSQL),
		JSONB_OUT("输出转换", DatabaseType.PostgreSQL),
		JSONB_PATH_EXISTS("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_PATH_EXISTS_OPR("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_PATH_EXISTS_TZ("判断键是否存在", DatabaseType.PostgreSQL),
		JSONB_PATH_MATCH("匹配", DatabaseType.PostgreSQL),
		JSONB_PATH_MATCH_OPR("匹配", DatabaseType.PostgreSQL),
		JSONB_PATH_MATCH_TZ("匹配", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY_ARRAY("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY_ARRAY_TZ("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY_FIRST("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY_FIRST_TZ("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_PATH_QUERY_TZ("JSONPath查询", DatabaseType.PostgreSQL),
		JSONB_POPULATE_RECORD("填充记录", DatabaseType.PostgreSQL),
		JSONB_POPULATE_RECORD_VALID("聚合过渡:jsonb_populate_record_val", DatabaseType.PostgreSQL),
		JSONB_POPULATE_RECORDSET("填充记录", DatabaseType.PostgreSQL),
		JSONB_PRETTY("格式化输出", DatabaseType.PostgreSQL),
		JSONB_RECV("二进制接收", DatabaseType.PostgreSQL),
		JSONB_SEND("二进制发送", DatabaseType.PostgreSQL),
		JSONB_SET("设置值", "json,path,expression", DatabaseType.PostgreSQL),
		JSONB_SET_LAX("宽松设置", DatabaseType.PostgreSQL),
		JSONB_STRIP_NULLS("去除null字段", DatabaseType.PostgreSQL),
		JSONB_SUBSCRIPT_HANDLER("处理器", DatabaseType.PostgreSQL),
		JSONB_TO_RECORD("转为记录", DatabaseType.PostgreSQL),
		JSONB_TO_RECORDSET("转为记录", DatabaseType.PostgreSQL),
		JSONB_TYPEOF("获取类型", DatabaseType.PostgreSQL),
		JSONPATH_IN("输入转换", DatabaseType.PostgreSQL),
		JSONPATH_OUT("输出转换", DatabaseType.PostgreSQL),
		JSONPATH_RECV("二进制接收", DatabaseType.PostgreSQL),
		JSONPATH_SEND("二进制发送", DatabaseType.PostgreSQL),
		ROW_TO_JSON("行转JSON", DatabaseType.PostgreSQL),
		TO_JSON("转JSON"),
		TO_JSONB("转JSONB", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    JsonFunction(String title) {
        this(title, null, null);
    }
    JsonFunction(String title, String params) {
        this(title, params, null);
    }
    JsonFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    JsonFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.JSON; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}