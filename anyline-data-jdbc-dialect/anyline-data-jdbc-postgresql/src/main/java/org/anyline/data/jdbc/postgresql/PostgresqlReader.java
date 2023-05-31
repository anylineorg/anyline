package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataReader;
import org.anyline.entity.geometry.Point;
import org.postgresql.geometric.PGpoint;

public enum PostgresqlReader {
    PointReader(new Object[]{PGpoint.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            PGpoint point = (PGpoint)value;
            return new Point(point.x, point.y);
        }
    })
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
