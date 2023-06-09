package org.anyline.entity.data;

public interface Trigger {
    enum EVENT{INSERT,DELETE,UPDATE}
    enum TIME{BEFORE,AFTER}

    Table getTable();

    org.anyline.entity.data.Trigger setTable(Table table);
    org.anyline.entity.data.Trigger setTable(String table);


    String getName();
    Trigger setName(String name) ;
    String getDefinition() ;
    Trigger setDefinition(String definition);
    TIME getTime();
    Trigger setTime(TIME time) ;
    EVENT getEvent() ;
    Trigger setEvent(EVENT event);
    boolean isEach() ;
    Trigger setEach(boolean each) ;
}
