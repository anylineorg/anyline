package org.anyline.entity.data;

import java.util.ArrayList;
import java.util.List;

public class Trigger  {
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

    public Table getTable() {
        return table;
    }

    public String getTableName() {
        return table.getName();
    }

    
    public org.anyline.entity.data.Trigger setTable(Table table) {
        this.table = table;
        return this;
    }
    public org.anyline.entity.data.Trigger setTable(String table) {
        this.table = new Table(table);
        return this;
    }

    public String getName() {
        return name;
    }

    public org.anyline.entity.data.Trigger setName(String name) {
        this.name = name;
        return this;
    }

    public String getDefinition() {
        return definition;
    }

    public org.anyline.entity.data.Trigger setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    public TIME getTime() {
        return time;
    }

    public org.anyline.entity.data.Trigger setTime(TIME time) {
        this.time = time;
        return this;
    }
    public org.anyline.entity.data.Trigger setTime(String time) {
        this.time = TIME.valueOf(time);
        return this;
    }

    public List<EVENT> getEvents() {
        return events;
    }

    public org.anyline.entity.data.Trigger addEvent(EVENT ... events) {
        for(EVENT event:events){
            this.events.add(event);
        }
        return this;
    }
    public org.anyline.entity.data.Trigger addEvent(String ... events) {
        for(String event:events){
            this.events.add(EVENT.valueOf(event));
        }
        return this;
    }

    public boolean isEach() {
        return each;
    }

    public org.anyline.entity.data.Trigger setEach(boolean each) {
        this.each = each;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public org.anyline.entity.data.Trigger setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
