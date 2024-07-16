package org.anyline.data.influxdb.run;

import com.influxdb.client.write.Point;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;

import java.util.ArrayList;
import java.util.List;

public class InfluxRun extends SimpleRun {
    protected String bucket;
    protected String org;
    protected String measurement;
    protected List<Point> points;

    @Override
    public boolean isEmpty(){
        if(null != points && !points.isEmpty()){
            return false;
        }
        return true;
    }
    public InfluxRun points(List<Point> points){
        this.points = points;
        return this;
    }
    public InfluxRun add(Point point){
        if(null == points){
            this.points = new ArrayList<>();
        }
        points.add(point);
        return this;
    }

    public List<Point> points(){
        return points;
    }
    public String measurement(){
        return this.measurement;
    }

    public String org(){
        return this.org;
    }

    public String bucket(){
        return this.bucket;
    }

    public InfluxRun bucket(String bucket){
        this.bucket = bucket;
        return this;
    }
    public InfluxRun measurement(String measurement){
        this.measurement = measurement;
        return this;
    }
    public InfluxRun org(String org){
        this.org = org;
        return this;
    }
    public InfluxRun table(String measurement){
        this.measurement = measurement;
        return this;
    }
    public InfluxRun(DataRuntime runtime) {
        super(runtime);
    }

    public InfluxRun(DataRuntime runtime, StringBuilder builder) {
        super(runtime, builder);
    }

    public InfluxRun(DataRuntime runtime, String sql) {
        super(runtime, sql);
    }
}
