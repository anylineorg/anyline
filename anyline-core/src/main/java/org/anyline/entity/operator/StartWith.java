package org.anyline.entity.operator;

public class StartWith extends BasicCompare{
    public StartWith(){
    }
    public StartWith(String value){
        this.value = value;
    }
    @Override
    public boolean compare(Object value) {
        if(null == this.value || null == value){
            return false;
        }
        return value.toString().toUpperCase().startsWith(this.value.toString().toUpperCase());
    }
}
