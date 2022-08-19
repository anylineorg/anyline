package org.anyline.entity.operator;

import java.math.BigDecimal;

public class BigEqual  extends BasicCompare{

    public BigEqual(){
    }
    public BigEqual(String target){
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
        return new BigDecimal(value.toString()).compareTo(new BigDecimal(target.toString())) >= 0;
    }
}
