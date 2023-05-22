package org.anyline.data.jdbc.cmdb;

import org.anyline.data.jdbc.opengauss.OpenGaussAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.cmdb")
public class ChinaMobileDBAdapter extends OpenGaussAdapter {
    
    public DatabaseType type(){
        return DatabaseType.ChinaMobileDB;
    }
    @Value("${anyline.data.jdbc.delimiter.cmdb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
