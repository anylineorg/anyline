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


package org.anyline.service; 
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;

import java.util.Collection;
 
public interface AnylineService{ 
	/**
	 * 按条件查询
	 * @param src			数据源(表或自定义SQL或SELECT语句) src			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		封装来自于http的查询条件 configs		封装来自于http的查询条件
	 * @param conditions	固定查询条件   conditions	固定查询条件
	 * 			原生SQL(AND GROUP ORDER)
	 * 			{原生}
	 * 			[+]CD:1
	 * 			[+]CD:
	 * 			[+]CD:null
	 * 			[+]CD:NULL
	 * 			
	 * @return return
	 */
	public DataSet querys(String src, ConfigStore configs, String ... conditions);
	public DataSet querys(String src, String ... conditions);
	public DataSet querys(String src, int fr, int to, String ... conditions);
	public DataRow query(String src, ConfigStore configs, String ... conditions);
	public DataRow query(String src, String ... conditions);

	//实现与query相同的功能
	public DataSet selects(String src, ConfigStore configs, String ... conditions);
	public DataSet selects(String src, String ... conditions);
	public DataSet selects(String src, int fr, int to, String ... conditions);
	public DataRow select(String src, ConfigStore configs, String ... conditions);
	public DataRow select(String src, String ... conditions);



	/**
	 * 如果二级缓存开启 会从二级缓存中提取数据
	 * @param cache	对应ehcache缓存配置文件 中的cache.name
	 * @param src src
	 * @param configs configs
	 * @param conditions conditions
	 * @return return
	 */
	public DataSet caches(String cache, String src, ConfigStore configs, String ... conditions);
	public DataSet caches(String cache, String src, String ... conditions);
	public DataSet caches(String cache, String src, int fr, int to, String ... conditions);
	public DataRow cache(String cache, String src, ConfigStore configs, String ... conditions);
	public DataRow cache(String cache, String src, String ... conditions);


	/*多表查询，左右连接时使用*/
	public DataSet querys(SQL sql, ConfigStore configs, String ... conditions);
	public DataSet querys(SQL sql, String ... conditions);
	public DataSet querys(SQL sql, int fr, int to, String ... conditions);
	public DataRow query(SQL sql, ConfigStore configs, String ... conditions);
	public DataRow query(SQL sql, String ... conditions);
	public DataSet selects(SQL sql, ConfigStore configs, String ... conditions);
	public DataSet selects(SQL sql, String ... conditions);
	public DataSet selects(SQL sql, int fr, int to, String ... conditions);
	public DataRow select(SQL sql, ConfigStore configs, String ... conditions);
	public DataRow select(SQL sql, String ... conditions);
	public DataSet caches(String cache, SQL sql, ConfigStore configs, String ... conditions);
	public DataSet caches(String cache, SQL sql, String ... conditions);
	public DataSet caches(String cache, SQL sql, int fr, int to, String ... conditions);
	public DataRow cache(String cache, SQL sql, ConfigStore configs, String ... conditions);
	public DataRow cache(String cache, SQL sql, String ... conditions);



	public DataRow next(DataRow row, String column, SQL.ORDER_TYPE order, ConfigStore configs, String ... conditions);
	public DataRow next(DataRow row, String column, SQL.ORDER_TYPE order, String ... conditions);
	public DataRow next(DataRow row, SQL.ORDER_TYPE order, String ... conditions);
	public DataRow next(DataRow row, ConfigStore configs, String ... conditions);
	public DataRow next(DataRow row, String ... conditions);
	
