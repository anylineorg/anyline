package org.anyline.entity.graph;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginDataRow;

public class GraphRow extends OriginDataRow {
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
