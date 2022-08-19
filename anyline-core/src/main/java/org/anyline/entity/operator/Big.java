package org.anyline.entity.operator;

import java.math.BigDecimal;

public class Big extends BasicCompare{

    public Big(){
    }
    public Big(String target){
        this.target = target;
    }
    @Override
    public boolean compare(Object value) {
        return compare(value, this.target);
    }

    @Override
    public boolean compare(Object value, Object target) {
        if(null == target || null == value){
            return false;
        }
        try {
            return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) > 0;
        }catch (Exception e){
            return false;
        }
    }
}
