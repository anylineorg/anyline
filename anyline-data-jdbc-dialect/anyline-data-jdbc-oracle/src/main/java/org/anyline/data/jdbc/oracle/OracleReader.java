package org.anyline.data.jdbc.oracle;

import oracle.sql.CLOB;
import org.anyline.adapter.DataReader;

public enum OracleReader {
    PointReader(new Object[]{CLOB.class}, new DataReader() {
        @Override
        public Object read(Object value) {
            if(value instanceof CLOB) {
                try {
                    value = ((CLOB) value).stringValue();
                }catch (Exception e){
                    e.printStackTrace();
                }
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
    OracleReader(Object[] supports, DataReader reader){
        this.supports = supports;
        this.reader = reader;
    }
}
