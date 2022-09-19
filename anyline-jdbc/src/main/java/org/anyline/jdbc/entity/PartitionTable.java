package org.anyline.jdbc.entity;

import org.anyline.listener.impl.DefaulDDtListener;

public class PartitionTable extends Table{
    protected String masterName;
    protected MasterTable master;

    public PartitionTable(){
        this.listener = new DefaulDDtListener();
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
}
