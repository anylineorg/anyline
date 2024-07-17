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

package org.anyline.data.influxdb.run;

import com.influxdb.client.write.Point;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION;
import org.anyline.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class InfluxRun extends SimpleRun {
    protected String bucket;
    protected String org;
    protected String measurement;
    protected List<Point> points;
    protected String api;


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

    public InfluxRun api(String api){
        this.api = api;
        return this;
    }
    public String api(){
        return api;
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

    public String log(ACTION.DML action, boolean placeholder) {
        StringBuilder builder = new StringBuilder();
        List<String> keys = null;
        builder.append("[org:").append(org).append("][bucket:").append(bucket).append("]");
        String cmd = null;
        if(action == ACTION.DML.SELECT) {
            cmd = getFinalQuery(placeholder);
        }else if(action == ACTION.DML.COUNT) {
            cmd = getTotalQuery(placeholder);
        }else if(action == ACTION.DML.UPDATE) {
            keys = getUpdateColumns();
            cmd = getFinalUpdate(placeholder);
        }else if(action == ACTION.DML.INSERT) {
            keys = getInsertColumns();
            cmd = getFinalInsert(placeholder);
        }else if(action == ACTION.DML.EXECUTE) {
            cmd = getFinalExecute(placeholder);
        }else if(action == ACTION.DML.DELETE) {
            cmd = getFinalDelete(placeholder);
        }else if(action == ACTION.DML.EXISTS) {
            cmd = getFinalExists(placeholder);
        }
        if(null != cmd && !cmd.isEmpty()) {
            builder.append("[cmd:\n").append(cmd);
            builder.append("\n]");
        }
        if(placeholder) {
            List<Object> values = getValues();
            if(null!= values && !values.isEmpty()) {
                builder.append("\n[param:");
                builder.append(LogUtil.param(keys, getValues()));
                builder.append("];");
            }
        }
        return builder.toString();
    }
}
