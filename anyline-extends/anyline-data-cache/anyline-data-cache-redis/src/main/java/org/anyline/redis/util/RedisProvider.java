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

package org.anyline.redis.util;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Hashtable;

@Component("anyline.redis.provider")
public class RedisProvider  implements CacheProvider {
    private static Hashtable<String, RedisProvider> instances = new Hashtable();
    @Autowired
    private RedisTemplate template;
    @Override
    public CacheElement get(String channel, String key) {
        return null;
    }

    @Override
    public void put(String channel, String key, Object value) {

    }

    @Override
    public boolean remove(String channel, String key) {
        return false;
    }

    @Override
    public boolean clear(String channel) {
        return false;
    }

    @Override
    public boolean clears() {
        return false;
    }

    @Override
    public HashSet<String> channels() {
        return null;
    }

    @Override
    public int getLvl() {
        return 2;
    }



    private String prefix = ConfigTable.getString("REDIS_PREFIX");
    public static RedisProvider newInstance(String key, String prefix){
        RedisProvider instance = instances.get(key);
        if(null == instance){
            instance = new RedisProvider();
            if(null != prefix) {
                instance.prefix = prefix;
            }
            if(null == instance.prefix){
                instance.prefix = "";
            }
            instances.put(key,instance);
        }
        return instance;
    }
    public static RedisProvider newInstance(String key){
        return newInstance(key,ConfigTable.getString("REDIS_PREFIX"));
    }
    public static RedisProvider getInstance(String key){
        return newInstance(key);
    }

    public static RedisProvider getInstance(){
        return newInstance(ConfigTable.getString("REDIS_PREFIX"));
    }
    public String key(String key){
        return prefix+key;
    }

}
