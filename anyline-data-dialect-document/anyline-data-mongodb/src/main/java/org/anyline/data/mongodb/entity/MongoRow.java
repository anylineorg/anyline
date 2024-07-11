package org.anyline.data.mongodb.entity;

import org.anyline.entity.OriginRow;

public class MongoRow extends OriginRow {
    public MongoRow(){
        primaryKeys.clear();
        primaryKeys.add("_id");
        parseKeyCase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}
