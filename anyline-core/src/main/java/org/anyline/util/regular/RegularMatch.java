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


package org.anyline.util.regular;

import org.anyline.util.ConfigTable;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 完全匹配模式
 *
 */
public class RegularMatch implements Regular{
	private static final Log log = LogProxy.get(RegularMatch.class);

	/**
	 * 匹配状态
	 * @param src  src
	 * @param regx  regx
	 * @return boolean
	 */
	public boolean match(String src, String regx) {
		boolean result = false;
		try{
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(src);
			result = matcher.matches();
		}catch(Exception e) {
			result = false;
			log.error("[match error][src:{}][regx:{}]", src, regx);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 提取子串
	 * @param src	输入字符串  src	输入字符串
	 * @param regx	表达式  regx	表达式
	 * @return List
	 */
	public List<List<String>> fetchs(String src, String regx) {
		List<List<String>> list = new ArrayList<List<String>>();
		try{
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(src);
			if(matcher.matches()) {
				List<String> item = new ArrayList<>();
				for(int i=0; i<=matcher.groupCount(); i++) {
					item.add(matcher.group(i));
				}
				list.add(item);
			}
		}catch(Exception e) {
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
				e.printStackTrace();
			}
		}
		return list;
	}
	/**
	 * 提取子串
	 * @param src		输入字符串  src		输入字符串
	 * @param regx		表达式  regx	表达式
	 * @param idx		指定提取位置  idx	指定提取位置
	 * @return List
	 */
	public List<String> fetch(String src, String regx, int idx) {
		List<String> list = new ArrayList<>();
		try{
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(src);
			if(matcher.matches()) {
				list.add(matcher.group(idx));
			}
		}catch(Exception e) {
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
				e.printStackTrace();
			}
		}
		return list;
	}
	public List<String> fetch(String src, String regx) throws Exception {
		return fetch(src, regx, 0);
	}

	/**
	 * 过滤 保留匹配项
	 * @param src  src
	 * @param regx  regx
	 * @return List
	 */
	public List<String> pick(List<String> src, String regx) {
		List<String> list = new ArrayList<>();
		for(String item : src) {
			if(match(item, regx)) {
				list.add(item);
			}
		}
		return list;
	}
	/**
	 * 过滤 删除匹配项
	 * @param src  src
	 * @param regx  regx
	 * @return List
	 */
	public List<String> wipe(List<String> src, String regx) {
		List<String> list = new ArrayList<>();
		for(String item : src) {
			if(!match(item, regx)) {
				list.add(item);
			}
		}
		return list;
	}
}