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


package org.anyline.data.jdbc.bigquery;


import org.anyline.data.metadata.function.*;
import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * Google BigQuery(GoogleSQL) 系统函数定义。
 * <p>Google BigQuery(GoogleSQL) 内置函数集，覆盖数学、字符串、日期时间、聚合、条件、类型转换等类别。</p>
 */

public enum BigQueryFunction implements SystemFunction {
	// ========== 数学函数 ==========
	ABS(MathFunction.ABS,"ABS", "ABS(${expression})"),
	ACOS(MathFunction.ACOS,"ACOS", "ACOS(${expression})"),
	ADD_MONTHS(DateTimeFunction.ADD_MONTHS,"ADD_MONTHS", "ADD_MONTHS(${date}, ${interval})"),
	ASCII(StringFunction.ASCII,"ASCII", "ASCII(${expression})"),
	ASIN(MathFunction.ASIN,"ASIN", "ASIN(${expression})"),
	ATAN(MathFunction.ATAN,"ATAN", "ATAN(${expression})"),
	ATAN2(MathFunction.ATAN2,"ATAN2", "ATAN2(${y}, ${x})"),
	AVG(AggregateFunction.AVG,"AVG", "AVG(${expression})"),

	// ========== 类型转换 ==========
	CAST(TypeCastFunction.CAST,"CAST", "CAST(${expression} AS ${type})"),

	// ========== 数学函数 ==========
	CEIL(MathFunction.CEIL,"CEIL", "CEIL(${expression})"),
	CEILING(MathFunction.CEILING,"CEILING", "CEILING(${expression})"),

	// ========== 字符串函数 ==========
	CHAR_LENGTH(StringFunction.CHAR_LENGTH,"CHAR_LENGTH", "CHAR_LENGTH(${expression})"),

	// ========== NULL处理 ==========
	COALESCE(NullFunction.COALESCE,"COALESCE", "COALESCE(${expression1}, ${expression2})"),

	// ========== 字符串函数 ==========
	CONCAT(StringFunction.CONCAT,"CONCAT", "CONCAT(${expression}, ...)"),
	CONCAT_WS(StringFunction.CONCAT_WS,"CONCAT_WS", "CONCAT_WS(${separator}, ${expression}, ...)"),
	COS(MathFunction.COS,"COS", "COS(${expression})"),
	COSH(MathFunction.COSH,"COSH", "COSH(${expression})"),
	COT(MathFunction.COT,"COT", "COT(${expression})"),
	COUNT(AggregateFunction.COUNT,"COUNT", "COUNT(${expression})"),

	// ========== 日期时间函数 ==========
	CURRENT_DATE(DateTimeFunction.CURRENT_DATE,"CURRENT_DATE", "CURRENT_DATE()"),
	CURRENT_TIMESTAMP(DateTimeFunction.CURRENT_TIMESTAMP,"CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP()"),

	// ========== 日期时间函数 ==========
	DATE_ADD(DateTimeFunction.DATE_ADD,"DATE_ADD", "DATE_ADD(${date}, INTERVAL ${interval} DAY)"),
	DATE_DIFF(DateTimeFunction.DATE_DIFF,"DATEDIFF", "DATE_DIFF(${end}, ${start}, DAY)"),
	DATE_FORMAT(DateTimeFunction.DATE_FORMAT,"DATE_FORMAT", "FORMAT_TIMESTAMP(${format}, ${expression})"),
	DATE_SUB(DateTimeFunction.DATE_SUB,"DATE_SUB", "DATE_SUB(${date}, INTERVAL ${interval} DAY)"),
	DATE_TRUNC(DateTimeFunction.DATE_TRUNC,"DATE_TRUNC", "DATE_TRUNC(${date}, ${unit})"),
	DAY(DateTimeFunction.DAY,"DAY", "DAY(${date})"),
	DAY_OF_WEEK(DateTimeFunction.DAY_OF_WEEK,"DAYOFWEEK", "DAYOFWEEK(${date})"),
	DAY_OF_YEAR(DateTimeFunction.DAY_OF_YEAR,"DAYOFYEAR", "DAYOFYEAR(${date})"),
	DAYNAME(DateTimeFunction.DAYNAME,"DAYNAME", "DAYNAME(${date})"),
	DEGREES(MathFunction.DEGREES,"DEGREES", "DEGREES(${expression})"),
	DENSE_RANK(AggregateFunction.DENSE_RANK,"DENSE_RANK", "DENSE_RANK() OVER (${window_spec})"),
	E(MathFunction.E,"E", "E()"),
	EXP(MathFunction.EXP,"EXP", "EXP(${expression})"),
	FACTORIAL(MathFunction.FACTORIAL,"FACTORIAL", "FACTORIAL(${expression})"),
	FIRST_VALUE(AggregateFunction.FIRST_VALUE,"FIRST_VALUE", "FIRST_VALUE(${expression})"),
	FLOOR(MathFunction.FLOOR,"FLOOR", "FLOOR(${expression})"),
	FROM_UNIXTIME(DateTimeFunction.FROM_UNIXTIME,"FROM_UNIXTIME", "TIMESTAMP_SECONDS(${timestamp})"),

