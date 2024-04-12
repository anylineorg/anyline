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

package org.anyline.adapter;

public interface DataReader {
    /**
     * 从数据库中读取数据(非基础类型时需要)
     * @param value value
     * @return Object
     */
    Object read(Object value);

    /**
     * 支持的类型符合这些类型的 在读取之后 由当前reader转换
     * @return class ColumnType StringColumnType
     */
    default Object[] supports(){
        return null;
    }
}
