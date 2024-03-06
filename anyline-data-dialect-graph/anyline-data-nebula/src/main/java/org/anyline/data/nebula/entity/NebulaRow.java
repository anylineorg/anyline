package org.anyline.data.nebula.entity;

import org.anyline.entity.DataRow;

public class NebulaRow extends DataRow {
    private Object pv;
    public Object getPrimaryValue(){
        return pv;
    }

    @Override
    public DataRow setPrimaryValue(Object value){
        pv = value;
        return this;
    }
}
