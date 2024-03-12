package org.anyline.data.elasticsearch.entity;

import org.anyline.entity.OriginDataRow;

public class ElasticSearchDataRow extends OriginDataRow {
    public ElasticSearchDataRow() {
        primaryKeys.add("_id");
        parseKeycase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
