package org.anyline.data.jdbc.nebula.entity;

import org.anyline.entity.OriginDataRow;

public class NebulaDataRow  extends OriginDataRow {
    public NebulaDataRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}