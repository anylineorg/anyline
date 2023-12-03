/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.metadata;


import org.anyline.entity.DataRow;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class MasterTable extends Table<MasterTable>  implements Serializable {
    protected String keyword = "STABLE"             ;
    private LinkedHashMap<String, Table> partitions  ; // 分区表
    protected MasterTable update;

    public MasterTable(){
    }
    public MasterTable(Table table){
        BeanUtil.copyFieldValue( this, table);
        update = null;
        setmap = false;
        getmap = false;
    }
    public MasterTable(String name){
        this(null, name);
    }
    public MasterTable(Schema schema, String table){
        this(null, schema, table);
    }
    public MasterTable(Catalog catalog, Schema schema, String name){
        this();
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }


    public Partition getPartition() {
        if(getmap && null != update){
            return update.partitionBy;
        }
        return partitionBy;
    }

    public MasterTable setPartition(Partition partition) {
        if(setmap && null != update){
            update.setPartition(partition);
            return this;
        }
        this.partitionBy = partition;
        return this;
    }
    public MasterTable setPartition(Partition.TYPE type, String ... columns){
        Partition partition = new Partition(type, columns);
        this.partitionBy = partition;
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


    public String toString(){
        return this.keyword+":"+name;
    }
}
