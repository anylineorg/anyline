package org.anyline.data.jdbc.dm;

import org.anyline.adapter.DataWriter;

public enum DMWriter {

    ;
    public Object[] supports(){
        return supports;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    DMWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
