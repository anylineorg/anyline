package org.anyline.metadata;

import java.io.Serializable;

public class Schema extends BaseMetadata<Schema> implements Serializable {
    public Schema(){

    }
    public Schema(String name){
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
    public boolean equal(Schema schema){
        String name = null;
        Catalog catalog = null;
        if(null != schema){
            name = schema.getName();
            catalog = schema.getCatalog();
        }
        boolean catalog_equal = false;
        if(null == this.catalog){
            if(null == catalog){
                catalog_equal = true;
            }
        }else{
            catalog_equal = this.catalog.equal(catalog);
        }
        if(catalog_equal){
            if(null == this.name){
                if(null == name){
                    return true;
                }
            }else if(this.name.equals(name)){
                return true;
            }
        }
        return false;
    }
}
