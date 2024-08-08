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



package org.anyline.data.run;

import org.anyline.metadata.Column;
import org.anyline.metadata.type.TypeMetadata;

public class RunValue {
    private String key;
    private Object value;
    private Column column;
    private String valueClass;
    private TypeMetadata valueType;

    private boolean placeholder = true;

    public RunValue() {}
    public RunValue(Column column, Object value) {
        if(null == column) {
            this.key = "none";
        }else{
            this.key = column.getName();
        }
        this.column = column;
        this.value = value;
    }
    public RunValue(String key, Object value, String valueClass) {
        this.key = key;
        this.value = value;
        this.valueClass = valueClass;
    }
    public RunValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public String getValueClass() {
        return valueClass;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
    }

    public TypeMetadata getValueType() {
        return valueType;
    }

    public void setValueType(TypeMetadata valueType) {
        this.valueType = valueType;
    }
}
