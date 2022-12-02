package org.anyline.data.jdbc.ds;

import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
 
public class DynamicDataSource extends AbstractRoutingDataSource { 
	private Logger log = LoggerFactory.getLogger(DynamicDataSource.class); 
    // 保存动态创建的数据源
    private static final Map<String,DataSource> dataSources = new HashMap<String,DataSource>();
	private static DataSource defaultDatasource;
	/** 
	 * 获取当前线程数据源 
	 */ 
	@Override 
	protected Object determineCurrentLookupKey() { 
		return DataSourceHolder.getDataSource(); 
	} 
	public static void addDataSource(String key, DataSource ds) { 
		dataSources.put(key, ds);
		reg(key,ds);
    }
	public static void setDefaultDatasource(DataSource ds){
		defaultDatasource = ds;
		reg("dataSource",ds);
	}
	public static DataSource getDefaultDatasource(){
		return defaultDatasource;
	}
	public static DataSource getDatasource(String key){
		DataSource ds = dataSources.get(key);
		return ds;
	}
	public static Map<String,DataSource> getDataSources(){
		return dataSources;
	}
	private static void reg(String key, DataSource ds){
		//注意 解析配置文件时 上下文还没有初始化完成
		ApplicationContext context = SpringContextUtil.getApplicationContext();
		if(null != context && !context.containsBean(key)){
			((ConfigurableApplicationContext)context).getBeanFactory().registerSingleton(key, ds);
		}
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
		if(null == dataSource){ 
			log.error("[获取数据源失败][thread:{}][key:{}]",Thread.currentThread().getId(), lookupKey); 
		} 
		return dataSource; 
	} 
}
