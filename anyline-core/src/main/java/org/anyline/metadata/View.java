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

import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class View extends Table implements Serializable {
    protected String keyword = "VIEW"            ;
    protected View update;
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
    public View(String schema, String table){
        this(null, schema, table);
    }
    public View(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }


    public View update(){
        return update(true, true);
    }
    public View update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }


    public View getUpdate() {
        return update;
    }

    public View setUpdate(View update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
            update.origin = this;
        }
        return this;
    }

    public View setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public View setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }
    public String getKeyword() {
        return keyword;
    }

    public View clone(){
        View copy = new View();
        BeanUtil.copyFieldValue(copy, this);

        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;;

        return copy;
    }
    public String toString(){
        return this.keyword+":"+name;
    }
}
