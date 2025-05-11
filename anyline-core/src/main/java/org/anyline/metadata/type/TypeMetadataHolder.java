package org.anyline.metadata.type;

import java.util.LinkedHashMap;

public class TypeMetadataHolder {
    private static final LinkedHashMap<DatabaseType, LinkedHashMap<String, TypeMetadata>> metas = new LinkedHashMap<>();
    public static void reg(DatabaseType type, String name, TypeMetadata metadata) {
        LinkedHashMap<String, TypeMetadata> map = metas.computeIfAbsent(type, k -> new LinkedHashMap<>());
        if(null == map){
            map = new LinkedHashMap<>();
            metas.put(type, map);
        }
        map.put(name, metadata);
    }
    public static TypeMetadata get(DatabaseType type, String name) {
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
