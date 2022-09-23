package org.anyline.jdbc.entity;


import org.anyline.entity.DataRow;
import org.anyline.listener.init.SimpleDDListener;

import java.util.LinkedHashMap;

public class MasterTable extends Table {
    protected String keyword = "STABLE"             ;
    private LinkedHashMap<String,Table> partitions  ; //分区表
    private Partition partition                     ; //分区方式

    public MasterTable(){
        this.listener = new SimpleDDListener();
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
    public Table getPartition(Tag ... tags){
        Table table = null;
        return table;
    }
    public String toString(){
        return keyword+":"+name;
    }
}
