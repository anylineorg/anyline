package org.anyline.data.jdbc.questdb;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.questdb")
public class QuestDbAdapter extends PostgresqlAdapter {
    public DatabaseType type(){
        return DatabaseType.QuestDB;
    }
    @Value("${anyline.jdbc.delimiter.questdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
