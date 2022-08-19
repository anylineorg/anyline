package org.anyline.entity.operator;

import java.util.Collection;
import java.util.List;

public class In extends BasicCompare{

    public In(){
    }
    public In(List<Object> targets){
        this.targets = targets;
    }
    @Override
    public boolean compare(Object value) {
        return compare(value, this.targets);
    }

    @Override
    public boolean compare(Object value, Object targets) {
        if(null != targets && targets instanceof Collection){
            Collection cols = (Collection) targets;
            for(Object v:cols){
                if(null != v && v.toString().equalsIgnoreCase(value.toString())){
                    return true;
                }
            }
        }
        return false;
    }
}
