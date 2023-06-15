package org.anyline.entity.data;


import org.anyline.util.BeanUtil;

import java.util.LinkedHashMap;

public class PartitionTable extends Table{
    protected String masterName;
    protected MasterTable master;
    protected PartitionTable update;

    public PartitionTable(){
    }
    public PartitionTable(String name){
        this(null, name);
    }
    public PartitionTable(String schema, String table){
        this(null, schema, table);
    }
    public PartitionTable(String catalog, String schema, String name){
        this();
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    public void setMaster(String masterName) {
        this.masterName = masterName;
    }

    public MasterTable getMaster() {
        return master;
    }

    public void setMaster(MasterTable master) {
        this.master = master;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public PartitionTable clone(){
        PartitionTable copy = new PartitionTable();
        BeanUtil.copyFieldValueNvl(copy, this);

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


    public PartitionTable update(){
        return update(true);
    }
    public PartitionTable update(boolean setmap){
        this.setmap = setmap;
        update = clone();
        update.update = null;
        return update;
    }
    public String toString(){
        return this.keyword+":"+name;
    }

}
