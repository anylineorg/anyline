package org.anyline.data.jdbc.nebula.entity;

import org.anyline.entity.OriginRow;

public class NebulaDataRow  extends OriginRow {
    public NebulaDataRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}