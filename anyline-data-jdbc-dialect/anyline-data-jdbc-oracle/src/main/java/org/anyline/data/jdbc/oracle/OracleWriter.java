package org.anyline.data.jdbc.oracle;

import org.anyline.adapter.DataWriter;

public enum OracleWriter {

    ;
    public Object[] supports(){
        return supports;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    OracleWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
