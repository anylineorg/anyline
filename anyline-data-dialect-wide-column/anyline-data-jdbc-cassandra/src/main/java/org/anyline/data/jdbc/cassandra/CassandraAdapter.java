/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.jdbc.cassandra;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;

import java.util.*;

@AnylineComponent("anyline.data.jdbc.adapter.cassandra")
public class CassandraAdapter extends AbstractJDBCAdapter implements JDBCAdapter {

    /* *****************************************************************************************************************
     *
     *                                           Constructor & Init
     *
     * ****************************************************************************************************************/

    public CassandraAdapter() {
        super();
        // Register MetadataReferHolders for Cassandra (no JDBC metadata column refs)
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Refer(null, null, null, 0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.INT, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.DATE, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIME, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Refer(null, null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Refer(null, null, null, 1, 1, 1));

        // Register TypeMetadataAlias
        for (CassandraTypeMetadataAlias alias : CassandraTypeMetadataAlias.values()) {
            clear(alias);
        }
        for (CassandraTypeMetadataAlias alias : CassandraTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
        delimiterFr = "";
        delimiterTo = "";
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.Cassandra;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return true;
    }

    @Override
    public void setListener(DDListener listener) {
    }

    @Override
    public void setListener(DMListener listener) {
    }

    /* *****************************************************************************************************************
     *
     *                                           DML (SELECT)
     *
     * ****************************************************************************************************************/

    /**
     * Cassandra SELECT pagination: LIMIT limit
     */
    @Override
    public String mergeFinalSelect(DataRuntime runtime, Run run) {
        String sql = run.getBaseSelect();
        String cols = run.getSelectColumn();
        if (!"*".equals(cols)) {
            String reg = "(?i)^select[\\s\\S]+from";
            sql = sql.replaceAll(reg, "SELECT " + cols + " FROM ");
        }
        OrderStore orders = run.getOrders();
        if (null != orders) {
            sql += orders.getRunText(getDelimiterFr() + getDelimiterTo());
        }
        PageNavi navi = run.getPageNavi();
        if (null != navi) {
            long limit = navi.getLastRow() - navi.getFirstRow() + 1;
            if (limit < 0) {
                limit = 0;
            }
            sql += " LIMIT " + limit;
        }
        sql = compressCondition(runtime, sql);
        return sql;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return concatFun(runtime, args);
    }

    /* *****************************************************************************************************************
     *
     *                                           DDL (CREATE TABLE)
     *
     * ****************************************************************************************************************/

    /**
     * CREATE TABLE for Cassandra.
     * Key differences from standard SQL:
     * - Uses PRIMARY KEY ((partition_keys), clustering_columns) syntax
     * - WITH clause for table options (CLUSTERING ORDER, compaction, compression, etc.)
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("CREATE TABLE ");
        checkTableExists(runtime, builder, false);
        name(runtime, builder, meta);
        builder.append("(");

        // Columns
        body(runtime, builder, meta);

        // Primary Key
        builder.deleteCharAt(builder.length() - 1);
        builder.append(", ");
        primary(runtime, builder, meta);
        builder.append(")");

        // Table options
        engine(runtime, builder, meta);
        comment(runtime, builder, meta);
        property(runtime, builder, meta);

        runs.addAll(buildAppendCommentRun(runtime, meta));
        runs.addAll(buildAppendColumnCommentRun(runtime, meta));
        runs.addAll(buildAppendIndexRun(runtime, meta));
        return runs;
    }

    /**
     * Cassandra PRIMARY KEY syntax:
     * PRIMARY KEY (partition_col)                          -- single partition key
     * PRIMARY KEY ((partition_key1, partition_key2), clustering_col1, clustering_col2)  -- composite
     */
    @Override
    public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta) {
        LinkedHashMap<String, Column> pks = meta.getPrimaryKeyColumns();
        if (null == pks || pks.isEmpty()) {
            return builder;
        }
        builder.append("PRIMARY KEY (");
        boolean first = true;
        for (Column col : pks.values()) {
            if (!first) builder.append(", ");
            first = false;
            builder.append(getDelimiterFr()).append(col.getName()).append(getDelimiterTo());
        }
        builder.append(")");
        return builder;
    }

    /* *****************************************************************************************************************
     *
     *                                           DDL (DROP)
     *
     * ****************************************************************************************************************/

    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("DROP TABLE ");
        checkTableExists(runtime, builder, true);
        name(runtime, builder, meta);
        return runs;
    }

    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("TRUNCATE ");
        name(runtime, builder, table);
        return runs;
    }

    /* *****************************************************************************************************************
     *
     *                                           DDL (CREATE KEYSPACE)
     *
     * ****************************************************************************************************************/

    /**
     * CREATE KEYSPACE instead of CREATE DATABASE for Cassandra.
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Database meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("CREATE KEYSPACE ");
        checkDatabaseExists(runtime, builder, false);
        builder.append(getDelimiterFr()).append(meta.getName()).append(getDelimiterTo());
        builder.append(" WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        return runs;
    }

    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Database meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("DROP KEYSPACE ");
        checkDatabaseExists(runtime, builder, true);
        builder.append(getDelimiterFr()).append(meta.getName()).append(getDelimiterTo());
        return runs;
    }

    /* *****************************************************************************************************************
     *
     *                                           Metadata Query
     *
     * ****************************************************************************************************************/

    /**
     * Query databases (keyspaces) from system_schema.keyspaces.
     */
    @Override
    public List<Run> buildSelectDatabasesRun(DataRuntime runtime, boolean greedy, Database query) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT keyspace_name FROM system_schema.keyspaces");
        if (null != query && BasicUtil.isNotEmpty(query.getName())) {
            builder.append(" WHERE keyspace_name='").append(query.getName()).append("'");
        }
        return runs;
    }

    /**
     * Query tables from system_schema.tables.
     */
    @Override
    public <T extends Table<T>> List<Run> buildSelectTablesRun(DataRuntime runtime, boolean greedy, Table<T> query, int types, ConfigStore configs) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();

        builder.append("SELECT keyspace_name, table_name, comment FROM system_schema.tables");
        if (null != query) {
            String schema = query.getSchemaName();
            String name = query.getName();
            List<String> conditions = new ArrayList<>();
            if (BasicUtil.isNotEmpty(schema)) {
                conditions.add("keyspace_name='" + schema + "'");
            }
            if (BasicUtil.isNotEmpty(name)) {
                conditions.add("table_name='" + name + "'");
            }
            if (!conditions.isEmpty()) {
                builder.append(" WHERE ").append(String.join(" AND ", conditions));
            }
        }
        return runs;
    }

    /**
     * Query columns from system_schema.columns.
     */
    @Override
    public List<Run> buildSelectColumnsRun(DataRuntime runtime, boolean metadata, Column query, ConfigStore configs) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();

        builder.append("SELECT keyspace_name, table_name, column_name, type, kind, position, clustering_order FROM system_schema.columns");
        if (null != query) {
            Table table = query.getTable();
            String schema = null != table ? table.getSchemaName() : null;
            String tableName = null != table ? table.getName() : null;
            List<String> conditions = new ArrayList<>();
            if (BasicUtil.isNotEmpty(schema)) {
                conditions.add("keyspace_name='" + schema + "'");
            }
            if (BasicUtil.isNotEmpty(tableName)) {
                conditions.add("table_name='" + tableName + "'");
            }
            if (!conditions.isEmpty()) {
                builder.append(" WHERE ").append(String.join(" AND ", conditions));
            }
        }
        return runs;
    }

    /* *****************************************************************************************************************
     *
     *                                           Metadata Field References
     *
     * ****************************************************************************************************************/

    @Override
    public MetadataFieldRefer initDatabaseFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Database.class);
        refer.map(Database.FIELD_NAME, "keyspace_name,KEYSPACE_NAME");
        return refer;
    }

    @Override
    public MetadataFieldRefer initTableFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Table.class);
        refer.map(Table.FIELD_NAME, "table_name,TABLE_NAME,NAME,TABNAME");
        refer.map(Table.FIELD_CATALOG, "keyspace_name,KEYSPACE_NAME");
        refer.map(Table.FIELD_SCHEMA, "keyspace_name,KEYSPACE_NAME");
        refer.map(Table.FIELD_COMMENT, "comment,COMMENT,TABLE_COMMENT,COMMENTS");
        return refer;
    }

    @Override
    public MetadataFieldRefer initColumnFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Column.class);
        refer.map(Column.FIELD_NAME, "column_name,COLUMN_NAME");
        refer.map(Column.FIELD_CATALOG, "keyspace_name,KEYSPACE_NAME");
        refer.map(Column.FIELD_SCHEMA, "keyspace_name,KEYSPACE_NAME");
        refer.map(Column.FIELD_TABLE, "table_name,TABLE_NAME");
        refer.map(Column.FIELD_POSITION, "position,POSITION,ORDINAL_POSITION");
        refer.map(Column.FIELD_NULLABLE, "kind,KIND");
        return refer;
    }

    @Override
    public MetadataFieldRefer initIndexFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Index.class);
        refer.map(Index.FIELD_NAME, "index_name,INDEX_NAME");
        refer.map(Index.FIELD_CATALOG, "keyspace_name,KEYSPACE_NAME");
        refer.map(Index.FIELD_SCHEMA, "keyspace_name,KEYSPACE_NAME");
        refer.map(Index.FIELD_TABLE, "table_name,TABLE_NAME");
        refer.map(Index.FIELD_COLUMN, "options,OPTIONS");
        return refer;
    }

    /* *****************************************************************************************************************
     *
     *                                           Column Detail
     *
     * ****************************************************************************************************************/

    @Override
    public <T extends Column> T detail(DataRuntime runtime, int index, T meta, Column query, DataRow row) {
        if (null == meta) {
            return null;
        }
        // kind: partition_key, clustering, regular, static
        String kind = row.getString("kind", "KIND");
        if ("partition_key".equalsIgnoreCase(kind)) {
            meta.setPrimaryKey(true);
        } else if ("clustering".equalsIgnoreCase(kind)) {
            meta.setPrimaryKey(true);
        }
        return meta;
    }
}