	// ========== 条件函数 ==========
	GREATEST(ConditionalFunction.GREATEST,"GREATEST", "GREATEST(${expression1}, ${expression2})"),
	GROUP_CONCAT(StringFunction.GROUP_CONCAT,"GROUP_CONCAT", "STRING_AGG(${expression})"),

	// ========== 日期时间函数 ==========
	HOUR(DateTimeFunction.HOUR,"HOUR", "HOUR(${time})"),

	// ========== 条件函数 ==========
	IF(ConditionalFunction.IF_ELSE,"IF", "IF(${condition}, ${true_value}, ${false_value})"),
	IFNULL(NullFunction.NVL,"IFNULL", "IFNULL(${expression}, ${replacement})"),

	// ========== 字符串函数 ==========
	INITCAP(StringFunction.INITCAP,"INITCAP", "INITCAP(${expression})"),
	INSTR(StringFunction.INSTR,"INSTR", "INSTR(${expression}, ${substring})"),
	ISNULL(BooleanFunction.IS_NULL,"ISNULL", "ISNULL(${expression})"),
	ISNOTNULL(BooleanFunction.IS_NOT_NULL,"ISNOTNULL", "ISNOTNULL(${expression})"),

	// ========== 聚合/窗口函数 ==========
	LAG(AggregateFunction.LAG,"LAG", "LAG(${expression})"),
	LAST_DAY(DateTimeFunction.LAST_DAY,"LAST_DAY", "LAST_DAY(${date})"),
	LAST_VALUE(AggregateFunction.LAST_VALUE,"LAST_VALUE", "LAST_VALUE(${expression})"),
	LEAD(AggregateFunction.LEAD,"LEAD", "LEAD(${expression})"),
	LEAST(ConditionalFunction.LEAST,"LEAST", "LEAST(${expression1}, ${expression2})"),

	// ========== 字符串函数 ==========
	LENGTH(StringFunction.LENGTH,"LENGTH", "LENGTH(${expression})"),
	LN(MathFunction.LN,"LN", "LN(${expression})"),
	LOCATE(StringFunction.LOCATE,"LOCATE", "STRPOS(${expression}, ${substring})"),

	// ========== 数学函数 ==========
	LOG(MathFunction.LOG,"LOG", "LOG(${expression})"),
	LOG10(MathFunction.LOG10,"LOG10", "LOG10(${expression})"),
	LOG2(MathFunction.LOG2,"LOG2", "LOG2(${expression})"),

	// ========== 字符串函数 ==========
	LOWER(StringFunction.LOWER,"LOWER", "LOWER(${expression})"),
	LPAD(StringFunction.LPAD,"LPAD", "LPAD(${expression}, ${length}, ${pad})"),
	LTRIM(StringFunction.LTRIM,"LTRIM", "LTRIM(${expression})"),

	// ========== 聚合函数 ==========
	MAX(AggregateFunction.MAX,"MAX", "MAX(${expression})"),
	MIN(AggregateFunction.MIN,"MIN", "MIN(${expression})"),

	// ========== 日期时间函数 ==========
	MINUTE(DateTimeFunction.MINUTE,"MINUTE", "MINUTE(${expression})"),

	// ========== 数学函数 ==========
	MOD(MathFunction.MOD,"MOD", "MOD(${dividend}, ${divisor})"),

	// ========== 日期时间函数 ==========
	MONTH(DateTimeFunction.MONTH,"MONTH", "MONTH(${date})"),
	MONTHS_BETWEEN(DateTimeFunction.MONTHS_BETWEEN,"MONTHS_BETWEEN", "MONTHS_BETWEEN(${date1}, ${date2})"),

	// ========== 日期时间函数 ==========
	NOW(DateTimeFunction.NOW,"NOW", "CURRENT_TIMESTAMP()"),
	NTILE(AggregateFunction.NTILE,"NTILE", "NTILE(${n})"),
	NULLIF(NullFunction.NULLIF,"NULLIF", "NULLIF(${expression1}, ${expression2})"),
	NVL(NullFunction.NVL,"NVL", "IFNULL(${expression}, ${replacement})"),
	NVL2(BooleanFunction.NVL2,"NVL2", "IF(${expression} IS NOT NULL, ${not_null}, ${null})"),

