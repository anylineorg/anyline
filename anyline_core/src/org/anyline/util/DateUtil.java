/* 
 * Copyright 2006-2015 the original author or authors.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	private static int MaxDate;// 一月最大天数
	private static int MaxYear;// 一年最大天数
	
	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.ms";
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DATE = "yyyy-MM-dd";
	private static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("ETC/GMT-8"), Locale.CHINESE);
	public static boolean between( Date cur,Date fr, Date to){
		if(cur.getTime() >= fr.getTime() && cur.getTime() <= to.getTime()){
			return true;
		}
		return false;
	}
	public static boolean between(String cur, String fr, String to){
		return between(parse(cur), parse(fr), parse(to));
	}
	public static long diff(int part, Date fr, Date to) {
		long result = 0;
		if (Calendar.YEAR == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.YEAR);
		}
		if (Calendar.MONTH == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR) * 12;
			calendar.setTime(fr);
			time -= calendar.get(Calendar.YEAR) * 12;
			calendar.setTime(to);
			time += calendar.get(Calendar.MONTH);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.MONTH);
		}
		if (Calendar.WEEK_OF_YEAR == part) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(fr);
			time -= calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(to);
			time += calendar.get(Calendar.WEEK_OF_YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.WEEK_OF_YEAR);
		}
		if (Calendar.DAY_OF_YEAR == part) {
			long time = to.getTime() / 1000 / 60 / 60 / 24;
			result = time - fr.getTime() / 1000 / 60 / 60 / 24;
		}
		if (Calendar.HOUR == part) {
			long time = to.getTime() / 1000 / 60 / 60;
			result = time - fr.getTime() / 1000 / 60 / 60;
		}
		if (Calendar.MINUTE == part) {
			long time = to.getTime() / 1000 / 60;
			result = time - fr.getTime() / 1000 / 60;
		}
		if (Calendar.SECOND == part) {
			long time = to.getTime() / 1000;
			result = time - fr.getTime() / 1000;
		}
		if (Calendar.MILLISECOND == part) {
			result = to.getTime() - fr.getTime();
		}
		return result;
	}

	public static long diff(int part, String fr, String to) {
		return diff(part, parse(fr), parse(to));
	}

	public static long diff(int part, Date fr) {
		return diff(part, fr, new Date());
	}

	public static long diff(int part, String fr) {
		return diff(part, parse(fr));
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format(Date date, String format) {
		if (null == date)
			return "";
		return new java.text.SimpleDateFormat(format).format(date);
//		calendar.setTime(date);
//		String year = calendar.get(Calendar.YEAR) + "";
//		String yy = calendar.get(Calendar.YEAR) % 100 + "";
//		String month = calendar.get(Calendar.MONTH) + 1 + "";
//		if (month.length() == 1)
//			month = "0" + month;
//		String dd = calendar.get(Calendar.DATE) + "";
//		if (dd.length() == 1)
//			dd = "0" + dd;
//		String h = calendar.get(Calendar.HOUR_OF_DAY) + "";
//		if (h.length() == 1)
//			h = "0" + h;
//		String m = calendar.get(Calendar.MINUTE) + "";
//		if (m.length() == 1)
//			m = "0" + m;
//		String s = calendar.get(Calendar.SECOND) + "";
//		if (s.length() == 1)
//			s = "0" + s;
//		String ms = calendar.get(Calendar.MILLISECOND) + "";
//
//		format = format.replace("yyyy", year);
//		format = format.replace("YYYY", year);
//		format = format.replace("yy", yy);
//		format = format.replace("YY", yy);
//		format = format.replace("MM", month);
//		format = format.replace("M", month);
//		format = format.replace("dd", dd);
//		format = format.replace("DD", dd);
//		format = format.replace("HH", h);
//		format = format.replace("hh", h);
//		format = format.replace("H", h);
//		format = format.replace("h", h);
//		format = format.replace("ms", ms);
//		format = format.replace("mm", m);
//		format = format.replace("m", m);
//		format = format.replace("ss", s);
//		format = format.replace("s", s);
//
//		return format;
	}

	public static String format() {
		return format(new Date(), FORMAT_DATE_TIME);
	}

	public static String format(String format) {
		return format(new Date(), format);
	}

	public static String format(Date date) {
		return format(date, FORMAT_FULL);
	}

	public static String format(String date, String format) {
		Date d = parse(date);
		return format(d, format);
	}

	/**
	 * 时间转换成分钟
	 * 
	 * @param hm
	 * @return
	 */
	public static int convertMinute(String hm) {
		int minute = -1;
		String sps[] = hm.split(":");
		int h = BasicUtil.parseInt(sps[0], 0);
		int m = BasicUtil.parseInt(sps[1], 0);
		minute = h * 60 + m;
		return minute;
	}

	public static int convertMinute() {
		String hm = format("hh:mm");
		return convertMinute(hm);
	}

	/**
	 * 得到二个日期间的间隔天数
	 */
	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		long day = 0;
		try {
			Date date = myFormatter.parse(sj1);
			Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return "";
		}
		return day + "";
	}

	/**
	 * 根据一个日期，返回是星期几的字符串
	 * 
	 * @param sdate
	 * @return
	 */
	public static String getWeek(String sdate) {
		// 再转换为时间
		Date date = DateUtil.strToDate(sdate);
		calendar.setTime(date);
		// int hour=c.get(Calendar.DAY_OF_WEEK);
		// hour中存的就是星期几了，其范围 1~7
		// 1=星期日 7=星期六，其他类推
		return new SimpleDateFormat("EEEE").format(calendar.getTime());
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 两个时间之间的天数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDays(String date1, String date2) {
		if (date1 == null || date1.equals(""))
			return 0;
		if (date2 == null || date2.equals(""))
			return 0;
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	// 获取当月第一天
	public static String getFirstDayOfMonth(Date date) {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(date);
		calendar.set(Calendar.DATE, 1);// 设为当前月的1号
		str = sdf.format(calendar.getTime());
		return str;
	}

	public static String getFirstDayOfMonth(String date) {
		return getFirstDayOfMonth(parse(date));
	}

	public static String getFirstDayOfMonth() {
		return getFirstDayOfMonth(new Date());
	}

	// 获得下个月第一天的日期
	public static String getFirstDayOfNextMonth(Date date) {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		str = sdf.format(calendar.getTime());
		return str;
	}

	public static String getFirstDayOfNextMonth(String date) {
		return getFirstDayOfNextMonth(parse(date));
	}

	public static String getFirstDayOfNextMonth() {
		return getFirstDayOfNextMonth(new Date());
	}

	// 上月第一天
	public static String getFirstDayOfPreviousMonth(Date date) {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(date);
		calendar.set(Calendar.DATE, 1);// 设为当前月的1号
		calendar.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		// lastDate.add(Calendar.DATE,-1);//减去一天，变为当月最后一天
		str = sdf.format(calendar.getTime());
		return str;
	}

	public static String getFirstDayOfPreviousMonth(String date) {
		return getFirstDayOfPreviousMonth(parse(date));
	}

	public static String getFirstDayOfPreviousMonth() {
		return getFirstDayOfPreviousMonth(new Date());
	}

	// 计算当月最后一天,返回字符串
	public static String getLastDayOfMonth(Date date) {
		String str = "";
		System.out.println(format(date));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("ETC/GMT-8"), Locale.CHINESE);
		calendar.setTimeInMillis(date.getTime()+100000);
		System.out.println(sdf.format(calendar.getTime()));
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设为当前月的1号
		System.out.println(sdf.format(calendar.getTime()));
		calendar.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		str = sdf.format(calendar.getTime());
		System.out.println(sdf.format(calendar.getTime()));
		calendar.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
		str = sdf.format(calendar.getTime());
		System.out.println(sdf.format(calendar.getTime()));
		return str;
	}

	public static String getLastDayOfMonth(String date) {
		return getLastDayOfMonth(parse(date));
	}

	public static String getLastDayOfMonth() {
		return getLastDayOfMonth(new Date());
	}

	// 获得上月最后一天的日期
	public static String getLastDayOfPreviousMonth(Date date) {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		str = sdf.format(calendar.getTime());
		return str;
	}

	public static String getLastDayOfPreviousMonth(String date) {
		return getLastDayOfPreviousMonth(parse(date));
	}

	public static String getLastDayOfPreviousMonth() {
		return getLastDayOfPreviousMonth(new Date());
	}

	// 获得下个月最后一天的日期
	public static String getLastDayOfNextMonth(Date date) {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 加一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		str = sdf.format(calendar.getTime());
		return str;
	}

	public static String getLastDayOfNextMonth(String date) {
		return getLastDayOfNextMonth(parse(date));
	}

	public static String getLastDayOfNextMonth() {
		return getLastDayOfNextMonth(new Date());
	}

	// 获得本周星期日的日期
	public static String getCurrentWeekday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
		Date monday = currentDate.getTime();

		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	// 获得当前日期与本周日相差的天数
	public static int getMondayPlus() {
		calendar.setTime(new Date());
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}

	// 获得本周一的日期
	public static String getMondayOFWeek() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	// 获得下周星期一的日期
	public static String getNextMonday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	// 获得下周星期日的日期
	public static String getNextSunday() {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 + 6);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public static int getMonthPlus() {
		calendar.setTime(new Date());
		int monthOfNumber = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
		MaxDate = calendar.get(Calendar.DATE);
		if (monthOfNumber == 1) {
			return -MaxDate;
		} else {
			return 1 - monthOfNumber;
		}
	}

	// 获得明年最后一天的日期
	public static String getNextYearEnd() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);// 加一个年
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		str = sdf.format(calendar.getTime());
		return str;
	}

	// 获得明年第一天的日期
	public static String getNextYearFirst() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.YEAR, 1);// 加一个年
		lastDate.set(Calendar.DAY_OF_YEAR, 1);
		str = sdf.format(lastDate.getTime());
		return str;
	}

	// 获得本年有多少天
	public static int getMaxYear() {
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		calendar.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = calendar.get(Calendar.DAY_OF_YEAR);
		return MaxYear;
	}

	private static int getYearPlus() {
		calendar.setTime(new Date());
		int yearOfNumber = calendar.get(Calendar.DAY_OF_YEAR);// 获得当天是一年中的第几天
		calendar.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		calendar.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = calendar.get(Calendar.DAY_OF_YEAR);
		if (yearOfNumber == 1) {
			return -MaxYear;
		} else {
			return 1 - yearOfNumber;
		}
	}

	// 获得本年第一天的日期
	public static String getCurrentYearFirst() {
		int yearPlus = getYearPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, yearPlus);
		Date yearDay = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preYearDay = df.format(yearDay);
		return preYearDay;
	}

	// 获得本年最后一天的日期 *
	public static String getCurrentYearEnd() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	// 获得上年����一天的日期 *
	public static String getPreviousYearFirst() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-1-1";
	}

	/**
	 * 获取某年某月的最后一天
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return 最后一天
	 */
	public static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;
	}

	/**
	 * 是否闰年
	 * 
	 * @param year
	 *            年
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * 转换成日期
	 * 
	 * @param dateString
	 * @param formatString
	 * @return
	 */
	public static Date parse(String date, String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		try {
			return df.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 转换成日期(使用默认格式)
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parse(String dateString) {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_FULL);
		try {
			date = sdf.parse(dateString);
		} catch (Exception e) {
			try {
				sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
				date = sdf.parse(dateString);
			} catch (Exception ex) {
				try {
					sdf = new SimpleDateFormat(FORMAT_DATE);
					date = sdf.parse(dateString);
				} catch (Exception exc) {
					try {
						sdf = new SimpleDateFormat("yyyyMMdd");
						date = sdf.parse(dateString);
					} catch (Exception exce) {
						try {
							sdf = new SimpleDateFormat();
							date = sdf.parse(dateString);
						} catch (Exception excep) {
							date = null;
						}
					}
				}
			}
		}
		return date;
	}

	/**
	 * 昨天
	 * 
	 * @return
	 */
	public static Date yesterday() {
		return addDay(-1);
	}

	/**
	 * 明天
	 * 
	 * @return
	 */
	public static Date tomorrow() {
		return addDay(1);
	}

	/**
	 * 现在
	 * 
	 * @return
	 */
	public static Date now() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * 按日加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addDay(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按日加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addDay(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按月加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addMonth(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}

	/**
	 * 按月加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addMonth(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}

	/**
	 * 按年加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addYear(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 当前日期所在周的第idx天 第1天：星期日 第7天：星期六
	 * 
	 * @param idx
	 * @param date
	 * @return
	 */
	public static Date dayOfWeek(int idx, Date date) {
		Date result = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - idx;
		cal.add(Calendar.DATE, -day_of_week);
		result = cal.getTime();
		return result;
	}

	public static Date dayOfWeek(int idx) {
		return dayOfWeek(idx, new Date());
	}

	/**
	 * 按年加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addYear(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按小时加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addHour(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}

	/**
	 * 按小时加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addHour(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}

	/**
	 * 按分钟加
	 * 
	 * @param value
	 * @return
	 */
	public static Date addMinute(int value) {
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}

	/**
	 * 按分钟加,指定日期
	 * 
	 * @param date
	 * @param value
	 * @return
	 */
	public static Date addMinute(Date date, int value) {
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}

	/**
	 * 年份
	 * 
	 * @return
	 */
	public static int year() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.YEAR);
	}

	public static int year(Date date) {
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 月份
	 * 
	 * @return
	 */
	public static int month() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.MONTH);
	}

	/**
	 * 日(号)
	 * 
	 * @return
	 */
	public static int day() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 小时(点)
	 * 
	 * @return
	 */
	public static int hour() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.HOUR);
	}

	/**
	 * 分钟
	 * 
	 * @return
	 */
	public static int minute() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.MINUTE);
	}

	/**
	 * 秒
	 * 
	 * @return
	 */
	public static int second() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.SECOND);
	}

	/**
	 * 星期几(礼拜几)
	 * 
	 * @return
	 */
	public static int weekday() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.DAY_OF_WEEK) - 1;
	}

	/**
	 * 一年中的第几个星期
	 * 
	 * @return
	 */
	public static int weekOfYear() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 是上午吗?
	 * 
	 * @return
	 */
	public static boolean isAm() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.AM_PM) == 0;
	}

	/**
	 * 是下午吗?
	 * 
	 * @return
	 */
	public static boolean isPm() {
		calendar.setTime(new Date());
		return calendar.get(Calendar.AM_PM) == 1;
	}
}