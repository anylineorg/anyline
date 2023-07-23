package org.anyline.metadata;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.io.Serializable;

public class Tag extends Column implements Serializable {
    protected String keyword = "TAG"            ;
    protected Tag update = null;

    public Tag(){
    }
    public Tag(String name, String type, Object value){
        this.name = name;
        this.typeName = type;
        this.value = value;
    }
    public Tag(String name, Object value){
        this.name = name;
        this.value = value;
    }


    public Tag getUpdate() {
        return update;
    }

    public Tag setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Tag setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public Tag update(){
        return update(true, true);
    }

    public Tag update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public Tag setUpdate(Tag update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
            update.origin = this;
        }
        return this;
    }


    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(typeName);
        if(null != precision && precision > 0){
            builder.append("(").append(precision);
            if(null != scale && scale > 0){
                builder.append(",").append(scale);
            }
            builder.append(")");
        }
        if(BasicUtil.isNotEmpty(value)){
            builder.append(" value: ").append(value);
        }
        return builder.toString();
    }
    public Tag clone(){
        Tag copy = new Tag();
        BeanUtil.copyFieldValue(update, this);
        return copy;
    }
    public String getKeyword() {
        return this.keyword;
    }
}