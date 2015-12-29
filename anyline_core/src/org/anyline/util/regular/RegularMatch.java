
package org.anyline.util.regular;

import java.util.ArrayList;
import java.util.List;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;


/**
 * 完全匹配模式
 *
 */
public class RegularMatch implements Regular{
	RegularMatch(){
		
	}
	private PatternCompiler patternCompiler = new Perl5Compiler();
	/**
	 * 匹配状态
	 * @param src
	 * @param regx
	 * @return
	 */
	public boolean match(String src, String regx){
		boolean result = false;
		try{
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			result = matcher.matches(src, pattern);
		}catch(Exception e){
			result = false;
		}
		return result;
	}
	
	/**
	 * 提取子串
	 * @param src	输入字符串
	 * @param regx	表达式
	 * @return
	 */
	public List<List<String>> fetch(String src, String regx){
		List<List<String>> list = new ArrayList<List<String>>();
		try{
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			PatternMatcherInput input = new PatternMatcherInput(src);
			while(matcher.matches(input, pattern)){
				MatchResult matchResult = matcher.getMatch();
				int groups = matchResult.groups();
				List<String> item = new ArrayList<String>();
				for(int i=0; i<=groups; i++){
					item.add(matchResult.group(i));
				}
				list.add(item);
			}
		}catch(Exception e){
			 
		}
		return list;
	}
	/**
	 * 提取子串
	 * @param src		输入字符串
	 * @param regx		表达式
	 * @param idx		指定提取位置
	 * @return
	 */
	public List<String> fetch(String src, String regx, int idx){
		List<String> list = new ArrayList<String>();
		
		try{
			Pattern pattern = patternCompiler.compile(regx, Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			PatternMatcherInput input = new PatternMatcherInput(src);
			while(matcher.matches(input, pattern)){
				MatchResult matchResult = matcher.getMatch();
				list.add(matchResult.group(idx));
			}
		}catch(Exception e){
			 
		}
		return list;
	}
	/**
	 * 过滤 保留匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public List<String> pick(List<String> src, String regx){
		List<String> list = new ArrayList<String>();
		for(String item : src){
			if(match(item, regx))
				list.add(item);
		}
		return list;
	}
	/**
	 * 过滤 删除匹配项
	 * @param src
	 * @param regx
	 * @return
	 */
	public List<String> wipe(List<String> src, String regx){
		List<String> list = new ArrayList<String>();
		for(String item : src){
			if(!match(item, regx))
				list.add(item);
		}
		return list;
	}
	
}
