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


package org.anyline.util.regular;

import java.util.ArrayList;
import java.util.List;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;


public class RegularUtil {
	private static Regular regular;
	public static Regular regularMatch 		= new RegularMatch();			//完全匹配模式
	public static Regular regularMatchPrefix 	= new RegularMatchPrefix();		//前缀匹配模式
	public static Regular regularContain 		= new RegularContain();			//包含匹配模式
	
	private static final List<Regular> regularList = new ArrayList<Regular>();
	public static final int MATCH_MODE_MATCH   = 0;				//完全匹配
	public static final int MATCH_MODE_PREFIX  = 1;				//前缀匹配
	public static final int MATCH_MODE_CONTAIN = 2;				//包含匹配
	public static final String REGEX_VARIABLE = "{(\\w+)}";		//变量{ID}
	
	private static Logger log = Logger.getLogger(RegularUtil.class);
	static{
		regularList.add(regularMatch);
		regularList.add(regularMatchPrefix);
		regularList.add(regularContain);
	}
	public static boolean match(String src, String regx, int mode){
		regular = regularList.get(mode);
		return regular.match(src, regx);
	}
	public static boolean match(String src, String regx){
		return match(src, regx, MATCH_MODE_CONTAIN);
	}
	
	/**
	 * 提取子串
	 * @param src	输入字符串
	 * @param regx	表达式
	 * @return
	 */
	public static List<List<String>> fetch(String src, String regx, int mode) throws Exception{
		List<List<String>> result = null;
		regular = regularList.get(mode);
		result = regular.fetch(src, regx);
		return result;
	}
	public static List<List<String>> fetch(String src, String regx) throws Exception{
		return fetch(src, regx, MATCH_MODE_CONTAIN);
	}
	public static List<String> fetch(String src, String regx, int mode, int idx) throws Exception{
		List<String> result = null;
		regular = regularList.get(mode);
		result = regular.fetch(src, regx, idx);
		return result;
	}
	public static List<String> filter(List<String> src, String regx, int regxMode, String filterType){
		if(Regular.FILTER_TYPE_PICK.equals(filterType)){
			return pick(src,regx,regxMode);
		}else if(Regular.FILTER_TYPE_WIPE.equals(filterType)){
			return wipe(src,regx,regxMode);
		}else{
			return new ArrayList<String>();
		}
	}
	/**
	 * 过滤 保留匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public static List<String> pick(List<String> src, String regx, int mode){
		regular = regularList.get(mode);
		return regular.pick(src, regx);
	}
	/**
	 * 过滤 删除匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public static List<String> wipe(List<String> src, String regx, int mode){
		regular = regularList.get(mode);
		return regular.wipe(src, regx);
	}
	/**
	 * 字符串下标 regx在src中首次出现的位置
	 * @param src   
	 * @param regx  
	 * @param idx   有效开始位置
	 * @return
	 * @throws Exception
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
			log.error("fetch(String,String):\n"+"src="+src+"\regx="+regx+"\n"+e);
			if(ConfigTable.isDebug()){
				e.printStackTrace();
			}
		}
		return idx;
	}
	public static int indexOf(String src, String regx){
		return indexOf(src,regx,0);
	}
	/**
	 * 表达式匹配值长度
	 * @param src
	 * @param regex
	 * @param mode
	 * @return
	 */
	public static List<String> regexpValue(String src, String regex, int mode){
		List<String> result = new ArrayList<String>();
		try{
			List<List<String>> rs = fetch(src, regex, mode);
			for(List<String> row:rs){
				result.add(row.get(0));
			}
		}catch(Exception e){
			log.error("regexpValue:\n"+e);
			if(ConfigTable.isDebug()){
				e.printStackTrace();
			}
		}
		return result;
	}
	public static String removeAllHtmlTag(String src){
		if(null == src){
			return src;
		}
		return src.replaceAll(Regular.html_tag_regexp, "");
	}
	/**
	 * 删除 tags之外的标签"<b>"与"</b>"只写一次 "b"
	 * @param src
	 * @param tags
	 * @return
	 */
	public static String removeHtmlTagExcept(String src, String ...tags){
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
	/**
	 * 删除简单标签外的其他标签
	 * @param src
	 * @return
	 */
	public static String removeHtmlTagExceptSimple(String src){
		return removeHtmlTagExcept(src,"br","b","strong","u","i","pre","ul","li","p");
	}
	public static List<String> fetchUrls(String src) throws Exception{
		List<String> urls = null;
		urls = fetch(src, Regular.html_tag_a_regexp, MATCH_MODE_CONTAIN, 4);
		return urls;
	}
	public static String fetchUrl(String src) throws Exception{
		List<String> urls = fetchUrls(src);
		if(null != urls && urls.size()>0){
			return urls.get(0);
		}
		return null;
	}
	public static String cut(String text,String ... tags){
		if(null == tags || tags.length < 2){
			/*没有开始结束标志*/
			return null;
		}
		int _fr = -1;	//开始下标
		int _to = -1;	//结束下标
		String frTag = "";
		String toTag = tags[tags.length-1];
		for(int i=0; i<tags.length-1; i++){
			frTag = tags[i];
			if(i>0){
				_fr= text.indexOf(frTag, _fr+tags[i-1].length());
			}else{
				_fr= text.indexOf(frTag, _fr);
			}
			if(_fr == -1){
				return null;
			}
		}
		_to = text.indexOf(toTag,_fr+frTag.length());
		if(_to <= _fr) {
			return null;
		}
		return text.substring(_fr+frTag.length(),_to).trim();
	}
	public static List<String> cuts(String text, String ... tags){
		List<String> list = new ArrayList<String>();
		while(true){
			String item = cut(text, tags);
			//if(BasicUtil.isEmpty(item)){
			if(null == item){
				break;
			}else{
				list.add(item);
				int idx = 0;
				//计算新起点 
				for(int i=0; i<tags.length; i++){
					idx = text.indexOf(tags[i], idx+1);
				}
				if(idx <= 0){
					break;
				}
				text = text.substring(idx);
			}
		}
		return list;
	}
//	/**
//	 * 解析并替换变量
//	 * @param src
//	 * @param data
//	 * @return
//	 */
//	public static String parseVariable(String src, Object data){	
//		 String result = src;
//		 try{
//			 List<String> keys = fetch(src, REGEX_VARIABLE, RegularUtil.MATCH_MODE_CONTAIN, 1);
//			 for(String key:keys){
//				 Object value = BasicUtil.zvl(BeanUtil.getPropertyValue(data, key));
//				 result = result.replace("{"+key+"}", value.toString());
//			 }
//		 }catch(Exception e){
//			 log.error(e);
//		 }
//		 return result;
//	}
}
