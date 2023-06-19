package org.anyline.data.jdbc.dm;

import dm.jdbc.driver.DmdbNClob;
import org.anyline.adapter.DataReader;

public enum DMReader {
    PointReader(new Object[]{DmdbNClob.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof DmdbNClob) {
                value = ((DmdbNClob)value).data;
            }
            return value;
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
    DMReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
