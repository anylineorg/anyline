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

package org.anyline.data.jdbc.oracle;

import oracle.sql.*;
import org.anyline.adapter.DataReader;

public enum OracleReader {
    ClobReader(new Object[]{CLOB.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof CLOB) {
                try {
                    value = ((CLOB) value).stringValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    BlobReader(new Object[]{BLOB.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof BLOB) {
                try {
                    value = ((BLOB) value).getBytes();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    NUMBERReader(new Object[]{NUMBER.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof NUMBER) {
                try {
                    value = ((NUMBER) value).bigDecimalValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    TimestampReader(new Object[]{TIMESTAMP.class, TIMESTAMPTZ.class, TIMESTAMPLTZ.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof TIMESTAMP) {
                try {
                    value = ((TIMESTAMP) value).timestampValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(value instanceof TIMESTAMPLTZ) {
                try {
                    value = ((TIMESTAMPLTZ) value).timestampValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(value instanceof TIMESTAMPTZ) {
                try {
                    value = ((TIMESTAMPTZ) value).timestampValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    RAWReader(new Object[]{RAW.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof RAW) {
                try {
                    value = ((RAW) value).stringValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    ROWIDReader(new Object[]{ROWID.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof ROWID) {
                try {
                    value = ((ROWID) value).stringValue();
                }catch (Exception e) {
                    e.printStackTrace();
                }
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
    OracleReader(Object[] supports, DataReader reader) {
        this.supports = supports;
        this.reader = reader;
    }
}
