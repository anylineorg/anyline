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

package org.anyline.metadata.type;

import java.util.LinkedHashMap;

public class TypeMetadataHolder {
    private static final LinkedHashMap<DatabaseType, LinkedHashMap<String, TypeMetadata>> metas = new LinkedHashMap<>();

    public static void reg(DatabaseType type, String name, TypeMetadata metadata, boolean override) {
        if(null == name){
            return;
        }
        name = name.toUpperCase();
        LinkedHashMap<String, TypeMetadata> map = metas.computeIfAbsent(type, k -> new LinkedHashMap<>());
        if(override || !map.containsKey(name)) {
            map.put(name, metadata);
        }
    }
    public static TypeMetadata get(DatabaseType type, String name) {
        if(null == name){
            return null;
        }
        name = name.toUpperCase();
        LinkedHashMap<String, TypeMetadata> map = metas.get(type);
        if(null == map){
            return null;
        }
        return map.get(name);
    }
    public static LinkedHashMap<String, TypeMetadata> gets(DatabaseType type){
        return metas.get(type);
    }
}
