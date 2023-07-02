package org.anyline.data.jdbc.tdsql;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.tdsql")
public class TDSQLAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.TDSQL;
    }
    @Value("${anyline.data.jdbc.delimiter.tdsql:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
