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


package org.anyline.data.jdbc.function.init;


import org.anyline.data.metadata.function.*;
import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * Apache Impala / HiveQL 系统函数定义。
 * <p>Impala 查询引擎操作 Hive 表时使用的函数集。</p>
 */

public enum ImpalaGenusFunction implements SystemFunction {
	ABS(MathFunction.ABS,"ABS", "ABS(${expression})"),
	ACOS(MathFunction.ACOS,"ACOS", "ACOS(${expression})"),
	ADD_MONTHS(DateTimeFunction.ADD_MONTHS,"ADD_MONTHS", "ADD_MONTHS(${date}, ${interval})"),
	ASCII(StringFunction.ASCII,"ASCII", "ASCII(${expression})"),
	ASIN(MathFunction.ASIN,"ASIN", "ASIN(${expression})"),
	ATAN(MathFunction.ATAN,"ATAN", "ATAN(${expression})"),
	AVG(AggregateFunction.AVG,"AVG", "AVG(${expression})"),
	CAST(TypeCastFunction.CAST,"CAST", "CAST(${expression} AS ${type})"),
	CEIL(MathFunction.CEIL,"CEIL", "CEIL(${expression})"),
	CEILING(MathFunction.CEILING,"CEILING", "CEILING(${expression})"),
	CHAR_LENGTH(StringFunction.CHAR_LENGTH,"CHAR_LENGTH", "CHAR_LENGTH(${expression})"),
	COALESCE(NullFunction.COALESCE,"COALESCE", "COALESCE(${expression1}, ${expression2})"),
	CONCAT(StringFunction.CONCAT,"CONCAT", "CONCAT(${expression}, ...)"),
	CONCAT_WS(StringFunction.CONCAT_WS,"CONCAT_WS", "CONCAT_WS(${separator}, ${expression}, ...)"),
	COS(MathFunction.COS,"COS", "COS(${expression})"),
	COSH(MathFunction.COSH,"COSH", "COSH(${expression})"),
	COT(MathFunction.COT,"COT", "COT(${expression})"),
	COUNT(AggregateFunction.COUNT,"COUNT", "COUNT(${expression})"),
	CURRENT_DATE(DateTimeFunction.CURRENT_DATE,"CURRENT_DATE", "CURRENT_DATE()"),
	CURRENT_TIMESTAMP(DateTimeFunction.CURRENT_TIMESTAMP,"CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP()"),
	DATE_ADD(DateTimeFunction.DATE_ADD,"DATE_ADD", "DATE_ADD(${date}, ${interval})"),
	DATE_DIFF(DateTimeFunction.DATE_DIFF,"DATEDIFF", "DATEDIFF(${start}, ${end})"),
	DATE_FORMAT(DateTimeFunction.DATE_FORMAT,"DATE_FORMAT", "DATE_FORMAT(${expression}, ${format})"),
	DATE_SUB(DateTimeFunction.DATE_SUB,"DATE_SUB", "DATE_SUB(${date}, ${interval})"),
	DATE_TRUNC(DateTimeFunction.DATE_TRUNC,"DATE_TRUNC", "DATE_TRUNC(${date}, ${unit})"),
	DAY(DateTimeFunction.DAY,"DAY", "DAY(${date})"),
	DAY_OF_WEEK(DateTimeFunction.DAY_OF_WEEK,"DAYOFWEEK", "DAYOFWEEK(${date})"),
	DAY_OF_YEAR(DateTimeFunction.DAY_OF_YEAR,"DAYOFYEAR", "DAYOFYEAR(${date})"),
	DAYNAME(DateTimeFunction.DAYNAME,"DAYNAME", "DAYNAME(${date})"),
	DEGREES(MathFunction.DEGREES,"DEGREES", "DEGREES(${expression})"),
	DENSE_RANK(AggregateFunction.DENSE_RANK,"DENSE_RANK", "DENSE_RANK() OVER (${window_spec})"),
	EXP(MathFunction.EXP,"EXP", "EXP(${expression})"),
	FACTORIAL(MathFunction.FACTORIAL,"FACTORIAL", "FACTORIAL(${expression})"),
	FIRST_VALUE(AggregateFunction.FIRST_VALUE,"FIRST_VALUE", "FIRST_VALUE(${expression})"),
	FLOOR(MathFunction.FLOOR,"FLOOR", "FLOOR(${expression})"),
	FROM_UNIXTIME(DateTimeFunction.FROM_UNIXTIME,"FROM_UNIXTIME", "FROM_UNIXTIME(${timestamp})"),
	GREATEST(ConditionalFunction.GREATEST,"GREATEST", "GREATEST(${expression1}, ${expression2})"),
	HOUR(DateTimeFunction.HOUR,"HOUR", "HOUR(${time})"),
	INITCAP(StringFunction.INITCAP,"INITCAP", "INITCAP(${expression})"),
	INSTR(StringFunction.INSTR,"INSTR", "INSTR(${expression}, ${substring})"),
	LAG(AggregateFunction.LAG,"LAG", "LAG(${expression})"),
	LAST_DAY(DateTimeFunction.LAST_DAY,"LAST_DAY", "LAST_DAY(${date})"),
	LAST_VALUE(AggregateFunction.LAST_VALUE,"LAST_VALUE", "LAST_VALUE(${expression})"),
	LEAD(AggregateFunction.LEAD,"LEAD", "LEAD(${expression})"),
	LEAST(ConditionalFunction.LEAST,"LEAST", "LEAST(${expression1}, ${expression2})"),
	LENGTH(StringFunction.LENGTH,"LENGTH", "LENGTH(${expression})"),
	LN(MathFunction.LN,"LN", "LN(${expression})"),
	LOCATE(StringFunction.LOCATE,"LOCATE", "LOCATE(${substring}, ${expression})"),
	LOG10(MathFunction.LOG10,"LOG10", "LOG10(${expression})"),
	LOG2(MathFunction.LOG2,"LOG2", "LOG2(${expression})"),
	LOWER(StringFunction.LOWER,"LOWER", "LOWER(${expression})"),
	LPAD(StringFunction.LPAD,"LPAD", "LPAD(${expression}, ${length}, ${pad})"),
	LTRIM(StringFunction.LTRIM,"LTRIM", "LTRIM(${expression})"),
	MAX(AggregateFunction.MAX,"MAX", "MAX(${expression})"),
	MD5(CryptoFunction.MD5,"MD5", "MD5(${expression})"),
	MIN(AggregateFunction.MIN,"MIN", "MIN(${expression})"),
	MINUTE(DateTimeFunction.MINUTE,"MINUTE", "MINUTE(${expression})"),
	MOD(MathFunction.MOD,"MOD", "MOD(${dividend}, ${divisor})"),
	MONTH(DateTimeFunction.MONTH,"MONTH", "MONTH(${date})"),
	MONTHS_BETWEEN(DateTimeFunction.MONTHS_BETWEEN,"MONTHS_BETWEEN", "MONTHS_BETWEEN(${date1}, ${date2})"),
	NOW(DateTimeFunction.NOW,"NOW", "NOW()"),
	NTILE(AggregateFunction.NTILE,"NTILE", "NTILE(${n})"),
	NULLIF(NullFunction.NULLIF,"NULLIF", "NULLIF(${expression1}, ${expression2})"),
	NVL(NullFunction.NVL,"NVL", "NVL(${expression}, ${replacement})"),
	NVL2(BooleanFunction.NVL2,"NVL2", "NVL2(${expression}, ${not_null}, ${null})"),
	PI(MathFunction.PI,"PI", "PI()"),
	POW(MathFunction.POW,"POW", "POW(${base}, ${exponent})"),
	POWER(MathFunction.POWER,"POWER", "POWER(${base}, ${exponent})"),
	QUARTER(DateTimeFunction.QUARTER,"QUARTER", "QUARTER(${date})"),
	RADIANS(MathFunction.RADIANS,"RADIANS", "RADIANS(${expression})"),
	RAND(MathFunction.RAND,"RAND", "RAND()"),
	RANK(AggregateFunction.RANK,"RANK", "RANK() OVER (${window_spec})"),
	REGEXP_EXTRACT(RegexFunction.REGEXP_LIKE,"REGEXP_EXTRACT", "REGEXP_EXTRACT(${expression}, ${pattern})"),
	REGEXP_REPLACE(RegexFunction.REGEXP_REPLACE,"REGEXP_REPLACE", "REGEXP_REPLACE(${expression}, ${pattern}, ${replacement})"),
	REPLACE(StringFunction.REPLACE,"REPLACE", "REPLACE(${expression}, ${search}, ${replace})"),
	REVERSE(StringFunction.REVERSE,"REVERSE", "REVERSE(${expression})"),
	ROUND(MathFunction.ROUND,"ROUND", "ROUND(${expression})"),
	ROW_NUMBER(AggregateFunction.ROW_NUMBER,"ROW_NUMBER", "ROW_NUMBER() OVER (${window_spec})"),
	RPAD(StringFunction.RPAD,"RPAD", "RPAD(${expression}, ${length}, ${pad})"),
	RTRIM(StringFunction.RTRIM,"RTRIM", "RTRIM(${expression})"),
	SECOND(DateTimeFunction.SECOND,"SECOND", "SECOND(${expression})"),
	SHA1(CryptoFunction.SHA1,"SHA1", "SHA1(${expression})"),
	SIGN(MathFunction.SIGN,"SIGN", "SIGN(${expression})"),
	SIN(MathFunction.SIN,"SIN", "SIN(${expression})"),
	SINH(MathFunction.SINH,"SINH", "SINH(${expression})"),
	SPLIT_PART(StringFunction.SPLIT_PART,"SPLIT_PART", "SPLIT_PART(${expression}, ${delimiter}, ${index})"),
	SQRT(MathFunction.SQRT,"SQRT", "SQRT(${expression})"),
	STDDEV(AggregateFunction.STDDEV,"STDDEV", "STDDEV(${expression})"),
	SUBSTR(StringFunction.SUBSTR,"SUBSTR", "SUBSTR(${expression}, ${start})"),
	SUBSTRING(StringFunction.SUBSTRING,"SUBSTRING", "SUBSTRING(${expression}, ${start}, ${length})"),
	SUM(AggregateFunction.SUM,"SUM", "SUM(${expression})"),
	TAN(MathFunction.TAN,"TAN", "TAN(${expression})"),
	TANH(MathFunction.TANH,"TANH", "TANH(${expression})"),
	TO_CHAR(TypeCastFunction.TO_CHAR,"CAST", "CAST(${expression} AS STRING)"),
	TO_DATE(TypeCastFunction.TO_DATE,"TO_DATE", "TO_DATE(${expression})"),
	TO_TIMESTAMP(TypeCastFunction.TO_TIMESTAMP,"TO_TIMESTAMP", "TO_TIMESTAMP(${expression})"),
	TRANSLATE(StringFunction.TRANSLATE,"TRANSLATE", "TRANSLATE(${expression}, ${from}, ${to})"),
	TRIM(StringFunction.TRIM,"TRIM", "TRIM(${expression})"),
	TRUNC(MathFunction.TRUNC,"TRUNC", "TRUNC(${expression}, ${precision})"),
	TRUNCATE(MathFunction.TRUNCATE,"TRUNC", "TRUNC(${expression}, ${precision})"),
	UNIX_TIMESTAMP(DateTimeFunction.UNIX_TIMESTAMP,"UNIX_TIMESTAMP", "UNIX_TIMESTAMP()"),
	UPPER(StringFunction.UPPER,"UPPER", "UPPER(${expression})"),
	UUID(UuidFunction.UUID,"UUID", "UUID()"),
	VARIANCE(AggregateFunction.VARIANCE,"VARIANCE", "VARIANCE(${expression})"),
	WEEK(DateTimeFunction.WEEK,"WEEK", "WEEK(${date})"),
	WEEK_OF_YEAR(DateTimeFunction.WEEK_OF_YEAR,"WEEKOFYEAR", "WEEKOFYEAR(${date})"),
	YEAR(DateTimeFunction.YEAR,"YEAR", "YEAR(${date})"),
	;


	private final String title;
	private final META meta;
	private final String formula;
	private boolean support = true;
	private List<String> params = null;
	private List<String> orders = null;

	ImpalaGenusFunction(META meta, String title, String formula) {
		this(meta, title, formula, true);
	}
	ImpalaGenusFunction(META meta, String title, String formula, boolean support) {
		this.meta = meta;
		this.title = title;
		this.formula = formula;
		this.support = support;
	}

	@Override public boolean support() { return support; }
	@Override public void support(boolean s) { this.support = s; }
	@Override public String title() { return title; }
	@Override public String formula() { return formula; }
	@Override public DatabaseType database() { return DatabaseType.Impala; }
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