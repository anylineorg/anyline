package org.anyline.data.entity;

public class Index extends Constraint{

    private boolean cluster     ; // 是否聚簇索引

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }
}