	// ========== 数学函数 ==========
	PI(MathFunction.PI,"PI", "PI()"),
	POW(MathFunction.POW,"POW", "POW(${base}, ${exponent})"),
	POWER(MathFunction.POWER,"POWER", "POWER(${base}, ${exponent})"),

	// ========== 日期时间函数 ==========
	QUARTER(DateTimeFunction.QUARTER,"QUARTER", "QUARTER(${date})"),

	// ========== 数学函数 ==========
	RADIANS(MathFunction.RADIANS,"RADIANS", "RADIANS(${expression})"),
	RAND(MathFunction.RAND,"RAND", "RAND()"),
	RANDOM(MathFunction.RANDOM,"RANDOM", "RAND()"),
	RANK(AggregateFunction.RANK,"RANK", "RANK() OVER (${window_spec})"),

	// ========== 正则函数 ==========
	REGEXP_EXTRACT(RegexFunction.REGEXP_LIKE,"REGEXP_EXTRACT", "REGEXP_EXTRACT(${expression}, ${pattern})"),
	REGEXP_LIKE(RegexFunction.REGEXP_LIKE,"REGEXP_LIKE", "REGEXP_CONTAINS(${expression}, ${pattern})"),
	REGEXP_REPLACE(RegexFunction.REGEXP_REPLACE,"REGEXP_REPLACE", "REGEXP_REPLACE(${expression}, ${pattern}, ${replacement})"),

	// ========== 字符串函数 ==========
	REPEAT(StringFunction.REPEAT,"REPEAT", "REPEAT(${expression}, ${count})"),
	REPLACE(StringFunction.REPLACE,"REPLACE", "REPLACE(${expression}, ${search}, ${replace})"),
	REVERSE(StringFunction.REVERSE,"REVERSE", "REVERSE(${expression})"),
	ROUND(MathFunction.ROUND,"ROUND", "ROUND(${expression})"),
	ROW_NUMBER(AggregateFunction.ROW_NUMBER,"ROW_NUMBER", "ROW_NUMBER() OVER (${window_spec})"),
	RPAD(StringFunction.RPAD,"RPAD", "RPAD(${expression}, ${length}, ${pad})"),
	RTRIM(StringFunction.RTRIM,"RTRIM", "RTRIM(${expression})"),

	// ========== 日期时间函数 ==========
	SECOND(DateTimeFunction.SECOND,"SECOND", "SECOND(${expression})"),

	// ========== 数学函数 ==========
	SIGN(MathFunction.SIGN,"SIGN", "SIGN(${expression})"),
	SIN(MathFunction.SIN,"SIN", "SIN(${expression})"),
	SINH(MathFunction.SINH,"SINH", "SINH(${expression})"),

	// ========== 字符串函数 ==========
	SPACE(StringFunction.SPACE,"SPACE", "SPACE(${count})"),
	SPLIT_PART(StringFunction.SPLIT_PART,"SPLIT_PART", "SPLIT(${expression}, ${delimiter})[SAFE_ORDINAL(${index})]"),

	// ========== 数学函数 ==========
	SQRT(MathFunction.SQRT,"SQRT", "SQRT(${expression})"),

	// ========== 聚合函数 ==========
	STDDEV(AggregateFunction.STDDEV,"STDDEV", "STDDEV(${expression})"),
	STDDEV_POP(AggregateFunction.STDDEV_POP,"STDDEV_POP", "STDDEV_POP(${expression})"),
	STDDEV_SAMP(AggregateFunction.STDDEV_SAMP,"STDDEV_SAMP", "STDDEV_SAMP(${expression})"),

	// ========== 字符串函数 ==========
	SUBSTR(StringFunction.SUBSTR,"SUBSTR", "SUBSTR(${expression}, ${start})"),
	SUBSTRING(StringFunction.SUBSTRING,"SUBSTRING", "SUBSTRING(${expression}, ${start}, ${length})"),

	// ========== 聚合函数 ==========
	SUM(AggregateFunction.SUM,"SUM", "SUM(${expression})"),

	// ========== 数学函数 ==========
	TAN(MathFunction.TAN,"TAN", "TAN(${expression})"),
	TANH(MathFunction.TANH,"TANH", "TANH(${expression})"),

	// ========== 日期时间函数 ==========
	TIMESTAMP(DateTimeFunction.TIMESTAMP,"TIMESTAMP", "TIMESTAMP(${expression})"),

