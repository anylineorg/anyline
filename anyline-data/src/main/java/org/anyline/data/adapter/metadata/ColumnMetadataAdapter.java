package org.anyline.data.adapter.metadata;

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
    /*
     * 以下三个属性以及是否忽略 以数据类型上设置的为准
     */
    /**
     * 长度
     */
    protected String[] lengthRefers;
    /**
     * 有效位位
     */
    protected String[] precisionRefers;
    /**
     * 小数位数
     */
    protected String[] scaleRefers;


    /**
     * 是否忽略长度，创建和比较时忽略，但元数据中可能会有对应的列也有值
     * -1:未设置可以继承上级 0:不忽略 1:忽略 2:根据情况(是否提供)
     */
    protected int ignoreLength = -1;
    protected int ignorePrecision = -1;
    protected int ignoreScale = -1;

    protected String[] nullableRefers;
    protected String[] charsetRefers;
    protected String[] collateRefers;

    public String[] getCollateRefers() {
        return collateRefers;
    }
    public String[] getCharsetRefers() {
        return charsetRefers;
    }
    public String[] getNullableRefers() {
        return nullableRefers;
    }
    public String[] getLengthRefers() {
        return lengthRefers;
    }
    public String getLengthRefer(){
        if(null != lengthRefers && lengthRefers.length > 0){
            return lengthRefers[0];
        }
        return null;
    }

    public ColumnMetadataAdapter setLengthRefers(String[] lengthRefers) {
        this.lengthRefers = lengthRefers;
        return this;
    }

    public ColumnMetadataAdapter setLengthRefer(String lengthRefers) {
        if(BasicUtil.isNotEmpty(lengthRefers)) {
            this.lengthRefers = lengthRefers.split(",");
        } else {

            this.lengthRefers = null;
        }
        return this;
    }
    public String[] getPrecisionRefers() {
        return precisionRefers;
    }

    public String getPrecisionRefer(){
        if(null != precisionRefers && precisionRefers.length > 0){
            return precisionRefers[0];
        }
        return null;
    }
    public ColumnMetadataAdapter setPrecisionRefers(String[] precisionRefer) {
        this.precisionRefers = precisionRefer;
        return this;
    }
    public ColumnMetadataAdapter setPrecisionRefer(String precisionRefer) {
        if(BasicUtil.isNotEmpty(precisionRefer)) {
            this.precisionRefers = precisionRefer.split(",");
        } else {

            this.precisionRefers = null;
        }
        return this;
    }

    public String[] getScaleRefers() {
        return scaleRefers;
    }

    public String getScaleRefer(){
        if(null != scaleRefers && scaleRefers.length > 0){
            return scaleRefers[0];
        }
        return null;
    }
    public ColumnMetadataAdapter setScaleRefer(String[] scaleRefer) {
        this.scaleRefers = scaleRefer;
        return this;
    }

    public ColumnMetadataAdapter setScaleRefer(String scaleRefer) {
        if(BasicUtil.isNotEmpty(scaleRefer)) {
            this.scaleRefers = scaleRefer.split(",");
        } else {

            this.scaleRefers = null;
        }
        return this;
    }
    public String[] getTypeRefers() {
        return typeRefers;
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

    public int ignoreLength() {
        return ignoreLength;
    }

    public ColumnMetadataAdapter setIgnoreLength(int ignoreLength) {
        this.ignoreLength = ignoreLength;
        return this;
    }

    public int ignorePrecision() {
        return ignorePrecision;
    }

    public ColumnMetadataAdapter setIgnorePrecision(int ignorePrecision) {
        this.ignorePrecision = ignorePrecision;
        return this;
    }

    public int ignoreScale() {
        return ignoreScale;
    }

    public ColumnMetadataAdapter setIgnoreScale(int ignoreScale) {
        this.ignoreScale = ignoreScale;
        return this;
    }

}
