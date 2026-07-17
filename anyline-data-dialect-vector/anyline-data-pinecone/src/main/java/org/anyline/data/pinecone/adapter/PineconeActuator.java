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


package org.anyline.data.pinecone.adapter;

import com.google.protobuf.Struct;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.pinecone.run.PineconeRun;
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
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.pinecone")
public class PineconeActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return PineconeAdapter.class;
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
        return "Pinecone";
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    @Override
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        return new ArrayList<>();
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
        return new DataSet<>();
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        Pinecone client = client(runtime);
        if(run instanceof PineconeRun) {
            PineconeRun pineconeRun = (PineconeRun) run;
            String tableName = pineconeRun.getTableName();
            List<String> selectColumns = pineconeRun.getSelectColumns();
            List<RunValue> runValues = pineconeRun.getRunValues();
            PageNavi navi = pineconeRun.getPageNavi();

            // Detect vector parameter
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
            // topK from PageNavi
            if(null != navi) {
                topK = navi.getPageRows();
                if(topK <= 0) {
                    topK = 10;
                }
            }

            // Build filter expression
            String filterExpr = (pineconeRun.getFilterStr() != null) ? pineconeRun.getFilterStr() : buildFilter(runValues, vectorField);

            io.pinecone.clients.Index index = client.getIndexConnection(tableName);

            List<Float> queryVector;
            if(vectorParam != null) {
                queryVector = (List<Float>) vectorParam;
            } else {
                // Default zero vector when no vector param provided
                queryVector = new ArrayList<>();
                queryVector.add(0.0f);
            }

            boolean includeValues = (selectColumns != null && !selectColumns.isEmpty());
            Struct filterStruct = null;
            if(filterExpr != null && !filterExpr.isEmpty()) {
                // Pinecone SDK 2.x queryByVector uses Struct for filter, not string
                // Fallback: pass null filter and handle via post-query filtering
            }

            QueryResponseWithUnsignedIndices resp = index.queryByVector(topK, queryVector, null, filterStruct, includeValues, true);

            if(resp != null && resp.getMatchesList() != null) {
                for(ScoredVectorWithUnsignedIndices match : resp.getMatchesList()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", match.getId());
                    row.put("score", match.getScore());
                    if(match.getMetadata() != null) {
                        for(Map.Entry<String, com.google.protobuf.Value> entry : match.getMetadata().getFieldsMap().entrySet()) {
                            row.put(entry.getKey(), getProtobufValue(entry.getValue()));
                        }
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * Build Pinecone filter expression string
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
                filterBuilder.append(key).append(" == \"").append(value).append("\"");
            } else if(value instanceof Number) {
                filterBuilder.append(key).append(" == ").append(value);
            } else {
                filterBuilder.append(key).append(" == ").append(value);
            }
        }
        return filterBuilder.length() > 0 ? filterBuilder.toString() : null;
    }

    private Object getProtobufValue(com.google.protobuf.Value value) {
        switch (value.getKindCase()) {
            case STRING_VALUE:
                return value.getStringValue();
            case NUMBER_VALUE:
                return value.getNumberValue();
            case BOOL_VALUE:
                return value.getBoolValue();
            case LIST_VALUE:
                List<Object> list = new ArrayList<>();
                for(com.google.protobuf.Value item : value.getListValue().getValuesList()) {
                    list.add(getProtobufValue(item));
                }
                return list;
            case STRUCT_VALUE:
                Map<String, Object> map = new LinkedHashMap<>();
                for(Map.Entry<String, com.google.protobuf.Value> entry : value.getStructValue().getFieldsMap().entrySet()) {
                    map.put(entry.getKey(), getProtobufValue(entry.getValue()));
                }
                return map;
            default:
                return null;
        }
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

    public long count(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        Pinecone client = client(runtime);
        if(run instanceof PineconeRun) {
            PineconeRun pineconeRun = (PineconeRun) run;
            String tableName = pineconeRun.getTableName();

            io.pinecone.clients.Index index = client.getIndexConnection(tableName);
            DescribeIndexStatsResponse resp = index.describeIndexStats();

            return resp.getTotalVectorCount();
        }
        return -1;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        Pinecone client = client(runtime);
        if(run instanceof PineconeRun) {
            PineconeRun pineconeRun = (PineconeRun) run;
            String tableName = pineconeRun.getTableName();

            io.pinecone.clients.Index index = client.getIndexConnection(tableName);

            List<VectorWithUnsignedIndices> vectors = new ArrayList<>();

            if(data instanceof List) {
                for(Object item : (List<?>) data) {
                    if(item instanceof Map) {
                        vectors.add(buildVector((Map<String, Object>) item));
                    } else if(item instanceof DataRow) {
                        vectors.add(buildVector(((DataRow) item).toMap()));
                    }
                }
            } else if(data instanceof Map) {
                vectors.add(buildVector((Map<String, Object>) data));
            } else if(data instanceof DataRow) {
                vectors.add(buildVector(((DataRow) data).toMap()));
            }

            if(!vectors.isEmpty()) {
                index.upsert(vectors, null);
                return vectors.size();
            }
        }
        return -1;
    }

    private VectorWithUnsignedIndices buildVector(Map<String, Object> data) {
        String id = data.containsKey("id") ? String.valueOf(data.get("id")) : UUID.randomUUID().toString();
        List<Float> values = new ArrayList<>();

        if(data.containsKey("vector")) {
            Object vectorObj = data.get("vector");
            if(vectorObj instanceof List) {
                for(Object item : (List<?>) vectorObj) {
                    values.add(((Number) item).floatValue());
                }
            }
        }

        // Build metadata
        Struct metadata = null;
        Map<String, Object> meta = new LinkedHashMap<>();
        for(Map.Entry<String, Object> entry : data.entrySet()) {
            if(!entry.getKey().equalsIgnoreCase("id") && !entry.getKey().equalsIgnoreCase("vector")) {
                meta.put(entry.getKey(), entry.getValue());
            }
        }

        if(!meta.isEmpty()) {
            com.google.protobuf.Struct.Builder structBuilder = com.google.protobuf.Struct.newBuilder();
            for(Map.Entry<String, Object> entry : meta.entrySet()) {
                structBuilder.putFields(entry.getKey(), buildProtobufValue(entry.getValue()));
            }
            metadata = structBuilder.build();
        }

        return new VectorWithUnsignedIndices(id, values, metadata, null);
    }

    private com.google.protobuf.Value buildProtobufValue(Object value) {
        if(value == null) {
            return com.google.protobuf.Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build();
        } else if(value instanceof String) {
            return com.google.protobuf.Value.newBuilder().setStringValue((String) value).build();
        } else if(value instanceof Number) {
            return com.google.protobuf.Value.newBuilder().setNumberValue(((Number) value).doubleValue()).build();
        } else if(value instanceof Boolean) {
            return com.google.protobuf.Value.newBuilder().setBoolValue((Boolean) value).build();
        } else if(value instanceof List) {
            com.google.protobuf.ListValue.Builder listBuilder = com.google.protobuf.ListValue.newBuilder();
            for(Object item : (List<?>) value) {
                listBuilder.addValues(buildProtobufValue(item));
            }
            return com.google.protobuf.Value.newBuilder().setListValue(listBuilder.build()).build();
        } else if(value instanceof Map) {
            com.google.protobuf.Struct.Builder structBuilder = com.google.protobuf.Struct.newBuilder();
            for(Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                structBuilder.putFields(String.valueOf(entry.getKey()), buildProtobufValue(entry.getValue()));
            }
            return com.google.protobuf.Value.newBuilder().setStructValue(structBuilder.build()).build();
        }
        return com.google.protobuf.Value.newBuilder().setStringValue(String.valueOf(value)).build();
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return insert(adapter, runtime, random, data, configs, run, null, null);
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        Pinecone client = client(runtime);
        if(run instanceof PineconeRun) {
            PineconeRun pineconeRun = (PineconeRun) run;
            String tableName = pineconeRun.getTableName();
            List<Object> values = pineconeRun.getValues();

            if(values != null && !values.isEmpty()) {
                io.pinecone.clients.Index index = client.getIndexConnection(tableName);

                List<String> ids = new ArrayList<>();
                for(Object value : values) {
                    ids.add(String.valueOf(value));
                }
                index.deleteByIds(ids);
                return ids.size();
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

    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    @Override
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        Pinecone client = client(runtime);

        try {
            IndexList indexList = client.listIndexes();
            if(indexList != null && indexList.getIndexes() != null) {
                for(IndexModel model : indexList.getIndexes()) {
                    String name = model.getName();
                    if(name != null && !previous.containsKey(name.toUpperCase())) {
                        Table table = new Table(name);
                        previous.put(name.toUpperCase(), (T)table);
                    }
                }
            }
        } catch (PineconeException e) {
        }

        return previous;
    }

    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        Pinecone client = client(runtime);

        try {
            IndexList indexList = client.listIndexes();
            if(indexList != null && indexList.getIndexes() != null) {
                for(IndexModel model : indexList.getIndexes()) {
                    String name = model.getName();
                    if(name == null) {
                        continue;
                    }
                    boolean exists = false;
                    for(Table<T> table : previous) {
                        if(name.equalsIgnoreCase(table.getName())) {
                            exists = true;
                            break;
                        }
                    }
                    if(!exists) {
                        Table table = new Table(name);
                        previous.add((T)table);
                    }
                }
            }
        } catch (PineconeException e) {
        }

        return previous;
    }

    @Override
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, View query, int types) throws Exception {
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
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        Pinecone client = client(runtime);
        String tableName = query.getTable().getName();

        try {
            IndexModel model = client.describeIndex(tableName);
            Index index = new Index();
            index.setName("default");
            index.setTable(query.getTable());
            if(model.getMetric() != null) {
                index.setType(model.getMetric().toString());
            }
            index.addColumn("vector");

            previous.put("DEFAULT", (T) index);
        } catch (PineconeException e) {
        }

        return previous;
    }

    private Pinecone client(DataRuntime runtime) {
        return (Pinecone) runtime.getProcessor();
    }

    public boolean create(DataRuntime runtime, Role role) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, Role role) throws Exception {
        return false;
    }

    public <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Role query) {
        return new ArrayList<>();
    }

    public boolean create(DataRuntime runtime, User user) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, User user) throws Exception {
        return false;
    }

    public <T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, User query) {
        return new ArrayList<>();
    }

    public boolean create(DataRuntime runtime, Privilege privilege) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, Privilege privilege) throws Exception {
        return false;
    }

    public <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) {
        return new ArrayList<>();
    }

    public boolean create(DataRuntime runtime, Database database) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, Database database) throws Exception {
        return false;
    }

    public boolean create(DataRuntime runtime, Table table) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, Table table) throws Exception {
        Pinecone client = client(runtime);
        client.deleteIndex(table.getName());
        return true;
    }

    public boolean create(DataRuntime runtime, View view) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, View view) throws Exception {
        return false;
    }

    public boolean create(DataRuntime runtime, Index index) throws Exception {
        return false;
    }

    public boolean drop(DataRuntime runtime, Index index) throws Exception {
        return false;
    }
}