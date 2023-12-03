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

public class View extends Table<View> implements Serializable {
    protected String keyword = "VIEW"            ;
    protected String definition;

    public String getDefinition() {
        if(getmap && null != update){
            return update.definition;
        }
        return definition;
    }

    public View setDefinition(String definition) {
        if(setmap && null != update){
            update.definition = definition;
        }
        this.definition = definition;
        return this;
    }


    public View(){
        this(null);
    }
    public View(String name){
        this(null, name);
    }
    public View(Schema schema, String table){
        this(null, schema, table);
    }
    public View(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }
    public String getKeyword() {
        return keyword;
    }

    public String toString(){
        return this.keyword+":"+name;
    }
}
