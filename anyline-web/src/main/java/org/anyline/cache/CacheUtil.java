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
 *          
 */
package org.anyline.cache;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.anyline.config.db.OrderStore;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtil {
	private static final Logger log = LoggerFactory.getLogger(CacheUtil.class);
	private static CacheManager manager = null;
	private static Hashtable<String,Long> reflushFlag = new Hashtable<String,Long>();		//缓存刷新标记
	
	public static CacheManager createManager(){
		long fr = System.currentTimeMillis();
		if(null == manager){
			manager = CacheManager.create();
	    	if(ConfigTable.isDebug()){
	    		log.warn("[加载ehcache配置文件][耗时:{}",System.currentTimeMillis() - fr);
	    		for(String name:manager.getCacheNames()){
	    			log.warn("[解析ehcache配置文件] [name:{}]",name);
	    		}
	    	}
		}
		return manager;
	}
	
	public static Cache getCache(String channel){
		CacheManager manager = createManager();
		Cache cache = manager.getCache(channel);
		if(null == cache){
			manager.addCache(channel);
		}
		cache = manager.getCache(channel);
		return cache;
	}
	public static List<String> getCacheNames(){
		List<String> names = new ArrayList<String>();
		CacheManager manager = createManager();
		for(String name:manager.getCacheNames()){
			names.add(name);
		}
		return names;
	}
	public static List<Cache> getCaches(){
		List<Cache> caches = new ArrayList<Cache>();
		CacheManager manager = createManager();
		for(String name:manager.getCacheNames()){
			caches.add(manager.getCache(name));
		}
		return caches;
	}
	public static Element getElement(String channel, String key){
		Element result = null;
		long fr = System.currentTimeMillis();
		Cache cache = getCache(channel);
		if(null != cache){
			result = cache.get(key);
			if(null == result){
		    	if(ConfigTable.isDebug()){
		    		log.warn("[缓存不存在][cnannel:{}][key:{}][生存:-1/{}]",channel, key,cache.getCacheConfiguration().getTimeToLiveSeconds());
		    	}
				return null;
			}
			if(result.isExpired()){
		    	if(ConfigTable.isDebug()){
		    		log.warn("[缓存数据提取成功但已过期][耗时:{}][cnannel:{}][key:{}][命中:{}][生存:{}/{}]",System.currentTimeMillis()-fr,channel,key,result.getHitCount(),(System.currentTimeMillis() - result.getCreationTime())/1000,result.getTimeToLive());
		    	}
		    	result = null;
			}else{
				if(ConfigTable.isDebug()){
		    		log.warn("[缓存数据提取成功并有效][耗时:{}][cnannel:{}][key:{}][命中:{}][生存:{}/{}]",System.currentTimeMillis()-fr,channel,key,result.getHitCount(),(System.currentTimeMillis() - result.getCreationTime())/1000,result.getTimeToLive());
		    	}
			}
		}
		return result;
	}
	
	
	public static void put(String channel, String key, Object value){
		Element element = new Element(key, value);
		put(channel, element);
	}
	public static void put(String channel, Element element){
		Cache cache = getCache(channel);
		if(null != cache){
			cache.put(element);
	    	if(ConfigTable.isDebug()){
	    		log.warn("[存储缓存数据][channel:{}][key:{}][生存:0/{}]",channel,element.getObjectKey(),cache.getCacheConfiguration().getTimeToLiveSeconds());
	    	}
		}
	}
	
	public static boolean remove(String channel, String key){
		boolean result = true;
		try{
			Cache cache = getCache(channel);
			if(null != cache){
				cache.remove(key);
			}
	    	if(ConfigTable.isDebug()){
	    		log.warn("[删除缓存数据] [channel:{}][key:{}]",channel, key);
	    	}
		}catch(Exception e){
			result = false;
		}
    	return result;
	}
	public static boolean clear(String channel){
		boolean result = true;
		try{
			CacheManager manager = createManager();
			manager.removeCache(channel);
	    	if(ConfigTable.isDebug()){
	    		log.warn("[清空缓存数据] [channel:{}]",channel);
	    	}
		}catch(Exception e){
			result = false;
		}
		return result;
	}
	/*
	 * 辅助缓存刷新控制, N秒内只接收一次刷新操作
	 * 调用刷新方法前,先调用start判断是否可刷新,刷新完成后调用stop
	 * start与stop使用同一个key,
	 * 其中两次刷新间隔时间在anyline-config中设置单位秒<property key="key">sec</property>
	 */
    /**
     * 开始刷新
     * 如果不符合刷新条件返回false
     * @param key
     * @return
     */
    public static boolean start(String key, int sec){
    	boolean result = false;
    	Long fr = reflushFlag.get(key);
    	long age = -1;			//已生存
    	if(null == fr){
    		result = true;
    	}else{
	    	age = (System.currentTimeMillis() - fr) / 1000;
	    	if(age > sec){
	    		result = true;
	    	}
    	}
    	if(result){
    		reflushFlag.put(key, System.currentTimeMillis());
    		if(ConfigTable.isDebug()){
    			log.warn("[频率控制放行][key:{}][间隔:{}/{}]", key, age, sec);
    		}
    	}else{
    		if(ConfigTable.isDebug()){
    			log.warn("[频率控制拦截][key:{}][间隔:{}/{}]", key, age, sec);
    		}
    	}
    	return result;
    }
    public static boolean start(String key){
    	int period = ConfigTable.getInt(key, 120);		//两次刷新最小间隔
    	return start(key, period);
    }
    /**
     * 刷新完成
     * @param key
     */
    public static void stop(String key, int sec){
    	Long fr = reflushFlag.get(key);
    	if(null == fr){
    		if(ConfigTable.isDebug()){
    			log.warn("[频率控制还原完成 有可能key拼写有误][key:{}]",key);
    		}
    		return;
    	}
    	long age = (System.currentTimeMillis() - fr)/1000;			//已生存
    	
    	if(age > sec){
    		reflushFlag.remove(key);
    	}
		if(ConfigTable.isDebug()){
			log.warn("[频率控制还原完成][key:{}][间隔:{}/{}]", key, age, sec);
		}
    }
    public static void stop(String key){
    	int period = ConfigTable.getInt(key, 120);					//两次刷新最小间隔
    	stop(key,period);
    }
    public boolean isRun(String key){
    	if(null == reflushFlag.get(key)){
    		return false;
    	}
    	return true;
    }
    /**
     * 已执行时间
     * @param key
     * @return
     */
    public long getRunTime(String key){
    	long result = -1;
    	Long fr = reflushFlag.get(key);
    	if(null != fr){
    		return System.currentTimeMillis() - fr;
    	}
    	return result;
    }
    /**
     * 创建集中缓存的key
     * @param table
     * @param row
     * @return
     */
    public static String crateCachePrimaryKey(String table, DataRow row){
    	String key = table;
    	List<String> pks = row.getPrimaryKeys();
    	if(BasicUtil.isNotEmpty(pks) && null != row){
    		for(String pk:pks){
    			String value = row.getString(pk);
    			key += "|" + pk + "=" + value;
    		}
    	}
    	return key;
    }
    /**
	 * 创建cache key
	 * @param page 是否需要拼接分页下标
	 * @param src
	 * @param store
	 * @param conditions
	 * @return
	 */
	public static String createCacheElementKey(boolean page, boolean order, String src, ConfigStore store, String ... conditions){
		conditions = BasicUtil.compressionSpace(conditions);
		String result = src+"|";
		if(null != store){
			ConfigChain chain = store.getConfigChain();
			if(null != chain){
				List<Config> configs = chain.getConfigs();
				if(null != configs){
					for(Config config:configs){
						List<Object> values = config.getValues();
						if(null != values){
							result += config.toString() + "|";
						}
					}	
				}
			}
			PageNavi navi = store.getPageNavi();
			if(page && null != navi){
				result += "page=" + navi.getCurPage()+"|first=" + navi.getFirstRow() + "|last="+navi.getLastRow()+"|";
			}
			if(order){
				OrderStore orders = store.getOrders();
				if(null != orders){
					result += orders.getRunText("").toUpperCase() +"|";
				}
			}
		}
		if(null != conditions){
			for(String condition:conditions){
				if(BasicUtil.isNotEmpty(condition)){
					if(condition.trim().toUpperCase().startsWith("ORDER")){
						if(order){
							result += condition.toUpperCase() + "|";
						}
					}else{
						result += condition+"|";
					}
				}
			}
		}
		if(ConfigTable.isDebug()){
			log.warn("[create cache key][key:{}]",result);
		}
		return MD5Util.crypto(result);
	}
}
