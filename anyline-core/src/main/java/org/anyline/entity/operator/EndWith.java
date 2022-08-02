package org.anyline.entity.operator;

public class EndWith extends BasicCompare{

    public EndWith(){
    }
    public EndWith(String value){
        this.value = value;
    }

    @Override
    public boolean compare(Object value) {
        if(null == this.value || null == value){
            return false;
        }
        return value.toString().toUpperCase().endsWith(this.value.toString().toUpperCase());
    }
}
