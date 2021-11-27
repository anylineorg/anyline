/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.util.regular; 
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.util.ConfigTable;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
public class RegularUtil {
	private static Regular regular; 
	public static Regular regularMatch 		= new RegularMatch();			//完全匹配模式 
	public static Regular regularMatchPrefix 	= new RegularMatchPrefix();		//前缀匹配模式 
	public static Regular regularContain 		= new RegularContain();			//包含匹配模式 
	 
	private static final Map<Regular.MATCH_MODE,Regular> regularList = new HashMap<Regular.MATCH_MODE,Regular>(); 
	public static final String REGEX_VARIABLE = "{(\\w+)}";		//变量{ID}

	public static final String TAG_BEGIN = "{begin}";
	public static final String TAG_END = "{end}"; 
	 
	private static final Logger log = LoggerFactory.getLogger(RegularUtil.class); 
	static{ 
		regularList.put(Regular.MATCH_MODE.MATCH, regularMatch); 
		regularList.put(Regular.MATCH_MODE.PREFIX, regularMatchPrefix); 
		regularList.put(Regular.MATCH_MODE.CONTAIN, regularContain); 
	} 
	public static synchronized boolean match(String src, String regx, Regular.MATCH_MODE mode){
		boolean result = false;
		if(ConfigTable.getBoolean("IS_REGULAR_LOG")){
			log.warn("[match][src:{}][regx:{}][mode:{}]",src, regx, mode);
		}
		if(null == src || null == regx ){
			return result;
		} 
		regular = regularList.get(mode);
		try{
			result = regular.match(src, regx);
		}catch(Exception e){
			log.warn("[match(src,regx,mode) error][src:{}][regx:{}][mode:{}]", src, regx, mode);
			e.printStackTrace();
		}
		if(ConfigTable.getBoolean("IS_REGULAR_LOG")){
			log.warn("[match][src:{}][regx:{}][mode:{}][result:{}]", src, regx, mode, result);
		}
		return result; 
	} 
	public static boolean match(String src, String regx){ 
		return match(src, regx, Regular.MATCH_MODE.CONTAIN); 
	} 
	 
