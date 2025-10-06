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

package org.anyline.data.dify.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.dify.entity.Document;
import org.anyline.data.dify.run.DifyRun;
import org.anyline.data.dify.runtime.DifyRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OriginRow;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.net.HttpResponse;
import org.anyline.net.HttpUtil;
import org.anyline.util.BeanUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

@AnylineComponent("anyline.environment.data.driver.actuator.dify")
public class DifyActuator implements DriverActuator {
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return DifyAdapter.class;
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
        DifyRun r = (DifyRun)run;
        String url = "/datasets/" + table.getId() + "/documents";
        Map<String, Object> params = r.getQueryParams();
        DataRow row = post((DifyRuntime) runtime, url, params);
        configs.setLastExecuteTime(System.currentTimeMillis() - fr);
        fr = System.currentTimeMillis();
        PageNavi navi = r.getNavi();
        set.setNavi(navi);
        if(null != row){
            int total = row.getInt("total", 0);
            if(null != navi){
                navi.setTotalRow(total);
            }
            DataSet<DataRow> datas = row.getSet("data");
            if(null != datas){
                for(DataRow data:datas){
                    Document doc = new Document();
                    doc.setId(data.getString("id"));
                    doc.setName(data.getString("name"));
                    set.add(doc);
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
        DifyRuntime rt = (DifyRuntime)runtime;
        DifyRun r = (DifyRun)run;
        Table table = r.getTable();
        List<Document> documents = r.getDocuments();
        for(Document document:documents){
            Map<String, Object> params = new HashMap<>();
            params.put("indexing_technique", "high_quality");
            Map<String, String> rule = new HashMap<>();
            rule.put("mode", "automatic");
            params.put("process_rule", rule);
            File file = document.getFile();
            DataRow row = null;
            if(null != file){
                String url = "/datasets/"+table.getId()+"/document/create-by-file";
                row = upload(rt, url, document, params);
            }else {
                params.put("name", document.getName());
                String url = "/datasets/"+table.getId()+"/document/create-by-text";
                params.put("text", document.getText());
                row = post(rt, url, params);
            }
            DataRow doc = row.getRow("DOCUMENT");
            if(null != doc){
                document.setId(doc.getString("ID"));
            }
            //元数据
            setMetadata(rt, table, document);
        }
        return cnt;
    }

    @Override
    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long cnt = 0;
        return cnt;
    }

    @Override
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        return -1;
    }

    /**
     * 根据文件 创建知识库文档
     * @param url url
     * @param document 文档
     * @param params 参数
     * @return DataRow
     * @throws Exception 异常
     */
    public DataRow upload(DifyRuntime runtime, String url, Document document, Map<String, Object> params) throws Exception {
        url = HttpUtil.mergePath(runtime.client().getHost(), url);
        RequestConfig config = RequestConfig.custom().build();
        HttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();
        try {
            HttpPost post = new HttpPost(url);

            post.setHeader("Authorization", "Bearer " +  runtime.client().getSecret());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);

            String jsonData = BeanUtil.map2json(params);
            builder.addTextBody("data", jsonData, ContentType.TEXT_PLAIN);
            builder.addBinaryBody(
                    "file",
                    document.getFile(),
                    ContentType.APPLICATION_OCTET_STREAM,
                    document.getFile().getName() //这里要用文件名
            );

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            String body = client.execute(post, response -> {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new IOException("上传失败，状态码: " + statusCode + "响应内容: " + responseBody);
                }
            });
            DataRow row = DataRow.parseJson(body);
            return row;
        } finally {
            if (client != null) {
                ((Closeable) client).close();
            }
        }
    }

    /**
     * 知识库添加 元数据定义,添加完成后返回元数据id
     * @param table 知识库
     * @param metadata 元数据
     * @throws Exception 异常
     */
    public void addMetadata(DifyRuntime runtime, Table table, org.anyline.data.dify.entity.Metadata metadata) throws Exception{
        String url = "/datasets/"+table.getId()+"/metadata";
        Map<String, Object> params = new HashMap<>();
        params.put("type", metadata.getType());
        params.put("name", metadata.getName());
        DataRow row = post(runtime, url, params);
        metadata.setId(row.getString("ID"));
    }

    /**
     * 设计文档元数据,所有元数据一次性设置，之前设置过的会被覆盖
     * 注意元数据id需要跟设置知识库元数据时返回的id保持一致
     * @param table 知识库(id)
     * @param documents 文档列表
     * @return boolean
     */
    public boolean setMetadata(DifyRuntime runtime, Table table, List<Document> documents){
        String url = "/datasets/"+table.getId()+"/documents/metadata";
        OriginRow body = new OriginRow();
        DataSet<DataRow> datas = body.puts("operation_data");
        boolean empty = true;
        for(Document document : documents){
            DataRow data = datas.add();
            data.put("document_id", document.getId());
            LinkedHashMap<String, Column> metadatas = document.getMetadatas();
            DataSet<DataRow> metas = data.puts("metadata_list");
            for (Column metadata:metadatas.values()){
                metas.add(((org.anyline.data.dify.entity.Metadata)metadata).map());
                empty = false;
            }
        }
        if(!empty){
            String json = BeanUtil.map2json(body);
            url = HttpUtil.mergePath(runtime.client().getHost(), url);
            Map<String, String> header = header(runtime);
            HttpResponse response = HttpUtil.post(header, url, "UTF-8", new StringEntity(json, "UTF-8"));
            int status = response.getStatus();
            String txt = response.getText();
            if(status == 200){
                return false;
            }
            DataRow row = DataRow.parseJson(txt);
            if(null == row){
                return false;
            }
            return row.getBoolean("success", false);
        }
        return false;
    }
    public boolean setMetadata(DifyRuntime runtime, Table table, Document document){
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        return setMetadata(runtime, table, documents);
    }
    public DataRow post(DifyRuntime runtime, String url, Map<String, Object> params) throws Exception{
        url = HttpUtil.mergePath(runtime.client().getHost(), url);
        Map<String, String> header = header(runtime);
        String json = BeanUtil.map2json(params);
        HttpResponse response = HttpUtil.post(header, url, "UTF-8", new StringEntity(json, "UTF-8"));
        int status = response.getStatus();
        String body = response.getText();
        return DataRow.parse(body);
    }

    public void post(DifyRuntime runtime, String url, Map<String, Object> params, File file){
        Map<String, String> header = header(runtime);
        String json = BeanUtil.map2json(params);
        params = new HashMap<>();
        params.put("data", json);
        Map<String, Object> files = new HashMap<>();
        files.put("file", file);
        HttpUtil.upload(url, files, "UTF-8", header, params);
    }
    public DataRow get(DifyRuntime runtime, String url, Map<String, Object> params) throws Exception{
        url = HttpUtil.mergePath(runtime.client().getHost(), url);
        Map<String, String> header = header(runtime);
        HttpResponse response = HttpUtil.get(header, url, "UTF-8", params);
        int status = response.getStatus();
        String body = response.getText();
        return DataRow.parse(body);
    }
    protected Map<String, String> header(DifyRuntime runtime){
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Accept", "application/json");
        DifyRuntime r = (DifyRuntime)runtime;
        header.put("Authorization", "Bearer " + r.getSecret());
        return header;
    }
}
