package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataWriter;
import org.anyline.entity.Point;
import org.postgresql.geometric.PGpoint;

public enum PostgresqlWriter {
    PGWriter(Point.class, new DataWriter() {
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
    public Class support(){
        return clazz;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Class clazz;
    private final DataWriter writer;
    PostgresqlWriter(Class clazz, DataWriter writer){
        this.clazz = clazz;
        this.writer = writer;
    }
}
