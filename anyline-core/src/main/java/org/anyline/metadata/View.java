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
import java.util.HashMap;
import java.util.Map;

public class View extends Table<View> implements Serializable {
    public enum TYPE implements Type{
        NORMAL(2);
        public final int value;
        TYPE(int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
    }

    private static Map<Integer, Type> types = new HashMap<>();
    static {
        for(TYPE type: TYPE.values()) {
            types.put(type.value, type);
        }
    }
    public static Map<Integer, Type> types() {
        return types;
    }

    protected String keyword = "VIEW"            ;
    protected boolean materialize = false        ; //是否物化
    protected Table target;

    public View() {
        this(null);
    }
    public View(String name) {
        this(null, name);
    }
    public View(Schema schema, String table) {
        this(null, schema, table);
    }
    public View(Catalog catalog, Schema schema, String name) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }
    public Table target(){
        return target;
    }
    public View target(Table target){
        this.target = target;
        return this;
    }
    public View target(String target){
        this.target = new Table(target);
        return this;
    }
    public String keyword() {
        return keyword;
    }

    public boolean isMaterialize() {
        return materialize;
    }

    public void setMaterialize(boolean materialize) {
        this.materialize = materialize;
    }

    public String toString() {
        return this.keyword+":"+name;
    }

/* ********************************* field refer ********************************** */
   
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_MATERIALIZE                   = "MATERIALIZE";
    public static final String FIELD_MATERIALIZE_CHECK             = "MATERIALIZE_CHECK";
    public static final String FIELD_MATERIALIZE_CHECK_VALUE       = "MATERIALIZE_CHECK_VALUE";
}