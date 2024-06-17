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



package org.anyline.data.jdbc.postgis;

import org.anyline.adapter.DataReader;
import org.postgis.*;

public enum PostgisReader {
    PointReader(new Object[]{Point.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof Point) {
                value = PostgisGeometryAdapter.parsePoint((Point) value);
            }
            return value;
        }
    }),
    PolygonReader(new Object[]{Polygon.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof Polygon) {
                value = PostgisGeometryAdapter.parsePolygon((Polygon) value);
            }
            return value;
        }
    }),
    PGgeometryLWReader(new Object[]{PGgeometryLW.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGgeometryLW) {
                value = PostgisGeometryAdapter.parsePGgeometryLW((PGgeometryLW) value);
            }
            return value;
        }
    }),
    PGgeometryReader(new Object[]{PGgeometry.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGgeometry) {
                value = PostgisGeometryAdapter.parsePGgeometry((PGgeometry) value);
            }
            return value;
        }
    }),
    PGbox3dReader(new Object[]{PGbox3d.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGbox3d) {
                value = PostgisGeometryAdapter.parsePGbox3d((PGbox3d) value);
            }
            return value;
        }
    }),
    PGbox2dReader(new Object[]{PGbox2d.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof PGbox2d) {
                value = PostgisGeometryAdapter.parsePGbox2d((PGbox2d) value);
            }
            return value;
        }
    }),
    MultiPolygonReader(new Object[]{MultiPolygon.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof MultiPolygon) {
                value = PostgisGeometryAdapter.parseMultiPolygon((MultiPolygon) value);
            }
            return value;
        }
    }),
    MultiPointReader(new Object[]{MultiPoint.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof MultiPoint) {
                value = PostgisGeometryAdapter.parseMultiPoint((MultiPoint) value);
            }
            return value;
        }
    }),
    MultiLineStringReader(new Object[]{MultiLineString.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof MultiLineString) {
                value = PostgisGeometryAdapter.parseMultiLineString((MultiLineString) value);
            }
            return value;
        }
    }),
    LineStringReader(new Object[]{LineString.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof LineString) {
                value = PostgisGeometryAdapter.parseLineString((LineString) value);
            }
            return value;
        }
    }),
    GeometryCollectionReader(new Object[]{GeometryCollection.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof GeometryCollection) {
                value = PostgisGeometryAdapter.parseGeometryCollection((GeometryCollection) value);
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
    PostgisReader(Object[] supports, DataReader reader) {
        this.supports = supports;
        this.reader = reader;
    }
}