	// ========== 类型转换 ==========
	TO_CHAR(TypeCastFunction.TO_CHAR,"CAST", "CAST(${expression} AS STRING)"),
	TO_DATE(TypeCastFunction.TO_DATE,"TO_DATE", "TO_DATE(${expression})"),
	TO_TIMESTAMP(TypeCastFunction.TO_TIMESTAMP,"TO_TIMESTAMP", "TO_TIMESTAMP(${expression})"),

	// ========== 字符串函数 ==========
	TRANSLATE(StringFunction.TRANSLATE,"TRANSLATE", "TRANSLATE(${expression}, ${from}, ${to})"),
	TRIM(StringFunction.TRIM,"TRIM", "TRIM(${expression})"),
	TRUNC(MathFunction.TRUNC,"TRUNC", "TRUNC(${expression}, ${precision})"),
	TRUNCATE(MathFunction.TRUNCATE,"TRUNCATE", "TRUNCATE(${expression}, ${precision})"),

	// ========== 日期时间函数 ==========
	UNIX_TIMESTAMP(DateTimeFunction.UNIX_TIMESTAMP,"UNIX_TIMESTAMP", "UNIX_SECONDS(CURRENT_TIMESTAMP())"),

	// ========== 字符串函数 ==========
	UPPER(StringFunction.UPPER,"UPPER", "UPPER(${expression})"),

	// ========== UUID函数 ==========
	UUID(UuidFunction.UUID,"UUID", "UUID()"),

	// ========== 聚合函数 ==========
	VARIANCE(AggregateFunction.VARIANCE,"VARIANCE", "VARIANCE(${expression})"),
	VAR_POP(AggregateFunction.VAR_POP,"VAR_POP", "VAR_POP(${expression})"),
	VAR_SAMP(AggregateFunction.VAR_SAMP,"VAR_SAMP", "VAR_SAMP(${expression})"),

	// ========== 日期时间函数 ==========
	WEEK(DateTimeFunction.WEEK,"WEEK", "WEEK(${date})"),
	WEEK_OF_YEAR(DateTimeFunction.WEEK_OF_YEAR,"WEEKOFYEAR", "WEEKOFYEAR(${date})"),

	// ========== 日期时间函数 ==========
	YEAR(DateTimeFunction.YEAR,"YEAR", "YEAR(${date})"),

	// ========== 布尔函数 ==========
	IS_NULL(BooleanFunction.IS_NULL,"IS_NULL", "IS_NULL(${expression})"),
	IS_NOT_NULL(BooleanFunction.IS_NOT_NULL,"IS_NOT_NULL", "IS_NOT_NULL(${expression})"),

	// ========== 加密函数 ==========
	MD5(CryptoFunction.MD5,"MD5", "TO_HEX(MD5(${expression}))"),
	SHA1(CryptoFunction.SHA1,"SHA1", "SHA1(${expression})"),
	SHA2(CryptoFunction.SHA2,"SHA2", "TO_HEX(SHA256(${expression}))"),
	SHA256(CryptoFunction.SHA256,"SHA256", "SHA256(${expression})"),
	SHA512(CryptoFunction.SHA512,"SHA512", "SHA512(${expression})"),

	// ========== 系统函数 ==========
	VERSION(SysFunction.VERSION,"VERSION", "VERSION()", false),
	CURRENT_DATABASE(SysFunction.CURRENT_DATABASE,"CURRENT_DATABASE", "SESSION_USER()"),
	USER(SysFunction.USER,"USER", "SESSION_USER()"),
	;

	private final String title;
	private final META meta;
	private final String formula;
	private boolean support = true;
	private List<String> params = null;
	private List<String> orders = null;

	BigQueryFunction(META meta, String title, String formula) {
		this(meta, title, formula, true);
	}
	BigQueryFunction(META meta, String title, String formula, boolean support) {
		this.meta = meta;
		this.title = title;
		this.formula = formula;
		this.support = support;
	}
	BigQueryFunction(META meta, boolean support) {
		this(meta, null, null, support);
	}

	@Override public boolean support() { return support; }
	@Override public void support(boolean s) { this.support = s; }
	@Override public String title() { return title; }
	@Override public String formula() { return formula; }
	@Override public DatabaseType database() { return DatabaseType.GoogleBigQuery; }
	@Override public META meta() { return meta; }
	@Override public List<String> params() { return params; }
	@Override public void params(List<String> p) { this.params = p; }
	@Override
	public List<String> orders() {
		if (orders != null && !orders.isEmpty()) return orders;
		if (formula != null && formula.contains("${")) orders = SystemFunction.extractPlaceholders(formula);
		return orders;
	}
}