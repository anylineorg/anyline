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



package org.anyline.data.elasticsearch.worker;

import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverWorker;
import org.anyline.data.elasticsearch.adapter.ElasticSearchAdapter;
import org.anyline.data.elasticsearch.run.ElasticSearchRun;
import org.anyline.data.elasticsearch.runtime.ElasticSearchRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.net.HttpResponse;
import org.anyline.util.FileUtil;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.environment.data.driver.worker.elasticsearch")
public class ElasticSearchWorker implements DriverWorker {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return ElasticSearchAdapter.class;
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
        long cnt = 0;
        ElasticSearchRun r = (ElasticSearchRun)run;
        String method = r.getMethod();
        String endpoint = r.getEndpoint();
        Request request = new Request(method, endpoint);
        request.setJsonEntity(run.getFinalUpdate(false));
        execute(client(runtime), request);
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception{
        long cnt = 0;
        ElasticSearchRun r = (ElasticSearchRun)run;
        String method = r.getMethod();
        String endpoint = r.getEndpoint();
        Request request = new Request(method, endpoint);
        request.setJsonEntity(run.getFinalUpdate(false));
        execute(client(runtime), request);
        return cnt;
    }

    @Override
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception{
        return null;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception{
        String cmd = run.getFinalExecute();

        return 0;
    }
    private RestClient client(DataRuntime runtime){
        return ((ElasticSearchRuntime)runtime).client();
    }
    private HttpResponse execute(RestClient client, Request request) throws Exception{
        HttpResponse result = new HttpResponse();
        Response response = client.performRequest(request);
        //{"_index":"index_user","_id":"102","_version":3,"result":"updated","_shards":{"total":2,"successful":2,"failed":0},"_seq_no":9,"_primary_term":1}
        String content = FileUtil.read(response.getEntity().getContent()).toString();
        result.setText(content);
        return result;
    }
}