	public DataRow prev(DataRow row, String column, SQL.ORDER_TYPE order, ConfigStore configs, String ... conditions);
	public DataRow prev(DataRow row, String column, SQL.ORDER_TYPE order, String ... conditions);
	public DataRow prev(DataRow row, SQL.ORDER_TYPE order, String ... conditions);
	public DataRow prev(DataRow row, ConfigStore configs, String ... conditions);
	public DataRow prev(DataRow row, String ... conditions);


	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param channel channel
	 * @param src src
	 * @param configs configs
	 * @param conditions conditions
	 * @return return
	 */
	public boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions);
	public boolean removeCache(String channel, String src, String ... conditions);
	public boolean removeCache(String channel, String src, int fr, int to, String ... conditions);
	/**
	 * 清空缓存
	 * @param channel channel
	 * @return return
	 */
	public boolean clearCache(String channel);
	 
	/** 
	 * 是否存在 
	 * @param src  src
	 * @param configs  configs
	 * @param conditions  conditions
	 * @return return
	 */ 
	public boolean exists(String src, ConfigStore configs, String ... conditions); 
	public boolean exists(String src, String ... conditions); 
	public boolean exists(String src, DataRow row);
	public boolean exists(DataRow row);
	
	public int count(String src, ConfigStore configs, String ... conditions);
	public int count(String src, String ... conditions);
	
	 
	 
	/** 
	 * 更新记录 
	 * @param columns	  需要更新的列 
	 * @param dest	   表 
	 * @param data data
	 * @return return
	 */
	public int update(String dest, Object data, String ... columns);
	public int update(Object data, String ... columns);
	public int update(String dest, ConfigStore configs, String ... conditions);
	
	public int update(boolean sync, String dest, Object data, String ... columns);
	public int update(boolean sync, Object data, String ... columns);
	/** 
	 * 保存(insert|update) 
	 * @param data  data
	 * @param checkPriamry  checkPriamry
	 * @param columns  columns
	 * @param dest 表 
	 * @return return
	 */ 
	public int save(String dest, Object data, boolean checkPriamry, String ... columns); 
	public int save(Object data, boolean checkPriamry, String ... columns); 
	public int save(Object data, String ... columns); 
	public int save(String dest, Object data, String ... columns); 
//
	public int save(boolean sync, String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, Object data, String ... columns);
	public int save(boolean sync, String dest, Object data, String ... columns); 
 
 
	public int insert(String dest, Object data, boolean checkPriamry, String ... columns); 
	public int insert(Object data, boolean checkPriamry, String ... columns); 
	public int insert(Object data, String ... columns); 
	public int insert(String dest, Object data, String ... columns); 


	/**
	 * 异步插入
	 * @param dest dest
	 * @param data data
	 * @param checkPriamry checkPriamry
	 * @param columns columns
	 * @return return
	 */
	public int batchInsert(String dest, Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(Object data, String ... columns);
	public int batchInsert(String dest, Object data, String ... columns); 
	/** 
	 * save insert区别 
	 * 操作单个对象时没有区别 
	 * 在操作集合时区别: 
	 * save会循环操作数据库每次都会判断insert|update 
	 * save 集合中的数据可以是不同的表不同的结构  
	 * insert 集合中的数据必须保存到相同的表,结构必须相同 
	 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势 
	 *  
	 */
	 
	/** 
	 * 执行 
	 * @param src  src
	 * @param configs  configs
	 * @param conditions  conditions
	 * @return return
	 */ 
	public int execute(String src, ConfigStore configs, String ... conditions); 
	public int execute(String src, String ... conditions); 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @param inputs  inputs
	 * @return return
	 */ 
	public boolean executeProcedure(String procedure, String... inputs); 
	public boolean execute(Procedure procedure, String... inputs);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @param inputs  inputs
	 * @return return
	 */ 
	public DataSet queryProcedure(String procedure, String ... inputs);
	public DataSet query(Procedure procedure, String ... inputs);

	public DataSet selectProcedure(String procedure, String ... inputs);
	public DataSet select(Procedure procedure, String ... inputs);

	public int delete(String table, ConfigStore configs, String ... conditions);
	/**
	 * 删除 根据主键删除 可设置复合主键
	 * @param dest 表
	 * @param set 数据
	 * @param columns 主键
	 * @return 影响行数
	 */
	public int delete(String dest, DataSet set, String ... columns);
	public int delete(DataSet set, String ... columns);
	public int delete(String dest, DataRow row, String ... columns);
	public int delete(DataRow row, String ... columns);

	/**
	 * 根据多列条件删除 delete("user","type","1", "age:20");
	 * @param table 表
	 * @param kvs key-value
	 * @return 影响行数
	 */
	public int delete(String table, String ... kvs);

	/**
	 * 根据一列的多个值删除多行
	 * @param table 表
	 * @param key 名
	 * @param values 值集合
	 * @return 影响行数
	 */
	public int deletes(String table, String key, Collection<Object> values);

	/**
	 * 根据一列的多个值删除多行
	 * @param table 表
	 * @param key 名
	 * @param values 值集合
	 * @return 影响行数
	 */
	public int deletes(String table, String key, String ... values);
}
