package org.anyline.entity.graph;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;

public class GraphRow extends OriginRow {
    private Object pv;
    public GraphRow(){}
    public Object getPrimaryValue(){
        return pv;
    }

    @Override
    public DataRow setPrimaryValue(Object value){
        pv = value;
        return this;
    }
}
