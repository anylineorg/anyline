package org.anyline.data.jdbc.neo4j.entity;

import org.anyline.entity.OriginRow;
import org.anyline.entity.graph.GraphRow;

public class Neo4jRow extends GraphRow {
    public Neo4jRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}