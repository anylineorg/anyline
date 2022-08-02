package org.anyline.entity.operator;

public class Like extends BasicCompare{
    public Like(){
    }
    public Like(String value){
        this.value = value;
    }
    @Override
    public boolean compare(Object value) {
        if(null == this.value || null == value){
            return false;
        }
        return value.toString().toUpperCase().contains(this.value.toString().toUpperCase());
    }
}
