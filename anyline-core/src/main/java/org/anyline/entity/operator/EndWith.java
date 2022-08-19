package org.anyline.entity.operator;

public class EndWith extends BasicCompare{

    public EndWith(){
    }
    public EndWith(String value){
        this.target = value;
    }

    @Override
    public boolean compare(Object value) {
        return compare(value, this.target);
    }

    @Override
    public boolean compare(Object value, Object tar) {
        if(null == target || null == value){
            return false;
        }
        return value.toString().toUpperCase().endsWith(target.toString().toUpperCase());
    }
}
