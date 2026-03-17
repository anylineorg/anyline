/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.web.tag;
 
 
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
 
/**
 * 从value|body起到现在经过多少时间
 * @author zh
 *
 */ 
public class TimeDelay extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L; 
	private Object nvl = false;	// 如果value为空("",null) 是否显示当时间,默认false
	private String precision = null;
	private Object deadline = null;
 
	public int doEndTag() throws JspException {
		try{
			String result = ""; 
			if(null == value) {
				value = body; 
			} 
			if(BasicUtil.isEmpty(value) && BasicUtil.parseBoolean(nvl)) {
				value = new Date(); 
			}
			if(null != value) {
				Date date = DateUtil.parse(value);
				long fr = date.getTime();
				long to = 0L;
				if(null != deadline){
					to = DateUtil.parse(deadline).getTime();
				}else {
					to = new Date().getTime();
				}
				long dif = to - fr;
				int p =  1;
				if(BasicUtil.isEmpty(precision)){
					p = precision(dif);
				}else{
					p = parsePrecision(precision);
				}
				result = convertMilliseconds(dif, p);
				JspWriter out = pageContext.getOut();
				out.print(result);
			}
		}catch(Exception e) {
			e.printStackTrace(); 
		}finally{
			release(); 
		} 
        return EVAL_PAGE;    
	}
	public static int parsePrecision(String precisionStr) {
		switch (precisionStr) {
			case "1": case "秒": return 1;
			case "2": case "分钟":  case "分": return 2;
			case "3": case "小时": case "时": return 3;
			case "4": case "天": return 4;
			case "5": case "月": return 5;
			case "6": case "年": return 6;
			default: return -1;
		}
	}
	public static int precision(long milliseconds){
		// 定义各时间单位对应的毫秒数阈值
		long secondThreshold = 60 * 60 * 1000L;           // 60分钟
		long minuteThreshold = 24 * 60 * 60 * 1000L;      // 24小时
		long hourThreshold = 7 * 24 * 60 * 60 * 1000L;    // 7天
		long dayThreshold = 30 * 24 * 60 * 60 * 1000L;    // 30天(约1个月)
		long monthThreshold = 365 * 24 * 60 * 60 * 1000L; // 365天(约1年)

		// 根据毫秒数大小决定显示精度
		if (milliseconds < secondThreshold) {
			// 小于1分钟，显示到秒
			return 1;
		} else if (milliseconds < minuteThreshold) {
			// 小于1小时，显示到分钟
			return 2;
		} else if (milliseconds < hourThreshold) {
			// 小于7天，显示到小时
			return 3;
		} else if (milliseconds < dayThreshold) {
			// 小于30天，显示到天
			return 4;
		} else if (milliseconds < monthThreshold) {
			// 小于1年，显示到天
			return 4;
		} else {
			// 大于等于1年，显示到天
			return 4;
		}
	}
	/**
	 * 将毫秒转换为带单位的时间字符串
	 *
	 * @param milliseconds 毫秒数
	 * @param precision 显示精度等级
	 * @return 格式化后的字符串
	 */
	public static String convertMilliseconds(long milliseconds, int precision) {
		// 基本时间单位换算
		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long months = days / 30;  // 简化计算，每月按30天计算
		long years = days / 365;  // 简化计算，每年按365天计算

		// 计算余数部分
		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 30;
		months %= 12;

		StringBuilder sb = new StringBuilder();

		// 根据精度显示相应的时间单位(只显示指定精度及以上的单位)
		switch (precision) {
			case 6: // 年 (只显示年)
				sb.append(years).append("年");
				break;
			case 5: // 月 (显示年月)
				if (years > 0) sb.append(years).append("年");
				sb.append(months).append("月");
				break;
			case 4: // 天 (显示年月天)
				if (years > 0) sb.append(years).append("年");
				if (months > 0) sb.append(months).append("月");
				sb.append(days).append("天");
				break;
			case 3: // 小时 (显示年月天小时)
				if (years > 0) sb.append(years).append("年");
				if (months > 0) sb.append(months).append("月");
				if (days > 0) sb.append(days).append("天");
				sb.append(hours).append("小时");
				break;
			case 2: // 分钟 (显示年月天小时分钟)
				if (years > 0) sb.append(years).append("年");
				if (months > 0) sb.append(months).append("月");
				if (days > 0) sb.append(days).append("天");
				if (hours > 0) sb.append(hours).append("小时");
				sb.append(minutes).append("分钟");
				break;
			case 1: // 秒 (显示年月天小时分钟秒)
				if (years > 0) sb.append(years).append("年");
				if (months > 0) sb.append(months).append("月");
				if (days > 0) sb.append(days).append("天");
				if (hours > 0) sb.append(hours).append("小时");
				if (minutes > 0) sb.append(minutes).append("分钟");
				sb.append(seconds).append("秒");
				break;
			default:
				return "无效的精度参数";
		}

		return sb.toString().trim();
	}
 
	@Override 
	public void release() {
		super.release();
		this.value = null;
		this.body = null;
		this.precision = null;
		this.deadline = null;
		this.nvl = false;
	}

	public Object getDeadline() {
		return deadline;
	}

	public void setDeadline(Object deadline) {
		this.deadline = deadline;
	}

	public String getPrecision() {
		return precision;
	}

	public void setPrecision(String precision) {
		this.precision = precision;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone(); 
	} 
}
