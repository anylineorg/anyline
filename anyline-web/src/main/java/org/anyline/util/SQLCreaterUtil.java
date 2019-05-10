package org.anyline.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.anyline.config.db.SQLCreater;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
@Repository
public class SQLCreaterUtil {

	private static ConcurrentHashMap<String,SQLCreater> creaters= new ConcurrentHashMap<String,SQLCreater>();
	private static SQLCreater defaultCreater = null;	//如果当前项目只有一个creater则不需要多次识别
	public static SQLCreater getCreater(JdbcTemplate jdbc){
		if(null != defaultCreater){
			return defaultCreater;
		}
		if(SpringContextUtil.getBeans(SQLCreater.class).size() ==1){
			defaultCreater = SpringContextUtil.getBean(SQLCreater.class);
			return defaultCreater;
		}
		SQLCreater creater = null;
		try {
			if(null != jdbc){
				DataSource ds = jdbc.getDataSource();
				Connection con = DataSourceUtils.getConnection(ds);
				String name = con.getMetaData().getDatabaseProductName().toLowerCase().replace(" ", "");
				if(!DataSourceUtils.isConnectionTransactional(con, ds)){
					DataSourceUtils.releaseConnection(con, ds);
				}
				SQLCreater.DB_TYPE type = SQLCreater.DB_TYPE.MYSQL;
				if(name.contains("mysql")){
					type = SQLCreater.DB_TYPE.MYSQL;
				}else if(name.contains("mssql") || name.contains("sqlserver")){
					type = SQLCreater.DB_TYPE.MSSQL;
				}else if(name.contains("oracle")){
					type = SQLCreater.DB_TYPE.ORACLE;
				}else if(name.contains("db2")){
					type = SQLCreater.DB_TYPE.DB2;
				}
				creater = creaters.get(name);
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
