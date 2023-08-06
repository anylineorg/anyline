package org.anyline.data.jdbc.util;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.anyline.util.SpringContextUtil;
import org.anyline.util.regular.RegularUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository("anyline.SQLAdapterUtil")
public class SQLAdapterUtil {

	private static final Logger log = LoggerFactory.getLogger(SQLAdapterUtil.class);
	private static ConcurrentHashMap<String, JDBCAdapter> adapters= new ConcurrentHashMap<>();
	private static Map<String, Boolean> supports = new HashMap<>();
	public SQLAdapterUtil(){}
	@Autowired(required = false)
	public void setAdapters(Map<String, JDBCAdapter> map){
		for (JDBCAdapter adapter:map.values()){
			String version = adapter.version();
			if(null == version) {
				adapters.put(adapter.type().name(), adapter);
			}else{
				adapters.put(adapter.type().name() + "_" + adapter.version(), adapter);
			}
			supports.put(adapter.type().name(), true);
		}
	}
	public static boolean support(DatabaseType type){
		if(supports.containsKey(type.name())){
			return true;
		}
		return false;
	}

	private static JDBCAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别

	/**
	 * 定准适配器
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含适配器数据源或客户端
	 * @return JDBCAdapter
	 */
	public static JDBCAdapter getAdapter(String datasource, JdbcTemplate template){
		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.values().iterator().next();
			return defaultAdapter;
		}
		JDBCAdapter adapter = null;
		// 根据 别名
		adapter = adapters.get("al-ds:"+datasource);
		if(null != adapter){
			return adapter;
		}

		DataSource ds = null;
		Connection con = null;
		try {
			String name = null;
			String version = null;
			if(null != template){
				ds = template.getDataSource();
				con = DataSourceUtils.getConnection(ds);
				DatabaseMetaData meta = con.getMetaData();
				name = meta.getDatabaseProductName().toLowerCase().replace(" ", "");
				version = meta.getDatabaseProductVersion();
				//根据jdbc名称+版本号
				adapter = getAdapter(datasource, name, version);
				if(null == adapter) {
					// 根据url中关键字
					adapter = getAdapter(datasource, meta.getURL().toLowerCase(), version);
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
	private static JDBCAdapter getAdapter(String datasource, String name, String version){
		JDBCAdapter adapter = null;
		if(null != version){
			version = version.toLowerCase();
		}

		if(support(DatabaseType.MYSQL) && name.contains("mysql")){
			adapter = adapters.get(DatabaseType.MYSQL.name());
		}else if(support(DatabaseType.MSSQL) && (name.contains("mssql") || name.contains("sqlserver"))){
			if(null != version ){
				version = version.split("\\.")[0];
				double v = BasicUtil.parseDouble(version, 0d);
				String key = null;
				if(v >= 9.0){
					key = "2005";
				}else{
					key = "2000";
				}
				adapter =  adapters.get(DatabaseType.MSSQL.name()+"_"+key);
			}
			if(null == adapter){
				adapter =  adapters.get(DatabaseType.MSSQL.name()+"_2005");
			}
		}else if(support(DatabaseType.ORACLE) && name.contains("oracle")){
			/*Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production
With the Partitioning, OLAP, Data Mining and Real Application Testing options*/
			if(null != version ){
				version = RegularUtil.cut(version, "release","-");
				if(null != version){
					//11.2.0.1.0
					version = version.split("\\.")[0];
				}
				double v = BasicUtil.parseDouble(version, 0d);
				String key = null;
				if(v >= 12.0){
					key = "12";
				}else{
					key = "11";
				}
				adapter =  adapters.get(DatabaseType.ORACLE.name()+"_"+key);
			}
			if(null == adapter) {
				adapter = adapters.get(DatabaseType.ORACLE.name() + "_" + version);
			}
		}else if(support(DatabaseType.PostgreSQL) && name.contains("postgresql")){
			adapter =  adapters.get(DatabaseType.PostgreSQL.name());
		}

		else if(support(DatabaseType.ClickHouse) && name.contains("clickhouse")){
			adapter =  adapters.get(DatabaseType.ClickHouse.name());
		}else if(support(DatabaseType.DB2) && name.contains("db2")){
			adapter =  adapters.get(DatabaseType.DB2.name());
		}else if(support(DatabaseType.Derby) && name.contains("derby")){
			adapter =  adapters.get(DatabaseType.Derby.name());
		}else if(support(DatabaseType.DM) && name.contains("dmdbms")){
			adapter =  adapters.get(DatabaseType.DM.name());
		}else if(support(DatabaseType.HighGo) && name.contains("hgdb") || name.contains("highgo")){
			adapter =  adapters.get(DatabaseType.HighGo.name());
		}else if(support(DatabaseType.KingBase) && name.contains("kingbase")){
			adapter =  adapters.get(DatabaseType.KingBase.name());
		}else if(support(DatabaseType.GBase) && name.contains("gbase")){
			adapter =  adapters.get(DatabaseType.GBase.name());
		}else if(support(DatabaseType.OceanBase) && name.contains("oceanbase")){
			adapter =  adapters.get(DatabaseType.OceanBase.name());
		}else if(support(DatabaseType.OpenGauss) && name.contains("opengauss")){
			adapter =  adapters.get(DatabaseType.OpenGauss.name());
		}else if(support(DatabaseType.PolarDB) && name.contains("polardb")){
			adapter =  adapters.get(DatabaseType.PolarDB.name());
		}else if(support(DatabaseType.SQLite) && name.contains("sqlite")){
			adapter =  adapters.get(DatabaseType.SQLite.name());
		}else if(support(DatabaseType.SQLite) && name.contains("informix")){
			adapter =  adapters.get(DatabaseType.Informix.name());
		}else if(support(DatabaseType.H2) && name.contains(":h2:")){
			adapter =  adapters.get(DatabaseType.H2.name());
		}else if(support(DatabaseType.Hive) && name.contains("hive")){
			adapter =  adapters.get(DatabaseType.H2.name());
		}else if(support(DatabaseType.HSQLDB) && name.contains("hsqldb")){
			adapter =  adapters.get(DatabaseType.HSQLDB.name());
		}else if(support(DatabaseType.TDengine) && name.contains("taos")){
			adapter =  adapters.get(DatabaseType.TDengine.name());
		}else if(support(DatabaseType.Neo4j) && name.contains("neo4j")){
			adapter =  adapters.get(DatabaseType.Neo4j.name());
		}else if(support(DatabaseType.Neo4j) && name.contains("uxdb")){
			adapter =  adapters.get(DatabaseType.UXDB.name());
		}else if(support(DatabaseType.HANA) && name.contains("sap")){
			adapter =  adapters.get(DatabaseType.HANA.name());
		}
		if(null != adapter) {
			adapters.put("al-ds:"+datasource, adapter);
			log.info("[检测数据库适配器][datasource:{}][特征:{}][适配器:{}]",datasource, name, adapter);
		}
		return adapter;
	}
}
