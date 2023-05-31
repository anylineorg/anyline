package org.anyline.data.jdbc.mysql;

import org.anyline.adapter.DataReader;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.geometry.Line;

public enum MySQLReader {
    PointReader(new Object[]{StandardColumnType.LINESTRING}, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return new Line(bytes);
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
    MySQLReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
