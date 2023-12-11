package org.anyline.metadata;

import java.io.Serializable;

public class Catalog extends BaseMetadata<Catalog> implements Serializable {
    public Catalog(){

    }
    public Catalog(String name){
        this.name = name;
    }
    public String toString(){
        return name;
    }
    public boolean isEmpty(){
        if(null == name || name.trim().isEmpty()){
            return true;
        }
        return false;
    }
    public boolean equal(Catalog catalog){
        String name = null;
        if(null != catalog){
            name = catalog.getName();
        }
        if(null == this.name){
            if(null == name){
                return true;
            }
        }else if(this.name.equals(name)){
            return true;
        }
        return false;
    }
}
