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



package org.anyline.data.adapter;

import org.anyline.adapter.DataReader;
import org.anyline.metadata.type.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SystemDataReaderFactory {
    private static final Logger log = LoggerFactory.getLogger(SystemDataReaderFactory.class);

    // 读取数据库后类型转换
    // 以数据库类型作key,未指定数据库类型的以NONE作key
    // value.class或ColumnType或String ColumnType
    //从数据库中读取(有些数据库会返回特定类型如PgPoint,可以根据Class定位reader,有些数据库返回通用类型好byte[]需要根据ColumnType定位reader)
    protected static Map<DatabaseType, Map<Object, DataReader>> readers = new HashMap<>();

    /**
     * 注册 DataReader
     * @param dbt 仅针对type类型的数据库，如果不指定则通用
     * @param supports 支持的类型,或者通过reader.supports()返回 必选一个
     * @param reader DataReader 符合supports条件的数据 在从数据库中读取后由当前reader转换类型
     */
    public static void reg(DatabaseType dbt, Object[] supports, DataReader reader) {
        if(null == supports) {
            supports = reader.supports();
        }
        if(null == supports) {
            log.warn("[DataReader 未声明支持类型][cass:{}]", reader.getClass().getName());
            return;
        }
        if(null == dbt) {
            dbt = DatabaseType.NONE;
        }
        Map<Object, DataReader> map = readers.get(dbt);
        if(null == map) {
            map = new HashMap<>();
            readers.put(dbt, map);
        }
        for(Object support:supports) {
            if(support instanceof String) {
                support = ((String) support).toUpperCase();
            }
            map.put(support, reader);
        }
    }
    public static void reg(DatabaseType dbt, DataReader reader) {
        reg(dbt, null, reader);
    }
    public static void reg(Object[] supports, DataReader reader) {
        reg(DatabaseType.NONE, supports, reader);
    }
    public static void reg(DataReader reader) {
        reg(DatabaseType.NONE, null, reader);
    }

    public static DataReader reader(DatabaseType dbt, Object type) {
        if(null == dbt) {
            dbt = DatabaseType.NONE;
        }
        if(type instanceof String) {
            type = ((String) type).toUpperCase();
        }
        Map<Object, DataReader> map = readers.get(dbt);
        if (null != map) {
            return map.get(type);
        }
        return null;
    }
}
