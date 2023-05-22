package org.anyline.data.jdbc.polardb;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.SQLAdapter;
import org.anyline.data.jdbc.postgresql.PostgresqlAdapter;
import org.anyline.data.run.Run;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.polardb")
public class PolarAdapter extends PostgresqlAdapter {
	
	public DatabaseType type(){
		return DatabaseType.PolarDB;
	}
	@Value("${anyline.data.jdbc.delimiter.polardb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}
} 
