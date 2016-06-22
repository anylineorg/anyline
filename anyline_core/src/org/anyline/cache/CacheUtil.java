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
package org.anyline.cache;

import java.util.Hashtable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;

public class CacheUtil {
	private static Logger LOG = Logger.getLogger(CacheUtil.class);
	private static CacheManager manager = null;
	private static Hashtable<String,Long> reflushFlag = new Hashtable<String,Long>();		//缓存刷新标记
	
	public static CacheManager create(){
		long fr = System.currentTimeMillis();
		if(null == manager){
			manager = CacheManager.create();
	    	if(ConfigTable.isDebug()){
	    		LOG.warn("[加载ehcache配置文件] [耗时:" + (System.currentTimeMillis() - fr) + "]");
	    		for(String name:manager.getCacheNames()){
	    			LOG.warn("[解析ehcache配置文件] [name:"+name+"]");
	    		}
	    	}
		}
		return manager;
	}
	
	public static Cache getCache(String channel){
		CacheManager manager = create();
		Cache cache = manager.getCache(channel);
		if(null == cache){
			create().addCache(channel);
		}
		cache = manager.getCache(channel);
		return cache;
	}
	public static Element getElement(String channel, String key){
		Element result = null;
		long fr = System.currentTimeMillis();
		Cache cache = getCache(channel);
		if(null != cache){
			result = cache.get(key);
			if(null == result){
		    	if(ConfigTable.isDebug()){
		    		LOG.warn("[缓存不存在] [cnannel:" + channel + "] [key:" + key + "]");
		    	}
				return null;
			}
			if(result.isExpired()){
		    	if(ConfigTable.isDebug()){
		    		LOG.warn("[缓存数据提取成功但已过期] [耗时:" + (System.currentTimeMillis()-fr) + "] [cnannel:" 
		    				+ channel + "] [key:" + key + "] [命中:" + result.getHitCount() + "] [生存:"
		    				+ (System.currentTimeMillis() - result.getCreationTime())/1000 + "/" + result.getTimeToLive() + "]");
		    	}
		    	result = null;
			}
			if(ConfigTable.isDebug()){
	    		LOG.warn("[缓存数据提取成功并有效] [耗时:"+(System.currentTimeMillis()-fr)+"] [cnannel:"  
	    				+ channel + "] [key:" + key + "] [命中:" + result.getHitCount() + "] [生存:"
	    				+ (System.currentTimeMillis() - result.getCreationTime())/1000 + "/" + result.getTimeToLive() + "]");
	    	}
		}
		return result;
	}
	public static Object getCache(String channel, String key){
		return getElement(channel, key);
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
	    		LOG.warn("[存储缓存数据] [channel:" + channel + "] [key:"+element.getObjectKey() + "]");
	    	}
		}
	}
	/**
     * 返回具体的方法全路径名称 参数
     * @param targetName 全路径
     * @param methodName 方法名称
     * @param arguments 参数
     * @return 完整方法名称
     */
    public static String getCacheKey(String targetName, String methodName, Object ... arguments) {
        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0)) {
            for (int i = 0; i < arguments.length; i++) {
                sb.append(".").append(arguments[i]);
            }
        }
        return sb.toString();
    }
    /**
     * 开始刷新
     * 如果不符合刷新条件返回false
     * @param key
     * @return
     */
    public static boolean start(String key){
    	Long fr = reflushFlag.get(key);
    	if(null == fr){
    		reflushFlag.put(key, System.currentTimeMillis());
    		return true;
    	}
    	int age = ConfigTable.getInt(key, 120) * 1000;
    	if(System.currentTimeMillis() - fr > age){
    		reflushFlag.put(key, System.currentTimeMillis());
    		return true;
    	}
    	return false;
    }
    /**
     * 刷新完成
     * @param key
     */
    public static void stop(String key){
    	reflushFlag.remove(key);
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
}
