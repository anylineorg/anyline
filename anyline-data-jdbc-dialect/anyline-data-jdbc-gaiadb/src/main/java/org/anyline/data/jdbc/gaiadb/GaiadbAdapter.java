package org.anyline.data.jdbc.gaiadb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.gaiadb")
public class GaiadbAdapter extends MySQLAdapter {
    
    public DatabaseType type(){
        return DatabaseType.GaiaDB;
    }
    @Value("${anyline.data.jdbc.delimiter.gaiadb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
