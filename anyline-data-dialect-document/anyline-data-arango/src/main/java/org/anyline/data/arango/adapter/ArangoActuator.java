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


package org.anyline.data.arango.adapter;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.arango.runtime.ArangoRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * ArangoDB 驱动执行器<br/>
 * 负责元数据查询(table/column/index/database 列表)以及通过 AQL 执行的数据操作<br/>
 * <br/>
 * 注意: insert/update/delete 的实际执行在 {@link ArangoAdapter} 中完成, Actuator 主要负责元数据与 AQL 查询
 */
@AnylineComponent("anyline.environment.data.driver.actuator.arango")
public class ArangoActuator implements DriverActuator {

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return ArangoAdapter.class;
    }

    private ArangoDatabase database(DataRuntime runtime) {
        ArangoRuntime rt = (ArangoRuntime) runtime;
        return rt.getDatabase();
    }

    public int priority() {
        return 0;
    }

    public DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime) {
        return null;
    }
    public Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource) {
        return null;
    }
    public void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource) {
    }
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, T meta) {
    }
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta) {
    }
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta) {
    }

    // ===== database metadata =====

    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        return "ArangoDB";
    }

    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        if(BasicUtil.isEmpty(version) && null != runtime) {
            try {
                ArangoDatabase db = database(runtime);
                version = db.getVersion().getVersion();
            } catch (Exception ignore) {}
        }
        return version;
    }

    @SuppressWarnings("unchecked")
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> list = new ArrayList<>();
        try {
            ArangoDatabase db = database(runtime);
            for (String name : db.arango().getDatabases()) {
                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T d = (T) new Database(name);
                list.add(d);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("databases 异常:", e);
            }
        }
        return list;
    }

    public List<Catalog> catalogs(DriverAdapter adapter, DataRuntime runtime) {
        List<Catalog> list = new ArrayList<>();
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<Schema> schemas(DriverAdapter adapter, DataRuntime runtime) {
        List<Schema> list = new ArrayList<>();
        try {
            ArangoDatabase db = database(runtime);
            for (String name : db.arango().getDatabases()) {
                list.add(new Schema(name));
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("schemas 异常:", e);
            }
        }
        return list;
    }

    // ===== AQL 查询执行 =====

    public DataSet<DataRow> query(DriverAdapter adapter, DataRuntime runtime, String random,
                                  boolean system, ACTION.DML action, Table table,
                                  ConfigStore configs, Run run, String cmd,
                                  List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        return adapter.query(runtime, random, system, table, configs, run);
    }

    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random,
                                    Procedure procedure, PageNavi navi) throws Exception {
        return new DataSet<>();
    }

    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random,
                                          ConfigStore configs, Run run) throws Exception {
        return adapter.maps(runtime, random, configs, run);
    }

    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random,
                                   ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(null != maps && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random,
                       Object data, ConfigStore configs, Run run,
                       String generatedKey, String[] pks) throws Exception {
        return adapter.insert(runtime, random, data, configs, run, pks);
    }

    public long update(DriverAdapter adapter, DataRuntime runtime, String random,
                       Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return adapter.update(runtime, random, dest, data, configs, run);
    }

    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random,
                                Procedure procedure, String sql, List<Parameter> inputs,
                                List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    public long execute(DriverAdapter adapter, DataRuntime runtime, String random,
                        ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    public long execute(DriverAdapter adapter, DataRuntime runtime, String random,
                        ConfigStore configs, List<Run> run) throws Exception {
        return 0;
    }

    // ===== 列元数据(从文档采样推断) =====

    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime,
                                                   String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime,
                                                                boolean create, LinkedHashMap<String, T> previous,
                                                                Column query) throws Exception {
        if(null == previous){
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            com.arangodb.ArangoCollection collection = db.collection(query.getTableName());
            if(null != collection) {
                String aqlTemplate = "FOR doc IN @@collection LIMIT @limit RETURN doc";
                Map<String, Object> bindVars = new HashMap<>();
                bindVars.put("limit", Long.valueOf(ConfigTable.CHECK_METADATA_SAMPLE));

                String inlined = inlineAql(aqlTemplate, query.getTableName(), bindVars);
                ArangoCursor<BaseDocument> cursor = db.query(inlined, BaseDocument.class);
                while (cursor.hasNext()) {
                    BaseDocument doc = cursor.next();
                    Set<String> fields = doc.getProperties().keySet();
                    for (String field : fields) {
                        String up = field.toUpperCase();
                        if (previous.containsKey(up)) {
                            continue;
                        }
                        Object value = doc.getProperties().get(field);
                        if (null != value) {
                            String type = value.getClass().getSimpleName();
                            Column column = new Column(field, type);
                            previous.put(up, (T) column);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("metadata 异常:", e);
            }
        }
        return previous;
    }

    // ===== tables / views =====

    @SuppressWarnings("unchecked")
    public <T extends Table> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime,
                                                             boolean create, LinkedHashMap<String, T> previous,
                                                             Table query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            Collection<CollectionEntity> entities = db.getCollections();

            for (CollectionEntity entity : entities) {
                if(entity.getIsSystem()) {
                    continue;
                }
                CollectionType ct = entity.getType();
                if(CollectionType.DOCUMENT != ct && CollectionType.EDGES != ct) {
                    continue;
                }

                // 过滤名称
                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(entity.getName().toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T table = (T) new Table(entity.getName());
                previous.put(entity.getName().toUpperCase(), table);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("tables 异常:", e);
            }
        }
        return previous;
    }

    public <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime,
                                            boolean create, List<T> previous,
                                            Table query, int types) throws Exception {
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        if(null != previous) {
            for(T t : previous) {
                map.put(t.getName().toUpperCase(), t);
            }
        }
        LinkedHashMap<String, T> result = tables(adapter, runtime, create, map, query, types);
        return new ArrayList<>(result.values());
    }

    @SuppressWarnings("unchecked")
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime,
                                                           boolean create, LinkedHashMap<String, T> previous,
                                                           View query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            Collection<com.arangodb.entity.ViewEntity> dbViews = db.getViews();
            for (com.arangodb.entity.ViewEntity entity : dbViews) {
                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(entity.getName().toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T view = (T) new View(entity.getName());
                previous.put(entity.getName().toUpperCase(), view);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("views 异常:", e);
            }
        }
        return previous;
    }

    public <T extends View> List<T> views(DriverAdapter adapter, DataRuntime runtime,
                                          boolean create, List<T> previous,
                                          View query, int types) throws Exception {
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        if(null != previous) {
            for(T t : previous) {
                map.put(t.getName().toUpperCase(), t);
            }
        }
        LinkedHashMap<String, T> result = views(adapter, runtime, create, map, query, types);
        return new ArrayList<>(result.values());
    }

    // ===== columns (from table list) =====

    public <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime,
                                                               boolean create, LinkedHashMap<String, T> previous,
                                                               Table table, String cmd) throws Exception {
        if(BasicUtil.isNotEmpty(table.getName())) {
            Column q = new Column();
            q.setTable(table.getName());
            return metadata(adapter, runtime, create, previous, q);
        }
        return previous != null ? previous : new LinkedHashMap<>();
    }

    // ===== AQL inline helper =====

    private String inlineAql(String aqlTemplate, String collectionName, Map<String, Object> bindVars) {
        String result = aqlTemplate;
        result = result.replace("@@collection", "`" + collectionName + "`");
        if(null != bindVars) {
            for(Map.Entry<String, Object> entry : bindVars.entrySet()) {
                String key = "@" + entry.getKey();
                String val = toAqlLiteral(entry.getValue());
                result = result.replace(key + " ", val + " ");
                result = result.replace(key + ")", val + ")");
                result = result.replace(key + "\n", val + "\n");
                result = result.replace(key, val);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String toAqlLiteral(Object value) {
        if(null == value) return "null";
        if(value instanceof String) {
            return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if(value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if(value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for(Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if(i++ > 0) sb.append(", ");
                sb.append("\"").append(entry.getKey()).append("\": ").append(toAqlLiteral(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if(value instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            int i = 0;
            for(Object item : (Collection<?>) value) {
                if(i++ > 0) sb.append(", ");
                sb.append(toAqlLiteral(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // ===== indexes =====

    @SuppressWarnings("unchecked")
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime,
                                                              boolean create, LinkedHashMap<String, T> previous,
                                                              Index query) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            com.arangodb.ArangoCollection collection = db.collection(query.getTableName());
            if(null != collection) {
                Collection<com.arangodb.entity.IndexEntity> idxes = collection.getIndexes();
                for (com.arangodb.entity.IndexEntity entity : idxes) {
                    String name = entity.getName();
                    if(BasicUtil.isEmpty(name)) {
                        name = entity.getId();
                    }
                    if(BasicUtil.isNotEmpty(query.getName())) {
                        String regex = query.getName().replace("%", ".*").replace("_", ".");
                        if(!RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            continue;
                        }
                    }
                    Index idx = new Index(name);
                    idx.setTable(query.getTableName());
                    idx.setType(entity.getType().name());
                    previous.put(name.toUpperCase(), (T) idx);
                }
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("indexes 异常:", e);
            }
        }
        return previous;
    }

}