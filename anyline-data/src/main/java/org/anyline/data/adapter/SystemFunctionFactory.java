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
    protected static Map<Object, Map<SystemFunction.META, SystemFunction>> functions = new HashMap<>();


    //DatabaseOrigin || DatabaseType
    public static void reg(Object type, SystemFunction function) {
        SystemFunction.META meta = function.meta();
        Map<SystemFunction.META, SystemFunction> maps = functions.get(type);
        if(null == maps) {
            maps = new HashMap<>();
            functions.put(type, maps);
        }
        maps.put(meta, function);
    }
    public static SystemFunction function(DatabaseType type, SystemFunction.META meta) {
        SystemFunction function = null;
        Map<SystemFunction.META, SystemFunction> maps = functions.get(type);
        if(null != maps) {
            function = maps.get(meta);
        }
        if(null == function) {
            DatabaseOrigin origin = type.origin();
            if(null != origin) {
                maps = functions.get(origin);
                if(null != maps) {
                    function = maps.get(meta);
                }
            }
        }
        return function;
    }
}
