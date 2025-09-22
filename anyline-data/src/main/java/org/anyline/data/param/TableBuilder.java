/*
 * Copyright 2006-2025 www.anyline.org
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
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OriginRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TableBuilder {
    private RunPrepare prepare = null;
    private boolean distinct = false;
    private ConfigStore conditions = new DefaultConfigStore();
    private LinkedHashMap<String,Column> columns = new LinkedHashMap<>(); //需要查询的列
    private List<RunPrepare> joins = new ArrayList<>();

    /**
     * 设置完属性后调用生成RunPrepare
     * @return RunPrepare
     */
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
        if(distinct) {
            prepare.setDistinct(true);
        }
        prepare.condition(conditions);
        return prepare;
    }

    /**
     * 根据约定的JSON格式生成RunPrepare
     * @param json json
     * @return RunPrepare
     */
    public static RunPrepare build(String json) {
        TableBuilder builder = null;
        DataRow row = DataRow.parseJson(json);
        Object master = row.get("table");
        String alias = row.getString("alias");
        String distinct = row.getString("distinct");
        if(master instanceof String) {
            Table table = new Table((String)master);
            if(BasicUtil.isNotEmpty(alias)) {
                table.setAlias(alias);
            }
            builder = TableBuilder.init(table);
        }
        if(BasicUtil.isNotEmpty(distinct)) {
            builder.prepare.setDistinct(true);
        }
        DataSet joins = row.getSet("joins");
        if(null != joins) {
            for (DataRow item : joins) {
                Object item_table = item.getString("table");
                String item_alias = item.getString("alias");
                String item_join = item.getString("type");
                Join.TYPE join_type = null;
                if (BasicUtil.isNotEmpty(item_join)) {
                    join_type = Join.TYPE.valueOf(item_join.trim().toUpperCase());
                } else {
                    join_type = Join.TYPE.LEFT;
                }
                Join join = new Join();
                join.setType(join_type);

                DefaultConfigStore configs = new DefaultConfigStore();
                Object conditions = item.get("conditions");
                if (conditions instanceof DataRow) { //参考ConfigBuilder示例
                    ConfigChain chain = ConfigBuilder.parseConfigChain((DataRow) conditions);
                    configs.setChain(chain);
                    join.setConditions(configs);
                } else if (conditions instanceof String) {
                    // conditions:"FI.ID = M.USER_ID"
                    join.addConditions((String) conditions);
                } else if (conditions instanceof List) {
                    // [{"FI.ID = M.USER_ID","FI.TYPE_ID > 10"}]
                    List<String> list = (List<String>) conditions;
                    for (String con : list) {
                        join.addConditions(con);
                    }
                }
                RunPrepare prepare = null;
                if (item_table instanceof String) {
                    Table item_tab = new Table((String) item_table);
                    if (BasicUtil.isNotEmpty(item_alias)) {
                        item_tab.setAlias(item_alias);
                    }
                    prepare = new DefaultTablePrepare(item_tab);
                }
                prepare.setJoin(join);
                builder.join(prepare);
            }
        }
        Object columns = row.get("columns");
        if(columns instanceof List) {
            List cols = (List)columns;
            for(Object col:cols) {
                if(col instanceof String) {
                    builder.addColumn((String)col);
                }
            }
        }

        return builder.build();
    }
