package org.anyline.entity.operator;

import java.math.BigDecimal;

public class Less extends BasicCompare{

    public Less(){
    }
    public Less(String value){
        this.value = value;
    }

    @Override
    public boolean compare(Object value) {
        if(null == this.value || null == value){
            return false;
        }
        try {
            return new BigDecimal(value.toString()).compareTo(new BigDecimal(this.value.toString())) < 0;
        }catch (Exception e){
            return false;
        }
    }
}
