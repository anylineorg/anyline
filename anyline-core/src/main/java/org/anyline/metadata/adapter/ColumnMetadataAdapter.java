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

    public String[] getNullableRefers() {
        return nullableRefers;
    }
    public ColumnMetadataAdapter setNullableRefers(String[] nullableRefers) {
        this.nullableRefers = nullableRefers;
        return this;
    }
    public ColumnMetadataAdapter setNullableRefers(String nullableRefers) {
        if(BasicUtil.isNotEmpty(nullableRefers)) {
            this.nullableRefers = nullableRefers.split(",");
        } else {

            this.nullableRefers = null;
        }
        return this;
    }

    public String[] getCharsetRefers() {
        return charsetRefers;
    }
    public ColumnMetadataAdapter setCharsetRefers(String[] charsetRefers) {
        this.charsetRefers = charsetRefers;
        return this;
    }
    public ColumnMetadataAdapter setCharsetRefers(String charsetRefers) {
        if(BasicUtil.isNotEmpty(charsetRefers)) {
            this.charsetRefers = charsetRefers.split(",");
        } else {

            this.charsetRefers = null;
        }
        return this;
    }

    public String[] getCollateRefers() {
        return collateRefers;
    }
    public ColumnMetadataAdapter setCollateRefers(String[] collateRefers) {
        this.collateRefers = collateRefers;
        return this;
    }
    public ColumnMetadataAdapter setCollateRefers(String collateRefers) {
        if(BasicUtil.isNotEmpty(collateRefers)) {
            this.collateRefers = collateRefers.split(",");
        } else {

            this.collateRefers = null;
        }
        return this;
    }
}
