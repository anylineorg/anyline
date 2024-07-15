package org.anyline.data.influxdb.metadata;

import org.anyline.metadata.Database;

public class InfluxBucket extends Database {
    public InfluxBucket(){
        super();
    }
    public InfluxBucket(String name){
        super(name);
    }
}
