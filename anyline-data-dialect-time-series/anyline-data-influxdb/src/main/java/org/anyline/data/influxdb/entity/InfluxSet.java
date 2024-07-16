package org.anyline.data.influxdb.entity;

import org.anyline.entity.DataSet;

public class InfluxSet extends DataSet {
    private String measurement;
    private String time;
    private String time_format;
    private String time_precision;
    private String time_zone;
    private String group_by;
    private String order_by;
    private String limit;
    private String offset;
    private String fill;
    private String fill_value;
    private String fill_time;
    private String fill_time_value;
    private String fill_time_format;
    private String fill_time_precision;
    private String fill_time_zone;


    protected String bucket;
    protected String org;

    public InfluxSet bucket(String bucket){
        this.bucket = bucket;
        return this;
    }
    public String bucket(){
        return this.bucket;
    }
    public InfluxSet org(String org){
        this.org = org;
        return this;
    }
    public String org(){
        return this.org;
    }
}
