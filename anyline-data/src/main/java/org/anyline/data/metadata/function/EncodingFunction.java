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
* ENCODING 类函数定义
* 包含所有数据库的 ENCODING 相关函数
*/
public enum EncodingFunction implements META {
		BIG5_TO_EUC_TW("BIG5转EUC_TW", DatabaseType.PostgreSQL),
		BIG5_TO_MIC("BIG5转MIC", DatabaseType.PostgreSQL),
		BIG5_TO_UTF8("BIG5转UTF8", DatabaseType.PostgreSQL),
		COLLATION_ACTUAL_VERSION("排序规则对象版本", "collation_oid", DatabaseType.PostgreSQL),
		COLLATION_IS_VISIBLE("排序规则可见性", "collation_oid", DatabaseType.PostgreSQL),
		CONVERSION_IS_VISIBLE("转化在搜索路径中可见", "conversion_oid", DatabaseType.PostgreSQL),
		DATABASE_COLLATION_ACTUAL_VERSION("排序规则版本", "db_oid", DatabaseType.PostgreSQL),
		EUC_CN_TO_MIC("EUC_CN转MIC", DatabaseType.PostgreSQL),
		EUC_CN_TO_UTF8("EUC_CN转UTF8", DatabaseType.PostgreSQL),
		EUC_JIS_2004_TO_SHIFT_JIS_2004("EUCJIS2004转ShiftJIS2004", DatabaseType.PostgreSQL),
		EUC_JIS_2004_TO_UTF8("EUCJIS2004转UTF8", DatabaseType.PostgreSQL),
		EUC_JP_TO_MIC("EUC_JP转MIC", DatabaseType.PostgreSQL),
		EUC_JP_TO_SJIS("EUC_JP转SJIS", DatabaseType.PostgreSQL),
		EUC_JP_TO_UTF8("EUC_JP转UTF8", DatabaseType.PostgreSQL),
		EUC_KR_TO_MIC("EUC_KR转MIC", DatabaseType.PostgreSQL),
		EUC_KR_TO_UTF8("EUC_KR转UTF8", DatabaseType.PostgreSQL),
		EUC_TW_TO_BIG5("EUC_TW转BIG5", DatabaseType.PostgreSQL),
		EUC_TW_TO_MIC("EUC_TW转MIC", DatabaseType.PostgreSQL),
		EUC_TW_TO_UTF8("EUC_TW转UTF8", DatabaseType.PostgreSQL),
		GB18030_TO_UTF8("GB18030转UTF8", DatabaseType.PostgreSQL),
		GBK_TO_UTF8("GBK转UTF8", DatabaseType.PostgreSQL),
		GETDATABASEENCODING("获取数据库编码", DatabaseType.PostgreSQL),
		ICLIKEJOINSEL("选择率估算:iclikejoinsel", DatabaseType.PostgreSQL),
		ICLIKESEL("选择率估算:iclikesel", DatabaseType.PostgreSQL),
		ICNLIKEJOINSEL("选择率估算:icnlikejoinsel", DatabaseType.PostgreSQL),
		ICNLIKESEL("选择率估算:icnlikesel", DatabaseType.PostgreSQL),
		ICU_UNICODE_VERSION("ICU使用UNICODE版本", DatabaseType.PostgreSQL),
		ICU_VERSION("ICU库版", DatabaseType.PostgreSQL),
		IMPORT_SYSTEM_COLLATIONS("添加系统排序规则", DatabaseType.PostgreSQL),
		ISO_TO_KOI8R("ISO转KOI8R", DatabaseType.PostgreSQL),
		ISO_TO_MIC("ISO转MIC", DatabaseType.PostgreSQL),
		ISO_TO_WIN1251("ISO转WIN1251", DatabaseType.PostgreSQL),
		ISO_TO_WIN866("ISO转WIN866", DatabaseType.PostgreSQL),
		ISO8859_1_TO_UTF8("ISO8859-1转UTF8", DatabaseType.PostgreSQL),
		ISO8859_TO_UTF8("ISO8859转UTF8", DatabaseType.PostgreSQL),
		JOHAB_TO_UTF8("JOHAB转UTF8", DatabaseType.PostgreSQL),
		JSON_TO_TSVECTOR("聚合过渡:json_to_tsvector", DatabaseType.PostgreSQL),
		JSONB_TO_TSVECTOR("聚合过渡:jsonb_to_tsvector", DatabaseType.PostgreSQL),
		KOI8R_TO_ISO("KOI8R转ISO", DatabaseType.PostgreSQL),
		KOI8R_TO_MIC("KOI8R转MIC", DatabaseType.PostgreSQL),
		KOI8R_TO_UTF8("KOI8R转UTF8", DatabaseType.PostgreSQL),
		KOI8R_TO_WIN1251("KOI8R转WIN1251", DatabaseType.PostgreSQL),
		KOI8R_TO_WIN866("KOI8R转WIN866", DatabaseType.PostgreSQL),
		KOI8U_TO_UTF8("KOI8U转UTF8", DatabaseType.PostgreSQL),
		LATIN1_TO_MIC("Latin1转MIC", DatabaseType.PostgreSQL),
		LATIN2_TO_MIC("Latin2转MIC", DatabaseType.PostgreSQL),
		LATIN2_TO_WIN1250("Latin2转Win1250", DatabaseType.PostgreSQL),
		LATIN3_TO_MIC("Latin3转MIC", DatabaseType.PostgreSQL),
		LATIN4_TO_MIC("Latin4转MIC", DatabaseType.PostgreSQL),
		MIC_TO_BIG5("MIC转BIG5", DatabaseType.PostgreSQL),
		MIC_TO_EUC_CN("MIC转EUC_CN", DatabaseType.PostgreSQL),
		MIC_TO_EUC_JP("MIC转EUC_JP", DatabaseType.PostgreSQL),
		MIC_TO_EUC_KR("MIC转EUC_KR", DatabaseType.PostgreSQL),
		MIC_TO_EUC_TW("MIC转EUC_TW", DatabaseType.PostgreSQL),
		MIC_TO_ISO("MIC转ISO", DatabaseType.PostgreSQL),
		MIC_TO_KOI8R("MIC转KOI8R", DatabaseType.PostgreSQL),
		MIC_TO_LATIN1("MIC转Latin1", DatabaseType.PostgreSQL),
		MIC_TO_LATIN2("MIC转Latin2", DatabaseType.PostgreSQL),
		MIC_TO_LATIN3("MIC转Latin3", DatabaseType.PostgreSQL),
		MIC_TO_LATIN4("MIC转Latin4", DatabaseType.PostgreSQL),
		MIC_TO_SJIS("MIC转SJIS", DatabaseType.PostgreSQL),
		MIC_TO_WIN1250("MIC转Win1250", DatabaseType.PostgreSQL),
		MIC_TO_WIN1251("MIC转Win1251", DatabaseType.PostgreSQL),
		MIC_TO_WIN866("MIC转Win866", DatabaseType.PostgreSQL),
		NLS_CHARSET_DECL_LEN("字符集声明长度", "charset_id,byte_length", DatabaseType.Oracle),
		NLS_CHARSET_ID("字符集ID", "charset_name", DatabaseType.Oracle),
		NLS_CHARSET_NAME("字符集名", "charset_id", DatabaseType.Oracle),
		NLS_COLLATION_ID("排序规则ID", "collation_name", DatabaseType.Oracle),
		NLS_COLLATION_NAME("排序规则名", "collation_id", DatabaseType.Oracle),
		NLS_SORT("NLS排序", "expression", DatabaseType.Oracle),
		NLSSORT("NLS排序", "expression"),
		NORMALIZE("Unicode规范化"),
		SHIFT_JIS_2004_TO_EUC_JIS_2004("ShiftJIS2004转EUCJIS2004", DatabaseType.PostgreSQL),
		SHIFT_JIS_2004_TO_UTF8("ShiftJIS2004转UTF8", DatabaseType.PostgreSQL),
		SIMILAR_TO_ESCAPE("转义处理", DatabaseType.PostgreSQL),
		SJIS_TO_EUC_JP("SJIS转EUC_JP", DatabaseType.PostgreSQL),
		SJIS_TO_MIC("SJIS转MIC", DatabaseType.PostgreSQL),
		SJIS_TO_UTF8("SJIS转UTF8", DatabaseType.PostgreSQL),
		TSVECTOR_TO_ARRAY("转为数组", DatabaseType.PostgreSQL),
		UHC_TO_UTF8("UHC转UTF8", DatabaseType.PostgreSQL),
		UNICODE_ASSIGNED("Unicode代码点"),
		UNICODE_VERSION("使用UNICODE版本", DatabaseType.PostgreSQL),
		UTF8_TO_BIG5("UTF8转BIG5", DatabaseType.PostgreSQL),
		UTF8_TO_EUC_CN("UTF8转EUC_CN", DatabaseType.PostgreSQL),
		UTF8_TO_EUC_JIS_2004("UTF8转EUCJIS2004", DatabaseType.PostgreSQL),
		UTF8_TO_EUC_JP("UTF8转EUC_JP", DatabaseType.PostgreSQL),
		UTF8_TO_EUC_KR("UTF8转EUC_KR", DatabaseType.PostgreSQL),
		UTF8_TO_EUC_TW("UTF8转EUC_TW", DatabaseType.PostgreSQL),
		UTF8_TO_GB18030("UTF8转GB18030", DatabaseType.PostgreSQL),
		UTF8_TO_GBK("UTF8转GBK", DatabaseType.PostgreSQL),
		UTF8_TO_ISO8859("UTF8转ISO8859", DatabaseType.PostgreSQL),
		UTF8_TO_ISO8859_1("UTF8转ISO8859-1", DatabaseType.PostgreSQL),
		UTF8_TO_JOHAB("UTF8转JOHAB", DatabaseType.PostgreSQL),
		UTF8_TO_KOI8R("UTF8转KOI8R", DatabaseType.PostgreSQL),
		UTF8_TO_KOI8U("UTF8转KOI8U", DatabaseType.PostgreSQL),
		UTF8_TO_SHIFT_JIS_2004("UTF8转ShiftJIS2004", DatabaseType.PostgreSQL),
		UTF8_TO_SJIS("UTF8转SJIS", DatabaseType.PostgreSQL),
		UTF8_TO_UHC("UTF8转UHC", DatabaseType.PostgreSQL),
		UTF8_TO_WIN("UTF8转WIN", DatabaseType.PostgreSQL),
		WIN_TO_UTF8("WIN转UTF8", DatabaseType.PostgreSQL),
		WIN1250_TO_LATIN2("Win1250转Latin2", DatabaseType.PostgreSQL),
		WIN1250_TO_MIC("Win1250转MIC", DatabaseType.PostgreSQL),
		WIN1251_TO_ISO("Win1251转ISO", DatabaseType.PostgreSQL),
		WIN1251_TO_KOI8R("Win1251转KOI8R", DatabaseType.PostgreSQL),
		WIN1251_TO_MIC("Win1251转MIC", DatabaseType.PostgreSQL),
		WIN1251_TO_WIN866("Win1251转Win866", DatabaseType.PostgreSQL),
		WIN866_TO_ISO("Win866转ISO", DatabaseType.PostgreSQL),
		WIN866_TO_KOI8R("Win866转KOI8R", DatabaseType.PostgreSQL),
		WIN866_TO_MIC("Win866转MIC", DatabaseType.PostgreSQL),
		WIN866_TO_WIN1251("Win866转Win1251", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    EncodingFunction(String title) {
        this(title, null, null);
    }
    EncodingFunction(String title, String params) {
        this(title, params, null);
    }
    EncodingFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    EncodingFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.ENCODING; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}