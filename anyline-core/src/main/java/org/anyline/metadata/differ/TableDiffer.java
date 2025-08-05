/*
 * Copyright 2006-2025 www.anyline.org
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

import org.anyline.metadata.Table;

import java.io.Serializable;

/**
 * 表或列之间的对比结果
 */
public class TableDiffer extends AbstractDiffer implements Serializable {
    private static final long serialVersionUID = 1L;
    private Table origin;
    private Table dest;
    private ColumnsDiffer columnsDiffer;
    private PrimaryKeyDiffer primaryKeyDiffer;
    private IndexesDiffer indexesDiffer;
    //部分数据库的触发器 关联不到表
    private TriggersDiffer triggersDiffer;
    public TableDiffer() {}
    public TableDiffer(Table origin, Table dest) {
        this.origin = origin;
        this.dest = dest;
    }

    @Override
    public MetadataDiffer setDirect(DIRECT direct) {
        if(direct == DIRECT.ORIGIN) {
            this.direct = origin;
        }else if(direct == DIRECT.DEST) {
            this.direct = dest;
        }
        if(null != columnsDiffer) {
            columnsDiffer.setDirect(this.direct);
        }
        return this;
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

    public static TableDiffer compare(Table origin, Table dest, Table direct) {
        if(null == dest) {
            dest = new Table();
        }
        if(null == direct) {
            if(null != origin) {
                direct = origin;
            }else{
                direct = dest;
            }
        }
        TableDiffer differ = new TableDiffer(origin, dest);
        //列
        differ.setColumnsDiffer(ColumnsDiffer.compare(origin.getColumns(), dest.getColumns(), direct));
        //主键
        differ.setPrimaryKeyDiffer(PrimaryKeyDiffer.compare(origin.getPrimaryKey(), dest.getPrimaryKey(), direct));
        //索引
        differ.setIndexesDiffer(IndexesDiffer.compare(origin.getIndexes(), dest.getIndexes(), direct));
        //其他属性

        differ.setDirect(direct);
        return differ;
    }

    public ColumnsDiffer getColumnsDiffer() {
        return columnsDiffer;
    }

    public void setColumnsDiffer(ColumnsDiffer columnsDiffer) {
        this.columnsDiffer = columnsDiffer;
    }

    public PrimaryKeyDiffer getPrimaryKeyDiffer() {
        return primaryKeyDiffer;
    }

    public void setPrimaryKeyDiffer(PrimaryKeyDiffer primaryKeyDiffer) {
        this.primaryKeyDiffer = primaryKeyDiffer;
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
