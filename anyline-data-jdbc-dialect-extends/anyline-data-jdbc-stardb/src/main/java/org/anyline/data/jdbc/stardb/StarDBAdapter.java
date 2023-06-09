package org.anyline.data.jdbc.stardb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.stardb")
public class StarDBAdapter extends MySQLAdapter {
    
    public DatabaseType type(){
        return DatabaseType.StarDB;
    }
    @Value("${anyline.data.jdbc.delimiter.stardb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
