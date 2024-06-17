/*
 * Copyright 2006-2023 www.anyline.org
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





package org.anyline.util;

import java.util.Date;

public class DateBuilder{
	private Date date = new Date();
	public DateBuilder(Date date) {
		this.date = date;
	}
	public DateBuilder(String date) {
		try {
			this.date = DateUtil.parse(date);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public DateBuilder(String date, String format) {
		try {
			this.date = DateUtil.parse(date, format);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public DateBuilder() {

	}
	public static DateBuilder init() {
		return new DateBuilder();
	}
	public static DateBuilder init(String date) {
		return new DateBuilder(date);
	}
	public static DateBuilder init(String date, String format) {
		return new DateBuilder(date, format);
	}
	public static DateBuilder init(Date date) {
		return new DateBuilder(date);
	}

	public Date getDate() {
		return date;
	}
	public Date date() {
		return date;
	}

	public DateBuilder addYear(int qty) {
		date = DateUtil.addYear(date, qty);
		return this;
	}
	public DateBuilder addMonth(int qty) {
		date = DateUtil.addMonth(date, qty);
		return this;
	}
	public DateBuilder addDay(int qty) {
		date = DateUtil.addDay(date, qty);
		return this;
	}
	public DateBuilder addHour(int qty) {
		date = DateUtil.addHour(date, qty);
		return this;
	}
	public DateBuilder addMinute(int qty) {
		date = DateUtil.addMinute(date, qty);
		return this;
	}
	public DateBuilder addSecond(int qty) {
		date = DateUtil.addSecond(date, qty);
		return this;
	}

	/**
	 * 是否在date之前
	 * @param date date
	 * @return boolean
	 */
	public boolean before(String date) throws Exception {
		return before(DateUtil.parse(date));
	}
	public boolean before(Date date) {
		return this.date.getTime() < date.getTime();
	}
	public boolean before() {
		return this.date.getTime() < new Date().getTime();
	}
	public boolean after(Date date) {
		return this.date.getTime() > date.getTime();
	}
	public boolean after() {
		return this.date.getTime() > new Date().getTime();
	}
	public boolean equal(Date date) {
		return this.date.getTime() == date.getTime();
	}
	public boolean between(Date min, Date max) {
		Long time = date.getTime();
		return time > min.getTime() && time <max.getTime();
	}
	public boolean between(String min, String max) throws Exception {
		return between(DateUtil.parse(min), DateUtil.parse(max));
	}

	/**
	 * 是否过期(在当前时间之前)
	 * @return boolean
	 */
	public boolean expire() {
		return before();
	}
	public String format(String format) {
		return DateUtil.format(date, format);
	}
	public String format() {
		return format(DateUtil.FORMAT_DATE_TIME);
	}

	/**
	 * 星期几
	 * @return String
	 */
	public String week() {
		return DateUtil.getWeek(date);
	}

	/**
	 * 年份
	 * @return int
	 */
	public int year() {
		return DateUtil.year(date);
	}

	/**
	 * 月份
	 * @return int
	 */
	public int month() {
		return DateUtil.month(date);
	}

	/**
	 * 当周第一天
	 * 周日作为一周的第一天
	 * @return DateBuilder
	 */
	public DateBuilder firstDayOfWeek() {
		date = DateUtil.getFirstDayOfWeek(date);
		return this;
	}

	/**
	 * 下周第一天
	 * @return DateBuilder
	 */

	public DateBuilder firstDayOfNextWeek() {
		date = DateUtil.getFirstDayOfNextWeek(date);
		return this;
	}

	/**
	 * 上个周第一天
	 * @return DateBuilder
	 */

	public DateBuilder firstDayOfPreviousWeek() {
		date = DateUtil.getFirstDayOfPreviousWeek(date);
		return this;
	}

	/**
	 * 当周最后天
	 * @return DateBuilder
	 */
	public DateBuilder lastDayOfWeek() {
		date = DateUtil.getLastDayOfWeek(date);
		return this;
	}

	/**
	 * 下周最后天
	 * @return DateBuilder
	 */
	public DateBuilder lastDayOfNextWeek() {
		date =  DateUtil.getLastDayOfNextWeek(date);
		return this;
	}

	/**
	 * 上个周最后天
	 * @return DateBuilder
	 */
	public DateBuilder lastDayOfPreviousWeek() {
		date = DateUtil.getLastDayOfPreviousWeek(date);
		return this;
	}

	/**
	 * 当月第一天
	 * @return DateBuilder
	 */

	public DateBuilder firstDayOfMonth() {
		date = DateUtil.getFirstDayOfMonth(date);
		return this;
	}

	/**
	 * 下个月第一天
	 * @return DateBuilder
	 */
	public DateBuilder firstDayOfNextMonth() {
		date = DateUtil.getFirstDayOfNextMonth(date);
		return this;
	}

	/**
	 * 上个月第一天
	 * @return DateBuilder
	 */

	public DateBuilder firstDayOfPreviousMonth() {
		date =  DateUtil.getFirstDayOfPreviousMonth(date);
		return this;
	}

	/**
	 * 当月最后一天
	 * @return DateBuilder
	 */
	public DateBuilder lastDayOfMonth() {
		date = DateUtil.getLastDayOfMonth(date);
		return this;
	}

	/**
	 * 上月最后一天
	 * @return DateBuilder
	 */

	public DateBuilder lastDayOfPreviousMonth() {
		date = DateUtil.getLastDayOfPreviousMonth(date);
		return this;
	}

	/**
	 * 下月最后一天
	 * @return DateBuilder
	 */

	public DateBuilder lastDayOfNextMonth() {
		date =  DateUtil.getLastDayOfNextMonth(date);
		return this;
	}

	// 本周星期日的日期
	public DateBuilder currentWeekday() {
		date =  DateUtil.getCurrentWeekday(date);
		return this;
	}
 
    /**
     *下周星期一的日期
     * @return DateBuilder
     */
	public DateBuilder mondayOFWeek() {
		date = DateUtil.getMondayOFWeek(date);
		return this;
	}

	public DateBuilder nextMonday() {
		date = DateUtil.getNextMonday(date);
		return this;
	}

	// 下周星期日的日期
	public DateBuilder nextSunday() {
		date = DateUtil.getNextSunday(date);
		return this;
	}

	/**
	 * 明年最后一天的日期
	 * @return DateBuilder
	 */
	public DateBuilder nextYearEnd() {
		date = DateUtil.getNextYearEnd(date);
		return this;
	}

	/**
	 * 明年第一天的日期
     * @return DateBuilder
	 */
	public DateBuilder nextYearFirst() {
		date = DateUtil.getNextYearFirst(date);
		return this;
	}
 

}
