package org.anyline.data.jdbc.ubisql;

import org.anyline.data.jdbc.tidb.TiDBAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.ubisql")
public class UbiSQLAdapter extends TiDBAdapter {
    
    public DatabaseType type(){
        return DatabaseType.UbiSQL;
    }
    @Value("${anyline.data.jdbc.delimiter.ubisql:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
