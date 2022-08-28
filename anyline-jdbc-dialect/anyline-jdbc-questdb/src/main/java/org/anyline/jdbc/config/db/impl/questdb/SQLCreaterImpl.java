package org.anyline.jdbc.config.db.impl.questdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.jdbc.creater.questdb")
public class SQLCreaterImpl extends org.anyline.jdbc.config.db.impl.postgresql.SQLCreaterImpl{
    public DB_TYPE type(){
        return DB_TYPE.QuestDB;
    }
    @Value("${anyline.jdbc.delimiter.questdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet() throws Exception {
        setDelimiter(delimiter);
    }
} 
