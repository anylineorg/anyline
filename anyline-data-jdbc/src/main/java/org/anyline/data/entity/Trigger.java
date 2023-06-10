package org.anyline.data.entity;

public class Trigger implements org.anyline.entity.data.Trigger{
    private String name;
    private org.anyline.entity.data.Table table;
    private String definition;
    private TIME time;
    private EVENT event;
    private boolean each; //每行触发发
    private String comment;

    public org.anyline.entity.data.Table getTable() {
        return table;
    }

    public String getTableName() {
        return table.getName();
    }

    @Override
    public org.anyline.entity.data.Trigger setTable(org.anyline.entity.data.Table table) {
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

    public EVENT getEvent() {
        return event;
    }

    public org.anyline.entity.data.Trigger setEvent(EVENT event) {
        this.event = event;
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
