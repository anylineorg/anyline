package org.anyline.metadata;

import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class PrimaryKey extends Index implements Serializable {
    private PrimaryKey update;
    public PrimaryKey(){
        primary = true;
    }
    public boolean isPrimary(){
        return true;
    }


    public Index getUpdate() {
        return update;
    }

    public PrimaryKey setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public PrimaryKey setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public PrimaryKey update(){
        return update(true, true);
    }

    public PrimaryKey update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public PrimaryKey setUpdate(PrimaryKey update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return this;
    }

    public PrimaryKey clone(){
        PrimaryKey copy = new PrimaryKey();
        BeanUtil.copyFieldValue(copy, this);

        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;
        return copy;
    }
}
