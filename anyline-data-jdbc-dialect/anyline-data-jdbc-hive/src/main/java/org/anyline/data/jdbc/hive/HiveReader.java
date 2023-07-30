package org.anyline.data.jdbc.hive;

import org.anyline.adapter.DataReader;
import org.anyline.data.metadata.StandardColumnType;

public enum HiveReader {
    ;
    public Object[] supports(){
        return supports;
    }
    public DataReader reader(){
        return reader;
    }
    private final Object[] supports;
    private final DataReader reader;
    HiveReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
