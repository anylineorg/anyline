package org.anyline.jdbc.cache; 
 
import java.util.Hashtable; 
 
import org.anyline.util.ConfigTable; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class PageLazyStore { 
	private static final Logger log = LoggerFactory.getLogger(PageLazyStore.class); 
	private static Hashtable<String, Integer> lazyTotal = new Hashtable<String,Integer>();		//总数 
	private static Hashtable<String, Long> lazyTime = new Hashtable<String,Long>();		//总数创建时间 
	/** 
	 * 缓存中的总条数 
	 * @param key		  key
	 * @param period	过期时间(秒)  period	过期时间(秒)
	 * @return return
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
				if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
					log.warn("[记录总数过期][key:{}][生存:{}/{}]", key, age, period); 
				} 
				return 0; 
			} 
		} 
		Integer result = lazyTotal.get(key); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[提取记录总数][key:{}][total:{}][生存:{}/{}]", key, result, age, period); 
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
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[重置记录总数][key:{}][old:{}]" + "[new:{}]", key, old, total); 
			} 
		}else{ 
			if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
				log.warn("[缓存记录总数][key:{}][total:{}]", key, total); 
			} 
		} 
	} 
} 
