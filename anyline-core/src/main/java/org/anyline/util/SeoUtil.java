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


package org.anyline.util; 
 
import java.util.ArrayList; 
import java.util.Collections; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
 
import org.anyline.util.regular.RegularUtil; 
 
public class SeoUtil { 
 
	private static final int MIN_BODY_LEN = 20;				//标签体内容长度达到多少时 插入关键词 
	private static final int KEYWORDS_DENSITY = 6;  		//关键词密度百分比上限 
	private static final int MIN_KEYWORDS_GAP = 50;			//相同关键词最小间距 
	private static final String SIGN_REPLACE_CHAR = "~";	//一个不常用字符,用来快速计算标点位置 
	private static final int SEARCH_SING_SCOPE = 10;		//在前后多大范围内查找标点 
	 
	/** 
	 * 插入关键词 
	 * @param src				源文  src				源文
	 * @param keys				关键词  keys				关键词
	 * @return return
	 * 优先靠近标点符号插入以避免破坏单词 
	 * 避免破坏原html标签 
	 * 按星期更新数据 
	 */ 
	public static String insertKeyword(String src, List<String> keys){ 
		List<Integer> idxList = new ArrayList<Integer> ();				//关键词插入位置 
		Map<Integer,String> idxKeyMap = new HashMap<Integer,String>();	//关键词插入位置对应(位置:关键词) 
 
		int srcLen = src.length(); 
		if(srcLen < MIN_BODY_LEN){ 
			return src; 
		} 
		//src = src.replace("<br/>", "\n"); 
		int week = DateUtil.getWeekOfYear() + DateUtil.month() ;//第几个星期+月份 
		int size = keys.size(); 
		for(int i=0; i<size; i++){ 
			String key = keys.get(i); 
			if(null == key || "".equals(key.trim())){ 
				continue; 
			} 
			key = key.trim(); 
			int bodyWordSize =srcLen/key.length();											//单词个数(默认单词长度与关键词长度相同) 
			int existKeyCount = BasicUtil.catSubCharCount(src, key);						//源文中已存在关键词个数 
			int insertKkeyCount = KEYWORDS_DENSITY * bodyWordSize / 100 - existKeyCount;	//插入关键词个数 
			if(0 == insertKkeyCount){ 
				continue; 
			} 
			int partLen = srcLen / insertKkeyCount;											//每段字数 
			for(int j=1; j<=insertKkeyCount; j++){ 
				int insertIdx = 0;	//插入位置 
				if(j%2 == 0){ 
					insertIdx = partLen * j + week * j; 
				}else{ 
					insertIdx = partLen * j - week * j; 
				} 
				 
				insertIdx = insertIdx - i*20; //错开不同的关键词 
				if(i>0){ 
					//粗略抵消一部分因插入前一个关键词造成的全文长度变化 
					insertIdx = insertIdx - (i+1) * keys.get(i-1).length() - j*key.length(); 
				} 
				insertIdx = Math.abs(insertIdx);  
				if(insertIdx > srcLen){ 
					insertIdx = insertIdx % srcLen; 
				} 
				 
				idxList.add(insertIdx); 
				idxKeyMap.put(insertIdx, key); 
				if(insertKkeyCount <= 1){ 
					//内容长度不足,避免关键词堆积 
					break;  
				} 
			}//end for 
		} 
		 
		src = insert(src,idxList,idxKeyMap); 
		//src = src.replace("\n","<br/>"); 
		return src; 
	} 
	/** 
	 * 返回指定位置附近标点位置,没有则返回原位置 
	 * @param idx  idx
	 * @return return
	 */ 
	private static int getNearSignIdx(List<Integer> idxs, int idx){ 
		for(int item: idxs){ 
			if(Math.abs(item-idx) <= SEARCH_SING_SCOPE){ 
				return item; 
			} 
		} 
		return idx; 
	} 
	/** 
	 * 搜索全部标点及>位置 
	 * @param src  src
	 * @return return
	 */ 
	private static List<Integer> getSingIdx(String src){ 
		List<Integer> idxs = new ArrayList<Integer>(); 
 
		String signTmpSrc = src.replaceAll("[,.;?!:'\"，。；？！：’”>]", SIGN_REPLACE_CHAR); 
		//String signTmpSrc = src.replaceAll("[\\pP‘’“”]", SIGN_REPLACE_CHAR); 
		int idx = -1; 
		while((idx = signTmpSrc.indexOf(SIGN_REPLACE_CHAR,idx+1)) != -1){ 
			idxs.add(0, idx); 
		} 
		return idxs; 
	} 
	/** 
	 * 插入关键词 
	 * @param src  src
	 * @param idxList  idxList
	 * @param idxKeyMap  idxKeyMap
	 * @return return
	 */ 
	private static String insert(String src, List<Integer> idxList, Map<Integer, String> idxKeyMap){ 
		List<Integer> signIdxList = getSingIdx(src);					//标点位置 
		Collections.sort(idxList); 
		int size = idxList.size(); 
		//从最后一个插入 
		for(int i=size-1; i>=0; i--){ 
			Integer idx = idxList.get(i); 
			String key =  idxKeyMap.get(idx); 
			int insertIdx = getNearSignIdx(signIdxList, idx);	//附近标点位置 
			insertIdx = insertIdx + 1;//插入标点后一个位置 
			if(!isNearExistKey(src, key, insertIdx)){//附近没有关键词执行插入并记数 
				//检查插入操作是否破坏了html标签 
				boolean isBreakTag = isBreakTag(src,insertIdx, key); 
				 
				if(!isBreakTag){ 
					src = src.substring(0,insertIdx) +"<b>" + key + "</b>" + src.substring(insertIdx); 
				} 
			} 
		} 
		return src; 
	} 
	/** 
	 * 判断在src的idx位置插入key是否会破坏html标签 
	 * @param src  src
	 * @param idx  idx
	 * @param key  key
	 * @return return
	 */ 
	private static boolean isBreakTag(String src, int idx, String key){ 
		boolean isBreak = false; 
		src = src.substring(0,idx) + key + src.substring(idx); 
		int fr = idx - 100; 
		int to = idx + key.length() + 100; 
		if(fr < 0){ 
			fr = 0; 
		} 
		if(to > src.length()){ 
			to = src.length(); 
		} 
		src = src.substring(fr,to); 
		String regx = "<[^>]*?" + key + "[^<]*?>"; 
		isBreak = RegularUtil.match(src, regx); 
		return isBreak; 
	} 
	/** 
	 * 附近是否存在关键词 
	 * @param src  src
	 * @param key  key
	 * @param idx  idx
	 * @return return
	 */ 
	private static boolean isNearExistKey(String src, String key, int idx){ 
		boolean keyExist = false;	// 
		int nearKeyIdx = src.indexOf(key, idx-key.length()-10);	//最近关键词位置 
		if(nearKeyIdx != -1 && Math.abs(nearKeyIdx - idx) <= MIN_KEYWORDS_GAP){ 
			keyExist = true; 
		}else{ 
			idx = idx + key.length() + 10; 
			if(idx >= src.length()){ 
				idx = src.length(); 
			} 
			String tmp = src.substring(0, idx); 
			nearKeyIdx = tmp.lastIndexOf(key); 
			if(nearKeyIdx != -1 && Math.abs(nearKeyIdx - idx) <= MIN_KEYWORDS_GAP){ 
				keyExist = true; 
			} 
		} 
		return keyExist; 
	} 
} 
