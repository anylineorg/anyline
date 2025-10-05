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

package org.anyline.data.influxdb.adapter;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.write.Point;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.influxdb.entity.InfluxPoint;
import org.anyline.data.influxdb.entity.InfluxSet;
import org.anyline.data.influxdb.metadata.InfluxBucket;
import org.anyline.data.influxdb.metadata.InfluxMeasurement;
import org.anyline.data.influxdb.run.InfluxRun;
import org.anyline.data.influxdb.runtime.InfluxRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataSet;
import org.anyline.metadata.*;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.apache.http.entity.StringEntity;

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
        return ((InfluxRuntime)runtime).client();
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

    /**
     * 数据库列表
     * @param adapter adapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return List
     */
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> databases = new ArrayList<>();
        InfluxDBClient client = client(runtime);
        List<Bucket> list =  client.getBucketsApi().findBuckets();
        for (Bucket bucket : list) {
            databases.add((T)new InfluxBucket(bucket.getName()));
        }
        return databases;
    }

    @Override
    public DataSet<DataRow> select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        InfluxSet set = new InfluxSet();
        InfluxRuntime rt = (InfluxRuntime)runtime;
        InfluxRun r = (InfluxRun)run;
        Map<String, String> header = r.headers();
        header.put("Authorization","Token " + rt.token());
        header.put("Accept", "application/csv");
        String api = r.api();
        String method = r.method();
        String url = HttpUtil.createFullPath(rt.getUrl(), api);
        HttpResponse response = null;
        String result = null;
        if("get".equalsIgnoreCase(method)) {
            response = HttpUtil.get(header, url);
        }else{
            String body = r.body();
            if(BasicUtil.isNotEmpty(body)) {
                response = HttpUtil.post(header, url, new StringEntity(body));
            }else{
                response = HttpUtil.post(header, url);
            }
        }
        result = response.getText();
        String alt = result;
        if(response.getStatus() == 200) {
            alt = BasicUtil.ellipsis(200, alt);
        }

        log.info("[influx http api][action:{}][status:{}]{}", run.action(), response.getStatus(), alt);
        String[] lines = result.split("\n");
        int len = lines.length;
        if(len > 1) {
            String[] titles = lines[0].split(",");
            int vol = titles.length;
            Map<String, InfluxMeasurement> measurements = new HashMap<>();
            for(int i=1; i<len; i++) {
                String[] cols = lines[i].split(",");
                String table_name = cols[0];
                InfluxMeasurement measurement = measurements.get(table_name);
                if(null == measurement) {
                    measurement = new InfluxMeasurement(table_name);
                    measurements.put(table_name, measurement);
                }
                InfluxPoint point = new InfluxPoint(measurement);
                for(int c=2; c<vol; c++) {
                    String value = "";
                    if(c < cols.length) {
                        value =cols[c];
                    }
                    point.put(titles[c], value);
                }
                set.add(point);
            }
        }
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
        InfluxDBClient client = client(runtime);
        WriteApiBlocking api = client.getWriteApiBlocking();
        InfluxRun r = (InfluxRun)run;
        List<Point> points = r.points();
        String bucket = r.bucket();
        String org = r.org();
        api.writePoints(bucket, org, points);
        cnt = points.size();
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        InfluxRuntime rt = (InfluxRuntime)runtime;
        InfluxRun r = (InfluxRun)run;
        Map<String, String> header = r.headers();
        header.put("Authorization","Token " + rt.token());
        String api = r.api();
        String method = r.method();
        String url = HttpUtil.createFullPath(rt.getUrl(), api);

        HttpResponse response = null;
        if("get".equalsIgnoreCase(method)) {
            response = HttpUtil.get(header, url);
        }else{
            String body = r.body();
            if(BasicUtil.isNotEmpty(body)) {
                response = HttpUtil.post(header, url, new StringEntity(body));
            }else{
                response = HttpUtil.post(header, url);
            }
        }
        log.info("[influx http api][action:{}][status:{}][response:{}]", run.action(), response.getStatus(), response.getText());
        return 0;
    }
}
