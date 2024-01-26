package org.anyline.data.adapter.config;

import org.anyline.util.BasicUtil;

public class IndexMetadataReaderConfig extends MetadataReaderConfig{
    /**
     * 排序
     */
    private String[] columnOrderRefer;
    /**
     * 在主键或索引中的顺序
     */
    private String[] columnPositionRefer;

    public String[] getColumnOrderRefer() {
        return columnOrderRefer;
    }

    public IndexMetadataReaderConfig setColumnOrderRefer(String[] columnOrderRefer) {
        this.columnOrderRefer = columnOrderRefer;
        return this;
    }

    public IndexMetadataReaderConfig setColumnOrderRefer(String columnOrderRefer) {
        if(BasicUtil.isNotEmpty(columnOrderRefer)) {
            this.columnOrderRefer = columnOrderRefer.split(",");
        }else{
            this.columnOrderRefer = null;
        }
        return this;
    }
    public String[] getColumnPositionRefer() {
        return columnPositionRefer;
    }

    public IndexMetadataReaderConfig setColumnPositionRefer(String[] columnPositionRefer) {
        this.columnPositionRefer = columnPositionRefer;
        return this;
    }
    public IndexMetadataReaderConfig setColumnPositionRefer(String columnPositionRefer) {
        if(BasicUtil.isNotEmpty(columnPositionRefer)) {
            this.columnPositionRefer = columnPositionRefer.split(",");
        }else{
            this.columnPositionRefer = null;
        }
        return this;
    }
}
