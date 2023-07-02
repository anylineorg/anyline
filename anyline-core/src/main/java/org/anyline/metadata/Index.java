package org.anyline.metadata;

import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Index extends Constraint  implements Serializable {
    protected boolean primary     ; // 是否是主键
    protected boolean cluster     ; // 是否聚簇索引
    protected boolean fulltext    ;
    protected boolean spatial     ;

    private Index update;


    public Index getUpdate() {
        return update;
    }

    public Index setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Index setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public Index update(){
        return update(true, true);
    }

    public Index update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public Index setUpdate(Index update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return this;
    }


    public boolean isCluster() {
        if(getmap && null != update){
            return update.cluster;
        }
        return cluster;
    }

    public Index setCluster(boolean cluster) {
        if(setmap && null != update){
            update.setCluster(cluster);
            return this;
        }
        this.cluster = cluster;
        return this;
    }

    public boolean isFulltext() {
        if(getmap && null != update){
            return update.fulltext;
        }
        return fulltext;
    }

    public Index setFulltext(boolean fulltext) {
        if(setmap && null != update){
            update.setFulltext(fulltext);
            return this;
        }
        this.fulltext = fulltext;
        return this;
    }

    public boolean isSpatial() {
        if(getmap && null != update){
            return update.spatial;
        }
        return spatial;
    }

    public Index setSpatial(boolean spatial) {
        if(setmap && null != update){
            update.setSpatial(spatial);
            return this;
        }
        this.spatial = spatial;
        return this;
    }

    public boolean isPrimary() {
        if(getmap && null != update){
            return update.primary;
        }
        return primary;
    }

    public Index setPrimary(boolean primary) {
        if(setmap && null != update){
            update.setPrimary(primary);
            return this;
        }
        this.primary = primary;
        if(primary){
            setCluster(true);
            setUnique(true);
        }
        return this;
    }
    public Index clone(){
        Index copy = new Index();
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