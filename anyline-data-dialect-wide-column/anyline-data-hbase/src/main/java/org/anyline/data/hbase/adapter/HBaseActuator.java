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

package org.anyline.data.hbase.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataSet;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AnylineComponent("anyline.environment.data.driver.actuator.hbase")
public class HBaseActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return HBaseAdapter.class;
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
        return set;
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
        long cnt = -1;

        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {

        //不返回影响行数
        return 0;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        String cmd = run.getFinalExecute();

        return 0;
    }
}
