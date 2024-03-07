package org.anyline.metadata;

import javafx.scene.control.Tab;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class TableDiffer {
    private ColumnsDiffer columnsDiffer;
    private IndexsDiffer indexsDiffer;
    public static TableDiffer compare(Table origin, Table dest){
        TableDiffer differ = new TableDiffer();
        if(null == dest){
            dest = new Table();
        }
        LinkedHashMap<String, Column> origins = origin.getColumns();
        LinkedHashMap<String, Column> dests = dest.getColumns();
        differ.setColumnsDiffer(ColumnsDiffer.compare(origins, dests));
        return differ;
    }

    public ColumnsDiffer getColumnsDiffer() {
        return columnsDiffer;
    }

    public void setColumnsDiffer(ColumnsDiffer columnsDiffer) {
        this.columnsDiffer = columnsDiffer;
    }

    public IndexsDiffer getIndexsDiffer() {
        return indexsDiffer;
    }

    public void setIndexsDiffer(IndexsDiffer indexsDiffer) {
        this.indexsDiffer = indexsDiffer;
    }
}
