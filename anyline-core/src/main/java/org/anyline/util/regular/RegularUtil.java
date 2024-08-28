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

package org.anyline.util.regular;

import org.apache.oro.text.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegularUtil {
	private static Regular regular;
	public static Regular regularMatch 		= new RegularMatch();			// 完全匹配模式
	public static Regular regularMatchPrefix 	= new RegularMatchPrefix();		// 前缀匹配模式
	public static Regular regularContain 		= new RegularContain();			// 包含匹配模式

	private static final Map<Regular.MATCH_MODE, Regular> regularList = new HashMap<Regular.MATCH_MODE, Regular>();
	public static final String REGEX_VARIABLE = "${(\\w+)}";		// 变量{ID}

	public static final String TAG_BEGIN = "${begin}";
	public static final String TAG_END = "${end}";

	private static final Logger log = LoggerFactory.getLogger(RegularUtil.class);
	static{
		regularList.put(Regular.MATCH_MODE.MATCH, regularMatch);
		regularList.put(Regular.MATCH_MODE.PREFIX, regularMatchPrefix);
		regularList.put(Regular.MATCH_MODE.CONTAIN, regularContain);
	}
	public static synchronized boolean match(String src, String regx, Regular.MATCH_MODE mode) {
		boolean result = false;
		if(null == src || null == regx ) {
			return result;
		}
		regular = regularList.get(mode);
		try{
			result = regular.match(src, regx);
		}catch(Exception e) {
			log.warn("[match(src, regx, mode) error][src:{}][regx:{}][mode:{}]", src, regx, mode);
			e.printStackTrace();
		}
		return result;
	}
	public static boolean match(String src, String regx) {
		return match(src, regx, Regular.MATCH_MODE.CONTAIN);
	}

	/**
	 * 提取子串
	 * @param src	输入字符串  src	输入字符串
	 * @param regx	表达式  regx	表达式
	 * @param mode	mode
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static synchronized List<List<String>> fetchs(String src, String regx, Regular.MATCH_MODE mode) throws Exception {
		List<List<String>> result = null;
		if(null != src) {
			regular = regularList.get(mode);
			result = regular.fetchs(src, regx);
		}
		return result;
	}
	public static List<List<String>> fetchs(String src, String regx) throws Exception {
		return fetchs(src, regx, Regular.MATCH_MODE.CONTAIN);
	}
	public static List<String> fetch(String src, String regx) throws Exception {
		return fetch(src, regx, Regular.MATCH_MODE.CONTAIN);
	}
	public static synchronized List<String> fetch(String src, String regx, Regular.MATCH_MODE mode, int idx) throws Exception {
		List<String> result = null;
		if(null != src) {
			regular = regularList.get(mode);
			result = regular.fetch(src, regx, idx);
		}
		return result;
	}
	public static synchronized List<String> fetch(String src, String regx, Regular.MATCH_MODE mode) throws Exception {
		List<String> result = null;
		if(null != src){
		regular = regularList.get(mode);
		result = regular.fetch(src, regx);
		}
		return result;
	}
	public static List<String> filter(List<String> src, String regx, Regular.MATCH_MODE mode, Regular.FILTER_TYPE type) {
		if(Regular.FILTER_TYPE.PICK == type) {
			return pick(src, regx, mode);
		}else if(Regular.FILTER_TYPE.WIPE == type) {
			return wipe(src, regx, mode);
		}else{
			return new ArrayList<>();
		}
	}

	/**
	 * 过滤 保留匹配项
	 * @param src  src
	 * @param regx  regx
	 * @param mode  mode
	 * @return List
	 */
	public static synchronized List<String> pick(List<String> src, String regx, Regular.MATCH_MODE mode) {
		regular = regularList.get(mode);
		return regular.pick(src, regx);
	}

	/**
	 * 过滤 删除匹配项
	 * @param src  src
	 * @param regx  regx
	 * @param mode  mode
	 * @return List
	 */
	public static synchronized List<String> wipe(List<String> src, String regx, Regular.MATCH_MODE mode) {
		regular = regularList.get(mode);
		return regular.wipe(src, regx);
	}

	/**
	 * 字符串下标 regx在src中首次出现的位置
	 * @param src     src
	 * @param regx    regx
	 * @param begin   有效开始位置
	 * @return int
	 */
	public static int indexOf(String src, String regx, int begin) {
		int idx = -1;
		try{
			PatternCompiler patternCompiler = new Perl5Compiler();
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			PatternMatcherInput input = new PatternMatcherInput(src);

			while(matcher.contains(input, pattern)) {
				MatchResult matchResult = matcher.getMatch();
				int tmp = matchResult.beginOffset(0);
				if(tmp >= begin) {//匹配位置从begin开始
					idx = tmp;
					break;
				}
			}
		}catch(Exception e) {
			log.error("[提取异常][src:{}][regx:{}]", src, regx);
			e.printStackTrace();
		}
		return idx;
	}
	public static int indexOf(String src, String regx) {
		return indexOf(src, regx, 0);
	}

	/**
	 * 表达式匹配值长度
	 * @param src  src
	 * @param regex  regex
	 * @param mode  mode
	 * @return List
	 */
	public static List<String> regexpValue(String src, String regex, Regular.MATCH_MODE mode) {
		List<String> result = new ArrayList<>();
		try{
			List<List<String>> rs = fetchs(src, regex, mode);
			for(List<String> row:rs) {
				result.add(row.get(0));
			}
		}catch(Exception e) {
			log.error("[提取异常][src:{}][reg:{}][mode:{}]", src, regex, mode);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除所有 包含attribute属性 的标签 连同标签体一起删除<br/>
	 * RegularUtil.removeTagWithBodyByAttribute(str, "class")<br/>
	 * &lt;input type="text" class="a"/&gt;<br/>
	 * &lt;input type="text" class="a"&gt;&lt;/input&gt;<br/>
	 * &lt;input type="text" class = "a"&gt;&lt;/input&gt;<br/>
	 * &lt;input type="text" class&gt;&lt;/input&gt;<br/>
	 * &lt;input type="text" class/&gt;<br/>
	 * &lt;input type="text" a="class"&gt;&lt;/input&gt;(不匹配)
	 * @param src src
	 * @param attribute 属性
	 * @return String
	 */
	public static String removeTagWithBodyByAttribute(String src, String attribute) {
		if(null == src || null == attribute) {
			return src;
		}
		String reg = "";
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?>[^>]*?</\\1>";//双标签
		src = src.replaceAll(reg, "");
		reg =  "<[\\w-]+[^>]*?\\s"+attribute+"\\b[^>]*?(>|(/>))";//单标签
		src = src.replaceAll(reg, "");
		return src;
	}

	/**
	 * 删除所有 包含attribute属性=value值  的标签连同标签体一起删除<br/>
	 * RegularUtil.removeTagWithBodyByAttributeValue(s, "class","a")<br/>
	 * &lt;input type="text" class="a"/&gt;<br/>
	 * &lt;input type="text" class="a"/&gt;&lt;/input/&gt;<br/>
	 * &lt;input type="text" class="a b"/&gt;&lt;/input/&gt;如果需要不匹配可以使用"[^\\s]a[^\\s]"<br/>
	 * &lt;input type="text" class="b a"/&gt;&lt;/input/&gt;<br/>
	 * &lt;input type="text" class="ab"/&gt;&lt;/input/&gt;(不匹配)如果需要匹配可以使用"a.*"<br/>
	 *
	 * @param src xml/html
	 * @param attribute 属性
	 * @param value 值
	 * @return String
	 */
	public static String removeTagWithBodyByAttributeValue(String src, String attribute, String value) {
		if(null == src || null == attribute || null == value) {
			return src;
		}
		String reg ="";
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?>[^>]*?</\\1>";//双标签
		src = src.replaceAll(reg, "");
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?/>";//单标签
		src = src.replaceAll(reg, "");
		return src;
	}

	/**
	 * 根据属性名与属性值 删除标签(只删除标签, 保留标签体)
	 * @param src xml/html
	 * @param attribute 属性名
	 * @param value 属性值
	 * @return String
	 */
	public static String removeTagByAttributeValue(String src, String attribute, String value) throws Exception {
		if(null == src || null == attribute || null == value) {
			return src;
		}
		//[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称]
		List<List<String>> lists = getTagWithBodyByAttributeValue(src, attribute, value);
		for(List<String> list:lists) {
			String all = list.get(0);
			String body = list.get(3);
			src = src.replace(all, body);
		}
		return src;
	}

	/**
	 * 根据属性名 删除标签(只删除标签, 保留标签体)
	 * @param src xml/html
	 * @param attribute 属性名
	 * @return String
	 */
	public static String removeTagByAttribute(String src, String attribute) throws Exception {
		if(null == src || null == attribute) {
			return src;
		}
		List<List<String>> lists = getTagWithBodyByAttribute(src, attribute);
		//[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称]
		for(List<String> list:lists) {
			String all = list.get(0);
			String body = list.get(3);
			src = src.replace(all, body);
		}
		return src;
	}

	/**
	 * 获取所有 包含attribute属性 的标签与标签体, 不支持相同标签嵌套<br/>
	 * [<br/>
	 * 	[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称], <br/>
	 * 	[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称]<br/>
	 * ]<br/>
	 * @param src xml/html
	 * @param attribute 属性
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static List<List<String>> getTagWithBodyByAttribute(String src, String attribute) throws Exception {
		List<List<String>> result = new ArrayList<>();
		String reg =  "(<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?>[^>]*?</\\1>)"	// 双标签
				+ "|(<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?(>|(/>)))";			// 单标签
		Regular regular = regularList.get(Regular.MATCH_MODE.CONTAIN);
		List<List<String>> list = regular.fetchs(src, reg);
		int idx = 0;
		for(List<String> tmp:list) {
			List<String> item = new ArrayList<>();
			String start = tmp.get(0);
			String all = null;
			String body = null;
			String end = null;
			String name = tmp.get(4);
			idx = src.indexOf(start, idx);
			if(!start.endsWith("/>")) {
				end = "</" + name + ">";
				int fr = idx+start.length();
				int to = src.indexOf(end, idx+start.length());
				if(to > fr) {
					body = src.substring(fr, to);
					if(body.contains(end)) {
						body = null;
					}
				}
				if(null == body) {
					end = null;
					all = start;
				}else{
					all = start + body + end;
				}
			}else{
				all = start;
			}
			item.add(all);
			item.add(start);
			item.add(end);
			item.add(body);
			item.add(name);
			result.add(item);
		}
		return result;
	}

	/**
	 * 获取所有 包含attribute属性并且值=value  的标签与标签体<br/>
	 * 单标签只匹配有/&gt;结尾的情况, 避免与双标签的开始标签混淆<br/>
	 * 如class="a" : attribute=class value=a<br/>
	 * style="width:100px;" :attribute=style value=width<br/>
	 * [<br/>
	 * 	[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称], <br/>
	 * 	[整个标签含标签体, 开始标签, 结束标签, 标签体, 标签名称]<br/>
	 * ]<br/>
	 * @param src xml/html
	 * @param attribute 属性
	 * @param value 值
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static List<List<String>> getTagWithBodyByAttributeValue(String src, String attribute, String value) throws Exception {
		List<List<String>> result = new ArrayList<>();
		Regular regular = regularList.get(Regular.MATCH_MODE.CONTAIN);
		String reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?>[^>]*?</\\1>";	// 双标签
		List<List<String>> list = regular.fetchs(src, reg);
		int idx = 0;
		for(List<String> tmp:list) {
			List<String> item = new ArrayList<>();
			String all = tmp.get(0);
			String name = tmp.get(1);
			String start = all.substring(0, all.indexOf(">")+1);
			String end = "</" + name + ">";
			String body = cut(all, start, end);
			item.add(all);
			item.add(start);
			item.add(end);
			item.add(body);
			item.add(name);
			result.add(item);
		}

		reg = "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?/>";	// 单标签
		list = regular.fetchs(src, reg);
		for(List<String> tmp:list) {
			List<String> item = new ArrayList<>();
			item.add(tmp.get(0));
			item.add(tmp.get(0));
			item.add(null);
			item.add(null);
			item.add(tmp.get(1));
			result.add(item);
		}
		return result;
	}

	/**
	 * 删除 tags之外的标签"&lt;b&gt;"与"&lt;/b&gt;"只写一次 "b"<br/>
	 * 只删除标签不删除标签体
	 * @param src  html
	 * @param tags  tags
	 * @return String
	 */
	public static String removeTagExcept(String src, String ...tags) {
		if(null == src || null == tags || tags.length == 0) {
			return src;
		}
		int size = tags.length;
		String reg = "(?i)<(?!(\\!"; // <!--不匹配注释
		for(int i=0; i<size; i++) {
			reg += "|";
			reg += "(/?\\s?" + tags[i] + "\\b)";
		}
		reg += "))[^>]+>";
		src = src.replaceAll(reg, "");
		return src;
	}
	public static String removeHtmlTagExcept(String src, String ...tags) {
		return removeTagExcept(src, tags);
	}

	/**
	 * 清除所有标签(只清除标签, 不清除标签体)
	 * @param src xml/html
	 * @param tags tags
	 * @return String
	 */
	public static String removeTag(String src, String ...tags) {
		if(null == src) {
			return src;
		}
		if(null == tags || tags.length==0) {
			src = src.replaceAll(Regular.PATTERN.HTML_TAG.getCode(), "");
		}else{
			for(String tag:tags) {
				String reg = "(?i)<"+tag+"[^>]*>.*?|</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTag(String src, String ...tags) {
		return removeTag(src, tags);
	}

	/**
	 * 删除标签及标签体
	 * @param src xml/html
	 * @param tags 标签, 如果不提供则删除所有标签
	 * @return String
	 */
	public static String removeTagWithBody(String src, String ...tags) {
		if(null == src) {
			return src;
		}
		if(null == tags || tags.length==0) {
			src = src.replaceAll(Regular.PATTERN.HTML_TAG_WITH_BODY.getCode(), "");
		}else{
			for(String tag:tags) {
				String reg = "(?i)<"+tag+"[^>]*>[\\s\\S]*?</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTagWithBody(String src, String ...tags) {
		return removeTagWithBody(src, tags);
	}

	/**
	 * 删除所有空标签
	 * @param src xml/html
	 * @return String
	 */
	public static String removeEmptyTag(String src) {
		if(null == src) {
			return src;
		}
		String reg = "(?i)(<(\\w+)[^<]*?>)\\s*(</\\2>)";
		src = src.replaceAll(reg, "");
		return src;
	}
	public static String removeHtmlEmptyTag(String src) {
		return removeEmptyTag(src);
	}

	/**
	 * 删除简单标签外的其他标签
	 * @param src  html
	 * @return String
	 */
	public static String removeHtmlTagExceptSimple(String src) {
		return removeHtmlTagExcept(src, "br","b","strong","u","i","pre","ul","li","p");
	}

	/**
	 * 删除所有标签的属性 只删除属性 不删除标签与标签体
	 * @param src xml/html
	 * @param attributes 属性 如果不传则删除所有属性
	 * @return String
	 */
	public static String removeAttribute(String src, String ... attributes) {
		if(null == src) {
			return src;
		}
		String reg = null;
		if(null != attributes && attributes.length > 0) {
			for(String attribute:attributes) {
				reg = attribute + "\\s*=\\s*\"[\\s\\S]*\"";
				src = src.replaceAll(reg, "");
				reg = attribute + "\\s*=\\s*\'[\\s\\S]*\'";
				src = src.replaceAll(reg, "");
				src = src.replaceAll("\\s+>",">");
			}
		}else{
			reg = "\\S+?\\s*?=\\s*?\"[\\s\\S]*?\"";
			src = src.replaceAll(reg, "");
			reg = "\\S+?\\s*?=\\s*?\'[\\s\\S]*?\'";
			src = src.replaceAll(reg, "");
			src = src.replaceAll("\\s+?>",">");
		}
		return src;
	}

	/**
	 * 提取所有a棱中的url
	 * @param src xml/html
	 * @return list
	 * @throws Exception 异常
	 */
	public static List<String> fetchUrls(String src) throws Exception {
		List<String> urls = null;
		urls = fetch(src, Regular.PATTERN.HTML_TAG_A.getCode(), Regular.MATCH_MODE.CONTAIN, 4);
		return urls;
	}

	public static String fetchUrl(String src) throws Exception {
		List<String> urls = fetchUrls(src);
		if(null != urls && !urls.isEmpty()) {
			return urls.get(0);
		}
		return null;
	}
	public static List<String> fetchNumbers(String src) throws Exception {
		List<String> numbers = null;
		numbers = fetch(src, "(\\-|\\+)?\\d+(\\.\\d+)?", Regular.MATCH_MODE.CONTAIN, 0);
		return numbers;
	}
	public static String fetchNumber(String src) throws Exception {
		List<String> numbers = fetchNumbers(src);
		if(null != numbers && !numbers.isEmpty()) {
			return numbers.get(0);
		}
		return null;
	}

	/**
	 * 提取双标签&lt;div&gt;content&lt;div&gt;
	 * 依次取出p, table, div中的内容 有嵌套时只取外层
	 * 只能提取同时有 开始结束标签的内容, 不能提取单标签内容如&lt;img&gt; &lt;br/&gt;
	 * 支持不同标签嵌套, 但不支持相同标签嵌套
	 * 不区分大小写
	 * 0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签
	 * @param txt text
	 * @param tags 标签名, 如div, span tags标签名, 如div, span
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static List<List<String>> fetchPairedTag(String txt, String ... tags) throws Exception {
		List<List<String>> result = new ArrayList<List<String>>();
		if(null != tags && tags.length>0) {
			String tagNames = "";
			int size = tags.length;
			for(int i=0; i<size; i++) {
				if(i==0) {
					tagNames += tags[i];
				}else{
					tagNames += "|"+tags[i];
				}
			}
			String regx = "(?i)(<(" + tagNames + ")[^<]*?>)([\\s\\S]*?)(</\\2>)";
			result = fetchs(txt, regx);
		}
		return result;
	}

	/**
	 * 提取单标签 如&lt;img&gt; &lt;br/&gt;
	 * 如果传入div等带有结束标签的参数 则只取出开始标签 &lt;div&gt;
	 * 不区分大小写
	 * 0:全文 1::标签name
	 * @param txt text
	 * @param tags 标签名, 如img br
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static List<List<String>> fetchSingleTag(String txt, String ... tags) throws Exception {
		List<List<String>> result = new ArrayList<List<String>>();
		if(null != tags && tags.length>0) {
			String tagNames = "";
			int size = tags.length;
			for(int i=0; i<size; i++) {
				if(i==0) {
					tagNames += tags[i];
				}else{
					tagNames += "|"+tags[i];
				}
			}
			String regx = "(?i)<(" +tagNames+")[\\s\\S]*?>";
			result = fetchs(txt, regx);
		}
		return result;
	}

	/**
	 * 提取单标签+双标签
	 * 不区分大小写
	 * 0:全文 1:开始标签 2:标签name 3:标签体 (单标签时null) 4:结束标签 (单标签时null)
	 * @param txt txt
	 * @param tags 标签名 tags标签名
	 * @return List
	 * @throws Exception 异常 Exception
	 */
	public static List<List<String>> fetchAllTag(String txt, String ... tags) throws Exception {
		List<List<String>> result = new ArrayList<List<String>>();
		List<List<String>> items = new ArrayList<List<String>>();
		if(null != tags && tags.length>0) {
			String names = "";
			int size = tags.length;
			for(int i=0; i<size; i++) {
				if(i==0) {
					names += tags[i];
				}else{
					names += "|"+tags[i];
				}
			}
			String regx = "(?i)((<(" + names + ")\\s[^<]*?>)([\\s\\S]*?)(</\\3>))|(<(" +names+")\\s[\\s\\S]*?>)";
			items = fetchs(txt, regx);
			for(List<String> item:items) {
				List<String> rtn = new ArrayList<>();
				if(null == item.get(7)) {
					// 双标签 0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 (单标签时null)
					rtn.add(item.get(0));
					rtn.add(item.get(2));
					rtn.add(item.get(3));
					rtn.add(item.get(4));
					rtn.add(item.get(5));
				}else{
					// 单标签  0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 (单标签时null)
					rtn.add(item.get(0));
					rtn.add(item.get(0));
					rtn.add(item.get(7));
					rtn.add(null);
					rtn.add(null);
				}
				result.add(rtn);
			}
		}
		return result;
	}

	/**
	 * 取出所有属性值
	 * 0全文  1:属性name 2:引号('|") 3:属性值
	 * fetchAttributeValues(txt, "id");
	 * @param txt txt
	 * @param attribute 属性
	 * @return List
	 */
	public static List<List<String>> fetchAttributeList(String txt, String attribute) {
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			String regx = "(?i)(" + attribute + ")\\s*=\\s*(['\"])([\\s\\S]*?)\\2";
			result = fetchs(txt, regx);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 取出属性及属性值
	 * 0全文  1:属性name 2:引号('|") 3:属性值
	 * fetchAttributeValues(txt, "id","name");
	 * @param txt txt
	 * @param attribute 属性
	 * @return List
	 */
	public static List<String> fetchAttribute(String txt, String attribute) {
		List<String> result = new ArrayList<>();
		List<List<String>> list = fetchAttributeList(txt, attribute);
		if(!list.isEmpty()) {
			result = list.get(0);
		}
		return result;
	}

	/**
	 * 取出所有的属性值
	 * @param txt txt
	 * @param attribute 属性名
	 * @return List
	 */
	public static List<String> fetchAttributeValues(String txt, String attribute) {
		List<String> result = new ArrayList<>();
		List<List<String>> list = fetchAttributeList(txt, attribute);
		for(List<String> attr: list) {
			if(attr.size() > 3 ) {
				result.add(attr.get(3));
			}
		}
		return result;
	}

	/**
	 * 取出的属性值(有多个的取第一个)
	 * @param txt txt
	 * @param attribute 属性名
	 * @return String
	 */
	public static String fetchAttributeValue(String txt, String attribute) {
		List<String> values = fetchAttributeValues(txt, attribute);
		if(!values.isEmpty()) {
			return values.get(0);
		}else{
			return null;
		}
	}
	public static String cut(String text, String ... tags) {
		return cut(text, false, tags);
	}

	/**
	 * 取tags[i-2]与tags[i-1]之间的文本
	 * @param text text
	 * @param tags tags
	 * @param contains 是否包含开始结束标签
	 * @return String
	 */
	public static String cut(String text, boolean contains, String ... tags) {
		if(null == text || null == tags || tags.length < 2) {
			/*没有开始结束标志*/
			return null;
		}
		int _fr = -1;	// 开始下标
		int _to = -1;	// 结束下标
		String frTag = "";
		String toTag = tags[tags.length-1];
		int frLength = 0;
		int toLength = 0;
		for(int i=0; i<tags.length-1; i++) {
			frTag = tags[i];
			if(frTag.equalsIgnoreCase(TAG_BEGIN)) {
				_fr = 0;
				frLength = 0;
			}else{
				if(i>0) {
					_fr= text.indexOf(frTag, _fr+tags[i-1].length());
				}else{
					_fr= text.indexOf(frTag, _fr);
				}
				if(_fr == -1) {
					return null;
				}
				frLength = frTag.length();
			}
		}
		if(frTag.equalsIgnoreCase(TAG_END)) {
			_to = text.length();

		}else{
			if(toTag.equalsIgnoreCase(TAG_END)) {
				_to = text.length();
				toLength = 0;
			}else{
				_to = text.indexOf(toTag, _fr+frLength);
				toLength = toTag.length();
			}
		}
		if(_to <= _fr) {
			return null;
		}
		String result = null;
		if(contains) {
			_fr = text.indexOf(tags[0]);
			result = text.substring(_fr, _to+toLength);
		}else{
			result = text.substring(_fr+frLength, _to);
		}
		return result;
	}

	public static List<String> cuts(String text, String ... tags) {
		return cuts(text, false, tags);
	}
	public static List<String> cuts(String text, boolean contains, String ... tags) {
		List<String> list = new ArrayList<>();
		while(true) {
			String item = cut(text, contains, tags);
			if(null == item) {
				break;
			}else{
				list.add(item);
				int idx = 0;
				// 计算新起点
				for(int i=0; i<tags.length; i++) {
					if(idx>0) {
						idx += 1;
					}
					idx = text.indexOf(tags[i], idx);
				}
				if(idx <= 0) {
					break;
				}
				text = text.substring(idx);
			}
		}
		return list;
	}
	public static boolean isDate(String str) {
		if(str == null) {
			return false;
		}
		str = str.replace("/","-");
		return regularMatch.match(str, Regular.PATTERN.DATE.getCode());
	}
	public static boolean isDateTime(String str) {
		if(null == str) {
			return false;
		}
		str = str.replace("/","-");
		return regularMatch.match(str, Regular.PATTERN.DATE_TIME.getCode());
	}

	public static boolean isUrl(String src) {
		if(null == src) {
			return false;
		}
		if(src.startsWith("http://") || src.startsWith("https://")) {
			return true;
		}
		if(src.startsWith("//")) {
			src = src.substring(2);
			int index1 = src.indexOf("."); 	// 域名中的.
			if(index1 == -1) {
				return false;
			}
			int index2 = src.indexOf("/");	// url中的path分隔
			if(index1 < index2) {			// 没有在/之前出现的 有可能是文件名中的.
				return true;
			}
			if(index2 == -1) {				// 没有域名
				return true;
			}
		}
		return false;
	}
}
