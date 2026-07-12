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
* FULLTEXT 类函数定义
* 包含所有数据库的 FULLTEXT 相关函数
*/
public enum FulltextFunction implements META {
		DISPELL_INIT("输入转换", DatabaseType.PostgreSQL),
		DISPELL_LEXIZE("词形还原", DatabaseType.PostgreSQL),
		DSIMPLE_INIT("输入转换", DatabaseType.PostgreSQL),
		DSIMPLE_LEXIZE("词形还原", DatabaseType.PostgreSQL),
		DSYNONYM_INIT("输入转换", DatabaseType.PostgreSQL),
		DSYNONYM_LEXIZE("词形还原", DatabaseType.PostgreSQL),
		FULLTEXTCATALOGPROPERTY("全文目录属性"),
		FULLTEXTSERVICEPROPERTY("全文服务属性"),
		GET_CURRENT_TS_CONFIG("当前默认文本搜索配置OID", DatabaseType.PostgreSQL),
		GTSQUERY_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		GTSQUERY_PENALTY("索引代价惩罚", DatabaseType.PostgreSQL),
		GTSQUERY_PICKSPLIT("索引分裂策略", DatabaseType.PostgreSQL),
		GTSQUERY_SAME("索引相等性检查", DatabaseType.PostgreSQL),
		GTSVECTOR_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		GTSVECTOR_OPTIONS("选项处理", DatabaseType.PostgreSQL),
		GTSVECTOR_PENALTY("索引代价惩罚", DatabaseType.PostgreSQL),
		GTSVECTOR_PICKSPLIT("索引分裂策略", DatabaseType.PostgreSQL),
		GTSVECTOR_SAME("索引相等性检查", DatabaseType.PostgreSQL),
		GTSVECTORIN("GTS向量输入", DatabaseType.PostgreSQL),
		GTSVECTOROUT("GTS向量输出", DatabaseType.PostgreSQL),
		MATCH("全文搜索", "expressions,search", DatabaseType.PostgreSQL),
		NUM_NODE("词位加上运算符数量", DatabaseType.PostgreSQL),
		PHRASE_TO_TSQUERY("文本规范化", DatabaseType.PostgreSQL),
		PHRASETO_TSQUERY("短语转tsquery", DatabaseType.PostgreSQL),
		PLAIN_TO_TSQUERY("文本规范化", DatabaseType.PostgreSQL),
		PLAINTO_TSQUERY("纯文本转tsquery", DatabaseType.PostgreSQL),
		PRSD_END("解析器结束", DatabaseType.PostgreSQL),
		PRSD_HEADLINE("解析器标题", DatabaseType.PostgreSQL),
		PRSD_LEXTYPE("解析器词位类型", DatabaseType.PostgreSQL),
		PRSD_NEXTTOKEN("解析器下一词元", DatabaseType.PostgreSQL),
		PRSD_START("解析器开始", DatabaseType.PostgreSQL),
		QUERY_TREE("文本规范化", DatabaseType.PostgreSQL),
		QUERYTREE("返回查询树", DatabaseType.PostgreSQL),
		SET_WEIGHT("为向量每个元素分配指定权重", DatabaseType.PostgreSQL),
		SETWEIGHT("tsvector权重", "tsvector", DatabaseType.PostgreSQL),
		STATS_CROSSTAB("交叉表统计"),
		THESAURUS_INIT("输入转换", DatabaseType.PostgreSQL),
		THESAURUS_LEXIZE("词形还原", DatabaseType.PostgreSQL),
		TO_TS_QUERY("根据指定或默认配置规范化单词", DatabaseType.PostgreSQL),
		TO_TS_VECTOR("JSON文档中每个字符串值转换为", DatabaseType.PostgreSQL),
		TO_TSQUERY("转TS查询", DatabaseType.PostgreSQL),
		TO_TSVECTOR("转TS向量", DatabaseType.PostgreSQL),
		TS_CONFIG_IS_VISIBLE("搜索配置可见性", DatabaseType.PostgreSQL),
		TS_DEBUG("提取文本标记", "config", DatabaseType.PostgreSQL),
		TS_DELETE("从向量中删除给定词位任何出现", DatabaseType.PostgreSQL),
		TS_DICT_IS_VISIBLE("搜索词典可见性", DatabaseType.PostgreSQL),
		TS_FILTER("仅从向量中选择具有给定权重元素", DatabaseType.PostgreSQL),
		TS_HEADLINE("查询匹配缩写", "config", DatabaseType.PostgreSQL),
		TS_LEXIZE("如果输入词典已知", "dictionary", DatabaseType.PostgreSQL),
		TS_MATCH_QV("查询向量匹配", DatabaseType.PostgreSQL),
		TS_MATCH_TQ("文本查询匹配", DatabaseType.PostgreSQL),
		TS_MATCH_TT("文本匹配", DatabaseType.PostgreSQL),
		TS_MATCH_VQ("向量查询匹配", DatabaseType.PostgreSQL),
		TS_PARSE("使用命名解析器提取词元", "parser", DatabaseType.PostgreSQL),
		TS_PARSER_IS_VISIBLE("解析器可见性", DatabaseType.PostgreSQL),
		TS_QUERY_PHRASE("短语查询", DatabaseType.PostgreSQL),
		TS_RANK("查询匹配分数", "tsvector", DatabaseType.PostgreSQL),
		TS_RANK_CD("使用覆盖密度算法计算一个分数", "tsvector", DatabaseType.PostgreSQL),
		TS_REWRITE("查询替换", "query", DatabaseType.PostgreSQL),
		TS_STAT("数据中每个不同词汇统计数据", "query", DatabaseType.PostgreSQL),
		TS_TEMPLATE_IS_VISIBLE("搜索模板可见性", DatabaseType.PostgreSQL),
		TS_TOKEN_TYPE("解析器令牌类型", DatabaseType.PostgreSQL),
		TS_TYPANALYZE("类型分析", DatabaseType.PostgreSQL),
		TS_VECTOR_TO_ARRAY("转换为词位数组", DatabaseType.PostgreSQL),
		TS_VECTOR_UPDATE_TRIGGER("自动更新关联纯文本文档列中列", DatabaseType.PostgreSQL),
		TS_VECTOR_UPDATE_TRIGGER_COLUMN("自动更新关联纯文本文档列中列", DatabaseType.PostgreSQL),
		TSMATCHJOINSEL("选择率估算:tsmatchjoinsel", DatabaseType.PostgreSQL),
		TSMATCHSEL("选择率估算:tsmatchsel", DatabaseType.PostgreSQL),
		TSQ_MCONTAINED("多查询是否被包含", DatabaseType.PostgreSQL),
		TSQUERY_AND("按位与", DatabaseType.PostgreSQL),
		TSQUERY_CMP("比较", DatabaseType.PostgreSQL),
		TSQUERY_EQ("相等判断", DatabaseType.PostgreSQL),
		TSQUERY_GE("大于等于判断", DatabaseType.PostgreSQL),
		TSQUERY_GT("大于判断", DatabaseType.PostgreSQL),
		TSQUERY_LE("小于等于判断", DatabaseType.PostgreSQL),
		TSQUERY_LT("小于判断", DatabaseType.PostgreSQL),
		TSQUERY_NE("不等判断", DatabaseType.PostgreSQL),
		TSQUERY_NOT("按位非", DatabaseType.PostgreSQL),
		TSQUERY_OR("按位或", DatabaseType.PostgreSQL),
		TSQUERY_PHRASE("短语匹配", DatabaseType.PostgreSQL),
		TSQUERYIN("输入", DatabaseType.PostgreSQL),
		TSQUERYOUT("输出", DatabaseType.PostgreSQL),
		TSQUERYRECV("接收", DatabaseType.PostgreSQL),
		TSQUERYSEND("发送", DatabaseType.PostgreSQL),
		TSVECTOR_CMP("比较", DatabaseType.PostgreSQL),
		TSVECTOR_CONCAT("连接", DatabaseType.PostgreSQL),
		TSVECTOR_EQ("相等判断", DatabaseType.PostgreSQL),
		TSVECTOR_GE("大于等于判断", DatabaseType.PostgreSQL),
		TSVECTOR_GT("大于判断", DatabaseType.PostgreSQL),
		TSVECTOR_LE("小于等于判断", DatabaseType.PostgreSQL),
		TSVECTOR_LT("小于判断", DatabaseType.PostgreSQL),
		TSVECTOR_NE("不等判断", DatabaseType.PostgreSQL),
		TSVECTOR_UPDATE_TRIGGER("更新触发器", DatabaseType.PostgreSQL),
		TSVECTOR_UPDATE_TRIGGER_COLUMN("更新触发器", DatabaseType.PostgreSQL),
		TSVECTORIN("输入", DatabaseType.PostgreSQL),
		TSVECTOROUT("输出", DatabaseType.PostgreSQL),
		TSVECTORRECV("接收", DatabaseType.PostgreSQL),
		TSVECTORSEND("发送", DatabaseType.PostgreSQL),
		WEBSEARCH_TO_TSQUERY("文本规范化", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    FulltextFunction(String title) {
        this(title, null, null);
    }
    FulltextFunction(String title, String params) {
        this(title, params, null);
    }
    FulltextFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    FulltextFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.FULLTEXT; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}