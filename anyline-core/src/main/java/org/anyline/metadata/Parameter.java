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



package org.anyline.metadata;

import org.anyline.metadata.type.TypeMetadata;

import java.io.Serializable;

public class Parameter extends Metadata<Parameter> implements Serializable {
    private boolean input;
    private boolean output;
    protected Integer length                      ;
    protected Integer precision                   ; // 有效长度(包含小数部分)  123.45：precision = 5, scale = 2 对于SQL Server 中 varchar(max)设置成 -1
    protected Integer scale                       ; // 小数部分的长度

    private Object value;
    private TypeMetadata columnType;
    private Integer type = java.sql.Types.VARCHAR;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(TypeMetadata type) {
        this.columnType = type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public boolean isOutput() {
        return output;
    }

    public void setOutput(boolean output) {
        this.output = output;
    }

    public TypeMetadata getColumnType() {
        return columnType;
    }

    public void setColumnType(TypeMetadata columnType) {
        this.columnType = columnType;
    }

    public Integer getLength() {
        return length;
    }
    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String toString(){
        return "{value:"+value+", type:"+type+"}";
    }

}
