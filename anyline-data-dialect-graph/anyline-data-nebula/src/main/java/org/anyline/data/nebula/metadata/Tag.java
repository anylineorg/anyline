package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Table;

public class Tag extends Table {
    public Tag(){

    }
    public Tag(String name){
        setName(name);
    }
    public Tag(String space, String name){
        setCatalog(space);
        setName(name);
    }
    @Override
    public String getKeyword(){
        return "TAG";
    }
}
