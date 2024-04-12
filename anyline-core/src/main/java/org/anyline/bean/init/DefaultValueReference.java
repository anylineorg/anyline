package org.anyline.bean.init;

import org.anyline.bean.ValueReference;

public class DefaultValueReference implements ValueReference {
    public DefaultValueReference(){}
    public DefaultValueReference(String name){
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
