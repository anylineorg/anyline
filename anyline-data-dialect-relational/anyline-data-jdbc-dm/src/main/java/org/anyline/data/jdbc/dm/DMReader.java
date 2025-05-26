/*
 * Copyright 2006-2025 www.anyline.org
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
import org.anyline.util.BeanUtil;

import java.io.BufferedReader;
import java.sql.NClob;

public enum DMReader {
    /**
     * text类型
     */
    DmdbNClobReader(new Object[]{DmdbNClob.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof DmdbNClob) {
                DmdbNClob clob = (DmdbNClob)value;
                if(clob.length == -1) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(clob.getCharacterStream());
                        StringBuilder builder = new StringBuilder();
                        String line = reader.readLine();
                        if(null != line){
                            builder.append(line);
                            while ((line = reader.readLine()) != null) {
                                builder.append("\n").append(line);
                            }
                        }
                        value = builder.toString();
                    }catch (Exception ignored){
                    }finally {
                        if(null != reader){
                            try {
                                reader.close();
                            }catch (Exception ignored){}
                        }
                    }
                }else {
                    value = clob.data;
                }
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
                DmdbBlob blob = (DmdbBlob)value;
                if(blob.length == -1){
                    try {
                        value = BeanUtil.stream2bytes(blob.getBinaryStream());
                    }catch (Exception ignored){}
                }else {
                    value = blob.data;
                }
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
                DmdbClob clob = (DmdbClob)value;
                if(clob.length == -1) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(clob.getCharacterStream());
                        StringBuilder builder = new StringBuilder();
                        String line = reader.readLine();
                        if(null != line){
                            builder.append(line);
                            while ((line = reader.readLine()) != null) {
                                builder.append("\n").append(line);
                            }
                        }
                        value = builder.toString();
                    }catch (Exception ignored){
                    }finally {
                        if(null != reader){
                            try {
                                reader.close();
                            }catch (Exception ignored){}
                        }
                    }
                }else {
                    value = clob.data;
                }
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
