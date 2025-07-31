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

package org.anyline.metadata;

import org.anyline.util.BasicUtil;

import java.io.Serializable;

public class Schema extends Metadata<Schema> implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String keyword = "SCHEMA"           ;
    public Schema() {

    }
    public Schema(String name) {
        this.name = name;
    }
    public String toString() {
        String str = keyword()+":";
        if(null != catalog) {
            str += getCatalogName() + ".";
        }
        str += name;
        return str;
    }
    public boolean isEmpty() {
        if(null == name || name.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean equals(Schema schema) {
        return equals(schema, true);
    }
    public boolean equals(Schema schema, boolean ignoreCase) {
        if(null == schema) {
            return false;
        }
        boolean catalog_equal = BasicUtil.equals(this.catalog, schema.getCatalog(), ignoreCase);
        if(catalog_equal) {
            return BasicUtil.equals(this.name, schema.getName(), ignoreCase);
        }
        return false;
    }
    public String keyword() {
        return this.keyword;
    }

/* ********************************* field refer ********************************** */
    public static final String FIELD_KEYWORD                       = "KEYWORD";
}