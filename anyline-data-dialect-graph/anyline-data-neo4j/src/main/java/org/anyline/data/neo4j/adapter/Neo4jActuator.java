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

package org.anyline.data.neo4j.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.neo4j.entity.Neo4jEdge;
import org.anyline.data.neo4j.entity.Neo4jRow;
import org.anyline.data.neo4j.entity.Neo4jVertex;
import org.anyline.data.neo4j.runtime.Neo4jRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.util.BeanUtil;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.neo4j")
public class Neo4jActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return org.anyline.data.neo4j.adapter.Neo4jAdapter.class;
    }
    protected Driver driver(DataRuntime runtime) {
        return ((Neo4jRuntime)runtime).driver();
    }
    private Session session(DataRuntime runtime) {
        return ((Neo4jRuntime)runtime).driver().session();
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
        return null;
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    @Override
    public DataSet<DataRow> select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        DataSet<DataRow> set = new DataSet();
        final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
        set.setMetadata(metadatas);
        Session session = session(runtime);
        Result rs = session.run(cmd);
        while (rs.hasNext()) {
            Neo4jRow row = new Neo4jRow();
            set.addRow(row);
            Record record = rs.next();
            List<String> keys = record.keys();
            for(String key : keys) {
                Value value = record.get(key);
                String type = value.type().name();

                Object val = null;
                if("NODE".equals(type)) {
                    val = vertex(value.asNode());
                }else if("RELATIONSHIP".equals(type)){
                    val = edge(value.asRelationship());
                }else {
                    val = value(value);
                }
                row.put(key, val);
            }
        }
        session.close();
        return set;
    }
    private Neo4jVertex vertex(Node node){
        Neo4jVertex vertex = new Neo4jVertex();
        for(String key : node.keys()) {
            Value value = node.get(key);
            String type = value.type().name();
            Object val = null;
            if("NODE".equals(type)) {
                val = vertex(value.asNode());
            }else if("RELATIONSHIP".equals(type)){
                val = edge(value.asRelationship());
            }else {
                val = value(value);
            }
            vertex.put(key, val);
        }
        vertex.setPrimaryValue(node.id());
        List<Table> tables = new ArrayList<>();;
        for(String label : node.labels()) {
            tables.add(new Table(label));
        }
        vertex.setTables(tables);
        return vertex;
    }
    protected Neo4jEdge edge(Relationship relationship){
        Neo4jEdge edge = new Neo4jEdge();
        for(String key : relationship.keys()) {
            Value value = relationship.get(key);
            String type = value.type().name();
            Object val = null;
            if("NODE".equals(type)) {
                val = vertex(value.asNode());
            }else if("RELATIONSHIP".equals(type)){
                val = edge(value.asRelationship());
            }else {
                val = value(value);
            }
            edge.put(key, val);
        }
        String type = relationship.type();
        edge.setTable(type);
        edge.setPrimaryValue(relationship.id());
        Long start = relationship.startNodeId();
        Long end = relationship.endNodeId();
        return edge;
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return null;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        Session session = session(runtime);
        String cmd = run.getFinalInsert();
        try (Transaction tx = session.beginTransaction()) {
            Result result = tx.run(cmd);
            if(result.hasNext()) {
                Record record = result.single();
                int size = record.size();
                if(size > 0) {
                    if (data instanceof Collection) {
                        Collection list = (Collection) data;
                        int idx = 0;
                        List<String> keys = record.keys();
                        for (Object item : list) {
                            String key = keys.get(idx);
                            Object value = value(record.get(idx));
                            set(runtime, random, item, key, value);
                            idx++;
                        }
                    } else {
                        String key = record.keys().get(0);
                        Object value = value(record.get(0));
                        set(runtime, random, data, key, value);
                    }
                }
            }
            tx.commit();
        }
        session.close();
        return 0;
    }
    private void set(DataRuntime runtime, String random, Object data, String key, Object value) {
        if(data instanceof DataRow && key.startsWith("pk")) {
            DataRow row = (DataRow) data;
            row.setPrimaryValue(value);
            log.info("{}[生成主键:{}]", random, value);
        }else{
            BeanUtil.setFieldValue(data, key, value);
        }
    }
    private Object value(Value value){
        if(null == value){
            return null;
        }
        if(value.type().name().equals("INTEGER")){
            return value.asInt();
        }
        if(value.type().name().equals("LONG")){
            return value.asLong();
        }
        if(value.type().name().equals("DOUBLE")){
            return value.asDouble();
        }
        if(value.type().name().equals("BOOLEAN")){
            return value.asBoolean();
        }
        if(value.type().name().equals("STRING")){
            return value.asString();
        }
        if(value.type().name().equals("MAP")){
            return value.asMap();
        }
        return value.asList();
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        Session session = session(runtime);
        String cmd = run.getBuilder().toString();
        session.writeTransaction(tx -> tx.run(cmd));
        //不返回影响行数
        session.close();
        return 0;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        String cmd = run.getFinalExecute();
        Session session = session(runtime);
        session.run(cmd);
        session.close();
        return 0;
    }
}
