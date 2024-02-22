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


package org.anyline.metadata.type;


import org.anyline.util.BasicUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public interface TypeMetadata {
    enum CATEGORY_GROUP{STRING, NUMBER, BOOLEAN, BYTES, DATETIME, COLLECTION, GEOMETRY, OTHER, NONE}
    //要用来区分 length/precision
    //BLOB不需要长度 BYTES需要长度
    //TIMESTAMP在有些数据库中支持SCALE需要在单独的alias中设置如TIMESTAMP(6)
    enum CATEGORY{
        CHAR(CATEGORY_GROUP.STRING, 0, 1, 1),
        TEXT(CATEGORY_GROUP.STRING, 1, 1, 1),
        BOOLEAN(CATEGORY_GROUP.BOOLEAN, 1, 1, 1),
        BYTES(CATEGORY_GROUP.BYTES, 0, 1, 1),
        BLOB(CATEGORY_GROUP.BYTES, 1, 1, 1),
        INT(CATEGORY_GROUP.NUMBER, 0, 1, 1),
        FLOAT(CATEGORY_GROUP.NUMBER, 1, 0, 0),
        DATE(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        TIME(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        DATETIME(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        TIMESTAMP(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        COLLECTION(CATEGORY_GROUP.COLLECTION, 1, 1, 1),
        GEOMETRY(CATEGORY_GROUP.GEOMETRY, 1, 1, 1),
        OTHER(CATEGORY_GROUP.OTHER, 1, 1, 1),
        NONE(CATEGORY_GROUP.NONE, 1, 1, 1);
        private final CATEGORY_GROUP group;
        private final int ignoreLength;
        private final int ignorePrecision;

        private final int ignoreScale;
        private TypeMetadata.Config config;
        CATEGORY(CATEGORY_GROUP group, int ignoreLength, int ignorePrecision, int ignoreScale) {
            this.group = group;
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public CATEGORY_GROUP group(){
            return group;
        }

        public TypeMetadata.Config config() {
            if(null == config){
                config = new TypeMetadata.Config();
                config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
            }
            return config;
        }
    }
    default boolean equals(TypeMetadata metadata){
        if(null == metadata){
            return false;
        }
        if(this.getOrigin() == metadata){
            return true;
        }
        if(this == metadata){
            return true;
        }
        if(this == metadata.getOrigin()){
            return true;
        }
        if(this.getOrigin() == metadata.getOrigin()){
            return true;
        }
        return false;
    }

    TypeMetadata ILLEGAL = new TypeMetadata() {

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.NONE;
        }

        @Override
        public CATEGORY_GROUP getCategoryGroup() {
            return CATEGORY_GROUP.NONE;
        }

        @Override
        public String getName() {
            return "ILLEGAL";
        }

        @Override
        public int ignoreLength() {
            return -1;
        }
        @Override
        public int ignorePrecision() {
            return -1;
        }

        @Override
        public int ignoreScale() {
            return -1;
        }

        @Override
        public boolean support() {
            return false;
        }

        @Override
        public void setArray(boolean array) {

        }

        @Override
        public Class compatible() {
            return null;
        }

        @Override
        public Class transfer() {
            return null;
        }

        @Override
        public List<DatabaseType> databaseTypes() {
            return new ArrayList<>();
        }

        @Override
        public Object convert(Object value, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz, boolean array) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean array, boolean placeholder) {
            return null;
        }
        @Override
        public Config config() {
            return new TypeMetadata.Config();
        }
    };
    //不识别的类型 原样输出
    TypeMetadata NONE = new TypeMetadata() {

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.NONE;
        }
        @Override
        public String getName() {
            return "NONE";
        }

        @Override
        public int ignoreLength() {
            return -1;
        }
        @Override
        public int ignorePrecision() {
            return -1;
        }

        @Override
        public int ignoreScale() {
            return -1;
        }

        @Override
        public boolean support() {
            return false;
        }

        @Override
        public void setArray(boolean array) {

        }

        @Override
        public Config config() {
            return new TypeMetadata.Config();
        }

        @Override
        public Class compatible() {
            return null;
        }

        @Override
        public Class transfer() {
            return null;
        }

        @Override
        public CATEGORY_GROUP getCategoryGroup() {
            return CATEGORY_GROUP.NONE;
        }

        @Override
        public List<DatabaseType> databaseTypes() {
            return new ArrayList<>();
        }

        @Override
        public Object convert(Object value, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz, boolean array) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean array, boolean placeholder) {
            return null;
        }
    };
    CATEGORY getCategory();
    CATEGORY_GROUP getCategoryGroup();
    String getName();
    default TypeMetadata getOrigin(){
        return this;
    }
    int ignoreLength();
    int ignorePrecision();
    int ignoreScale();
    boolean support();
    default String formula() {
        return getName();
    }
    default boolean isArray(){
        return false;
    }
    void setArray(boolean array);
    TypeMetadata.Config config();
    /**
     * 写入数据库或查询条件时的类型
     * @return Class
     */
    Class compatible();

    /**
     * 中间转换类型
     * 如 value(double[]) > transfer(Point) > byte[](compatible)
     * @return Class
     */
    Class transfer();


    /**
     * 支持的数据库
     * @return DatabaseType
     */
    List<DatabaseType> databaseTypes();

    Object convert(Object value, Object def);
    default Object convert(Object value, Class target){
        return convert(value, target, false);
    }
    Object convert(Object value, Class target, boolean array);
    default Object convert(Object value, Class target, Object def){
        return convert(value, target, false, def);
    }
    Object convert(Object value, Class target, boolean array, Object def);
    Object convert(Object value, Object obj, Field field);

    default Object read(Object value, Object def, Class clazz){
        return read(value, def, clazz, false);
    }
    Object read(Object value, Object def, Class clazz, boolean array);
    default Object write(Object value, Object def, boolean placeholder){
        return write(value, def, false, placeholder);
    }
    Object write(Object value, Object def, boolean array, boolean placeholder);


    class Config {
        /**
         * SQL生成公式如INTERVAL DAY(｛p｝) TO HOUR
         * 不提供则根据NAME 生成
         */
        private String formula;
        /**
         * 是否忽略长度，创建和比较时忽略，但元数据中可能会有对应的列也有值
         * -1:未设置可以继承上级 0:不忽略 1:忽略 2:根据情况(是否提供) 3:用来处理precision和scale相互依赖的情况,只有同时有值才生效,其中一个没值就全忽略
         */
        private int ignoreLength = -1;
        private int ignorePrecision = -1;
        private int ignoreScale = -1;
        /**
         * 读取元数据时 字符类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] lengthRefers;
        /**
         * 读取元数据时 数字类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] precisionRefers;
        /**
         * 读取元数据时 小数位对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] scaleRefers;
        public Config(){}
        public Config(String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public Config(String lengthRefer, String precisionRefer, String scaleRefer){
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
        }

        public Config(int ignoreLength, int ignorePrecision, int ignoreScale){
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public int ignoreLength() {
            return ignoreLength;
        }

        public Config setIgnoreLength(int ignoreLength) {
            this.ignoreLength = ignoreLength;
            return this;
        }

        public int ignorePrecision() {
            return ignorePrecision;
        }

        public Config setIgnorePrecision(int ignorePrecision) {
            this.ignorePrecision = ignorePrecision;
            return this;
        }

        public int ignoreScale() {
            return ignoreScale;
        }

        public Config setIgnoreScale(int ignoreScale) {
            this.ignoreScale = ignoreScale;
            return this;
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

        public Config setLengthRefers(String[] lengthRefers) {
            this.lengthRefers = lengthRefers;
            return this;
        }

        public Config setLengthRefer(String lengthRefer) {
            if(BasicUtil.isNotEmpty(lengthRefer)) {
                this.lengthRefers = lengthRefer.split(",");
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
        public Config setPrecisionRefers(String[] precisionRefers) {
            this.precisionRefers = precisionRefers;
            return this;
        }

        public Config setPrecisionRefer(String precisionRefer) {
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
        public Config setScaleRefers(String[] scaleRefers) {
            this.scaleRefers = scaleRefers;
            return this;
        }

        public Config setScaleRefer(String scaleRefer) {
            if(BasicUtil.isNotEmpty(scaleRefer)) {
                this.scaleRefers = scaleRefer.split(",");
            } else {

                this.scaleRefers = null;
            }
            return this;
        }

        public String getFormula() {
            return formula;
        }

        public void setFormula(String formula) {
            this.formula = formula;
        }

        /**
         * 合并copy的属性(非空并且!=-1的属性)
         * @param copy
         * @return
         */
        public Config merge(Config copy){
            if(null != copy){
                String formula = copy.getFormula();
                int ignoreLength = copy.ignoreLength();
                int ignorePrecision = copy.ignorePrecision;
                int ignoreScale = copy.ignoreScale();
                if(BasicUtil.isNotEmpty(formula)){
                    this.formula = formula;
                }
                if(-1 != ignoreLength){
                    this.ignoreLength = ignoreLength;
                }
                if(-1 != ignorePrecision){
                    this.ignorePrecision = ignorePrecision;
                }
                if(-1 != ignoreScale){
                    this.ignoreScale = ignoreScale;
                }
                String[] lengthRefers = copy.getLengthRefers();;
                String[] precisionRefers = copy.getPrecisionRefers();
                String[] scaleRefers = copy.getScaleRefers();
                if(null != lengthRefers && lengthRefers.length > 0){
                    this.lengthRefers = lengthRefers;
                }
                if(null != precisionRefers && precisionRefers.length > 0){
                    this.precisionRefers = precisionRefers;
                }
                if(null != scaleRefers && scaleRefers.length > 0){
                    this.scaleRefers = scaleRefers;
                }
            }
            return this;
        }
    }
}
