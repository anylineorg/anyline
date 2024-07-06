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

import org.anyline.util.BasicUtil;

public class IndexMetadataAdapter extends AbstractMetadataAdapter {
    /**
     * 排序
     */
    private String[] columnOrderRefer;
    /**
     * 索引类型BTREE等
     */
    private String[] typeRefer;
    /**
     * 在主键或索引中的顺序
     */
    private String[] columnPositionRefer;
    /**
     * 判断是否主键
     */
    private String[] checkPrimaryRefer;
    private String[] checkPrimaryValue;
    /**
     * 判断是否唯一索引
     */
    private String[] checkUniqueRefer;
    private String[] checkUniqueValue;

    public String[] getTypeRefers() {
        return typeRefer;
    }

    public IndexMetadataAdapter setTypeRefer(String[] typeRefer) {
        this.typeRefer = typeRefer;
        return this;
    }

    public IndexMetadataAdapter setTypeRefer(String typeRefer) {
        if(BasicUtil.isNotEmpty(typeRefer)) {
            this.typeRefer = typeRefer.split(",");
        }else{
            this.typeRefer = null;
        }
        return this;
    }
    public String[] getColumnOrderRefers() {
        return columnOrderRefer;
    }

    public IndexMetadataAdapter setColumnOrderRefer(String[] columnOrderRefer) {
        this.columnOrderRefer = columnOrderRefer;
        return this;
    }

    public IndexMetadataAdapter setColumnOrderRefer(String columnOrderRefer) {
        if(BasicUtil.isNotEmpty(columnOrderRefer)) {
            this.columnOrderRefer = columnOrderRefer.split(",");
        }else{
            this.columnOrderRefer = null;
        }
        return this;
    }

    public String[] getColumnPositionRefers() {
        return columnPositionRefer;
    }
    public String getColumnPositionRefer() {
        if(null != columnPositionRefer && columnPositionRefer.length > 0) {
            return columnPositionRefer[0];
        }
        return null;
    }

    public IndexMetadataAdapter setColumnPositionRefer(String[] columnPositionRefer) {
        this.columnPositionRefer = columnPositionRefer;
        return this;
    }
    public IndexMetadataAdapter setColumnPositionRefer(String columnPositionRefer) {
        if(BasicUtil.isNotEmpty(columnPositionRefer)) {
            this.columnPositionRefer = columnPositionRefer.split(",");
        }else{
            this.columnPositionRefer = null;
        }
        return this;
    }


    public String[] getCheckPrimaryRefers() {
        return checkPrimaryRefer;
    }
    public String getCheckPrimaryRefer() {
        if(null != checkPrimaryRefer && checkPrimaryRefer.length > 0) {
            return checkPrimaryRefer[0];
        }
        return null;
    }

    public IndexMetadataAdapter setCheckPrimaryRefer(String[] checkPrimaryRefer) {
        this.checkPrimaryRefer = checkPrimaryRefer;
        return this;
    }
    public IndexMetadataAdapter setCheckPrimaryRefer(String checkPrimaryRefer) {
        if(BasicUtil.isNotEmpty(checkPrimaryRefer)) {
            this.checkPrimaryRefer = checkPrimaryRefer.split(",");
        }else{
            this.checkPrimaryRefer = null;
        }
        return this;
    }


    public String[] getCheckPrimaryValues() {
        return checkPrimaryValue;
    }
    public String getCheckPrimaryValue() {
        if(null != checkPrimaryValue && checkPrimaryValue.length > 0) {
            return checkPrimaryValue[0];
        }
        return null;
    }

    public IndexMetadataAdapter setCheckPrimaryValue(String[] checkPrimaryValue) {
        this.checkPrimaryValue = checkPrimaryValue;
        return this;
    }
    public IndexMetadataAdapter setCheckPrimaryValue(String checkPrimaryValue) {
        if(BasicUtil.isNotEmpty(checkPrimaryValue)) {
            this.checkPrimaryValue = checkPrimaryValue.split(",");
        }else{
            this.checkPrimaryValue = null;
        }
        return this;
    }


    public String[] getCheckUniqueRefers() {
        return checkUniqueRefer;
    }
    public String getCheckUniqueRefer() {
        if(null != checkUniqueRefer && checkUniqueRefer.length > 0) {
            return checkUniqueRefer[0];
        }
        return null;
    }

    public IndexMetadataAdapter setCheckUniqueRefer(String[] checkUniqueRefer) {
        this.checkUniqueRefer = checkUniqueRefer;
        return this;
    }
    public IndexMetadataAdapter setCheckUniqueRefer(String checkUniqueRefer) {
        if(BasicUtil.isNotEmpty(checkUniqueRefer)) {
            this.checkUniqueRefer = checkUniqueRefer.split(",");
        }else{
            this.checkUniqueRefer = null;
        }
        return this;
    }


    public String[] getCheckUniqueValues() {
        return checkUniqueValue;
    }
    public String getCheckUniqueValue() {
        if(null != checkUniqueValue && checkUniqueValue.length > 0) {
            return checkUniqueValue[0];
        }
        return null;
    }

    public IndexMetadataAdapter setCheckUniqueValue(String[] checkUniqueValue) {
        this.checkUniqueValue = checkUniqueValue;
        return this;
    }
    public IndexMetadataAdapter setCheckUniqueValue(String checkUniqueValue) {
        if(BasicUtil.isNotEmpty(checkUniqueValue)) {
            this.checkUniqueValue = checkUniqueValue.split(",");
        }else{
            this.checkUniqueValue = null;
        }
        return this;
    }
}
