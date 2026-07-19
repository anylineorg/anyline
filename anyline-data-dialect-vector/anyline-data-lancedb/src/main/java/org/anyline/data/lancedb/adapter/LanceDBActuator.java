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


package org.anyline.data.lancedb.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.lancedb.metadata.LanceDBCollection;
import org.anyline.data.lancedb.run.LanceDBRun;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import org.lance.namespace.model.QueryTableRequestColumns;

@AnylineComponent("anyline.environment.data.driver.actuator.lancedb")
public class LanceDBActuator implements DriverActuator {
    private static final Log log = LogProxy.get(LanceDBActuator.class);

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return LanceDBAdapter.class;
    }

    @Override
    public int priority() {
        return 0;
    }
    @Override
    public DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime) {
        return null;
    }
    @Override
    public Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource) {
        return null;
    }
    @Override
    public void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource) {

    }
    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, T meta) {

    }
    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta) {

    }
    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta) {

    }

    @Override
    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        return "LanceDB";
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    /**
     * 获取 LanceNamespace 客户端
     */
    private LanceNamespace client(DataRuntime runtime) {
        return (LanceNamespace) runtime.getProcessor();
    }

    /**
     * 数据库列表 → LanceDB 中对应 namespace 列表
     */
    @Override
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> list = new ArrayList<>();
        LanceNamespace client = client(runtime);
        try {
            ListNamespacesRequest req = new ListNamespacesRequest();
            req.setId(Collections.emptyList());
            ListNamespacesResponse resp = client.listNamespaces(req);
            if(resp != null && resp.getNamespaces() != null) {
                for(String name : resp.getNamespaces()) {
                    Database database = new Database(name);
                    list.add((T)database);
                }
            }
        } catch (Exception e) {
            log.error("LanceDB list namespaces 异常:", e);
        }
        return list;
    }
    @Override
    public List<Catalog> catalogs(DriverAdapter adapter, DataRuntime runtime) {
        return new ArrayList<>();
    }

    @Override
    public List<Schema> schemas(DriverAdapter adapter, DataRuntime runtime) {
        return new ArrayList<>();
    }

    @Override
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String,Column> columns) throws Exception {
        DataSet<DataRow> result = new DataSet<>();
        if(action == ACTION.DML.SELECT) {
            List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
            for(Map<String, Object> map : maps) {
                DataRow row = new DataRow();
                row.putAll(map);
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception {
        return new DataSet();
    }

    /**
     * select [命令执行]<br/>
     * LanceDB 查询：向量搜索 或 标量过滤查询
     */
    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        LanceNamespace client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();
            List<String> selectColumns = tableRun.getSelectColumns();
            List<RunValue> runValues = tableRun.getRunValues();
            PageNavi navi = tableRun.getPageNavi();
            OrderStore orders = tableRun.getOrders();

            // 检测是否是向量搜索
            Object vectorParam = null;
            String vectorField = null;
            int topK = 10;
            if(run instanceof LanceDBRun) {
                LanceDBRun ldbRun = (LanceDBRun) run;
                if(ldbRun.isVectorSearch() && ldbRun.getQueryVector() != null) {
                    vectorParam = ldbRun.getQueryVector();
                    vectorField = ldbRun.getVectorField();
                    topK = ldbRun.getTopK();
                }
            }
            if(null != runValues && vectorParam == null) {
                for(RunValue rv : runValues) {
                    Object value = rv.getValue();
                    if(value instanceof List && !((List<?>)value).isEmpty()
                        && ((List<?>)value).get(0) instanceof Number) {
                        vectorParam = value;
                        vectorField = rv.getKey();
                        break;
                    }
                }
            }
            if(null != navi && vectorParam != null) {
                topK = navi.getPageRows();
                if(topK <= 0) {
                    topK = 10;
                }
            }

            // 构建filter表达式
            String filterExpr = null;
            if(run instanceof LanceDBRun) {
                LanceDBRun ldbRun = (LanceDBRun) run;
                filterExpr = ldbRun.getFilter();
            }
            if(null == filterExpr) {
                filterExpr = buildFilter(runValues, vectorField);
            }

            // 查询执行
            if(vectorParam != null && vectorField != null) {
                // 向量搜索
                result = vectorSearch(client, tableName, vectorField, (List<Float>) vectorParam,
                    topK, selectColumns, filterExpr);
            } else {
                // 标量过滤查询
                result = queryData(client, tableName, selectColumns, filterExpr, navi);
            }
        }
        return result;
    }

    /**
     * 向量搜索
     */
    private List<Map<String, Object>> vectorSearch(LanceNamespace client, String tableName, String vectorField,
            List<Float> vector, int topK, List<String> selectColumns, String filterExpr) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            QueryTableRequest query = new QueryTableRequest();
            query.setId(Arrays.asList(tableName));
            query.setK(topK);

            QueryTableRequestVector vectorReq = new QueryTableRequestVector();
            vectorReq.setSingleVector(vector);
            query.setVector(vectorReq);

            if(selectColumns != null && !selectColumns.isEmpty()) {
                QueryTableRequestColumns columns = new QueryTableRequestColumns();
                columns.setColumnNames(selectColumns);
                query.setColumns(columns);
            }
            if(filterExpr != null && !filterExpr.isEmpty()) {
                query.setFilter(filterExpr);
            }

            byte[] arrowData = client.queryTable(query);
            result = parseArrowIpc(arrowData);
        } catch (Exception e) {
            log.error("LanceDB vector search 异常:", e);
        }
        return result;
    }

    /**
     * 标量过滤查询
     */
    private List<Map<String, Object>> queryData(LanceNamespace client, String tableName,
            List<String> selectColumns, String filterExpr, PageNavi navi) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            QueryTableRequest query = new QueryTableRequest();
            query.setId(Arrays.asList(tableName));
            // 对于非向量查询，使用filter
            if(filterExpr != null && !filterExpr.isEmpty()) {
                query.setFilter(filterExpr);
            }
            if(selectColumns != null && !selectColumns.isEmpty()) {
                QueryTableRequestColumns columns = new QueryTableRequestColumns();
                columns.setColumnNames(selectColumns);
                query.setColumns(columns);
            }
            // 设置limit
            if(navi != null && navi.getPageRows() > 0) {
                // LanceDB query does not support offset/limit directly
                // Results are paginated client-side
            }

            byte[] arrowData = client.queryTable(query);
            result = parseArrowIpc(arrowData);

            // 客户端分页
            if(navi != null && navi.getFirstRow() > 0) {
                int start = (int) navi.getFirstRow();
                if(start < result.size()) {
                    result = result.subList(start, result.size());
                }
            }
            if(navi != null && navi.getPageRows() > 0) {
                if(result.size() > navi.getPageRows()) {
                    result = result.subList(0, navi.getPageRows());
                }
            }
        } catch (Exception e) {
            log.error("LanceDB query 异常:", e);
        }
        return result;
    }

    /**
     * 解析 Arrow IPC 格式结果为 Map 列表
     * LanceDB 返回 Arrow IPC 字节，需用 ArrowFileReader 解析
     */
    private List<Map<String, Object>> parseArrowIpc(byte[] arrowData) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        if(arrowData == null || arrowData.length == 0) {
            return result;
        }

        try {
            org.apache.arrow.memory.RootAllocator allocator = new org.apache.arrow.memory.RootAllocator();
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(arrowData);
            org.apache.arrow.vector.ipc.ArrowStreamReader reader = new org.apache.arrow.vector.ipc.ArrowStreamReader(bais, allocator);

            while(reader.loadNextBatch()) {
                org.apache.arrow.vector.VectorSchemaRoot root = reader.getVectorSchemaRoot();
                if(root == null) continue;

                int rowCount = root.getRowCount();
                List<org.apache.arrow.vector.FieldVector> fieldVectors = root.getFieldVectors();
                List<String> fieldNames = new ArrayList<>();
                for(org.apache.arrow.vector.FieldVector fv : fieldVectors) {
                    fieldNames.add(fv.getName());
                }

                for(int i = 0; i < rowCount; i++) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for(int j = 0; j < fieldVectors.size(); j++) {
                        org.apache.arrow.vector.FieldVector fv = fieldVectors.get(j);
                        Object value = fv.getObject(i);
                        row.put(fieldNames.get(j), value);
                    }
                    result.add(row);
                }
            }
            reader.close();
            allocator.close();
        } catch (Exception e) {
            log.error("解析 Arrow IPC 数据异常:", e);
        }
        return result;
    }

    /**
     * 构建 LanceDB filter 表达式
     */
    private String buildFilter(List<RunValue> runValues, String excludeField) {
        if(null == runValues || runValues.isEmpty()) {
            return null;
        }
        StringBuilder filterBuilder = new StringBuilder();
        for(RunValue rv : runValues) {
            String key = rv.getKey();
            Object value = rv.getValue();
            if(value instanceof List) {
                continue;
            }
            if(excludeField != null && excludeField.equals(key)) {
                continue;
            }
            if(BasicUtil.isEmpty(key)) {
                continue;
            }
            if(filterBuilder.length() > 0) {
                filterBuilder.append(" AND ");
            }
            if(value instanceof String) {
                filterBuilder.append(key).append(" = '").append(value).append("'");
            } else if(value instanceof Number) {
                filterBuilder.append(key).append(" = ").append(value);
            } else {
                filterBuilder.append(key).append(" = '").append(value).append("'");
            }
        }
        return filterBuilder.length() > 0 ? filterBuilder.toString() : null;
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

    /**
     * count 统计
     */
    public long count(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        LanceNamespace client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();

            CountTableRowsRequest req = new CountTableRowsRequest();
            req.setId(Arrays.asList(tableName));
            Long count = client.countTableRows(req);
            return count != null ? count : -1;
        }
        return -1;
    }

    /**
     * 执行insert
     */
    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        LanceNamespace client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();

            // LanceDB insert uses Arrow IPC format
            // For now, we handle simple data objects
            List<Map<String, Object>> rows = new ArrayList<>();
            if(data instanceof List) {
                for(Object item : (List<?>) data) {
                    if(item instanceof Map) {
                        rows.add((Map<String, Object>) item);
                    } else if(item instanceof DataRow) {
                        rows.add(((DataRow) item).toMap());
                    }
                }
            } else if(data instanceof Map) {
                rows.add((Map<String, Object>) data);
            } else if(data instanceof DataRow) {
                rows.add(((DataRow) data).toMap());
            }

            if(!rows.isEmpty()) {
                // Build Arrow IPC data from maps
                byte[] arrowData = buildArrowIpcData(rows);
                if(arrowData != null) {
                    InsertIntoTableRequest req = new InsertIntoTableRequest();
                    req.setId(Arrays.asList(tableName));
                    req.setMode("append");
                    client.insertIntoTable(req, arrowData);
                    return rows.size();
                }
            }
        }
        return -1;
    }

    /**
     * 将 Map 列表转换为 Arrow IPC 字节数组
     */
    private byte[] buildArrowIpcData(List<Map<String, Object>> rows) {
        if(rows == null || rows.isEmpty()) {
            return null;
        }
        try {
            org.apache.arrow.memory.RootAllocator allocator = new org.apache.arrow.memory.RootAllocator();

            // 收集所有列名
            LinkedHashSet<String> allColumns = new LinkedHashSet<>();
            for(Map<String, Object> row : rows) {
                allColumns.addAll(row.keySet());
            }

            // 构建 schema
            List<org.apache.arrow.vector.types.pojo.Field> fields = new ArrayList<>();
            for(String col : allColumns) {
                fields.add(new org.apache.arrow.vector.types.pojo.Field(col,
                    org.apache.arrow.vector.types.pojo.FieldType.nullable(org.apache.arrow.vector.types.Types.MinorType.VARCHAR.getType()),
                    null));
            }
            org.apache.arrow.vector.types.pojo.Schema schema = new org.apache.arrow.vector.types.pojo.Schema(fields);

            // 使用 VectorSchemaRoot
            org.apache.arrow.vector.VectorSchemaRoot root = org.apache.arrow.vector.VectorSchemaRoot.create(schema, allocator);
            root.allocateNew();

            // 填充数据
            for(int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);
                for(int j = 0; j < fields.size(); j++) {
                    String colName = fields.get(j).getName();
                    Object value = row.get(colName);
                    org.apache.arrow.vector.VarCharVector vector = (org.apache.arrow.vector.VarCharVector) root.getVector(colName);
                    if(value != null) {
                        vector.setSafe(i, value.toString().getBytes());
                    } else {
                        vector.setNull(i);
                    }
                }
            }
            root.setRowCount(rows.size());

            // 序列化为 Arrow IPC 字节
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            org.apache.arrow.vector.ipc.ArrowStreamWriter writer = new org.apache.arrow.vector.ipc.ArrowStreamWriter(root, null, baos);
            writer.writeBatch();
            writer.close();
            root.close();
            allocator.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("构建 Arrow IPC 数据异常:", e);
            return null;
        }
    }

    /**
     * update [命令执行]<br/>
     * LanceDB DDL操作(create/drop) 通过此方法执行
     */
    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long result = 0;
        if(null != run){
            ACTION action = run.action();
            if(action == ACTION.DDL.DATABASE_CREATE){
                create(runtime, (Database) run.metadata());
                result = 1;
            }else if (action == ACTION.DDL.DATABASE_DROP){
                drop(runtime, (Database) run.metadata());
                result = 1;
            }else if(action == ACTION.DDL.TABLE_CREATE){
                create(runtime, (Table)run.metadata());
            }else if(action == ACTION.DDL.TABLE_DROP){
                drop(runtime, (Table)run.metadata());
            }
        }
        return result;
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    /**
     * execute [命令执行]<br/>
     * LanceDB 删除操作
     */
    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        LanceNamespace client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();

            String filterExpr = null;
            if(run instanceof LanceDBRun) {
                LanceDBRun ldbRun = (LanceDBRun) run;
                filterExpr = ldbRun.getFilter();
            }
            if(filterExpr == null) {
                List<RunValue> runValues = tableRun.getRunValues();
                filterExpr = buildFilter(runValues, null);
            }

            if(filterExpr != null) {
                DeleteFromTableRequest req = new DeleteFromTableRequest();
                req.setId(Arrays.asList(tableName));
                req.setPredicate(filterExpr);
                client.deleteFromTable(req);
                return 1;
            }
        }
        return -1;
    }
    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, List<Run> run) throws Exception {
        long total = 0;
        for(Run r : run) {
            total += execute(adapter, runtime, random, configs, r);
        }
        return total;
    }

    /**
     * truncate 清空表数据
     */
    public long truncate(DriverAdapter adapter, DataRuntime runtime, String random, Table table) throws Exception {
        LanceNamespace client = client(runtime);
        DeleteFromTableRequest req = new DeleteFromTableRequest();
        req.setId(Arrays.asList(table.getName()));
        req.setPredicate("true");
        client.deleteFromTable(req);
        return 1;
    }

    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    /**
     * table[结果集封装]<br/>
     * 根据 LanceDB SDK 查询表列表
     */
    @Override
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        if(null == previous){
            previous = new LinkedHashMap<>();
        }
        LanceNamespace client = client(runtime);
        String name = query.getName();
        if(BasicUtil.isNotEmpty(name)){
            // 查询指定表
            boolean empty = previous.isEmpty();
            if(empty || !previous.containsKey(name.toUpperCase())) {
                LanceDBCollection table = new LanceDBCollection();
                table.setName(name);
                previous.put(name.toUpperCase(), (T) table);
            }
            // 补充列信息
            for(T item : previous.values()) {
                try {
                    DescribeTableRequest req = new DescribeTableRequest();
                    req.setId(Arrays.asList(item.getName()));
                    DescribeTableResponse resp = client.describeTable(req);
                    if(resp != null && resp.getSchema() != null) {
                        // LanceDB Schema 可以通过 describeTable 返回的 schema 信息解析列
                    }
                } catch(Exception e) {
                    log.warn("describe table 异常: {}", e.getMessage());
                }
            }
        } else {
            ListTablesRequest req = new ListTablesRequest();
            req.setId(Collections.emptyList());
            ListTablesResponse resp = client.listTables(req);
            List<String> names = new ArrayList<>(resp.getTables());
            for(String item : names) {
                boolean append = previous.isEmpty() || !previous.containsKey(item.toUpperCase());
                if(append) {
                    LanceDBCollection table = new LanceDBCollection();
                    table.setName(item);
                    previous.put(item.toUpperCase(), (T) table);
                }
            }
        }
        return previous;
    }

    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if(null == previous){
            previous = new ArrayList<>();
        }
        LanceNamespace client = client(runtime);
        boolean empty = previous.isEmpty();
        ListTablesRequest req = new ListTablesRequest();
        req.setId(Collections.emptyList());
        ListTablesResponse resp = client.listTables(req);
        List<String> names = new ArrayList<>(resp.getTables());
        for(String name : names){
            boolean append = true;
            if(!empty){
                for(Table item : previous){
                    if(name.equalsIgnoreCase(item.getName())){
                        append = false;
                        break;
                    }
                }
            }
            if(append){
                LanceDBCollection table = new LanceDBCollection();
                table.setName(name);
                previous.add((T)table);
            }
        }
        return previous;
    }

    @Override
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, View query, int types) throws Exception {
        return previous;
    }

    @Override
    public <T extends View> List<T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, View query, int types) throws Exception {
        return previous;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table table, String cmd) throws Exception {
        return previous;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Column query) throws Exception {
        return new LinkedHashMap<>();
    }

    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
        return new LinkedHashMap<>();
    }

    /* *****************************************************************************************************************
     *                                                     DDL
     ******************************************************************************************************************/

    /**
     * 创建 Database（LanceDB namespace）
     */
    public boolean create(DataRuntime runtime, Database database) throws Exception {
        LanceNamespace client = client(runtime);
        CreateNamespaceRequest req = new CreateNamespaceRequest();
        req.setId(Arrays.asList(database.getName()));
        client.createNamespace(req);
        return true;
    }

    /**
     * 删除 Database（LanceDB namespace）
     */
    public boolean drop(DataRuntime runtime, Database database) throws Exception {
        LanceNamespace client = client(runtime);
        DropNamespaceRequest req = new DropNamespaceRequest();
        req.setId(Arrays.asList(database.getName()));
        client.dropNamespace(req);
        return true;
    }

    /**
     * 创建 Table
     */
    public boolean create(DataRuntime runtime, Table table) throws Exception {
        LanceNamespace client = client(runtime);
        CreateTableRequest req = new CreateTableRequest();
        // LanceDB table path: [namespace, tableName]
        String schema = table.getSchemaName();
        if(BasicUtil.isNotEmpty(schema)) {
            req.setId(Arrays.asList(schema, table.getName()));
        } else {
            req.setId(Arrays.asList(table.getName()));
        }

        // Build initial Arrow IPC empty data for table creation
        byte[] emptyData = buildArrowIpcData(new ArrayList<>());
        if(emptyData == null) {
            // Create minimal schema data for table init
            List<Map<String, Object>> initRows = new ArrayList<>();
            initRows.add(new LinkedHashMap<>());
            emptyData = buildArrowIpcData(initRows);
        }

        client.createTable(req, emptyData);
        return true;
    }

    /**
     * 删除 Table
     */
    public boolean drop(DataRuntime runtime, Table table) throws Exception {
        LanceNamespace client = client(runtime);
        DropTableRequest req = new DropTableRequest();
        String schema = table.getSchemaName();
        if(BasicUtil.isNotEmpty(schema)) {
            req.setId(Arrays.asList(schema, table.getName()));
        } else {
            req.setId(Arrays.asList(table.getName()));
        }
        client.dropTable(req);
        return true;
    }

    /**
     * 创建索引 (LanceDB 当前版本主要通过建表时定义向量列来实现索引)
     */
    public boolean create(DataRuntime runtime, Index index) throws Exception {
        // LanceDB currently handles indexing automatically during table creation
        return true;
    }

    /**
     * 删除索引
     */
    public boolean drop(DataRuntime runtime, Index index) throws Exception {
        return true;
    }

    /**
     * 创建 User
     */
    public boolean create(DataRuntime runtime, User user) throws Exception {
        return true;
    }

    /**
     * 删除 User
     */
    public boolean drop(DataRuntime runtime, User user) throws Exception {
        return true;
    }

    /**
     * 创建 Role
     */
    public boolean create(DataRuntime runtime, Role role) throws Exception {
        return true;
    }

    /**
     * 删除 Role
     */
    public boolean drop(DataRuntime runtime, Role role) throws Exception {
        return true;
    }

    /**
     * 授权
     */
    public boolean grant(DataRuntime runtime, Privilege privilege) throws Exception {
        return true;
    }

    /**
     * 回收权限
     */
    public boolean revoke(DataRuntime runtime, Privilege privilege) throws Exception {
        return true;
    }
}