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

public interface ColumnType {
    public static ColumnType ILLEGAL = new ColumnType() {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean ignorePrecision() {
            return false;
        }

        @Override
        public boolean ignoreScale() {
            return false;
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
        public DatabaseType[] dbs() {
            return new DatabaseType[0];
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
        public Object convert(Object value,  Class target,  boolean array,  Object def) {
            return null;
        }

        @Override
        public Object convert(Object value,  Object obj,  Field field) {
            return null;
        }

        @Override
        public Object read(Object value,  Object def,  Class clazz) {
            return null;
        }

        @Override
        public Object read(Object value,  Object def,  Class clazz,  boolean array) {
            return null;
        }

        @Override
        public Object write(Object value,  Object def,  boolean placeholder) {
            return null;
        }

        @Override
        public Object write(Object value,  Object def,  boolean array,  boolean placeholder) {
            return null;
        }
    };
    String getName();
    boolean ignorePrecision();
    boolean ignoreScale();
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
    DatabaseType[] dbs();

    Object convert(Object value,  Object def);
    default Object convert(Object value,  Class target){
        return convert(value,  target,  false);
    }
    Object convert(Object value,  Class target,  boolean array);
    default Object convert(Object value,  Class target,  Object def){
        return convert(value,  target,  false,  def);
    }
    Object convert(Object value,  Class target,  boolean array,  Object def);
    Object convert(Object value,  Object obj,  Field field);

    default Object read(Object value,  Object def,  Class clazz){
        return read(value,  def,  clazz,  false);
    }
    Object read(Object value,  Object def,  Class clazz,  boolean array);
    default Object write(Object value,  Object def,  boolean placeholder){
        return write(value,  def,  false,  placeholder);
    }
    Object write(Object value,  Object def,  boolean array,  boolean placeholder);
}
