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


import org.anyline.metadata.DateFormatPattern;

/**
 * Apache Impala / HiveQL 日期格式化参数定义。
 * <p>Impala 使用 Java SimpleDateFormat 风格的格式字符串。</p>
 */

public enum ImpalaGenusDateFormatPattern implements DateFormatPattern {
	// 年份
	Y(META.YEAR, "y"),
	YY(META.YY, "yy"),
	YYYY(META.YYYY, "yyyy"),
	IY(META.IY, "yy"),
	IYYY(META.IYYY, "yyyy"),

	// 月份
	MM(META.MM, "MM"),
	MONTH(META.MONTH, "MMMM"),
	Month(META.MONTH, "MMMM"),
	MON(META.MON, "MMM"),
	Mon(META.MON, "MMM"),
	RM(META.RM, "M"),

	// 日
	DD(META.DD, "dd"),
	DDD(META.DDD, "D"),
	DAY_OF_MONTH(META.DAY_OF_MONTH, "d"),
	DAY_OF_MONTH_TH(META.DAY_OF_MONTH_TH, "dd"),
	DAY_OF_YEAR(META.DAY_OF_YEAR, "D"),

	// 星期
	DAY(META.DAY, "EEEE"),
	Day(META.DAY, "EEEE"),
	day(META.day, "EEEE"),
	DY(META.DY, "EEE"),
	Dy(META.Dy, "EEE"),
	dy(META.dy, "EEE"),
	WEEK_NAME(META.WEEK_NAME, "EEEE"),
	WEEK_NM(META.WEEK_NM, "EEEE"),
	DAY_OF_WEEK(META.DAY_OF_WEEK, "u"),
	DY_OF_WEEK(META.DY_OF_WEEK, "u"),
	ID(META.ID, "u"),
	WEEK_OF_YEAR(META.WEEK_OF_YEAR, "w"),
	WK_OF_YEAR(META.WK_OF_YEAR, "w"),

	// 周
	W(META.W, "W"),
	WW(META.WW, "w"),
	IW(META.IW, "w"),
	WEEK(META.WEEK, "w"),

	// 小时
	HH12(META.HH12, "hh"),
	HH24(META.HH24, "HH"),
	hh12(META.HH12, "hh"),
	hh24(META.HH24, "HH"),

	// 分钟
	MI(META.MI, "mm"),

	// 秒
	SS(META.SS, "ss"),

	// 毫秒/微秒
	MS(META.MS, "SSS"),
	US(META.US, "SSSSSS"),
	FF(META.FF, "S"),
	FF1(META.FF1, "S"),
	FF2(META.FF2, "SS"),
	FF3(META.FF3, "SSS"),
	FF4(META.FF4, "SSSS"),
	FF5(META.FF5, "SSSSS"),
	FF6(META.FF6, "SSSSSS"),

	// 子午指示
	AM(META.AM, "am"),
	am(META.am, "am"),
	PM(META.PM, "pm"),
	pm(META.pm, "pm"),

	// 时间组合
	TIME(META.TIME, "HH:mm:ss"),
	TIME_AP(META.TIME_AP, "hh:mm:ss a"),
	TS(META.TS, "HH:mm:ss"),

	// 世纪
	CC(META.CC, "yy"),

	// 季度
	Q(META.Q, "Q"),

	// 儒略日
	J(META.J, "D"),

	// 时区（Impala 时区支持有限）
	TZ(META.TZ, "z"),
	tz(META.tz, "z"),

	// 纪元
	BC(META.BC, "G"),
	AD(META.AD, "G"),
	;

	private final String define;
	private final META meta;

	public META meta() {
		return meta;
	}

	public String define() {
		return define;
	}

	ImpalaGenusDateFormatPattern(META meta, String define) {
		this.meta = meta;
		this.define = define;
	}
}