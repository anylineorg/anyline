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
import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseOrigin;
import org.anyline.metadata.type.DatabaseType;

import java.util.HashMap;
import java.util.Map;

public class SystemFunctionFactory {
    private static final Log log = LogProxy.get(SystemFunctionFactory.class);

    //DatabaseOrigin || DatabaseType
    protected static Map<Object, Map<SystemFunction.META, SystemFunction>> metas = new HashMap<>();
    protected static Map<Object, Map<String, SystemFunction>> names = new HashMap<>();

    //DatabaseOrigin || DatabaseType
    public static void reg(Object type, SystemFunction function) {
        SystemFunction.META meta = function.meta();
        Map<SystemFunction.META, SystemFunction> meta_maps = metas.get(type);
        if(null == meta_maps) {
            meta_maps = new HashMap<>();
            metas.put(type, meta_maps);
        }
        meta_maps.put(meta, function);
        Map<String, SystemFunction> name_maps = names.get(type);
        if(null == name_maps) {
            name_maps = new HashMap<>();
            names.put(type, name_maps);
        }
        name_maps.put(function.define(), function);

    }
    public static SystemFunction function(DatabaseType type, SystemFunction.META meta) {
        SystemFunction function = null;
        Map<SystemFunction.META, SystemFunction> maps = metas.get(type);
        if(null != maps) {
            function = maps.get(meta);
        }
        if(null == function) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = metas.get(origin);
                if(null != maps) {
                    function = maps.get(meta);
                }
            }
        }
        return function;
    }

    /**
     * 根据name 定位函数
     * @param type 数据库类型
     * @param name 名称
     * @return SystemFunction
     */
    public static SystemFunction function(DatabaseType type, String name) {
        SystemFunction function = null;
        Map<String, SystemFunction> maps = names.get(type);
        if(null != maps) {
            function = maps.get(name);
            if(null == function) {
                function = maps.get(name.toUpperCase());
            }
        }
        if(null == function) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = names.get(origin);
                if(null != maps) {
                    function = maps.get(name);
                    if(null == function) {
                        function = maps.get(name.toUpperCase());
                    }
                }
            }
        }
        return function;
    }

    public static Map<Object, Map<SystemFunction.META, SystemFunction>> metas(){
        return metas;
    }

    public static Map<SystemFunction.META, SystemFunction> metas(DatabaseType type){
        Map<SystemFunction.META, SystemFunction> maps = metas.get(type);
        if(null == maps || maps.isEmpty()) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = metas.get(origin);
            }
        }
        return maps;
    }

    public static Map<Object, Map<String, SystemFunction>> names(){
        return names;
    }

    public static Map<String, SystemFunction> names(DatabaseType type){
        Map<String, SystemFunction> maps = names.get(type);
        if(null == maps || maps.isEmpty()) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = names.get(origin);
            }
        }
        return maps;
    }
}
