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

package org.anyline.data.handler;

public interface StreamHandler extends DataHandler {
    /**
     * 每次从ResultSet中读取的行数
     * @return int
     */
    default int size() {return Integer.MIN_VALUE;}

    /**
     * 用于项目中释放连接 keep()返回true时需要在项目中释放连接
     * @param handler ConnectionHandler
     */
    default void handler(ConnectionHandler handler) {}

    /**
     * read(ResultSet result)之后 是否保存ResultSet连接状态，如果保持则需要在项目中调用ConnectionHandler释放连接
     * @return boolean
     */
    default boolean keep() {
        return false;
    }
}
