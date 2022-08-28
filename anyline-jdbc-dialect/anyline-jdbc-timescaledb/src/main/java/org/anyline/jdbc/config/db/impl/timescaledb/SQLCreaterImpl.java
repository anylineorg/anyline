package org.anyline.jdbc.config.db.impl.timescaledb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.creater.timescaledb")
public class SQLCreaterImpl extends org.anyline.jdbc.config.db.impl.postgresql.SQLCreaterImpl{
    public DB_TYPE type(){
        return DB_TYPE.TimescaleDB;
    }
    @Value("${anyline.jdbc.delimiter.timescaledb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet() throws Exception {
        setDelimiter(delimiter);
    }
} 
