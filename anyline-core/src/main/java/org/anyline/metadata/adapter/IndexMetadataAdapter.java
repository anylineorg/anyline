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
