package org.anyline.data.jdbc.timescale;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.timescale")
public class TimescaleAdapter extends PostgresqlAdapter {
    public DB_TYPE type(){
        return DB_TYPE.Timescale;
    }
    @Value("${anyline.jdbc.delimiter.timescale:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
}
