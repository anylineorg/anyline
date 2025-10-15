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

package org.anyline.data.coze.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.coze.entity.Document;
import org.anyline.data.coze.run.CozeRun;
import org.anyline.data.coze.runtime.CozeRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.DefaultPageNavi;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.BeanUtil;
import org.apache.http.entity.StringEntity;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.Coze")
public class CozeActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return CozeAdapter.class;
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
    public DataSet<DataRow>  select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        DataSet<DataRow> set = new DataSet();
        long fr = System.currentTimeMillis();
        CozeRuntime r = (CozeRuntime) runtime;
        Map<String, String> headers = header(r);
        headers.put("Agw-Js-Conv", "str");
        String api = r.client().getHost() + "/open_api/knowledge/document/list";

        Map<String, Object> params = new HashMap<>();
        params.put("dataset_id", table.getId());
        int page = 1;
        int pages = 1;
        int vol = 30;
        while (true) {
            if(page > pages){
                break;
            }
            params.put("page", 1);
            params.put("size", 30);
            HttpResponse response = HttpUtil.post(headers, api, "UTF-8", new StringEntity(BeanUtil.map2json(params), "UTF-8"));
            DataRow json = DataRow.parseJson(response.getText());
            DataSet<DataRow> infos = json.getSet("document_infos");
            for(DataRow info: infos){
                Document document = new Document();
                set.add(document);
                document.setId(info.getString("document_id"));
                document.setName(info.getString("document_name"));
                document.setUrl(info.getString("web_url"));
            }
            PageNavi navi = new DefaultPageNavi();
            navi.setPageRows(vol);
            navi.setTotalRow(json.getLong("total"));

            set.setNavi(navi);
            pages = (int)navi.getTotalPage();
            page ++;
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
        CozeRun r = (CozeRun) run;
        CozeRuntime rt = (CozeRuntime) runtime;
        Map<String, String> headers = header(rt);
        String api = rt.client().getHost() + "/open_api/knowledge/document/create";
        Table table = run.getTable();
        headers.put("Agw-Js-Conv", "str");
        Map<String, Object> body = new HashMap<>();
        body.put("dataset_id", table.getId());
        List<Map> docs = new ArrayList<>();
        List<Document> documents = r.getDocuments();
        cnt = documents.size();
        for(Document document:documents){
            Map<String, Object> doc = new HashMap<>();
            doc.put("name", document.getName());
            Map<String, Object> source = new HashMap<>();
            source.put("web_url", document.getUrl());
            source.put("document_source", 1);
            doc.put("source_info", source);
            Map<String, Object> rule = new HashMap<>();
            rule.put("update_type", 1);
            rule.put("update_interval", 24);
            doc.put("update_rule", rule);
            docs.add(doc);
        }
        body.put("document_bases", docs);
        Map<String, Object> chunk = new HashMap<>();
        chunk.put("chunk_type", 0);
        body.put("chunk_strategy", chunk);
        body.put("format_type", 0);
        String json = BeanUtil.map2json(body);
        HttpResponse response = HttpUtil.post(headers, api,"UTF-8", new StringEntity(json, "UTF-8"));
        int status = response.getStatus();
        String text = response.getText();
        DataRow row = DataRow.parseJson(text);
        DataSet<DataRow> infos = row.getSet("document_infos");
        int i = 0;
        for(DataRow info:infos){
            Document document = documents.get(i);
            document.setId(info.getString("document_id"));
            document.setName(info.getString("name"));
            i++;
        }
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long cnt = 0;
        CozeRun r = (CozeRun)run;
        CozeRuntime rt = (CozeRuntime)runtime;
        Map<String, String> headers = header(rt);
        List<Document> documents = r.getDocuments();
        headers.put("Agw-Js-Conv", "str");
        String api = rt.client().getHost() + "/open_api/knowledge/document/update";
        for(Document document:documents){
            Map<String, Object> params = new HashMap<>();
            params.put("document_id", document.getId());
            params.put("document_name", document.getName());
            Map<String, Object> update_rule = new HashMap<>();
            params.put("update_rule", update_rule);
            update_rule.put("update_type", 0);
            HttpResponse response = HttpUtil.post(headers, api, "UTF-8", new StringEntity(BeanUtil.map2json(params), "UTF-8"));
            cnt ++;
        }
        return cnt;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        long count = 0;
        ACTION action = run.action();
        CozeRun r = (CozeRun)run;
        CozeRuntime rt = (CozeRuntime)runtime;
        if(action == ACTION.DML.DELETE){
            List<Document> documents = r.getDocuments();
            for(Document document:documents){
                count += delete(rt, r.getTable(), document);
            }
        }else if(action == ACTION.DDL.TABLE_CREATE){
            Table table = r.getTable();
            create(rt, table);
            count ++;
        }
        return count;
    }

    public int delete(CozeRuntime runtime, Table table, Document document) throws Exception {
        CozeRuntime rt = (CozeRuntime)runtime;
        Map<String, String> headers = header(rt);
        String api = rt.client().getHost() + "/open_api/knowledge/document/delete";
        headers.put("Agw-Js-Conv", "str");
        Map<String, Object> body = new HashMap<>();
        List<String> docs = new ArrayList<>();
        docs.add(document.getId());
        body.put("document_ids", docs);
        String json = BeanUtil.map2json(body);
        HttpResponse response = HttpUtil.post(headers, api,"UTF-8", new StringEntity(json, "UTF-8"));
        return 1;
    }
    /**
     * 根据文件 创建知识库文档
     * @param url url
     * @param document 文档
     * @param params 参数
     * @return DataRow
     * @throws Exception 异常
     */
    public DataRow upload(CozeRuntime runtime, String url, Document document, Map<String, Object> params) throws Exception {
       return null;
    }

    /**
     * 创建知识库
     * @param table
     * @return
     * @throws Exception
     */
    public Table create(CozeRuntime runtime, Table table) throws Exception{
        String name = table.getName();
        String api = runtime.client().getHost() + "/v1/datasets";
        Map<String, String> headers = header(runtime);
        Map<String, Object> body = new HashMap<>();
        Schema schema = table.getSchema();
        if(null != schema) {
            body.put("space_id", schema.getId());
        }
        body.put("name", name);
        body.put("format_type", 0);
        String json = BeanUtil.map2json(body);
        HttpResponse response = HttpUtil.post(headers, api,"UTF-8", new StringEntity(json, "UTF-8"));
        int status = response.getStatus();
        if(status != 200){
            throw new Exception("创建知识库http状态异常:" + status);
        }
        String text = response.getText();
        DataRow row = DataRow.parseJson(text);
        int code = row.getInt("code");
        if(code != 0){
            throw new Exception(row.getString("msg"));
        }
        table.setId(row.getRow("data").getString("dataset_id"));
        table.setName(name);
        return table;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, Table query, int types) throws Exception {
        if(null == previous){
            previous = new ArrayList<>();
        }

        CozeRuntime rt = (CozeRuntime)runtime;
        Map<String, String> headers = header(rt);
        String api = rt.client().getHost() + "/v1/datasets";
        Map<String, Object> params = new HashMap<>();
        params.put("name", query.getName());
        try {
            HttpResponse response = HttpUtil.get(headers, api, "UTF-8", params);
            int status = response.getStatus();
            String text = response.getText();
            DataRow row = DataRow.parseJson(text);
            DataSet<DataRow> set = row.getRow("data").getSet("dataset_list");
            for(DataRow item:set){
                T table = (T)new Table();
                previous.add(table);
                //table.setSpace(item.getString("space_id"));
                table.setId(item.getString("dataset_id"));
                table.setName(item.getString("name"));
                /*table.setAvatar(item.getString("avatar_url"));
                table.setIcon(item.getString("icon_url"));
                table.setStatus(item.getInt("status"));
                table.setSlice(item.getInt("slice_count"));
                table.setFiles((List<String>)item.getList("file_list"));*/

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return previous;
    }

    public DataRow post(CozeRuntime runtime, String url, Map<String, Object> params) throws Exception{
        url = HttpUtil.mergePath(runtime.client().getHost(), url);
        Map<String, String> header = header(runtime);
        String json = BeanUtil.map2json(params);
        HttpResponse response = HttpUtil.post(header, url, "UTF-8", new StringEntity(json, "UTF-8"));
        int status = response.getStatus();
        String body = response.getText();
        return DataRow.parse(body);
    }

    public void post(CozeRuntime runtime, String url, Map<String, Object> params, File file){
        Map<String, String> header = header(runtime);
        String json = BeanUtil.map2json(params);
        params = new HashMap<>();
        params.put("data", json);
        Map<String, Object> files = new HashMap<>();
        files.put("file", file);
        HttpUtil.upload(url, files, "UTF-8", header, params);
    }
    public DataRow get(CozeRuntime runtime, String url, Map<String, Object> params) throws Exception{
        url = HttpUtil.mergePath(runtime.client().getHost(), url);
        Map<String, String> header = header(runtime);
        HttpResponse response = HttpUtil.get(header, url, "UTF-8", params);
        int status = response.getStatus();
        String body = response.getText();
        return DataRow.parse(body);
    }
    protected Map<String, String> header(CozeRuntime runtime){
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + runtime.getSecret());
        header.put("Content-Type", "application/json");
        return header;
    }
}
