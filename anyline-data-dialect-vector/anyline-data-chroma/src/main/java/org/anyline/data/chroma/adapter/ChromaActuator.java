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


package org.anyline.data.chroma.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.chroma.client.ChromaClient;
import org.anyline.data.chroma.run.ChromaRun;
import org.anyline.data.chroma.runtime.ChromaRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.chroma")
public class ChromaActuator implements DriverActuator {

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return ChromaAdapter.class;
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
        return "Chroma";
    }

    /**
     * 从DataRuntime中获取Chroma客户端
     * @param runtime 运行环境
     * @return Chroma客户端
     */
    protected ChromaClient client(DataRuntime runtime) {
        Object processor = runtime.getProcessor();
        if(processor instanceof ChromaClient) {
            return (ChromaClient) processor;
        }
        if(runtime instanceof ChromaRuntime) {
            Object client = ((ChromaRuntime) runtime).client();
            if(client instanceof ChromaClient) {
                return (ChromaClient) client;
            }
        }
        return null;
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        ChromaClient client = client(runtime);
        if(null != client) {
            return client.version();
        }
        return null;
    }

    @Override
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> result = new ArrayList<>();
        ChromaClient client = client(runtime);
        if(null != client) {
            T database = (T) new Database(client.database());
            result.add(database);
        }
        return result;
    }

    @Override
    public List<Catalog> catalogs(DriverAdapter adapter, DataRuntime runtime) {
        //Chroma 没有 catalog 概念
        return new ArrayList<>();
    }

    @Override
    public List<Schema> schemas(DriverAdapter adapter, DataRuntime runtime) {
        List<Schema> result = new ArrayList<>();
        ChromaClient client = client(runtime);
        if(null != client) {
            //Chroma 的 database 对应 anyline 的 schema
            result.add(new Schema(client.database()));
        }
        return result;
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
     * 查询集合中的记录<br/>
     * 参考图数据库(neo4j/nebula)的方式: 从通用Run接口取表名/条件/参数/分页, 不依赖具体的Run子类<br/>
     * ChromaRun 上额外设置的过滤表达式作为可选增强
     */
    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        ChromaClient client = client(runtime);
        if(null == client || null == run) {
            return result;
        }
        String table = run.getTableName();
        if(BasicUtil.isEmpty(table)) {
            return result;
        }
        Map<String, Object> where = where(run);
        int limit = 0;
        PageNavi navi = run.getPageNavi();
        if(null != navi) {
            limit = navi.getPageRows();
        }
        if(limit <= 0) {
            limit = 10;
        }

        List<Float> vector = vector(run);
        if(null != vector) {
            //参数中包含向量: 按向量检索 POST /collections/{name}/query
            return query(client.query(table, vector, limit, where));
        }
        //普通查询 POST /collections/{name}/get
        DataRow resp = client.get(table, ids(run), where, limit, null);
        return records(resp);
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
     * 集合中记录数 GET /collections/{name}/count
     */
    public long count(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        ChromaClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String table = run.getTableName();
        if(BasicUtil.isEmpty(table)) {
            return -1;
        }
        return client.count(table);
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        ChromaClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String table = run.getTableName();
        if(BasicUtil.isEmpty(table)) {
            return -1;
        }
        List<Map<String, Object>> rows = rows(data);
        if(rows.isEmpty()) {
            return -1;
        }
        List<String> ids = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        for(Map<String, Object> row:rows) {
            ids.add(id(row));
            embeddings.add(vector(row));
            documents.add(document(row));
            metadatas.add(metadata(row));
        }
        client.add(table, ids, embeddings, metadatas, documents);
        return ids.size();
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        ChromaClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String table = run.getTableName();
        if(BasicUtil.isEmpty(table)) {
            return -1;
        }
        List<Map<String, Object>> rows = rows(data);
        if(rows.isEmpty()) {
            return -1;
        }
        List<String> ids = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        for(Map<String, Object> row:rows) {
            ids.add(id(row));
            embeddings.add(vector(row));
            documents.add(document(row));
            metadatas.add(metadata(row));
        }
        //Chroma 的写入本身就是 upsert 语义(按id覆盖), 更新用 upsert 接口
        client.upsert(table, ids, embeddings, metadatas, documents);
        return ids.size();
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    /**
     * 删除记录 POST /collections/{name}/delete
     */
    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        ChromaClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String table = run.getTableName();
        if(BasicUtil.isEmpty(table)) {
            return -1;
        }
        List<String> ids = ids(run);
        Map<String, Object> where = where(run);
        if(ids.isEmpty() && where.isEmpty()) {
            return -1;
        }
        return client.delete(table, ids, where);
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, List<Run> run) throws Exception {
        long total = 0;
        for(Run r : run) {
            total += execute(adapter, runtime, random, configs, r);
        }
        return total;
    }

    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        //Chroma 集合没有固定的列定义, 只有 id/document/embedding/metadata
        return new LinkedHashMap<>();
    }

    /**
     * 查询集合(对应关系库的表) GET /collections
     */
    @Override
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        List<T> list = this.<T>tables(adapter, runtime, create, (List<T>) null, query, types);
        if(null != list) {
            for(T table:list) {
                previous.put(table.getName().toUpperCase(), table);
            }
        }
        return previous;
    }

    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        ChromaClient client = client(runtime);
        if(null == client) {
            return previous;
        }
        try {
            List<DataRow> collections = client.collections();
            for(DataRow collection:collections) {
                String name = field(collection, "name");
                if(BasicUtil.isEmpty(name)) {
                    continue;
                }
                if(null != query && null != query.getName() && !query.getName().equalsIgnoreCase(name)) {
                    continue;
                }
                T table = (T) new Table(name);
                previous.add(table);
            }
        }catch (Exception e) {
            log.error("[Chroma 查询集合异常]", e);
        }
        return previous;
    }

    @Override
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, View query, int types) throws Exception {
        //Chroma 没有视图概念
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
        return previous;
    }

    /* *********************************************************************************************
     * 											子流程
     ***********************************************************************************************/

    /**
     * 从Run的查询条件中构造 Chroma 的 where(元数据过滤)<br/>
     * 格式参考官方 where-filter: {"key":{"$eq":value}} 多个条件 {"$and":[...]}
     */
    protected Map<String, Object> where(Run run) {
        Map<String, Object> where = new LinkedHashMap<>();
        List<RunValue> values = run.getRunValues();
        if(null == values || values.isEmpty()) {
            return where;
        }
        List<Object> items = new ArrayList<>();
        for(RunValue value:values) {
            if(null == value) {
                continue;
            }
            String key = value.getKey();
            Object val = value.getValue();
            if(BasicUtil.isEmpty(key) || null == val) {
                continue;
            }
            if(val instanceof Collection) {
                //向量等数组不作为元数据过滤条件
                continue;
            }
            Map<String, Object> condition = new LinkedHashMap<>();
            condition.put("$eq", val);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put(key, condition);
            items.add(item);
        }
        if(1 == items.size()) {
            return (Map<String, Object>) items.get(0);
        }
        if(items.size() > 1) {
            where.put("$and", items);
        }
        return where;
    }

    /**
     * 从Run的查询条件中提取id
     */
    protected List<String> ids(Run run) {
        List<String> ids = new ArrayList<>();
        List<RunValue> values = run.getRunValues();
        if(null == values) {
            return ids;
        }
        for(RunValue value:values) {
            if(null == value) {
                continue;
            }
            String key = value.getKey();
            if(null != key && "id".equalsIgnoreCase(key)) {
                ids.add(String.valueOf(value.getValue()));
            }
        }
        //ChromaRun 上单独设置的过滤表达式优先用于条件拼接, 这里不重复处理
        return ids;
    }

    /**
     * 从Run的查询参数中提取向量(第一个元素是数字的List)
     */
    protected List<Float> vector(Run run) {
        List<RunValue> values = run.getRunValues();
        if(null != values) {
            for(RunValue value:values) {
                if(null == value) {
                    continue;
                }
                Object val = value.getValue();
                if(val instanceof List && !((List<?>) val).isEmpty()) {
                    Object first = ((List<?>) val).get(0);
                    if(first instanceof Number) {
                        return vector((List<?>) val);
                    }
                }
            }
        }
        if(run instanceof ChromaRun) {
            return ((ChromaRun) run).getQueryVector();
        }
        return null;
    }

    protected List<Float> vector(List<?> list) {
        List<Float> result = new ArrayList<>();
        for(Object item:list) {
            if(item instanceof Number) {
                result.add(((Number) item).floatValue());
            }
        }
        return result;
    }

    /**
     * 解析 get 响应: ids/documents/metadatas/embeddings 都是一维数组, 下标一一对应
     */
    protected List<Map<String, Object>> records(DataRow resp) {
        List<Map<String, Object>> result = new ArrayList<>();
        if(null == resp) {
            return result;
        }
        List<Object> ids = list(resp, "ids");
        List<Object> documents = list(resp, "documents");
        List<Object> metadatas = list(resp, "metadatas");
        List<Object> embeddings = list(resp, "embeddings");
        for(int i = 0; i < ids.size(); i++) {
            result.add(row(ids, documents, metadatas, embeddings, null, i));
        }
        return result;
    }

    /**
     * 解析 query 响应: 每个查询向量一组结果(二维数组), 这里只处理第1组
     */
    protected List<Map<String, Object>> query(DataRow resp) {
        List<Map<String, Object>> result = new ArrayList<>();
        if(null == resp) {
            return result;
        }
        List<Object> ids = list(resp, "ids");
        List<Object> documents = list(resp, "documents");
        List<Object> metadatas = list(resp, "metadatas");
        List<Object> embeddings = list(resp, "embeddings");
        List<Object> distances = list(resp, "distances");
        if(!ids.isEmpty() && ids.get(0) instanceof List) {
            ids = (List<Object>) ids.get(0);
        }
        if(!documents.isEmpty() && documents.get(0) instanceof List) {
            documents = (List<Object>) documents.get(0);
        }
        if(!metadatas.isEmpty() && metadatas.get(0) instanceof List) {
            metadatas = (List<Object>) metadatas.get(0);
        }
        if(!embeddings.isEmpty() && embeddings.get(0) instanceof List) {
            embeddings = (List<Object>) embeddings.get(0);
        }
        if(!distances.isEmpty() && distances.get(0) instanceof List) {
            distances = (List<Object>) distances.get(0);
        }
        for(int i = 0; i < ids.size(); i++) {
            result.add(row(ids, documents, metadatas, embeddings, distances, i));
        }
        return result;
    }

    private Map<String, Object> row(List<Object> ids, List<Object> documents, List<Object> metadatas, List<Object> embeddings, List<Object> distances, int i) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", value(ids, i));
        Object metadata = value(metadatas, i);
        if(metadata instanceof Map) {
            row.putAll((Map<String, Object>) metadata);
        }else if(null != metadata) {
            row.put("metadata", metadata);
        }
        Object document = value(documents, i);
        if(null != document) {
            row.put("document", document);
        }
        Object embedding = value(embeddings, i);
        if(null != embedding) {
            row.put("embedding", embedding);
        }
        Object distance = value(distances, i);
        if(null != distance) {
            row.put("score", distance);
        }
        return row;
    }

    private Object value(List<Object> list, int i) {
        if(null == list || i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    /**
     * 写入数据整理: 支持 DataSet/DataRow/Map/Entity/Collection
     */
    protected List<Map<String, Object>> rows(Object data) {
        List<Map<String, Object>> result = new ArrayList<>();
        if(null == data) {
            return result;
        }
        if(data instanceof DataSet) {
            for(DataRow item:(DataSet<DataRow>) data) {
                result.add(item.toMap());
            }
        }else if(data instanceof Collection) {
            for(Object item:(Collection<?>) data) {
                result.addAll(rows(item));
            }
        }else if(data instanceof DataRow) {
            result.add(((DataRow) data).toMap());
        }else if(data instanceof Map) {
            result.add((Map<String, Object>) data);
        }else{
            Map<String, Object> map = BeanUtil.object2map(data);
            if(null != map) {
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 记录id 取 id/ID 字段, 没有则生成
     */
    protected String id(Map<String, Object> row) {
        for(String key:row.keySet()) {
            if("id".equalsIgnoreCase(key) && null != row.get(key)) {
                return String.valueOf(row.get(key));
            }
        }
        return UUID.randomUUID().toString();
    }

    /**
     * 向量字段 取 vector/embedding
     */
    protected List<Float> vector(Map<String, Object> row) {
        for(String key:row.keySet()) {
            if("vector".equalsIgnoreCase(key) || "embedding".equalsIgnoreCase(key) || "embeddings".equalsIgnoreCase(key)) {
                Object value = row.get(key);
                if(value instanceof List) {
                    return vector((List<?>) value);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * 文档字段 取 document/content/text
     */
    protected String document(Map<String, Object> row) {
        for(String key:row.keySet()) {
            if("document".equalsIgnoreCase(key) || "content".equalsIgnoreCase(key) || "text".equalsIgnoreCase(key)) {
                Object value = row.get(key);
                if(null != value) {
                    return String.valueOf(value);
                }
            }
        }
        return null;
    }

    /**
     * 除 id/向量/文档 之外的字段作为元数据
     */
    protected Map<String, Object> metadata(Map<String, Object> row) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for(Map.Entry<String, Object> entry:row.entrySet()) {
            String key = entry.getKey();
            if("id".equalsIgnoreCase(key)
                    || "vector".equalsIgnoreCase(key)
                    || "embedding".equalsIgnoreCase(key)
                    || "embeddings".equalsIgnoreCase(key)
                    || "document".equalsIgnoreCase(key)
                    || "content".equalsIgnoreCase(key)
                    || "text".equalsIgnoreCase(key)) {
                continue;
            }
            Object value = entry.getValue();
            //Chroma 元数据只支持 string/int/float/bool
            if(null == value
                    || value instanceof String
                    || value instanceof Number
                    || value instanceof Boolean) {
                metadata.put(key, value);
            }else{
                metadata.put(key, String.valueOf(value));
            }
        }
        return metadata;
    }

    /**
     * 取结果集中的数组(兼容key大小写)
     */
    protected List<Object> list(DataRow row, String key) {
        Object value = field(row, key);
        if(value instanceof List) {
            return (List<Object>) value;
        }
        return new ArrayList<>();
    }

    protected String field(DataRow row, String key) {
        Object value = row.get(key);
        if(null == value) {
            value = row.get(key.toUpperCase());
        }
        if(null == value) {
            value = row.get(key.toLowerCase());
        }
        if(null == value) {
            return null;
        }
        return String.valueOf(value);
    }
}