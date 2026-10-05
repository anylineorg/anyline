/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.jdbc.cassandra;

import org.anyline.adapter.DataWriter;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.DateUtil;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cassandra(CQL) 写入时的类型转换<br/>
 * 参考 https://cassandra.apache.org/doc/latest/cassandra/cql/index.html<br/>
 * CQL 字面量: 日期时间用字符串('yyyy-MM-dd HH:mm:ss'), uuid 用字符串字面量, 集合用 [list]/{set}/{k:v}
 */
public enum CassandraWriter {

    /**
     * date/time/timestamp: CQL 支持 'yyyy-MM-dd' 与 'yyyy-MM-dd HH:mm:ss.SSS' 字符串字面量
     */
    DateWriter(new Object[]{Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class, LocalDate.class, LocalDateTime.class, Instant.class}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(null == value) {
                return value;
            }
            if(null != placeholder && placeholder) {
                //占位符(PreparedStatement)模式下由驱动处理
                return value;
            }
            Date date = (Date) ConvertProxy.convert(value, Date.class, false);
            TypeMetadata.CATEGORY category = null != type ? type.getCategory() : null;
            if(category == TypeMetadata.CATEGORY.DATE) {
                return "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
            }
            if(category == TypeMetadata.CATEGORY.TIME) {
                return "'" + DateUtil.format(date, "HH:mm:ss") + "'";
            }
            return "'" + DateUtil.format(date) + "'";
        }
    }),

    /**
     * uuid/timeuuid: CQL 中 uuid 可用字符串字面量表示(隐式转换)
     */
    UUIDWriter(new Object[]{UUID.class}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(null == value) {
                return value;
            }
            if(null != placeholder && placeholder) {
                return value;
            }
            return "'" + value + "'";
        }
    }),

    /**
     * inet: CQL 中 inet 用 '127.0.0.1' 字符串字面量
     */
    InetWriter(new Object[]{InetAddress.class}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(null == value) {
                return value;
            }
            if(null != placeholder && placeholder) {
                return value;
            }
            return "'" + value + "'";
        }
    }),

    /**
     * 集合: list -> ['a','b']  set -> {'a','b'}  map -> {'k':'v'}
     */
    CollectionWriter(new Object[]{Collection.class, Map.class}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(null == value) {
                return value;
            }
            if(null != placeholder && placeholder) {
                return value;
            }
            if(value instanceof Map) {
                StringBuilder builder = new StringBuilder("{");
                boolean first = true;
                for(Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    if(!first) {
                        builder.append(",");
                    }
                    first = false;
                    builder.append(literal(entry.getKey())).append(":").append(literal(entry.getValue()));
                }
                builder.append("}");
                return builder.toString();
            }
            if(value instanceof Collection) {
                //set 用 {} list 用 []
                boolean set = value instanceof java.util.Set;
                String fr = set ? "{" : "[";
                String to = set ? "}" : "]";
                StringBuilder builder = new StringBuilder(fr);
                boolean first = true;
                for(Object item : (Collection<?>) value) {
                    if(!first) {
                        builder.append(",");
                    }
                    first = false;
                    builder.append(literal(item));
                }
                builder.append(to);
                return builder.toString();
            }
            return value;
        }
    })
    ;

    public Object[] supports() {
        return supports;
    }

    public DataWriter writer() {
        return writer;
    }

    private final Object[] supports;
    private final DataWriter writer;

    CassandraWriter(Object[] supports, DataWriter writer) {
        this.supports = supports;
        this.writer = writer;
    }

    /**
     * 集合元素字面量: 字符串/日期/uuid 加单引号并把 ' 转义成 ''(CQL 转义方式)
     */
    private static String literal(Object value) {
        if(null == value) {
            return "null";
        }
        if(value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }
}