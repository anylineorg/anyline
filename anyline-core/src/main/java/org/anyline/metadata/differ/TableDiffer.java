package org.anyline.metadata.differ;

import org.anyline.metadata.Table;

/**
 * 表或列之间的对比结果
 */
public class TableDiffer implements MetadataDiffer {
    private ColumnsDiffer columnsDiffer;
    private IndexsDiffer indexsDiffer;
    private TriggersDiffer triggersDiffer;

    public static TableDiffer compare(Table origin, Table dest){
        TableDiffer differ = new TableDiffer();
        if(null == dest){
            dest = new Table();
        }

        differ.setColumnsDiffer(ColumnsDiffer.compare(origin.getColumns(), dest.getColumns()));

        differ.setIndexsDiffer(IndexsDiffer.compare(origin.getIndexes(), dest.getIndexes()));
        
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

    public TriggersDiffer getTriggersDiffer() {
        return triggersDiffer;
    }

    public void setTriggersDiffer(TriggersDiffer triggersDiffer) {
        this.triggersDiffer = triggersDiffer;
    }
}
