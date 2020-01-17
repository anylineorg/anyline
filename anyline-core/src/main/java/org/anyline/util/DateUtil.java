/*
 * Copyright 2006-2015 www.anyline.org
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
 *
 *
 */

package org.anyline.util;

import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class DateUtil {
	private static int MaxDate;// 一月最大天数

	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.S";
	public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_DATE = "yyyy-MM-dd";
	public static final int DATE_PART_YEAR = Calendar.YEAR;
	public static final int DATE_PART_MONTH = Calendar.MONTH;
	public static final int DATE_PART_DATE = Calendar.DATE;
	public static final int DATE_PART_DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
	public static final int DATE_PART_HOUR = Calendar.HOUR;
	public static final int DATE_PART_MINUTE = Calendar.MINUTE;
	public static final int DATE_PART_SECOND = Calendar.SECOND;
	public static final int DATE_PART_MILLISECOND = Calendar.MILLISECOND;


	private static final Object calendarLock = new Object();
	private static Map<String, ThreadLocal<Calendar>> calendars = new HashMap<String, ThreadLocal<Calendar>>();
	private static Calendar getCalendar(TimeZone zone, Locale local) {
		if(null == zone){
			zone = TimeZone.getTimeZone("Asia/Shanghai");
		}
		if(null == local){
			local = Locale.CHINESE;
		}
		final TimeZone _zone = zone;
		final Locale _local = local;
		String key = zone.getDisplayName() + local.getDisplayName();
		ThreadLocal<Calendar> instance = calendars.get(key);
		if (instance == null) {
			synchronized (calendarLock) {
				instance = calendars.get(key);
				if (instance == null) {
					instance = new ThreadLocal<Calendar>() {
						@Override
						protected Calendar initialValue() {
							return Calendar.getInstance(_zone,_local);
						}
					};
					calendars.put(key, instance);
				}
			}
		}
		return instance.get();
	}
	private static Calendar getCalendar() {
		return getCalendar(null,null);
	}

	/**
	 * cur是否在fr与to之间(包含fr,to)
	 * @param cur 时间
	 * @param fr 开始时间
	 * @param to 结束时间
	 * @return boolean
	 */
	public static boolean between(Date cur, Date fr, Date to) {
		if (cur.getTime() >= fr.getTime() && cur.getTime() <= to.getTime()) {
			return true;
		}
		return false;
	}

	/**
	 * cur是否在fr与to之间(包含fr,to)
	 * @param cur 时间
	 * @param fr 开始时间
	 * @param to 结束时间
	 * @return boolean
	 */
	public static boolean between(String cur, String fr, String to) {
		return between(parse(cur), parse(fr), parse(to));
	}

	/**
	 * 时间差
	 * @param part  参考Calendar
	 * @param fr  开始时间
	 * @param to  结束时间
	 * @return long
	 */
	public static long diff(int part, Date fr, Date to) {
		long result = 0;
		Calendar calendar = getCalendar();
		if (Calendar.YEAR == part) {
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.YEAR);
		}
		if (Calendar.MONTH == part) {
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
			calendar.setTime(to);
			int time = calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(fr);
			time -= calendar.get(Calendar.YEAR) * 52;
			calendar.setTime(to);
			time += calendar.get(Calendar.WEEK_OF_YEAR);
			calendar.setTime(fr);
			result = time - calendar.get(Calendar.WEEK_OF_YEAR);
		}
		long ms = to.getTime() - fr.getTime();
		if (Calendar.DAY_OF_YEAR == part || Calendar.DATE == part) {
			result = ms / 1000 / 60 / 60 / 24;
		} else if (Calendar.HOUR == part) {
			result = ms / 1000 / 60 / 60;
		} else if (Calendar.MINUTE == part) {
			result = ms / 1000 / 60;
		} else if (Calendar.SECOND == part) {
			result = ms / 1000;
		} else if (Calendar.MILLISECOND == part) {
			result = ms;
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
	 * @param locale 地区/语言,格式化月份，星期几时根据地区语言,
	 *               如MMMMM(zh:一月,en:January)MMMM(zh:一月,en:Jan) EEE(zh:星期五,en:Fri) EEEE(en:Friday)
	 * @param date  日期
	 * @param format  格式
	 * @return String
	 */
	public static String format(Locale locale, Date date, String format) {
		if (null == date || null == format)
			return "";
		return new java.text.SimpleDateFormat(format, locale).format(date);
	}

	public static String format(Locale locale, Long date, String format) {
		if (null == date || null == format)
			return "";
		return new java.text.SimpleDateFormat(format, locale).format(date);
	}

	public static String format(Locale locale) {
		return format(locale, new Date(), FORMAT_DATE_TIME);
	}

	public static String format(Locale locale, String format) {
		return format(locale, new Date(), format);
	}

	public static String format(Locale locale, Date date) {
		return format(locale, date, FORMAT_FULL);
	}

	public static String format(Locale locale, Long date) {
		return format(locale, date, FORMAT_FULL);
	}

	public static String format(Locale locale, String date, String format) {
		Date d = parse(date);
		return format(locale, d, format);
	}

	public static String format(Date date, String format) {
		return format(Locale.CHINA, date, format);
	}

	public static String format(Long date, String format) {
		return format(Locale.CHINA, date, format);
	}

	public static String format() {
		return format(Locale.CHINA);
	}

	public static String format(String format) {
		return format(Locale.CHINA, format);
	}

	public static String format(Date date) {
		return format(Locale.CHINA, date);
	}

	public static String format(Long date) {
		return format(Locale.CHINA, date);
	}

	public static String format(String date, String format) {
		return format(Locale.CHINA, date, format);
	}



	/**
	 * 时间转换成分钟
	 * @param hm  时间(10:10=610)
	 * @return int
	 */
	public static int convertMinute(String hm) {
		int minute = -1;
		if (!hm.contains(":")) {
			return minute;
		}
		String sps[] = hm.split(":");
		int h = BasicUtil.parseInt(sps[0], 0);
		int m = BasicUtil.parseInt(sps[1], 0);
		minute = h * 60 + m;
		return minute;
	}

	public static int convertMinute() {
		String hm = format("HH:mm");
		return convertMinute(hm);
	}

	/**
	 * 分钟转换成时间
	 * @param minute  分钟(610=10:10)
	 * @return String
	 */
	public static String convertMinute(int minute) {
		String time = "";
		int h = minute / 60;
		int m = minute % 60;
		if (h < 10) {
			time += "0";
		}
		time += h + ":";
		if (m < 10) {
			time += "0";
		}
		time += m;
		return time;
	}

	/**
	 * 星期几
	 * @param date  date
	 * @return return
	 */
	public static String getWeek(Date date) {
		Calendar calendar = getCalendar();
		// 再转换为时间
		calendar.setTime(date);
		// int hour=c.get(Calendar.DAY_OF_WEEK);
		// hour中存的就是星期几了，其范围 1~7
		// 1=星期日 7=星期六，其他类推
		return new SimpleDateFormat("EEEE").format(calendar.getTime());
	}


	/**
	 * 当周第一天
	 * 周日作为一周的第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_WEEK, 1);// 设为当前周的第一天
		return calendar.getTime();
	}

	public static Date getFirstDayOfWeek(String date) {
		return getFirstDayOfWeek(parse(date));
	}

	public static Date getFirstDayOfWeek() {
		return getFirstDayOfWeek(new Date());
	}

	/**
	 * 下周第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfNextWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.WEEK_OF_YEAR, 1);// 减一个周
		calendar.set(Calendar.DAY_OF_WEEK, 1);// 把日期设置为当周第一天
		return calendar.getTime();
	}

	public static Date getFirstDayOfNextWeek(String date) {
		return getFirstDayOfNextWeek(parse(date));
	}

	public static Date getFirstDayOfNextWeek() {
		return getFirstDayOfNextWeek(new Date());
	}


	/**
	 * 上个周第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfPreviousWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.WEEK_OF_YEAR, -1);// 减一个周
		calendar.set(Calendar.DAY_OF_WEEK, 1);// 设为当前周第一天
		return calendar.getTime();
	}

	public static Date getFirstDayOfPreviousWeek(String date) {
		return getFirstDayOfPreviousWeek(parse(date));
	}

	public static Date getFirstDayOfPreviousWeek() {
		return getFirstDayOfPreviousWeek(new Date());
	}

	/**
	 * 当周最后天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_WEEK, 7);
		return calendar.getTime();
	}

	public static Date getLastDayOfWeek(String date) {
		return getLastDayOfWeek(parse(date));
	}

	public static Date getLastDayOfWeek() {
		return getLastDayOfWeek(new Date());
	}

	/**
	 * 下周最后天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfNextWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.WEEK_OF_YEAR, 1);// 减一个周
		calendar.set(Calendar.DAY_OF_WEEK, 7);
		return calendar.getTime();
	}

	public static Date getLastDayOfNextWeek(String date) {
		return getLastDayOfNextWeek(parse(date));
	}

	public static Date getLastDayOfNextWeek() {
		return getLastDayOfNextWeek(new Date());
	}


	/**
	 * 上个周最后天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfPreviousWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.WEEK_OF_YEAR, -1);// 减一个周
		calendar.set(Calendar.DAY_OF_WEEK, 7);
		return calendar.getTime();
	}

	public static Date getLastDayOfPreviousWeek(String date) {
		return getLastDayOfPreviousWeek(parse(date));
	}

	public static Date getLastDayOfPreviousWeek() {
		return getLastDayOfPreviousWeek(new Date());
	}

	/**
	 * 当月第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设为当前月的1号
		return calendar.getTime();
	}

	public static Date getFirstDayOfMonth(String date) {
		return getFirstDayOfMonth(parse(date));
	}

	public static Date getFirstDayOfMonth() {
		return getFirstDayOfMonth(new Date());
	}

	/**
	 * 下个月第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfNextMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		return calendar.getTime();
	}

	public static Date getFirstDayOfNextMonth(String date) {
		return getFirstDayOfNextMonth(parse(date));
	}

	public static Date getFirstDayOfNextMonth() {
		return getFirstDayOfNextMonth(new Date());
	}

	/**
	 * 上个月第一天
	 * @param date  date
	 * @return return
	 */
	public static Date getFirstDayOfPreviousMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DATE, 1);// 设为当前月的1号
		calendar.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		return calendar.getTime();
	}

	public static Date getFirstDayOfPreviousMonth(String date) {
		return getFirstDayOfPreviousMonth(parse(date));
	}

	public static Date getFirstDayOfPreviousMonth() {
		return getFirstDayOfPreviousMonth(new Date());
	}

	/**
	 * 当月最后一天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTimeInMillis(date.getTime() + 100000);
		calendar.set(Calendar.DAY_OF_MONTH, 1);// 设为当前月的1号
		calendar.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		calendar.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfMonth(String date) {
		return getLastDayOfMonth(parse(date));
	}

	public static Date getLastDayOfMonth() {
		return getLastDayOfMonth(new Date());
	}

	/**
	 * 上月最后一天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfPreviousMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);// 减一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfPreviousMonth(String date) {
		return getLastDayOfPreviousMonth(parse(date));
	}

	public static Date getLastDayOfPreviousMonth() {
		return getLastDayOfPreviousMonth(new Date());
	}

	/**
	 * 下月最后一天
	 * @param date  date
	 * @return return
	 */
	public static Date getLastDayOfNextMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);// 加一个月
		calendar.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		calendar.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		return calendar.getTime();
	}

	public static Date getLastDayOfNextMonth(String date) {
		return getLastDayOfNextMonth(parse(date));
	}

	public static Date getLastDayOfNextMonth() {
		return getLastDayOfNextMonth(new Date());
	}

	// 获得本周星期日的日期
	public static Date getCurrentWeekday(Date date) {
		Calendar calendar = getCalendar();
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 6);
		return calendar.getTime();
	}

	public static Date getCurrentWeekday(String date) {
		return getCurrentWeekday(parse(date));
	}

	public static Date getCurrentWeekday() {
		return getCurrentWeekday(new Date());
	}

	// 获得当前日期与本周日相差的天数
	public static int getMondayPlus(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}

	public static int getMondayPlus() {
		return getMondayPlus(new Date());
	}

	// 获得本周一的日期
	public static Date getMondayOFWeek(Date date) {
		Calendar calendar = getCalendar();
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus);
		return calendar.getTime();
	}

	public static Date getMondayOFWeek(String date) {
		return getMondayOFWeek(parse(date));
	}

	public static Date getMondayOFWeek() {
		return getMondayOFWeek(new Date());
	}

	// 获得下周星期一的日期
	public static Date getNextMonday(Date date) {
		Calendar calendar = getCalendar();
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 7);
		return calendar.getTime();
	}

	public static Date getNextMonday(String date) {
		return getNextMonday(parse(date));
	}

	public static Date getNextMonday() {
		return getNextMonday(new Date());
	}

	// 获得下周星期日的日期
	public static Date getNextSunday(Date date) {
		Calendar calendar = getCalendar();
		int mondayPlus = getMondayPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, mondayPlus + 7 + 6);
		return calendar.getTime();
	}

	public static Date getNextSunday(String date) {
		return getNextSunday(parse(date));
	}

	public static Date getNextSunday() {
		return getNextSunday(new Date());
	}

	// 当前日期与本周日相差几天
	public static int getMonthPlus(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
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
	public static Date getNextYearEnd(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, 1);// 加一个年
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime();
	}

	public static Date getNextYearEnd(String date) {
		return getNextYearEnd(parse(date));
	}

	public static Date getNextYearEnd() {
		return getNextYearEnd(new Date());
	}

	// 获得明年第一天的日期
	public static Date getNextYearFirst(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, 1);// 加一个年
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		return calendar.getTime();
	}

	public static Date getNextYearFirst(String date) {
		return getNextYearFirst(parse(date));
	}

	public static Date getNextYearFirst() {
		return getNextYearFirst(new Date());
	}

	/**
	 * 一年多少天
	 * @param date date
	 * @return return
	 */
	public static int countDaysOfYear(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		calendar.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		return calendar.get(Calendar.DAY_OF_YEAR);
	}

	public static int countDaysOfYear(int year) {
		Date date = parse(year+"-01-01");
		return countDaysOfYear(date);
	}
	public static int countDaysOfYear() {
		return countDaysOfYear(new Date());
	}

	/**
	 * 一年多少天
	 * @param date date
	 * @return return
	 */
	public static int countDaysOfMonth(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1); // 把时间调整为当月的第一天；
		calendar.add(Calendar.MONTH,1); // 月份调至下个月；
		calendar.add(Calendar.DAY_OF_MONTH, -1); // 时间减去一天（就等于上个月的最后一天）
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public static int countDaysOfMonth(String ym) {
		Date date = parse(ym+"-01");
		return countDaysOfMonth(date);
	}
	public static int countDaysOfMonth() {
		return countDaysOfMonth(new Date());
	}

	private static int getYearPlus(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
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
	public static Date getFirstDayOfYear(Date date) {
		Calendar calendar = getCalendar();
		int yearPlus = getYearPlus(date);
		calendar.setTime(date);
		calendar.add(Calendar.DATE, yearPlus);
		return calendar.getTime();
	}

	public static Date getFirstDayOfYear(String date) {
		return getFirstDayOfYear(parse(date));
	}

	public static Date getFirstDayOfYear() {
		return getFirstDayOfYear(new Date());
	}

	// 获得本年最后一天的日期 *
	public static String getCurrentYearEnd(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	public static String getCurrentYearEnd(String date) {
		return getCurrentYearEnd(parse(date));
	}

	public static String getCurrentYearEnd() {
		return getCurrentYearEnd(new Date());
	}

	// 获得上年第一天的日期 *
	public static String getPreviousYearFirst(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-01-01";
	}

	public static String getPreviousYearFirst(String date) {
		return getPreviousYearFirst(parse(date));
	}

	public static String getPreviousYearFirst() {
		return getPreviousYearFirst(new Date());
	}

	/**
	 * 获取某年某月的最后一天
	 *
	 * @param year 年
	 * @param month   月
	 * @return 最后一天
	 */
	public static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8
				|| month == 10 || month == 12) {
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
	 * @param year 年
	 * @return return
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * 转换成日期
	 *
	 * @param date  date
	 * @param format  format
	 * @return return
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
	 * @param str  str
	 * @return return
	 */
	public static Date parse(String str) {
		if (BasicUtil.isEmpty(str)) {
			return null;
		}
		str = str.trim();
		if (str.length() <= 5
				&& !RegularUtil.match(str, Regular.PATTERN.DATE_TIME.getCode(),
				Regular.MATCH_MODE.MATCH)) {
			return null;
		}
		Date date = null;
		String format = FORMAT_FULL;
		if (!str.contains(".")) {
			// 不带毫秒
			format = FORMAT_DATE_TIME;
		}

		if (!str.contains(":")) {
			// 不带时间
			format = FORMAT_DATE;
		} else if (!str.contains(" ")) {
			// 不带日期
			format = format.replace("yyyy-MM-dd ", "");
		}
		if (BasicUtil.catSubCharCount(str, ":") == 1) {
			// 只有时分 没有秒
			format = format.replace(":ss", "");
		}

		if (str.contains("/")) {
			format = format.replace("-", "/");
		}
		if (!str.contains("-") && !str.contains("/")) {
			format = format.replace("-", "").replace("/", "");
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			date = sdf.parse(str);
		} catch (Exception e) {
			try {
				sdf = new SimpleDateFormat();
				date = sdf.parse(str);
			} catch (Exception excep) {
				date = null;
			}
		}
		return date;
	}

	public static boolean isDate(String str) {
		return parse(str) != null;
	}

	/**
	 * 昨天
	 *
	 * @return return
	 */
	public static Date yesterday() {
		return addDay(-1);
	}

	/**
	 * 明天
	 *
	 * @return return
	 */
	public static Date tomorrow() {
		return addDay(1);
	}

	/**
	 * 现在
	 *
	 * @return return
	 */
	public static Date now() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * 按日加
	 *
	 * @param value  value
	 * @return return
	 */
	public static Date addDay(int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 按日加,指定日期
	 *
	 * @param date  date
	 * @param value  value
	 * @return return
	 */
	public static Date addDay(Date date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return calendar.getTime();
	}
	public static String addDay(String date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(parse(date));
		calendar.add(Calendar.DAY_OF_YEAR, value);
		return DateUtil.format(calendar.getTime(),"yyyy-MM-dd");
	}

	/**
	 * 按月加
	 *
	 * @param value  value
	 * @return return
	 */
	public static Date addMonth(int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}

	/**
	 * 按月加,指定日期
	 *
	 * @param date  date
	 * @param value  value
	 * @return return
	 */
	public static Date addMonth(Date date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, value);
		return calendar.getTime();
	}
	public static Date addMonth(String date, int value) {
		return addMonth(parse(date), value);
	}

	/**
	 * 按年加
	 *
	 * @param value  value
	 * @return return
	 */
	public static Date addYear(int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}

	/**
	 * 当前日期所在周的第idx天 第1天：星期日 第7天：星期六
	 *
	 * @param idx  idx
	 * @param date  date
	 * @return return
	 */
	public static Date getDateOfWeek(int idx, Date date) {
		Date result = null;
		Calendar cal = getCalendar();
		cal.setTime(date);
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - idx;
		cal.add(Calendar.DATE, -day_of_week);
		result = cal.getTime();
		return result;
	}

	public static Date getDateOfWeek(int idx) {
		return getDateOfWeek(idx, new Date());
	}

	/**
	 * 星期几(礼拜几)
	 * @param date date
	 * @return return
	 */
	public static int getDayOfWeek(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK) - 1;
	}

	public static int getDayOfWeek() {
		return getDayOfWeek(new Date());
	}
	public static int getDayOfWeek(String date){
		return getDayOfWeek(parse(date));
	}
	/**
	 * 区间日期
	 * @param fr  fr
	 * @param to  to
	 * @return return
	 */
	public static List<Date> getDays(Date fr, Date to){
		List<Date> list = new ArrayList<Date>();
		list.add(fr);
		while(true){
			fr = DateUtil.addDay(fr, 1);
			if(DateUtil.diff(DATE_PART_DATE, fr, to) >0){
				break;
			}
			list.add(fr);
		}
		return list;
	}
	public static List<Date> getDaysOfYear(int year){
		return getDaysOfYear(parse(year+"-01-01"));
	}
	public static List<Date> getDaysOfYear(String year){
		String ymd = year + "-01-01";
		if(year.length()>4){
			ymd = year;
		}
		return getDaysOfYear(parse(ymd));
	}
	public static List<Date> getDaysOfYear(Date date){
		List<Date> list = new ArrayList<Date>();
		Date start = getFirstDayOfYear(date);
		int qty = countDaysOfYear(date);
		for(int i=0; i<qty; i++){
			list.add(addDay(start, i));
		}
		return list;
	}
	public static List<Date> getDaysOfMonth(String ym){
		String ymd = ym+"-01";
		if(ym.length()>7){
			ymd = ym;
		}
		return getDaysOfMonth(parse(ymd));
	}
	public static List<Date> getDaysOfMonth(Date ym){
		List<Date> list = new ArrayList<Date>();
		Date start = getFirstDayOfMonth(ym);
		int qty = countDaysOfMonth(ym);
		for(int i=0; i<qty; i++){
			Date date = addDay(start, i);
			list.add(date);
		}
		return list;
	}

	public static List<Date> getDaysOfWeek(int year, int week){
		Calendar calendar = getCalendar();
		List<Date> list = new ArrayList<Date>();
		calendar.setTime(new Date());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, week);
		calendar.set(Calendar.DAY_OF_WEEK, 1);
		Date start = getFirstDayOfNextWeek(calendar.getTime());
		for(int i=0; i<7; i++){
			Date date = addDay(start, i);
			list.add(date);
		}
		return list;
	}
	public static List<Date> getDaysOfWeek(String date){
		return getDaysOfWeek(parse(date));
	}
	public static List<Date> getDaysOfWeek(Date date){
		List<Date> list = new ArrayList<Date>();
		Date start = getFirstDayOfWeek(date);
		for(int i=0; i<7; i++){
			list.add(addDay(start, i));
		}
		return list;
	}

	public List<String> getDays(String fr, String to){
		List<String> list = new ArrayList<String>();
		list.add(fr);
		while(true){
			fr = DateUtil.addDay(fr, 1);
			if(DateUtil.diff(DATE_PART_DATE, fr, to) >0){
				break;
			}
			list.add(fr);
		}
		return list;
	}
	public static String max(String ... dates){
		String result = null;
		if(null != dates){
			for(String date:dates){
				if(null == result || diff(DATE_PART_SECOND, result, date) >0){
					result = date;
				}
			}
		}
		return result;
	}
	public static String min(String ... dates){
		String result = null;
		if(null != dates){
			for(String date:dates){
				if(null == result || diff(DATE_PART_SECOND, result, date) <0){
					result = date;
				}
			}
		}
		return result;
	}
	public static Date max(Date ... dates){
		Date result = null;
		if(null != dates){
			for(Date date:dates){
				if(null == result || diff(DATE_PART_SECOND, result, date) >0){
					result = date;
				}
			}
		}
		return result;
	}
	public static Date min(Date ... dates){
		Date result = null;
		if(null != dates){
			for(Date date:dates){
				if(null == result || diff(DATE_PART_SECOND, result, date) <0){
					result = date;
				}
			}
		}
		return result;
	}
	/**
	 * 一年中的第几个星期
	 * @param date date
	 * @return return
	 */
	public static int getWeekOfYear(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	public static int getWeekOfYear() {
		return getWeekOfYear(new Date());
	}

	/**
	 * 按年加,指定日期
	 *
	 * @param date  date
	 * @param value  value
	 * @return return
	 */
	public static Date addYear(Date date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, value);
		return calendar.getTime();
	}
	public static Date addYear(String date, int value) {
		return addYear(parse(date), value);
	}

	/**
	 * 按小时加
	 *
	 * @param value  value
	 * @return return
	 */
	public static Date addHour(int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}

	/**
	 * 按小时加,指定日期
	 *
	 * @param date  date
	 * @param value  value
	 * @return return
	 */
	public static Date addHour(Date date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, value);
		return calendar.getTime();
	}
	public static Date addHour(String date, int value) {
		return addHour(parse(date), value);
	}

	/**
	 * 按分钟加
	 *
	 * @param value  value
	 * @return return
	 */
	public static Date addMinute(int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}

	/**
	 * 按分钟加,指定日期
	 *
	 * @param date  date
	 * @param value  value
	 * @return return
	 */
	public static Date addMinute(Date date, int value) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, value);
		return calendar.getTime();
	}
	public static Date addMinute(String date, int value) {
		return addMinute(parse(date), value);
	}

	/**
	 * 年份
	 * @param date date
	 * @return return
	 */
	public static int year(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static int year() {
		return year(new Date());
	}

	/**
	 * 月份
	 * @param date date
	 * @return return
	 */
	public static int month(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH)+1;
	}

	public static int month() {
		return month(new Date());
	}
	/**
	 * 星期几(礼拜几)
	 * @param date date
	 * @return return
	 */
	public static int dayOfWeek(Date date) {
		return getDayOfWeek(date);
	}

	public static int dayOfWeek() {
		return getDayOfWeek();
	}
	public static int dayOfWeek(String date){
		return getDayOfWeek(date);
	}
	/**
	 * 日(号)
	 * @param date date
	 * @return return
	 */
	public static int dayOfMonth(Date date) {
		return getDayOfMonth(date);
	}
	public static int dayOfMonth(String date){
		return getDayOfMonth(date);
	}

	public static int dayOfMonth() {
		return getDayOfMonth();
	}
	public static int getDayOfMonth(Date date){
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public static int getDayOfMonth(String date){
		return getDayOfMonth(parse(date));
	}

	public static int getDayOfMonth(){
		return getDayOfMonth(new Date());
	}


	public static int day(Date date) {
		return dayOfMonth(date);
	}

	public static int day() {
		return day(new Date());
	}

	public static int dayOfYear(Date date) {
		return getDayOfYear(date);
	}

	public static int dayOfYear(String date) {
		return getDayOfYear(date);
	}

	public static int dayOfYear() {
		return getDayOfYear();
	}

	public static int getDayOfYear(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_YEAR);
	}

	public static int getDayOfYear(String date) {
		return getDayOfYear(parse(date));
	}


	public static int getDayOfYear() {
		return dayOfYear(new Date());
	}

	/**
	 * 小时(点)
	 * @param date date
	 * @return return
	 */
	public static int hour(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR);
	}

	public static int hour() {
		return hour(new Date());
	}

	/**
	 * 分钟
	 * @param date date
	 * @return return
	 */
	public static int minute(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	public static int minute() {
		return minute(new Date());
	}

	/**
	 * 秒
	 * @param date date
	 * @return return
	 */
	public static int second(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.SECOND);
	}

	public static int second() {
		return second(new Date());
	}

	/**
	 * fr 大于  to返回 1
	 *
	 * @param fr  fr
	 * @param to  to
	 * @return return
	 */
	public static int compare(Date fr, Date to) {
		int result = 0;
		if (fr.getTime() > to.getTime()) {
			result = 1;
		} else {
			result = -1;
		}
		return result;
	}

	public static int compare(String fr, String to) {
		return compare(parse(fr), parse(to));
	}

	/**
	 * 是上午吗?
	 * @param date date
	 * @return return
	 */
	public static boolean isAm(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.AM_PM) == 0;
	}

	public static boolean isAm() {
		return isAm(new Date());
	}

	/**
	 * 是下午吗?
	 * @param date date
	 * @return return
	 */
	public static boolean isPm(Date date) {
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.AM_PM) == 1;
	}

	public static boolean isPm() {
		return isPm(new Date());
	}

	public static String conversion(double src){
		return conversion((long)src);
	}
	/**
	 *
	 * @param src  src
	 * @return return
	 */
	public static String conversion(long src){
		String result = "";
		long s = 0;
		long m = 0;
		long h = 0;
		long d = 0;
		long ms = 0;
		d = src / 1000 / 60 / 60 /24;
		h = (src - d*24*60*60*1000) / 1000 /60/60;
		m = (src - d*24*60*60*1000 - h*60*60*1000) / 1000 /60;
		s = (src - d*24*60*60*1000 - h*60*60*1000 - m*60*1000) / 1000;
		ms = src %1000;
		if(d>0){
			result += d+"天";
		}
		if(h>0
				|| (d>0 && (m+s+ms>0))
		){
			result += h+"时";
		}
		if(m>0
				|| (h>0 && (s+ms>0))
				|| (d>0  && (s+ms>0))
		){
			result += m+"分";
		}
		if(s>0
				||(m>0&&ms>0)
				||(h>0&&ms>0)
				||(d>0&&ms>0)
		){
			if(ms==0){
				result += s+"秒";
			}else{
				result += s+"."+BasicUtil.fillChar(ms+"",3)+"秒";
			}
		}
		if(src<1000){
			if(ms>0){
				result += ms+"毫秒";
			}
		}
		if(src<=0){
			result = "0毫秒";
		}
		return result;
	}
}
