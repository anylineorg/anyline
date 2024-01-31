package org.anyline.data.adapter;

import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;

public class MetadataAdapterHolder {

    //读取元数据时对应的列(如长度、小数位对应的列)
    /**
     * 当前adapter 数据类型-配置
     */
    private static LinkedHashMap<String, TypeMetadata.Config> typeConfigs = new LinkedHashMap<>();
    /**
     * 当前adapter 数据类型大类-配置
     */
    private static LinkedHashMap<DatabaseType,LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config>> typeCategoryConfigs = new LinkedHashMap<>();

    /**
     * 通用标准类型大类-配置
     */
    private static LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Config> standardCategoryConfigs = new LinkedHashMap<>();
}
