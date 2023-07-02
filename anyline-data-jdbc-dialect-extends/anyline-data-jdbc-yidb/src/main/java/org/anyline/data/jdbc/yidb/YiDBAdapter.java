package org.anyline.data.jdbc.yidb;

import org.anyline.data.jdbc.tidb.TiDBAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.yidb")
public class YiDBAdapter extends TiDBAdapter {
    
    public DatabaseType type(){
        return DatabaseType.YiDB;
    }
    @Value("${anyline.data.jdbc.delimiter.yidb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
