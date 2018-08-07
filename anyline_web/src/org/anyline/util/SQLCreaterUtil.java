package org.anyline.util;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.anyline.config.db.SQLCreater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class SQLCreaterUtil {

	private static JdbcTemplate jdbc;
	private static ConcurrentHashMap<String,SQLCreater> creaters= new ConcurrentHashMap<String,SQLCreater>();
	@Autowired  
    public void setJdbc(JdbcTemplate jdbc) {  
		SQLCreaterUtil.jdbc = jdbc;  
    }  
	public static SQLCreater getCreater(){
		SQLCreater creater = null;
		try {
			String clazz = jdbc.getDataSource().getConnection().toString().toLowerCase();
			SQLCreater.DB_TYPE type = SQLCreater.DB_TYPE.MYSQL;
			if(clazz.contains("mysql")){
				type = SQLCreater.DB_TYPE.MYSQL;
			}else if(clazz.contains("mssql") || clazz.contains("sqlserver")){
				type = SQLCreater.DB_TYPE.MSSQL;
			}else if(clazz.contains("oracle")){
				type = SQLCreater.DB_TYPE.ORACLE;
			}else if(clazz.contains("db2")){
				type = SQLCreater.DB_TYPE.DB2;
			}
			creater = creaters.get(clazz);
			if(null == creater){
				Map<String,SQLCreater> creaters = SpringContextUtil.getBeans(SQLCreater.class);
				for(SQLCreater item: creaters.values()){
					if(type ==item.type()){
						creater = item;
						creaters.put(type.getName(), creater);
						break;
					}
				}
			}
			if(null == creater){
				creater = SpringContextUtil.getBean(SQLCreater.class);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return creater;
	}
}
