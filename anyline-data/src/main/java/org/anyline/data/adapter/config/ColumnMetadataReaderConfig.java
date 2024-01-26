package org.anyline.data.adapter.config;

import org.anyline.util.BasicUtil;

public class ColumnMetadataReaderConfig extends MetadataReaderConfig<ColumnMetadataReaderConfig>{
    protected String[] typeRefer;
    protected String[] lengthRefer;
    protected String[] precisionRefer;
    protected String[] scaleRefer;
    protected String[] positionRefer;

    public String[] getLengthRefers() {
        return lengthRefer;
    }
    public String getLengthRefer(){
        if(null != lengthRefer && lengthRefer.length > 0){
            return lengthRefer[0];
        }
        return null;
    }

    public ColumnMetadataReaderConfig setLengthRefer(String[] lengthRefer) {
        this.lengthRefer = lengthRefer;
        return this;
    }

    public ColumnMetadataReaderConfig setLength(String length) {
        if(BasicUtil.isNotEmpty(length)) {
            this.lengthRefer = length.split(",");
        } else {

            this.lengthRefer = null;
        }
        return this;
    }
    public String[] getPrecisionRefers() {
        return precisionRefer;
    }

    public String getPrecisionRefer(){
        if(null != precisionRefer && precisionRefer.length > 0){
            return precisionRefer[0];
        }
        return null;
    }
    public ColumnMetadataReaderConfig setPrecisionRefer(String[] precisionRefer) {
        this.precisionRefer = precisionRefer;
        return this;
    }

    public ColumnMetadataReaderConfig setPrecision(String precision) {
        if(BasicUtil.isNotEmpty(precision)) {
            this.precisionRefer = precision.split(",");
        } else {

            this.precisionRefer = null;
        }
        return this;
    }
    public String[] getScaleRefers() {
        return scaleRefer;
    }

    public String getScaleRefer(){
        if(null != scaleRefer && scaleRefer.length > 0){
            return scaleRefer[0];
        }
        return null;
    }
    public ColumnMetadataReaderConfig setScaleRefer(String[] scaleRefer) {
        this.scaleRefer = scaleRefer;
        return this;
    }

    public ColumnMetadataReaderConfig setScaleRefer(String scaleRefer) {
        if(BasicUtil.isNotEmpty(scaleRefer)) {
            this.scaleRefer = scaleRefer.split(",");
        } else {

            this.scaleRefer = null;
        }
        return this;
    }
    public String[] getTypeRefers() {
        return typeRefer;
    }

    public ColumnMetadataReaderConfig setTypeRefer(String[] typeRefer) {
        this.typeRefer = typeRefer;
        return this;
    }
    public ColumnMetadataReaderConfig setTypeRefer(String type) {
        if(BasicUtil.isNotEmpty(type)) {
            this.typeRefer = type.split(",");
        } else {

            this.typeRefer = null;
        }
        return this;
    }
    public String[] getPositionRefers() {
        return positionRefer;
    }

    public ColumnMetadataReaderConfig setPositionRefer(String[] positionRefer) {
        this.positionRefer = positionRefer;
        return this;
    }
    public ColumnMetadataReaderConfig setPositionRefer(String positionRefer) {
        if(BasicUtil.isNotEmpty(positionRefer)) {
            this.positionRefer = positionRefer.split(",");
        } else {

            this.positionRefer = null;
        }
        return this;
    }
}
