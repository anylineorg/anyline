package org.anyline.data.jdbc.lightdb;

import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.lightdb")
public class LightDBAdapter extends PostgresqlAdapter {
	
	public DatabaseType type(){
		return DatabaseType.LightDB;
	}
	@Value("${anyline.data.jdbc.delimiter.lightdb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}
} 
