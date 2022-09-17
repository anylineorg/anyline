package org.anyline.jdbc.entity;

import org.anyline.jdbc.config.db.SQLAdapter;
import org.anyline.listener.DDListener;
import org.anyline.service.AnylineService;

import java.util.LinkedHashMap;

public class Index extends Constraint{

    private boolean cluster     ; //是否聚簇索引

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }
}