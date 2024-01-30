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
    protected String[] typeRefers;
    /**
     * 列顺序
     */
    protected String[] positionRefers;


    protected String[] nullableRefers;
    protected String[] charsetRefers;
    protected String[] collateRefers;

    protected TypeMetadata.Config typeConfig = null;

    public String[] getCollateRefers() {
        return collateRefers;
    }
    public String[] getCharsetRefers() {
        return charsetRefers;
    }
    public String[] getNullableRefers() {
        return nullableRefers;
    }

    public String[] getTypeRefers() {
        return typeRefers;
    }

    public TypeMetadata.Config getTypeConfig() {
        return typeConfig;
    }

    public void setTypeConfig(TypeMetadata.Config typeConfig) {
        this.typeConfig = typeConfig;
    }

    public ColumnMetadataAdapter setTypeRefers(String[] typeRefer) {
        this.typeRefers = typeRefer;
        return this;
    }
    public ColumnMetadataAdapter setTypeRefer(String type) {
        if(BasicUtil.isNotEmpty(type)) {
            this.typeRefers = type.split(",");
        } else {

            this.typeRefers = null;
        }
        return this;
    }
    public String[] getPositionRefers() {
        return positionRefers;
    }

    public ColumnMetadataAdapter setPositionRefer(String[] positionRefer) {
        this.positionRefers = positionRefer;
        return this;
    }
    public ColumnMetadataAdapter setPositionRefer(String positionRefer) {
        if(BasicUtil.isNotEmpty(positionRefer)) {
            this.positionRefers = positionRefer.split(",");
        } else {

            this.positionRefers = null;
        }
        return this;
    }


}
