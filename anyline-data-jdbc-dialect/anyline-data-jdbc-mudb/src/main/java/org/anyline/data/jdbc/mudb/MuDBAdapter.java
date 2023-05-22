package org.anyline.data.jdbc.mudb;

import org.anyline.data.jdbc.opengauss.OpenGaussAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.mudb")
public class MuDBAdapter extends OpenGaussAdapter {
    
    public DatabaseType type(){
        return DatabaseType.MuDB;
    }
    @Value("${anyline.data.jdbc.delimiter.mudb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
