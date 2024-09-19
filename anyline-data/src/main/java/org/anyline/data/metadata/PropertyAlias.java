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

package org.anyline.data.metadata;

import org.anyline.metadata.Metadata;
import org.anyline.metadata.type.DatabaseType;

public interface PropertyAlias {
    /**
     * 输入属性名称
     * @return String
     */
    String input();

    /**
     * 适用数据库的类型
     * @return String
     */
    default DatabaseType database() {
        return null;
    }
    /**
     * 兼容当前数据库的类型
     * @return String
     */
    String value();

    /**
     * 属性分组 如 ENGINE
     * @return String
     */
    default String group() {
        return "default";
    }

    /**
     * 适用类型 如Table.class
     * @return Class
     */
    default Class<? extends Metadata> metadata() {
        return Metadata.class;
    }
}
