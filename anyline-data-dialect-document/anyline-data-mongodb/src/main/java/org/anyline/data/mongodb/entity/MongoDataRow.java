package org.anyline.data.mongodb.entity;

import org.anyline.entity.OriginRow;

public class MongoDataRow extends OriginRow {
    public MongoDataRow() {
        primaryKeys.add("_id");
        parseKeyCase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
