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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource; 
 
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
 
    public static void setDataSource(String dataSource) { 
    	if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
    		log.warn("[切换数据源][thread:{}][数据源:{}]",Thread.currentThread().getId(),dataSource); 
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_DEFAULT.set(false); 
    } 
 
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
	public static void reg(String ds){ 
		if(!dataSources.contains(ds)){ 
			dataSources.add(ds); 
		} 
	} 
	public static boolean contains(String ds){ 
		return dataSources.contains(ds); 
	}
	public static DataSource reg(String key, DataSource ds) throws Exception{
		return addDataSource(key, ds);
	}
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

}
