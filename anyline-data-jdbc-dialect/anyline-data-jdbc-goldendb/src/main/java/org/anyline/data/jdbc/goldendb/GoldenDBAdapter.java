package org.anyline.data.jdbc.goldendb;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.goldendb")
public class GoldenDBAdapter extends MySQLAdapter {
	
	public DatabaseType type(){
		return DatabaseType.GoldenDB;
	}
	@Value("${anyline.data.jdbc.delimiter.goldendb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}
} 
