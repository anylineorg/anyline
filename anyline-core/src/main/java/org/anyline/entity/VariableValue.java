package org.anyline.entity;


public class VariableValue {
    private String value;
    public VariableValue(String value){
        this.value = value;
    }
    public String value(){
        return value;
    }
    public void value(String value){
        this.value = value;
    }
    public String toString(){
        return value;
    }
}