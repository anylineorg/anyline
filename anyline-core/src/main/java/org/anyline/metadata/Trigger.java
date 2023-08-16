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
import java.util.ArrayList;
import java.util.List;

public class Trigger  implements Serializable {
    public enum EVENT{
        INSERT,DELETE,UPDATE;
    }
    public enum TIME{
        BEFORE("BEFORE"),
        AFTER("AFTER"),
        INSTEAD ("INSTEAD OF");
        final String sql;
        TIME(String sql){
            this.sql = sql;
        }
        public String sql(){
            return sql;
        }
    }

    private String name;
    private Table table;
    private String definition;
    private TIME time;
    private List<EVENT> events = new ArrayList<>();
    private boolean each = true; //每行触发发
    private String comment;

    protected Trigger update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了table,catalog, schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了table,catalog, schema,name,drop,action)


    /**
     * 相关表
     * @param update 是否检测upate
     * @return table
     */
    public Table getTable(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return table.getUpdate();
            }
        }
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getTableName(boolean update) {
        Table table = getTable(update);
        if(null != table){
            return table.getName();
        }
        return null;
    }

    public Trigger setTable(String table) {
         this.table = new Table(table);
        return this;
    }




    public String getName() {
        return name;
    }

    public Trigger setName(String name) {
        this.name = name;
        return this;
    }

    public String getDefinition() {
        if(getmap && null != update){
            return update.definition;
        }
        return definition;
    }

    public Trigger setDefinition(String definition) {
        if(setmap && null != update){
            update.definition = definition;
            return this;
        }
        this.definition = definition;
        return this;
    }

    public TIME getTime() {
        if(getmap && null != update){
            return update.time;
        }
        return time;
    }

    public Trigger setTime(TIME time) {
        if(setmap && null != update){
            update.time = time;
            return this;
        }
        this.time = time;
        return this;
    }
    public Trigger setTime(String time) {
        if(setmap && null != update){
            update.setTime(time);
            return this;
        }
        this.time = TIME.valueOf(time);
        return this;
    }

    public List<EVENT> getEvents() {
        if(getmap && null != update){
            return update.events;
        }
        return events;
    }

    public Trigger addEvent(EVENT ... events) {
        if(setmap && null != update){
            update.addEvent(events);
            return this;
        }
        for(EVENT event:events){
            this.events.add(event);
        }
        return this;
    }
    public Trigger addEvent(String ... events) {
        if(setmap && null != update){
            update.addEvent(events);
            return this;
        }
        for(String event:events){
            this.events.add(EVENT.valueOf(event));
        }
        return this;
    }

    public boolean isEach() {
        if(getmap && null != update){
            return update.each;
        }
        return each;
    }

    public Trigger setEach(boolean each) {
        if(setmap && null != update){
            update.each = each;
            return this;
        }
        this.each = each;
        return this;
    }

    public String getComment() {
        if(getmap && null != update){
            return update.comment;
        }
        return comment;
    }

    public Trigger setComment(String comment) {
        if(setmap && null != update){
            update.comment = comment;
            return this;
        }
        this.comment = comment;
        return this;
    }


    public Trigger update(){
        return update(true, true);
    }
    public Trigger update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }


    public Trigger getUpdate() {
        return update;
    }

    public Trigger setUpdate(Trigger update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return this;
    }

    public Trigger setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Trigger setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public Trigger clone(){
        Trigger copy = new Trigger();
        BeanUtil.copyFieldValue(copy, this);

        copy.events.addAll(this.events);

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;;

        return copy;
    }
}
