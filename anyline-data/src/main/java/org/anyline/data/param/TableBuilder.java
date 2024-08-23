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

import org.anyline.data.entity.Join;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.VirtualTablePrepare;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TableBuilder {
    private RunPrepare prepare = null;
    private ConfigStore conditions = new DefaultConfigStore();
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>(); //需要查询的列
    private List<RunPrepare> joins = new ArrayList<>();

    public RunPrepare build() {
        for (RunPrepare item : joins) {
            prepare.join(item);
        }
        if(null != columns && !columns.isEmpty()) {
            prepare.getColumns().clear();
            for(Column col: columns.values()) {
                prepare.addColumn(col);
            }
        }
        prepare.condition(conditions);
        return prepare;
    }
    public TableBuilder select(String ... columns){
        return columns(columns);
    }
    public static TableBuilder init() {
        TableBuilder builder = new TableBuilder();
        return builder;
    }
    public static TableBuilder init(String table) {
        return from(table);
    }
    public static TableBuilder from(String table) {
        return from(new Table(table));
    }
    public static TableBuilder init(Table table) {
        return from(table);
    }
    public static TableBuilder from(Table table) {
        TableBuilder builder = new TableBuilder();
        builder.prepare = new DefaultTablePrepare(table);
        return builder;
    }
    public static TableBuilder init(String alias, RunPrepare prepare) {
        return from(alias, prepare);
    }
    public static TableBuilder from(String alias, RunPrepare prepare) {
        TableBuilder builder = new TableBuilder();
        builder.prepare = new VirtualTablePrepare(prepare).setAlias(alias);
        return builder;
    }
    public static TableBuilder init(String table, String columns) {
        TableBuilder builder = init(table);
        builder.addColumns(columns);
        return builder;
    }
    public TableBuilder condition(ConfigStore configs) {
        this.conditions = configs;
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
    public TableBuilder columns(String ... columns) {
        return addColumns(columns);
    }

    public TableBuilder setColumns(String ... columns) {
        this.columns = new LinkedHashMap<>();
        if(null != columns) {
            for (String column:columns) {
                addColumn(column);
            }
        }
        return this;
    }

    public TableBuilder join(String alias, RunPrepare prepare, Join.TYPE type, ConfigStore configs) {
        Join join = new Join();
        join.setType(type);
        join.setConditions(configs);
        return join(alias, prepare, join);
    }
    public TableBuilder join(String alias, RunPrepare prepare, Join.TYPE type, String ... conditions) {
        Join join = new Join();
        join.setType(type);
        join.setConditions(conditions);
        return join(alias, prepare, join);
    }
    public TableBuilder join(String alias, RunPrepare prepare, Join join) {
        prepare.setJoin(join);
        return join(alias, prepare);
    }
    public TableBuilder join(String alias, RunPrepare prepare) {
        this.joins.add(new VirtualTablePrepare(prepare).setAlias(alias));
        return this;
    }
    public TableBuilder join(Join.TYPE type, String table, String ... conditions) {
        return join(type, new Table(table), conditions);
    }
    public TableBuilder join(Join.TYPE type, Table table, String ... conditions) {
        RunPrepare prepare = new DefaultTablePrepare(table);
        Join join = new Join();
        join.setType(type);
        join.setConditions(conditions);
        prepare.setJoin(join);
        this.joins.add(prepare);
        return this;
    }
    public TableBuilder inner(Table table, String ... conditions) {
        return join(Join.TYPE.INNER, table.getFullName(), conditions);
    }
    public TableBuilder inner(String table, String ... conditions) {
        return join(Join.TYPE.INNER, table, conditions);
    }
    public TableBuilder left(String table, String ... conditions) {
        return join(Join.TYPE.LEFT, table, conditions);
    }
    public TableBuilder left(Table table, String ... conditions) {
        return join(Join.TYPE.LEFT, table, conditions);
    }
    public TableBuilder right(String table, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, conditions);
    }
    public TableBuilder right(Table table, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, conditions);
    }
    public TableBuilder full(String table, String ... conditions) {
        return join(Join.TYPE.FULL, table, conditions);
    }

    public TableBuilder full(Table table, String ... conditions) {
        return join(Join.TYPE.FULL, table, conditions);
    }


    public TableBuilder join(String alias, Join.TYPE type, RunPrepare prepare, String ... conditions) {
        Join join = new Join();
        join.setType(type);
        join.setConditions(conditions);
        prepare.setJoin(join);
        return join(alias, prepare);
    }
    public TableBuilder inner(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.INNER, prepare, conditions);
    }
    public TableBuilder left(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.LEFT, prepare, conditions);
    }
    public TableBuilder right(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.RIGHT, prepare, conditions);
    }

    public TableBuilder full(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.FULL, prepare, conditions);
    }
}
