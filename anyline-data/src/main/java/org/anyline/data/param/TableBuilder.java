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
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TableBuilder {

    private Table table;
    private String datasource;
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>(); //需要查询的列
    private List<Join> joins = new ArrayList<>();//关联表

    public static TableBuilder init() {
        TableBuilder builder = new TableBuilder();
        return builder;
    }
    public static TableBuilder init(String table) {
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        return builder;
    }
    public static TableBuilder init(Table table) {
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        return builder;
    }
    public static TableBuilder init(String table, String columns) {
        TableBuilder builder = new TableBuilder();
        builder.setTable(table);
        builder.addColumns(columns);
        return builder;
    }

    public TableBuilder setDataSource(String datasoruce) {
        this.datasource = datasoruce;
        return this;
    }
    public TableBuilder setTable(String table) {
        this.table = new Table(table);
        return this;
    }
    public TableBuilder setTable(Table table) {
        this.table = table;
        return this;
    }
    public TableBuilder addColumn(String column) {
        if(!columns.containsKey(column.toUpperCase())) {
            columns.put(column.toUpperCase(), new Column(column));
        }
        return this;
    }
    public TableBuilder addColumns(String ... columns) {
        if(null != columns) {
            for (String column:columns) {
                addColumn(column);
            }
        }
        return this;
    }
    public RunPrepare build() {
        DefaultTablePrepare prepare = new DefaultTablePrepare();
        prepare.setDest(datasource);
        prepare.setTable(table);
        for(Join join:joins) {
            prepare.join(join);
        }
        for(Column col: columns.values()) {
            prepare.addColumn(col);
        }
        return prepare;
    }

    public TableBuilder join(Join join) {
        joins.add(join);
        return this;
    }
    public TableBuilder join(Join.TYPE type, String table, String condition) {
        return join(type, new Table(table), condition);
    }
    public TableBuilder join(Join.TYPE type, Table table, String condition) {
        Join join = new Join();
        join.setTable(table);
        join.setType(type);
        join.setCondition(condition);
        return join(join);
    }
    public Table getTable() {
        return table;
    }
    public TableBuilder inner(Table table, String condition) {
        return join(Join.TYPE.INNER, table.getFullName(), condition);
    }
    public TableBuilder inner(String table, String condition) {
        return join(Join.TYPE.INNER, table, condition);
    }
    public TableBuilder left(String table, String condition) {
        return join(Join.TYPE.LEFT, table, condition);
    }
    public TableBuilder left(Table table, String condition) {
        return join(Join.TYPE.LEFT, table, condition);
    }
    public TableBuilder right(String table, String condition) {
        return join(Join.TYPE.RIGHT, table, condition);
    }
    public TableBuilder right(Table table, String condition) {
        return join(Join.TYPE.RIGHT, table, condition);
    }
    public TableBuilder full(String table, String condition) {
        return join(Join.TYPE.FULL, table, condition);
    }

    public TableBuilder full(Table table, String condition) {
        return join(Join.TYPE.FULL, table, condition);
    }

}
