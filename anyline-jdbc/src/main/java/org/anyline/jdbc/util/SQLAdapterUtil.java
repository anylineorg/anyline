package org.anyline.jdbc.util;

import org.anyline.jdbc.adapter.JDBCAdapter;
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
public class SQLAdapterUtil {

	private static ConcurrentHashMap<String, JDBCAdapter> adapters= new ConcurrentHashMap<>();
	public SQLAdapterUtil(){}
	@Autowired(required = false)
	public void setAdapters(Map<String, JDBCAdapter> map){
		for (JDBCAdapter adapter:map.values()){
			adapters.put(adapter.type().getCode(), adapter);
		}
	}

	private static JDBCAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别
	public static JDBCAdapter getAdapter(JdbcTemplate jdbc){


		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.values().iterator().next();
			return defaultAdapter;
		}
		JDBCAdapter adapter = null;
		JDBCAdapter.DB_TYPE type = DataSourceHolder.dialect();
		if(null != type){
			// 根据 别名
			adapter = getAdapter(type.getName());
			if(null != adapter){
				return adapter;
			}
		}

		DataSource ds = null;
		Connection con = null;
		try {
			if(null != jdbc){
				ds = jdbc.getDataSource();
				con = DataSourceUtils.getConnection(ds);
				String name = con.getMetaData().getDatabaseProductName().toLowerCase().replace(" ", "");
				name += con.getMetaData().getURL().toLowerCase();
				// 根据url中关键字
				adapter = getAdapter(name);

			}
			if(null == adapter){
				adapter = SpringContextUtil.getBean(JDBCAdapter.class);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return adapter;
	}
	private static JDBCAdapter getAdapter(String name){
		JDBCAdapter adapter = null;
		adapter = adapters.get(name);
		if(null != adapter){
			return adapter;
		}
		if(name.contains("mysql")){
			adapter = adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.MYSQL.getCode());
		}else if(name.contains("mssql") || name.contains("sqlserver")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.MSSQL.getCode());
		}else if(name.contains("oracle")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.ORACLE.getCode());
		}else if(name.contains("postgresql")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.PostgreSQL.getCode());
		}

		else if(name.contains("clickhouse")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.ClickHouse.getCode());
		}else if(name.contains("db2")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.DB2.getCode());
		}else if(name.contains("derby")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.Derby.getCode());
		}else if(name.contains("dmdbms")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.DM.getCode());
		}else if(name.contains("hgdb") || name.contains("highgo")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.HighGo.getCode());
		}else if(name.contains("kingbase")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.KingBase.getCode());
		}else if(name.contains("oceanbase")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.OceanBase.getCode());
		}else if(name.contains("polardb")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.PolarDB.getCode());
		}else if(name.contains("sqlite")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.SQLite.getCode());
		}else if(name.contains(":h2:")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.H2.getCode());
		}else if(name.contains("hsqldb")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.HSQLDB.getCode());
		}else if(name.contains("taos")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.TDengine.getCode());
		}else if(name.contains("neo4j")){
			adapter =  adapters.get(org.anyline.jdbc.adapter.JDBCAdapter.DB_TYPE.Neo4j.getCode());
		}
		adapters.put(name, adapter);
		return adapter;
	}
}
