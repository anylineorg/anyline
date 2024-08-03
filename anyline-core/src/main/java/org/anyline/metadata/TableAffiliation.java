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

import org.anyline.data.Run;
import org.anyline.util.BasicUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class TableAffiliation<E extends TableAffiliation> extends Metadata<E> implements Serializable {

    protected transient Table<?> table;

    public String getIdentity() {
        if(null == identity) {
            identity = BasicUtil.nvl(getCatalogName(), "") + "_" + BasicUtil.nvl(getSchemaName(), "") + "_" + BasicUtil.nvl(getTableName(false), "") + "_" + BasicUtil.nvl(getName(), "") ;
            identity = identity.toUpperCase();
            //identity = MD5Util.crypto(identity.toUpperCase());
        }
        return identity;
    }
    public void addDdl(String ddl) {
        if(this.ddls == null) {
            this.ddls = new ArrayList<>();
        }
        ddls.add(ddl);
        if(null != this.table){
            this.table.addDdl(ddl);
        }
    }
    public void addRun(Run run) {
        if(null != table){
            table.addRun(run);
        }
        if(null != origin){
            origin.addRun(run);
            return;
        }
        if(this.runs == null) {
            this.runs = new ArrayList<>();
        }
        if(!runs.contains(run)) {
            runs.add(run);
        }
    }
    public boolean execute() {
        if(null != table){
            if(!table.execute()){
                return false;
            }
        }
        if(null != origin){
            if(!origin.execute()){
                return false;
            }
        }
        return execute;
    }
    /**
     * 相关表
     * @param update 是否检测update
     * @return table
     */
    public Table getTable(boolean update) {
        if(update) {
            if(null != table && null != table.getUpdate()) {
                return table.getUpdate();
            }
        }
        return table;
    }

    public Table getTable() {
        return getTable(false);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getTableName(boolean update) {
        Table table = getTable(update);
        if(null != table) {
            return table.getName();
        }
        return null;
    }

    public String getTableName() {
        return getTableName(false);
    }
    public E setTable(String table) {
        this.table = new Table(table);
        return (E)this;
    }

    public Catalog getCatalog() {
        if(null == catalog && null != table){
            catalog = table.getCatalog();
        }
        return catalog;
    }
    public Schema getSchema() {
        if(null == schema && null != table){
            schema = table.getSchema();
        }
        return schema;
    }

}
