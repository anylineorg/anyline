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

import org.anyline.metadata.type.TypeMetadata;

import java.io.Serializable;

public class Parameter extends Metadata<Parameter> implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean input;
    private boolean output;
    protected Integer length                      ;
    protected Integer precision                   ; // 有效长度(包含小数部分)  123.45：precision = 5, scale = 2 对于SQL Server 中 varchar(max)设置成 -1
    protected Integer scale                       ; // 小数部分的长度
    protected DataTypeDefine dataType;
    private Object value;
    private TypeMetadata columnType;
    private Integer type;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setType(TypeMetadata type) {
        this.columnType = type;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public DataTypeDefine getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeDefine dataType) {
        this.dataType = dataType;
    }

    /* ********************************* field refer ********************************** */
    public static final String FIELD_INPUT                         = "INPUT";
    public static final String FIELD_INPUT_CHECK                   = "INPUT_CHECK";
    public static final String FIELD_INPUT_CHECK_VALUE             = "INPUT_CHECK_VALUE";
    public static final String FIELD_OUTPUT                        = "OUTPUT";
    public static final String FIELD_OUTPUT_CHECK                  = "OUTPUT_CHECK";
    public static final String FIELD_OUTPUT_CHECK_VALUE            = "OUTPUT_CHECK_VALUE";
    public static final String FIELD_LENGTH                        = "LENGTH";
    public static final String FIELD_PRECISION                     = "PRECISION";
    public static final String FIELD_SCALE                         = "SCALE";
    public static final String FIELD_VALUE                         = "VALUE";
    public static final String FIELD_COLUMN_TYPE                   = "COLUMN_TYPE";
    public static final String FIELD_TYPE                          = "TYPE";
    public static final String FIELD_DATA_TYPE                     = "DATA_TYPE";
    public static final String FIELD_OCTET_LENGTH                  = "OCTET_LENGTH";

    public static final String FIELD_MODE                          = "MODE";
}