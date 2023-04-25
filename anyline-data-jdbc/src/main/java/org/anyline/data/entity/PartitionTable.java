package org.anyline.data.entity;

import org.anyline.data.listener.init.DefaultDDListener;

public class PartitionTable extends Table{
    protected String masterName;
    protected PartitionTable update;
    protected MasterTable master;

    public PartitionTable(){
        this.listener = new DefaultDDListener();
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
        PartitionTable table = new PartitionTable();
        table.master = master;
        table.masterName = masterName;
        table.catalog = catalog;
        table.schema = schema;
        table.name = name;
        table.comment = comment;
        table.type = type;
        table.typeCat = typeCat;
        table.typeSchema = typeSchema;
        table.typeName = typeName;
        table.selfReferencingColumn = selfReferencingColumn;
        table.refGeneration = refGeneration;
        table.engine = engine;
        table.charset = charset;
        table.collate = collate;
        table.ttl = ttl;
        table.checkSchemaTime = checkSchemaTime;
        table.primaryKey = primaryKey;
        table.columns = columns;
        table.tags = tags;
        table.indexs = indexs;
        table.constraints = constraints;
        table.listener = listener;
        table.autoDropColumn = autoDropColumn;
        table.update = update;
        return table;
    }
    public PartitionTable update(){
        update = clone();
        update.setUpdate(null);
        return update;
    }
    public String toString(){
        return this.keyword+":"+name;
    }

}
