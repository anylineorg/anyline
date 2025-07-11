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

package org.anyline.metadata;

import java.io.Serializable;
import java.util.LinkedHashMap;
public class ForeignKey extends Constraint<ForeignKey> implements Serializable {
    protected String keyword = "FOREIGNKEY"           ;
    public boolean isForeign() {
        return true;
    }

    protected Table reference;
    protected ForeignKey update;
    protected String onUpdate;
    protected String onDelete;

    public ForeignKey() {}
    public ForeignKey(String name) {
        this.setName(name);
    }

    /**
     * 外键
     * @param table 表
     * @param column 列
     * @param rtable 依赖表
     * @param rcolumn 依赖列
     */
    public ForeignKey(String table, String column, String rtable, String rcolumn) {
        setTable(table);
        setReference(rtable);
        addColumn(column, rcolumn);
    }

    public ForeignKey addColumn(String column, String reference) {

        addColumn(new Column(column).setReference(new Column(reference)));
        return this;
    }
    public ForeignKey setReference(Table reference) {
        if(setmap && null != update) {
            update.setReference(reference);
            return this;
        }
        this.reference = reference;
        return this;
    }

    /**
     * 添加依赖表
     * @param reference 依赖表
     * @return ForeignKey
     */
    public ForeignKey setReference(String reference) {
        if(setmap && null != update) {
            update.setReference(reference);
            return this;
        }
        this.reference = new Table(reference);
        return this;
    }

    public Table getReference() {
        if(getmap && null != update) {
            return update.reference;
        }
        return reference;
    }

    /**
     * 添加列
     * @param column 列 需要设置reference属性
     * @return ForeignKey
     */
    public ForeignKey addColumn(Column column) {
        if(setmap && null != update) {
            update.addColumn(column);
            return this;
        }
        super.addColumn(column);
        return this;
    }

    /**
     * 添加列
     * @param column 列
     * @param table 依赖表
     * @param reference 依赖列
     * @return ForeignKey
     */
    public ForeignKey addColumn(String column, String table, Column reference) {
        if(setmap && null != update) {
            update.addColumn(column, table, reference);
            return this;
        }
        this.reference = new Table(table);
        addColumn(new Column(column).setReference(reference));
        return this;
    }

    /**
     * 添加列
     * @param column 列
     * @param reference 依赖列
     * @return ForeignKey
     */
    public ForeignKey addColumn(String column, Column reference) {
        if(setmap && null != update) {
            update.addColumn(column, reference);
            return this;
        }
        addColumn(new Column(column).setReference(reference));
        return this;
    }

    public String keyword() {
        return this.keyword;
    }
    public ForeignKey clone() {
        ForeignKey copy = super.clone();
        copy.reference = this.reference.clone();
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()) {
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        return copy;
    }

    public String onUpdate() {
        return onUpdate;
    }

    public void onUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    public String onDelete() {
        return onDelete;
    }

    public void onDelete(String onDelete) {
        this.onDelete = onDelete;
    }

    /* ********************************* field refer ********************************** */
    public static final String FIELD_KEYWORD                            = "KEYWORD";
    public static final String FIELD_REFERENCE_CATALOG                  = "REFERENCE_CATALOG";
    public static final String FIELD_REFERENCE_SCHEMA                   = "REFERENCE_SCHEMA";
    public static final String FIELD_REFERENCE_TABLE                    = "REFERENCE_TABLE";
    public static final String FIELD_REFERENCE_COLUMN                   = "REFERENCE_COLUMN";
    public static final String FIELD_COLUMN                             = "COLUMN";
    public static final String FIELD_COLUMN_POSITION                    = "COLUMN_POSITION";
    public static final String FIELD_COLUMN_ON_UPDATE                   = "ON_UPDATE";
    public static final String FIELD_COLUMN_ON_DELETE                   = "ON_DELETE";
}