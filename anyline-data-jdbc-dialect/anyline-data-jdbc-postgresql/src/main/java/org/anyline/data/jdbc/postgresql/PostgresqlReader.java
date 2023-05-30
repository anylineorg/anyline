package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataReader;
import org.anyline.entity.Point;
import org.postgresql.geometric.PGpoint;

public enum PostgresqlReader {
    PointReader(PGpoint.class, new DataReader() {
        @Override
        public Object read(Object value) {
            PGpoint point = (PGpoint)value;
            return new Point(point.x, point.y);
        }
    })
    ;
    public Class support(){
        return clazz;
    }
    public DataReader reader(){
        return reader;
    }
    private final Class clazz;
    private final DataReader reader;
    PostgresqlReader(Class clazz, DataReader reader){
        this.clazz = clazz;
        this.reader = reader;
    }
}
