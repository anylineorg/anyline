package org.anyline.data.jdbc.neo4j.entity;

import org.anyline.entity.OriginalDataRow;

public class Neo4jRow extends OriginalDataRow {
    public Neo4jRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}