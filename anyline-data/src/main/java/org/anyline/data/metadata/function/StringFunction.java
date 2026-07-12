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
* STRING 类函数定义
* 包含所有数据库的 STRING 相关函数
*/
public enum StringFunction implements META {
		ANYTEXTCAT("文本拼接", "expression1"),
		ASCII("ASCII码值", "expression"),
		ASCIISTR("转ASCII字符串", "expression"),
		BIN("转二进制字符串", "expression"),
		BIN_TO_UUID("二进制转UUID", "binary"),
		BINTOCHAR("二进制转字符串", "binary"),
		BPCHAR("定长字符类型", DatabaseType.PostgreSQL),
		BPCHAR_LARGER("取较大值", DatabaseType.PostgreSQL),
		BPCHAR_PATTERN_GE("大于等于判断", DatabaseType.PostgreSQL),
		BPCHAR_PATTERN_GT("大于判断", DatabaseType.PostgreSQL),
		BPCHAR_PATTERN_LE("小于等于判断", DatabaseType.PostgreSQL),
		BPCHAR_PATTERN_LT("小于判断", DatabaseType.PostgreSQL),
		BPCHAR_SMALLER("取较小值", DatabaseType.PostgreSQL),
		BPCHAR_SORTSUPPORT("排序支持", DatabaseType.PostgreSQL),
		BPCHARCMP("定长字符比较", DatabaseType.PostgreSQL),
		BPCHAREQ("定长字符相等", DatabaseType.PostgreSQL),
		BPCHARGE("定长字符大于等于", DatabaseType.PostgreSQL),
		BPCHARGT("定长字符大于", DatabaseType.PostgreSQL),
		BPCHARICLIKE("定长字符ILIKE", DatabaseType.PostgreSQL),
		BPCHARICNLIKE("定长字符NOTILIKE", DatabaseType.PostgreSQL),
		BPCHARICREGEXEQ("定长字符icase正则匹配", DatabaseType.PostgreSQL),
		BPCHARICREGEXNE("定长字符icase正则不匹配", DatabaseType.PostgreSQL),
		BPCHARIN("定长字符输入", DatabaseType.PostgreSQL),
		BPCHARLE("定长字符小于等于", DatabaseType.PostgreSQL),
		BPCHARLIKE("定长字符LIKE", DatabaseType.PostgreSQL),
		BPCHARLT("定长字符小于", DatabaseType.PostgreSQL),
		BPCHARNE("定长字符不等", DatabaseType.PostgreSQL),
		BPCHARNLIKE("定长字符NOTLIKE", DatabaseType.PostgreSQL),
		BPCHAROUT("定长字符输出", DatabaseType.PostgreSQL),
		BPCHARRECV("定长字符接收", DatabaseType.PostgreSQL),
		BPCHARREGEXEQ("定长字符正则匹配", DatabaseType.PostgreSQL),
		BPCHARREGEXNE("定长字符正则不匹配", DatabaseType.PostgreSQL),
		BPCHARSEND("定长字符发送", DatabaseType.PostgreSQL),
		BPCHARTYPMODIN("定长字符类型修饰输入", DatabaseType.PostgreSQL),
		BPCHARTYPMODOUT("定长字符类型修饰输出", DatabaseType.PostgreSQL),
		BTRIM("去除字符串两端指定字符", "expression"),
		BYTEA_STRING_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		BYTEA_STRING_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		BYTEACAT("bytea操作:cat", DatabaseType.PostgreSQL),
		BYTEACMP("bytea操作:cmp", DatabaseType.PostgreSQL),
		BYTEAEQ("bytea操作:eq", DatabaseType.PostgreSQL),
		BYTEAGT("bytea操作:gt", DatabaseType.PostgreSQL),
		BYTEAIN("bytea操作:in", DatabaseType.PostgreSQL),
		BYTEALE("bytea操作:le", DatabaseType.PostgreSQL),
		BYTEALIKE("bytea操作:like", DatabaseType.PostgreSQL),
		BYTEALT("bytea操作:lt", DatabaseType.PostgreSQL),
		BYTEANE("bytea操作:ne", DatabaseType.PostgreSQL),
		BYTEANLIKE("bytea操作:nlike", DatabaseType.PostgreSQL),
		BYTEAOUT("bytea操作:out", DatabaseType.PostgreSQL),
		BYTEARECV("bytea操作:recv", DatabaseType.PostgreSQL),
		BYTEASEND("bytea操作:send", DatabaseType.PostgreSQL),
		CASE_FOLD("排序规则折叠", "expression", DatabaseType.PostgreSQL),
		CHAR("整数转字符", "expression"),
		CHAR_INDEX("查找字符串位置", "expression"),
		CHAR_LENGTH("字符长度", "expression"),
		CHARACTER_LENGTH("字符长度", "expression"),
		CHAREQ("chareq操作符", DatabaseType.PostgreSQL),
		CHARGE("charge操作符", DatabaseType.PostgreSQL),
		CHARGT("字符大于", DatabaseType.PostgreSQL),
		CHARIN("字符输入", DatabaseType.PostgreSQL),
		CHARINDEX("字符位置", "expression"),
		CHARLE("charle操作符", DatabaseType.PostgreSQL),
		CHARLT("charlt操作符", DatabaseType.PostgreSQL),
		CHARNE("charne操作符", DatabaseType.PostgreSQL),
		CHAROUT("字符输出", DatabaseType.PostgreSQL),
		CHARRECV("字符接收", DatabaseType.PostgreSQL),
		CHARSEND("字符发送", DatabaseType.PostgreSQL),
		CHARSET("字符集", "expression", DatabaseType.MySQL),
		CHARTOBIN("字符转二进制", "expression"),
		CHARTOROWID("字符转ROWID", "expression"),
		CHR("int转字符", "expression"),
		COERCIBILITY("字符串参数排序规则强制性值", "expression"),
		COLLATION("字符串参数排序规则", "expression"),
		COMPOSE("组合字符串", "expression"),
		CONCAT("字符串连接", "expression"),
		CONCAT_WS("带分隔符连接", "separator,expression"),
		CSTRING_IN("输入转换", DatabaseType.PostgreSQL),
		CSTRING_OUT("输出转换", DatabaseType.PostgreSQL),
		CSTRING_RECV("二进制接收", DatabaseType.PostgreSQL),
		CSTRING_SEND("二进制发送", DatabaseType.PostgreSQL),
		DECOMPOSE("分解字符串", "expression"),
		DIFFERENCE("求两个字符串读音相似性", "expression1"),
		ELT("按索引取字符串", "index,expression"),
		EXPORT_SET("导出位集合", "bits,on,off", DatabaseType.MySQL),
		FIELD("返回索引位置", "expression,expression"),
		FIND_IN_SET("查找集合位置", "expression,expression_list", DatabaseType.MySQL),
		FORMAT("格式化数字", "expression,format"),
		GROUP_CONCAT("分组连接", "expression"),
		HEAD_ASCII("第一个字符ASCII值", "expression"),
		INITCAP("单词首字母大写", "expression"),
		INITCAP_FUNC("INITCAP(GBase)", "expression"),
		INPUT_ERROR_INFO("验证数据类型", "expression"),
		INPUT_IS_VALID("验证数据类型", "expression"),
		INS("插入", "expression"),
		INSERT("插入子串", "expression,position,length,substring"),
		INSERT_TEXT("替换子串", "expression", DatabaseType.MySQL),
		INSERT_TEXT_C("替换指定子串", "expression"),
		INSTR("子串位置", "expression,substring"),
		INSTRB("查找子串位置字节", "expression"),
		INSTRC("查找子串位置字符", "expression"),
		IS_NORMALIZED("Unicode规范化检查", "expression"),
		LCASE("转小写", "expression"),
		LEFT("左截取", "expression,length"),
		LEN("计算长度", "expression"),
		LENGTH("字节长度", "expression"),
		LENGTH2("长度UTF16", "expression"),
		LENGTH4("长度UTF32", "expression"),
		LENGTHB("长度字节", "expression"),
		LENGTHC("长度字符", "expression"),
		LIKE("模式匹配", "expression,pattern"),
		LIKE_ESCAPE("LIKE转义字符", DatabaseType.PostgreSQL),
		LIKEJOINSEL("LIKE连接率", DatabaseType.PostgreSQL),
		LIKESEL("LIKE选择率", DatabaseType.PostgreSQL),
		LIST_AGG("list聚合", "expression,delimiter"),
		LISTAGG("列表聚合", "expression"),
		LOCATE("子串位置", "expression,substring"),
		LOWER("转小写", "expression"),
		LPAD("左填充", "expression,length,pad"),
		LPAD_FUNC("LPAD(GBase)", "expression"),
		LTRIM("去左空格", "expression,chars"),
		MAKE_SET("构造位集合", "bits,set", DatabaseType.MySQL),
		MID("取子串", "expression,pos,len", DatabaseType.MySQL),
		NAME_CONST("命名常量", "name,expression", DatabaseType.MySQL),
		NAMECONCATOID("名称拼接OID", DatabaseType.PostgreSQL),
		NAMEEQTEXT("名称等于文本", DatabaseType.PostgreSQL),
		NAMEGETEXT("名称大于等于文本", DatabaseType.PostgreSQL),
		NAMEGTTEXT("名称大于文本", DatabaseType.PostgreSQL),
		NAMEICLIKE("名称不区分大小写LIKE", DatabaseType.PostgreSQL),
		NAMEICNLIKE("名称不区分大小写NOTLIKE", DatabaseType.PostgreSQL),
		NAMELETEXT("名称小于等于文本", DatabaseType.PostgreSQL),
		NAMELIKE("名称LIKE", DatabaseType.PostgreSQL),
		NAMELTTEXT("名称小于文本", DatabaseType.PostgreSQL),
		NAMENETEXT("名称不等于文本", DatabaseType.PostgreSQL),
		NAMENLIKE("名称NOTLIKE", DatabaseType.PostgreSQL),
		NLS_INITCAP("首字母大写", "expression", DatabaseType.Oracle),
		NLS_LOWER("转小写", "expression", DatabaseType.Oracle),
		NLS_UPPER("转大写", "expression", DatabaseType.Oracle),
		NOT_LIKE("LIKE比较", "expression"),
		NOTLIKE("NOTLIKE字符串比较", "expression"),
		NUMERICTYPMODIN("numeric操作符", DatabaseType.PostgreSQL),
		NUMERICTYPMODOUT("numeric操作符", DatabaseType.PostgreSQL),
		OCTET_LENGTH("小于等于判断", "expression"),
		OVERLAY("替换指定子串", "expression"),
		PARSE_IDENT("qualified_identifier拆分为界定符数组", DatabaseType.PostgreSQL),
		PARSENAME("解析对象名", "name"),
		PAT_INDEX("一个字串在指定表达式中起始位置", "pattern"),
		POSITION("子串位置", "substring,expression"),
		POSITIONJOINSEL("位置连接选择率", DatabaseType.PostgreSQL),
		POSITIONSEL("位置选择率", DatabaseType.PostgreSQL),
		PREFIXJOINSEL("前缀连接选择率", DatabaseType.PostgreSQL),
		PREFIXSEL("前缀选择率", DatabaseType.PostgreSQL),
		QUOTE("引号转义", "expression", DatabaseType.PostgreSQL),
		QUOTE_IDENT("输出字符串表达式", DatabaseType.PostgreSQL),
		QUOTE_LITERAL("输出字符串表达式", DatabaseType.PostgreSQL),
		QUOTE_NULLABLE("输出字符串表达式", DatabaseType.PostgreSQL),
		QUOTENAME("引用名称", "name"),
		REPEAT("重复字符串", "expression,count"),
		REPLACE("替换子串", "expression,search,replace"),
		REVERSE("反转字符串", "expression"),
		RIGHT("右截取", "expression,length"),
		ROWIDTOCHAR("ROWID转字符", "rowid"),
		RPAD("右填充", "expression,length,pad"),
		RPAD_FUNC("RPAD(GBase)", "expression"),
		RTRIM("去右空格", "expression,chars"),
		SOUNDEX("语音编码", "expression"),
		SOUNDS_LIKE("LIKE比较", DatabaseType.MySQL),
		SPACE("空格字符串", "count"),
		SPLIT_PART("以给定字符串为分隔符", "expression,delimiter,index", DatabaseType.PostgreSQL),
		STARTS_WITH("字符串以前缀开头", "expression"),
		STR("数值转字符串", "expression"),
		STRCMP("字符串比较", "expression1,expression2"),
		STRING_AGG("字符串聚合", "expr"),
		STRING_AGG_FINALFN("聚合终结", DatabaseType.PostgreSQL),
		STRING_AGG_TRANSFN("聚合过渡", DatabaseType.PostgreSQL),
		STRING_ESCAPE("字符串转义", "expression"),
		STRING_FORMAT("根据格式字符串格式化输出", "format"),
		STRING_POSITION("文本位置查找", "expression,substring"),
		STRING_SPLIT("字符串split", "expression"),
		STRING_TO_ARRAY("按分隔符拆分字符串为数组", "expression"),
		STRING_TO_TABLE("分隔符拆分为行", "expression", DatabaseType.PostgreSQL),
		STRIP("去除字符串两端空白", "expression"),
		STRPOS("查找子串位置", "expression"),
		STUFF("插入删除子串", "expression"),
		SUBSTR("取子串", "expression,start,length"),
		SUBSTR_BIT("字节截取子串", "expression"),
		SUBSTRB("子串字节", "expression"),
		SUBSTRBB("子串字节", "expression"),
		SUBSTRING("取子串", "expression,start,length"),
		SUBSTRING_INDEX("按分隔符取子串", "expression,delimiter,count"),
		TEXT_EQUAL("相等判断", "expression1"),
		TEXT_GE("大于等于判断", DatabaseType.PostgreSQL),
		TEXT_GT("大于判断", DatabaseType.PostgreSQL),
		TEXT_LARGER("取字符串较大值", "expression1"),
		TEXT_LE("小于等于判断", DatabaseType.PostgreSQL),
		TEXT_LT("小于判断", DatabaseType.PostgreSQL),
		TEXT_PATTERN_GE("大于等于判断", DatabaseType.PostgreSQL),
		TEXT_PATTERN_GT("大于判断", DatabaseType.PostgreSQL),
		TEXT_PATTERN_LE("小于等于判断", DatabaseType.PostgreSQL),
		TEXT_PATTERN_LT("小于判断", DatabaseType.PostgreSQL),
		TEXT_SMALLER("取字符串较小值", "expression1"),
		TEXTANYCAT("文本任意连接", DatabaseType.PostgreSQL),
		TEXTCAT("文本连接", DatabaseType.PostgreSQL),
		TEXTEQ("texteq操作符", DatabaseType.PostgreSQL),
		TEXTEQNAME("texteq操作符", DatabaseType.PostgreSQL),
		TEXTGENAME("textge操作符", DatabaseType.PostgreSQL),
		TEXTGTNAME("textgt操作符", DatabaseType.PostgreSQL),
		TEXTICLIKE("文本不区分大小写LIKE", DatabaseType.PostgreSQL),
		TEXTICLIKE_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		TEXTICNLIKE("文本不区分大小写NOTLIKE", DatabaseType.PostgreSQL),
		TEXTIN("字符串输入", DatabaseType.PostgreSQL),
		TEXTLEN("textle操作符", DatabaseType.PostgreSQL),
		TEXTLENAME("textle操作符", DatabaseType.PostgreSQL),
		TEXTLIKE("文本LIKE", DatabaseType.PostgreSQL),
		TEXTLIKE_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		TEXTLTNAME("textlt操作符", DatabaseType.PostgreSQL),
		TEXTNE("textne操作符", DatabaseType.PostgreSQL),
		TEXTNENAME("textne操作符", DatabaseType.PostgreSQL),
		TEXTNLIKE("文本NOTLIKE", DatabaseType.PostgreSQL),
		TEXTOUT("字符串输出", DatabaseType.PostgreSQL),
		TEXTRECV("字符串接收", DatabaseType.PostgreSQL),
		TEXTSEND("字符串发送", DatabaseType.PostgreSQL),
		TO_ASCII("文本从其他编码转换为ASCII", "expression"),
		TRANSLATE("字符翻译", "expression,from,to"),
		TRIM("去空格", "expression"),
		UCASE("转大写", "expression"),
		UNI_STR("国家字符集", "expression", DatabaseType.Oracle),
		UNICODE_STRING("参数中转义Unicode字符", "expression"),
		UNISTR("Unicode字符串", DatabaseType.PostgreSQL),
		UPPER("转大写", "expression"),
		UPPER_LIMIT("取字符串与其最近上限", "expression"),
		VARCHAR_FORMAT("varchar格式化", "expression"),
		VARCHAR_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		VARCHARIN("变长字符输入", DatabaseType.PostgreSQL),
		VARCHAROUT("变长字符输出", DatabaseType.PostgreSQL),
		VARCHARRECV("变长字符接收", DatabaseType.PostgreSQL),
		VARCHARSEND("变长字符发送", DatabaseType.PostgreSQL),
		VARCHARTYPMODIN("变长类型修饰输入", DatabaseType.PostgreSQL),
		VARCHARTYPMODOUT("变长类型修饰输出", DatabaseType.PostgreSQL),
		WEIGHT_STRING("字符串权重字符串", "expression", DatabaseType.MySQL),
		WM_CONCAT("连接", "expression")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    StringFunction(String title) {
        this(title, null, null);
    }
    StringFunction(String title, String params) {
        this(title, params, null);
    }
    StringFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    StringFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public META.Category category() { return META.Category.STRING; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}