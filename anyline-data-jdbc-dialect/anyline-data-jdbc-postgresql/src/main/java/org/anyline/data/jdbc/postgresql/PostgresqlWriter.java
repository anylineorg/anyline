package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.Point;
import org.postgresql.geometric.PGpoint;

public enum PostgresqlWriter {
    PointWriter(new Object[]{Point.class, StandardColumnType.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Point) {
                Point point = (Point) value;
                if (placeholder) {
                    return new PGpoint(point.x(), point.y());
                } else {
                    return "POINT(" + point.x() + "," + point.y() + ")";
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
