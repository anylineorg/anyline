package org.anyline.entity.data;


import org.anyline.entity.DataRow;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class MasterTable extends Table  implements Serializable {
    protected String keyword = "STABLE"             ;
    private LinkedHashMap<String,Table> partitions  ; // 分区表
    protected MasterTable update;
    private Partition partition                     ; // 分区方式

    public MasterTable(){
    }
    public MasterTable(String name){
        this(null, name);
    }
    public MasterTable(String schema, String table){
        this(null, schema, table);
    }
    public MasterTable(String catalog, String schema, String name){
        this();
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }


    public MasterTable getUpdate() {
        return update;
    }

    public MasterTable setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public MasterTable setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public MasterTable update(){
        return update(true, true);
    }

    public MasterTable update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public MasterTable setUpdate(MasterTable update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        update.update = null;
        return this;
    }

    public Partition getPartition() {
        if(getmap && null != update){
            return update.partition;
        }
        return partition;
    }

    public MasterTable setPartition(Partition partition) {
        if(setmap && null != update){
            update.setPartition(partition);
            return this;
        }
        this.partition = partition;
        return this;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public LinkedHashMap<String, Table> getPartitions() {
        if(getmap && null != update){
            return update.partitions;
        }
        return partitions;
    }

    public MasterTable setPartitions(LinkedHashMap<String, Table> partitions) {
        if(setmap && null != update){
            update.setPartitions(partitions);
            return this;
        }
        this.partitions = partitions;
        return this;
    }

    /**
     * 根据值定位分区表
     * @param value value
     * @return table table
     */
    public Table getPartition(DataRow value){
        if(getmap && null != update){
            return update.getPartition(value);
        }
        Table table = null;
        return table;
    }
    /**
     * 根据标签定位分区表
     * @param tags tags
     * @return table table
     */
    public Table getPartition(Tag... tags){
        if(getmap && null != update){
            return update.getPartition(tags);
        }
        Table table = null;
        return table;
    }

    public MasterTable clone(){
        MasterTable copy = new MasterTable();
        BeanUtil.copyFieldValue(copy, this);

        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;;
        return copy;
    }

    public String toString(){
        return this.keyword+":"+name;
    }
}
