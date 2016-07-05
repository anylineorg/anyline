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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;


public class NumberUtil {
	/**
	 * 数据格式化
	 * 
	 * @param src
	 * @param pattern
	 * @return
	 */
	public static String format(String src, String pattern) {
		if (null == src) {
			return "";
		}
		return format(new BigDecimal(src), pattern);
	}

	/**
	 * 数字格式化
	 * 
	 * @param src
	 * @param pattern
	 * @return
	 */
	public static String format(Number src, String pattern) {
		if (null == src) {
			return "";
		}
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(src);
	}
	public static String toUpper(String num){
		return "";
	}

	/**
	 * 选取最大数
	 * 
	 * @param num
	 * @return
	 */
	public static double getMax(double num, double... nums) {
		double max = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (max < nums[i]) {
					max = nums[i];
				}
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 * 
	 * @param num
	 * @return
	 */
	public static double getMin(double num, double... nums) {
		double min = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (min > nums[i]) {
					min = nums[i];
				}
			}
		}
		return min;
	}

	/**
	 * 选取最大数
	 * 
	 * @param num
	 * @return
	 */
	public static int getMax(int num, int... nums) {
		int max = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (max < nums[i]) {
					max = nums[i];
				}
			}
		}
		return max;
	}

	/**
	 * 选取最小数
	 * 
	 * @param num
	 * @return
	 */
	public static int getMin(int num, int... nums) {
		int min = num;
		if (null != nums) {
			int size = nums.length;
			for (int i = 0; i < size; i++) {
				if (min > nums[i]) {
					min = nums[i];
				}
			}
		}
		return min;
	}

}
