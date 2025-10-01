package org.anyline.data.dify.entity;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private Object value;
    private String type = null;

    public Metadata(){}
    public Metadata(String name, Object value){
        this.name = name;
        this.value = value;
    }
    public Metadata(String id, String name, Object value){
        this.id = id;
        this.name = name;
        this.value = value;
    }
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    public DataRow map(){
        return map(true);
    }
    public DataRow map(boolean empty) {
        DataRow row = new OriginRow();
        row.put("id", id);
        row.put("name", name);
        row.put("value", value);
        row.put("type", type);
        return row;
    }
    public String json(){
        return BeanUtil.map2string(map());
    }
    public String json(boolean empty){
        return BeanUtil.map2string(map(empty));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
