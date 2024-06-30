/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.metadata.differ;

import org.anyline.metadata.Column;
import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

/**
 * 表或列之间的对比结果
 */
public class TableDiffer implements MetadataDiffer {
    private Table origin;
    private Table dest;
    private ColumnsDiffer columnsDiffer;
    private IndexesDiffer indexesDiffer;
    //部分数据库的触发器 关联不到表
    private TriggersDiffer triggersDiffer;
    public TableDiffer() {}
    public TableDiffer(Table origin, Table dest) {
        this.origin = origin;
        this.dest = dest;
    }
    public boolean isEmpty() {
        if(null != columnsDiffer && !columnsDiffer.isEmpty()) {
            return false;
        }
        if(null != indexesDiffer && !indexesDiffer.isEmpty()) {
            return false;
        }
        return true;
    }

    public static TableDiffer compare(Table origin, Table dest) {
        if(null == dest) {
            dest = new Table();
        }

        TableDiffer differ = new TableDiffer(origin, dest);
        LinkedHashMap<String, Column> originColumns = origin.getColumns();
        LinkedHashMap<String, Column> destColumns = dest.getColumns();

        differ.setColumnsDiffer(ColumnsDiffer.compare(originColumns, destColumns));

        differ.setIndexesDiffer(IndexesDiffer.compare(origin.getIndexes(), dest.getIndexes()));
        return differ;
    }

    public ColumnsDiffer getColumnsDiffer() {
        return columnsDiffer;
    }

    public void setColumnsDiffer(ColumnsDiffer columnsDiffer) {
        this.columnsDiffer = columnsDiffer;
    }

    public IndexesDiffer getIndexesDiffer() {
        return indexesDiffer;
    }

    public void setIndexesDiffer(IndexesDiffer indexesDiffer) {
        this.indexesDiffer = indexesDiffer;
    }

    public TriggersDiffer getTriggersDiffer() {
        return triggersDiffer;
    }

    public void setTriggersDiffer(TriggersDiffer triggersDiffer) {
        this.triggersDiffer = triggersDiffer;
    }

    public Table getOrigin() {
        return origin;
    }

    public void setOrigin(Table origin) {
        this.origin = origin;
    }

    public Table getDest() {
        return dest;
    }

    public void setDest(Table dest) {
        this.dest = dest;
    }
}
