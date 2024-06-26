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
     * 在主键或索引中的顺序
     */
    private String[] columnPositionRefer;

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
        if(null != columnOrderRefer || columnOrderRefer.length > 0) {
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
}
