package org.anyline.data.jdbc.nebula.entity;

import org.anyline.entity.OriginalDataRow;

public class NebulaDataRow  extends OriginalDataRow {
    public NebulaDataRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
}