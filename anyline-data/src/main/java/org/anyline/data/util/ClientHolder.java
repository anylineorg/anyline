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
 
 
package org.anyline.data.util;

import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientHolder {
	public static Logger log = LoggerFactory.getLogger(ClientHolder.class);

	// 切换前数据源 
    protected static final ThreadLocal<String> THREAD_RECALL_SOURCE = new ThreadLocal<String>();
	// 当前数据源 
	protected static final ThreadLocal<String> THREAD_CUR_SOURCE = new ThreadLocal<String>();
    // 是否还原默认数据源,执行一次操作后还原回  切换之前的数据源
	protected static final ThreadLocal<Boolean> THREAD_AUTO_RECOVER = new ThreadLocal<Boolean>();
	protected static List<String> dataSources = new ArrayList<>();
	//数据源对应的数据库类型
	protected static Map<String, DatabaseType> types = new HashMap<>();

    static{
    	THREAD_AUTO_RECOVER.set(false); 
    }


	/**
	 * 已注册成功的所有数据源
	 * @return List
	 */
	public static List<String> list(){
		return dataSources;
	}

    public static String curDataSource() {
        return THREAD_CUR_SOURCE.get();
    }

	public static DatabaseType dialect(){
		String ds = curDataSource();
		return types.get(ds);
	}
	public static void dialect(String ds, DatabaseType type){
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

		//不要切换到默认数据源，避免误操作
		/*if(BasicUtil.isEmpty(dataSource)){
			setDefaultDataSource();
			return;
		}*/
		if(null == dataSource || !dataSources.contains(dataSource)){
			throw new RuntimeException("数据源未注册:"+dataSource);
		}
    	if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
    		log.info("[切换数据源][thread:{}][数据源:{}>{}][auto recover:{}]", Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get(), dataSource, auto);
    	} 
    	THREAD_RECALL_SOURCE.set(THREAD_CUR_SOURCE.get());//记录切换前数据源 
    	THREAD_CUR_SOURCE.set(dataSource); 
    	THREAD_AUTO_RECOVER.set(auto); 
    } 
    // 恢复切换前数据源 
    public static void recoverDataSource(){
		String fr = THREAD_CUR_SOURCE.get();
		String to = THREAD_RECALL_SOURCE.get();
		if(null == fr && null == to){
			return;
		}
		if(null != fr && fr.equals(to)){
			return;
		}
    	THREAD_CUR_SOURCE.set(to);
		log.info("[还原数据源][thread:{}][数据源:{}>{}][auto recover:{}]", Thread.currentThread().getId(), fr, to);
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
			log.info("[切换数据源][thread:{}][数据源:{}>默认数据源]",Thread.currentThread().getId(), THREAD_RECALL_SOURCE.get());
		}
	}
    public static void clearDataSource() {
    	THREAD_CUR_SOURCE.remove(); 
    } 
    public static boolean isAutoRecover(){
    	if(null == THREAD_AUTO_RECOVER || null == THREAD_AUTO_RECOVER.get()){
    		return false; 
    	} 
    	return THREAD_AUTO_RECOVER.get(); 
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



}
