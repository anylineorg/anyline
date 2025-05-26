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

package org.anyline.data.milvus.metadata;

import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;

public class MilvusSchema extends Metadata<MilvusSchema> {
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();

    public Column primary() {
        for(Column column:columns.values()) {
            if(column.isPrimaryKey()) {
                return column;
            }
        }
        return null;
    }
    public LinkedHashMap<String, Column> getColumns(){
        return this.columns;
    }
    public MilvusSchema addColumn(LinkedHashMap<String, Column> columns) {
        this.columns.putAll(columns);
        return this;
    }
    public MilvusSchema addColumn(Column column) {
        if(setmap && null != update) {
            update.addColumn(column);
            return this;
        }
        if (null == columns) {
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);

        return this;
    }
    public Column addColumn(String name, String type, int precision, int scale) {
        Column column = new Column(name, type, precision, scale);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, String type, int precision) {
        Column column = new Column(name, type, precision);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, String type) {
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, String type, String comment) {
        return addColumn(name, type, true, null).setComment(comment);
    }
    public Column addColumn(String name, TypeMetadata type) {
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def) {
        Column column = new Column();
        column.setName(name);
        column.nullable(nullable);
        column.setDefaultValue(def);
        column.setTypeName(type);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, TypeMetadata type, boolean nullable, Object def) {
        Column column = new Column();
        column.setName(name);
        column.nullable(nullable);
        column.setDefaultValue(def);
        column.setTypeMetadata(type);
        addColumn(column);
        return column;
    }
    public Column getColumn(String name) {
        if(getmap && null != update) {
            return update.getColumn(name);
        }
        if(null == columns || null == name) {
            return null;
        }
        return columns.get(name.toUpperCase());
    }
}
