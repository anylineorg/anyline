package org.anyline.data.jdbc.neo4j.entity;

import org.anyline.entity.OriginDataRow;

public class Neo4jRow extends OriginDataRow {
    public Neo4jRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}