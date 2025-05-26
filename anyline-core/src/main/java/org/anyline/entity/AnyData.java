/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.entity;

import java.io.Serializable;

public interface AnyData<T> extends Serializable, Cloneable {
    /**
     * key转小写,转换后删除原来的key
     * @param recursion 是否递归执行(仅支持AnyData类型)
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    T toLowerKey(boolean recursion, String... keys);
    /**
     * key转小写,转换后删除原来的key
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    default T toLowerKey(String... keys) {
        return toLowerKey(true, keys);
    }

    /**
     * key转小写,转换后删除原来的key
     * @param recursion 是否递归执行(仅支持AnyData类型)
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    T toUpperKey(boolean recursion, String... keys);

    /**
     * key转大写,转换后删除原来的key
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    default T toUpperKey(String... keys) {
        return toUpperKey(true, keys);
    }

    String toJSON();

}
