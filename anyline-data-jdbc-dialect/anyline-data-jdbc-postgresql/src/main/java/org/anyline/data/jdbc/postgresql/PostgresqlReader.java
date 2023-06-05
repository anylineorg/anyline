package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataReader;
import org.postgresql.geometric.*;

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
    ;
    public Object[] supports(){
        return supports;
    }
    public DataReader reader(){
        return reader;
    }
    private final Object[] supports;
    private final DataReader reader;
    PostgresqlReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
