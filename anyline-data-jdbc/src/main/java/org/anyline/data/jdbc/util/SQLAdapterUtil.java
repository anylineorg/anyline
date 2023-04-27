package org.anyline.data.jdbc.util;

import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.adapter.JDBCAdapter;
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

@Repository
public class SQLAdapterUtil {

	private static final Logger log = LoggerFactory.getLogger(SQLAdapterUtil.class);
	private static ConcurrentHashMap<String, JDBCAdapter> adapters= new ConcurrentHashMap<>();
	public SQLAdapterUtil(){}
	@Autowired(required = false)
	public void setAdapters(Map<String, JDBCAdapter> map){
		for (JDBCAdapter adapter:map.values()){
			adapters.put(adapter.type().getCode(), adapter);
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
		if(adapters.containsKey(JDBCAdapter.DB_TYPE.MYSQL.getCode()) && name.contains("mysql")){
			adapter = adapters.get(JDBCAdapter.DB_TYPE.MYSQL.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.MSSQL.getCode()) && (name.contains("mssql") || name.contains("sqlserver"))){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.MSSQL.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.ORACLE.getCode()) && name.contains("oracle")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.ORACLE.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.PostgreSQL.getCode()) && name.contains("postgresql")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.PostgreSQL.getCode());
		}

		else if(adapters.containsKey(JDBCAdapter.DB_TYPE.ClickHouse.getCode()) && name.contains("clickhouse")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.ClickHouse.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.DB2.getCode()) && name.contains("db2")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.DB2.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.Derby.getCode()) && name.contains("derby")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.Derby.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.DM.getCode()) && name.contains("dmdbms")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.DM.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.HighGo.getCode()) && name.contains("hgdb") || name.contains("highgo")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.HighGo.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.KingBase.getCode()) && name.contains("kingbase")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.KingBase.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.OceanBase.getCode()) && name.contains("oceanbase")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.OceanBase.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.PolarDB.getCode()) && name.contains("polardb")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.PolarDB.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.SQLite.getCode()) && name.contains("sqlite")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.SQLite.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.H2.getCode()) && name.contains(":h2:")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.H2.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.HSQLDB.getCode()) && name.contains("hsqldb")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.HSQLDB.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.TDengine.getCode()) && name.contains("taos")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.TDengine.getCode());
		}else if(adapters.containsKey(JDBCAdapter.DB_TYPE.Neo4j.getCode()) && name.contains("neo4j")){
			adapter =  adapters.get(JDBCAdapter.DB_TYPE.Neo4j.getCode());
		}
		if(null != adapter) {
			adapters.put(name, adapter);
			log.warn("[检测数据库适配器][根据url检测完成][url:{}][适配器:{}]", name, adapter);
		}


		return adapter;
	}
}
