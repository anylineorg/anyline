package org.anyline.data.jdbc.mariadb;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.*;

public enum MariaWriter {

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
                    return MariaGeometryAdapter.wkb(point);
                } else {
                    return MariaGeometryAdapter.sql(point);
                }
            }
            return value;
        }
    }),

    LineWriter(new Object[]{LineString.class, StandardColumnType.LINESTRING}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof LineString) {
                LineString line = (LineString) value;
                if (placeholder) {
                    return MariaGeometryAdapter.wkb(line);
                } else {
                    return MariaGeometryAdapter.sql(line);
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
                    return MariaGeometryAdapter.wkb(polygon);
                } else {
                    return MariaGeometryAdapter.sql(polygon);
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
                    return MariaGeometryAdapter.wkb(multiPoint);
                } else {
                    return MariaGeometryAdapter.sql(multiPoint);
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
                    return MariaGeometryAdapter.wkb(multiLine);
                } else {
                    return MariaGeometryAdapter.sql(multiLine);
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
                    return MariaGeometryAdapter.wkb(multiPolygon);
                } else {
                    return MariaGeometryAdapter.sql(multiPolygon);
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
                    return MariaGeometryAdapter.wkb(collection);
                } else {
                    return MariaGeometryAdapter.sql(collection);
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
    MariaWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
