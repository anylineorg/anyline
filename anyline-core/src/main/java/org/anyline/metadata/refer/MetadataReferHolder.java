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

package org.anyline.metadata.refer;

import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;

public class MetadataReferHolder {

    //读取元数据时对应的列(如长度、小数位对应的列)
    /**
     * 具体数据库 数据类型-配置
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<TypeMetadata, TypeMetadata.Refer>> typeRefers = new LinkedHashMap<>();
    /**
     * 具体数据库 数据类型名称-配置
     * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<String, TypeMetadata.Refer>> typeNameRefers = new LinkedHashMap<>();
    /**
     * 具体数据库 数据类型大类-配置
     */
    private static LinkedHashMap<DatabaseType, LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Refer>> typeCategoryRefers = new LinkedHashMap<>();

    static {
        reg(DatabaseType.COMMON, org.anyline.metadata.type.TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Refer( 0, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Refer(1,1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Refer(0, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Refer(1,1,1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.INT, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Refer(1, 0, 0));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.DATE, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.TIME, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Refer(1, 1, 1));
        reg(DatabaseType.COMMON, TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Refer(1, 1, 1));
    }

    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param type 数据类型
     * @param refer 配置项 主要包括数据类型规则以及是否忽略长度、精度
     * @return Config
     */
    public static TypeMetadata.Refer reg(DatabaseType database, TypeMetadata type, TypeMetadata.Refer refer) {
        LinkedHashMap<TypeMetadata, TypeMetadata.Refer> refers = typeRefers.get(database);
        if(null == refers) {
            refers = new LinkedHashMap<>();
            typeRefers.put(database, refers);
        }
        TypeMetadata.Refer src = refers.get(type);
        if(null == src) {
            src = new TypeMetadata.Refer();
        }
        src.merge(refer);
        refers.put(type, src);

        String name = type.getName();
        reg(database, name, refer);
        return src;
    }

    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param type 类型名称或别名
     * @param refer 配置项 主要包括数据类型规则以及是否忽略长度、精度
     * @return Config
     */
    public static TypeMetadata.Refer reg(DatabaseType database, String type, TypeMetadata.Refer refer) {
        LinkedHashMap<String, TypeMetadata.Refer> refers = typeNameRefers.get(database);
        if(null == refers) {
            refers = new LinkedHashMap<>();
            typeNameRefers.put(database, refers);
        }
        TypeMetadata.Refer src = refers.get(type.toUpperCase());
        if(null == src) {
            src = new TypeMetadata.Refer();
        }
        src.merge(refer);
        refers.put(type.toUpperCase(), src);
        return src;
    }

    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param database 数据库类型
     * @param category 数据类型大类
     * @param refer 配置项 主要包括数据类型规则以及是否忽略长度、精度
     * @return Config
     */
    public static TypeMetadata.Refer reg(DatabaseType database, TypeMetadata.CATEGORY category, TypeMetadata.Refer refer) {
        LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Refer> refers = typeCategoryRefers.get(database);
        if(null == refers) {
            refers = new LinkedHashMap<>();
            typeCategoryRefers.put(database, refers);
        }
        TypeMetadata.Refer src = refers.get(category);
        if(null == src) {
            src = refer;
        }else{
            src.merge(refer);
        }
        refers.put(category, src);
        return src;
    }

    /**
     * 根据类型获取元数据配置项
     * @param database 数据库类型
     * @param type 数据类型
     * @return config
     */
    public static TypeMetadata.Refer get(DatabaseType database, TypeMetadata type) {
        TypeMetadata.Refer refer = null;
        if(null != type) {
            LinkedHashMap<TypeMetadata, TypeMetadata.Refer> refers = typeRefers.get(database);
            if (null != refers) {
                refer = refers.get(type);
            }
        }
        return refer;
    }

    /**
     * 根据类型名称获取元数据配置项
     * @param database 数据库类型
     * @param type 数据类型名称
     * @return config
     */
    public static TypeMetadata.Refer get(DatabaseType database, String type) {
        TypeMetadata.Refer refer = null;
        if(null != type) {
            LinkedHashMap<String, TypeMetadata.Refer> refers = typeNameRefers.get(database);
            if (null != refers) {
                refer = refers.get(type.toUpperCase());
            }
        }
        return refer;
    }

    /**
     * 根据类型大类获取元数据配置项
     * @param database 数据库类型
     * @param category 数据类型大类
     * @return config
     */
    public static TypeMetadata.Refer get(DatabaseType database, TypeMetadata.CATEGORY category) {
        TypeMetadata.Refer refer = null;
        if(null != category) {
            LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Refer> refers = typeCategoryRefers.get(database);
            if (null != refers) {
                refer = refers.get(category);
            }
        }
        if(null == refer) {
            LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Refer> refers = typeCategoryRefers.get(DatabaseType.NONE);
            if (null != refers) {
                refer = refers.get(category);
            }
        }
        return refer;
    }

    /**
     * @param database 数据库类型
     * @param type TypeMetadata
     * @return int
     */
    public static int ignoreLength(DatabaseType database, TypeMetadata type) {
        if(null == type) {
            return -1;
        }
        int result = -1;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带
		4.配置类-数据类型大类
		 */
        //1.配置类 数据类型
        TypeMetadata.Refer refer = MetadataReferHolder.get(database, type);
        if(null != refer) {
            result = refer.ignoreLength();
        }
        //2.配置类-数据类型名称
        if(result == -1) {
            //根据数据类型名称
            refer = MetadataReferHolder.get(database, type.getName());
            if(null != refer) {
                result = refer.ignoreLength();
            }
        }
        //3.数据类型自带
        if(result ==-1) {
            result = type.ignoreLength();
        }
        //4.配置类-数据类型大类
        if(result ==-1) {
            refer = MetadataReferHolder.get(database, type.getCategory());
            if(null != refer) {
                result = refer.ignoreLength();
            }
        }
        return result;
    }

    /**
     * @param database 数据库类型
     * @param type TypeMetadata
     * @return int
     */
    public static int ignorePrecision(DatabaseType database, TypeMetadata type) {
        if(null == type) {
            return -1;
        }
        int result = -1;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带
		4.配置类-数据类型大类
		 */
        //1.配置类 数据类型
        TypeMetadata.Refer refer = MetadataReferHolder.get(database, type);
        if(null != refer) {
            result = refer.ignorePrecision();
        }
        //2.配置类-数据类型名称
        if(result == -1) {
            //根据数据类型名称
            refer = MetadataReferHolder.get(database, type.getName());
            if(null != refer) {
                result = refer.ignorePrecision();
            }
        }
        //3.数据类型自带
        if(result ==-1) {
            result = type.ignorePrecision();
        }
        //4.配置类-数据类型大类
        if(result ==-1) {
            refer = MetadataReferHolder.get(database, type.getCategory());
            if(null != refer) {
                result = refer.ignorePrecision();
            }
        }
        return result;
    }

    /**
     * @param database 数据库类型
     * @param type TypeMetadata
     * @return int
     */
    public static int ignoreScale(DatabaseType database, TypeMetadata type) {
        if(null == type) {
            return -1;
        }
        int result = -1;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带
		4.配置类-数据类型大类
		 */
        //1.配置类 数据类型
        TypeMetadata.Refer refer = MetadataReferHolder.get(database, type);
        if(null != refer) {
            result = refer.ignoreScale();
        }
        //2.配置类-数据类型名称
        if(result == -1) {
            //根据数据类型名称
            refer = MetadataReferHolder.get(database, type.getName());
            if(null != refer) {
                result = refer.ignoreScale();
            }
        }
        //3.数据类型自带
        if(result ==-1) {
            result = type.ignorePrecision();
        }
        //4.配置类-数据类型大类
        if(result ==-1) {
            refer = MetadataReferHolder.get(database, type.getCategory());
            if(null != refer) {
                result = refer.ignoreScale();
            }
        }
        return result;
    }

    /**
     * @param database 数据库类型
     * @param type TypeMetadata
     * @return String
     */
    public static String formula(DatabaseType database, TypeMetadata type) {
        if(null == type) {
            return null;
        }
        String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带
		4.配置类-数据类型大类
		 */
        //1.配置类 数据类型
        TypeMetadata.Refer refer = MetadataReferHolder.get(database, type);
        if(null != refer) {
            result = refer.getFormula();
        }
        //2.配置类-数据类型名称
        if(null == result) {
            //根据数据类型名称
            refer = MetadataReferHolder.get(database, type.getName());
            if(null != refer) {
                result = refer.getFormula();
            }
        }
        //3.数据类型自带
        if(null == result) {
            result = type.formula();
        }
        //4.配置类-数据类型大类
        if(null == result) {
            refer = MetadataReferHolder.get(database, type.getCategory());
            if(null != refer) {
                result = refer.getFormula();
            }
        }
        return result;
    }

    /**
     * 原数据类型(不带长度等参数)
     * @param database 数据库类型
     * @param type TypeMetadata
     * @return String
     */
    public static String metadata(DatabaseType database, TypeMetadata type) {
        if(null == type) {
            return null;
        }
        String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带
		4.配置类-数据类型大类
		 */
        //1.配置类 数据类型
        TypeMetadata.Refer refer = MetadataReferHolder.get(database, type);
        if(null != refer) {
            result = refer.getMeta();
        }
        //2.配置类-数据类型名称
        if(null == result) {
            //根据数据类型名称
            refer = MetadataReferHolder.get(database, type.getName());
            if(null != refer) {
                result = refer.getMeta();
            }
        }
        //3.数据类型自带
        if(null == result) {
            result = type.getName();
        }
        //4.配置类-数据类型大类
        if(null == result) {
            refer = MetadataReferHolder.get(database, type.getCategory());
            if(null != refer) {
                result = refer.getMeta();
            }
        }
        return result;
    }
}
