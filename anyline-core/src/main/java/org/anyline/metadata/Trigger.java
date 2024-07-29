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
import java.util.List;

public class Trigger extends TableAffiliation<Trigger> implements Serializable {
    protected String keyword = "TRIGGER";
    public enum EVENT{
        INSERT, DELETE, UPDATE;
    }
    public enum TIME{
        BEFORE("BEFORE"),
        AFTER("AFTER"),
        INSTEAD ("INSTEAD OF");
        final String sql;
        TIME(String sql) {
            this.sql = sql;
        }
        public String sql() {
            return sql;
        }
    }
    private TIME time;
    private List<EVENT> events = new ArrayList<>();
    private boolean each = true; //每行触发发

    public Trigger drop() {
        this.action = ACTION.DDL.TRIGGER_DROP;
        return super.drop();
    }
    public void setEach(boolean each) {
        this.each = each;
    }

    public TIME getTime() {
        if(getmap && null != update) {
            return ((Trigger)update).time;
        }
        return time;
    }

    public Trigger setTime(TIME time) {
        if(setmap && null != update) {
            ((Trigger)update).time = time;
            return this;
        }
        this.time = time;
        return this;
    }
    public Trigger setTime(String time) {
        if(setmap && null != update) {
            ((Trigger)update).setTime(time);
            return this;
        }
        this.time = TIME.valueOf(time);
        return this;
    }

    public List<EVENT> getEvents() {
        if(getmap && null != update) {
            return ((Trigger)update).events;
        }
        return events;
    }

    public Trigger addEvent(EVENT ... events) {
        if(setmap && null != update) {
            ((Trigger)update).addEvent(events);
            return this;
        }
        for(EVENT event:events) {
            this.events.add(event);
        }
        return this;
    }
    public Trigger setEvent(List<EVENT> events) {
        if(setmap && null != update) {
            ((Trigger)update).setEvent(events);
            return this;
        }
        this.events = events;
        return this;
    }
    public Trigger addEvent(String ... events) {
        if(setmap && null != update) {
            ((Trigger)update).addEvent(events);
            return this;
        }
        for(String event:events) {
            this.events.add(EVENT.valueOf(event));
        }
        return this;
    }

    public boolean isEach() {
        if(getmap && null != update) {
            return ((Trigger)update).each;
        }
        return each;
    }

    public String getKeyword() {
        return keyword;
    }
    public Trigger clone() {
        Trigger clone = super.clone();
        clone.events.addAll(this.events);
        return clone;
    }
}
