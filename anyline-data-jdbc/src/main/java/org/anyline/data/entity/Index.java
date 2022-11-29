package org.anyline.data.entity;

public class Index extends Constraint{
    private boolean primary     ; // 是否是主键
    private boolean cluster     ; // 是否聚簇索引

    public boolean isCluster() {
        return cluster;
    }

    public Index setCluster(boolean cluster) {
        this.cluster = cluster;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Index setPrimary(boolean primary) {
        this.primary = primary;
        if(primary){
            setCluster(true);
            setUnique(true);
        }
        return this;
    }
}