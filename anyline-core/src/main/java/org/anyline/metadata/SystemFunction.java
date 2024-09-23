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

package org.anyline.metadata;

import org.anyline.metadata.type.DatabaseType;

public interface SystemFunction {
    enum META{
        CONCAT("拼接String", 9);
        META(String title, int cnt){
            this.title = title;
            this.valueCount = cnt;
        }
        private final String title;
        private final int valueCount;
        /**
         * 支持参数数量 0:没有参数 IS NULL/IS EMPTY 1:一个参数 2:两个参数 BETWEEN 9:多个参数  IN()
         * @return boolean
         */
        public int valueCount() {
            return valueCount;
        }
        public String title() {
            return title;
        }
    }
    DatabaseType database();
    META meta();
    String formula(boolean placeholder, boolean unicode, Object ... args);
    default String formula(Object ... args) {
        return formula(false, false, args);
    }
    default String formula(boolean placeholder, Object ... args) {
        return formula(placeholder, false, args);
    }
}
