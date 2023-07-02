package org.anyline.data.jdbc.vastbase;

import org.anyline.data.jdbc.opengauss.OpenGaussAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.vastbase")
public class VastbaseAdapter extends OpenGaussAdapter {
    
    public DatabaseType type(){
        return DatabaseType.Vastbase;
    }
    @Value("${anyline.data.jdbc.delimiter.vastbase:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
} 
