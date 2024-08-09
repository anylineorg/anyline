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

import org.anyline.adapter.DataWriter;
import org.anyline.metadata.type.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataWriterFactory {
    private static final Logger log = LoggerFactory.getLogger(DataWriterFactory.class);

    // 写入数据库前类型转换
    // 以数据库类型作key,未指定数据库类型的以NONE作key
    // value.class或ColumnType或String ColumnType
    protected static Map<DatabaseType, Map<Object, DataWriter>> writers = new HashMap<>();

    /**
     * 注册 DataWriter
     * @param dbt 仅针对type类型的数据库，如果不指定则通用
     * @param supports 支持的类型,或者通过writer.supports()返回 必选一个
     * @param writer DataWriter 符合supports条件的数据 在写入数据库之前由当前writer转换类型
     */
    public static void reg(DatabaseType dbt, Object[] supports, DataWriter writer) {
        if(null == supports) {
            supports = writer.supports();
        }
        if(null == supports) {
            log.warn("[DataWriter 未声明支持类型][cass:{}]", writer.getClass().getName());
            return;
        }
        if(null == dbt) {
            dbt = DatabaseType.NONE;
        }
        Map<Object, DataWriter> map = writers.get(dbt);
        if(null == map) {
            map = new HashMap<>();
            writers.put(dbt, map);
        }
        for(Object support:supports) {
            if(support instanceof String) {
                support = ((String) support).toUpperCase();
            }
            map.put(support, writer);
        }
    }
    public static void reg(DatabaseType dbt, DataWriter writer) {
        reg(dbt, null, writer);
    }
    public static void reg(Object[] supports, DataWriter writer) {
        reg(DatabaseType.NONE, supports, writer);
    }
    public static void reg(DataWriter writer) {
        reg(DatabaseType.NONE, null, writer);
    }

    public static DataWriter writer(DatabaseType dbt, Object type) {
        if(null == dbt) {
            dbt = DatabaseType.NONE;
        }
        if(type instanceof String) {
            type = ((String) type).toUpperCase();
        }
        Map<Object, DataWriter> map = writers.get(dbt);
        if (null != map) {
            return map.get(type);
        }
        return null;
    }
}
