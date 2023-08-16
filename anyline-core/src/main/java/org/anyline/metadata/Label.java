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

public class Label extends Table implements Serializable {
    protected String keyword = "Label"            ;
    protected Label update;

    public Label(){
        this(null);
    }
    public Label(String name){
        this(null, name);
    }
    public Label(String schema, String table){
        this(null, schema, table);
    }
    public Label(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }


    public Label update(){
        return update(true, true);
    }
    public Label update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }


    public Label getUpdate() {
        return update;
    }

    public Label setUpdate(Label update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
            update.origin = this;
        }
        return this;
    }

    public Label setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Label setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }
    public String getKeyword() {
        return keyword;
    }

    public Label clone(){
        Label copy = new Label();
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
