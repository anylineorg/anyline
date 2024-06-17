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



package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataReader;
import org.postgresql.geometric.*;
import org.postgresql.jdbc.PgArray;

public enum PostgresqlReader {
    PointReader(new Object[]{PGpoint.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGpoint) {
                value = PostgresqlGeometryAdapter.parsePoint((PGpoint) value);
            }
            return value;
        }
    }),
    LineSegmentReader(new Object[]{PGlseg.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGlseg) {
                value = PostgresqlGeometryAdapter.parseLineSegment((PGlseg) value);
            }
            return value;
        }
    }),
    PathReader(new Object[]{PGpath.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGpath) {
                value = PostgresqlGeometryAdapter.parsePath((PGpath) value);
            }
            return value;
        }
    }),
    PolygonReader(new Object[]{PGpolygon.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGpolygon) {
                value = PostgresqlGeometryAdapter.parsePolygon((PGpolygon) value);
            }
            return value;
        }
    }),
    CircleReader(new Object[]{PGcircle.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGcircle) {
                value = PostgresqlGeometryAdapter.parseCircle((PGcircle) value);
            }
            return value;
        }
    }),
    //直线
    LineReader(new Object[]{PGline.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGline) {
                value = PostgresqlGeometryAdapter.parseLine((PGline) value);
            }
            return value;
        }
    }),
    //长方形
    BoxReader(new Object[]{PGbox.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGbox) {
                value = PostgresqlGeometryAdapter.parseBox((PGbox) value);
            }
            return value;
        }
    }),
    //数组
    ArrayReader(new Object[]{PgArray.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PgArray) {
                PgArray array = (PgArray) value;
                try {
                    value = array.getArray();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return value;
        }
    }),
    ;
    public Object[] supports() {
        return supports;
    }
    public DataReader reader() {
        return reader;
    }
    private final Object[] supports;
    private final DataReader reader;
    PostgresqlReader(Object[] supports, DataReader reader) {
        this.supports = supports;
        this.reader = reader;
    }
}
