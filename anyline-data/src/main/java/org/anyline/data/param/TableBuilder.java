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


package org.anyline.data.param;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.entity.Join;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {

    private String table;
    private String datasource;
    private String alias;
    private List<String> queryColumns = new ArrayList<>();
    private List<Join> joins = new ArrayList<Join>();//关联表

    public static TableBuilder init(){
        TableBuilder builder = new TableBuilder();
        return builder;
    }
    public static TableBuilder init(String table){
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        return builder;
    }
    public static TableBuilder init(String table, String columns){
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        builder.addColumns(columns);
        return builder;
    }

    public TableBuilder setDatasource(String datasoruce){
        this.datasource = datasoruce;
        return this;
    }
    public TableBuilder setAlias(String alias){
        this.alias = alias;
        return this;
    }
    public TableBuilder setTable(String table){
        this.table = table;
        return this;
    }
    public TableBuilder addColumn(String column){
        if(!queryColumns.contains(column)){
            queryColumns.add(column);
        }
        return this;
    }
    public TableBuilder addColumns(String ... columns){
        if(null != columns) {
            for (String column:columns) {
                addColumn(column);
            }
        }
        return this;
    }
    public RunPrepare build(){
        DefaultTablePrepare sql = new DefaultTablePrepare();
        sql.setDest(datasource);
        sql.setTable(table);
        if(BasicUtil.isNotEmpty(alias)) {
            sql.setAlias(alias);
        }
        for(Join join:joins){
            sql.join(join);
        }
        for(String col:queryColumns) {
            sql.addColumn(col);
        }
        return sql;
    }

    public TableBuilder join(Join join){
        joins.add(join);
        return this;
    }
    public TableBuilder join(Join.TYPE type, String table, String condition){
        Join join = new Join();
        join.setName(table);
        join.setType(type);
        join.setCondition(condition);
        return join(join);
    }
    public String getTable(){
        return table;
    }
    public TableBuilder inner(String table, String condition){
        return join(Join.TYPE.INNER, table, condition);
    }
    public TableBuilder left(String table, String condition){
        return join(Join.TYPE.LEFT, table, condition);
    }
    public TableBuilder right(String table, String condition){
        return join(Join.TYPE.RIGHT, table, condition);
    }
    public TableBuilder full(String table, String condition){
        return join(Join.TYPE.FULL, table, condition);
    }

}
