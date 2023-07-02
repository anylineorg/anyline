package org.anyline.data.jdbc.hotdb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.hotdb")
public class HotDBAdapter extends MySQLAdapter {
    
    public DatabaseType type(){
        return DatabaseType.HotDB;
    }
    @Value("${anyline.data.jdbc.delimiter.hotdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
