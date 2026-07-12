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
* CATALOG 类函数定义
* 包含所有数据库的 CATALOG 相关函数
*/
public enum CatalogFunction implements META {
		BASE_TYPE("基础类型", "type_oid", DatabaseType.PostgreSQL),
		BINARY_UPGRADE_SET_NEXT_PG_AUTHID_OID("设置值", DatabaseType.PostgreSQL),
		BRINHANDLER("BRIN索引处理器", DatabaseType.PostgreSQL),
		COL_DESCRIPTION("列注释", "table_oid,expression_number", DatabaseType.PostgreSQL),
		DESCRIBE_OBJECT("数据库对象文本描述", "type,obj_oid,expression_oid", DatabaseType.PostgreSQL),
		ENUM_FIRST("枚举类型第一个值", "enum_type", DatabaseType.PostgreSQL),
		ENUM_LARGER("取较大值", DatabaseType.PostgreSQL),
		ENUM_LAST("枚举类型最后一个值", "enum_type", DatabaseType.PostgreSQL),
		ENUM_RANGE("两个给定枚举值之间范围", "enum_type", DatabaseType.PostgreSQL),
		ENUM_SMALLER("取较小值", DatabaseType.PostgreSQL),
		EVENT_TRIGGER_DDL_COMMANDS("每个用户作执行DDL命令列表", DatabaseType.PostgreSQL),
		EVENT_TRIGGER_TABLE_REWRITE_OID("即将重写表OID", DatabaseType.PostgreSQL),
		EVENT_TRIGGER_TABLE_REWRITE_REASON("解释重写原因代码", DatabaseType.PostgreSQL),
		FILE_NODE_RELATION("给定表空间OID和存储它文件节点", DatabaseType.PostgreSQL),
		FORMAT_TYPE("数据类型SQL名", DatabaseType.PostgreSQL),
		FUNCTION_IS_VISIBLE("搜索路径中功能可见", DatabaseType.PostgreSQL),
		GET_CATALOG_FOREIGN_KEYS("外键关系", DatabaseType.PostgreSQL),
		GET_CONSTRAINT_DEF("重建约束创建命令", DatabaseType.PostgreSQL),
		GET_EXPR("反编译表达式", DatabaseType.PostgreSQL),
		GET_FUNCTION_ARGUMENTS("重建过程参数列表", DatabaseType.PostgreSQL),
		GET_FUNCTION_DEF("重建或过程创建命令", DatabaseType.PostgreSQL),
		GET_FUNCTION_IDENTITY_ARGUMENTS("重建识别或过程所需参数列表", DatabaseType.PostgreSQL),
		GET_FUNCTION_RESULT("重建的子句", DatabaseType.PostgreSQL),
		GET_INDEX_DEF("重建索引创建命令", DatabaseType.PostgreSQL),
		GET_KEYWORDS("描述服务器识别关键字", DatabaseType.PostgreSQL),
		GET_LOADED_MODULES("已加载模块列表", DatabaseType.PostgreSQL),
		GET_OBJECT_ADDRESS("标识类型代码对象", DatabaseType.PostgreSQL),
		GET_PART_KEY_DEF("重建分区表分区键定义", DatabaseType.PostgreSQL),
		GET_RULE_DEF("重建规则创建命令", DatabaseType.PostgreSQL),
		GET_STATISTICS_OBJECT_DEF("重建扩展统计信息对象创建命令", DatabaseType.PostgreSQL),
		GET_TRIGGER_DEF("重建触发器创建命令", DatabaseType.PostgreSQL),
		GET_VIEW_DEF("重建视图或具体化视图基础命令", DatabaseType.PostgreSQL),
		GINARRAYCONSISTENT("GIN数组一致性", DatabaseType.PostgreSQL),
		GINARRAYEXTRACT("GIN数组提取", DatabaseType.PostgreSQL),
		GINARRAYTRICONSISTENT("GIN数组三值一致性", DatabaseType.PostgreSQL),
		GINHANDLER("GIN索引处理器", DatabaseType.PostgreSQL),
		GINQUERYARRAYEXTRACT("GIN查询数组提取", DatabaseType.PostgreSQL),
		GISTHANDLER("GiST索引处理器", DatabaseType.PostgreSQL),
		IDENTIFY_OBJECT("包含足够信息行", DatabaseType.PostgreSQL),
		IDENTIFY_OBJECT_AS_ADDRESS("标识数据库对象", DatabaseType.PostgreSQL),
		INDEX_AM_HAS_PROPERTY("索引属性检查", DatabaseType.PostgreSQL),
		INDEX_COLUMN_HAS_PROPERTY("测试索引列是否具有命名属性", DatabaseType.PostgreSQL),
		INDEX_HAS_PROPERTY("测试索引是否具有命名属性", DatabaseType.PostgreSQL),
		MCV_LIST_ITEMS("存储在多列MCV列表中所有项目", DatabaseType.PostgreSQL),
		OBJ_DESCRIPTION("获得对象描述信息", "object_id", DatabaseType.PostgreSQL),
		OBJECTPROPERTY("对象属性"),
		OBJECTPROPERTYEX("对象扩展属性"),
		OPERATOR_CLASS_IS_VISIBLE("运算符类在搜索路径中可见", DatabaseType.PostgreSQL),
		OPERATOR_FAMILY_IS_VISIBLE("运算符族在搜索路径中是否可见", DatabaseType.PostgreSQL),
		OPERATOR_IS_VISIBLE("运算符在搜索路径中可见", DatabaseType.PostgreSQL),
		OPTIONS_TO_TABLE("存储选项集", DatabaseType.PostgreSQL),
		PARTITION_ANCESTORS("列出给定分区祖先关系", DatabaseType.PostgreSQL),
		PARTITION_ROOT("给定关系所属分区树最顶层父级", DatabaseType.PostgreSQL),
		PARTITION_TREE("列出分区树", DatabaseType.PostgreSQL),
		SH_OBJECT_DESCRIPTION("获取对象注释", DatabaseType.PostgreSQL),
		SPGHANDLER("SP-GiST索引处理器", DatabaseType.PostgreSQL),
		STATISTICS_OBJ_IS_VISIBLE("统计对象在搜索路径中是否可见", DatabaseType.PostgreSQL),
		TABLE_IS_VISIBLE("表格在搜索路径中可见", DatabaseType.PostgreSQL),
		TO_REG_CLASS("文本关系名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_COLLATION("文本排序规则名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_NAMESPACE("文本架构名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_OPER("文本运算符名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_OPERATOR("运算符名转OID", DatabaseType.PostgreSQL),
		TO_REG_PROC("文本或过程名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_PROCEDURE("过程名转OID", DatabaseType.PostgreSQL),
		TO_REG_ROLE("文本角色名称转换为其OID", DatabaseType.PostgreSQL),
		TO_REG_TYPE("解析文本字符串", DatabaseType.PostgreSQL),
		TO_REG_TYPE_MOD("解析文本字符串", DatabaseType.PostgreSQL),
		TRIGGER_DEPTH("触发器的当前嵌套级别", DatabaseType.Oracle),
		TYPE_IS_VISIBLE("类型（或域）在搜索路径中是否可见", DatabaseType.PostgreSQL),
		TYPEOF("传递给它值数据类型OID", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    CatalogFunction(String title) {
        this(title, null, null);
    }
    CatalogFunction(String title, String params) {
        this(title, params, null);
    }
    CatalogFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    CatalogFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.CATALOG; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}