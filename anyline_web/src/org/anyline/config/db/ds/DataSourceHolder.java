/* 
 * Copyright 2006-2015 www.anyline.org
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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.db.ds;

import org.anyline.util.BasicUtil;

public class DataSourceHolder {
	//数据源标识
    private static final ThreadLocal<String> THREAD_SOURCE = new ThreadLocal<String>();
    //是否还原默认数据源,执行一次操作后还原回默认数据源
    private static final ThreadLocal<String> THREAD_AUTO_DEFAULT = new ThreadLocal<String>();
    static{
    	THREAD_AUTO_DEFAULT.set("false");
    }
    public static String getDataSource() {
        return THREAD_SOURCE.get();
    }

    public static void setDataSource(String dataSource) {
    	THREAD_SOURCE.set(dataSource);
    	THREAD_AUTO_DEFAULT.set("false");
    }

    public static void setDataSource(String dataSource, boolean auto) {
    	THREAD_SOURCE.set(dataSource);
    	if(auto){
    		THREAD_AUTO_DEFAULT.set("true");
    	}else{
        	THREAD_AUTO_DEFAULT.set("false");
    	}
    }

    public static void setDefaultDataSource(){
    	clearDataSource();
    	THREAD_AUTO_DEFAULT.set("false");
    }
    public static void clearDataSource() {
    	THREAD_SOURCE.remove();
    }
    public static boolean isAutoDefault(){
    	String result = THREAD_AUTO_DEFAULT.get();
    	return BasicUtil.parseBoolean(result, false);
    }
}