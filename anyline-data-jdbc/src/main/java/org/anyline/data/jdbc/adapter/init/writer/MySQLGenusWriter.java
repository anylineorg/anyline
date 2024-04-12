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

package org.anyline.data.jdbc.adapter.init.writer;

import org.anyline.adapter.DataWriter;
import org.anyline.data.jdbc.adapter.init.geometry.MySQLGeometryAdapter;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.entity.geometry.*;

public enum MySQLGenusWriter {

    PointWriter(new Object[]{Point.class, StandardTypeMetadata.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            Point point = null;
            if(value instanceof Point) {
                point = (Point) value;
            }else if(value instanceof double[]){
                double[] xy = (double[]) value;
                if(xy.length >= 2){
                    point = new Point(xy[0], xy[1]);
                }
            }else if(value instanceof Double[]){
                Double[] xy = (Double[]) value;
                if(xy.length >= 2){
                    point = new Point(xy[0], xy[1]);
                }
            }else if(value instanceof int[]){
                int[] xy = (int[]) value;
                if(xy.length >= 2){
                    point = new Point(xy[0], xy[1]);
                }
            }else if(value instanceof Integer[]){
                Integer[] xy = (Integer[]) value;
                if(xy.length >= 2){
                    point = new Point(xy[0], xy[1]);
                }
            }
            if(null != point) {
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(point);
                } else {
                    return MySQLGeometryAdapter.sql(point);
                }
            }
            return value;
        }
    }),

    LineWriter(new Object[]{LineString.class, StandardTypeMetadata.LINESTRING}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof LineString) {
                LineString line = (LineString) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(line);
                } else {
                    return MySQLGeometryAdapter.sql(line);
                }
            }
            return value;
        }
    }),

    PolygonWriter(new Object[]{Polygon.class, StandardTypeMetadata.POLYGON}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Polygon) {
                Polygon polygon = (Polygon) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(polygon);
                } else {
                    return MySQLGeometryAdapter.sql(polygon);
                }
            }
            return value;
        }
    }),

    MultiPointWriter(new Object[]{MultiPoint.class, StandardTypeMetadata.MULTIPOINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof MultiPoint) {
                MultiPoint multiPoint = (MultiPoint) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(multiPoint);
                } else {
                    return MySQLGeometryAdapter.sql(multiPoint);
                }
            }
            return value;
        }
    }),

    MultiLineWriter(new Object[]{MultiLine.class, StandardTypeMetadata.MULTILINESTRING}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof MultiLine) {
                MultiLine multiLine = (MultiLine) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(multiLine);
                } else {
                    return MySQLGeometryAdapter.sql(multiLine);
                }
            }
            return value;
        }
    }),

    MultiPolygonWriter(new Object[]{MultiPolygon.class, StandardTypeMetadata.MULTIPOLYGON}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof MultiPolygon) {
                MultiPolygon multiPolygon = (MultiPolygon) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(multiPolygon);
                } else {
                    return MySQLGeometryAdapter.sql(multiPolygon);
                }
            }
            return value;
        }
    }),

    GeometryCollectionWriter(new Object[]{GeometryCollection.class, StandardTypeMetadata.GEOMETRYCOLLECTION}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof GeometryCollection) {
                GeometryCollection collection = (GeometryCollection) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(collection);
                } else {
                    return MySQLGeometryAdapter.sql(collection);
                }
            }
            return value;
        }
    })

    ;
    public Object[] supports(){
        return supports;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    MySQLGenusWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
