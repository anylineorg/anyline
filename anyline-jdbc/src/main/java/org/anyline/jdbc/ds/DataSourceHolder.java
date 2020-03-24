/*  
 * Copyright 2006-2020 www.anyline.org
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
 
 
package org.anyline.jdbc.ds; 
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceHolder { 
	public static Logger log = LoggerFactory.getLogger(DataSourceHolder.class); 
	//切换前数据源 
    private static final ThreadLocal<String> THREAD_RECALL_SOURCE = new ThreadLocal<String>(); 
	//当前数据源 
    private static final ThreadLocal<String> THREAD_CUR_SOURCE = new ThreadLocal<String>(); 
    //是否还原默认数据源,执行一次操作后还原回默认数据源 
    private static final ThreadLocal<Boolean> THREAD_AUTO_DEFAULT = new ThreadLocal<Boolean>(); 
    private static List<String> dataSources = new ArrayList<String>(); 
    static{ 
    	THREAD_AUTO_DEFAULT.set(false); 
    } 
    public static String getDataSource() { 
        return THREAD_CUR_SOURCE.get(); 
    }

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 */
	public static void setDataSource(String dataSource) {
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[切换数据源][thread:{}][数据源:{}]",Thread.currentThread().getId(),dataSource); 
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_DEFAULT.set(false); 
    }

	/**
	 * 设置当前数据源名称
	 * @param dataSource 数据源在spring context中注册的名称
	 * @param auto 扫行完后切换回原来的数据库
	 */
    public static void setDataSource(String dataSource, boolean auto) { 
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[切换数据源][thread:{}][数据源:{}][auto default:{}]",Thread.currentThread().getId(),dataSource,auto); 
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_DEFAULT.set(auto); 
    } 
    //恢复切换前数据源 
    public static void recoverDataSource(){ 
    	THREAD_CUR_SOURCE.set(THREAD_RECALL_SOURCE.get()); 
    } 
    public static void setDefaultDataSource(){ 
    	clearDataSource(); 
    	if(dataSources.contains("default")){ 
    		setDataSource("default"); 
    	} 
    	THREAD_AUTO_DEFAULT.set(false); 
    } 
    public static void clearDataSource() { 
    	THREAD_CUR_SOURCE.remove(); 
    } 
    public static boolean isAutoDefault(){ 
    	if(null == THREAD_AUTO_DEFAULT || null == THREAD_AUTO_DEFAULT.get()){ 
    		return false; 
    	} 
    	return THREAD_AUTO_DEFAULT.get(); 
    } 
 
	/** 
	 * 解析数据源,并返回修改后的SQL 
	 * &lt;mysql_ds&gt;crm_user 
	 * @param src  src
	 * @return return
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

	/**
	 * 注册新的数据源，只是把spring context中现有的数据源名称添加到数据源名称列表
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
	 * @throws Exception Exception
	 */
	public static DataSource addDataSource(String key, DataSource ds) throws Exception{
		if(dataSources.contains(key)){ 
			throw new Exception("[重复注册][thread:"+Thread.currentThread().getId()+"][key:"+key+"]"); 
		} 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[创建数据源][thread:{}][key:{}]",Thread.currentThread().getId(), key); 
		} 
		DynamicDataSource.addDataSource(key, ds); 
		dataSources.add(key);
		return ds;
	}

	public static DataSource reg(String key, String type, String driver, String url, String user, String password) throws Exception{
		Map<String,String> param = new HashMap<String,String>();
		param.put("type", type);
		param.put("driver", driver);
		param.put("url", url);
		param.put("user", user);
		param.put("password", password);
		DataSource ds = buildDataSource(param);
		return reg(key,ds);
	}
	public static DataSource reg(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds);
	}

	public static DataSource reg(String key, Map<String,?> param) throws Exception{
		return addDataSource(key, buildDataSource(param));
	}

	/**
	 * 创建数据源
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static DataSource buildDataSource(Map<String, ?> params) throws Exception{
        try {
            String type = (String)params.get("type");
            if (type == null) {
                throw new Exception("未设置数据源类型(如:type=com.zaxxer.hikari.HikariDataSource)");
            }
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
            Object driver =  BeanUtil.propertyValue(params,"driver","driver-class","driver-class-name");
			Object url =  BeanUtil.propertyValue(params,"url","jdbc-url");
			Object user =  BeanUtil.propertyValue(params,"user","username");
            DataSource ds =  dataSourceType.newInstance();
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
