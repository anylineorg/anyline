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
	
}
