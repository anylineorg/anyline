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


package org.anyline.data.faiss.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.faiss.run.FAissRun;
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
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.faiss")
public class FAissActuator implements DriverActuator {
    public static final Log log = LogProxy.get(FAissActuator.class);

    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return FAissAdapter.class;
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
        return "Faiss";
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
        return new DataSet();
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        if(null == run) {
            return result;
        }
        //参考图数据库的方式: 从通用Run接口取索引/条件/参数/分页, 不依赖具体的Run子类
        String index = run.getTableName();
        if(null == runtime || null == runtime.getProcessor()) {
            log.warn("[Faiss select 未执行][index:{}][原因:Faiss官方没有Java SDK, 也没有官方服务端协议, 需要注入自建服务客户端]", index);
            return result;
        }
        log.warn("[Faiss select 未执行][index:{}][原因:未实现客户端调用, 请覆盖FAissActuator.maps实现]", index);
        return result;
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
        if(null == run) {
            return -1;
        }
        log.warn("[Faiss count 未执行][index:{}][原因:Faiss官方没有Java SDK, 也没有官方服务端协议, 需要注入自建服务客户端或覆盖FAissActuator.count]", run.getTableName());
        return -1;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        return -1;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
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
        return previous;
    }

    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
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
        return previous;
    }

}