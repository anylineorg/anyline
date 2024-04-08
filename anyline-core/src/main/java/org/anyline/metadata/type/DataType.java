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

public interface DataType {
    public static DataType ILLEGAL = new DataType() {

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return null;
        }

        @Override
        public DataType convert(Convert convert) {
            return null;
        }

        @Override
        public Convert convert(Class clazz) {
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
        public Class supportClass() {
            return null;
        }
    };
    /**
     * 从数据库中读取数据, 常用的基本类型可以自动转换, 不常用的如json/point/polygon/blob等转换成anyline对应的类型
     * @param value value
     * @param def 默认值
     * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
     * @return Object
     */
    public abstract Object read(Object value, Object def, Class clazz);
    /**
     * 写入数据库前类型转换<br/>
     * 如果有占位符成数据库可接受的Java数据类型<br/>
     * 如果没有占位符 需要确定加单引号或内置函数<br/>
     * @param placeholder 是否占位符
     * @param value value
     * @param def 默认值
     * @return Object
     */
    public abstract Object write(Object value, Object def, boolean placeholder);

    public abstract DataType convert(Convert convert);
    public abstract Convert convert(Class clazz);

    //public abstract JavaType getJavaType();
    // public String getName();

    /**
     * 定义列时 数据类型格式
     * @return boolean
     */
    public abstract int ignoreLength();
    public abstract int ignorePrecision();
    public abstract int ignoreScale();
    public abstract boolean support();
    public abstract Class supportClass();
}
