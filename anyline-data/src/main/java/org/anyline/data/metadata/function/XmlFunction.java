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
* XML 类函数定义
* 包含所有数据库的 XML 相关函数
*/
public enum XmlFunction implements META {
		CURSOR_TO_XML("游标转XML", "cursor", DatabaseType.PostgreSQL),
		CURSOR_TO_XML_SCHEMA("游标转XML模式", "cursor", DatabaseType.PostgreSQL),
		CURSOR_TO_XMLSCHEMA("游标XMLSchema", DatabaseType.PostgreSQL),
		DATABASE_TO_XML("数据库转XML", "indent", DatabaseType.PostgreSQL),
		DATABASE_TO_XML_AND_XML_SCHEMA("数据库转XML及模式", "indent", DatabaseType.PostgreSQL),
		DATABASE_TO_XML_AND_XMLSCHEMA("数据库转XML和XSD", DatabaseType.PostgreSQL),
		DATABASE_TO_XML_SCHEMA("数据库转XML模式", DatabaseType.PostgreSQL),
		DATABASE_TO_XMLSCHEMA("数据库XMLSchema", DatabaseType.PostgreSQL),
		EXISTS_NODE("XML路径存在判断", "xmltype,xpath", DatabaseType.Oracle),
		EXISTSNODE("XML路径存在判断", DatabaseType.Oracle),
		EXTRACT_VALUE("使用XPath表示法提取值", "xml,xpath"),
		QUERY_TO_XML("查询转XML", DatabaseType.PostgreSQL),
		QUERY_TO_XML_AND_XML_SCHEMA("转XML及模式", DatabaseType.PostgreSQL),
		QUERY_TO_XML_AND_XMLSCHEMA("结果转XML和XSD", DatabaseType.PostgreSQL),
		QUERY_TO_XML_SCHEMA("查询转XML模式", DatabaseType.PostgreSQL),
		QUERY_TO_XMLSCHEMA("结果XMLSchema", DatabaseType.PostgreSQL),
		SCHEMA_TO_XML("模式转XML", DatabaseType.PostgreSQL),
		SCHEMA_TO_XML_AND_XML_SCHEMA("模式转XML及模式", DatabaseType.PostgreSQL),
		SCHEMA_TO_XML_AND_XMLSCHEMA("Schema转XML和XSD", DatabaseType.PostgreSQL),
		SCHEMA_TO_XML_SCHEMA("模式转XML模式", DatabaseType.PostgreSQL),
		SCHEMA_TO_XMLSCHEMA("模式转XML模式", DatabaseType.PostgreSQL),
		SYS_XML_AGG("聚合XML文档", "xml", DatabaseType.Oracle),
		SYS_XML_GEN("XML文档类型实例", "xml", DatabaseType.Oracle),
		TABLE_TO_XML("表转XML", DatabaseType.PostgreSQL),
		TABLE_TO_XML_AND_XML_SCHEMA("表转XML及模式", DatabaseType.PostgreSQL),
		TABLE_TO_XML_AND_XMLSCHEMA("表转XML和XSD", DatabaseType.PostgreSQL),
		TABLE_TO_XML_SCHEMA("表转XML模式", DatabaseType.PostgreSQL),
		TABLE_TO_XMLSCHEMA("表XMLSchema", DatabaseType.PostgreSQL),
		UPDATE_XML("替换XML片段", "xml,xpath,new_xml"),
		XML("XML类型转换", DatabaseType.PostgreSQL),
		XML_AGG("XML聚合", "xml"),
		XML_CAST("转标量SQL类型", "expression,type"),
		XML_CDATA("生成 CDATA", "expression"),
		XML_COL_ATT_VAL("一个XML片段", "expression"),
		XML_COMMENT("XML注释", "expression"),
		XML_CONCAT("XML值串接", "xml"),
		XML_DIFF("符合Xdiff模式XML中差异", "xml1,xml2"),
		XML_ELEMENT("XML元素", "name,expression"),
		XML_EXISTS("判断键是否存在", "xpath,xml"),
		XML_FOREST("XML林", "expression"),
		XML_IN("输入转换", DatabaseType.PostgreSQL),
		XML_IS_VALID("符合XML模式", "xml"),
		XML_IS_WELL_FORMED("XML格式良好"),
		XML_IS_WELL_FORMED_CONTENT("XML内容格式良好"),
		XML_IS_WELL_FORMED_DOCUMENT("XML文档格式良好"),
		XML_OUT("输出转换", DatabaseType.PostgreSQL),
		XML_PARSE("解析并从评估结果生成XML实例", "xml"),
		XML_PATCH("更改修补XML文档", "xml1,xml2"),
		XML_PI("XML处理指令", "name,expression"),
		XML_QUERY("XQuery表达式", "xpath,xml"),
		XML_RECV("二进制接收", DatabaseType.PostgreSQL),
		XML_ROOT("XML根节点", "xml,version"),
		XML_SEND("二进制发送", DatabaseType.PostgreSQL),
		XML_SEQUENCE("顶层节点数组", "xml", DatabaseType.Oracle),
		XML_SERIALIZE("包含value_expr内容字符串或LOB", "xml"),
		XML_TABLE("基于XML生成一个表", "xpath,xml"),
		XML_TO_TEXT("XML文本"),
		XML_TRANSFORM("XML转换", "xml,xslt"),
		XMLAGG("XML聚合", DatabaseType.PostgreSQL),
		XMLATTRIBUTES("XML属性"),
		XMLCOMMENT("XML注释", DatabaseType.PostgreSQL),
		XMLCONCAT2("XML连接", DatabaseType.PostgreSQL),
		XMLELEMENT("XML元素"),
		XMLEXISTS("XML存在性", DatabaseType.PostgreSQL),
		XMLFOREST("XML森林"),
		XMLNAMESPACES("XML命名空间"),
		XMLPARSE("解析XML"),
		XMLPI("XML处理指令"),
		XMLROOT("XML根元素"),
		XMLSERIALIZE("XML序列化"),
		XMLTABLE("XML表"),
		XMLTEXT("XML文本"),
		XMLVALIDATE("验证XML文档", DatabaseType.PostgreSQL),
		XMLXSROBJECTID("XMLXSR对象ID"),
		XPATH("根据XML值xml计算XPath"),
		XPATH_EXISTS("判断键是否存在")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    XmlFunction(String title) {
        this(title, null, null);
    }
    XmlFunction(String title, String params) {
        this(title, params, null);
    }
    XmlFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    XmlFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.XML; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}