package org.anyline.data.jdbc.util;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.entity.data.DatabaseType;
import org.anyline.util.LogUtil;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository("anyline.SQLAdapterUtil")
public class SQLAdapterUtil {

	private static final Logger log = LoggerFactory.getLogger(SQLAdapterUtil.class);
	private static ConcurrentHashMap<String, JDBCAdapter> adapters= new ConcurrentHashMap<>();
	public SQLAdapterUtil(){}
	@Autowired(required = false)
	public void setAdapters(Map<String, JDBCAdapter> map){
		for (JDBCAdapter adapter:map.values()){
			adapters.put(adapter.type().name(), adapter);
		}
	}

	private static JDBCAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别

	public static JDBCAdapter getAdapter(JdbcTemplate template){
		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.values().iterator().next();
			return defaultAdapter;
		}
		JDBCAdapter adapter = null;
		DatabaseType type = DataSourceHolder.dialect();
		if(null != type){
			// 根据 别名
			adapter = getAdapter(type.name());
			if(null != adapter){
				return adapter;
			}
		}

		DataSource ds = null;
		Connection con = null;
		try {
			String name = null;
			if(null != template){
				ds = template.getDataSource();
				con = DataSourceUtils.getConnection(ds);
				DatabaseMetaData meta = con.getMetaData();
				name = meta.getDatabaseProductName().toLowerCase().replace(" ", "");
				adapter = getAdapter(name);
				if(null == adapter) {
					// 根据url中关键字
					adapter = getAdapter(meta.getURL().toLowerCase());
				}
			}
			if(null == adapter){
				log.warn("[检测数据库适配器][检测失败][可用适配器数量:{}][检测其他可用的适配器]", adapters.size());
				adapter = SpringContextUtil.getBean(JDBCAdapter.class);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		if(null == adapter){
			log.error("[检测数据库适配器][检测其他可用的适配器失败][可用适配器数量:{}][{}]", adapters.size(), LogUtil.format("可能没有依赖anyline-data-jdbc-*(如mysql,neo4j)或没有扫描org.anyline包", 31));
		}
		return adapter;
	}
	private static JDBCAdapter getAdapter(String name){
		JDBCAdapter adapter = null;
		adapter = adapters.get(name);
		if(null != adapter){
			return adapter;
		}
		if(adapters.containsKey(DatabaseType.MYSQL.name()) && name.contains("mysql")){
			adapter = adapters.get(DatabaseType.MYSQL.name());
		}else if(adapters.containsKey(DatabaseType.MSSQL.name()) && (name.contains("mssql") || name.contains("sqlserver"))){
			adapter =  adapters.get(DatabaseType.MSSQL.name());
		}else if(adapters.containsKey(DatabaseType.ORACLE.name()) && name.contains("oracle")){
			adapter =  adapters.get(DatabaseType.ORACLE.name());
		}else if(adapters.containsKey(DatabaseType.PostgreSQL.name()) && name.contains("postgresql")){
			adapter =  adapters.get(DatabaseType.PostgreSQL.name());
		}

		else if(adapters.containsKey(DatabaseType.ClickHouse.name()) && name.contains("clickhouse")){
			adapter =  adapters.get(DatabaseType.ClickHouse.name());
		}else if(adapters.containsKey(DatabaseType.DB2.name()) && name.contains("db2")){
			adapter =  adapters.get(DatabaseType.DB2.name());
		}else if(adapters.containsKey(DatabaseType.Derby.name()) && name.contains("derby")){
			adapter =  adapters.get(DatabaseType.Derby.name());
		}else if(adapters.containsKey(DatabaseType.DM.name()) && name.contains("dmdbms")){
			adapter =  adapters.get(DatabaseType.DM.name());
		}else if(adapters.containsKey(DatabaseType.HighGo.name()) && name.contains("hgdb") || name.contains("highgo")){
			adapter =  adapters.get(DatabaseType.HighGo.name());
		}else if(adapters.containsKey(DatabaseType.KingBase.name()) && name.contains("kingbase")){
			adapter =  adapters.get(DatabaseType.KingBase.name());
		}else if(adapters.containsKey(DatabaseType.GBase.name()) && name.contains("gbase")){
			adapter =  adapters.get(DatabaseType.GBase.name());
		}else if(adapters.containsKey(DatabaseType.OceanBase.name()) && name.contains("oceanbase")){
			adapter =  adapters.get(DatabaseType.OceanBase.name());
		}else if(adapters.containsKey(DatabaseType.OpenGauss.name()) && name.contains("opengauss")){
			adapter =  adapters.get(DatabaseType.OpenGauss.name());
		}else if(adapters.containsKey(DatabaseType.PolarDB.name()) && name.contains("polardb")){
			adapter =  adapters.get(DatabaseType.PolarDB.name());
		}else if(adapters.containsKey(DatabaseType.SQLite.name()) && name.contains("sqlite")){
			adapter =  adapters.get(DatabaseType.SQLite.name());
		}else if(adapters.containsKey(DatabaseType.H2.name()) && name.contains(":h2:")){
			adapter =  adapters.get(DatabaseType.H2.name());
		}else if(adapters.containsKey(DatabaseType.HSQLDB.name()) && name.contains("hsqldb")){
			adapter =  adapters.get(DatabaseType.HSQLDB.name());
		}else if(adapters.containsKey(DatabaseType.TDengine.name()) && name.contains("taos")){
			adapter =  adapters.get(DatabaseType.TDengine.name());
		}else if(adapters.containsKey(DatabaseType.Neo4j.name()) && name.contains("neo4j")){
			adapter =  adapters.get(DatabaseType.Neo4j.name());
		}
		if(null != adapter) {
			adapters.put(name, adapter);
			log.warn("[检测数据库适配器][根据url检测完成][url:{}][适配器:{}]", name, adapter);
		}


		return adapter;
	}
}
