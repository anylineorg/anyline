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

import org.anyline.adapter.DataReader;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cassandra(CQL) 读取时的类型转换<br/>
 * JDBC 驱动返回的 CQL 特有类型(uuid/inet/blob/timestamp)转成 anyline 可序列化的 Java 类型
 */
public enum CassandraReader {

    /**
     * uuid/timeuuid -> 字符串
     */
    UUIDReader(new Object[]{UUID.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof UUID) {
                return value.toString();
            }
            return value;
        }
    }),

    /**
     * inet -> 字符串(ip)
     */
    InetReader(new Object[]{InetAddress.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof InetAddress) {
                return ((InetAddress) value).getHostAddress();
            }
            return value;
        }
    }),

    /**
     * blob -> byte[]
     */
    ByteBufferReader(new Object[]{ByteBuffer.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer) value;
                byte[] bytes = new byte[buffer.remaining()];
                //不移动原buffer的position(只读副本)
                buffer.duplicate().get(bytes);
                return bytes;
            }
            return value;
        }
    }),

    /**
     * CQL timestamp 返回 Instant/LocalDateTime, 统一转成 java.util.Date 便于格式化
     */
    InstantReader(new Object[]{Instant.class, LocalDateTime.class, LocalDate.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof Instant) {
                return new java.util.Date(((Instant) value).toEpochMilli());
            }
            return value;
        }
    })
    ;

    public Object[] supports() {
        return supports;
    }

    public DataReader reader() {
        return reader;
    }

    private final Object[] supports;
    private final DataReader reader;

    CassandraReader(Object[] supports, DataReader reader) {
        this.supports = supports;
        this.reader = reader;
    }
}