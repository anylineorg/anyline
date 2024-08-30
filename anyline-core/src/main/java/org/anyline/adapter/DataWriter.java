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

import org.anyline.metadata.type.TypeMetadata;

public interface DataWriter {
    /**
     * 写入数据库前类型转换(非基础类型时需要)
     * @param value value
     * @param placeholder 是否启用占位符 不启用占位符时 注意判断是否需要引号
     * @param type 要写入到列的数据类型 开启了ConfigTable.IS_AUTO_CHECK_METADATA的情况下 当前方法才会接收到这个类型
     * @return Object 把value转换成 java默认类型 或 数据库驱动中支持的类型
     */
    Object write(Object value, boolean placeholder, TypeMetadata type);

    /**
     * 支持的类型符合这些类型的 在写入数据库之前 由当前writer转换
     * @return class ColumnType StringColumnType
     */
    default Object[] supports() {
        return null;
    }
}
