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
* REGEX 类函数定义
* 包含所有数据库的 REGEX 相关函数
*/
public enum RegexFunction implements META {
		ICREGEXEQJOINSEL("选择率估算:icregexeqjoinsel", DatabaseType.PostgreSQL),
		ICREGEXEQSEL("选择率估算:icregexeqsel", DatabaseType.PostgreSQL),
		ICREGEXNEJOINSEL("选择率估算:icregexnejoinsel", DatabaseType.PostgreSQL),
		ICREGEXNESEL("选择率估算:icregexnesel", DatabaseType.PostgreSQL),
		NAMEICREGEXEQ("名称不区分大小写正则等于", DatabaseType.PostgreSQL),
		NAMEICREGEXNE("名称不区分大小写正则不等于", DatabaseType.PostgreSQL),
		NAMEREGEXEQ("名称正则等于", DatabaseType.PostgreSQL),
		NAMEREGEXNE("名称正则不等于", DatabaseType.PostgreSQL),
		NOT_REGEXP("正则匹配"),
		REGEXEQSEL("选择率估算:regexeqsel", DatabaseType.PostgreSQL),
		REGEXNESEL("选择率估算:regexnesel", DatabaseType.PostgreSQL),
		REGEXP("字符串是否与正则表达式匹配", "expression,pattern"),
		REGEXP_COUNT("正则计数", "expression,pattern"),
		REGEXP_INSTR("正则位置", "expression,pattern,position,occurrence,return_option,match_parameter,subexpr"),
		REGEXP_LIKE("正则匹配", "expression,pattern"),
		REGEXP_MATCH("正则提取子字符串", DatabaseType.PostgreSQL),
		REGEXP_MATCHES("正则全局匹配", DatabaseType.PostgreSQL),
		REGEXP_POSITION("子字符串匹配正则表达式起始索引"),
		REGEXP_REPLACE("正则替换", "expression,pattern,replacement,position,occurrence,match_parameter"),
		REGEXP_SPLIT_TO_ARRAY("正则分割字符串为数组", DatabaseType.PostgreSQL),
		REGEXP_SPLIT_TO_TABLE("正则分割字符串为行", DatabaseType.PostgreSQL),
		REGEXP_SUBSTR("正则取子串", "expression,pattern,position,occurrence,match_parameter,subexpr"),
		RLIKE("字符串是否与正则表达式匹配"),
		SIMILAR_ESCAPE("转义处理", DatabaseType.PostgreSQL),
		TEXTICREGEXEQ("文本不区分大小写正则等于", DatabaseType.PostgreSQL),
		TEXTICREGEXEQ_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		TEXTICREGEXNE("文本不区分大小写正则不等于", DatabaseType.PostgreSQL),
		TEXTREGEXEQ("文本正则等于", DatabaseType.PostgreSQL),
		TEXTREGEXEQ_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		TEXTREGEXNE("文本正则不等于", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    RegexFunction(String title) {
        this(title, null, null);
    }
    RegexFunction(String title, String params) {
        this(title, params, null);
    }
    RegexFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    RegexFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.REGEX; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}