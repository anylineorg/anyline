package org.anyline.data.jdbc.doris;


import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.doris")
public class DorisAdapter extends MySQLAdapter {

    public DatabaseType typeMetadata(){
        return DatabaseType.Doris;
    }
    @Value("${anyline.data.jdbc.delimiter.doris:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }
}