	/** 
	 * 提取子串 
	 * @param src	输入字符串  src	输入字符串
	 * @param regx	表达式  regx	表达式
	 * @param mode	mode
	 * @return return
	 * @throws Exception Exception
	 */ 
	public static synchronized List<List<String>> fetchs(String src, String regx, Regular.MATCH_MODE mode) throws Exception{ 
		List<List<String>> result = null; 
		regular = regularList.get(mode); 
		result = regular.fetchs(src, regx); 
		return result; 
	} 
	public static List<List<String>> fetchs(String src, String regx) throws Exception{ 
		return fetchs(src, regx, Regular.MATCH_MODE.CONTAIN); 
	} 
	public static List<String> fetch(String src, String regx) throws Exception{ 
		return fetch(src, regx, Regular.MATCH_MODE.CONTAIN); 
	} 
	public static synchronized List<String> fetch(String src, String regx, Regular.MATCH_MODE mode, int idx) throws Exception{ 
		List<String> result = null; 
		regular = regularList.get(mode); 
		result = regular.fetch(src, regx, idx); 
		return result; 
	} 
	public static synchronized List<String> fetch(String src, String regx, Regular.MATCH_MODE mode) throws Exception{ 
		List<String> result = null; 
		regular = regularList.get(mode); 
		result = regular.fetch(src, regx); 
		return result; 
	} 
	public static List<String> filter(List<String> src, String regx, Regular.MATCH_MODE mode, Regular.FILTER_TYPE type){ 
		if(Regular.FILTER_TYPE.PICK == type){ 
			return pick(src,regx,mode); 
		}else if(Regular.FILTER_TYPE.WIPE == type){ 
			return wipe(src,regx,mode); 
		}else{ 
			return new ArrayList<String>(); 
		} 
	} 
	/** 
	 * 过滤 保留匹配项 
	 * @param src  src
	 * @param regx  regx
	 * @param mode  mode
	 * @return return
	 */ 
	public static synchronized List<String> pick(List<String> src, String regx, Regular.MATCH_MODE mode){ 
		regular = regularList.get(mode); 
		return regular.pick(src, regx); 
	} 
	/** 
	 * 过滤 删除匹配项 
	 * @param src  src
	 * @param regx  regx
	 * @param mode  mode
	 * @return return
	 */ 
	public static synchronized List<String> wipe(List<String> src, String regx, Regular.MATCH_MODE mode){ 
		regular = regularList.get(mode); 
		return regular.wipe(src, regx); 
	} 
	/** 
	 * 字符串下标 regx在src中首次出现的位置 
	 * @param src     src
	 * @param regx    regx
	 * @param begin   有效开始位置 
	 * @return return
	 */ 
	public static int indexOf(String src, String regx, int begin){ 
		int idx = -1; 
		try{ 
			PatternCompiler patternCompiler = new Perl5Compiler(); 
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK); 
			PatternMatcher matcher = new Perl5Matcher(); 
			PatternMatcherInput input = new PatternMatcherInput(src); 
			 
			while(matcher.contains(input, pattern)){ 
				MatchResult matchResult = matcher.getMatch(); 
				int tmp = matchResult.beginOffset(0); 
				if(tmp >= begin){//匹配位置从begin开始 
					idx = tmp; 
					break; 
				} 
			} 
		}catch(Exception e){
			log.error("[提取异常][src:{}][regx:{}]", src, regx); 
			e.printStackTrace();
		} 
		return idx; 
	} 
	public static int indexOf(String src, String regx){ 
		return indexOf(src,regx,0); 
	} 
	/** 
	 * 表达式匹配值长度 
	 * @param src  src
	 * @param regex  regex
	 * @param mode  mode
	 * @return return
	 */ 
	public static List<String> regexpValue(String src, String regex, Regular.MATCH_MODE mode){ 
		List<String> result = new ArrayList<String>(); 
		try{ 
			List<List<String>> rs = fetchs(src, regex, mode); 
			for(List<String> row:rs){ 
				result.add(row.get(0)); 
			} 
		}catch(Exception e){
			log.error("[提取异常][src:{}][reg:{}][mode:{}]", src, regex, mode);
			e.printStackTrace();
		} 
		return result; 
	}
	/**
	 * 清除所有标签(只清除标签，不清除标签体)
	 * @param src src
	 * @return return
	 */
	public static String removeAllTag(String src){
		if(null == src){
			return src;
		}
		return src.replaceAll(Regular.PATTERN.HTML_TAG.getCode(), "");
	}
	public static String removeAllHtmlTag(String src){
		return removeAllTag(src);
	}
	/*清除所有包括指定属性的标签|包括标签体
清除所有包括指定属性值的标签|包括标签体

获取所有包括指定属性的标签
获取所有包括指定属性值的标签*/
	/**
	 * 删除所有 包含attribute属性 的标签与标签体
	 * RegularUtil.removeAllTagAndBodyWithAttribute(str,"class")
	 * &lt;input type="text" class="a"/&gt;
	 * &lt;input type="text" class="a"&gt;&lt;/input&gt;
	 * &lt;input type="text" class = "a"&gt;&lt;/input&gt;
	 * &lt;input type="text" class&gt;&lt;/input&gt;
	 * &lt;input type="text" class/&gt;
	 * &lt;input type="text" a="class"&gt;&lt;/input&gt;(不匹配)
	 * @param src src
	 * @param attribute attribute
	 * @return return
	 */
	public static String removeAllTagAndBodyWithAttribute(String src, String attribute){
		String reg = "";
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?>[^>]*?</\\1>";//双标签
		src = src.replaceAll(reg, "");
		reg =  "<[\\w-]+[^>]*?\\s"+attribute+"\\b[^>]*?(>|(/>))";//单标签
		src = src.replaceAll(reg, "");
		return src;
	}
	/**
	 * RegularUtil.removeAllTagAndBodyWithAttributeValue(s,"class","a")
	 * 删除所有 包含attribute属性=value值  的标签与标签体
	 * &lt;input type="text" class="a"/&gt;
	 * &lt;input type="text" class="a"/&gt;&lt;/input/&gt;
	 * &lt;input type="text" class="a b"/&gt;&lt;/input/&gt;如果需要不匹配可以使用"[^\\s]a[^\\s]"
	 * &lt;input type="text" class="b a"/&gt;&lt;/input/&gt;
	 * &lt;input type="text" class="ab"/&gt;&lt;/input/&gt;(不匹配)如果需要匹配可以使用"a.*"
	 * 
	 * @param src src
	 * @param attribute attribute
	 * @param value value
	 * @return return
	 */
	public static String removeAllTagAndBodyWithAttributeValue(String src, String attribute, String value){
		String reg ="";
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?>[^>]*?</\\1>";//双标签
		src = src.replaceAll(reg, "");
		reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?/>";//单标签
		src = src.replaceAll(reg, "");
		return src;
	}

	/**
	 * 获取所有 包含attribute属性 的标签与标签体,不支持相同标签嵌套
	 * [
	 * 	[整个标签含标签体,开始标签，结束标签，标签体，标签名称],
	 * 	[整个标签含标签体,开始标签，结束标签，标签体，标签名称]
	 * ]
	 * @param src src
	 * @param attribute attribute
	 * @return return
	 * @throws Exception Exception
	 */
	public static List<List<String>> getAllTagAndBodyWithAttribute(String src, String attribute) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		String reg =  "(<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?>[^>]*?</\\1>)"	//双标签
				+ "|(<([\\w-]+)[^>]*?\\s"+attribute+"\\b[^>]*?(>|(/>)))";			//单标签
		Regular regular = regularList.get(Regular.MATCH_MODE.CONTAIN);
		List<List<String>> list = regular.fetchs(src, reg);
		int idx = 0;
		for(List<String> tmp:list){
			List<String> item = new ArrayList<String>();
			String start = tmp.get(0);
			String all = null;
			String body = null;
			String end = null;
			String name = tmp.get(4);
			idx = src.indexOf(start, idx);
			if(!start.endsWith("/>")){
				end = "</" + name + ">";
				int fr = idx+start.length();
				int to = src.indexOf(end,idx+start.length());
				if(to > fr) {
					body = src.substring(fr, to);
					if(body.contains(end)){
						body = null;
					}
				}
				if(null == body){
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
	 * 获取所有 包含attribute属性包含value值  的标签与标签体
	 * 单标签只匹配有/&gt;结尾的情况，避免与双标签的开始标签混淆
	 * 如class="a" : attribute=class value=a
	 * style="width:100px;" :attribute=style value=width
	 * [
	 * 	[整个标签含标签体,开始标签，结束标签，标签体，标签名称],
	 * 	[整个标签含标签体,开始标签，结束标签，标签体，标签名称]
	 * ]
	 * @param src src
	 * @param attribute attribute
	 * @param value value
	 * @return return
	 * @throws Exception Exception
	 */
	public static List<List<String>> getAllTagAndBodyWithAttributeValue(String src, String attribute, String value) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		Regular regular = regularList.get(Regular.MATCH_MODE.CONTAIN);
		String reg =  "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?>[^>]*?</\\1>";	//双标签
		List<List<String>> list = regular.fetchs(src, reg);
		int idx = 0;
		for(List<String> tmp:list){
			List<String> item = new ArrayList<String>();
			String all = tmp.get(0);
			String name = tmp.get(1);
			String start = all.substring(0,all.indexOf(">")+1);
			String end = "</" + name + ">";
			String body = cut(all, start,end);
			item.add(all);
			item.add(start);
			item.add(end);
			item.add(body);
			item.add(name);
			result.add(item);
		}

		reg = "<([\\w-]+)[^>]*?\\s"+attribute+"\\b[\\s]*=[\\s]*(['\"])[^>]*?\\b"+value+"\\b[^>]*?\\2[^>]*?/>";	//单标签
		list = regular.fetchs(src, reg);
		for(List<String> tmp:list){
			List<String> item = new ArrayList<String>();
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
	 * 删除 tags之外的标签"&lt;b&gt;"与"&lt;/b&gt;"只写一次 "b"
	 * 只删除标签不删除标签体 
	 * @param src  src
	 * @param tags  tags
	 * @return return
	 */ 
	public static String removeTagExcept(String src, String ...tags){ 
		if(null == src || null == tags || tags.length == 0){ 
			return src; 
		} 
		int size = tags.length; 
		String reg = "(?i)<(?!("; 
		for(int i=0; i<size; i++){ 
			reg += "(/?\\s?" + tags[i] + "\\b)"; 
			if(i < size-1){ 
				reg += "|"; 
			} 
		} 
		reg += "))[^>]+>"; 
		src = src.replaceAll(reg, ""); 
		return src; 
	}
	public static String removeHtmlTagExcept(String src, String ...tags){
		return removeTagExcept(src, tags);
	}
	/**
	 * 只删除标签,不删除标签体
	 * @param src src
	 * @param tags tags
	 * @return return
	 */
	public static String removeTag(String src, String ...tags){
		if(null == tags || tags.length==0){
			src = removeAllHtmlTag(src);
		}else{
			for(String tag:tags){
				String reg = "(?i)<"+tag+"[^>]*>.*?|</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTag(String src, String ...tags){
		return removeTag(src, tags);
	}
	/**
	 * 删除标签及标签体
	 * @param src src
	 * @param tags tags
	 * @return return
	 */
	public static String removeTagWithBody(String src, String ...tags){
		if(null == tags || tags.length==0){
			src = removeAllHtmlTag(src);
		}else{
			for(String tag:tags){
				String reg = "(?i)<"+tag+"[^>]*>.*?</"+tag+">";
				src = src.replaceAll(reg, "");
			}
		}
		return src;
	}
	public static String removeHtmlTagWithBody(String src, String ...tags){
		return removeTagWithBody(src, tags);
	}
	public static String removeEmptyTag(String src){
		String reg = "(?i)(<(\\w+)[^<]*?>)\\s*(</\\2>)";
		src = src.replaceAll(reg, "");
		return src;
	}
	public static String removeHtmlEmptyTag(String src){
		return removeEmptyTag(src);
	}
	
	/** 
	 * 删除简单标签外的其他标签 
	 * @param src  src
	 * @return return
	 */ 
	public static String removeHtmlTagExceptSimple(String src){ 
		return removeHtmlTagExcept(src,"br","b","strong","u","i","pre","ul","li","p"); 
	} 
	public static List<String> fetchUrls(String src) throws Exception{ 
		List<String> urls = null; 
		urls = fetch(src, Regular.PATTERN.HTML_TAG_A.getCode(), Regular.MATCH_MODE.CONTAIN, 4); 
		return urls; 
	} 
	public static String fetchUrl(String src) throws Exception{ 
		List<String> urls = fetchUrls(src); 
		if(null != urls && urls.size()>0){ 
			return urls.get(0); 
		} 
		return null; 
	}
	/**
	 * 提取双标签&lt;div&gt;content&lt;div&gt;
	 * 依次取出p,table,div中的内容 有嵌套时只取外层 
	 * 只能提取同时有 开始结束标签的内容，不能提取单标签内容如&lt;img&gt; &lt;br/&gt;
	 * 支持不同标签嵌套，但不支持相同标签嵌套
	 * 不区分大小写
	 * 0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 
	 * @param txt text
	 * @param tags 标签名,如div,span tags标签名,如div,span
	 * @return return
	 * @throws Exception Exception
	 */
	public static List<List<String>> fetchPairedTag(String txt,String ... tags) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		if(null != tags && tags.length>0){
			String tagNames = "";
			int size = tags.length;
			for(int i=0; i<size; i++){
				if(i==0){
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
	 * @param tags 标签名,如img br
	 * @return return
	 * @throws Exception Exception
	 */
	public static List<List<String>> fetchSingleTag(String txt,String ... tags) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		if(null != tags && tags.length>0){
			String tagNames = "";
			int size = tags.length;
			for(int i=0; i<size; i++){
				if(i==0){
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
	 * @return return
	 * @throws Exception Exception
	 */
	public static List<List<String>> fetchAllTag(String txt,String ... tags) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		List<List<String>> items = new ArrayList<List<String>>();
		if(null != tags && tags.length>0){
			String tagNames = "";
			int size = tags.length;
			for(int i=0; i<size; i++){
				if(i==0){
					tagNames += tags[i];
				}else{
					tagNames += "|"+tags[i];
				}
			}
			String regx = "(?i)((<(" + tagNames + ")[^<]*?>)([\\s\\S]*?)(</\\3>))|(<(" +tagNames+")[\\s\\S]*?>)";
			items = fetchs(txt, regx);
			for(List<String> item:items){
				List<String> rtn = new ArrayList<String>();
				if(null == item.get(7)){
					//双标签 0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 (单标签时null)
					rtn.add(item.get(0));
					rtn.add(item.get(2));
					rtn.add(item.get(3));
					rtn.add(item.get(4));
					rtn.add(item.get(5));
				}else{
					//单标签  0:全文 1:开始标签 2:标签name 3:标签体 4:结束标签 (单标签时null)
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
	 * fetchAttributeValues(txt,"id");
	 * @param txt txt
	 * @param tag tag
	 * @return return
	 */
	public static List<List<String>> fetchAttributeList(String txt, String tag){
		List<List<String>> result = new ArrayList<List<String>>();
		try{
			String regx = "(?i)(" + tag + ")\\s*=\\s*(['\"])([\\s\\S]*?)\\2";
			result = fetchs(txt, regx);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 取出d属性值
	 * 0全文  1:属性name 2:引号('|") 3:属性值
	 * fetchAttributeValues(txt,"id","name");
	 * @param txt txt
	 * @param tag tag
	 * @return return 
	 */
	public static List<String> fetchAttribute(String txt, String tag){
		List<String> result = new ArrayList<String>();
		List<List<String>> list = fetchAttributeList(txt, tag);
		if(list.size() >0){
			result = list.get(0);
		}
		return result;
	}
	public static List<String> fetchAttributeValues(String txt, String tag){
		List<String> result = new ArrayList<String>();
		List<List<String>> list = fetchAttributeList(txt, tag);
		for(List<String> attr: list){
			if(attr.size() > 3 ){
				result.add(attr.get(3));
			}
		}
		return result;
	}
	public static String fetchAttributeValue(String txt, String tag){
		List<String> values = fetchAttributeValues(txt, tag);
		if(values.size()>0){
			return values.get(0);
		}else{
			return null;
		}
	}
	public static String cut(String text, String ... tags){
		return cut(text, false, tags);
	}
	/**
	 * 取tags[i-2]与tags[i-1]之间的文本
	 * @param text text
	 * @param tags tags
	 * @param contains 是否包含开始结束标签
	 * @return return
	 */ 
	public static String cut(String text, boolean contains, String ... tags){
		if(null == text || null == tags || tags.length < 2){ 
			/*没有开始结束标志*/ 
			return null; 
		} 
		int _fr = -1;	//开始下标 
		int _to = -1;	//结束下标 
		String frTag = ""; 
		String toTag = tags[tags.length-1];
		int frLength = 0;
		int toLength = 0;
		for(int i=0; i<tags.length-1; i++){ 
			frTag = tags[i];
			if(frTag.equalsIgnoreCase(TAG_BEGIN)){
				_fr = 0;
				frLength = 0;
			}else{ 
				if(i>0){ 
					_fr= text.indexOf(frTag, _fr+tags[i-1].length()); 
				}else{ 
					_fr= text.indexOf(frTag, _fr); 
				} 
				if(_fr == -1){ 
					return null; 
				}
				frLength = frTag.length();
			} 
		}
		if(frTag.equalsIgnoreCase(TAG_END)){
			_to = text.length();

		}else{
			if(toTag.equalsIgnoreCase(TAG_END)){
				_to = text.length();
				toLength = 0;
			}else{ 
				_to = text.indexOf(toTag,_fr+frLength);
				toLength = toTag.length();
			}
		} 
		if(_to <= _fr) {
			return null;
		}
		String result = null;
		if(contains){
			_fr = text.indexOf(tags[0]);
			result = text.substring(_fr,_to+toLength);
		}else{
			result = text.substring(_fr+frLength,_to);
		}
		return result;
	}

	public static List<String> cuts(String text, String ... tags){
		return cuts(text, false, tags);
	}
	public static List<String> cuts(String text, boolean contains, String ... tags){
		List<String> list = new ArrayList<String>(); 
		while(true){ 
			String item = cut(text,contains, tags);
			if(null == item){ 
				break; 
			}else{ 
				list.add(item);
				int idx = 0;
				//计算新起点 
				for(int i=0; i<tags.length; i++){
					if(idx>0){
						idx += 1;
					}
					idx = text.indexOf(tags[i], idx);
				}
				if(idx <= 0){
					break;
				} 
				text = text.substring(idx); 
			} 
		} 
		return list; 
	}
	public static boolean isDate(String str){
		if(str == null){
			return false;
		}
		str = str.replace("/", "-");
		return regularMatch.match(str, Regular.PATTERN.DATE.getCode());
	}
	public static boolean isDateTime(String str){
		if(null == str){
			return false;
		}
		str = str.replace("/", "-");
		return regularMatch.match(str, Regular.PATTERN.DATE_TIME.getCode());
	}
} 
