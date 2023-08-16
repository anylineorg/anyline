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


import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class PartitionTable extends Table implements Serializable {
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
