package org.anyline.data.jdbc.hashdata;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.hashdata")
public class HashDataAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.HashData;
    }
    @Value("${anyline.data.jdbc.delimiter.hashdata:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
