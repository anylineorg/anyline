package org.anyline.entity.data;

public class Index extends Constraint {
    private boolean primary     ; // 是否是主键
    private boolean cluster     ; // 是否聚簇索引
    private boolean fulltext    ;
    private boolean spatial     ;

    public boolean isCluster() {
        return cluster;
    }

    public Index setCluster(boolean cluster) {
        this.cluster = cluster;
        return this;
    }

    public boolean isFulltext() {
        return fulltext;
    }

    public void setFulltext(boolean fulltext) {
        this.fulltext = fulltext;
    }

    public boolean isSpatial() {
        return spatial;
    }

    public void setSpatial(boolean spatial) {
        this.spatial = spatial;
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