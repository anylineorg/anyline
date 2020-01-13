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
package org.anyline.cache.ecache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Component("anyline.cache.provider")
public class ECacheProvider implements CacheProvider {
	private static final Logger log = LoggerFactory.getLogger(ECacheProvider.class);
	private CacheManager manager = null;
	private Hashtable<String,Long> reflushFlag = new Hashtable<String,Long>();		//缓存刷新标记
	
	public CacheManager createManager(){
		long fr = System.currentTimeMillis();
		if(null == manager){
			manager = CacheManager.create();
	    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
	    		log.warn("[加载ehcache配置文件][耗时:{}",System.currentTimeMillis() - fr);
	    		for(String name:manager.getCacheNames()){
	    			log.warn("[解析ehcache配置文件] [name:{}]",name);
	    		}
	    	}
		}
		return manager;
	}

	public Cache getCache(String channel){
		CacheManager manager = createManager();
		Cache cache = manager.getCache(channel);
		if(null == cache){
			manager.addCache(channel);
		}
		cache = manager.getCache(channel);
		return cache;
	}
	public List<String> getCacheNames(){
		List<String> names = new ArrayList<String>();
		CacheManager manager = createManager();
		for(String name:manager.getCacheNames()){
			names.add(name);
		}
		return names;
	}
	public List<Cache> getCaches(){
		List<Cache> caches = new ArrayList<Cache>();
		CacheManager manager = createManager();
		for(String name:manager.getCacheNames()){
			caches.add(manager.getCache(name));
		}
		return caches;
	}
	public CacheElement get(String channel, String key){
		CacheElement result = new CacheElement();
		Element element = getElement(channel, key);
		if(null != element){
			result.setCreateTime(element.getCreationTime());
			result.setValue(element.getObjectValue());
			result.setExpires(element.getTimeToLive());
		}
		return result;
	}
	public Element getElement(String channel, String key){
		Element result = null;
		long fr = System.currentTimeMillis();
		Cache cache = getCache(channel);
		if(null != cache){
			result = cache.get(key);
			if(null == result){
		    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
		    		log.warn("[缓存不存在][cnannel:{}][key:{}][生存:-1/{}]",channel, key,cache.getCacheConfiguration().getTimeToLiveSeconds());
		    	}
				return null;
			}
			if(result.isExpired()){
		    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
		    		log.warn("[缓存数据提取成功但已过期][耗时:{}][cnannel:{}][key:{}][命中:{}][生存:{}/{}]",System.currentTimeMillis()-fr,channel,key,result.getHitCount(),(System.currentTimeMillis() - result.getCreationTime())/1000,result.getTimeToLive());
		    	}
		    	result = null;
			}else{
				if(ConfigTable.isDebug() && log.isWarnEnabled()){
		    		log.warn("[缓存数据提取成功并有效][耗时:{}][cnannel:{}][key:{}][命中:{}][生存:{}/{}]",System.currentTimeMillis()-fr,channel,key,result.getHitCount(),(System.currentTimeMillis() - result.getCreationTime())/1000,result.getTimeToLive());
		    	}
			}
		}
		return result;
	}
	
	
	public void put(String channel, String key, Object value){
		Element element = new Element(key, value);
		put(channel, element);
	}
	public void put(String channel, Element element){
		Cache cache = getCache(channel);
		if(null != cache){
			cache.put(element);
	    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
	    		log.warn("[存储缓存数据][channel:{}][key:{}][生存:0/{}]",channel,element.getObjectKey(),cache.getCacheConfiguration().getTimeToLiveSeconds());
	    	}
		}
	}
	
	public boolean remove(String channel, String key){
		boolean result = true;
		try{
			Cache cache = getCache(channel);
			if(null != cache){
				cache.remove(key);
			}
	    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
	    		log.warn("[删除缓存数据] [channel:{}][key:{}]",channel, key);
	    	}
		}catch(Exception e){
			result = false;
		}
    	return result;
	}
	public boolean clear(String channel){
		boolean result = true;
		try{
			CacheManager manager = createManager();
			manager.removeCache(channel);
	    	if(ConfigTable.isDebug() && log.isWarnEnabled()){
	    		log.warn("[清空缓存数据] [channel:{}]",channel);
	    	}
		}catch(Exception e){
			result = false;
		}
		return result;
	} 
}
