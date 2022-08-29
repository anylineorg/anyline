package org.anyline.jdbc.util;

import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SQLCreaterUtil {

	private static ConcurrentHashMap<String, SQLCreater> creaters= new ConcurrentHashMap<>();

	@Autowired(required = false)
	public void setCreaters(Map<String, SQLCreater> map){
		for (SQLCreater creater:map.values()){
			creaters.put(creater.type().getCode(), creater);
		}
	}

	private static SQLCreater defaultCreater = null;	//如果当前项目只有一个creater则不需要多次识别
	public static SQLCreater getCreater(JdbcTemplate jdbc){

		String ds = DataSourceHolder.getDataSource();

		if(null != defaultCreater){
			return defaultCreater;
		}
		if(creaters.size() ==1){
			defaultCreater = creaters.values().iterator().next();
			return defaultCreater;
		}
		SQLCreater creater = null;
		try {
			if(null != jdbc){
				DataSource ds = jdbc.getDataSource();
				Connection con = DataSourceUtils.getConnection(ds);
				String name = con.getMetaData().getDatabaseProductName().toLowerCase().replace(" ", "");
				name += con.getMetaData().getURL().toLowerCase();
				if(!DataSourceUtils.isConnectionTransactional(con, ds)){
					DataSourceUtils.releaseConnection(con, ds);
				}
				creater = creaters.get(name);
				if(null != creater){
					return creater;
				}
				if(name.contains("mysql")){
					creater = creaters.get(SQLCreater.DB_TYPE.MYSQL.getCode());
				}else if(name.contains("mssql") || name.contains("sqlserver")){
					creater =  creaters.get(SQLCreater.DB_TYPE.MSSQL.getCode());
				}else if(name.contains("oracle")){
					creater =  creaters.get(SQLCreater.DB_TYPE.ORACLE.getCode());
				}else if(name.contains("postgresql")){
					creater =  creaters.get(SQLCreater.DB_TYPE.PostgreSQL.getCode());
				}

				else if(name.contains("clickhouse")){
					creater =  creaters.get(SQLCreater.DB_TYPE.ClickHouse.getCode());
				}else if(name.contains("db2")){
					creater =  creaters.get(SQLCreater.DB_TYPE.DB2.getCode());
				}else if(name.contains("derby")){
					creater =  creaters.get(SQLCreater.DB_TYPE.Derby.getCode());
				}else if(name.contains("dmdbms")){
					creater =  creaters.get(SQLCreater.DB_TYPE.DM.getCode());
				}else if(name.contains("hgdb") || name.contains("highgo")){
					creater =  creaters.get(SQLCreater.DB_TYPE.HighGo.getCode());
				}else if(name.contains("kingbase")){
					creater =  creaters.get(SQLCreater.DB_TYPE.KingBase.getCode());
				}else if(name.contains("oceanbase")){
					creater =  creaters.get(SQLCreater.DB_TYPE.OceanBase.getCode());
				}else if(name.contains("polardb")){
					creater =  creaters.get(SQLCreater.DB_TYPE.PolarDB.getCode());
				}else if(name.contains("sqlite")){
					creater =  creaters.get(SQLCreater.DB_TYPE.SQLite.getCode());
				}else if(name.contains(":h2:")){
					creater =  creaters.get(SQLCreater.DB_TYPE.H2.getCode());
				}else if(name.contains("hsqldb")){
					creater =  creaters.get(SQLCreater.DB_TYPE.HSQLDB.getCode());
				}else if(name.contains("taos")){
					creater =  creaters.get(SQLCreater.DB_TYPE.TDengine.getCode());
				}
				creaters.put(name, creater);
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
