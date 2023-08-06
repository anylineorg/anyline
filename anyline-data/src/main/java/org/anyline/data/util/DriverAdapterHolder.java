package org.anyline.data.util;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.LogUtil;
import org.anyline.util.SpringContextUtil;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository("anyline.data.DriverAdapterHolder")
public class DriverAdapterHolder {

	private static final Logger log = LoggerFactory.getLogger(DriverAdapterHolder.class);
	private static ConcurrentHashMap<String, DriverAdapter> adapters= new ConcurrentHashMap<>();
	private static Map<String, Boolean> supports = new HashMap<>();
	private static List<DriverAdapterHolder> utils = new ArrayList<>();
	public DriverAdapterHolder(){}
	@Autowired(required = false)
	public void setAdapters(Map<String, DriverAdapter> map){
		for (DriverAdapter adapter:map.values()){
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

	private static DriverAdapter defaultAdapter = null;	// 如果当前项目只有一个adapter则不需要多次识别

	/**
	 * 定准适配器
	 * @param datasource 数据源名称(配置文件中的key)
	 * @param runtime 运行环境主要包含适配器数据源或客户端
	 * @return DriverAdapter
	 */
	public static DriverAdapter getAdapter(String datasource, DataRuntime runtime){
		if(null != defaultAdapter){
			return defaultAdapter;
		}
		if(adapters.size() ==1){
			defaultAdapter = adapters.values().iterator().next();
			return defaultAdapter;
		}
		DriverAdapter adapter = null;
		// 根据 别名
		adapter = adapters.get("al-ds:"+datasource);
		if(null != adapter){
			return adapter;
		}
		try {
			String feature = runtime.getFeature();
			if(null != runtime){
				//根据特征(先不要加版本号，提取版本号需要建立连接太慢)
				adapter = getAdapter(datasource, feature, null);
				if(null == adapter){
					//根据特征+版本号
					String version = runtime.getVersion();
					adapter = getAdapter(datasource, feature, version);
				}
			}
			if(null == adapter){

			}
			if(null == adapter){
				log.warn("[检测数据库适配器][检测失败][可用适配器数量:{}][检测其他可用的适配器]", adapters.size());
				adapter = SpringContextUtil.getBean(DriverAdapter.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == adapter){
			log.error("[检测数据库适配器][检测其他可用的适配器失败][可用适配器数量:{}][{}]", adapters.size(), LogUtil.format("可能没有依赖anyline-data-jdbc-*(如mysql,neo4j)或没有扫描org.anyline包", 31));
		}
		return adapter;
	}
	private static DriverAdapter getAdapter(){
		return null;
	}

	/**
	 * 检测可支持版本数量
	 * @return boolean
	 */
	private static int versions(DatabaseType type, String ... versions){
		int qty = 0;
		for(String version:versions){
			if(adapters.containsKey(type.name()+"_"+version)){
				qty ++;
			}
		}
		return qty;
	}

	/**
	 * 取第一个存在的版本
	 * @param type DatabaseType
	 * @param versions 版本号
	 * @return adapter
	 */
	private static DriverAdapter adapter(DatabaseType type, String ... versions){
		for(String version:versions){
			DriverAdapter adapter = adapters.get(type.name() + "_" + version);
			if(null != adapter){
				return adapter;
			}
		}
		return null;
	}

	/**
	 * 根据数据源特征定位适配器
	 * @param datasource 数据源key
	 * @param feature 特征
	 * @param version 版本
	 * @return 适配器
	 */
	private static DriverAdapter getAdapter(String datasource, String feature, String version){
		DriverAdapter adapter = null;
		if(null != version){
			version = version.toLowerCase();
		}

		if(support(DatabaseType.MYSQL) && feature.contains("mysql")){
			adapter = adapters.get(DatabaseType.MYSQL.name());
		}else if(support(DatabaseType.MSSQL) && (feature.contains("mssql") || feature.contains("sqlserver"))){
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
			}else{
				//如果没有提供版本号并且环境中只有一个版本
				if(versions(DatabaseType.ORACLE, "2000", "2005") == 1 ){
					adapter = adapter(DatabaseType.MSSQL, "2005", "2000");
				}
			}

			if(null == adapter){
				adapter =  adapters.get(DatabaseType.MSSQL.name()+"_2005");
			}
		}else if(support(DatabaseType.ORACLE) && feature.contains("oracle")){
			/*Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production With the Partitioning, OLAP, Data Mining and Real Application Testing options*/
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
			}else{
				//如果没有提供版本号并且环境中只有一个版本
				if(versions(DatabaseType.ORACLE, "11", "12") == 1 ){
					adapter = adapter(DatabaseType.ORACLE, "11", "12");
				}
			}
			if(null == adapter) {
				adapter = adapters.get(DatabaseType.ORACLE.name() + "_" + version);
			}
		}else if(support(DatabaseType.PostgreSQL) && feature.contains("postgresql")){
			adapter =  adapters.get(DatabaseType.PostgreSQL.name());
		}

		else if(support(DatabaseType.ClickHouse) && feature.contains("clickhouse")){
			adapter =  adapters.get(DatabaseType.ClickHouse.name());
		}else if(support(DatabaseType.DB2) && feature.contains("db2")){
			adapter =  adapters.get(DatabaseType.DB2.name());
		}else if(support(DatabaseType.Derby) && feature.contains("derby")){
			adapter =  adapters.get(DatabaseType.Derby.name());
		}else if(support(DatabaseType.DM) && feature.contains("dmdbms")){
			adapter =  adapters.get(DatabaseType.DM.name());
		}else if(support(DatabaseType.HighGo) && feature.contains("hgdb") || feature.contains("highgo")){
			adapter =  adapters.get(DatabaseType.HighGo.name());
		}else if(support(DatabaseType.KingBase) && feature.contains("kingbase")){
			adapter =  adapters.get(DatabaseType.KingBase.name());
		}else if(support(DatabaseType.GBase) && feature.contains("gbase")){
			adapter =  adapters.get(DatabaseType.GBase.name());
		}else if(support(DatabaseType.OceanBase) && feature.contains("oceanbase")){
			adapter =  adapters.get(DatabaseType.OceanBase.name());
		}else if(support(DatabaseType.OpenGauss) && feature.contains("opengauss")){
			adapter =  adapters.get(DatabaseType.OpenGauss.name());
		}else if(support(DatabaseType.PolarDB) && feature.contains("polardb")){
			adapter =  adapters.get(DatabaseType.PolarDB.name());
		}else if(support(DatabaseType.SQLite) && feature.contains("sqlite")){
			adapter =  adapters.get(DatabaseType.SQLite.name());
		}else if(support(DatabaseType.SQLite) && feature.contains("informix")){
			adapter =  adapters.get(DatabaseType.Informix.name());
		}else if(support(DatabaseType.H2) && feature.contains(":h2:")){
			adapter =  adapters.get(DatabaseType.H2.name());
		}else if(support(DatabaseType.Hive) && feature.contains("hive")){
			adapter =  adapters.get(DatabaseType.H2.name());
		}else if(support(DatabaseType.HSQLDB) && feature.contains("hsqldb")){
			adapter =  adapters.get(DatabaseType.HSQLDB.name());
		}else if(support(DatabaseType.TDengine) && feature.contains("taos")){
			adapter =  adapters.get(DatabaseType.TDengine.name());
		}else if(support(DatabaseType.Neo4j) && feature.contains("neo4j")){
			adapter =  adapters.get(DatabaseType.Neo4j.name());
		}else if(support(DatabaseType.Neo4j) && feature.contains("uxdb")){
			adapter =  adapters.get(DatabaseType.UXDB.name());
		}else if(support(DatabaseType.HANA) && feature.contains("sap")){
			adapter =  adapters.get(DatabaseType.HANA.name());
		}else if(support(DatabaseType.MongoDB) && feature.contains("mongo")){
			adapter =  adapters.get(DatabaseType.MongoDB.name());
		}
		if(null != adapter) {
			adapters.put("al-ds:"+datasource, adapter);
			log.info("[检测数据库适配器][datasource:{}][特征:{}][适配器:{}]",datasource, feature, adapter);
		}
		return adapter;
	}
}
