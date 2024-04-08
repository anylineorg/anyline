package org.anyline.data.elasticsearch.entity;

import org.anyline.entity.OriginRow;

public class ElasticSearchDataRow extends OriginRow {
    public ElasticSearchDataRow() {
        primaryKeys.add("_id");
        parseKeycase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
