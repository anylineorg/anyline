package org.anyline.metadata;

import org.anyline.util.BasicUtil;

import java.io.Serializable;

public class Schema extends BaseMetadata<Schema> implements Serializable {
    protected String keyword = "SCHEMA"           ;
    public Schema(){

    }
    public Schema(String name){
        this.name = name;
    }
    public String toString(){
        String str = getKeyword()+":";
        if(null != catalog){
            str += getCatalogName() + ".";
        }
        str += name;
        return str;
    }
    public boolean isEmpty(){
        if(null == name || name.trim().isEmpty()){
            return true;
        }
        return false;
    }

    public boolean equals(Schema schema){
        return equals(schema, true);
    }
    public boolean equals(Schema schema, boolean ignoreCase){
        if(null == schema){
            return false;
        }
        boolean catalog_equal = BasicUtil.equals(this.catalog, schema.getCatalog(), ignoreCase);
        if(catalog_equal){
            return BasicUtil.equals(this.name, schema.getName(), ignoreCase);
        }
        return false;
    }
    public String getKeyword() {
        return this.keyword;
    }
}
