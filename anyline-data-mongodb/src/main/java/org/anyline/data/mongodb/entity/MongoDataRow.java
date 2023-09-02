package org.anyline.data.mongodb.entity;

import org.anyline.entity.OriginalDataRow;

public class MongoDataRow extends OriginalDataRow {
    public MongoDataRow() {
        primaryKeys.add("_id");
        parseKeycase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
