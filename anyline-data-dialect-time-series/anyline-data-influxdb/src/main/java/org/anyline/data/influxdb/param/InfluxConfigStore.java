package org.anyline.data.influxdb.param;

import org.anyline.data.param.init.DefaultConfigStore;

public class InfluxConfigStore extends DefaultConfigStore {
    private String bucket;
    private String org;
    private String start;
    private String stop;

    public InfluxConfigStore(){
        super();
    }
    public InfluxConfigStore start(String start){
        this.start = start;
        return this;
    }
    public String start(){
        return start;
    }
    public InfluxConfigStore stop(String stop){
        this.stop = stop;
        return this;
    }
    public String stop(){
        return stop;
    }
    public InfluxConfigStore range(String start, String stop){
        this.start = start;
        this.stop = stop;
        return this;
    }
    public InfluxConfigStore org(String org){
        this.org = org;
        return this;
    }
    public String org(){
        return org;
    }
    public InfluxConfigStore bucket(String bucket){
        this.bucket = bucket;
        return this;
    }
    public String bucket(){
        return bucket;
    }
}
