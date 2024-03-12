package org.anyline.data.mongodb.entity;

import org.anyline.entity.OriginDataRow;

public class MongoDataRow extends OriginDataRow {
    public MongoDataRow() {
        primaryKeys.add("_id");
        parseKeycase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
