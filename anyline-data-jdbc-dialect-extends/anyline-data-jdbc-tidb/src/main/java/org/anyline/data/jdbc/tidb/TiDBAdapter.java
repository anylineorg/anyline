package org.anyline.data.jdbc.tidb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.tidb")
public class TiDBAdapter extends MySQLAdapter {
    
    public DatabaseType type(){
        return DatabaseType.TiDB;
    }
    @Value("${anyline.data.jdbc.delimiter.tidb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
