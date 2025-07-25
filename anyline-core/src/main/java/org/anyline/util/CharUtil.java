/*
 * Copyright 2006-2025 www.anyline.org
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
 
import java.util.Locale; 
 
public class CharUtil {
 
	private static int[] pyvalue = new int[] {-20319, -20317, -20304, -20295,
			-20292, -20283, -20265, -20257, -20242, -20230, -20051, -20036,
			-20032, -20026, -20002, -19990, -19986, -19982, -19976, -19805,
			-19784, -19775, -19774, -19763, -19756, -19751, -19746, -19741,
			-19739, -19728, -19725, -19715, -19540, -19531, -19525, -19515,
			-19500, -19484, -19479, -19467, -19289, -19288, -19281, -19275,
			-19270, -19263, -19261, -19249, -19243, -19242, -19238, -19235,
			-19227, -19224, -19218, -19212, -19038, -19023, -19018, -19006,
			-19003, -18996, -18977, -18961, -18952, -18783, -18774, -18773,
			-18763, -18756, -18741, -18735, -18731, -18722, -18710, -18697,
			-18696, -18526, -18518, -18501, -18490, -18478, -18463, -18448,
			-18447, -18446, -18239, -18237, -18231, -18220, -18211, -18201,
			-18184, -18183, -18181, -18012, -17997, -17988, -17970, -17964,
			-17961, -17950, -17947, -17931, -17928, -17922, -17759, -17752,
			-17733, -17730, -17721, -17703, -17701, -17697, -17692, -17683,
			-17676, -17496, -17487, -17482, -17468, -17454, -17433, -17427,
			-17417, -17202, -17185, -16983, -16970, -16942, -16915, -16733,
			-16708, -16706, -16689, -16664, -16657, -16647, -16474, -16470,
			-16465, -16459, -16452, -16448, -16433, -16429, -16427, -16423,
			-16419, -16412, -16407, -16403, -16401, -16393, -16220, -16216,
			-16212, -16205, -16202, -16187, -16180, -16171, -16169, -16158,
			-16155, -15959, -15958, -15944, -15933, -15920, -15915, -15903,
			-15889, -15878, -15707, -15701, -15681, -15667, -15661, -15659,
			-15652, -15640, -15631, -15625, -15454, -15448, -15436, -15435,
			-15419, -15416, -15408, -15394, -15385, -15377, -15375, -15369,
			-15363, -15362, -15183, -15180, -15165, -15158, -15153, -15150,
			-15149, -15144, -15143, -15141, -15140, -15139, -15128, -15121,
			-15119, -15117, -15110, -15109, -14941, -14937, -14933, -14930,
			-14929, -14928, -14926, -14922, -14921, -14914, -14908, -14902,
			-14894, -14889, -14882, -14873, -14871, -14857, -14678, -14674,
			-14670, -14668, -14663, -14654, -14645, -14630, -14594, -14429,
			-14407, -14399, -14384, -14379, -14368, -14355, -14353, -14345,
			-14170, -14159, -14151, -14149, -14145, -14140, -14137, -14135,
			-14125, -14123, -14122, -14112, -14109, -14099, -14097, -14094,
			-14092, -14090, -14087, -14083, -13917, -13914, -13910, -13907,
			-13906, -13905, -13896, -13894, -13878, -13870, -13859, -13847,
			-13831, -13658, -13611, -13601, -13406, -13404, -13400, -13398,
			-13395, -13391, -13387, -13383, -13367, -13359, -13356, -13343,
			-13340, -13329, -13326, -13318, -13147, -13138, -13120, -13107,
			-13096, -13095, -13091, -13076, -13068, -13063, -13060, -12888,
			-12875, -12871, -12860, -12858, -12852, -12849, -12838, -12831,
			-12829, -12812, -12802, -12607, -12597, -12594, -12585, -12556,
			-12359, -12346, -12320, -12300, -12120, -12099, -12089, -12074,
			-12067, -12058, -12039, -11867, -11861, -11847, -11831, -11798,
			-11781, -11604, -11589, -11536, -11358, -11340, -11339, -11324,
			-11303, -11097, -11077, -11067, -11055, -11052, -11045, -11041,
			-11038, -11024, -11020, -11019, -11018, -11014, -10838, -10832,
			-10815, -10800, -10790, -10780, -10764, -10587, -10544, -10533,
			-10519, -10331, -10329, -10328, -10322, -10315, -10309, -10307,
			-10296, -10281, -10274, -10270, -10262, -10260, -10256, -10254 };
	private static String[] pystr = new String[] {"a","ai","an","ang","ao",
			"ba","bai","ban","bang","bao","bei","ben","beng","bi",
			"bian","biao","bie","bin","bing","bo","bu","ca","cai",
			"can","cang","cao","ce","ceng","cha","chai","chan","chang",
			"chao","che","chen","cheng","chi","chong","chou","chu",
			"chuai","chuan","chuang","chui","chun","chuo","ci","cong",
			"cou","cu","cuan","cui","cun","cuo","da","dai","dan",
			"dang","dao","de","deng","di","dian","diao","die","ding",
			"diu","dong","dou","du","duan","dui","dun","duo","e","en",
			"er","fa","fan","fang","fei","fen","feng","fo","fou","fu",
			"ga","gai","gan","gang","gao","ge","gei","gen","geng",
			"gong","gou","gu","gua","guai","guan","guang","gui","gun",
			"guo","ha","hai","han","hang","hao","he","hei","hen",
			"heng","hong","hou","hu","hua","huai","huan","huang","hui",
			"hun","huo","ji","jia","jian","jiang","jiao","jie","jin",
			"jing","jiong","jiu","ju","juan","jue","jun","ka","kai",
			"kan","kang","kao","ke","ken","keng","kong","kou","ku",
			"kua","kuai","kuan","kuang","kui","kun","kuo","la","lai",
			"lan","lang","lao","le","lei","leng","li","lia","lian",
			"liang","liao","lie","lin","ling","liu","long","lou","lu",
			"lv","luan","lue","lun","luo","ma","mai","man","mang",
			"mao","me","mei","men","meng","mi","mian","miao","mie",
			"min","ming","miu","mo","mou","mu","na","nai","nan",
			"nang","nao","ne","nei","nen","neng","ni","nian","niang",
			"niao","nie","nin","ning","niu","nong","nu","nv","nuan",
			"nue","nuo","o","ou","pa","pai","pan","pang","pao","pei",
			"pen","peng","pi","pian","piao","pie","pin","ping","po",
			"pu","qi","qia","qian","qiang","qiao","qie","qin","qing",
			"qiong","qiu","qu","quan","que","qun","ran","rang","rao",
			"re","ren","reng","ri","rong","rou","ru","ruan","rui",
			"run","ruo","sa","sai","san","sang","sao","se","sen",
			"seng","sha","shai","shan","shang","shao","she","shen",
			"sheng","shi","shou","shu","shua","shuai","shuan","shuang",
			"shui","shun","shuo","si","song","sou","su","suan","sui",
			"sun","suo","ta","tai","tan","tang","tao","te","teng",
			"ti","tian","tiao","tie","ting","tong","tou","tu","tuan",
			"tui","tun","tuo","wa","wai","wan","wang","wei","wen",
			"weng","wo","wu","xi","xia","xian","xiang","xiao","xie",
			"xin","xing","xiong","xiu","xu","xuan","xue","xun","ya",
			"yan","yang","yao","ye","yi","yin","ying","yo","yong",
			"you","yu","yuan","yue","yun","za","zai","zan","zang",
			"zao","ze","zei","zen","zeng","zha","zhai","zhan","zhang",
			"zhao","zhe","zhen","zheng","zhi","zhong","zhou","zhu",
			"zhua","zhuai","zhuan","zhuang","zhui","zhun","zhuo","zi",
			"zong","zou","zu","zuan","zui","zun","zuo" };
 
 
	/** 
	 * 汉字转成ASCII码 
	 *  
	 * @param chs  chs
	 *            汉字字符串 
	 * @return int
	 */ 
	private static int getChsAscii(String chs) {
 
		int asc = 0; 
		try {
			byte[] bytes = chs.getBytes("gb2312"); 
			if (bytes == null || bytes.length > 2 || bytes.length <= 0) {
				throw new Exception("illegal resource string");
			} 
			if (bytes.length == 1) {
				asc = bytes[0]; 
			} 
			if (bytes.length == 2) {
				int hightByte = 256 + bytes[0]; 
				int lowByte = 256 + bytes[1]; 
				asc = (256 * hightByte + lowByte) - 256 * 256; 
			} 
		} catch (Exception e) {
		} 
		return asc; 
	} 
 
	/** 
	 * 单字解析 汉字转拼音
	 *  
	 * @param str  str
	 * @return String
	 */ 
	public static String convert(String str) {
 
		String result = null; 
		int ascii = getChsAscii(str); 
		if (ascii > 0 && ascii < 160) {
			result = String.valueOf((char) ascii); 
		} else {
			for (int i = (pyvalue.length - 1); i >= 0; i--) {
				if (pyvalue[i] <= ascii) {
					result = pystr[i]; 
					break; 
				} 
			} 
		} 
		return result; 
	} 
 
	/** 
	 * 获取汉字字符串的拼音 
	 *  
	 * @param chs  chs
	 * @return String
	 */ 
	public static String pinyin(String chs) {
		return pinyin(chs, false);
	} 
 
 
	/** 
	 * 获取汉字字符串的拼音 
	 *  
	 * @param chs  chs
	 * @param first 是否只取首字母 
	 * @return String
	 */ 
	public static String pinyin(String chs, boolean first) {
		chs = chs.toLowerCase(Locale.getDefault()); 
		String key, value;
		StringBuilder buffer = new StringBuilder(); 
		for (int i = 0; i < chs.length(); i++) {
			key = chs.substring(i, i + 1);
			value = convert(key); 
			if(first) {
				buffer.append(value.charAt(0)); 
			}else{
				buffer.append(value); 
			} 
		} 
		return buffer.toString(); 
	}

	/**
	 * 首字母大写
	 * @param origin 原文
	 * @return String
	 */
	public static String toUpperCaseHeader(String origin) {
		char[] cs=origin.toCharArray();
		cs[0] = Character.toUpperCase(cs[0]);
		return String.valueOf(cs);
	}

	/**
	 * 半角转全角的(SBC case) 注意$没有转成￥
	 *
	 * @param input 任意字符串
	 * @return 全角字符串
	 */
	public static String dbc2sbc(String input) {
		if (BasicUtil.isEmpty(input)) {
			return input;
		}
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 42) {//×
				c[i] = (char) 215;
				continue;
			}
			if (c[i] == 45) {//—
				c[i] = (char) 8212;
				continue;
			}
			if (c[i] == 39) {//‘
				c[i] = (char) 8216;
				continue;
			}
			if (c[i] == 34) {//“
				c[i] = (char) 8220;
				continue;
			}
			if (c[i] == 32) {//空格
				c[i] = (char) 12288;
				continue;
			}
			if (c[i] == 46) {//。
				c[i] = (char) 12290;
				continue;
			}
			if (c[i] == 91) {//【
				c[i] = (char) 12304;
				continue;
			}
			if (c[i] == 93) {//】
				c[i] = (char) 12305;
				continue;
			}
			if (c[i] < 127) {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角(DBC case)注意￥没有转成$ 顿号、没有转换
	 * @param input 任意字符串
	 * @return 半角字符串
	 */
	public static String sbc2dbc(String input) {
		if (BasicUtil.isEmpty(input)) {
			return input;
		}
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 215) {//×
				c[i] = (char) 42;
				continue;
			}
			if (c[i] == 8212) {//—
				c[i] = (char) 45;
				continue;
			}
			if (c[i] == 8216) {//‘
				c[i] = (char) 39;
				continue;
			}
			if (c[i] == 8220) {//“
				c[i] = (char) 34;
				continue;
			}
			if (c[i] == 12288) {//空格
				c[i] = (char) 32;
				continue;
			}
			if (c[i] == 12290) {//。
				c[i] = (char) 46;
				continue;
			}
			if (c[i] == 12304) {//【
				c[i] = (char) 91;
				continue;
			}
			if (c[i] == 12305) {//】
				c[i] = (char) 93;
				continue;
			}
			if (c[i] == 12308) {//〔-12308不是〔-65288
				c[i] = (char) 40;
				continue;
			}
			if (c[i] == 12309) {//〕-12309
				c[i] = (char) 41;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}
}
