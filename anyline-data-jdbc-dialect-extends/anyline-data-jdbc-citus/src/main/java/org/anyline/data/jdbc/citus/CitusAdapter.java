package org.anyline.data.jdbc.citus;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.citus")
public class CitusAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.Citus;
    }
    @Value("${anyline.data.jdbc.delimiter.citus:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
