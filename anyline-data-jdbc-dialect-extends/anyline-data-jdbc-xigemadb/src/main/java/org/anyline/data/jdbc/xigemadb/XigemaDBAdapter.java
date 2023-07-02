package org.anyline.data.jdbc.xigemadb;

import org.anyline.data.jdbc.informix.InformixAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.xigemadb")
public class XigemaDBAdapter extends InformixAdapter {
    
    public DatabaseType type(){
        return DatabaseType.xigemaDB;
    }
    @Value("${anyline.data.jdbc.delimiter.xigemadb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
