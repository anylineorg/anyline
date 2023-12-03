package org.anyline.data.elasticsearch.adapter;

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.net.HttpResponse;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository("anyline.data.adapter.elasticsearch")
public class ElasticSearchAdapter extends DefaultDriverAdapter implements DriverAdapter {
    private static Logger log = LoggerFactory.getLogger(ElasticSearchAdapter.class);

    @Override
    public DatabaseType type() {
        return DatabaseType.ElasticSearch;
    }

    @Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, DataSource dataSource, T meta) {

    }

    @Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, Connection con, T meta) {

    }

    @Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, T meta) {

    }

    @Override
    public LinkedHashMap<String, Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        return null;
    }

    public ElasticSearchAdapter(){
        super();
        for (ElasticSearchColumnTypeAlias alias : ElasticSearchColumnTypeAlias.values()) {
            types.put(alias.name(), alias.standard());
        }
    }

    /*PUT /es_db
 {
 "mappings":{
     "properties":{
         "name":{"type":"keyword","index":true,"store":true},
         "sex":{"type":"integer","index":true,"store":true},
         "age":{"type":"integer","index":true,"store":true},
         "book":{"type":"text","index":true,"store":true},
         "address":{"type":"text","index":true,"store":true}
     }
 }
 }*/
    public boolean create(DataRuntime runtime, Table meta){
        boolean result = false;
        DataRow body = new DataRow(KeyAdapter.KEY_CASE.SRC);
        DataRow mappings = new DataRow(KeyAdapter.KEY_CASE.SRC);
        LinkedHashMap<String, Column> columns = meta.getColumns();
        LinkedHashMap<String,DataRow> properties = new LinkedHashMap<>();
        for(Column column:columns.values()){
            DataRow col = new DataRow(KeyAdapter.KEY_CASE.SRC);
            String type = column.getFullType();
            Boolean index = column.getIndex();
            Boolean store = column.getStore();
            String analyzer = column.getAnalyzer();
            String searchAnalyzer = column.getSearchAnalyzer();
            if(BasicUtil.isNotEmpty(type)){
                col.put("type", type);
            }
            if(null != index){
                col.put("index", index);
            }
            if(null != store){
                col.put("store", store);
            }
            if(BasicUtil.isNotEmpty(analyzer)){
                col.put("analyzer", analyzer);
            }
            if(BasicUtil.isNotEmpty(searchAnalyzer)){
                col.put("search_analyzer", searchAnalyzer);
            }
            properties.put(column.getName(), col);
        }
        if(null != meta.getExtend()){
            body.put("settings", meta.getExtend());
        }
        body.put("mappings", mappings);
        mappings.put("properties", properties);
        String json = body.toJSON();
        log.warn("[create index][map:{}]", json);
        Request request = new Request("PUT", "/" + meta.getName());
        request.setJsonEntity(json);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200){
            result = true;
        }
        return result;
    }
    public boolean drop(DataRuntime runtime, Table table){
        boolean result = false;
        Request request = new Request("DELETE", table.getName());
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200){
            result = true;
        }
        return result;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }

    protected RestClient client(DataRuntime runtime){
        Object processor = runtime.getProcessor();
        return (RestClient) processor;
    }
    /*
PUT index_user/_bulk
{"index":{"_index":"index_user", "_id":"10011"}}
{"id":1001, "name":"a b", "age":20}
{"index":{"_index":"index_user", "_id":"10012"}}
{"id":1002, "name":"b c", "age":20}
{"index":{"_index":"index_user", "_id":"10013"}}
{"id":1003, "name":"c d", "age":30}*/
    public boolean inserts(DataRuntime runtime, String table, Collection list){
        boolean result = false;
        String pk = "_id";
        String method = "PUT";
        String endpoint = table+"/_bulk";
        String body = null;
        StringBuilder builder = new StringBuilder();
        for(Object entity:list){
            Object _id = BeanUtil.getFieldValue(entity, pk);
            if (null == _id) {
                pk = "id";
                _id = BeanUtil.getFieldValue(entity, pk);
            }
            builder.append("{\"index\":{\"_index\":\"").append(table).append("\", \"_id\":\"").append(_id).append("\"}}\n");
            builder.append(BeanUtil.object2json(entity)).append("\n");
        }
        Request request = new Request(
                method,
                endpoint);
        body = BeanUtil.object2json(builder.toString());
        request.setJsonEntity(body);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200 ||  response.getStatus() == 201){
            result = true;
        }
        return result;
    }

    /**
     *PUT index_user/_bulk
     * {"index":{"_index":"index_user", "_id":"10011"}}
     * {"id":1001, "name":"a b", "age":20}
     * {"index":{"_index":"index_user", "_id":"10012"}}
     * {"id":1002, "name":"b c", "age":20}
     * {"index":{"_index":"index_user", "_id":"10013"}}
     * {"id":1003, "name":"c d", "age":30}
     * @param table
     * @param set
     * @return boolean
     */
    public boolean insert(DataRuntime runtime, String table, DataSet set){
        boolean result = false;
        String method = "PUT";
        String endpoint = "*/_bulk";
        String body = null;
        StringBuilder builder = new StringBuilder();
        for(DataRow row:set){
            String pk = "_id";
            Object _id = BeanUtil.getFieldValue(row, pk);
            if (null == _id) {
                pk = "id";
                _id = BeanUtil.getFieldValue(row, pk);
            }
            row.remove("_id");
            builder.append("{\"index\":{\"_index\":\"").append(table).append("\", \"_id\":\"").append(_id).append("\"}}\n");
            builder.append(row.toJSON()).append("\n");
        }
        Request request = new Request(
                method,
                endpoint);
        body = builder.toString();
        request.setJsonEntity(body);
        HttpResponse response = exe(runtime, request);
        return result;
    }
    public boolean insert(DataRuntime runtime, String table, DataRow entity){
        boolean result = false;
        String pk = "_id";
        String method = "POST";
        String endpoint = null;
        String body = null;
        //一般需要设置用于索引的主键 如法规id = l100 问答id = q100
        Object _id = BeanUtil.getFieldValue(entity, pk);
        if (null == _id) {
            pk = "id";
            _id = BeanUtil.getFieldValue(entity, pk);
        }
        endpoint = table + "/_doc/";
        if (BasicUtil.isNotEmpty(_id)) {
            method = "PUT";
            endpoint += _id;
        }
        entity.remove("_id");
        Request request = new Request(
                method,
                endpoint);
        body = BeanUtil.object2json(entity);
        request.setJsonEntity(body);
        HttpResponse response = exe(runtime, request);
        if(BasicUtil.isEmpty(_id)){
            DataRow row = DataRow.parse(response.getText());
            _id = row.getString(pk);
            if(BasicUtil.isNotEmpty(_id)){
                BeanUtil.setFieldValue(entity, pk, _id);
            }
        }
        return result;
    }
    private HttpResponse exe(DataRuntime runtime, Request request){
        HttpResponse result = new HttpResponse();
        RestClient client = client(runtime);
        try {
            Response response = client.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            result.setStatus(status);
            //{"_index":"index_user","_id":"102","_version":3,"result":"updated","_shards":{"total":2,"successful":2,"failed":0},"_seq_no":9,"_primary_term":1}
            String content = FileUtil.read(response.getEntity().getContent()).toString();
            result.setText(content);
            log.warn("[status:{}]", status);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public DataRow get(DataRuntime runtime, String table, String id){
        DataRow result = null;
        String method = "GET";
        String endpoint = "/"+table+"/_doc/"+id;
        Request request = new Request(
                method,
                endpoint);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200) {
            String txt = response.getText();
            DataRow row = DataRow.parseJson(txt);
            result = row.getRow("_source");
        }
        return result;
    }
    public DataSet search(DataRuntime runtime, String table, DataRow body, PageNavi page){
        DataSet set = null;
        if(null != page){
            body.put("from", page.getFirstRow());
            body.put("size", page.getPageRows());
        }
        String method = "POST";
        String endpoint = table+"/_search";
        Request request = new Request(
                method,
                endpoint);
        String json = body.toLowerKey(true).toJSON();
        log.warn("[search][body:{}]", body);
        request.setJsonEntity(json);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200) {
            String txt = response.getText();
            DataRow row = DataRow.parseJson(txt);
            Object total = row.recursion("hits", "total", "value");
            if(null == page){
                page = new DefaultPageNavi();
            }
            page.setTotalRow(BasicUtil.parseInt(total,0));
            set = new DataSet();
            set.setNavi(page);
            DataSet hits = row.getRow("hits").getSet("hits");
            for(DataRow hit:hits){
                DataRow item = hit.getRow("_source");
                item.put("_id", hit.get("_id"));
                DataRow highlight = hit.getRow("highlight");
                if(null != highlight){
                    for(String key:highlight.keySet()){
                        List vals = highlight.getList(key);
                        if(null != vals && vals.size()>0){
                            item.put(key, vals.get(0));
                        }
                    }
                }
                set.add(item);
            }

        }
        return set;
    }
    /*GET _analyze
{
  "analyzer": "ik_max_word",
  "text": ["马铃薯真好吃"]
}
*/
    public LinkedHashMap<String,DataRow> analyze(DataRuntime runtime, String key){
        return analyze(runtime, key, null);
    }
    public LinkedHashMap<String,DataRow> analyze(DataRuntime runtime, String key, String mode){
        LinkedHashMap<String,DataRow> maps = new LinkedHashMap<>();
        DataRow body = new DataRow(KeyAdapter.KEY_CASE.SRC);
        if(BasicUtil.isEmpty(mode)){
            mode = "ik_smart";
        }
        body.put("analyzer", mode);
        body.put("text", new String[]{key});

        Request request = new Request(
                "GET",
                "_analyze");
        request.setJsonEntity(BeanUtil.object2json(body));
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200) {
            DataRow row = DataRow.parseJson(response.getText());
            DataSet tokens = row.getSet("tokens");
            for(DataRow token:tokens){
                String k = token.getString("token");
                if(k.length() > 1){
                    maps.put(k, token);
                }
            }
        }
        return maps;
    }


    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean strut) {
        LinkedHashMap<String, T> tables = new LinkedHashMap<>();
        RestClient client = client(runtime);
        String method = "GET";
        String endpoint = "_cat/indices";
        Request request = new Request(
                method,
                endpoint);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200 ||  response.getStatus() == 201){
            String txt =response.getText();
            String[] lines =txt.split("\n");
            for(String line:lines){
                String[] cols = BasicUtil.compress(line).split(" ");
                if(cols.length>2) {
                    T table = (T)new Table(cols[2]);
                    tables.put(cols[2].toUpperCase(), table);
                }
            }
        }
        if(strut){
            for(Table table:tables.values()){
                LinkedHashMap<String, Column> columns = columns(runtime, random, false, table, false);
                table.setColumns(columns);
            }
        }
        return tables;
    }


    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary) {
        LinkedHashMap<String, T> columns = new LinkedHashMap<>();
        RestClient client = client(runtime);
        String method = "GET";
        String endpoint = table.getName();
        Request request = new Request(
                method,
                endpoint);
        HttpResponse response = exe(runtime, request);
        if(response.getStatus() == 200 ||  response.getStatus() == 201){
            String txt =response.getText();
            DataRow row = DataRow.parseJson(KeyAdapter.KEY_CASE.SRC, txt);
            DataRow fields = (DataRow)row.recursion(table.getName(),"mappings","properties");
            if(null != fields){
                List<String> keys = fields.keys();
                for(String key:keys){
                    DataRow ps = fields.getRow(key);
                    if(null != ps) {
                        T column = (T)new Column(key);
                        column.setType(ps.getString("type"));
                        column.setStore(ps.getBoolean("store", null));
                        column.setAnalyzer(ps.getString("analyzer"));
                        column.setSearchAnalyzer(ps.getString("search_analyzer"));
                        columns.put(key.toUpperCase(), column);
                    }

                }
            }
        }
        return columns;
    }
}
