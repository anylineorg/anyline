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



package org.anyline.metadata.adapter;

import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;

/**
 * 读取Column元数据依据
 */
public class ColumnMetadataAdapter extends AbstractMetadataAdapter<ColumnMetadataAdapter> {
    /**
     * 数据类型
     */
    protected String[] dataTypeRefer;
    /**
     * 列顺序
     */
    protected String[] positionRefer;

    protected String[] nullableRefer;
    protected String[] charsetRefer;
    protected String[] collateRefer;

    protected TypeMetadata.Config typeConfig = null;

    public String[] getDataTypeRefers() {
        return dataTypeRefer;
    }

    public TypeMetadata.Config getTypeConfig() {
        return typeConfig;
    }

    public void setTypeConfig(TypeMetadata.Config typeConfig) {
        this.typeConfig = typeConfig;
    }

    public ColumnMetadataAdapter setDataTypeRefer(String[] typeRefer) {
        this.dataTypeRefer = typeRefer;
        return this;
    }
    public ColumnMetadataAdapter setDataTypeRefer(String type) {
        if(BasicUtil.isNotEmpty(type)) {
            this.dataTypeRefer = type.split(",");
        } else {

            this.dataTypeRefer = null;
        }
        return this;
    }
    public String[] getPositionRefers() {
        return positionRefer;
    }

    public ColumnMetadataAdapter setPositionRefer(String[] positionRefer) {
        this.positionRefer = positionRefer;
        return this;
    }
    public ColumnMetadataAdapter setPositionRefer(String positionRefer) {
        if(BasicUtil.isNotEmpty(positionRefer)) {
            this.positionRefer = positionRefer.split(",");
        } else {

            this.positionRefer = null;
        }
        return this;
    }

    public String[] getNullableRefers() {
        return nullableRefer;
    }
    public ColumnMetadataAdapter setNullableRefer(String[] nullableRefer) {
        this.nullableRefer = nullableRefer;
        return this;
    }
    public ColumnMetadataAdapter setNullableRefer(String nullableRefer) {
        if(BasicUtil.isNotEmpty(nullableRefer)) {
            this.nullableRefer = nullableRefer.split(",");
        } else {

            this.nullableRefer = null;
        }
        return this;
    }

    public String[] getCharsetRefers() {
        return charsetRefer;
    }
    public ColumnMetadataAdapter setCharsetRefer(String[] charsetRefer) {
        this.charsetRefer = charsetRefer;
        return this;
    }
    public ColumnMetadataAdapter setCharsetRefer(String charsetRefer) {
        if(BasicUtil.isNotEmpty(charsetRefer)) {
            this.charsetRefer = charsetRefer.split(",");
        } else {

            this.charsetRefer = null;
        }
        return this;
    }

    public String[] getCollateRefers() {
        return collateRefer;
    }
    public ColumnMetadataAdapter setCollateRefer(String[] collateRefers) {
        this.collateRefer = collateRefers;
        return this;
    }
    public ColumnMetadataAdapter setCollateRefer(String collateRefers) {
        if(BasicUtil.isNotEmpty(collateRefers)) {
            this.collateRefer = collateRefers.split(",");
        } else {

            this.collateRefer = null;
        }
        return this;
    }
}
