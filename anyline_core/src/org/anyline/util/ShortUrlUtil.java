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


package org.anyline.util;


/**
 * 短URL转换
 * 
 * @author once
 */
public class ShortUrlUtil {
//	static final Logger log = Logger.getLogger(ShortUrlUtil.class);
//	// [a - z, A - Z, 0 - 9] 
//	/**
//	 * 根据长地址ID，得到一个6位的 62 进制数,作为短地址
//	 * @param id
//	 * @return
//	 */
//	public ArrayList<Integer> base62(int id) {
//		ArrayList<Integer> value = new ArrayList<Integer>();
//		while (id > 0) {
//			int remainder = id % 62;
//			value.add(remainder);
//			id = id / 62;
//		}
//		
//		return value;
//	}
//	public static int base10(ArrayList<Integer> base62) {
//		//make sure the size of base62 is 6
//		for (int i = 1; i <= 6 - base62.size(); i++) {
//			base62.add(0, 0);
//		}
//		
//		int id = 0;
//		int size = base62.size();
//		for (int i = 0; i < size; i++) {
//			int value = base62.get(i);
//			id += (int) (value * Math.pow(62, size - i - 1));
//		}
//		
//		return id;
//	}
//	/** 
//     * @param args 
//     */  
//   public static void main(String[] args) {  
//  
//      String sLongUrl = "http://www..com" ; //长链接  
//      String[] aResult = shortUrl (sLongUrl);  
//      // 打印出结果  
//      for ( int i = 0; i < aResult. length ; i++) {  
//          System. out .println( "[" + i + "]" + aResult[i]);  
//      }  
//   }  
//  
//   public static String[] shortUrl(String url) {  
//      // 可以自定义生成 MD5 加密字符传前的混合 KEY  
//      String key = "mengdelong" ;  
//      // 要使用生成 URL 的字符  
//      String[] chars = new String[] { "a" , "b" , "c" , "d" , "e" , "f" , "g" , "h" ,  
//             "i" , "j" , "k" , "l" , "m" , "n" , "o" , "p" , "q" , "r" , "s" , "t" ,  
//             "u" , "v" , "w" , "x" , "y" , "z" , "0" , "1" , "2" , "3" , "4" , "5" ,  
//             "6" , "7" , "8" , "9" , "A" , "B" , "C" , "D" , "E" , "F" , "G" , "H" ,  
//             "I" , "J" , "K" , "L" , "M" , "N" , "O" , "P" , "Q" , "R" , "S" , "T" ,  
//             "U" , "V" , "W" , "X" , "Y" , "Z"  
//  
//      };  
//      // 对传入网址进行 MD5 加密  
//      String sMD5EncryptResult = MD5Util.crypto(key + url);  
//      String hex = sMD5EncryptResult;  
//  
//      String[] resUrl = new String[4];  
//      for ( int i = 0; i < 4; i++) {  
//  
//          // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算  
//          String sTempSubString = hex.substring(i * 8, i * 8 + 8);  
//  
//          // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用 long ，则会越界  
//          long lHexLong = 0x3FFFFFFF & Long.parseLong (sTempSubString, 16);  
//          String outChars = "" ;  
//          for ( int j = 0; j < 6; j++) {  
//             // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引  
//             long index = 0x0000003D & lHexLong;  
//             // 把取得的字符相加  
//             outChars += chars[( int ) index];  
//             // 每次循环按位右移 5 位  
//             lHexLong = lHexLong >> 5;  
//          }  
//          // 把字符串存入对应索引的输出数组  
//          resUrl[i] = outChars;  
//      }  
//      return resUrl;  
//   }  
}
