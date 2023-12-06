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

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
public class Constraint<E extends Constraint> extends BaseMetadata<E> implements Serializable {
    public enum TYPE{
        PRIMARY_KEY, UNIQUE, NOT_NULL, FOREIGN_KEY, DEFAULT
    }
    protected TYPE type;
    protected LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
    public Constraint(){
    }

    public Constraint(String name){
        setName(name);
    }
    public Constraint(Table table, String name){
        setTable(table);
        setName(name);
    }
    public Constraint(Table table, String name, String type){
        setTable(table);
        setName(name);
        setType(type);
    }
    public String getName() {
        if(null == name){
            name = "constraint_";
            if(null != columns){
                name += BeanUtil.concat(columns.keySet());
            }
        }
        return name;
    }

    public String getTableName(boolean update) {
       Table table = getTable(update);
       if(null != table) {
           return table.getName();
       }
       return null;
    }

    public Table getTable(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return (Table)table.getUpdate();
            }
        }
        return table;
    }


    public boolean isUnique() {
        if(getmap && null != update){
            return update.isUnique();
        }
        return type == TYPE.UNIQUE || type == TYPE.PRIMARY_KEY;
    }

    public TYPE getType() {
        if(getmap && null != update){
            return update.type;
        }
        return type;
    }

    public E setType(TYPE type) {
        this.type = type;
        return (E)this;
    }

    public E setType(String type) {
        if(null != type){
            type = type.toUpperCase();
            if(type.contains("PRIMARY")){
                this.type = TYPE.PRIMARY_KEY;
            }else if(type.contains("FOREIGN")){
                this.type = TYPE.FOREIGN_KEY;
            }else if(type.contains("UNIQUE")){
                this.type = TYPE.UNIQUE;
            }else if(type.contains("NOT")){
                this.type = TYPE.NOT_NULL;
            }
        }
        return (E)this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        if(getmap && null != update){
            return update.columns;
        }
        return columns;
    }
    public Column getColumn(String name) {
        if(getmap && null != update){
            return update.getColumn(name);
        }
        if(null != columns && null != name){
            return columns.get(name.toUpperCase());
        }
        return null;
    }

    public E setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        return (E)this;
    }
    public E addColumn(Column column){
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return (E)this;
    }

    public E addColumn(String column){
        return addColumn(new Column(column));
    }

    public E addColumn(String column, String order){
        return addColumn(new Column(column).setOrder(order));
    }
    public E addColumn(String column, String order, int position){
        return addColumn(new Column(column).setOrder(order).setPosition(position));
    }


    public E clone(){
        E copy = super.clone();

        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        return copy;
    }
}
