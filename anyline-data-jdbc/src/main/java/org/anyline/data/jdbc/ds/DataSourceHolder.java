/*  
 * Copyright 2006-2023 www.anyline.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 *           
 */ 
 
 
package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.EntityAdapterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;

public class DataSourceHolder { 
	public static Logger log = LoggerFactory.getLogger(DataSourceHolder.class); 
	// 切换前数据源 
    private static final ThreadLocal<String> THREAD_RECALL_SOURCE = new ThreadLocal<String>(); 
	// 当前数据源 
    private static final ThreadLocal<String> THREAD_CUR_SOURCE = new ThreadLocal<String>(); 
    // 是否还原默认数据源,执行一次操作后还原回  切换之前的数据源
    private static final ThreadLocal<Boolean> THREAD_AUTO_RECOVER = new ThreadLocal<Boolean>(); 
    private static List<String> dataSources = new ArrayList<>();
	private static Map<String, JDBCAdapter.DB_TYPE> types = new HashMap<>();
    static{ 
    	THREAD_AUTO_RECOVER.set(false); 
    } 
    public static String getDataSource() { 
        return THREAD_CUR_SOURCE.get(); 
    }

	public static JDBCAdapter.DB_TYPE dialect(){
		String ds = getDataSource();
		return types.get(ds);
	}
	public static void dialect(String ds, JDBCAdapter.DB_TYPE type){
		types.put(ds, type);
	}

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 */
	public static void setDataSource(String dataSource) {
		setDataSource(dataSource, false);
    }

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 * @param auto 执行完后切换回原来的数据库
	 */
    public static void setDataSource(String dataSource, boolean auto) {
		if(null == dataSource || !dataSources.contains(dataSource)){
			throw new RuntimeException("数据源未注册:"+dataSource);
		}
    	if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){ 
    		log.warn("[切换数据源][thread:{}][数据源:{}>{}][auto recover:{}]", Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get(), dataSource, auto);
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_RECOVER.set(auto); 
    } 
    // 恢复切换前数据源 
    public static void recoverDataSource(){ 
    	THREAD_CUR_SOURCE.set(THREAD_RECALL_SOURCE.get()); 
    } 
    public static void setDefaultDataSource(){ 
    	clearDataSource();
		if(dataSources.contains("dataSource")){
			setDataSource("dataSource");
		}else if(dataSources.contains("default")){
			setDataSource("default");
		}
    	THREAD_AUTO_RECOVER.set(false);
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
			log.warn("[切换数据源][thread:{}][数据源:{}>默认数据源]",Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get());
		}
	}
    public static void clearDataSource() { 
    	THREAD_CUR_SOURCE.remove(); 
    } 
    public static boolean isAutoDefault(){ 
    	if(null == THREAD_AUTO_RECOVER || null == THREAD_AUTO_RECOVER.get()){ 
    		return false; 
    	} 
    	return THREAD_AUTO_RECOVER.get(); 
    } 
 
	/** 
	 * 解析数据源,并返回修改后的SQL 
	 * &lt;mysql_ds&gt;crm_user 
	 * @param src  src
	 * @return String
	 */ 
	public static String parseDataSource(String src){
		if(null != src && src.startsWith("<")){ 
			int fr = src.indexOf("<"); 
			int to = src.indexOf(">"); 
			if(fr != -1){ 
				String ds = src.substring(fr+1,to); 
				src = src.substring(to+1); 
				setDataSource(ds, true); 
			} 
		} 
		return src;
	}
	public static String parseDataSource(String dest, Object obj){
		if(BasicUtil.isNotEmpty(dest) || null == obj){
			return parseDataSource(dest);
		}
		String result = "";
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			String link = row.getDataLink();
			if(BasicUtil.isNotEmpty(link)){
				DataSourceHolder.setDataSource(link, true);
			}
			result = row.getDataSource();
		}else if(obj instanceof DataSet){
			DataSet set = (DataSet)obj;
			if(set.size()>0){
				result = parseDataSource(dest, set.getRow(0));
			}
		} else if (obj instanceof Collection) {
			Object first = ((Collection)obj).iterator().next();
			result = EntityAdapterProxy.table(first.getClass());
		} else{
			result = EntityAdapterProxy.table(obj.getClass());
		}
		result = parseDataSource(result);
		return result;
	}
	/**
	 * 注册新的数据源,只是把spring context中现有的数据源名称添加到数据源名称列表
	 * @param ds 数据源名称
	 */
	public static void reg(String ds){ 
		if(!dataSources.contains(ds)){ 
			dataSources.add(ds); 
		} 
	}

	/**
	 * 数据源列表中是否已包含指定数据源
	 * @param ds 数据源名称
	 * @return boolean
	 */
	public static boolean contains(String ds){ 
		return dataSources.contains(ds); 
	}

	/**
	 * 注册数据源
	 * @param key 数据源名称
	 * @param ds 数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public static DataSource addDataSource(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds,true);
	}
	public static DataSource addDataSource(DataSource ds) throws Exception{
		return addDefaultDataSource( ds);
	}
	public static DataSource addDefaultDataSource(DataSource ds) throws Exception{
		return addDataSource("dataSource", ds,true);
	}


	/**
	 * 注册数据源
	 * @param key 数据源名称
	 * @param ds 数据源
	 * @param over 是否允许覆盖已有的数据源
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public static DataSource addDataSource(String key, DataSource ds, boolean over) throws Exception{
		if(!over && dataSources.contains(key)){
			throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]");
		}
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
			log.warn("[创建数据源][thread:{}][key:{}]",Thread.currentThread().getId(), key);
		}
		DynamicDataSource.addDataSource(key, ds);
		if(!dataSources.contains(key)) {
			dataSources.add(key);
		}
		return ds;
	}

	/**
	 *
	 * @param key 切换数据源依据 默认key=dataSource
	 * @param pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
	 * @param driver 驱动类 如 com.mysql.cj.jdbc.Driver
	 * @param url url
	 * @param user 用户名
	 * @param password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	public static DataSource reg(String key, String pool, String driver, String url, String user, String password) throws Exception{
		Map<String,String> param = new HashMap<String,String>();
		param.put("pool", pool);
		param.put("driver", driver);
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		DataSource ds = buildDataSource(param);
		return reg(key, ds, true);
	}
	public static DataSource reg(String key, JDBCAdapter.DB_TYPE type, String url, String user, String password) throws Exception{
		return reg(key, "om.zaxxer.hikari.HikariDataSource", type.getDriver(), url, user, password);
	}
	public static DataSource reg(String key, DataSource ds, boolean over) throws Exception{
		return addDataSource(key, ds, over);
	}
	public static DataSource reg(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds, true);
	}

	public static DataSource reg(String key, Map<String,?> param, boolean over) throws Exception{
		return addDataSource(key, buildDataSource(param), over);
	}
	public static DataSource reg(String key, Map<String,?> param) throws Exception{
		return addDataSource(key, buildDataSource(param), true);
	}

	/**
	 * 创建数据源
	 * @param params 数据源参数
	 * 	  pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
	 * 	  driver 驱动类 如 com.mysql.cj.jdbc.Driver
	 * 	  url url
	 * 	  user 用户名
	 * 	  password 密码
	 * @return DataSource
	 * @throws Exception 异常 Exception
	 */
	@SuppressWarnings("unchecked")
	public static DataSource buildDataSource(Map<String, ?> params) throws Exception{
        try {
            String pool = (String)params.get("pool");
			if(BasicUtil.isEmpty(pool)){
				pool = (String)params.get("type");
			}
            if (pool == null) {
                throw new Exception("未设置数据源类型(如:pool=com.zaxxer.hikari.HikariDataSource)");
            }
            Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName((String) pool);
            Object driver =  BeanUtil.propertyNvl(params,"driver","driver-class","driver-class-name");
			Object url =  BeanUtil.propertyNvl(params,"url","jdbc-url");
			Object user =  BeanUtil.propertyNvl(params,"user","username");
            DataSource ds =  poolClass.newInstance();
            Map<String,Object> map = new HashMap<String,Object>();
            map.putAll(params);
            map.put("url", url);
            map.put("jdbcUrl", url);
			map.put("driver",driver);
			map.put("driverClass",driver);
			map.put("driverClassName",driver);
			map.put("user",user);
			map.put("username",user);
            BeanUtil.setFieldsValue(ds, map);
            return ds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
