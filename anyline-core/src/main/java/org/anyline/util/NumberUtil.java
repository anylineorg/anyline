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
import java.util.ArrayList;
import java.util.List;
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
	public static double max(double... nums) {
		double max = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
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
	public static double min(double... nums) {
		double min = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (min > nums[i]) {
				min = nums[i];
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
	public static int max(int... nums) {
		int max = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
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
	public static int min(int... nums) {
		int min = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (min > nums[i]) {
				min = nums[i];
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
	public static long max(long... nums) {
		long max = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
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
	public static long min(long... nums) {
		long min = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (min > nums[i]) {
				min = nums[i];
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
	public static float max(float... nums) {
		float max = nums[0];
		int size = nums.length;
		for (int i = 0; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
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
	public static float min(float... nums) {
		float min = nums[0];
		int size = nums.length;
		for (int i = 1; i < size; i++) {
			if (min > nums[i]) {
				min = nums[i];
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
	public static short max(short... nums) {
		short max = nums[0];
		int size = nums.length;
		for (int i = 1; i < size; i++) {
			if (max < nums[i]) {
				max = nums[i];
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
	public static short min(short... nums) {
		short min = nums[0];
		int size = nums.length;
		for (int i = 1; i < size; i++) {
			if (min > nums[i]) {
				min = nums[i];
			}
		}
		return min;
	}
	public static int random(int fr, int to) {
		int result = 0;
		Random r = new Random();
		result = fr + r.nextInt(to - fr);
		return result;
	}
	public static double random(double fr, double to) {
		double result = 0;
		Random r = new Random();
		result = fr + r.nextDouble() * (to - fr);
		return result;
	}
	public static long random(long fr, long to) {
		long result = 0;
		Random r = new Random();
		result = fr + r.nextLong() * (to - fr);
		return result;
	}
	public static float random(float fr, float to) {
		float result = 0;
		Random r = new Random();
		result = fr + r.nextFloat() * (to - fr);
		return result;
	}

	public static List<Integer> random(int fr, int to, int qty) {
		List<Integer> list = new ArrayList<Integer>();
		Random r = new Random();
		while(true){
			int rdm = fr + r.nextInt(to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Double> random(double fr, double to, int qty) {
		List<Double> list = new ArrayList<Double>();
		Random r = new Random();
		while(true){
			double rdm = fr + r.nextDouble() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Float> random(float fr, float to, int qty) {
		List<Float> list = new ArrayList<Float>();
		Random r = new Random();
		while(true){
			float rdm = fr + r.nextFloat() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	public static List<Long> random(long fr, long to, int qty) {
		List<Long> list = new ArrayList<Long>();
		Random r = new Random();
		while(true){
			long rdm = fr + r.nextLong() * (to - fr);
			if(list.contains(rdm)){
				continue;
			}
			list.add(rdm);
			if(list.size() == qty){
				break;
			}
		}
		return list;
	}
	
	public static boolean isInt(double src){
		return src == (int)src;
	}
	public static boolean isInt(float src){
		return src == (int)src;
	}
}
