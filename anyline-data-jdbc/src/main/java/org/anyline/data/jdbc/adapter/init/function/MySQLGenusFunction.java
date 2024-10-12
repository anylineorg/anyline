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

package org.anyline.data.jdbc.adapter.init.function;

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

public enum MySQLGenusFunction implements SystemFunction {
    CONCAT(META.CONCAT) {
        @Override
        public String formula(Boolean placeholder, Boolean unicode, Object... args) {
            return "";
        }
    };
    MySQLGenusFunction(META meta){
        this.meta = meta;
    }
    private final SystemFunction.META meta;
    public DatabaseType database() {
        return null;
    }
    public META meta() {
        return meta;
    }
}
