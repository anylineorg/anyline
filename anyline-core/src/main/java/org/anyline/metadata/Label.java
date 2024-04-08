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

import java.io.Serializable;

public class Label extends Table<Label> implements Serializable {
    protected String keyword = "LABEL"            ;
    protected Label update;

    public Label(){
        this(null);
    }
    public Label(String name){
        this(null, name);
    }
    public Label(Schema schema, String table){
        this(null, schema, table);
    }
    public Label(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public Label drop(){
        this.action = ACTION.DDL.COLUMN_DROP;
        return super.drop();
    }

    public String getKeyword() {
        return this.keyword;
    }

    public String toString(){
        return this.keyword+":"+name;
    }
}
