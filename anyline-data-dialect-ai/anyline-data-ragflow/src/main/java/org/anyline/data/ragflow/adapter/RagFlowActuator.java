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


package org.anyline.data.ragflow.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.ragflow.client.RagFlowClient;
import org.anyline.data.ragflow.run.RagFlowRun;
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
 * RagFlow 执行器<br/>
 * 不生成命令, 只根据Adapter生成的Run调用 RagFlowClient(按官方REST API实现) 并封装结果<br/>
 * 从通用Run接口取表名/列/参数/分页, RagFlowRun 只作为可选增强(向量/topK/过滤)
 */
@AnylineComponent("anyline.environment.data.driver.actuator.ragflow")
public class RagFlowActuator implements DriverActuator {
    public static final Log log = LogProxy.get(RagFlowActuator.class);

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return RagFlowAdapter.class;
    }

    @Override
    public int priority() {
        return 0;
    }

    /**
     * 取当前数据源的客户端
     */
    protected RagFlowClient client(DataRuntime runtime) {
        if(null == runtime) {
            return null;
        }
        Object processor = runtime.getProcessor();
        if(processor instanceof RagFlowClient) {
            return (RagFlowClient) processor;
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
        RagFlowClient client = client(runtime);
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
        RagFlowClient client = client(runtime);
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

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        RagFlowClient client = client(runtime);
        if(null == client || null == run) {
            return result;
        }
        String collection = run.getTableName();
        if(BasicUtil.isEmpty(collection)) {
            log.warn("[RagFlow][查询未执行][原因:未指定集合(表名)]");
            return result;
        }
        List<Float> vector = null;
        int topK = 10;
        String filter = null;
        if(run instanceof RagFlowRun) {
            RagFlowRun qr = (RagFlowRun) run;
            topK = qr.getTopK();
            filter = qr.getQuestion();
        }
        if(BasicUtil.isNotEmpty(filter)) {
            //RagFlow 检索: 把过滤条件当作检索语句(question)
            return client.search(collection, vector, topK, filter);
        }
        //没有检索向量时按条件遍历
        int limit = 100;
        int offset = 0;
        PageNavi navi = run.getPageNavi();
        if(null != navi) {
            limit = (int) (navi.getLastRow() - navi.getFirstRow() + 1);
            if(limit <= 0) {
                limit = 100;
            }
            offset = (int) navi.getFirstRow() - 1;
            if(offset < 0) {
                offset = 0;
            }
        }
        return client.scroll(collection, limit, offset, filter);
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
        RagFlowClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String collection = run.getTableName();
        if(BasicUtil.isEmpty(collection)) {
            return -1;
        }
        String filter = null;
        if(run instanceof RagFlowRun) {
            filter = ((RagFlowRun) run).getQuestion();
        }
        return client.count(collection, filter);
    }

    /* *********************************************************************************************
     * 											写入
     ***********************************************************************************************/
    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        RagFlowClient client = client(runtime);
        if(null == client || null == run || null == data) {
            return -1;
        }
        String collection = run.getTableName();
        if(BasicUtil.isEmpty(collection)) {
            return -1;
        }
        return client.upsert(collection, rows(data));
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        RagFlowClient client = client(runtime);
        if(null == client || null == run || null == data) {
            return -1;
        }
        String collection = run.getTableName();
        if(BasicUtil.isEmpty(collection)) {
            return -1;
        }
        return client.upsert(collection, rows(data));
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        RagFlowClient client = client(runtime);
        if(null == client || null == run) {
            return -1;
        }
        String collection = run.getTableName();
        if(BasicUtil.isEmpty(collection)) {
            return -1;
        }
        //按id删除(delete from collection where id in (...))
        List<Object> ids = run.getValues();
        return client.delete(collection, ids);
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