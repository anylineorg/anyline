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



package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;

import java.io.Serializable;

/**
 * 为什么不用VertexType或VertexCollection,因为type容易误会成一个属性,collection容易误会成结果集
 */
public class VertexTable extends GraphTable implements Serializable {

    public VertexTable() {
    }
    public VertexTable(String name) {
        super(name);
    }

    public VertexTable(String schema, String table) {
        super(schema, table);
    }
    public VertexTable(Schema schema, String table) {
        super(schema, table);
    }
    public VertexTable(String catalog, String schema, String name) {
        super(catalog, schema, name);
    }
    public VertexTable(Catalog catalog, Schema schema, String name) {
        super(catalog, schema, name);
    }

}
