package org.anyline.config.db.ds;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    // 保存动态创建的数据源
    private static final Map<String,DataSource> dataSources = new HashMap<String,DataSource>();
	/**
	 * 获取当前线程数据源
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceHolder.getDataSource();
	}
	public static void addDataSource(String key, DataSource ds) {
		dataSources.put(key, ds);
    }

	@Override
	protected DataSource determineTargetDataSource() {
		DataSource dataSource = null;
		Object lookupKey = determineCurrentLookupKey();
		dataSource = dataSources.get(lookupKey);
		if(null == dataSource){
			try{
				dataSource = super.determineTargetDataSource();
			}catch(Exception e){
				
			}
		}
		return dataSource;
	}
}