package org.anyline.data.jdbc.oceanbase;

import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.oceanbase")
public class OceanBaseAdapter extends MySQLAdapter {
	
	public DatabaseType type(){
		return DatabaseType.OceanBase;
	} 
	public OceanBaseAdapter(){
		delimiterFr = "`";
		delimiterTo = "`";
	}

	@Value("${anyline.data.jdbc.delimiter.oceanbase:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

} 
