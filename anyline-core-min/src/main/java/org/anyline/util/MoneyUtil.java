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
 
public class MoneyUtil { 
	public static String format(double n){ 
		String fraction[] = { "角", "分" }; 
		String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" }; 
		String unit[][] = { { "元", "万", "亿" }, { "", "拾", "佰", "仟" } }; 
 
		String head = n < 0 ? "负" : ""; 
		n = Math.abs(n); 
 
		String s = ""; 
		for (int i = 0; i < fraction.length; i++) { 
			s += (digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]) 
					.replaceAll("(零.)+", ""); 
		} 
		if (s.length() < 1) { 
			s = "整"; 
		} 
		int integerPart = (int) Math.floor(n); 
 
		for (int i = 0; i < unit[0].length && integerPart > 0; i++) { 
			String p = ""; 
			for (int j = 0; j < unit[1].length && n > 0; j++) { 
				p = digit[integerPart % 10] + unit[1][j] + p; 
				integerPart = integerPart / 10; 
			} 
			s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] 
					+ s; 
		} 
		return head 
				+ s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "") 
						.replaceAll("(零.)+", "零").replaceAll("^整$", "零元整"); 
	} 
} 
