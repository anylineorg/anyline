package org.anyline.data.jdbc.mysql;

import org.anyline.adapter.DataReader;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.Line;
import org.anyline.entity.metadata.ColumnType;

public enum MySQLReader {
    PointReader(StandardColumnType.LINESTRING, new DataReader() {
        @Override
        public Object read(Object value) {
            byte[] bytes = (byte[]) value;
            return new Line(bytes);
        }
    })
    ;
    public ColumnType support(){
        return type;
    }
    public DataReader reader(){
        return reader;
    }
    private final ColumnType type;
    private final DataReader reader;
    MySQLReader(ColumnType type, DataReader reader){
        this.type = type;
        this.reader = reader;
    }
}
