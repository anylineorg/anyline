package org.anyline.data.jdbc.sinodb;

import org.anyline.data.jdbc.tidb.TiDBAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.sinodb")
public class SinoDBAdapter extends TiDBAdapter {
    
    public DatabaseType type(){
        return DatabaseType.SinoDB;
    }
    @Value("${anyline.data.jdbc.delimiter.sinodb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
