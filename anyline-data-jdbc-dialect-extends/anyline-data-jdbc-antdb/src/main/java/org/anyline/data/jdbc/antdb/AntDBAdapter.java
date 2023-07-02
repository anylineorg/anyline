package org.anyline.data.jdbc.antdb;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.antdb")
public class AntDBAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.AntDB;
    }
    @Value("${anyline.data.jdbc.delimiter.antdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
