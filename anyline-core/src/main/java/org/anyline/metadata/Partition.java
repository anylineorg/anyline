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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class Partition  implements Serializable {
    public enum TYPE{LIST, RANGE, HASH}
    //RANGE
    private Object from;
    private Object to;
    //LIST
    private List<Object> list;
    //HASH
    private int modulus;
    private int remainder;

    private TYPE type;
    private LinkedHashMap<String, Column> columns;

    public Partition(){

    }
    public Partition(TYPE type){
        this.type = type;
    }
    public Partition(TYPE type, String ... columns){
        this.type = type;
        this.columns = new LinkedHashMap<>();
        for(String column:columns){
            this.columns.put(column.toUpperCase(), new Column(column));
        }
    }
    public TYPE getType() {
        return type;
    }

    public Partition setType(TYPE type) {
        this.type = type;
        return this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    public Partition setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        return this;
    }

    public Partition setColumns(String ... columns){
        this.columns = new LinkedHashMap<>();
        for(String column:columns){
            this.columns.put(column.toUpperCase(), new Column(column));
        }
        return this;
    }
    public Partition addColumn(Column column){
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return this;
    }
    public Partition addColumn(String column){
        return addColumn(new Column(column));
    }

    public Partition setRange(Object from, Object to){
        this.from = from;
        this.to = to;
        return this;
    }
    public Object getFrom() {
        return from;
    }

    public Partition setFrom(Object from) {
        this.from = from;
        return this;
    }

    public Object getTo() {
        return to;
    }

    public Partition setTo(Object to) {
        this.to = to;
        return this;
    }

    public List<Object> getList() {
        return list;
    }

    public Partition setList(List<Object> list) {
        this.list = list;
        return this;
    }
    public Partition addList(Object ... items) {
        if(null == list) {
            this.list = new ArrayList<>();
        }
        for(Object item:items) {
            if (item instanceof Collection) {
                Collection cons = (Collection) item;
                for(Object con:cons){
                    addList(con);
                }
            }else if(item instanceof Object[]){
                Object[] objs = (Object[]) item;
                for(Object obj:objs){
                    addList(obj);
                }
            }else {
                list.add(item);
            }
        }
        return this;
    }

    public int getModulus() {
        return modulus;
    }

    public Partition setModulus(int modulus) {
        this.modulus = modulus;
        return this;
    }
    public Partition setHash(int modulus, int remainder) {
        this.modulus = modulus;
        this.remainder = remainder;
        return this;
    }

    public int getRemainder() {
        return remainder;
    }

    public Partition setRemainder(int remainder) {
        this.remainder = remainder;
        return this;
    }
}
