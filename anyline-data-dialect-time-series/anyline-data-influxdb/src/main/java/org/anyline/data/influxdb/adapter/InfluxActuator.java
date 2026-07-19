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


package org.anyline.data.influxdb.adapter;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.influxdb.entity.InfluxPoint;
import org.anyline.data.influxdb.entity.InfluxSet;
import org.anyline.data.influxdb.metadata.InfluxBucket;
import org.anyline.data.influxdb.metadata.InfluxMeasurement;
import org.anyline.data.influxdb.run.InfluxJsonRun;
import org.anyline.data.influxdb.run.InfluxRun;
import org.anyline.data.influxdb.run.InfluxSqlRun;
import org.anyline.data.influxdb.run.InfluxVndRun;
import org.anyline.data.influxdb.runtime.InfluxRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.influxdb")
public class InfluxActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return InfluxAdapter.class;
    }

    protected InfluxDBClient client(DataRuntime runtime) {
        return ((InfluxRuntime) runtime).client();
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
        return "InfluxDB";
    }

    @Override
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        return null;
    }

    @Override
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> databases = new ArrayList<>();
        InfluxDBClient client = client(runtime);
        List<Bucket> list = client.getBucketsApi().findBuckets();
        for (Bucket bucket : list) {
            databases.add((T) new InfluxBucket(bucket.getName()));
        }
        return databases;
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
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        InfluxSet set = new InfluxSet();
        InfluxRuntime rt = (InfluxRuntime) runtime;
        InfluxRun r = (InfluxRun) run;

        String fluxQuery = buildFluxQuery(r, rt);
        if (BasicUtil.isEmpty(fluxQuery)) {
            return set;
        }

        QueryApi queryApi = client(runtime).getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, rt.org());

        Map<String, InfluxMeasurement> measurements = new HashMap<>();
        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                String tableName = record.getMeasurement();
                if (BasicUtil.isEmpty(tableName)) {
                    tableName = "_measurement";
                }

                InfluxMeasurement measurement = measurements.get(tableName);
                if (measurement == null) {
                    measurement = new InfluxMeasurement(tableName);
                    measurements.put(tableName, measurement);
                }

                InfluxPoint point = new InfluxPoint(measurement);
                for (String key : record.getValues().keySet()) {
                    Object value = record.getValueByKey(key);
                    point.put(key, value);
                }
                set.add(point);
            }
        }

        return set;
    }

    private String buildFluxQuery(InfluxRun run, InfluxRuntime rt) {
        if (run instanceof InfluxVndRun) {
            return ((InfluxVndRun) run).body();
        } else if (run instanceof InfluxSqlRun) {
            return convertInfluxQLToFlux(((InfluxSqlRun) run).sql(), rt.bucket());
        } else if (run instanceof InfluxJsonRun) {
            return "";
        }
        return "";
    }

    private String convertInfluxQLToFlux(String influxQL, String bucket) {
        if (BasicUtil.isEmpty(influxQL)) {
            return "";
        }
        influxQL = influxQL.trim().toLowerCase();
        String flux = "from(bucket: \"" + bucket + "\") ";

        if (influxQL.startsWith("select")) {
            String[] parts = influxQL.split("from", 2);
            String selectPart = parts[0].replace("select", "").trim();
            String fromPart = parts.length > 1 ? parts[1].trim() : "";

            String[] tableParts = fromPart.split("\\s+", 2);
            String measurement = tableParts[0];
            String whereClause = tableParts.length > 1 ? tableParts[1] : "";

            if (!"*".equals(selectPart)) {
                flux += "|> filter(fn: (r) => exists r[\"" + selectPart + "\"]) ";
            }

            flux += "|> filter(fn: (r) => r._measurement == \"" + measurement + "\") ";

            if (whereClause.startsWith("where")) {
                whereClause = whereClause.substring(5).trim();
                String[] conditions = whereClause.split("\\s+and\\s+");
                for (String condition : conditions) {
                    condition = condition.trim();
                    String[] kv = condition.split("\\s*=\\s*", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String value = kv[1].trim();
                        if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        flux += "|> filter(fn: (r) => r[\"" + key + "\"] == \"" + value + "\") ";
                    }
                }
            }
        }

        return flux;
    }

    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        InfluxRuntime rt = (InfluxRuntime) runtime;
        InfluxRun r = (InfluxRun) run;

        String fluxQuery = buildFluxQuery(r, rt);
        if (BasicUtil.isEmpty(fluxQuery)) {
            return result;
        }

        QueryApi queryApi = client(runtime).getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, rt.org());

        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String key : record.getValues().keySet()) {
                    Object value = record.getValueByKey(key);
                    row.put(key, value);
                }
                result.add(row);
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if (maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        InfluxDBClient client = client(runtime);
        WriteApiBlocking api = client.getWriteApiBlocking();
        InfluxRun r = (InfluxRun) run;
        List<Point> points = r.points();
        String bucket = r.bucket();
        String org = r.org();

        InfluxRuntime rt = (InfluxRuntime) runtime;
        if (BasicUtil.isEmpty(bucket)) {
            bucket = rt.bucket();
        }
        if (BasicUtil.isEmpty(org)) {
            org = rt.org();
        }

        api.writePoints(bucket, org, points);
        return points.size();
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        InfluxRuntime rt = (InfluxRuntime) runtime;
        InfluxRun r = (InfluxRun) run;

        if (r instanceof InfluxJsonRun) {
            String bucket = r.bucket();
            String org = r.org();

            if (BasicUtil.isEmpty(bucket)) {
                bucket = rt.bucket();
            }
            if (BasicUtil.isEmpty(org)) {
                org = rt.org();
            }

            String body = r.body();
            if (BasicUtil.isNotEmpty(body)) {
                DeletePredicateRequest request = new DeletePredicateRequest();
                request.setPredicate(body);
                client(runtime).getDeleteApi().delete(request, bucket, org);
                return 1;
            }
        }

        return 0;
    }

    @Override
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    @Override
    public <T extends Table<T>> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table<T> query, int types) throws Exception {
        if (previous == null) {
            previous = new LinkedHashMap<>();
        }

        InfluxRuntime rt = (InfluxRuntime) runtime;
        String bucket = rt.bucket();

        String fluxQuery = "from(bucket: \"" + bucket + "\") |> distinct(column: \"_measurement\")";

        QueryApi queryApi = client(runtime).getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, rt.org());

        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                String measurement = record.getValueByKey("_value").toString();
                if (!previous.containsKey(measurement.toUpperCase())) {
                    T table = (T) new Table();
                    table.setName(measurement);
                    previous.put(measurement.toUpperCase(), table);
                }
            }
        }

        return previous;
    }

    @Override
    public <T extends Table<T>> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table<T> query, int types) throws Exception {
        if (previous == null) {
            previous = new ArrayList<>();
        }

        InfluxRuntime rt = (InfluxRuntime) runtime;
        String bucket = rt.bucket();

        String fluxQuery = "from(bucket: \"" + bucket + "\") |> distinct(column: \"_measurement\")";

        QueryApi queryApi = client(runtime).getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, rt.org());

        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                String measurement = record.getValueByKey("_value").toString();
                boolean exists = false;
                for (Table<T> table : previous) {
                    if (measurement.equalsIgnoreCase(table.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    T table = (T) new Table();
                    table.setName(measurement);
                    previous.add(table);
                }
            }
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
        if (previous == null) {
            previous = new LinkedHashMap<>();
        }

        InfluxRuntime rt = (InfluxRuntime) runtime;
        String bucket = rt.bucket();
        String measurement = table.getName();

        String fluxQuery = "from(bucket: \"" + bucket + "\") " +
                "|> filter(fn: (r) => r._measurement == \"" + measurement + "\") " +
                "|> limit(n: 1) " +
                "|> keys()";

        QueryApi queryApi = client(runtime).getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, rt.org());

        for (FluxTable fluxTable : tables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                String key = record.getValueByKey("_value").toString();
                if (!previous.containsKey(key.toUpperCase())) {
                    Column column = new Column(key);
                    previous.put(key.toUpperCase(), (T) column);
                }
            }
        }

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
        return false;
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