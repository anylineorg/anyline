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

import java.util.List;

public interface Regular {
	public static enum FILTER_TYPE{WIPE,PICK};//过滤方式 WIPE:删除匹配项|PICK:保留匹配项
	public static enum MATCH_MODE{MATCH,PREFIX,CONTAIN};//匹配方式 MATCH:完全匹配 PREFIX:前缀匹配 CONTAIN:包含匹配
	public static enum PATTERN{
		/**   
		* 匹配email地址 
		* 格式: XXX@XXX.XXX.XX   
		* 匹配 : foo@bar.com 或 foobar@foobar.com.au 
		* 不匹配: foo@bar 或 $$$@bar.com   
		*/   
		EAMIL{
			public String getName(){return "邮箱";}
			public String getCode(){return "(?:\\w[-._\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3}$)";}
		}
		/**   
		* 匹配图象 
		* 格式: /相对路径/文件名.后缀 (后缀为gif,dmp,png)   
		* 匹配 : /forum/head_icon/admini2005111_ff.gif 或 admini2005111.dmp
		* 不匹配: c:/admins4512.gif   
		*/
		,IMG{
			public String getName(){return "图片";}
			public String getCode(){return "^(/{0,1}\\w){1,}\\.(gif|dmp|png|jpg|ico)$|^\\w{1,}\\.(gif|dmp|png|jpg|ico)$";}
		}
		/**   
		* 匹配匹配并提取url 
		* 格式: XXXX://XXX.XXX.XXX.XX/XXX.XXX?XXX=XXX   
		* 匹配 : http://www.anyline.org 或news://www
		* 提取(MatchResult matchResult=matcher.getMatch()):   
		*              matchResult.group(0)= http://www.anyline.org:8080/index.html?login=true   
		*              matchResult.group(1) = http   
		*              matchResult.group(2) = www.anyline.org   
		*              matchResult.group(3) = :8080   
		*              matchResult.group(4) = /index.html?login=true   
		* 不匹配: c:\window   
		*/   
		,URL{
			public String getName(){return "url";}
			public String getCode(){return "(\\w+)://([^/:]+)(:\\d*)?([^#\\s]*)";}
		}
		/**   
		* 匹配并提取http 
		* 格式: http://XXX.XXX.XXX.XX/XXX.XXX?XXX=XXX 或 ftp://XXX.XXX.XXX 或 https://XXX   
		* 匹配 : http://www.anyline.org:8080/index.html?login=true
		* 提取(MatchResult matchResult=matcher.getMatch()):   
		*              matchResult.group(0)= http://www.anyline.org:8080/index.html?login=true   
		*              matchResult.group(1) = http   
		*              matchResult.group(2) = www.anyline.org   
		*              matchResult.group(3) = :8080   
		*              matchResult.group(4) = /index.html?login=true   
		* 不匹配: news://www   
		*/   
		,HTTP_FTP{
			public String getName(){return "http|https|ftp";}
			public String getCode(){return "(http|https|ftp)://([^/:]+)(:\\d*)?([^#\\s]*)";}
		}
		/**   
		* 匹配日期 
		* 格式(首位不为0): XXXX-XX-XX 或 XXXX XX XX 或 XXXX-X-X  
		* 范围:1900--2099 
		* 匹配 : 2005-04-04 
		* 不匹配: 01-01-01   
		*/
		,DATE{
			public String getName(){return "日期";}
			public String getCode(){return "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$";}
		}
		/**
		 * 时间   20:20 | 20:20:20  | 20:20:20.999 
		 */
		,TIME{
			public String getName(){return "时间";}
			public String getCode(){return "(20|21|22|23|[0-1]\\d):[0-5]\\d(:[0-5]\\d(.\\d{1,3})?)?";}
		}
		,DATE_TIME{
			public String getName(){return "日期或日期时间或时间";}
			public String getCode(){return "^((?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29))?(\\s)?((20|21|22|23|[0-1]\\d):[0-5]\\d(:[0-5]\\d(.\\d{1,3})?)?)?$";}
		}
		/**   
		* 匹配电话 
		* 格式为: 0XXX-XXXXXX(10-13位首位必须为0) 或0XXX XXXXXXX(10-13位首位必须为0) 或 
		* (0XXX)XXXXXXXX(11-14位首位必须为0) 或 XXXXXXXX(6-8位首位不为0) 或   
		* XXXXXXXXXXX(11位首位不为0) 
		* 匹配 : 0371-123456 或 (0371)1234567 或 (0371)12345678 或 010-123456 或   
		* 010-12345678 或 12345678912 
		* 不匹配: 1111-134355 或 0123456789   
		*/   
		,PHONE{
			public String getName(){return "电话";}
			public String getCode(){return "^(?:0[0-9]{2,3}[-\\s]{1}|\\(0[0-9]{2,4}\\))[0-9]{6,8}$|^[1-9]{1}[0-9]{5,7}$|^[1-9]{1}[0-9]{10}$";}
		}  
		/**   
		* 匹配身份证 
		* 格式为: XXXXXXXXXX(10位) 或 XXXXXXXXXXXXX(13位) 或 XXXXXXXXXXXXXXX(15位) 或   
		* XXXXXXXXXXXXXXXXXX(18位) 
		* 匹配 : 0123456789123  
		* 不匹配: 0123456    
		*/  
		,ID_CARD{
			public String getName(){return "身份证";}
			public String getCode(){return "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$";}
		}
		,ZIP_CODE{
			public String getName(){return "邮编代码";}
			public String getCode(){return "^[0-9]{6}$";}
		}
		/**   
		* 不包括特殊字符的匹配 (字符串中不包括符号 数学次方号^ 单引号' 双引号" 分号; 逗号, 帽号: 数学减号- 右尖括号> 左尖括号<  反斜杠\ 即空格,制表符,回车符等 )
		*/ 
		,NONE_SPECIAL_CHAR{
			public String getName(){return "不包括特殊字符";}
			public String getCode(){return "^[^'\"\\;,:-<>\\s].+$";}
		}
		,NON_NEGATIVE_INTEGER{
			public String getName(){return "非负整数(正整数+0)";}
			public String getCode(){return "^\\d+$";}
		}
		/**
		 * html标签
		 */
		,HTML_TAG{
			public String getName(){return "html tag";}
			public String getCode(){return "<(.*?)[^>]*>.*?|<.*?/>";}
		}
		/**
		 * img标签 图片地址取下标2
		 */
		,HTML_TAG_IMG{
			public String getName(){return "html.img";}
			public String getCode(){return "(?i)<img.+?src[\\s]*=[\\s]*(['\"\\s])([\\S]+)\\1[\\s\\S]*?>";}
		}
		/**
		 * a标签 href:4 标签体:6
		 */
		,HTML_TAG_A{
			public String getName(){return "html.a";}
			public String getCode(){return "(<a\\s+([^>h]|h(?!ref\\s))*href[\\s+]?=[\\s+]?('|\"))([^(\\s+|'|\")]*)([^>]*>)(.*?)</a>";}
		}
		,CN{
			public String getName(){return "中文";}
			public String getCode(){return "[\u2E80-\uFE4F]";}
		}
		,EN{
			public String getName(){return "英文";}
			public String getCode(){return "[a-zA-Z]";}
		}
		,UPPER_CHAR{
			public String getName(){return "26个英文字母的大写组成的字符串";}
			public String getCode(){return "^[A-Z]+$";}
		}
		,LOWER_CHAR{
			public String getName(){return "26个英文字母的小写组成的字符串";}
			public String getCode(){return "^[a-z]+$";}
		}
		,INTEGER{
			public String getName(){return "整数 ";}
			public String getCode(){return "^-?\\d+$";}
		}
		,POSITIVE_INTEGER{
			public String getName(){return "正整数 ";}
			public String getCode(){return "^[0-9]*[1-9][0-9]*$";}
		}
		,NON_POSITIVE_INTEGER{
			public String getName(){return "非正整数(负整数 + 0)";}
			public String getCode(){return "^((-\\d+)|(0+))$";}
		}
		,NEGATIVE_INTEGER{
			public String getName(){return "负整数";}
			public String getCode(){return "^-[0-9]*[1-9][0-9]*$";}
		}
		,FLOAT{
			public String getName(){return "浮点数 ";}
			public String getCode(){return "^(-?\\d+)(\\.\\d+)?$";}
		}
		,NON_NEGATIVE_FLOAT{
			public String getName(){return "非负浮点数(正浮点数+0)";}
			public String getCode(){return "^\\d+(\\.\\d+)?$";}
		}
		,POSITIVE_FLOAT{
			public String getName(){return "正浮点数";}
			public String getCode(){return "^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$";}
		}
		,NON_POSITIVE_FLOAT{
			public String getName(){return "非正浮点数(负浮点数+0)";}
			public String getCode(){return "^((-\\d+(\\.\\d+)?)|(0+(\\.0+)?))$";}
		}
		,NEGATIVE_FLOAT{
			public String getName(){return "负浮点数";}
			public String getCode(){return "^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))$";}
		}
		;
		public abstract String getName();
		public abstract String getCode();
	};   

	/**
	* 匹配状态
	* @param src
	* @param regx
	* @return
	*/
	public boolean match(String src, String regx);

	/**
	* 提取子串
	* @param src	输入字符串
	* @param regx	表达式
	* @return
	*/
	public List<List<String>> fetch(String src, String regx) throws Exception;
	/**
	* 提取子串
	* @param src		输入字符串
	* @param regx		表达式
	* @param idx		指定提取位置
	* @return
	*/
	public List<String> fetch(String src, String regx, int idx) throws Exception;
	/**
	* 过滤  仅保留匹配项
	* @param src
	* @param regx
	* @return
	*/
	public List<String> pick(List<String> src, String regx);
	/**
	* 过滤 删除匹配项
	* @param src
	* @param regx
	* @return
	*/
	public List<String> wipe(List<String> src, String regx);

}
