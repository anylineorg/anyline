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

package org.anyline.data.jdbc.clickhouse;

import org.anyline.adapter.DataReader;

public enum ClickhouseReader {
    UnsignedByte(new Object[]{com.clickhouse.data.value.UnsignedByte.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.UnsignedByte) {
                try {
                    value = ((com.clickhouse.data.value.UnsignedByte) value).doubleValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ,
    UnsignedLong(new Object[]{com.clickhouse.data.value.UnsignedLong.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.UnsignedLong) {
                try {
                    value = ((com.clickhouse.data.value.UnsignedLong) value).longValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ,
    UnsignedInt(new Object[]{com.clickhouse.data.value.UnsignedInteger.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.UnsignedInteger) {
                try {
                    value = ((com.clickhouse.data.value.UnsignedInteger) value).intValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ,
    UnsignedShort(new Object[]{com.clickhouse.data.value.UnsignedShort.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.UnsignedShort) {
                try {
                    value = ((com.clickhouse.data.value.UnsignedShort) value).shortValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ,
    Decimal(new Object[]{com.clickhouse.data.value.ClickHouseBigDecimalValue.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.ClickHouseBigDecimalValue) {
                try {
                    value = ((com.clickhouse.data.value.ClickHouseBigDecimalValue) value).getValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ,
    Float(new Object[]{com.clickhouse.data.value.ClickHouseFloatValue.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof com.clickhouse.data.value.ClickHouseFloatValue) {
                try {
                    value = ((com.clickhouse.data.value.ClickHouseFloatValue) value).getValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return value;
        }
    })
    ;
    public Object[] supports(){
        return supports;
    }
    public DataReader reader(){
        return reader;
    }
    private final Object[] supports;
    private final DataReader reader;
    ClickhouseReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
