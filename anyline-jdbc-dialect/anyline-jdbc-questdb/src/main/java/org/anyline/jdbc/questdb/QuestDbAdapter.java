package org.anyline.jdbc.questdb;

import org.anyline.jdbc.postgresql.PostgresqlAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.sql.adapter.questdb")
public class QuestDbAdapter extends PostgresqlAdapter {
    public DB_TYPE type(){
        return DB_TYPE.QuestDB;
    }
    @Value("${anyline.jdbc.delimiter.questdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet() throws Exception {
        setDelimiter(delimiter);
    }
} 
