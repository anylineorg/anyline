package org.anyline.data.adapter;

import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;

public class MetadataAdapterHolder {

    //读取元数据时对应的列(如长度、小数位对应的列)
    /**
     * 具体数据库 数据类型-配置
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<TypeMetadata, TypeMetadata.Config>> typeConfigs = new LinkedHashMap<>();
    /**
     * 具体数据库 数据类型名称-配置
     * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<String, TypeMetadata.Config>> typeNameConfigs = new LinkedHashMap<>();
    /**
     * 具体数据库 数据类型大类-配置
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config>> typeCategoryConfigs = new LinkedHashMap<>();


    static {
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Config( 0, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Config(1,1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Config(0, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Config(1,1,1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.INT, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Config(1, 0, 0));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.DATE, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.TIME, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Config(1, 1, 1));
        reg(DatabaseType.NONE, TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Config(1, 1, 1));
    }
    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param type 数据类型
     * @param config 配置项
     * @return Config
     */
    public static TypeMetadata.Config reg(DatabaseType database, TypeMetadata type, TypeMetadata.Config config){
        LinkedHashMap<TypeMetadata, TypeMetadata.Config> configs = typeConfigs.get(database);
        if(null == configs){
            configs = new LinkedHashMap<>();
            typeConfigs.put(database, configs);
        }
        TypeMetadata.Config src = configs.get(type);
        if(null == src){
            src = config;
        }else{
            src.merge(config);
        }
        configs.put(type, src);

        String name = type.getName();
        reg(database, name, config);
        return src;
    }
    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param type 类型名称或别名
     * @param config 配置项
     * @return Config
     */
    public static TypeMetadata.Config reg(DatabaseType database, String type, TypeMetadata.Config config){
        LinkedHashMap<String, TypeMetadata.Config> configs = typeNameConfigs.get(database);
        if(null == configs){
            configs = new LinkedHashMap<>();
            typeNameConfigs.put(database, configs);
        }
        TypeMetadata.Config src = configs.get(type.toUpperCase());
        if(null == src){
            src = config;
        }else{
            src.merge(config);
        }
        configs.put(type.toUpperCase(), src);
        return src;
    }


    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param category 数据类型大类
     * @param config 配置项
     * @return Config
     */
    public static TypeMetadata.Config reg(DatabaseType database, TypeMetadata.CATEGORY category, TypeMetadata.Config config){
        LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config> configs = typeCategoryConfigs.get(database);
        if(null == configs){
            configs = new LinkedHashMap<>();
            typeCategoryConfigs.put(database, configs);
        }
        TypeMetadata.Config src = configs.get(category);
        if(null == src){
            src = config;
        }else{
            src.merge(config);
        }
        configs.put(category, src);
        return src;
    }

    /**
     * 根据类型获取元数据配置项
     * @param database 数据库类型
     * @param type 数据类型
     * @return config
     */
    public static TypeMetadata.Config get(DatabaseType database, TypeMetadata type){
        TypeMetadata.Config config = null;
        if(null != type) {
            LinkedHashMap<TypeMetadata, TypeMetadata.Config> configs = typeConfigs.get(database);
            if (null != configs) {
                config = configs.get(type);
            }
        }
        return config;
    }
    /**
     * 根据类型名称获取元数据配置项
     * @param database 数据库类型
     * @param type 数据类型名称
     * @return config
     */
    public static TypeMetadata.Config get(DatabaseType database, String type){
        TypeMetadata.Config config = null;
        if(null != type) {
            LinkedHashMap<String, TypeMetadata.Config> configs = typeNameConfigs.get(database);
            if (null != configs) {
                config = configs.get(type.toUpperCase());
            }
        }
        return config;
    }
    /**
     * 根据类型大类获取元数据配置项
     * @param database 数据库类型
     * @param category 数据类型大类
     * @return config
     */
    public static TypeMetadata.Config get(DatabaseType database, TypeMetadata.CATEGORY category){
        TypeMetadata.Config config = null;
        if(null != category) {
            LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config> configs = typeCategoryConfigs.get(database);
            if (null != configs) {
                config = configs.get(category);
            }
        }
        if(null == config){
            LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config> configs = typeCategoryConfigs.get(DatabaseType.NONE);
            if (null != configs) {
                config = configs.get(category);
            }
        }
        return config;
    }
}
