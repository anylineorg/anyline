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


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public interface TypeMetadata {
    enum CATEGORY{STRING, INT, FLOAT, BOOLEAN, DATE, TIMESTAMP, COLLECTION, BYTES, GEOMETRY, NONE}
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
        public String getName() {
            return null;
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
    };
    CATEGORY getCategory();
    String getName();
    default TypeMetadata getOrigin(){
        return this;
    }
    int ignoreLength();
    int ignorePrecision();
    int ignoreScale();
    boolean support();
    default boolean isArray(){
        return false;
    }
    void setArray(boolean array);
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
         * 是否忽略长度，创建和比较时忽略，但元数据中可能会有对应的列也有值
         * -1:未设置可以继承上级 0:不忽略 1:忽略 2:根据情况(是否提供)
         */
        private int ignoreLength = -1;
        private int ignorePrecision = -1;
        private int ignoreScale = -1;
        /**
         * 读取元数据时 字符类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String lengthColumn;
        /**
         * 读取元数据时 数字类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String precisionColumn;
        /**
         * 读取元数据时 小数位对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String scaleColumn;
        public Config(){}
        public Config(String precision){
            this.precisionColumn = precision;
        }
        public Config(String length, String precision, String scale){
            this.lengthColumn = length;
            this.precisionColumn = precision;
            this.scaleColumn = scale;
        }
        public Config(String precision, String scale){
            this.precisionColumn = precision;
            this.scaleColumn = scale;
        }

        public int ignoreLength() {
            return ignoreLength;
        }

        public void setIgnoreLength(int ignoreLength) {
            this.ignoreLength = ignoreLength;
        }

        public int ignorePrecision() {
            return ignorePrecision;
        }

        public void setIgnorePrecision(int ignorePrecision) {
            this.ignorePrecision = ignorePrecision;
        }

        public int ignoreScale() {
            return ignoreScale;
        }

        public void setIgnoreScale(int ignoreScale) {
            this.ignoreScale = ignoreScale;
        }

        public String getLengthColumn() {
            return lengthColumn;
        }

        public void setLengthColumn(String lengthColumn) {
            this.lengthColumn = lengthColumn;
        }

        public String getPrecisionColumn() {
            return precisionColumn;
        }

        public void setPrecisionColumn(String precisionColumn) {
            this.precisionColumn = precisionColumn;
        }

        public String getScaleColumn() {
            return scaleColumn;
        }

        public void setScaleColumn(String scaleColumn) {
            this.scaleColumn = scaleColumn;
        }
    }
}
