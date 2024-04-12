
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
 */

package org.anyline.data.cache;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.param.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.encrypt.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.List;

public class CacheUtil {
	private static final Logger log = LoggerFactory.getLogger(CacheUtil.class);
	private static Hashtable<String, Long> reflushFlag = new Hashtable<String, Long>();		// 缓存刷新标记

	/*
	 * 辅助缓存刷新控制, N秒内只接收一次刷新操作
	 * 调用刷新方法前, 先调用start判断是否可刷新, 刷新完成后调用stop
	 * start与stop使用同一个key,
	 * 其中两次刷新间隔时间在anyline-config中设置单位秒<property key="key">sec</property>
	 */
    /**
     * 开始刷新
     * 如果不符合刷新条件返回false
     * @param key key
     * @param sec sec
     * @return boolean
     */
    public static boolean start(String key, int sec){
    	boolean result = false;
    	Long fr = reflushFlag.get(key);
    	long age = -1;			// 已生存
    	if(null == fr){
    		result = true;
    	}else{
	    	age = (System.currentTimeMillis() - fr) / 1000;
	    	if(age > sec){
	    		result = true;
	    	}
    	}
    	if(result){
    		reflushFlag.put(key, System.currentTimeMillis());
    		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
    			log.info("[频率控制放行][key:{}][间隔:{}/{}]", key, age, sec);
    		}
    	}else{
    		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
    			log.info("[频率控制拦截][key:{}][间隔:{}/{}]", key, age, sec);
    		}
    	}
    	return result;
    }
    public static boolean start(String key){
    	int period = ConfigTable.getInt(key, 120);		// 两次刷新最小间隔
    	return start(key, period);
    }
    /**
     * 刷新完成
     * @param key key
     * @param sec sec
     */
    public static void stop(String key, int sec){
    	Long fr = reflushFlag.get(key);
    	if(null == fr){
    		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
    			log.info("[频率控制还原完成 有可能key拼写有误][key:{}]", key);
    		}
    		return;
    	}
    	long age = (System.currentTimeMillis() - fr)/1000;			// 已生存
    	
    	if(age > sec){
    		reflushFlag.remove(key);
    	}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[频率控制还原完成][key:{}][间隔:{}/{}]", key, age, sec);
		}
    }
    public static void stop(String key){
    	int period = ConfigTable.getInt(key, 120);					// 两次刷新最小间隔
    	stop(key, period);
    }
    public static boolean isRun(String key){
    	if(null == reflushFlag.get(key)){
    		return false;
    	}
    	return true;
    }
    /**
     * 已执行时间
     * @param key key
     * @return long
     */
    public static long getRunTime(String key){
    	long result = -1;
    	Long fr = reflushFlag.get(key);
    	if(null != fr){
    		return System.currentTimeMillis() - fr;
    	}
    	return result;
    }
    /**
     * 创建集中缓存的key
     * @param table 表
     * @param row row
     * @return String
     */
    public static String crateCachePrimaryKey(String table, DataRow row){
    	String key = table;
    	List<String> pks = row.getPrimaryKeys();
    	if(BasicUtil.isNotEmpty(pks) && null != row){
    		for(String pk:pks){
    			String value = row.getString(pk);
    			key += "|" + pk + "=" + value;
    		}
    	}
    	return key;
    }
    /**
	 * 创建cache key
	 * @param page 是否需要拼接分页下标
	 * @param order order
	 * @param dest 查询或操作的目标(表、存储过程、SQL等)
	 * @param store 根据http等上下文构造查询条件
	 * @param conditions 固定查询条件
	 * @return String
	 */
	public static String createCacheElementKey(boolean page, boolean order, String dest, ConfigStore store, String ... conditions){
		conditions = BasicUtil.compress(conditions);
		String result = dest+"|";
		if(null != store){
			ConfigChain chain = store.getConfigChain();
			if(null != chain){
				List<Config> configs = chain.getConfigs();
				if(null != configs){
					for(Config config:configs){
						List<Object> values = config.getValues();
						if(null != values){
							result += config.toString() + "|";
						}
					}	
				}
			}
			PageNavi navi = store.getPageNavi();
			if(page && null != navi){
				result += "page=" + navi.getCurPage()+"|first=" + navi.getFirstRow() + "|last="+navi.getLastRow()+"|";
			}
			if(order){
				OrderStore orders = store.getOrders();
				if(null != orders){
					result += orders.getRunText("").toUpperCase() +"|";
				}
			}
		}
		if(null != conditions){
			for(String condition:conditions){
				if(BasicUtil.isNotEmpty(condition)){
					if(condition.trim().toUpperCase().startsWith("ORDER")){
						if(order){
							result += condition.toUpperCase() + "|";
						}
					}else{
						result += condition+"|";
					}
				}
			}
		}
		if(ConfigTable.IS_DEBUG && log.isInfoEnabled()){
			log.info("[create cache key][key:{}]", result);
		}
		return MD5Util.crypto(result);
	}

	public static String createCacheElementKey(boolean page, boolean order, Table dest, ConfigStore store, String ... conditions){
		String key = null;
		if(null != dest){
			key = dest.toString();
		}
		return createCacheElementKey(page, order, key, store, conditions);
	}
} 
