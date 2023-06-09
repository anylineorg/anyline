package org.anyline.data.jdbc.cudb;

import org.anyline.data.jdbc.opengauss.OpenGaussAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.cudb")
public class ChinaUnicomDBAdapter extends OpenGaussAdapter {
    
    public DatabaseType type(){
        return DatabaseType.ChinaUnicomDB;
    }
    @Value("${anyline.data.jdbc.delimiter.cudb:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
