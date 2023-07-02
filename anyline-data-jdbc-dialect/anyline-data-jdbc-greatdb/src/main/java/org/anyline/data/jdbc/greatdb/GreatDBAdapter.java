package org.anyline.data.jdbc.greatdb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.greatdb")
public class GreatDBAdapter extends MySQLAdapter {
	
	public DatabaseType type(){
		return DatabaseType.GreatDB;
	}
	@Value("${anyline.data.jdbc.delimiter.greatdb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}
} 
