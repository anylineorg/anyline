package org.anyline.entity.operator;

public class Equal extends BasicCompare{

    public Equal(){
    }
    public Equal(String target){
        this.target = target;
    }
    @Override
    public boolean compare(Object value) {
        return compare(value, this.target);
    }

    @Override
    public boolean compare(Object value, Object target) {
        if(null == target){
            if(null == value){
                return true;
            }else {
                return false;
            }
        }
        return target.toString().equalsIgnoreCase(value.toString());
    }
}
