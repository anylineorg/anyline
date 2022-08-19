package org.anyline.entity.operator;

import java.math.BigDecimal;

public class Between extends BasicCompare{
    private BigDecimal min;
    private BigDecimal max;
    public Between(){
    }
    public Between(Object min, Object max){
        this.min = new BigDecimal(min.toString());
        this.max = new BigDecimal(max.toString());
    }
    public Between(BigDecimal min, BigDecimal max){
        this.min = min;
        this.max = max;
    }
    public Between(Integer min, Integer max){
        this.min = new BigDecimal(min);
        this.max = new BigDecimal(max);
    }
    public Between(Double min, Double max){
        this.min = new BigDecimal(min);
        this.max = new BigDecimal(max);
    }
    public Between(Long min, Long max){
        this.min = new BigDecimal(min);
        this.max = new BigDecimal(max);
    }
    @Override
    public boolean compare(Object value) {
        if(targets.size() < 2){
            return false;
        }
        if(null == value){
            return false;
        }
        try {
            BigDecimal v = new BigDecimal(value.toString());
            if (v.compareTo(min) >= 0 && v.compareTo(max) <= 0) {
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    @Override
    public boolean compare(Object value, Object target) {
        return false;
    }
}
