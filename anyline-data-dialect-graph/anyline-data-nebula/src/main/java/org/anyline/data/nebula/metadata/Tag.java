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

package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;
import org.anyline.metadata.graph.VertexTable;

public class Tag extends VertexTable {
    protected String keyword = "TAG"            ;
    public Tag() {
    }
    public Tag(String name) {
        super(name);
    }

    public Tag(String schema, String table) {
        super(schema, table);
    }
    public Tag(Schema schema, String table) {
        super(schema, table);
    }
    public Tag(String catalog, String schema, String name) {
        super(catalog, schema, name);
    }
    public Tag(Catalog catalog, Schema schema, String name) {
        super(catalog, schema, name);
    }

    @Override
    public String getKeyword() {
        return keyword;
    }
}
