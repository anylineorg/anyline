package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataWriter;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.Point;
import org.anyline.entity.metadata.ColumnType;
import org.postgresql.geometric.PGpoint;

public enum PostgresqlWriter {
    PointWriter(Point.class, StandardColumnType.POINT, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            Point point = (Point)value;
            if(placeholder) {
                return new PGpoint(point.getX(), point.getY());
            }else{
                return "POINT("+point.getX()+","+point.getY()+")";
            }
        }
    })
    ;
    public Class supportClass(){
        return clazz;
    }
    public ColumnType supportType(){
        return type;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Class clazz;
    private final ColumnType type;
    private final DataWriter writer;
    PostgresqlWriter(Class clazz, ColumnType type, DataWriter writer){
        this.clazz = clazz;
        this.type = type;
        this.writer = writer;
    }
}
