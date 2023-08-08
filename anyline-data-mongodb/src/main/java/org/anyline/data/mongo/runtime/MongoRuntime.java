package org.anyline.data.mongo.runtime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ClientHolder;
import org.anyline.data.util.DriverAdapterHolder;

public class MongoRuntime implements DataRuntime {

    /**
     * 表示数据源名称
     */
    private String key;
    /**
     * 运行环境特征 如jdbc-url
     * 用来匹配 DriverAdapter
     */
    protected String feature;
    /**
     * 运行环境版本 用来匹配 DriverAdapter
     */
    protected String version;
    protected DriverAdapter adapter;
    protected MongoClient client;
    protected MongoDatabase database;
    protected AnylineDao dao;

    public AnylineDao getDao() {
        return dao;
    }

    public void setDao(AnylineDao dao) {
        this.dao = dao;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }


    public void setVersion(String version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getClient() {
        return client;
    }

    public void setClient(Object client) {
        this.client = (MongoClient) client;
    }


    public DriverAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            if("mongo".equals(ds)){
                ds = ClientHolder.curDataSource();
            }
            adapter = DriverAdapterHolder.getAdapter(ds, this);
        }
        return adapter;
    }
    public String datasource(){
        String ds = key;
        if("mongo".equals(ds)){
            ds = ClientHolder.curDataSource();
        }
        return ds;
    }
    public void setAdapter(DriverAdapter adapter) {
        this.adapter = adapter;
    }

    public MongoRuntime(String key, MongoClient client, MongoDatabase database, DriverAdapter adapter){
        setKey(key);
        setClient(database);
        setAdapter(adapter);
    }
    public MongoRuntime(){
    }

    public MongoClient client(){
        return client;
    }
    public String getFeature() {
        if(null == feature){
            if(null != client){
                feature = client.getClass().getName();
            }
        }
        return feature;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public String getVersion() {
        return version;
    }
}
