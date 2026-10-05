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


package org.anyline.data.tugraph.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.tugraph.client.TuGraphClient;
import org.anyline.data.tugraph.run.TuGraphRun;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.run.Run;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TuGraph 执行器<br/>
 * 不生成命令, 只根据Adapter生成的Run调用 TuGraphClient(按官方REST API实现) 并封装结果<br/>
 * 从通用Run接口取表名/列/参数/分页, TuGraphRun 只作为可选增强(向量/topK/过滤)
 */
@AnylineComponent("anyline.environment.data.driver.actuator.tugraph")
public class TuGraphActuator implements DriverActuator {
    public static final Log log = LogProxy.get(TuGraphActuator.class);

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return TuGraphAdapter.class;
    }

    @Override
    public int priority() {
        return 0;
    }

    /**
     * 取当前数据源的客户端
     */
    protected TuGraphClient client(DataRuntime runtime) {
        if(null == runtime) {
            return null;
        }
        Object processor = runtime.getProcessor();
        if(processor instanceof TuGraphClient) {
            return (TuGraphClient) processor;
        }
        return null;
    }

    /**
     * 对象转Map(BeanUtil没有直接的toMap, 这里经json转换)
     */
    protected Map<String, Object> toMap(Object obj) {
        if(null == obj) {
            return new LinkedHashMap<>();
        }
        return DataRow.parseJson(BeanUtil.object2json(obj));
    }

    /**
     * 把DataRow/DataSet/Map/Entity转成行列表
     */
    protected List<Map<String, Object>> rows(Object data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if(null == data) {
            return rows;
        }
        if(data instanceof DataSet) {
            for(DataRow row : (DataSet<DataRow>) data) {
                rows.add(row);
            }
        } else if(data instanceof Collection) {
            for(Object item : (Collection<?>) data) {
                if(item instanceof Map) {
                    rows.add((Map<String, Object>) item);
                } else {
                    rows.add(toMap(item));
                }
            }
        } else if(data instanceof Map) {
            rows.add((Map<String, Object>) data);
        } else {
            rows.add(toMap(data));
        }
        return rows;
    }

    /* *********************************************************************************************
     * 											连接
     ***********************************************************************************************/
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
        return product;
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return version;
    }

    /* *********************************************************************************************
     * 											元数据
     ***********************************************************************************************/
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
    @SuppressWarnings("unchecked")
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        TuGraphClient client = client(runtime);
        if(null == client) {
            return previous;
        }
        String name = null != query ? query.getName() : null;
        for(Map<String, Object> item : client.collections()) {
            Object value = item.get("name");
            if(null == value) {
                continue;
            }
            String collection = value.toString();
            if(BasicUtil.isNotEmpty(name) && !name.equalsIgnoreCase(collection)) {
                continue;
            }
            T table = (T) new Table(collection);
            previous.put(collection, table);
        }
        return previous;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        TuGraphClient client = client(runtime);
        if(null == client) {
            return previous;
        }
        String name = null != query ? query.getName() : null;
        for(Map<String, Object> item : client.collections()) {
            Object value = item.get("name");
            if(null == value) {
                continue;
            }
            String collection = value.toString();
            if(BasicUtil.isNotEmpty(name) && !name.equalsIgnoreCase(collection)) {
                continue;
            }
            T table = (T) new Table(collection);
            previous.add(table);
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
        return previous;
    }

    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
        return previous;
    }

    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    /* *********************************************************************************************
     * 											查询
     ***********************************************************************************************/
    @Override
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        DataSet<DataRow> set = new DataSet<>();
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        for(Map<String, Object> map : maps) {
            DataRow row = new DataRow();
            row.putAll(map);
            set.add(row);
        }
        return set;
    }

    @Override
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception {
        return new DataSet<>();
    }

    /**
     * 子图(graph): 优先 TuGraphRun.graph → 表名 → 默认 default
     */
    protected String graph(Run run) {
        String graph = null;
        if(run instanceof TuGraphRun) {
            graph = ((TuGraphRun) run).getGraph();
        }
        if(BasicUtil.isEmpty(graph)) {
            graph = run.getTableName();
        }
        if(BasicUtil.isEmpty(graph)) {
            graph = TuGraphClient.DEFAULT_GRAPH;
        }
        return graph;
    }

    /**
     * 取 Run 中已合成的 openCypher(type: select/insert/update/delete/execute), 都没有时取 builder 内容
     */
    protected String script(Run run, String type) {
        if(null == run) {
            return null;
        }
        String script = null;
        if("select".equals(type)) {
            script = run.getFinalSelect();
        } else if("insert".equals(type)) {
            script = run.getFinalInsert();
        } else if("update".equals(type)) {
            script = run.getFinalUpdate();
        } else if("delete".equals(type)) {
            script = run.getFinalDelete();
        } else {
            script = run.getFinalExecute();
        }
        if(BasicUtil.isEmpty(script) && null != run.getBuilder()) {
            script = run.getBuilder().toString();
        }
        return script;
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        TuGraphClient client = client(runtime);
        if(null == client || null == run) {
            return result;
        }
        String script = script(run, "select");
        if(BasicUtil.isEmpty(script)) {
            log.warn("[TuGraph][查询未执行][原因:未合成Cypher]");
            return result;
        }
        return client.cypher(graph(run), script);
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(!maps.isEmpty()) {
            return maps.get(0);
        }
        return new LinkedHashMap<>();
    }

    /**
     * 统计: DriverActuator接口没有声明count, 由Adapter.count调用
     */
    public long count(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        TuGraphClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String script = script(run, "select");
        if(BasicUtil.isEmpty(script)) {
            return -1;
        }
        //TuGraph 没有独立的count接口, 按查询执行后取行数
        return client.cypher(graph(run), script).size();
    }

    /* *********************************************************************************************
     * 											写入
     ***********************************************************************************************/
    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        TuGraphClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String script = script(run, "insert");
        if(BasicUtil.isEmpty(script)) {
            log.warn("[TuGraph][写入未执行][原因:未合成Cypher]");
            return -1;
        }
        return client.execute(graph(run), script);
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        TuGraphClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String script = script(run, "update");
        if(BasicUtil.isEmpty(script)) {
            log.warn("[TuGraph][更新未执行][原因:未合成Cypher]");
            return -1;
        }
        return client.execute(graph(run), script);
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        TuGraphClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        //删除优先用 delete 命令, 没有时用 execute(如 DROP/CREATE 等原生Cypher)
        String script = script(run, "delete");
        if(BasicUtil.isEmpty(script)) {
            script = script(run, "execute");
        }
        if(BasicUtil.isEmpty(script)) {
            log.warn("[TuGraph][执行未执行][原因:未合成Cypher]");
            return -1;
        }
        return client.execute(graph(run), script);
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, List<Run> runs) throws Exception {
        long count = 0;
        for(Run run : runs) {
            count += execute(adapter, runtime, random, configs, run);
        }
        return count;
    }
}