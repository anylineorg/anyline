package org.anyline.entity.operator;

public class StartWith extends BasicCompare{
    public StartWith(){
    }
    public StartWith(String target){
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
        return value.toString().toUpperCase().startsWith(target.toString().toUpperCase());
    }
}
