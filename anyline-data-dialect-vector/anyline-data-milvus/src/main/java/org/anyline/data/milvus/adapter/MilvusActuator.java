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


package org.anyline.data.milvus.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import io.milvus.v2.service.index.request.*;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.collection.request.GetCollectionStatsReq;
import io.milvus.v2.service.collection.response.GetCollectionStatsResp;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.milvus.metadata.MilvusCollection;
import org.anyline.data.milvus.run.MilvusRun;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.milvus")
public class MilvusActuator implements DriverActuator {
    private static final Log log = LogProxy.get(MilvusActuator.class);
    
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return MilvusAdapter.class;
    }

    /**
     * 返回值越高 优先级越高
     * 支持相同DriverAdapter的worker只有一个生效，以优先级最高的为准
     * @return int
     */
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

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 product
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param product 上一步查询结果
     * @return product
     */
    @Override
    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        return null;
    }

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 version
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param version 上一步查询结果
     * @return version
     */
    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    /**
     * 数据库列表
     * @param adapter adapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return List
     */
    @Override
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        //https://milvus.io/docs/zh/create-collection.md
        List<T> list = new ArrayList<>();
        MilvusClientV2 client = client(runtime);
        //注意 旧版本驱动中没有这个方法
        ListDatabasesResp databases = client.listDatabases();
        List<String> names = databases.getDatabaseNames();
        for(String name:names){
            Database database = new Database(name);
            list.add((T)database);
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
    
    /**
     * query procedure [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param procedure 存储过程
     * @param navi 分页
     * @return DataSet
     */
    @Override
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception {
        return new DataSet();
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return maps
     */
    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        MilvusClientV2 client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();
            List<String> selectColumns = tableRun.getSelectColumns();
            List<RunValue> runValues = tableRun.getRunValues();
            PageNavi navi = tableRun.getPageNavi();
            OrderStore orders = tableRun.getOrders();

            // 1. 检测是否向量搜索：值中包含向量List
            Object vectorParam = null;
            String vectorField = null;
            int topK = 10;
            if(null != runValues) {
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
            // 从PageNavi获取topK
            if(null != navi) {
                topK = navi.getPageRows();
                if(topK <= 0) {
                    topK = 10;
                }
            }

            // 2. 构建filter表达式
            String filterExpr = null;
            if(run instanceof MilvusRun) {
                MilvusRun milvusRun = (MilvusRun) run;
                filterExpr = milvusRun.getFilter();
            }
            if(null == filterExpr) {
                filterExpr = buildFilter(runValues, vectorField);
            }

            // 3. 根据查询类型分发
            if(vectorParam != null && vectorField != null) {
                // 向量搜索 (SearchReq)
                result = searchMaps(client, tableName, vectorField, (List<List<Float>>) vectorParam, 
                    topK, selectColumns, filterExpr, navi, orders);
            } else {
                // 标量查询 (QueryReq)
                result = queryMaps(client, tableName, selectColumns, filterExpr, navi, orders);
            }
        }
        return result;
    }

    /**
     * 执行向量搜索
     */
    private List<Map<String, Object>> searchMaps(MilvusClientV2 client, String tableName, String vectorField, 
            List<List<Float>> vectors, int topK, List<String> selectColumns, String filterExpr, 
            PageNavi navi, OrderStore orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 将 List<List<Float>> 转换为 List<BaseVector>
        List<io.milvus.v2.service.vector.request.data.BaseVector> data = new ArrayList<>();
        if(vectors != null) {
            for(List<Float> vec : vectors) {
                data.add(new io.milvus.v2.service.vector.request.data.FloatVec(vec));
            }
        }
        
        SearchReq.SearchReqBuilder builder = SearchReq.builder()
                .collectionName(tableName)
                .annsField(vectorField)
                .data(data)
                .topK(topK);
        
        if(selectColumns != null && !selectColumns.isEmpty()) {
            builder.outputFields(selectColumns);
        }
        if(filterExpr != null && !filterExpr.isEmpty()) {
            builder.filter(filterExpr);
        }
        // 分页：向量搜索通过offset实现
        if(navi != null && navi.getFirstRow() > 0) {
            builder.offset(navi.getFirstRow());
        }
        
        SearchResp resp = client.search(builder.build());
        if(resp != null && resp.getSearchResults() != null) {
            for(List<SearchResp.SearchResult> resultList : resp.getSearchResults()) {
                for(SearchResp.SearchResult sr : resultList) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", sr.getId());
                    row.put("score", sr.getScore());
                    if(sr.getEntity() != null) {
                        row.putAll(sr.getEntity());
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * 执行标量查询（不带向量的filter查询）
     */
    private List<Map<String, Object>> queryMaps(MilvusClientV2 client, String tableName, 
            List<String> selectColumns, String filterExpr, PageNavi navi, OrderStore orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryReq.QueryReqBuilder builder = QueryReq.builder()
                .collectionName(tableName);
        
        if(selectColumns != null && !selectColumns.isEmpty()) {
            builder.outputFields(selectColumns);
        }
        if(filterExpr != null && !filterExpr.isEmpty()) {
            builder.filter(filterExpr);
        }
        
        // 分页处理
        if(navi != null) {
            if(navi.getFirstRow() > 0) {
                builder.offset((int) navi.getFirstRow());
            }
            int limit = navi.getPageRows();
            if(limit > 0) {
                builder.limit(limit);
            }
        }
        
        try {
            QueryResp resp = client.query(builder.build());
            if(resp != null && resp.getQueryResults() != null) {
                for(QueryResp.QueryResult qr : resp.getQueryResults()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    if(qr.getEntity() != null) {
                        row.putAll(qr.getEntity());
                    }
                    result.add(row);
                }
            }
        } catch(Exception e) {
            // Milvus query API 可能抛异常，记录日志
            log.error("Milvus query 异常: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 构建Milvus filter表达式
     * @param runValues 运行参数值
     * @param excludeField 排除的字段（如向量搜索字段）
     * @return filter表达式
     */
    private String buildFilter(List<RunValue> runValues, String excludeField) {
        if(null == runValues || runValues.isEmpty()) {
            return null;
        }
        StringBuilder filterBuilder = new StringBuilder();
        for(RunValue rv : runValues) {
            String key = rv.getKey();
            Object value = rv.getValue();
            // 跳过向量参数
            if(value instanceof List) {
                continue;
            }
            // 跳过排除字段
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
                filterBuilder.append(key).append(" == \"").append(value).append("\"");
            } else if(value instanceof Number) {
                filterBuilder.append(key).append(" == ").append(value);
            } else {
                filterBuilder.append(key).append(" == ").append(value);
            }
        }
        return filterBuilder.length() > 0 ? filterBuilder.toString() : null;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return map
     */
    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }
    
    public long count(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        MilvusClientV2 client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();
            
            GetCollectionStatsReq req = GetCollectionStatsReq.builder()
                    .collectionName(tableName)
                    .build();
            GetCollectionStatsResp resp = client.getCollectionStats(req);
            
            Long count = resp.getNumOfEntities();
            return count != null ? count : -1;
        }
        return -1;
    }

    /**
     * 执行insert
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param data 待插入数据
     * @param configs ConfigStore
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param generatedKey 执行insert后返回自增主键的key
     * @param pks
     * @return long
     * @throws Exception Exception
     */
    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        MilvusClientV2 client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();
            List<JsonObject> rows = new ArrayList<>();
            Gson gson = new Gson();
            
            if(data instanceof List) {
                for(Object item : (List<?>) data) {
                    if(item instanceof Map) {
                        rows.add(gson.toJsonTree((Map<String, Object>) item).getAsJsonObject());
                    } else if(item instanceof DataRow) {
                        rows.add(gson.toJsonTree(((DataRow) item).toMap()).getAsJsonObject());
                    }
                }
            } else if(data instanceof Map) {
                rows.add(gson.toJsonTree((Map<String, Object>) data).getAsJsonObject());
            } else if(data instanceof DataRow) {
                rows.add(gson.toJsonTree(((DataRow) data).toMap()).getAsJsonObject());
            }
            
            if(!rows.isEmpty()) {
                InsertReq req = InsertReq.builder()
                        .collectionName(tableName)
                        .data(rows)
                        .build();
                InsertResp resp = client.insert(req);
                return resp.getInsertCnt();
            }
        }
        return -1;
    }

    /**
     * update [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long result = 0;
        if(null != run){
            ACTION aciton = run.action();
            if(aciton == ACTION.DDL.DATABASE_CREATE){
                create(runtime, (Database) run.metadata());
                result = 1;
            }else if (aciton == ACTION.DDL.DATABASE_DROP){
                drop(runtime, (Database) run.metadata());
                result = 1;
            }else if(aciton == ACTION.DDL.TABLE_CREATE){
                create(runtime, (Table)run.metadata());
            }else if(aciton == ACTION.DDL.TABLE_DROP){
                drop(runtime, (Table)run.metadata());
            }
        }
        return result;
    }

    /**
     * procedure [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @param random  random
     * @return 输出参数
     */
    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        MilvusClientV2 client = client(runtime);
        if(run instanceof TableRun) {
            TableRun tableRun = (TableRun) run;
            String tableName = tableRun.getTableName();
            List<Object> values = tableRun.getValues();
            
            if(values != null && !values.isEmpty()) {
                DeleteReq req = DeleteReq.builder()
                        .collectionName(tableName)
                        .ids(values)
                        .build();
                DeleteResp resp = client.delete(req);
                return resp.getDeleteCnt();
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
     * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param comment 是否需要查询列注释
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        if(null == previous){
            previous = new LinkedHashMap<>();
        }
        List<String> names = new ArrayList<>();
        String name = query.getName();
        MilvusClientV2 client = client(runtime);
        if(BasicUtil.isNotEmpty(name)){
            names.add(name);
        }else {
            boolean empty = previous.isEmpty();
            ListCollectionsResp list = client.listCollections();
            names = list.getCollectionNames();
            for (String item : names) {
                if (empty || !previous.containsKey(item.toUpperCase())) {
                    MilvusCollection table = new MilvusCollection();
                    table.setName(item);
                    previous.put(item.toUpperCase(), (T) table);
                }
            }
        }
        for (T item : previous.values()) {
            DescribeCollectionReq request = DescribeCollectionReq.builder()
                    .collectionName(item.getName())
                    .build();
            DescribeCollectionResp resp = client.describeCollection(request);
            List<CreateCollectionReq.FieldSchema> fields = resp.getCollectionSchema().getFieldSchemaList();
            for(CreateCollectionReq.FieldSchema field : fields){
                Column column = column(field);
                item.addColumn(column);
            }
        }
        return previous;
    }
    private Column column(CreateCollectionReq.FieldSchema field){
        Column column = new Column();
        column.setName(field.getName());
        column.setPrimary(field.getIsPrimaryKey());
        column.setNullable(field.getIsNullable());
        DataType dt = field.getDataType();
        column.setType(dt.name());
        Integer length = field.getMaxLength();
        if(null != length){
            column.setLength(length);
        }
        Integer dimension = field.getDimension();
        if(null != dimension){
            column.setDimension(dimension);
        }
        column.setComment(field.getDescription());
        return column;
    }
    
    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if(null == previous){
            previous = new ArrayList<>();
        }
        MilvusClientV2 client = client(runtime);
        boolean empty = previous.isEmpty();
        ListCollectionsResp list = client.listCollections();
        List<String> names = list.getCollectionNames();
        for(String name:names){
            boolean append = true;
            if(empty){
                append = true;
            }else{
                for(Table item:previous){
                    if(name.equalsIgnoreCase(item.getName())){
                        append = false;
                        break;
                    }
                }
            }
            if(append){
                MilvusCollection table = new MilvusCollection();
                table.setName(name);
                previous.add((T)table);
            }
        }
        return previous;
    }

    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, View query, int types) throws Exception {
        return previous;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends View> List<T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, View query, int types) throws Exception {
        return previous;
    }

    /**
     * 根据结果集解析列结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param table 表
     * @param cmd sql
     * @return columns
     * @param <T> Column
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table table, String cmd) throws Exception {
        return previous;
    }

    /**
     * 根方法(3)根据驱动内置元数据接口补充表结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @return columns
     * @param <T> Column
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Column query) throws Exception {
        return new LinkedHashMap<>();
    }

    /**
     * index[结果集封装]<br/>
     * 根据驱动内置元数据接口查询索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        MilvusClientV2 client = client(runtime);
        String tableName = query.getTable().getName();
        
        List<String> indexNames = client.listIndexes(ListIndexesReq.builder()
                .collectionName(tableName)
                .build());
        
        for(String indexName : indexNames) {
            DescribeIndexReq describeReq = DescribeIndexReq.builder()
                    .collectionName(tableName)
                    .indexName(indexName)
                    .build();
            DescribeIndexResp describeResp = client.describeIndex(describeReq);
            DescribeIndexResp.IndexDesc desc = describeResp.getIndexDescByIndexName(indexName);
            
            if(desc != null) {
                Index index = new Index();
                index.setName(indexName);
                index.setTable(query.getTable());
                index.setType(desc.getIndexType().name());
                index.addColumn(desc.getFieldName());
                
                previous.put(indexName.toUpperCase(), (T) index);
            }
        }
        return previous;
    }
    
    public boolean createIndex(DataRuntime runtime, String collectionName, String fieldName, String indexType, Map<String, Object> params) throws Exception {
        MilvusClientV2 client = client(runtime);
        IndexParam.IndexParamBuilder indexParamBuilder = IndexParam.builder()
                .fieldName(fieldName)
                .indexType(IndexParam.IndexType.valueOf(indexType));
        
        if(params != null && !params.isEmpty()) {
            indexParamBuilder.extraParams(params);
        }
        
        CreateIndexReq req = CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(Collections.singletonList(indexParamBuilder.build()))
                .build();
        
        client.createIndex(req);
        return true;
    }
    
    public boolean dropIndex(DataRuntime runtime, String collectionName, String indexName) throws Exception {
        MilvusClientV2 client = client(runtime);
        DropIndexReq req = DropIndexReq.builder()
                .collectionName(collectionName)
                .indexName(indexName)
                .build();
        client.dropIndex(req);
        return true;
    }

    private MilvusClientV2 client(DataRuntime runtime) {
        return (MilvusClientV2) runtime.getProcessor();
    }


    /* *****************************************************************************************************************
     * 													role
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, Role role) throws Exception
     * boolean rename(DataRuntime runtime, Role origin, Role update) throws Exception;
     * boolean delete(DataRuntime runtime, Role role) throws Exception
     * <T extends Role> List<T> roles(Catalog catalog, Schema schema, String pattern) throws Exception
     ******************************************************************************************************************/
    /**
     * role[调用入口]<br/>
     * 创建角色
     * @param role 角色
     * @return boolean
     */
    public boolean create(DataRuntime runtime, Role role) throws Exception {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/createRole.md
        CreateRoleReq req = CreateRoleReq.builder()
            .roleName(role.getName())
            .build();
        client(runtime).createRole(req);
        return true;
    }
    
    /**
     * role[调用入口]<br/>
     * 删除角色
     * @param role 角色
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, Role role) throws Exception {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/dropRole.md
        DropRoleReq req = DropRoleReq.builder()
            .roleName(role.getName())
            .build();
        client(runtime).dropRole(req);
        return true;
    }
    public <T extends Role> List<T>  roles(DataRuntime runtime, String random, boolean greedy, Role query) {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/listRoles.md
        List<T> list = new ArrayList<>();
        String user = query.getUserName();
        if(null != user) {
            //用户相关角色
            DescribeUserResp response = client(runtime).describeUser(DescribeUserReq.builder()
                    .userName(user)
                    .build()
            );
            if(null != response) {
                List<String> roles = response.getRoles();
                for(String role:roles) {
                    list.add((T)new Role(role));
                }
            }
        }else{
            //全部角色
            List<String> roles = client(runtime).listRoles();
            for(String role:roles) {
                list.add((T)new Role(role));
            }
        }

        return list;
    }

    /* *****************************************************************************************************************
     * 													user
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, User user) throws Exception
     * boolean rename(DataRuntime runtime, User origin, User update) throws Exception;
     * boolean drop(DataRuntime runtime, User user) throws Exception
     * List<User> users(Catalog catalog, Schema schema, String pattern) throws Exception
     ******************************************************************************************************************/

    /**
     * user[调用入口]<br/>
     * 创建 用户
     * @param user 用户
     * @return boolean
     */
    public boolean create(DataRuntime runtime, User user) throws Exception {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/createUser.md
        CreateUserReq req = CreateUserReq.builder()
            .userName(user.getName())
            .password(user.getPassword())
            .build();
        client(runtime).createUser(req);
        return true;
    }
    
    /**
     * user[调用入口]<br/>
     * 删除 用户
     * @param user 用户
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, User user) throws Exception {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/dropUser.md
        DropUserReq req = DropUserReq.builder()
            .userName(user.getName())
            .build();
        client(runtime).dropUser(req);
        return true;
    }
    public <T extends User> List<T>  users(DataRuntime runtime, String random, boolean greedy, User query) {
        //https://milvus.io/api-reference/java/v2.4.x/v2/Authentication/listUsers.md
        List<T> list = new ArrayList<>();
        List<String> users = client(runtime).listUsers();
        for(String user:users) {
            list.add((T)new User(user));
        }
        return list;
    }
    /* *****************************************************************************************************************
     * 													privilege
     * -----------------------------------------------------------------------------------------------------------------
     * <T extends Privilege> List<T> privileges(DataRuntime runtime, User user)
     ******************************************************************************************************************/

    /**
     * privilege[调用入口]<br/>
     * 查询用户权限
     * @param query 查询条件 根据metadata属性
     * @return List
     */

    public <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) throws Exception {
        List<T> list = new ArrayList<>();
        String role = query.getRoleName();
        String user = query.getUserName();
        if(null != user) {
            Map<String, T> map = new HashMap<>();
            List<Role> roles = roles(runtime, random, false, new Role().setUser(user));
            for(Role r:roles) {
                List<T> ps = privileges(runtime, random, greedy, new Privilege().setRole(r));
                for(T p:ps) {
                    map.put(p.getName(), p);
                }
            }
            list.addAll(map.values());
        }else if(null != role) {
            //角色相关权限
            DescribeRoleReq describe = DescribeRoleReq.builder()
                    .roleName(role)
                    .build();
            DescribeRoleResp response = client(runtime).describeRole(describe);
            if(null != response) {
                List<DescribeRoleResp.GrantInfo> grants = response.getGrantInfos();
                for(DescribeRoleResp.GrantInfo grant:grants) {
                    Privilege privilege = new Privilege();
                    privilege.setRole(new Role(role));
                    privilege.setObjectName(grant.getObjectName());
                    privilege.setObjectType(grant.getObjectType());
                    privilege.setName(grant.getPrivilege());
                    privilege.setDatabase(grant.getDbName());
                    list.add((T)privilege);
                }
            }
        }
        return list;
    }

    /* *****************************************************************************************************************
     * 													grant
     * -----------------------------------------------------------------------------------------------------------------
     * boolean grant(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * boolean grant(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean grant(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param user 用户
     * @param privileges 权限
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
        //没有实现 可以通过 给角色授权 给用户赋角色
        return true;
    }

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, User user, Role ... roles) throws Exception {
        for(Role role:roles) {
            client(runtime).grantRole(GrantRoleReq.builder()
                    .roleName(role.getName())
                    .userName(user.getName())
                    .build()
            );
        }
        return true;
    }

    /**
     * privilege[调用入口]<br/>
     * 授权
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */

    public boolean grant(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        for(Privilege privilege:privileges) {
            GrantPrivilegeReq.GrantPrivilegeReqBuilder build = GrantPrivilegeReq.builder();
            build.roleName(role.getName());
            if(BasicUtil.isNotEmpty(privilege.getObjectName())) {
                build.objectName(privilege.getObjectName());
            }
            if(BasicUtil.isNotEmpty(privilege.getObjectType())) {
                build.objectType(privilege.getObjectType());
            }
            build.privilege(privilege.getName());
            GrantPrivilegeReq req = build.build();
            client(runtime).grantPrivilege(req);
        }
        return true;
    }

    /* *****************************************************************************************************************
     * 													revoke
     * -----------------------------------------------------------------------------------------------------------------
     * boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/

    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param user 用户
     * @param privileges 权限
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
        //没有实现 可以通过 给用户删除角色 或给角色删除权限
        return true;
    }

    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception {
        for(Role role:roles) {
            client(runtime).revokeRole(RevokeRoleReq.builder()
                    .roleName(role.getName())
                    .userName(user.getName())
                    .build()
            );
        }
        return true;
    }
    
    /**
     * revoke[调用入口]<br/>
     * 撤销授权
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */

    public boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        for (Privilege privilege:privileges) {
            client(runtime).revokePrivilege(RevokePrivilegeReq.builder()
                    .dbName(privilege.getDatabaseName())
                    .roleName(role.getName())
                    .objectType(privilege.getObjectType())
                    .privilege(privilege.getName())
                    .objectName(privilege.getObjectName())
                    .build()
            );
        }
        return true;
    }



    /* *****************************************************************************************************************
     * 													database
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, Database meta) throws Exception
     * boolean drop(DataRuntime runtime, Database meta) throws Exception
     ******************************************************************************************************************/
    /**
     * database[调用入口]<br/>
     * 创建数据库
     * @param meta 数据库
     * @return boolean
     */
    public boolean create(DataRuntime runtime, Database meta) throws Exception {
        LinkedHashMap<String, Object> map = meta.getProperty();
        CreateDatabaseReq.CreateDatabaseReqBuilder<?, ?> builder = CreateDatabaseReq.builder();
        builder.databaseName(meta.getName());
        if(null != map && !map.isEmpty()){
            Map<String, String> pros = new HashMap<>();
            for(String key:map.keySet()){
                Object value = map.get(key);
                if(null != value) {
                    pros.put(key, value.toString());
                }
            }
            builder.properties(pros);
        }
        client(runtime).createDatabase(builder.build());
        return true;
    }
    
    /**
     * database[调用入口]<br/>
     * 删除数据库
     * @param meta 数据库
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, Database meta) throws Exception {
        client(runtime).dropDatabase(DropDatabaseReq.builder()
                .databaseName(meta.getName())
                .build());
        return true;
    }

    /* *****************************************************************************************************************
     * 													table
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, Table meta) throws Exception
     * boolean drop(DataRuntime runtime, Table meta) throws Exception
     ******************************************************************************************************************/
    /**
     * database[调用入口]<br/>
     * 创建Table
     * @param meta Table
     * @return boolean
     */
    public boolean create(DataRuntime runtime, Table meta) throws Exception {
        CreateCollectionReq req = CreateCollectionReq.builder()
                .collectionName(meta.getName())
                .collectionSchema(schema(runtime, meta))
                .build();
        client(runtime).createCollection(req);
        return true;
    }
    private CreateCollectionReq.CollectionSchema schema(DataRuntime runtime, Table table){
        LinkedHashMap<String, Column> columns = table.getColumns();
        CreateCollectionReq.CollectionSchema schema = client(runtime).createSchema();
        for(Column column:columns.values()){
            TypeMetadata type = column.getTypeMetadata();
            boolean pk = column.isPrimaryKey();
            if(pk && type.getCategory() == TypeMetadata.CATEGORY.INT){
                //主键要求int64 或 varchar
                type = StandardTypeMetadata.INT64;
            }
            TypeMetadata.Refer refer = MetadataReferHolder.get(DatabaseType.Milvus, type);
            AddFieldReq.AddFieldReqBuilder<?, ?> builder = AddFieldReq.builder();
            DataType dt = DataType.valueOf(refer.getMeta());
            builder.fieldName(column.getName())
                    .dataType(dt)
                    .isPrimaryKey(pk)
                    .autoID(column.isAutoIncrement());
            if(refer.ignorePrecision() == 0){
                //不忽略precision
                builder.maxLength(column.getLength());
            }
            if(type.getCategory() == TypeMetadata.CATEGORY.VECTOR){
                //向量类型需要设置维度
                builder.dimension(column.getPrecision());
            }

            schema.addField(builder.build());
        }
        return schema;
    }
    
    /**
     * database[调用入口]<br/>
     * 删除Table
     * @param meta Table
     * @return boolean
     */
    public boolean drop(DataRuntime runtime, Table meta) throws Exception {
        DropCollectionReq req = DropCollectionReq.builder()
                .collectionName(meta.getName())
                .build();
        client(runtime).dropCollection(req);
        return true;
    }

}