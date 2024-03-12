package org.anyline.data.nebula.entity;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginDataRow;

public class NebulaRow extends OriginDataRow {
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
