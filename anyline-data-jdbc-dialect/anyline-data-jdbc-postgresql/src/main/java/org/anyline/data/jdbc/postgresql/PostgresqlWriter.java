package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.*;
import org.postgresql.geometric.*;

public enum PostgresqlWriter {
    PointWriter(new Object[]{Point.class, StandardColumnType.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Point) {
                Point point = (Point) value;
                PGpoint pg = PostgresqlGeometryAdapter.convert(point);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
                }
            }
            return value;
        }
    }),
    LineSegmentWriter(new Object[]{LineSegment.class, StandardColumnType.LSEG}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof LineSegment) {
                LineSegment segment = (LineSegment) value;
                PGlseg pg = PostgresqlGeometryAdapter.convert(segment);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
                }
            }
            return value;
        }
    }),
    PathWriter(new Object[]{LineString.class, StandardColumnType.PATH}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof LineString) {
                LineString string = (LineString) value;
                PGpath pg = PostgresqlGeometryAdapter.convert(string);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
                }
            }
            return value;
        }
    }),
    LineWriter(new Object[]{Line.class, StandardColumnType.LINE}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Line) {
                Line line = (Line) value;
                PGline pg = PostgresqlGeometryAdapter.convert(line);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
                }
            }
            return value;
        }
    }),
    BoxWriter(new Object[]{Box.class, StandardColumnType.BOX}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Box) {
                Box box = (Box) value;
                PGbox pg = PostgresqlGeometryAdapter.convert(box);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
                }
            }
            return value;
        }
    }),
    CircleWriter(new Object[]{Circle.class, StandardColumnType.CIRCLE}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Circle) {
                Circle circle = (Circle) value;
                PGcircle pg = PostgresqlGeometryAdapter.convert(circle);
                if (placeholder) {
                    return pg;
                } else {
                    return pg.getValue();
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
    PostgresqlWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
