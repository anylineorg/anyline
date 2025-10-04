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

package org.anyline.data.elasticsearch.adapter;

import org.anyline.adapter.KeyAdapter;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.elasticsearch.entity.ElasticSearchRow;
import org.anyline.data.elasticsearch.run.ElasticSearchRun;
import org.anyline.data.elasticsearch.runtime.ElasticSearchRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.net.HttpResponse;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AnylineComponent("anyline.environment.data.driver.actuator.elasticsearch")
public class ElasticSearchActuator implements DriverActuator {
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

    public String dsl(DriverAdapter adapter, DataRuntime runtime, String random, String sql) throws Exception {
        String dsl = null;
        Request request = new Request(
            "POST",
            "/_sql/translate");
        DataRow body = new OriginRow();
        body.put("query", sql);
        request.setJsonEntity(body.json());
        HttpResponse response = execute(random, runtime, request);
        dsl = response.getText();
        return dsl;
    }
    @Override
    public DataSet select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        DataSet set = new DataSet();
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        set.setMetadata(columns);
        long fr = System.currentTimeMillis();
        ElasticSearchRun er = (ElasticSearchRun)run;
        String method = er.getMethod();
        String endpoint = er.getEndpoint();
        Request request = new Request(
                method,
                endpoint);
        String body = run.getFinalQuery();
        if(BasicUtil.isNotEmpty(body)) {
            request.setJsonEntity(body);
        }
        HttpResponse response = execute(random, runtime, request);
        configs.setLastExecuteTime(System.currentTimeMillis() - fr);
        fr = System.currentTimeMillis();
        String txt = response.getText();
        if(txt.startsWith("{")) {
            DataRow json = DataRow.parseJson(KeyAdapter.KEY_CASE.SRC, txt);
            DataRow hits_ = json.getRow("hits");
            if(null != hits_) {
                long total = BasicUtil.parseLong(hits_.recursion("total", "value"),0L);
                PageNavi navi = configs.getPageNavi();
                if(null == navi) {
                    navi = new DefaultPageNavi();
                }
                navi.setTotalRow(total);
                set.setNavi(navi);
                DataSet<DataRow> hits = hits_.getSet("hits");
                for(DataRow hit:hits) {
                    ElasticSearchRow row = new ElasticSearchRow();
                    row.setScore(hit.getDouble("_score", 0));
                    DataRow source = hit.getRow("_source");
                    if(null != source) {
                        row.putAll(source);
                    }else{
                        //根据SQL2DSL的在fields中
                        DataRow fields = hit.getRow("fields");
                        if(null != fields) {
                            for (String field : fields.keySet()) {
                                List<?> vs = (List<?>) fields.get(field);
                                if (null != vs && !vs.isEmpty()) {
                                    row.put(field, vs.get(0));
                                } else {
                                    row.put(field, null);
                                }
                            }
                        }
                    }
                    row.put("_id", hit.get("_id"));
                    set.add(row);
                }
                /*   "_index": "es_index_table",
                "_id": "1I3OJ5IBw0-YrZZCmw0k",
                "_score": null,
                "fields": {
                    "title": [
                        "Elasticsearch使用Lucen"
                    ],
                    "content": [
                        "Elasticsearch使用Lucene，"
                    ]
                },
                "sort": [
                    3
                ]*/
            }else{
                //根据sql查询
                /*{"columns":[{"name":"content","type":"text"}],"rows":[["ElaK stack”）。"],["Elasticsearch可以用于搜索各种文档。它提供 片，每个...]*/
                List<?> cols = json.getList("columns");
                List<String> col_names = new ArrayList<>();
                for(Object col:cols){
                    DataRow col_ = (DataRow)col;
                    String col_name = col_.getString("name");
                    col_names.add(col_name);
                    Column column = new Column(col_name, col_.getString("type"));
                    columns.put(col_name.toUpperCase(), column);
                }
                int col_size = cols.size();
                List<?> datas = json.getList("rows");
                for(Object data:datas){
                    DataRow row = new DataRow();
                    List<?> data_ = (List<?>)data;
                    for(int i=0; i<col_size; i++){
                        String col_name = col_names.get(i);
                        Object col_value = data_.get(i);
                        row.put(col_name, col_value);
                    }
                    set.add(row);
                }
            }
        }else{
            String[] lines =txt.split("\n");
            for(String line:lines) {
                DataRow row = set.add();
                String[] cols = BasicUtil.compress(line).split(" ");
                int size = cols.length;
                for(int i=0; i<size; i++) {
                    row.put(i+"", cols[i]);
                }
            }
        }
        configs.setLastPackageTime(System.currentTimeMillis() - fr);
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
        long cnt = 0;
        ElasticSearchRun r = (ElasticSearchRun)run;
        String method = r.getMethod();
        String endpoint = r.getEndpoint();
        Request request = new Request(method, endpoint);
        request.setJsonEntity(run.getFinalUpdate(false));
        //{"_index":"es_index_table","_id":"123","_version":2,"result":"updated","_shards":{"total":2,"successful":2,"failed":0},"_seq_no":2,"_primary_term":1}
        HttpResponse response = execute(random, client(runtime), request);
        if(null != response) {
            String txt = response.getText();
            if(null != txt) {
                try{
                    DataRow json = DataRow.parseJson(txt);
                    String pv = json.getString("_id");
                    if(null != pv) {
                        if(data instanceof DataRow) {
                            DataRow row = (DataRow)data;
                            row.setPrimaryValue(pv);
                        }else{
                            String pk = EntityAdapterProxy.primaryKey(data.getClass(), true);
                            if(null != pk) {
                                BeanUtil.setFieldValue(data, pk, pv);
                            }
                        }
                    }
                }catch (Exception e) {
                    log.error("插入数据异常", e);
                }
            }
        }
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long cnt = 0;
        ElasticSearchRun r = (ElasticSearchRun)run;
        String method = r.getMethod();
        String endpoint = r.getEndpoint();
        Request request = new Request(method, endpoint);
        request.setJsonEntity(run.getFinalUpdate(false));
        execute(random, client(runtime), request);
        return cnt;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return update(adapter, runtime, random, null, null, configs, run);
    }
    protected RestClient client(DataRuntime runtime) {
        return ((ElasticSearchRuntime)runtime).client();
    }
    protected HttpResponse execute(String random, DataRuntime runtime, Request request) throws Exception {
        return execute(random, client(runtime), request);
    }
    protected HttpResponse execute(String random, RestClient client, Request request) throws Exception {
        HttpResponse result = new HttpResponse();
        Response response = client.performRequest(request);
        //{"_index":"index_user","_id":"102","_version":3,"result":"updated","_shards":{"total":2,"successful":2,"failed":0},"_seq_no":9,"_primary_term":1}
        String content = FileUtil.read(response.getEntity().getContent()).toString();
        if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            if(content.contains("\"errors\":true")) {
                log.info("{}[response:{}]", random,content);
            }else {
                log.warn("{}[response:{}...]", random, BasicUtil.cut(content, 0, 500));
            }
        }
        result.setText(content);
        return result;
    }
}
