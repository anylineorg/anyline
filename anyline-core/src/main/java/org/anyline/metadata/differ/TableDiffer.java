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
    private ColumnsDiffer columnsDiffer;
    private IndexsDiffer indexsDiffer;
    //部分数据库的触发器 关联不到表
    private TriggersDiffer triggersDiffer;
    public boolean isEmpty(){
        if(null != columnsDiffer && !columnsDiffer.isEmpty()){
            return false;
        }
        if(null != indexsDiffer && !indexsDiffer.isEmpty()){
            return false;
        }
        return true;
    }

    public static TableDiffer compare(Table origin, Table dest){
        TableDiffer differ = new TableDiffer();
        if(null == dest){
            dest = new Table();
        }

        LinkedHashMap<String, Column> originColumns = origin.getColumns();
        LinkedHashMap<String, Column> destColumns = dest.getColumns();

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
