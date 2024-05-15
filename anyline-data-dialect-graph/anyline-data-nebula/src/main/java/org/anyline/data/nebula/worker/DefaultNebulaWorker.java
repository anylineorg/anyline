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



package org.anyline.data.nebula.worker;

import com.vesoft.nebula.Vertex;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.data.DateWrapper;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverWorker;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.nebula.adapter.NebulaAdapter;
import org.anyline.data.nebula.runtime.NebulaRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.graph.GraphRow;
import org.anyline.entity.graph.VertexRow;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultNebulaWorker implements DriverWorker {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return NebulaAdapter.class;
    }
    protected SessionPool session(DataRuntime runtime){
        return ((NebulaRuntime)runtime).session();
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
    public DataSet select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        DataSet set = new DataSet();
        final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
        set.setMetadata(metadatas);
        SessionPool sesion = session(runtime);
        if(null == sesion){
            return set;
        }
        ResultSet rs = sesion.execute(cmd);
        /*
         *是否忽略查询结果中顶层的key,可能返回多个结果集
         * 0-不忽略
         * 1-忽略
         * 2-如果1个结果集则忽略 多个则保留
         */
        int IGNORE_GRAPH_QUERY_RESULT_TOP_KEY = ConfigStore.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY(configs);
        /*
         * 是否忽略查询结果中的表名,数据可能存在于多个表中
         * 0-不忽略 CRM_USER.id
         * 1-忽略 id
         * 2-如果1个表则忽略 多个表则保留
         */
        int IGNORE_GRAPH_QUERY_RESULT_TABLE = ConfigStore.IGNORE_GRAPH_QUERY_RESULT_TABLE(configs);
        /*
         * 是否合并查询结果中的表,合并后会少一层表名被合并到key中(如果不忽略表名)
         * 0-不合并 {"HR_USER":{"name":"n22","id":22},"CRM_USER":{"name":"n22","id":22}}
         * 1-合并  {"HR_USER.name":"n22","HR_USER.id":22,"CRM_USER.name":"n22","CRM_USER.id":22}}
         * 2-如果1个表则合并 多个表则不合并
         */
        int MERGE_GRAPH_QUERY_RESULT_TABLE = ConfigStore.MERGE_GRAPH_QUERY_RESULT_TABLE(configs);
        int size = rs.rowsSize();
        List<String> cols = rs.getColumnNames();
        if(IGNORE_GRAPH_QUERY_RESULT_TOP_KEY == 2){
            if(cols.size() == 1){
                IGNORE_GRAPH_QUERY_RESULT_TOP_KEY = 1;
            }else{
                IGNORE_GRAPH_QUERY_RESULT_TOP_KEY = 0;
            }
        }
        for(int  i=0; i<size; i++) {
            ResultSet.Record record = rs.rowValues(i); //一行数据
            GraphRow top = new GraphRow();
            for (String col : cols) { //return中的key
                ValueWrapper wrapper = record.get(col);
                if(wrapper.isEmpty()){
                    top.put(col, null);
                    continue;
                }
                com.vesoft.nebula.Value value = wrapper.getValue();
                if (wrapper.isVertex()) {//点
                    DataRow row = null;
                    if (IGNORE_GRAPH_QUERY_RESULT_TOP_KEY == 0) { //是否忽略查询结果中顶层的key
                        row = new VertexRow();
                        top.put(col, row);
                    } else {
                        row = top;
                    }
                    Vertex vertex = value.getVVal();
                    Object vid = vertex.vid.getFieldValue();
                    if (vid instanceof byte[]) {
                        vid = new String((byte[]) vid);
                    }
                    row.setPrimaryValue(vid);
                    if (MERGE_GRAPH_QUERY_RESULT_TABLE == 2) {
                        if (vertex.tags.size() > 1) {
                            MERGE_GRAPH_QUERY_RESULT_TABLE = 1;
                        } else {
                            MERGE_GRAPH_QUERY_RESULT_TABLE = 0;
                        }
                    }
                    for (com.vesoft.nebula.Tag tag : vertex.tags) {
                        String tag_name = new String(tag.name);
                        Table src_table = new org.anyline.data.nebula.metadata.Tag(tag_name);
                        top.addTable(src_table);
                        row.addTable(src_table);
                        DataRow tag_row = null;
                        if (MERGE_GRAPH_QUERY_RESULT_TABLE == 1) {
                            tag_row = row;
                        } else {
                            tag_row = new VertexRow();
                            tag_row.addTable(src_table);
                            row.put(tag_name, tag_row);
                        }
                        Map<byte[], com.vesoft.nebula.Value> props = tag.getProps();
                        for (byte[] key : props.keySet()) {
                            String k = new String(key);
                            com.vesoft.nebula.Value v = props.get(key);
                            Object fv = v.getFieldValue();
                            if (fv instanceof byte[]) {
                                fv = new String((byte[]) fv);
                            }
                            if (MERGE_GRAPH_QUERY_RESULT_TABLE == 1 && IGNORE_GRAPH_QUERY_RESULT_TABLE != 1) {
                                k = tag_name + "." + k;
                            }
                            tag_row.put(k, fv);
                        }
                    }
                }else if(wrapper.isEdge()){

                }else if(wrapper.isString()){
                    top.put(col, wrapper.asString());
                }else if(wrapper.isBoolean()){
                    top.put(col, wrapper.asBoolean());
                }else if(wrapper.isDouble()){
                    top.put(col, wrapper.asDouble());
                }else if(wrapper.isLong()){
                    top.put(col, wrapper.asLong());
                }else if(wrapper.isDate()){
                    DateWrapper date = wrapper.asDate();
                    LocalDate local = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                    top.put(col, local);
                }else if(wrapper.isTime()){
                    top.put(col, wrapper.asTime().getLocalTime());
                }else if(wrapper.isDateTime()){
                    top.put(col, wrapper.asDateTime().getLocalDateTime());
                }else if(wrapper.isList()){
                    top.put(col, wrapper.asList());
                }else{
                    top.put(col, wrapper.asString());
                }
            }
            set.addRow(top);
        }

        StreamHandler _handler = null;
        if(null != configs){
            DataHandler handler = configs.handler();
            if(handler instanceof StreamHandler){
                _handler = (StreamHandler) handler;
            }
        }
        return set;
    }

    @Override
    public DataSet querys(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception{
        return null;
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception{
        return null;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception{
        long cnt = -1;
        SessionPool session = session(runtime);
        if(null == session){
            return cnt;
        }
        String cmd = run.getFinalInsert();
        ResultSet rs = session.execute(cmd);
        if(!rs.isSucceeded()){
            throw new SQLUpdateException(rs.getErrorMessage());
        }
        cnt = run.getRows();
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception{
        SessionPool session = session(runtime);
        String cmd = run.getBuilder().toString();
        ResultSet rs = session.execute(cmd);
        if(!rs.isSucceeded()){
            throw new SQLUpdateException(rs.getErrorMessage());
        }
        //不返回影响行数
        return 0;
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception{
        return null;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception{
        String cmd = run.getFinalExecute();
        SessionPool session = session(runtime);
        ResultSet rs = session.execute(cmd);
        if(!rs.isSucceeded()){
            throw new SQLUpdateException(rs.getErrorMessage());
        }
        return 0;
    }
}
