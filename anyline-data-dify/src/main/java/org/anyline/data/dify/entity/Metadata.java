package org.anyline.data.dify.entity;

import java.io.Serializable;

public class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String json(){
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\":\"").append(id).append("\",\"name\":\"").append(name).append("\",\"value\":\"").append(value).append("\"}");
        return builder.toString();
    }
}
