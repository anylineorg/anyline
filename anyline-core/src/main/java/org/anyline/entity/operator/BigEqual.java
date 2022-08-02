package org.anyline.entity.operator;

import java.math.BigDecimal;

public class BigEqual  extends BasicCompare{

    public BigEqual(){
    }
    public BigEqual(String value){
        this.value = value;
    }

    @Override
    public boolean compare(Object value) {
        if(null == this.value || null == value){
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(new BigDecimal(this.value.toString())) >= 0;
    }
}
