package org.anyline.data.jdbc.mysql;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.*;

public enum MySQLWriter {

    PointWriter(new Object[]{Point.class, StandardColumnType.POINT}, new DataWriter() {
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

    LineWriter(new Object[]{Line.class, StandardColumnType.LINESTRING}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Line) {
                Line line = (Line) value;
                if (placeholder) {
                    return MySQLGeometryAdapter.wkb(line);
                } else {
                    return MySQLGeometryAdapter.sql(line);
                }
            }
            return value;
        }
    }),

    PolygonWriter(new Object[]{Polygon.class, StandardColumnType.POLYGON}, new DataWriter() {
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

    MultiPointWriter(new Object[]{MultiPoint.class, StandardColumnType.MULTIPOINT}, new DataWriter() {
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

    MultiLineWriter(new Object[]{MultiLine.class, StandardColumnType.MULTILINESTRING}, new DataWriter() {
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

    MultiPolygonWriter(new Object[]{MultiPolygon.class, StandardColumnType.MULTIPOLYGON}, new DataWriter() {
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

    GeometryCollectionWriter(new Object[]{GeometryCollection.class, StandardColumnType.GEOMETRYCOLLECTION}, new DataWriter() {
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
    MySQLWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
