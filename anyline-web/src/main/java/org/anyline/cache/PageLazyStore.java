package org.anyline.cache;

import java.util.Hashtable;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;

public class PageLazyStore {
	private static final Logger log = Logger.getLogger(PageLazyStore.class);
	private static Hashtable<String, Integer> lazyTotal = new Hashtable<String,Integer>();		//总数
	private static Hashtable<String, Long> lazyTime = new Hashtable<String,Long>();		//总数创建时间
	/**
	 * 缓存中的总条数
	 * @param key		
	 * @param period	过期时间(秒)
	 * @return
	 */
	public static int getTotal(String key, long period) {
		Long fr = lazyTime.get(key);		//创建时间
		long age = -1;
		if(null != fr){
			age = System.currentTimeMillis() - fr; 
			if(age > period){
				//过期
				lazyTotal.remove(key);
				lazyTime.remove(key);
				if(ConfigTable.isDebug()){
					log.warn("[记录总数过期] [key:" + key + "] [生存:" + age + "/" + period + "]");
				}
				return 0;
			}
		}
		Integer result = lazyTotal.get(key);
		if(ConfigTable.isDebug()){
			log.warn("[提取记录总数] [key:" + key + "] [total:" + result + "] [生存:" + age + "/" + period + "]");
		}
		if(null == result){
			return 0;
		}
		return result;
	}
	public static void setTotal(String key, int total) {
		Integer old = lazyTotal.get(key);
		if(null == old || old != total){
			//新计数 或 更新计数
			lazyTime.put(key, System.currentTimeMillis());
			lazyTotal.put(key, total);
			if(ConfigTable.isDebug()){
				log.warn("[重置记录总数] [key:"+key + "] [old:" + old +"]" + "[new:" + total + "]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[缓存记录总数] [key:"+key + "] [total:" + total + "]");
			}
		}
	}
}