/*
{
table:"MM_USER",
alias:"MM",
columns:["MM.ID AS MM_ID", "FI.ID AS FI_ID", "FF.ACCOUNT"]
joins:[
    {
        alias:"FI",
        table:"FI_USER"
        type:"LEFT"
        conditions:{"join": "AND", items:[{"join":"AND", text:"FI.ID = M.USER_ID"}]}
    },
    {
        alias:"FI",
        table:{子查询}
        type:"LEFT"
        conditions:[{"FI.ID = M.USER_ID","FI.TYPE_ID > 10"}]
    }
]
}

*/

    public DataRow map(boolean empty) {
        DataRow row = new OriginRow();
        if(empty || null != prepare) {
            if(prepare instanceof DefaultTablePrepare) {
                DefaultTablePrepare table = (DefaultTablePrepare)prepare;
                row.put("table",  table.getTableName());
                row.put("alias", table.getTable().getAlias());
                row.put("distinct", prepare.getDistinct());
            }else{
            }
        }
        if(null != joins && !joins.isEmpty()) {
            DataSet set = new DataSet();
            for(RunPrepare item:joins) {
                set.add(item.map(empty, true));
            }
            row.put("joins", set);
        }
        if(null != columns && !columns.isEmpty()) {
            row.put("columns", Column.names(columns.values()));
        }else{
            if(null != prepare.getColumns() && !prepare.getColumns().isEmpty()) {
                row.put("columns", Column.names(prepare.getColumns().values()));
            }
        }
        return row;
    }
    public String json(boolean empty) {
        return map(empty).json();
    }
    public TableBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }
    public String json() {
        return json(false);
    }
    public TableBuilder select(String ... columns) {
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

    /**
     * 外键关联
     * SELECT u.NAME AS USER_NAME
     * LEFT JOIN CRM_USER AS U ON M.USER_ID = U.ID
     * LEFT JOIN {table} AS U ON M.{column} = U.{fk}
     * @param fk 当前主表外键列 如 USER_ID
     * @param table 外键关联表 如 user as u
     * @param pk 外键表主键 如 user.ID
     * @param intents 查询结果 别名如NAME:USER_NAME
     * @param conditions 其他关联条件
     * @return this
     */
    public TableBuilder foreign(String fk, Table table, String pk, LinkedHashMap<String, String> intents, String ... conditions){
        boolean empty = columns.isEmpty();
        String table_alias = table.getAlias();
        if(BasicUtil.isEmpty(table_alias)){
            table_alias = table.getName();
        }
        String master_alias = prepare.getAlias();
        if(BasicUtil.isEmpty(master_alias)){
            master_alias = prepare.getTableName();
        }
        for(String relation:intents.keySet()){
            String col = table_alias + "." + relation + " AS " + intents.get(relation);
            columns.put(col.toUpperCase(), new Column(col));
        }
        if(empty){
            columns.put(master_alias+".*", new Column(master_alias+".*"));
        }
        List<String> cons = new ArrayList<>();
        cons.add(master_alias+"."+fk + " = " + table_alias + "." + pk);
        if(null != conditions) {
            for (String condition:conditions) {
                cons.add(condition);
            }
        }
        join(Join.TYPE.LEFT, table, new DefaultConfigStore(), cons);
        return this;
    }

    /**
     * 外键关联
     * SELECT u.NAME AS USER_NAME
     * LEFT JOIN CRM_USER AS U ON M.USER_ID = U.ID
     * LEFT JOIN {table} AS U ON M.{column} = U.{fk}
     * @param column 当前主表外键列 如 USER_ID
     * @param table 外键关联表 如 user as u
     * @param fk 外键表主键 如 user.ID
     * @param relation 关联表查询列 如u.NAME AS USER_NAME 中的u.NAME
     * @param alias 联表查询列 别名如u.NAME AS USER_NAME 中的 USER_NAME
     * @param conditions 关联条件
     * @return this
     */
    public TableBuilder foreign(String column, Table table, String fk, String relation, String alias, String ... conditions){
        LinkedHashMap<String, String> relations = new LinkedHashMap<>();
        relations.put(relation, alias);
        return foreign(column, table, fk, relations, conditions);
    }

    public TableBuilder foreign(String column, String table, String fk, String relation, String alias, String ... conditions){
        LinkedHashMap<String, String> relations = new LinkedHashMap<>();
        relations.put(relation, alias);
        return foreign(column, table, fk, relations, conditions);
    }
    public TableBuilder foreign(String column, String table, String fk, LinkedHashMap<String, String> relations, String ... conditions){
        return foreign(column, new Table(table), fk, relations, conditions);
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
        List<String> list = SQLUtil.columns(columns);
        for(String column:list) {
            addColumn(column);
        }
        return this;
    }
    public TableBuilder addColumns(List<String> columns) {
        for(String column:columns) {
            addColumn(column);
        }
        return this;
    }
    public TableBuilder columns(String ... columns) {
        return addColumns(columns);
    }
    public TableBuilder columns(List<String> columns) {
        return addColumns(columns);
    }

    public TableBuilder setColumns(String ... columns) {
        this.columns = new LinkedHashMap<>();
        addColumns(columns);
        return this;
    }

    public TableBuilder setColumns(List<String> columns) {
        this.columns = new LinkedHashMap<>();
        addColumns(columns);
        return this;
    }

    public TableBuilder join(RunPrepare prepare) {
        this.joins.add(prepare);
        return this;
    }
    public TableBuilder join(String alias, RunPrepare prepare, Join.TYPE type, ConfigStore configs, String ... conditions) {
        Join join = new Join();
        join.setType(type);
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != conditions){
            for(String condition:conditions){
                configs.and(condition);
            }
        }
        join.setConditions(configs);
        return join(alias, prepare, join);
    }
    public TableBuilder join(String alias, RunPrepare prepare, Join.TYPE type, String ... conditions) {
        return join(alias, prepare, type, new DefaultConfigStore(), conditions);
    }
    public TableBuilder join(String alias, RunPrepare prepare, Join join) {
        prepare.setJoin(join);
        return join(alias, prepare);
    }
    public TableBuilder join(String alias, RunPrepare prepare) {
        this.joins.add(new VirtualTablePrepare(prepare).setAlias(alias));
        return this;
    }
    public TableBuilder join(Join.TYPE type, Table table, ConfigStore configs, String ... conditions) {
        RunPrepare prepare = new DefaultTablePrepare(table);
        Join join = new Join();
        join.setType(type);
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != conditions){
            for(String condition:conditions){
                configs.and(condition);
            }
        }
        join.setConditions(configs);
        prepare.setJoin(join);
        this.joins.add(prepare);
        return this;
    }
    public TableBuilder join(Join.TYPE type, Table table, String ... conditions) {
        return join(type, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder join(Join.TYPE type, Table table, ConfigStore configs, List<String> conditions) {
        RunPrepare prepare = new DefaultTablePrepare(table);
        Join join = new Join();
        join.setType(type);
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != conditions){
            for(String condition:conditions){
                configs.and(condition);
            }
        }
        join.setConditions(configs);
        prepare.setJoin(join);
        this.joins.add(prepare);
        return this;
    }
    public TableBuilder join(Join.TYPE type, Table table, List<String> conditions) {
        return join(type, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder join(Join.TYPE type, String table, ConfigStore configs, String ... conditions) {
        return join(type, new Table(table), configs, conditions);
    }
    public TableBuilder join(Join.TYPE type, String table, String ... conditions) {
        return join(type, new Table(table), new DefaultConfigStore(), conditions);
    }
    public TableBuilder inner(Table table, String ... conditions) {
        return join(Join.TYPE.INNER, table.getFullName(), conditions);
    }
    public TableBuilder inner(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.INNER, table.getFullName(), configs, conditions);
    }
    public TableBuilder inner(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.INNER, table, configs, conditions);
    }
    public TableBuilder inner(String table, String ... conditions) {
        return join(Join.TYPE.INNER, table, conditions);
    }
    public TableBuilder left(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.LEFT, table, configs, conditions);
    }
    public TableBuilder left(String table, String ... conditions) {
        return join(Join.TYPE.LEFT, table, new DefaultConfigStore(), conditions);
    }

    public TableBuilder left(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.LEFT, table, configs, conditions);
    }
    public TableBuilder left(Table table, String ... conditions) {
        return join(Join.TYPE.LEFT, table, new DefaultConfigStore(), conditions);
    }

    public TableBuilder leftLateral(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.LEFT_LATERAL, table, configs, conditions);
    }
    public TableBuilder leftLateral(String table, String ... conditions) {
        return join(Join.TYPE.LEFT_LATERAL, table, new DefaultConfigStore(), conditions);
    }

    public TableBuilder leftLateral(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.LEFT_LATERAL, table, configs, conditions);
    }
    public TableBuilder leftLateral(Table table, String ... conditions) {
        return join(Join.TYPE.LEFT_LATERAL, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder right(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, configs, conditions);
    }
    public TableBuilder right(String table, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder right(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, configs, conditions);
    }
    public TableBuilder right(Table table, String ... conditions) {
        return join(Join.TYPE.RIGHT, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder full(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.FULL, table, configs, conditions);
    }


    public TableBuilder rightLateral(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.RIGHT_LATERAL, table, configs, conditions);
    }
    public TableBuilder rightLateral(String table, String ... conditions) {
        return join(Join.TYPE.RIGHT_LATERAL, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder rightLateral(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.RIGHT_LATERAL, table, configs, conditions);
    }
    public TableBuilder rightLateral(Table table, String ... conditions) {
        return join(Join.TYPE.RIGHT_LATERAL, table, new DefaultConfigStore(), conditions);
    }
    public TableBuilder fullLateral(String table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.RIGHT_LATERAL, table, configs, conditions);
    }


    public TableBuilder full(String table, String ... conditions) {
        return join(Join.TYPE.FULL, table, new DefaultConfigStore(),  conditions);
    }

    public TableBuilder full(Table table, ConfigStore configs, String ... conditions) {
        return join(Join.TYPE.FULL, table, configs, conditions);
    }
    public TableBuilder full(Table table, String ... conditions) {
        return join(Join.TYPE.FULL, table, new DefaultConfigStore(), conditions);
    }


    public TableBuilder join(String alias, Join.TYPE type, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        Join join = new Join();
        join.setType(type);
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != conditions){
            for(String condition:conditions){
                configs.and(condition);
            }
        }
        join.setConditions(configs);
        prepare.setJoin(join);
        return join(alias, prepare);
    }
    public TableBuilder join(String alias, Join.TYPE type, RunPrepare prepare, ConfigStore configs, List<String> conditions) {
        Join join = new Join();
        join.setType(type);
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != conditions){
            for(String condition:conditions){
                configs.and(condition);
            }
        }
        join.setConditions(configs);
        prepare.setJoin(join);
        return join(alias, prepare);
    }
    public TableBuilder inner(String alias, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return join(alias, Join.TYPE.INNER, prepare, configs, conditions);
    }
    public TableBuilder inner(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.INNER, prepare, new DefaultConfigStore(), conditions);
    }
    public TableBuilder left(String alias, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return join(alias, Join.TYPE.LEFT, prepare, configs, conditions);
    }
    public TableBuilder left(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.LEFT, prepare, new DefaultConfigStore(), conditions);
    }
    public TableBuilder right(String alias, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return join(alias, Join.TYPE.RIGHT, prepare, configs, conditions);
    }

    public TableBuilder right(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.RIGHT, prepare, new DefaultConfigStore(), conditions);
    }

    public TableBuilder full(String alias, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return join(alias, Join.TYPE.FULL, prepare, configs, conditions);
    }
    public TableBuilder full(String alias, RunPrepare prepare, String ... conditions) {
        return join(alias, Join.TYPE.FULL, prepare, new DefaultConfigStore(), conditions);
    }
}
