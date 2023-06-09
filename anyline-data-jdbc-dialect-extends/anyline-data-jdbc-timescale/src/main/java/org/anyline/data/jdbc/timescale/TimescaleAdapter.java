package org.anyline.data.jdbc.timescale;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.timescale")
public class TimescaleAdapter extends PostgresqlAdapter {
    
    public DatabaseType type(){
        return DatabaseType.Timescale;
    }
    @Value("${anyline.data.jdbc.delimiter.timescale:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
}
