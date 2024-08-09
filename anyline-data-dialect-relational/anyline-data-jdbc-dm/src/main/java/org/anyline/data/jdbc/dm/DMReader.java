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

package org.anyline.data.jdbc.dm;

import dm.jdbc.driver.DmdbBlob;
import dm.jdbc.driver.DmdbClob;
import dm.jdbc.driver.DmdbNClob;
import org.anyline.adapter.DataReader;

public enum DMReader {
    /**
     * text类型
     */
    DmdbNClobReader(new Object[]{DmdbNClob.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof DmdbNClob) {
                value = ((DmdbNClob)value).data;
            }
            return value;
        }
    }),
    /**
     * blob类型
     */
    DmdbBlobReader(new Object[]{DmdbBlob.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof DmdbBlob) {
                value = ((DmdbBlob)value).data;
            }
            return value;
        }
    }),
    /**
     * clob类型
     */
    DmdbClobReader(new Object[]{DmdbClob.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof DmdbClob) {
                value = ((DmdbClob)value).data;
            }
            return value;
        }
    })
    ;
    /**
     * 支持的类型符合这些类型的 在读取之后 由当前reader转换
     * @return class ColumnType StringColumnType
     */
    public Object[] supports() {
        return supports;
    }
    public DataReader reader() {
        return reader;
    }
    private final Object[] supports;
    private final DataReader reader;
    DMReader(Object[] supports, DataReader reader) {
        this.supports = supports;
        this.reader = reader;
    }
}
