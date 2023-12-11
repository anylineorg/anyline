package org.anyline.data.elasticsearch.entity;

import org.anyline.entity.OriginalDataRow;

public class ElasticSearchDataRow extends OriginalDataRow {
    public ElasticSearchDataRow() {
        primaryKeys.add("_id");
        parseKeycase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
