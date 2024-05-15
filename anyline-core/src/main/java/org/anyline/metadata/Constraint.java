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

import org.anyline.entity.Order;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
public class Constraint<E extends Constraint> extends Metadata<E> implements Serializable {
    protected String keyword = "CONSTRAINT"           ;
    public enum TYPE{
        PRIMARY_KEY, UNIQUE, NOT_NULL, FOREIGN_KEY, CHECK, DEFAULT
    }
    protected TYPE type;
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected LinkedHashMap<String, Integer> positions = new LinkedHashMap<>();
    protected LinkedHashMap<String, Order.TYPE> orders = new LinkedHashMap<>();
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
            if(type.contains("PRIMARY") || type.equals("P")){
                this.type = TYPE.PRIMARY_KEY;
            }else if(type.contains("FOREIGN")){
                this.type = TYPE.FOREIGN_KEY;
            }else if(type.contains("UNIQUE")){
                this.type = TYPE.UNIQUE;
            }else if(type.contains("NOT")){
                this.type = TYPE.NOT_NULL;
            }else if(type.contains("DEFAULT")){
                this.type = TYPE.DEFAULT;
            }else if(type.contains("CHECK") || type.equals("C")){
                this.type = TYPE.CHECK;
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
        return addColumn(column, order, 0);
    }
    public E addColumn(String column, String order, int position){
        positions.put(column.toUpperCase(), position);
        Order.TYPE type = Order.TYPE.ASC;
        if(null != order && order.toUpperCase().contains("DESC")){
            type = Order.TYPE.DESC;
        }
        setOrder(column, type);
        return addColumn(new Column(column));
    }

    public E setOrders(LinkedHashMap<String, Order.TYPE> orders) {
        this.orders = orders;
        return (E)this;
    }

    public E setOrder(String column, Order.TYPE order) {
        this.orders.put(column.toUpperCase(), order);
        return (E)this;
    }
    public E setOrder(Column column, Order.TYPE order) {
        this.orders.put(column.getName().toUpperCase(), order);
        return (E)this;
    }
    public Order.TYPE getOrder(String column){
        return orders.get(column.toUpperCase());
    }

    public String getKeyword() {
        return this.keyword;
    }
    public E clone(){
        E copy = super.clone();

        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        return copy;
    }
}
