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



package org.anyline.data.cache;

import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
 
public class PageLazyStore {
	private static final Logger log = LoggerFactory.getLogger(PageLazyStore.class); 
	private static Hashtable<String, Long> lazyTotal = new Hashtable();		// 总数
	private static Hashtable<String, Long> lazyTime = new Hashtable();		// 总数创建时间
	/** 
	 * 缓存中的总条数 
	 * @param key		  key
	 * @param period	过期时间(秒)  period	过期时间(秒)
	 * @return int
	 */ 
	public static long getTotal(String key, long period) {
		Long fr = lazyTime.get(key);		// 创建时间 
		long age = -1; 
		if(null != fr) {
			age = System.currentTimeMillis() - fr;  
			if(age > period) {
				// 过期 
				lazyTotal.remove(key); 
				lazyTime.remove(key); 
				if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
					log.info("[记录总数过期][key:{}][生存:{}/{}]", key, age, period);
				} 
				return 0; 
			} 
		} 
		Long result = lazyTotal.get(key);
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
			log.info("[提取记录总数][key:{}][total:{}][生存:{}/{}]", key, result, age, period);
		} 
		if(null == result) {
			return 0; 
		} 
		return result; 
	} 
	public static void setTotal(String key, long total) {
		Long old = lazyTotal.get(key);
		if(null == old || old != total) {
			// 新计数 或 更新计数 
			lazyTime.put(key, System.currentTimeMillis());
			lazyTotal.put(key, total);
			if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
				log.info("[重置记录总数][key:{}][old:{}]" + "[new:{}]", key, old, total);
			} 
		}else{
			if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
				log.info("[缓存记录总数][key:{}][total:{}]", key, total);
			} 
		} 
	} 
} 
