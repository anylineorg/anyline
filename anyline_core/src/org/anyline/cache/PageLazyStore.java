package org.anyline.cache;

import java.util.Hashtable;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;

public class PageLazyStore {
	private static Logger LOG = Logger.getLogger(PageLazyStore.class);
	private static Hashtable<String, Integer> lazyTotal = new Hashtable<String,Integer>();		//总数
	private static Hashtable<String, Long> lazyTime = new Hashtable<String,Long>();		//总数创建时间
	/**
	 * 缓存中的总条数
	 * @param key		
	 * @param period	过期时间
	 * @return
	 */
	public static int getTotal(String key, int period) {
		Long fr = lazyTime.get(key);		//创建时间
		if(null != fr){
			if((System.currentTimeMillis() - fr)/1000 > period){
				//过期
				lazyTotal.remove(key);
				lazyTime.remove(key);
				if(ConfigTable.isDebug()){
					LOG.warn("记录总数lazy过期 key:"+key);
				}
				return 0;
			}
		}
		Integer result = lazyTotal.get(key);
		if(ConfigTable.isDebug()){
			LOG.warn("提取记录总数lazy key"+key+" total:"+result);
		}
		if(null == result){
			return 0;
		}
		return result;
	}
	public static void setTotal(String key, int total) {
		lazyTotal.put(key, total);
		if(ConfigTable.isDebug()){
			LOG.warn("设置记录总数lazy key:"+key + " total:"+total);
		}
	}
}
