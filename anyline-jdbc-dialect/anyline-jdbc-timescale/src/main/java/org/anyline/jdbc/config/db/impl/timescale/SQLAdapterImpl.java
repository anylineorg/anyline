package org.anyline.jdbc.config.db.impl.timescale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.sql.adapter.timescale")
public class SQLAdapterImpl extends org.anyline.jdbc.config.db.impl.postgresql.SQLAdapterImpl {
    public DB_TYPE type(){
        return DB_TYPE.Timescale;
    }
    @Value("${anyline.jdbc.delimiter.timescale:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet() throws Exception {
        setDelimiter(delimiter);
    }
} 
