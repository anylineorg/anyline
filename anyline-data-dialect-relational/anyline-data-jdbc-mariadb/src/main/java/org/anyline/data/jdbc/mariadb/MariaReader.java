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


package org.anyline.data.jdbc.mariadb;

import org.anyline.adapter.DataReader;
import org.anyline.data.metadata.StandardColumnType;

public enum MariaReader {
    GeometryReader(new Object[]{StandardColumnType.GEOMETRY}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parse(bytes);
        }
    }),
    PointReader(new Object[]{StandardColumnType.POINT}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parsePoint(bytes);
        }
    }),
    LineReader(new Object[]{StandardColumnType.LINESTRING}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parseLine(bytes);
        }
    }),
    PolygonReader(new Object[]{StandardColumnType.POLYGON}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parsePolygon(bytes);
        }
    }),
    MultiPointReader(new Object[]{StandardColumnType.MULTIPOINT}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parseMultiPoint(bytes);
        }
    }),
    MultiLineReader(new Object[]{StandardColumnType.MULTILINESTRING}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parseMultiLine(bytes);
        }
    }),
    MultiPolygonReader(new Object[]{StandardColumnType.MULTIPOLYGON}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parseMultiPolygon(bytes);
        }
    }),
    GeometryCollectionReader(new Object[]{StandardColumnType.GEOMETRYCOLLECTION}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return MariaGeometryAdapter.parseGeometryCollection(bytes);
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
    MariaReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
