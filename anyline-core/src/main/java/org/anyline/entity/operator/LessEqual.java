package org.anyline.entity.operator;

import java.math.BigDecimal;

public class LessEqual extends BasicCompare{
    public LessEqual(){
    }
    public LessEqual(String target){
        this.target = target;
    }
    @Override
    public boolean compare(Object value) {
        return compare(value, target);
    }

    @Override
    public boolean compare(Object value, Object target) {
        if(null == target || null == value){
            return false;
        }
        try {
            return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) <= 0;
        }catch (Exception e){
            return false;
        }
    }
}
