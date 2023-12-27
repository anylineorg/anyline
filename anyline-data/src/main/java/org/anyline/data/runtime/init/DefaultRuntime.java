package org.anyline.data.runtime.init;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;

public class DefaultRuntime implements DataRuntime {

    /**
     * 复制来源
     */
    private String origin;
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
    protected RuntimeHolder holder;

    @Override
    public String origin() {
        return origin;
    }

    @Override
    public void origin(String origin) {
        this.origin = origin;
    }

    @Override
    public String getFeature(boolean connection) {
        return null;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Override
    public String getVersion() {
        return null;
    }


    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void setDriver(String driver) {

    }

    @Override
    public String getDriver() {
        return null;
    }

    @Override
    public void setUrl(String url) {

    }

    @Override
    public String getUrl() {
        return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Object getProcessor() {
        return null;
    }

    @Override
    public void setProcessor(Object processor) {

    }


    public DriverAdapter getAdapter() {
        if(null == adapter){
            String ds = key;
            adapter = DriverAdapterHolder.getAdapter(ds, this);
        }
        return adapter;
    }

    @Override
    public String datasource() {
        return key;
    }

    public void setAdapter(DriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void setHolder(RuntimeHolder holder) {
        this.holder = holder;
    }

    @Override
    public RuntimeHolder getHolder() {
        return holder;
    }


}
