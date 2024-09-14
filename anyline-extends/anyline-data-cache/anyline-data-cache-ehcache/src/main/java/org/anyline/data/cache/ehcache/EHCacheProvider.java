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
package org.anyline.data.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.proxy.CacheProxy;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

@Component("anyline.data.cache.provider")
public class EHCacheProvider implements CacheProvider {
	private static final Log log = LogProxy.get(EHCacheProvider.class);
	private CacheManager manager = null;
	private HashSet<String> channels = new HashSet<>();
	private Hashtable<String,Long> reflushFlag = new Hashtable<String,Long>();		// 缓存刷新标记
	public int getLvl() {
		return 1;
	}
	public EHCacheProvider() {
		CacheProxy.init(this);
	}
	public static InputStream getConfigFile() throws Exception{
		File file = null;
		String path = ConfigTable.getString("EHCACHE_CONFIG_PATH");
		log.info("[检测ehcache配置文件][path={}]", path);
		if(null != path) {
			file = new File(path);
			if (file.exists()) {
				log.info("[加载ehcache配置文件][path={}]", path);
				return new FileInputStream(file);
			}
		}
		if("jar".equals(ConfigTable.getProjectProtocol())) {
			path = FileUtil.merge(ConfigTable.getRoot(),"config", "ehcache.xml");
			log.info("[检测ehcache配置文件][path={}]", path);
			file = new File(path);
			if(file.exists()) {
				log.info("[加载ehcache配置文件][path={}]", path);
				return new FileInputStream(file);
			}
			path = FileUtil.merge(ConfigTable.getRoot(), "ehcache.xml");
			log.info("[检测ehcache配置文件][path={}]", path);
			file = new File(path);
			if(file.exists()) {
				log.info("[加载ehcache配置文件][path={}]", path);
				return new FileInputStream(file);
			}
			path = FileUtil.merge(ConfigTable.getClassPath(), "ehcache.xml");
			log.info("[检测ehcache配置文件][path={}]", path);
			file = new File(path);
			if(file.exists()) {
				log.info("[加载ehcache配置文件][path={}]", path);
				return new FileInputStream(file);
			}
			InputStream in = ConfigTable.class.getClassLoader().getResourceAsStream("/ehcache.xml");
			return in;
		}
		return null;
	}
	public CacheManager manager() {
		long fr = System.currentTimeMillis();
		if(null == manager) {
			try {
				InputStream in = getConfigFile();
				if(null != in) {
					manager = CacheManager.create(in);
				}else{
					manager = CacheManager.create();
				}
				String[] names = manager.getCacheNames();
				for(String name:names) {
					channels.add(name);
				}
				if (ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
					log.info("[加载ehcache配置文件][耗时:{}", System.currentTimeMillis() - fr);
					for (String name : manager.getCacheNames()) {
						log.info("[解析ehcache配置文件] [name:{}]", name);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return manager;
	}

	public Cache getCache(String channel) {
		CacheManager manager = manager();
		Cache cache = manager.getCache(channel);
		if(null == cache) {
			manager.addCache(channel);
		}
		cache = manager.getCache(channel);
		return cache;
	}
	public List<String> getCacheNames() {
		List<String> names = new ArrayList<>();
		CacheManager manager = manager();
		for(String name:manager.getCacheNames()) {
			names.add(name);
		}
		return names;
	}
	public List<Cache> getCaches() {
		List<Cache> caches = new ArrayList<Cache>();
		CacheManager manager = manager();
		for(String name:manager.getCacheNames()) {
			caches.add(manager.getCache(name));
		}
		return caches;
	}
	public CacheElement get(String channel, String key) {
		CacheElement result = new CacheElement();
		Element element = getElement(channel, key);
		if(null != element) {
			result.setCreateTime(element.getCreationTime());
			result.setValue(element.getObjectValue());
			result.setExpires(element.getTimeToLive());
		}
		return result;
	}
	public Element getElement(String channel, String key) {
		Element result = null;
		long fr = System.currentTimeMillis();
		Cache cache = getCache(channel);
		if(null != cache) {
			result = cache.get(key);
			if(null == result) {
		    	if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
		    		log.info("[缓存不存在][channel:{}][key:{}][生存:-1/{}]", channel, key,cache.getCacheConfiguration().getTimeToLiveSeconds());
		    	}
				return null;
			}
			if(result.isExpired()) {
		    	if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
		    		log.info("[缓存数据提取成功但已过期][耗时:{}][channel:{}][key:{}][命中:{}][生存:{}/{}]", System.currentTimeMillis()-fr, channel, key, result.getHitCount(), (System.currentTimeMillis() - result.getCreationTime())/1000, result.getTimeToLive());
		    	}
		    	result = null;
			}else{
				if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
		    		log.info("[缓存数据提取成功并有效][耗时:{}][channel:{}][key:{}][命中:{}][生存:{}/{}]", System.currentTimeMillis()-fr, channel, key, result.getHitCount(), (System.currentTimeMillis() - result.getCreationTime())/1000, result.getTimeToLive());
		    	}
			}
		}
		return result;
	}


	public void put(String channel, String key, Object value) {
		Element element = new Element(key, value);
		put(channel, element);
	}
	public void put(String channel, Element element) {
		Cache cache = getCache(channel);
		if(null != cache) {
			cache.put(element);
	    	if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
	    		log.info("[存储缓存数据][channel:{}][key:{}][生存:0/{}]",channel,element.getObjectKey(),cache.getCacheConfiguration().getTimeToLiveSeconds());
	    	}
		}
	}

	public boolean remove(String channel, String key) {
		boolean result = true;
		try{
			Cache cache = getCache(channel);
			if(null != cache) {
				cache.remove(key);
			}
	    	if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
	    		log.info("[删除缓存数据] [channel:{}][key:{}]",channel, key);
	    	}
		}catch(Exception e) {
			result = false;
		}
    	return result;
	}
	public boolean clear(String channel) {
		boolean result = true;
		try{
			CacheManager manager = manager();
			manager.removeCache(channel);
	    	if(ConfigTable.IS_DEBUG && log.isInfoEnabled()) {
	    		log.info("[清空缓存数据] [channel:{}]",channel);
	    	}
		}catch(Exception e) {
			result = false;
		}
		return result;
	}
	public boolean clears() {
		manager().clearAll();
		/*for(String channel:channels) {
			clear(channel);
		}*/
		return true;
	}
	public HashSet<String> channels() {
		return channels;
	}
}
