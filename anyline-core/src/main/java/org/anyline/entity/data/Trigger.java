package org.anyline.entity.data;

import java.util.List;

public interface Trigger {
    enum EVENT{
        INSERT,DELETE,UPDATE;
    }
    enum TIME{
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

    Table getTable();
    String getTableName();

    org.anyline.entity.data.Trigger setTable(Table table);
    org.anyline.entity.data.Trigger setTable(String table);


    String getName();
    Trigger setName(String name) ;
    String getDefinition() ;
    Trigger setDefinition(String definition);
    TIME getTime();
    Trigger setTime(TIME time) ;
    Trigger setTime(String time) ;
    List<EVENT> getEvents() ;
    Trigger addEvent(EVENT ... event);
    Trigger addEvent(String ... event);
    boolean isEach() ;
    Trigger setEach(boolean each) ;

    String getComment();

    Trigger setComment(String comment);
}
