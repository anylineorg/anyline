package org.anyline.data.jdbc.mysql;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.Line;
import org.anyline.entity.geometry.Point;

public enum MySQLWriter {

    PointWriter(new Object[]{Point.class, StandardColumnType.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Point) {
                Point point = (Point) value;
                if (placeholder) {
                    return point.bytes();
                } else {
                    return "POINT(" + point.getX() + "," + point.getY() + ")";
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
                    return line.bytes();
                } else {
                    return value;
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
