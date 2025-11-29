/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.adapter;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.DateFormatPattern;
import org.anyline.metadata.type.DatabaseOrigin;
import org.anyline.metadata.type.DatabaseType;

import java.util.LinkedHashMap;
import java.util.Map;

public class DateFormatPatternFactory {
    private static final Log log = LogProxy.get(DateFormatPatternFactory.class);

    //DatabaseOrigin || DatabaseType
    protected static Map<Object, Map<DateFormatPattern.META, DateFormatPattern>> metas = new LinkedHashMap<>();
    protected static Map<Object, Map<String, DateFormatPattern>> names = new LinkedHashMap<>();

    //DatabaseOrigin || DatabaseType
    public static void reg(Object type, DateFormatPattern pattern) {
        reg(type, pattern, true);
    }
    public static void reg(Object type, DateFormatPattern pattern, boolean overwrite) {
        DateFormatPattern.META meta = pattern.meta();
        Map<DateFormatPattern.META, DateFormatPattern> meta_maps = metas.get(type);
        if(null == meta_maps) {
            meta_maps = new LinkedHashMap<>();
            metas.put(type, meta_maps);
        }
        if(overwrite || !meta_maps.containsKey(meta)){
            meta_maps.put(meta, pattern);
        }
        Map<String, DateFormatPattern> name_maps = names.get(type);
        if(null == name_maps) {
            name_maps = new LinkedHashMap<>();
            names.put(type, name_maps);
        }
        if(overwrite || !name_maps.containsKey(pattern.define())){
            name_maps.put(pattern.define(), pattern);
        }

    }
    public static DateFormatPattern pattern(DatabaseType type, DateFormatPattern.META meta) {
        DateFormatPattern pattern = null;
        Map<DateFormatPattern.META, DateFormatPattern> maps = metas.get(type);
        if(null != maps) {
            pattern = maps.get(meta);
        }
        if(null == pattern) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = metas.get(origin);
                if(null != maps) {
                    pattern = maps.get(meta);
                }
            }
        }
        return pattern;
    }

    /**
     * 根据name 定位函数
     * @param type 数据库类型
     * @param name 名称
     * @return DateFormatPattern
     */
    public static DateFormatPattern pattern(DatabaseType type, String name) {
        DateFormatPattern pattern = null;
        Map<String, DateFormatPattern> maps = names.get(type);
        if(null != maps) {
            pattern = maps.get(name);
            if(null == pattern) {
                pattern = maps.get(name.toUpperCase());
            }
        }
        if(null == pattern) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = names.get(origin);
                if(null != maps) {
                    pattern = maps.get(name);
                    if(null == pattern) {
                        pattern = maps.get(name.toUpperCase());
                    }
                }
            }
        }
        return pattern;
    }

    public static Map<Object, Map<DateFormatPattern.META, DateFormatPattern>> metas(){
        return metas;
    }

    public static Map<DateFormatPattern.META, DateFormatPattern> metas(DatabaseType type){
        Map<DateFormatPattern.META, DateFormatPattern> maps = metas.get(type);
        if(null == maps || maps.isEmpty()) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = metas.get(origin);
            }
        }
        return maps;
    }

    public static Map<Object, Map<String, DateFormatPattern>> names(){
        return names;
    }

    public static Map<String, DateFormatPattern> names(DatabaseType type){
        Map<String, DateFormatPattern> maps = names.get(type);
        if(null == maps || maps.isEmpty()) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = names.get(origin);
            }
        }
        return maps;
    }
}
