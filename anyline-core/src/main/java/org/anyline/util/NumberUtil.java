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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;


public class NumberUtil {
	/**
	 * 数据格式化
	 * 
	 * @param src
	 * @param pattern
	 * @return
	 */
	public static String format(String src, String pattern) {
		if (BasicUtil.isEmpty(src) || BasicUtil.isEmpty(src.trim())) {
			return "";
		}
		BigDecimal num = null;
		try{
			num = new BigDecimal(src);
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
		return format(num, pattern);
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
	/**
	 * 中文大写(简体)
	 * @param num
	 * @return
	 */
	public static String upper(long num){
		return NumberTextUtil.getInstance(NumberTextUtil.Lang.ChineseSimplified).getText(num);
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
	public static int getRandom(int fr, int to) {
		int result = 0;
		Random r = new Random();
		result = fr + r.nextInt(to - fr);
		return result;
	}
	public static double getRandom(double fr, double to) {
		double result = 0;
		Random r = new Random();
		result = fr + r.nextDouble() * (to - fr);
		return result;
	}

}
