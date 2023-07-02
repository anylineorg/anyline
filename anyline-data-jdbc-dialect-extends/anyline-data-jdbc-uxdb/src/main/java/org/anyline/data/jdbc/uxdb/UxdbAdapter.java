package org.anyline.data.jdbc.uxdb;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.uxdb")
public class UxdbAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.UXDB;
    }
    @Value("${anyline.data.jdbc.delimiter.uxdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
