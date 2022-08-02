package org.anyline.entity.operator;

public class Equal extends BasicCompare{

    public Equal(){
    }
    public Equal(String value){
        this.value = value;
    }
    @Override
    public boolean compare(Object value) {
        if(null == this.value){
            if(null == value){
                return true;
            }else {
                return false;
            }
        }
        return this.value.toString().equalsIgnoreCase(value.toString());
    }
}
