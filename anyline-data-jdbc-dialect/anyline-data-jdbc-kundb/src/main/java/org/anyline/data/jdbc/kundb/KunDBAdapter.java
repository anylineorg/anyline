package org.anyline.data.jdbc.kundb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.kundb")
public class KunDBAdapter extends MySQLAdapter {
    
    public DatabaseType type(){
        return DatabaseType.KunDB;
    }
    @Value("${anyline.data.jdbc.delimiter.kundb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
