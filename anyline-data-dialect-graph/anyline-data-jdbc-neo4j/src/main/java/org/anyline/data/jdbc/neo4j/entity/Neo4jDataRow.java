package org.anyline.data.jdbc.neo4j.entity;

import org.anyline.entity.OriginalDataRow;

public class Neo4jDataRow  extends OriginalDataRow {
    public Neo4jDataRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}