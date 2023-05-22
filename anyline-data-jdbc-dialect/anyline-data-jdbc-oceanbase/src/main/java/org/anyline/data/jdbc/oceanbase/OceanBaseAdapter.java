package org.anyline.data.jdbc.oceanbase;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.SQLAdapter;
import org.anyline.data.jdbc.mysql.MySQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.object.MappingSqlQuery;
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
