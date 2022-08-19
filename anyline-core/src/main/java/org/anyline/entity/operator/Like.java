package org.anyline.entity.operator;

public class Like extends BasicCompare{
    public Like(){
    }
    public Like(String target){
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
        return value.toString().toUpperCase().contains(target.toString().toUpperCase());
    }
}
