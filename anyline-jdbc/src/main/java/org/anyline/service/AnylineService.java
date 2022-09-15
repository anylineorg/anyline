/* 
 * Copyright 2006-2022 www.anyline.org
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
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.entity.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineService<E>{
	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件
	 * @param conditions	固定查询条件
	 * 			原生SQL(AND GROUP ORDER)
	 * 			{原生}
	 * 			[+]CD:1
	 * 			[+]CD:
	 * 			[+]CD:null
	 * 			[+]CD:NULL
	 * 			
	 * @return DataSet
	 */
	public DataSet querys(String src, ConfigStore configs, Object obj, String ... conditions);
	public DataSet querys(String src, Object obj, String ... conditions);
	public DataSet querys(String src, PageNavi navi, Object obj, String ... conditions);

	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param first 起 下标从0开始
	 * @param last 止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	public DataSet querys(String src, int first, int last, Object obj, String ... conditions);
	public DataRow query(String src, ConfigStore configs, Object obj, String ... conditions);
	public DataRow query(String src, Object obj, String ... conditions);


	public DataSet querys(String src, ConfigStore configs, String ... conditions);
	public DataSet querys(String src,  String ... conditions);
	public DataSet querys(String src, PageNavi navi,  String ... conditions);

	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param first 起 下标从0开始
	 * @param last 止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	public DataSet querys(String src, int first, int last,  String ... conditions);
	public DataRow query(String src, ConfigStore configs,  String ... conditions);
	public DataRow query(String src, String ... conditions);





	/**
	 *
	 * @param clazz 返回类型
	 * @param configs 根据http等上下文构造查询条件
	 * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return EntitySet
	 * @param <T> T
	 */
	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, T entity, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, T entity, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, T entity, String ... conditions);
	public <T> T query(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	public <T> T query(Class<T> clazz, T entity, String ... conditions);

	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, String ... conditions);
	public <T> T query(Class<T> clazz, ConfigStore configs, String ... conditions);
	public <T> T query(Class<T> clazz, String ... conditions);



	/*根据service构造泛型查询*/
	public EntitySet<E> gets(ConfigStore configs, String ... conditions);
	public EntitySet<E> gets(PageNavi navi, String ... conditions);

	//与public DataSet querys(String src, String ... conditions);  签名冲突
	public EntitySet<E> gets(String ... conditions);
	public EntitySet<E> gets(int first, int last, String ... conditions);
	public E get(ConfigStore configs, String ... conditions);
	public E get(String ... conditions);

	/**
	 * 直接返回Map集合不封装,不分页
	 * @param src			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions	固定查询条件
	 * @return List
	 */
	public List<Map<String,Object>> maps(String src, ConfigStore configs, Object obj, String ... conditions);
	public List<Map<String,Object>> maps(String src, Object obj, String ... conditions);
	public List<Map<String,Object>> maps(String src, int first, int last, Object obj, String ... conditions);
	public List<Map<String,Object>> maps(String src, ConfigStore configs, String ... conditions);
	public List<Map<String,Object>> maps(String src, String ... conditions);
	public List<Map<String,Object>> maps(String src, int first, int last, String ... conditions);



	/**
	 * 列名转找成参数名 可以给condition()提供参数用来接收前端参数
	 * @param table 表
	 * @return List
	 */
	public List<String> column2param(String table);



	/**
	 * 如果二级缓存开启 会从二级缓存中提取数据
	 * @param cache			对应ehcache缓存配置文件 中的cache.name
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 	固定查询条件
	 * @return DataSet
	 */
	public DataSet caches(String cache, String src, ConfigStore configs, Object obj, String ... conditions);
	public DataSet caches(String cache, String src, Object obj, String ... conditions);
	public DataSet caches(String cache, String src, int first, int last, Object obj, String ... conditions);
	public DataRow cache(String cache, String src, ConfigStore configs, Object obj, String ... conditions);
	public DataRow cache(String cache, String src, Object obj, String ... conditions);

	public DataSet caches(String cache, String src, ConfigStore configs,  String ... conditions);
	public DataSet caches(String cache, String src, String ... conditions);
	public DataSet caches(String cache, String src, int first, int last, String ... conditions);
	public DataRow cache(String cache, String src, ConfigStore configs, String ... conditions);
	public DataRow cache(String cache, String src, String ... conditions);



	/*多表查询,左右连接时使用*/
	public DataSet querys(SQL sql, ConfigStore configs, Object obj, String ... conditions);
	public DataSet querys(SQL sql, Object obj, String ... conditions);
	public DataSet querys(SQL sql, int first, int last, Object obj, String ... conditions);
	public DataRow query(SQL sql, ConfigStore configs, Object obj, String ... conditions);
	public DataRow query(SQL sql, Object obj, String ... conditions);

	public DataSet querys(SQL sql, ConfigStore configs,  String ... conditions);
	public DataSet querys(SQL sql,  String ... conditions);
	public DataSet querys(SQL sql, int first, int last,  String ... conditions);
	public DataRow query(SQL sql, ConfigStore configs,  String ... conditions);
	public DataRow query(SQL sql, String ... conditions);


	public DataSet caches(String cache, SQL sql, ConfigStore configs, Object obj, String ... conditions);
	public DataSet caches(String cache, SQL sql, Object obj, String ... conditions);
	public DataSet caches(String cache, SQL sql, int first, int last, Object obj, String ... conditions);
	public DataRow cache(String cache, SQL sql, ConfigStore configs, Object obj, String ... conditions);
	public DataRow cache(String cache, SQL sql, Object obj, String ... conditions);

	public DataSet caches(String cache, SQL sql, ConfigStore configs, String ... conditions);
	public DataSet caches(String cache, SQL sql, String ... conditions);
	public DataSet caches(String cache, SQL sql, int first, int last, String ... conditions);
	public DataRow cache(String cache, SQL sql, ConfigStore configs, String ... conditions);
	public DataRow cache(String cache, SQL sql, String ... conditions);

	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param channel 		channel
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs  		根据http等上下文构造查询条件
	 * @param conditions 	固定查询条件
	 * @return boolean
	 */
	public boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions);
	public boolean removeCache(String channel, String src, String ... conditions);
	public boolean removeCache(String channel, String src, int first, int last, String ... conditions);
	/**
	 * 清空缓存
	 * @param channel channel
	 * @return boolean
	 */
	public boolean clearCache(String channel);
	 
	/** 
	 * 是否存在 
	 * @param src  			数据源(表或自定义SQL或SELECT语句)
	 * @param configs  		根据http等上下文构造查询条件
	 * @param conditions 	固定查询条件
	 * @return boolean
	 */
	public boolean exists(String src, ConfigStore configs, Object obj, String ... conditions);
	public boolean exists(String src, Object obj, String ... conditions);
	public boolean exists(String src, ConfigStore configs, String ... conditions);
	public boolean exists(String src, String ... conditions);
	public boolean exists(String src, DataRow row);
	public boolean exists(DataRow row);

	public int count(String src, ConfigStore configs, Object obj, String ... conditions);
	public int count(String src, Object obj, String ... conditions);
	public int count(String src, ConfigStore configs, String ... conditions);
	public int count(String src, String ... conditions);
	
	 
	 
	/** 
	 * 更新记录 
	 * @param fixs	  需要更新的列
	 * @param columns	  需要更新的列
	 * @param dest	   表 
	 * @param data data
	 * @return int
	 */
	public int update(String dest, Object data, List<String> fixs, String ... columns);
	public int update(Object data, List<String> fixs, String ... columns);
	public int update(String dest, ConfigStore configs, List<String> fixs, String ... conditions);

	public int update(boolean async, String dest, Object data, List<String> fixs, String ... columns);
	public int update(boolean async, Object data, List<String> fixs, String ... columns);


	public int update(String dest, Object data, String[] fixs, String ... columns);
	public int update(Object data, String[] fixs, String ... columns);
	public int update(String dest, ConfigStore configs, String[] fixs, String ... conditions);

	public int update(boolean async, String dest, Object data, String[] fixs, String ... columns);
	public int update(boolean async, Object data, String[] fixs, String ... columns);



	public int update(String dest, Object data, String ... columns);
	public int update(Object data, String ... columns);
	public int update(String dest, ConfigStore configs, String ... conditions);

	public int update(boolean async, String dest, Object data, String ... columns);
	public int update(boolean async, Object data, String ... columns);
	/** 
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param data  数据
	 * @param checkPriamry 是否检测主键
	 * @param fixs 指定更新或保存的列
	 * @param columns 指定更新或保存的列
	 * @param dest 表 
	 * @return 影响行数
	 */
	public int save(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int save(Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int save(Object data, List<String> fixs, String ... columns);
	public int save(String dest, Object data, List<String> fixs, String ... columns);

	public int save(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int save(Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int save(Object data, String[] fixs, String ... columns);
	public int save(String dest, Object data, String[] fixs, String ... columns);


	public int save(String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(Object data, boolean checkPriamry, String ... columns);
	public int save(Object data, String ... columns);
	public int save(String dest, Object data, String ... columns);


	/**
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param async 是否异步执行
	 * @param data  数据
	 * @param checkPriamry 是否检测主键
	 * @param fixs 指定更新或保存的列
	 * @param columns 指定更新或保存的列
	 * @param dest 表
	 * @return 影响行数
	 */
	public int save(boolean async, String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int save(boolean async, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int save(boolean async, Object data, List<String> fixs, String ... columns);
	public int save(boolean async, String dest, Object data, List<String> fixs, String ... columns);

	public int save(boolean async, String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int save(boolean async, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int save(boolean async, Object data, String[] fixs, String ... columns);
	public int save(boolean async, String dest, Object data, String[] fixs, String ... columns);

	public int save(boolean async, String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean async, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean async, Object data, String ... columns);
	public int save(boolean async, String dest, Object data, String ... columns);


	public int insert(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int insert(Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int insert(Object data, List<String> fixs, String ... columns);
	public int insert(String dest, Object data, List<String> fixs, String ... columns);

	public int insert(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int insert(Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int insert(Object data, String[] fixs, String ... columns);
	public int insert(String dest, Object data, String[] fixs, String ... columns);


	public int insert(String dest, Object data, boolean checkPriamry, String ... columns);
	public int insert(Object data, boolean checkPriamry, String ... columns);
	public int insert(Object data, String ... columns);
	public int insert(String dest, Object data, String ... columns);


	/**
	 * 异步插入
	 * @param dest dest
	 * @param data data
	 * @param checkPriamry checkPriamry
	 * @param fixs 指定更新或保存的列
	 * @param columns columns
	 * @return int
	 */
	public int batchInsert(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int batchInsert(Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	public int batchInsert(Object data, List<String> fixs, String ... columns);
	public int batchInsert(String dest, Object data, List<String> fixs, String ... columns);


	public int batchInsert(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int batchInsert(Object data, boolean checkPriamry, String[] fixs, String ... columns);
	public int batchInsert(Object data, String[] fixs, String ... columns);
	public int batchInsert(String dest, Object data, String[] fixs, String ... columns);

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
	 * @return int
	 */ 
	public int execute(String src, ConfigStore configs, String ... conditions); 
	public int execute(String src, String ... conditions); 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @param inputs  inputs
	 * @return boolean
	 */ 
	public boolean executeProcedure(String procedure, String... inputs); 
	public boolean execute(Procedure procedure, String... inputs);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @param first  first
	 * @param last  last
	 * @param inputs  inputs
	 * @return DataSet
	 */
	public DataSet querysProcedure(String procedure, int first, int last , String ... inputs);
	public DataSet querysProcedure(String procedure, PageNavi navi , String ... inputs);
	public DataSet querysProcedure(String procedure, String ... inputs);
	public DataSet querys(Procedure procedure, int first, int last,  String ... inputs);
	public DataSet querys(Procedure procedure, PageNavi navi ,  String ... inputs);

	public DataRow queryProcedure(String procedure, String ... inputs);
	public DataRow query(Procedure procedure, String ... inputs);

	public int delete(String table, ConfigStore configs, String ... conditions);
	/**
	 * 删除 根据columns列删除 可设置复合主键
	 * @param dest 表
	 * @param set 数据
	 * @param columns 生成删除条件的列，如果不设置则根据主键删除
	 * @return 影响行数
	 */
	public int delete(String dest, DataSet set, String ... columns);
	public int delete(DataSet set, String ... columns);
	public int delete(String dest, DataRow row, String ... columns);

	/**
	 * 根据columns列删除
	 * @param obj obj
	 * @param columns 生成删除条件的列，如果不设置则根据主键删除
	 * @return 影响行数
	 */
	public int delete(Object obj, String ... columns);

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


	public List<String> tables(String catalog, String schema, String name, String types);
	public List<String> tables(String schema, String name, String types);
	public List<String> tables(String name, String types);
	public List<String> tables(String types);
	public List<String> tables();

	public List<String> stables(String catalog, String schema, String name, String types);
	public List<String> stables(String schema, String name, String types);
	public List<String> stables(String name, String types);
	public List<String> stables(String types);
	public List<String> stables();


	public List<String> columns(Table table);
	public List<String> columns(String table);
	public List<String> columns(String catalog, String schema, String table);

	public List<String> tags(Table table);
	public List<String> tags(String table);
	public List<String> tags(String catalog, String schema, String table);



	public DDLService ddl();
	public MetaDataService metadata();
	public interface DDLService{
		/**
		 * 列是否存在
		 * @param column column
		 * @return boolean
		 */
		public boolean exists(Column column);
		/**
		 * 修改列  名称 数据类型 位置 默认值
		 * 执行save前先调用column.update()设置修改后的属性
		 * column.update().setName().setDefaultValue().setAfter()....
		 * @param column column
		 * @throws Exception SQL异常
		 */
		public boolean save(Column column) throws Exception;
		public boolean add(Column column) throws Exception;
		public boolean alter(Column column) throws Exception;
		public boolean drop(Column column) throws Exception;

		/**
		 * 添加标签
		 * @param tag tag
		 * @return boolean
		 * @throws Exception Exception
		 */
		public boolean add(Tag tag) throws Exception;
		public boolean save(Tag tag) throws Exception;
		public boolean alter(Tag tag) throws Exception;
		public boolean drop(Tag tag) throws Exception;

		/**
		 * 表是否存在
		 * @param table table
		 * @return boolean
		 */
		public boolean exists(Table table);
		public boolean save(Table table) throws Exception;
		public boolean create(Table table) throws Exception;
		public boolean alter(Table table) throws Exception;
		public boolean drop(Table table) throws Exception;

		/**
		 * 超表是否存在
		 * @param table table
		 * @return boolean
		 */
		public boolean exists(STable table);
		public boolean save(STable table) throws Exception;
		public boolean create(STable table) throws Exception;
		public boolean alter(STable table) throws Exception;
		public boolean drop(STable table) throws Exception;

		/**
		 * 添加索引
		 * @param index index
		 * @return boolean
		 * @throws Exception Exception
		 */
		public boolean add(Index index) throws Exception;
		public boolean drop(Index index) throws Exception;
	}
	public interface MetaDataService{
		/**
		 * tables
		 * @param catalog 对于MySQL，则对应相应的数据库，对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名，而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意，其登陆名必须是大写，不然的话是无法获取到相应的数据，而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话，可以直接设置为null，如果设置为特定的表名称，则返回该表的具体信息。
		 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
		 * @return List
		 */
		public List<Table> tables(String catalog, String schema, String name, String types);
		public List<Table> tables(String schema, String name, String types);
		public List<Table> tables(String name, String types);
		public List<Table> tables(String types);
		public List<Table> tables();
		public Table table(String catalog, String schema, String name);
		public Table table(String schema, String name);
		public Table table(String name);


		public List<STable> stables(String catalog, String schema, String name, String types);
		public List<STable> stables(String schema, String name, String types);
		public List<STable> stables(String name, String types);
		public List<STable> stables(String types);
		public List<STable> stables();
		public STable stable(String catalog, String schema, String name);
		public STable stable(String schema, String name);
		public STable stable(String name);


		public List<Column> columns(Table table);
		public List<Column> columns(String table);
		public List<Column> columns(String catalog, String schema, String table);

		public List<Tag> tags(Table table);
		public List<Tag> tags(String table);
		public List<Tag> tags(String catalog, String schema, String table);

		/**
		 * 查询表中所有的表，注意这里的map.KEY全部转大写
		 * @param table table
		 * @param map true
		 * @return map
		 */
		public LinkedHashMap<String,Column> columns(Table table, boolean map);
		public LinkedHashMap<String,Column> columns(String table, boolean map);
		public LinkedHashMap<String,Column> columns(String catalog, String schema, String table, boolean map);

		public LinkedHashMap<String,Tag> tags(Table table, boolean map);
		public LinkedHashMap<String,Tag> tags(String table, boolean map);
		public LinkedHashMap<String,Tag> tags(String catalog, String schema, String table, boolean map);


	}
}
