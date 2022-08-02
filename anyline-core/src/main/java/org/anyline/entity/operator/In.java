package org.anyline.entity.operator;

import java.util.List;

public class In extends BasicCompare{

    public In(){
    }
    public In(List<Object> values){
        this.values = values;
    }
    @Override
    public boolean compare(Object value) {
        if(null == this.values || null == value){
            return false;
        }
        for(Object v:values){
            if(null != v && v.toString().equalsIgnoreCase(value.toString())){
                return true;
            }
        }
        return false;
    }
}
