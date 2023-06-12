package org.anyline.entity.data;


import org.anyline.entity.DataRow;

import java.util.LinkedHashMap;

public class MasterTable extends Table {
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public LinkedHashMap<String, Table> getPartitions() {
        return partitions;
    }

    public void setPartitions(LinkedHashMap<String, Table> partitions) {
        this.partitions = partitions;
    }

    /**
     * 根据值定位分区表
     * @param value value
     * @return table table
     */
    public Table getPartition(DataRow value){
        Table table = null;
        return table;
    }
    /**
     * 根据标签定位分区表
     * @param tags tags
     * @return table table
     */
    public Table getPartition(Tag... tags){
        Table table = null;
        return table;
    }

    public MasterTable clone(){
        MasterTable table = new MasterTable();
        table.partition = partition;
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
        table.autoDropColumn = autoDropColumn;
        table.update = update;
        return table;
    }
    public MasterTable update(){
        update = clone();
        update.setUpdate(null);
        return update;
    }
    public String toString(){
        return this.keyword+":"+name;
    }
}
