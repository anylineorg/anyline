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


package org.anyline.service;
import org.anyline.entity.data.Function;
import org.anyline.data.entity.*;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.entity.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineService<E>{


	/**
	 * 切换数据源
	 * @param datasource 数据源
	 */
	AnylineService datasource(String datasource);
	AnylineService datasource();
	AnylineService setDataSource(String datasource);
	AnylineService setDataSource(String datasource, boolean auto);
	AnylineService setDefaultDataSource();
	AnylineService recoverDataSource();
	String getDataSource();

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * BATCH INSERT		: 批量插入
	 * UPDATE			: 更新
	 * SAVE				: 根据情况插入或更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 * CACHE			: 缓存
	 * METADATA			: 简单格式元数据,只返回NAME
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													INSERT
	 ******************************************************************************************************************/

	/**
	 * 插入数据
	 * @param dest 表名
	 * @param data entity或list或DataRow或DataSet
	 * @param checkPriamry 是否检测主键
	 * @param fixs 需要插入哪些列
	 * @param columns 需要插入哪些列
	 * @return 影响行数
	 */
	int insert(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int insert(Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int insert(Object data, List<String> fixs, String ... columns);
	int insert(String dest, Object data, List<String> fixs, String ... columns);

	int insert(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int insert(Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int insert(Object data, String[] fixs, String ... columns);
	int insert(String dest, Object data, String[] fixs, String ... columns);

	int insert(String dest, Object data, boolean checkPriamry, String ... columns);
	int insert(Object data, boolean checkPriamry, String ... columns);
	int insert(Object data, String ... columns);
	int insert(String dest, Object data, String ... columns);

	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/

	/**
	 * 更新记录
	 * 默认情况下以主键为更新条件,需在更新的数据保存在data中
	 * 如果提供了dest则更新dest表,如果没有提供则根据data解析出表名
	 * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键,则根据TYPE_CODE更新
	 * 可以提供了ConfigStore以实现更复杂的更新条件
	 * 需要更新的列通过fixs/columns提供
	 * @param fixs	  	需要更新的列
	 * @param columns	需要更新的列
	 * @param dest	   	表
	 * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
	 * @param configs 	更新条件
	 * @return int 影响行数
	 */
	int update(String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns);
	int update(String dest, Object data, List<String> fixs, String ... columns);
	int update(String dest, Object data, String[] fixs, String ... columns);
	int update(String dest, Object data, ConfigStore configs, String[] fixs, String ... columns);
	int update(String dest, Object data, String ... columns);
	int update(String dest, Object data, ConfigStore configs, String ... columns);

	int update(Object data, ConfigStore configs, List<String> fixs, String ... columns);
	int update(Object data, List<String> fixs, String ... columns);
	int update(Object data, String[] fixs, String ... columns);
	int update(Object data, ConfigStore configs, String[] fixs, String ... columns);
	int update(Object data, String ... columns);
	int update(Object data, ConfigStore configs, String ... columns);



	int update(boolean async, String dest, Object data, List<String> fixs, String ... columns);
	int update(boolean async, String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns);
	int update(boolean async, String dest, Object data, String[] fixs, String ... columns);
	int update(boolean async, String dest, Object data, ConfigStore configs, String[] fixs, String ... columns);
	int update(boolean async, String dest, Object data, String ... columns);
	int update(boolean async, String dest, Object data, ConfigStore configs, String ... columns);
	
	int update(boolean async, Object data, List<String> fixs, String ... columns);
	int update(boolean async, Object data, ConfigStore configs, List<String> fixs, String ... columns);
	int update(boolean async, Object data, String[] fixs, String ... columns);
	int update(boolean async, Object data, ConfigStore configs, String[] fixs, String ... columns);
	int update(boolean async, Object data, String ... columns);
	int update(boolean async, Object data, ConfigStore configs, String ... columns);


	/* *****************************************************************************************************************
	 * 													SAVE
	 ******************************************************************************************************************/


	/**
	 * save insert区别
	 * 操作单个对象时没有区别
	 * 在操作集合时区别:
	 * save会循环操作数据库每次都会判断insert|update
	 * save 集合中的数据可以是不同的表不同的结构
	 * insert 集合中的数据必须保存到相同的表,结构必须相同
	 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
	 *
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param data  数据
	 * @param checkPriamry 是否检测主键
	 * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
	 * @param columns 指定更新或保存的列
	 * @param dest 表
	 * @return 影响行数
	 */
	int save(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int save(Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int save(Object data, List<String> fixs, String ... columns);
	int save(String dest, Object data, List<String> fixs, String ... columns);

	int save(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int save(Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int save(Object data, String[] fixs, String ... columns);
	int save(String dest, Object data, String[] fixs, String ... columns);


	int save(String dest, Object data, boolean checkPriamry, String ... columns);
	int save(Object data, boolean checkPriamry, String ... columns);
	int save(Object data, String ... columns);
	int save(String dest, Object data, String ... columns);


	/**
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param async 是否异步执行
	 * @param data  数据
	 * @param checkPriamry 是否检测主键
	 * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
	 * @param columns 指定更新或保存的列
	 * @param dest 表
	 * @return 影响行数
	 */
	int save(boolean async, String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int save(boolean async, Object data, boolean checkPriamry, List<String> fixs, String ... columns);
	int save(boolean async, Object data, List<String> fixs, String ... columns);
	int save(boolean async, String dest, Object data, List<String> fixs, String ... columns);

	int save(boolean async, String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int save(boolean async, Object data, boolean checkPriamry, String[] fixs, String ... columns);
	int save(boolean async, Object data, String[] fixs, String ... columns);
	int save(boolean async, String dest, Object data, String[] fixs, String ... columns);

	int save(boolean async, String dest, Object data, boolean checkPriamry, String ... columns);
	int save(boolean async, Object data, boolean checkPriamry, String ... columns);
	int save(boolean async, Object data, String ... columns);
	int save(boolean async, String dest, Object data, String ... columns);


	/* *****************************************************************************************************************
	 * 													QUERY
	 ******************************************************************************************************************/

	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件
	 * @param conditions	固定查询条件 <br/>
	 * 			CD:1 生成SQL: CD = 1<br/>
	 * 			CD: 忽略<br/>
	 * 			CD:null 忽略<br/>
	 * 			CD:NULL 生成SQL:CD IS NULL<br/>
	 * 			原生SQL(包括GROUP、ORDER、HAVING等)如 ID > 1 AND ID < 10<br/>
	 * 			${原生SQL}:${}之内的SQL不全处理 如果原生SQL比较复杂(如出现小时格式)可能与以上几种格式混淆,可以用${}表示不解析按原文执行<br/>
	 * 			
	 * @return DataSet
	 */
	DataSet querys(String src, ConfigStore configs, Object obj, String ... conditions);
	DataSet querys(String src, Object obj, String ... conditions);
	DataSet querys(String src, PageNavi navi, Object obj, String ... conditions);

	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param first 起 下标从0开始
	 * @param last 止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	DataSet querys(String src, int first, int last, Object obj, String ... conditions);
	DataRow query(String src, ConfigStore configs, Object obj, String ... conditions);
	DataRow query(String src, Object obj, String ... conditions);


	DataSet querys(String src, ConfigStore configs, String ... conditions);
	DataSet querys(String src,  String ... conditions);
	DataSet querys(String src, PageNavi navi,  String ... conditions);

	/**
	 * 按条件查询
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param first 		起 下标从0开始
	 * @param last 			止
	 * @param conditions	固定查询条件
	 * @return DataSet
	 */
	DataSet querys(String src, int first, int last,  String ... conditions);
	DataRow query(String src, ConfigStore configs,  String ... conditions);
	DataRow query(String src, String ... conditions);

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param name 	序列名
	 * @return long 查询失败返回null
	 */
	BigDecimal sequence(boolean next, String name);
	/**
	 * 查询序列next value
	 * @param name 序列名
	 * @return long 查询失败返回null
	 */
	BigDecimal sequence(String name);
	/**
	 * 查询序列cur 或 next value
	 * @param names 序列名
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @return DataRow 查询结果按序列名保存到DataRow中，查询失败返回null
	 */
	DataRow sequences(boolean next, String ... names);
	DataRow sequences(String ... names);

	/**
	 * 根据calzz返回实体集合
	 * 为了更容易记忆和区分,往后的版本中删除,所有返回实体类的方法换成selects/select
	 * @param clazz clazz
	 * @param configs configs
	 * @param entity entity
	 * @param conditions conditions
	 * @return EntitySet
	 * @param <T>  entity
	 */
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, T entity, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, T entity, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, int first, int last, T entity, String ... conditions);
	@Deprecated
	<T> T query(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	@Deprecated
	<T> T query(Class<T> clazz, T entity, String ... conditions);

	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, String ... conditions);
	@Deprecated
	<T> EntitySet<T> querys(Class<T> clazz, int first, int last, String ... conditions);
	@Deprecated
	<T> T query(Class<T> clazz, ConfigStore configs, String ... conditions);
	@Deprecated
	<T> T query(Class<T> clazz, String ... conditions);


	/**
	 * 根据SQL或自定义SQL返回实体
	 * @param src SQL或自定义SQL
	 * @param clazz 返回类型
	 * @param configs 根据http等上下文构造查询条件
	 * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return EntitySet
	 * @param <T> T
	 */
	<T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, T entity, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, T entity, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, int first, int last, T entity, String ... conditions);
	<T> T select(String src, Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	<T> T select(String src, Class<T> clazz, T entity, String ... conditions);

	<T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, String ... conditions);
	<T> EntitySet<T> selects(String src, Class<T> clazz, int first, int last, String ... conditions);
	<T> T select(String src, Class<T> clazz, ConfigStore configs, String ... conditions);
	<T> T select(String src, Class<T> clazz, String ... conditions);



	/**
	 *
	 * @param clazz 返回类型
	 * @param configs 根据http等上下文构造查询条件
	 * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 固定查询条件
	 * @return EntitySet
	 * @param <T> T
	 */
	<T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, T entity, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, T entity, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, int first, int last, T entity, String ... conditions);
	<T> T select(Class<T> clazz, ConfigStore configs, T entity, String ... conditions);
	<T> T select(Class<T> clazz, T entity, String ... conditions);

	<T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, String ... conditions);
	<T> EntitySet<T> selects(Class<T> clazz, int first, int last, String ... conditions);
	<T> T select(Class<T> clazz, ConfigStore configs, String ... conditions);
	<T> T select(Class<T> clazz, String ... conditions);




	/*根据service构造泛型查询*/
	EntitySet<E> gets(ConfigStore configs, String ... conditions);
	EntitySet<E> gets(PageNavi navi, String ... conditions);

	// 与DataSet querys(String src, String ... conditions);  签名冲突
	EntitySet<E> gets(String ... conditions);
	EntitySet<E> gets(int first, int last, String ... conditions);
	E get(ConfigStore configs, String ... conditions);
	E get(String ... conditions);

	/**
	 * 直接返回Map集合不封装,不分页
	 * @param src			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions	固定查询条件
	 * @return List
	 */
	List<Map<String,Object>> maps(String src, ConfigStore configs, Object obj, String ... conditions);
	List<Map<String,Object>> maps(String src, Object obj, String ... conditions);
	List<Map<String,Object>> maps(String src, int first, int last, Object obj, String ... conditions);
	List<Map<String,Object>> maps(String src, ConfigStore configs, String ... conditions);
	List<Map<String,Object>> maps(String src, String ... conditions);
	List<Map<String,Object>> maps(String src, int first, int last, String ... conditions);



	/**
	 * 列名转找成参数名 可以给condition()提供参数用来接收前端参数
	 * @param table 表
	 * @return List
	 */
	List<String> column2param(String table);



	/**
	 * 如果二级缓存开启 会从二级缓存中提取数据
	 * @param cache			对应ehcache缓存配置文件 中的cache.name
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		根据http等上下文构造查询条件
	 * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
	 * @param conditions 	固定查询条件
	 * @return DataSet
	 */
	DataSet caches(String cache, String src, ConfigStore configs, Object obj, String ... conditions);
	DataSet caches(String cache, String src, Object obj, String ... conditions);
	DataSet caches(String cache, String src, int first, int last, Object obj, String ... conditions);
	DataRow cache(String cache, String src, ConfigStore configs, Object obj, String ... conditions);
	DataRow cache(String cache, String src, Object obj, String ... conditions);

	DataSet caches(String cache, String src, ConfigStore configs,  String ... conditions);
	DataSet caches(String cache, String src, String ... conditions);
	DataSet caches(String cache, String src, int first, int last, String ... conditions);
	DataRow cache(String cache, String src, ConfigStore configs, String ... conditions);
	DataRow cache(String cache, String src, String ... conditions);



	/*多表查询,左右连接时使用*/
	DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	DataSet querys(RunPrepare prepare, Object obj, String ... conditions);
	DataSet querys(RunPrepare prepare, int first, int last, Object obj, String ... conditions);
	DataRow query(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	DataRow query(RunPrepare prepare, Object obj, String ... conditions);

	DataSet querys(RunPrepare prepare, ConfigStore configs,  String ... conditions);
	DataSet querys(RunPrepare prepare,  String ... conditions);
	DataSet querys(RunPrepare prepare, int first, int last,  String ... conditions);
	DataRow query(RunPrepare prepare, ConfigStore configs,  String ... conditions);
	DataRow query(RunPrepare prepare, String ... conditions);


	DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	DataSet caches(String cache, RunPrepare prepare, Object obj, String ... conditions);
	DataSet caches(String cache, RunPrepare prepare, int first, int last, Object obj, String ... conditions);
	DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	DataRow cache(String cache, RunPrepare prepare, Object obj, String ... conditions);

	DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions);
	DataSet caches(String cache, RunPrepare prepare, String ... conditions);
	DataSet caches(String cache, RunPrepare prepare, int first, int last, String ... conditions);
	DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions);
	DataRow cache(String cache, RunPrepare prepare, String ... conditions);

	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param channel 		channel
	 * @param src 			数据源(表或自定义SQL或SELECT语句)
	 * @param configs  		根据http等上下文构造查询条件
	 * @param conditions 	固定查询条件
	 * @return boolean
	 */
	boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions);
	boolean removeCache(String channel, String src, String ... conditions);
	boolean removeCache(String channel, String src, int first, int last, String ... conditions);
	/**
	 * 清空缓存
	 * @param channel channel
	 * @return boolean
	 */
	boolean clearCache(String channel);

	/* *****************************************************************************************************************
	 * 													EXISTS
	 ******************************************************************************************************************/

	/** 
	 * 是否存在 
	 * @param src  			数据源(表或自定义SQL或SELECT语句)
	 * @param configs  		根据http等上下文构造查询条件
	 * @param conditions 	固定查询条件
	 * @return boolean
	 */
	boolean exists(String src, ConfigStore configs, Object obj, String ... conditions);
	boolean exists(String src, Object obj, String ... conditions);
	boolean exists(String src, ConfigStore configs, String ... conditions);
	boolean exists(String src, String ... conditions);
	boolean exists(String src, DataRow row);
	boolean exists(DataRow row);

	/* *****************************************************************************************************************
	 * 													COUNT
	 ******************************************************************************************************************/
	int count(String src, ConfigStore configs, Object obj, String ... conditions);
	int count(String src, Object obj, String ... conditions);
	int count(String src, ConfigStore configs, String ... conditions);
	int count(String src, String ... conditions);



	/* *****************************************************************************************************************
	 * 													EXECUTE
	 ******************************************************************************************************************/

	/** 
	 * 执行 
	 * @param src  src
	 * @param configs  configs
	 * @param conditions  conditions
	 * @return int
	 */ 
	int execute(String src, ConfigStore configs, String ... conditions); 
	int execute(String src, String ... conditions); 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @param inputs  inputs
	 * @return boolean
	 */ 
	boolean executeProcedure(String procedure, String... inputs); 
	boolean execute(Procedure procedure, String... inputs);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @param first  first
	 * @param last  last
	 * @param inputs  inputs
	 * @return DataSet
	 */
	DataSet querysProcedure(String procedure, int first, int last , String ... inputs);
	DataSet querysProcedure(String procedure, PageNavi navi , String ... inputs);
	DataSet querysProcedure(String procedure, String ... inputs);
	DataSet querys(Procedure procedure, String ... inputs);
	DataSet querys(Procedure procedure, int first, int last,  String ... inputs);
	DataSet querys(Procedure procedure, PageNavi navi ,  String ... inputs);

	DataRow queryProcedure(String procedure, String ... inputs);
	DataRow query(Procedure procedure, String ... inputs);

	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/

	/**
	 * 根据ConfigStore中的条件+conditions条件删除
	 * @param table 表
	 * @param configs 匹配条件
	 * @param conditions  匹配条件
	 * @return 影响行数
	 */
	int delete(String table, ConfigStore configs, String ... conditions);
	/**
	 * 删除 根据columns列删除 可设置复合主键<br/>
	 * 注意:为了避免整表删除,columns必须提供否则会抛出异常 <br/>
	 * 如果要删除整表需要单独写原生的SQL调用execute(sql) <br/>
	 * @param dest 表 如果不指定表名则根据set中的表名删除
	 * @param set 数据
	 * @param columns 生成删除条件的列,如果不设置则根据主键删除
	 * @return 影响行数
	 */
	int delete(String dest, DataSet set, String ... columns);
	int delete(DataSet set, String ... columns);
	int delete(String dest, DataRow row, String ... columns);

	/**
	 * 根据columns列删除 <br/>
	 * 注意:为了避免整表删除,columns必须提供否则会抛出异常 <br/>
	 * 如果要删除整表需要单独写原生的SQL调用execute(sql) <br/>
	 * delete(User/DataRow, "TYPE","AGE")<br/>
	 * DELETE FROM USER WHERE TYPE = ? AND AGE = ?
	 * @param obj 实体对象或DataRow/Dataset
	 * @param columns 生成删除条件的列,如果不设置则根据主键删除
	 * @return 影响行数
	 */
	int delete(Object obj, String ... columns);

	/**
	 * 根据多列条件删除<br/>
	 * 注意:为了避免整表删除,values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * 以k,v,k,v或"k:v"形式提供参数<br/>
	 * delete("HR_EMPLOYEE","type","1", "age:20");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE TYPE = 1 AND AGE = 20<br/>
	 *<br/>
	 * 注意以下两咱情况,并不会忽略空值
	 *<br/>
	 * service.delete("HR_EMPLOYEE","ID","", "CODE:20");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE ID = '' AND CODE = 20<br/>
	 *<br/>
	 * service.delete("HR_EMPLOYEE","ID","1", "CODE:");<br/>
	 * DELETE FROM HR_EMPLOYEE WHERE ID = 1 AND CODE = ''<br/>
	 * @param table 表
	 * @param kvs key-value
	 * @return 影响行数
	 */
	int delete(String table, String ... kvs);

	/**
	 * 根据一列的多个值删除<br/>
	 * 注意:为了避免整表删除,values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * delete("USER", "TYPE", [1,2,3])<br/>
	 * DELETE FROM USER WHERE TYPE IN(1,2,3)
	 * @param table 表
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	public<T> int deletes(String table, String key, Collection<T> values);

	/**
	 * 根据一列的多个值删除<br/>
	 * 注意:为了避免整表删除,values必须提供否则会抛出异常<br/>
	 * 整表删除请调用service.execute("DELETE FROM TAB");或service.truncate("TAB“)<br/>
	 * delete("USER", "TYPE", "1","2","3")<br/>
	 * DELETE FROM USER WHERE TYPE IN(1,2,3)
	 * @param table 表
	 * @param key 名
	 * @param values 值集合
	 * @return 影响行数
	 */
	public<T> int deletes(String table, String key, T ... values);

	int truncate(String table);


	/* *****************************************************************************************************************
	 * 													METADATA
	 ******************************************************************************************************************/

	List<String> tables(boolean greedy, String catalog, String schema, String name, String types);
	List<String> tables(boolean greedy, String schema, String name, String types);
	List<String> tables(boolean greedy, String name, String types);
	List<String> tables(boolean greedy, String types);
	List<String> tables(boolean greedy);


	List<String> tables(String catalog, String schema, String name, String types);
	List<String> tables(String schema, String name, String types);
	List<String> tables(String name, String types);
	List<String> tables(String types);
	List<String> tables();


	List<String> views(boolean greedy, String catalog, String schema, String name, String types);
	List<String> views(boolean greedy, String schema, String name, String types);
	List<String> views(boolean greedy, String name, String types);
	List<String> views(boolean greedy, String types);
	List<String> views(boolean greedy);


	List<String> views(String catalog, String schema, String name, String types);
	List<String> views(String schema, String name, String types);
	List<String> views(String name, String types);
	List<String> views(String types);
	List<String> views();


	List<String> mtables(boolean greedy, String catalog, String schema, String name, String types);
	List<String> mtables(boolean greedy, String schema, String name, String types);
	List<String> mtables(boolean greedy, String name, String types);
	List<String> mtables(boolean greedy, String types);
	List<String> mtables(boolean greedy);

	List<String> mtables(String catalog, String schema, String name, String types);
	List<String> mtables(String schema, String name, String types);
	List<String> mtables(String name, String types);
	List<String> mtables(String types);
	List<String> mtables();


	List<String> columns(boolean greedy, Table table);
	List<String> columns(boolean greedy, String table);
	List<String> columns(boolean greedy, String catalog, String schema, String table);
	List<String> columns(Table table);
	List<String> columns(String table);
	List<String> columns(String catalog, String schema, String table);

	List<String> tags(boolean greedy,Table table);
	List<String> tags(boolean greedy, String catalog, String schema, String table);
	List<String> tags(boolean greedy, String table);
	List<String> tags(Table table);
	List<String> tags(String table);
	List<String> tags(String catalog, String schema, String table);


	void clearColumnCache(boolean greedy, String catalog, String schema, String table);
	void clearColumnCache(boolean greedy, String table);
	void clearColumnCache(boolean greedy);
	void clearColumnCache(String catalog, String schema, String table);
	void clearTagCache(boolean greedy, String catalog, String schema, String table);
	void clearColumnCache(String table);
	void clearColumnCache();
	void clearTagCache(String catalog, String schema, String table);




	public boolean save(Table table) throws Exception;
	public boolean save(Column column) throws Exception ;
	public boolean drop(Table table) throws Exception;
	public boolean drop(Column column) throws Exception;

	DDLService ddl();
	MetaDataService metadata();

	ConfigStore condition();


	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/
	interface MetaDataService{


		/* *****************************************************************************************************************
		 * 													database
		 ******************************************************************************************************************/

		/**
		 * 查询所有数据库
		 * @return databases
		 */
		LinkedHashMap<String,Database> databases();


		/* *****************************************************************************************************************
		 * 													table
		 ******************************************************************************************************************/

		/**
		 * 表是否存在
		 * @param table 表
		 * @return boolean
		 */
		boolean exists(Table table);
		boolean exists(boolean greedy, Table table);
		boolean exists(View view);
		boolean exists(boolean greedy, View view);
		/**
		 * tables
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
		 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
		 * @return tables
		 */
		<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String catalog, String schema, String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String schema, String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String types);
		<T extends Table> LinkedHashMap<String, T> tables(boolean greedy);

		<T extends Table> LinkedHashMap<String, T> tables(String catalog, String schema, String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(String schema, String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(String name, String types);
		<T extends Table> LinkedHashMap<String, T> tables(String types);
		<T extends Table> LinkedHashMap<String, T> tables();


		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
		 * @param struct 是否查询详细结构(列、索引、主外键、约束等)
		 * @return
		 */
		Table table(boolean greedy, String catalog, String schema, String name, boolean struct);
		Table table(boolean greedy, String schema, String name, boolean struct);
		Table table(boolean greedy, String name, boolean struct);

		Table table(String catalog, String schema, String name, boolean struct);
		Table table(String schema, String name, boolean struct);
		Table table(String name, boolean struct);


		Table table(boolean greedy, String catalog, String schema, String name);
		Table table(boolean greedy, String schema, String name);
		Table table(boolean greedy, String name);

		Table table(String catalog, String schema, String name);
		Table table(String schema, String name);
		Table table(String name);

		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/


		<T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String name, String types);
		<T extends View> LinkedHashMap<String, T> views(boolean greedy, String schema, String name, String types);
		<T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, String types);
		<T extends View> LinkedHashMap<String, T> views(boolean greedy, String types);
		<T extends View> LinkedHashMap<String, T> views(boolean greedy);

		<T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String name, String types);
		<T extends View> LinkedHashMap<String, T> views(String schema, String name, String types);
		<T extends View> LinkedHashMap<String, T> views(String name, String types);
		<T extends View> LinkedHashMap<String, T> views(String types);
		<T extends View> LinkedHashMap<String, T> views();


		View view(boolean greedy, String catalog, String schema, String name);
		View view(boolean greedy, String schema, String name);
		View view(boolean greedy, String name);

		View view(String catalog, String schema, String name);
		View view(String schema, String name);
		View view(String name);

		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		/**
		 * 主表
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param table 表名
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy,MasterTable table);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy);


		boolean exists(MasterTable table);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables(String types);
		<T extends MasterTable> LinkedHashMap<String, T> mtables();

		MasterTable mtable(boolean greedy, String catalog, String schema, String name);
		MasterTable mtable(boolean greedy, String schema, String name);
		MasterTable mtable(boolean greedy, String name);

		MasterTable mtable(String catalog, String schema, String name);
		MasterTable mtable(String schema, String name);
		MasterTable mtable(String name);


		/* *****************************************************************************************************************
		 * 													partition table
		 ******************************************************************************************************************/

		/**
		 * 子表
		 * @param table 表名
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy, PartitionTable table);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master);


		boolean exists(PartitionTable table);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(String master);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master);

		/**
		 * 根据主表与标签值查询分区表(子表)
		 * @param master 主表
		 * @param tags 标签值
		 * @param name 子表名
		 * @return PartitionTables
		 */
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags);

		PartitionTable ptable(boolean greedy, String catalog, String schema, String master, String name);
		PartitionTable ptable(boolean greedy, String schema, String master, String name);
		PartitionTable ptable(boolean greedy, String master, String name);

		<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags, String name);
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags);

		PartitionTable ptable(String catalog, String schema, String master, String name);
		PartitionTable ptable(String schema, String master, String name);
		PartitionTable ptable(String master, String name);

		/* *****************************************************************************************************************
		 * 													column
		 ******************************************************************************************************************/

		/**
		 * 列是否存在
		 * @param column 列
		 * @return boolean
		 */
		boolean exists(boolean greedy, Column column);
		boolean exists(boolean greedy,Table table, String column);
		boolean exists(boolean greedy, String table, String column);
		boolean exists(boolean greedy, String catalog, String schema, String table, String column);
		boolean exists(Column column);
		boolean exists(String table, String column);
		boolean exists(Table table, String column);
		boolean exists(String catalog, String schema, String table, String column);
		/**
		 * 查询表中所有的表,注意这里的map.KEY全部转大写
		 * @param table 表
		 * @return map
		 */
		<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table);
		<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table);
		<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String catalog, String schema, String table);
		<T extends Column> LinkedHashMap<String, T> columns(Table table);
		<T extends Column> LinkedHashMap<String, T> columns(String table);
		<T extends Column> LinkedHashMap<String, T> columns(String catalog, String schema, String table);

		/**
		 * 查询table中的column列
		 * @param table 表
		 * @param name 列名(不区分大小写)
		 * @return Column
		 */
		Column column(boolean greedy, Table table, String name);
		Column column(boolean greedy, String table, String name);
		Column column(boolean greedy, String catalog, String schema, String table, String name);
		Column column(Table table, String name);
		Column column(String table, String name);
		Column column(String catalog, String schema, String table, String name);


		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/

		<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy,Table table);
		<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table);
		<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table);
		<T extends Tag> LinkedHashMap<String, T> tags(Table table);
		<T extends Tag> LinkedHashMap<String, T> tags(String table);
		<T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table);


		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		PrimaryKey primary(boolean greedy,Table table);
		PrimaryKey primary(boolean greedy,String table);
		PrimaryKey primary(boolean greedy,String catalog, String schema, String table);
		PrimaryKey primary(Table table);
		PrimaryKey primary(String table);
		PrimaryKey primary(String catalog, String schema, String table);

		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy,Table table);
		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy,String table);
		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy,String catalog, String schema, String table);
		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(Table table);
		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(String table);
		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(String catalog, String schema, String table);

		ForeignKey foreign(boolean greedy,Table table, String ... columns);
		ForeignKey foreign(boolean greedy,Table table, List<String> columns);
		ForeignKey foreign(boolean greedy,String table, String ... columns);
		ForeignKey foreign(boolean greedy,String table, List<String> columns);
		//与上面的foreign(boolean greedy,String table, String ... columns)冲突
		//ForeignKey foreign(boolean greedy,String catalog, String schema, String table, String ... columns);
		ForeignKey foreign(boolean greedy,String catalog, String schema, String table, List<String> columns);
		ForeignKey foreign(Table table, String ... columns);
		ForeignKey foreign(Table table, List<String> columns);
		ForeignKey foreign(String table, String ... columns);
		ForeignKey foreign(String table, List<String> columns);
		//与上面的foreign(String table, String ... columns)冲突
		//ForeignKey foreign(String catalog, String schema, String table, String ... columns);
		ForeignKey foreign(String catalog, String schema, String table, List<String> columns);
		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy,Table table);
		<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy,String table);
		<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy,String catalog, String schema, String table);

		<T extends Index> LinkedHashMap<String, T> indexs(Table table);
		<T extends Index> LinkedHashMap<String, T> indexs(String table);
		<T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table);


		Index index(boolean greedy,Table table, String name);
		Index index(boolean greedy,String table, String name);
		Index index(boolean greedy,String name);
		Index index(Table table, String name);
		Index index(String table, String name);
		Index index(String name);


		/* *****************************************************************************************************************
		 * 													constraint
		 ******************************************************************************************************************/

		<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy,Table table);
		<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy,String table);
		<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy,String catalog, String schema, String table);
		<T extends Constraint> LinkedHashMap<String, T> constraints(Table table);
		<T extends Constraint> LinkedHashMap<String, T> constraints(String table);
		<T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table);
		Constraint constraint(boolean greedy,Table table, String name);
		Constraint constraint(boolean greedy,String table, String name);
		Constraint constraint(boolean greedy,String name);
		Constraint constraint(Table table, String name);
		Constraint constraint(String table, String name);
		Constraint constraint(String name);


		/* *****************************************************************************************************************
		 * 													trigger
		 ******************************************************************************************************************/
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<org.anyline.entity.data.Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String catalog, String schema, String table, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String schema, String table, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String table, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(boolean greedy);

		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(String catalog, String schema, String name, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(String schema, String name, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(String name, List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers(List<Trigger.EVENT> events);
		<T extends org.anyline.entity.data.Trigger> LinkedHashMap<String, T> triggers();


		Trigger trigger(boolean greedy, String catalog, String schema, String name);
		Trigger trigger(boolean greedy, String schema, String name);
		Trigger trigger(boolean greedy, String name);

		Trigger trigger(String catalog, String schema, String name);
		Trigger trigger(String schema, String name);
		Trigger trigger(String name);

		/* *****************************************************************************************************************
		 * 													procedure
		 ******************************************************************************************************************/

		<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String schema, String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy);

		<T extends Procedure> LinkedHashMap<String, T> procedures(String catalog, String schema, String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures(String schema, String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures(String name);
		<T extends Procedure> LinkedHashMap<String, T> procedures();


		Procedure procedure(boolean greedy, String catalog, String schema, String name);
		Procedure procedure(boolean greedy, String schema, String name);
		Procedure procedure(boolean greedy, String name);

		Procedure procedure(String catalog, String schema, String name);
		Procedure procedure(String schema, String name);
		Procedure procedure(String name);

		/* *****************************************************************************************************************
		 * 													function
		 ******************************************************************************************************************/

		<T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name);
		<T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String schema, String name);
		<T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String name);
		<T extends Function> LinkedHashMap<String, T> functions(boolean greedy);

		<T extends Function> LinkedHashMap<String, T> functions(String catalog, String schema, String name);
		<T extends Function> LinkedHashMap<String, T> functions(String schema, String name);
		<T extends Function> LinkedHashMap<String, T> functions(String name);
		<T extends Function> LinkedHashMap<String, T> functions();


		Function function(boolean greedy, String catalog, String schema, String name);
		Function function(boolean greedy, String schema, String name);
		Function function(boolean greedy, String name);

		Function function(String catalog, String schema, String name);
		Function function(String schema, String name);
		Function function(String name);

	}



	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	interface DDLService{


		/* *****************************************************************************************************************
		 * 													table
		 ******************************************************************************************************************/

		boolean save(Table table) throws Exception;
		boolean create(Table table) throws Exception;
		boolean alter(Table table) throws Exception;
		boolean drop(Table table) throws Exception;

		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/

		boolean save(View view) throws Exception;
		boolean create(View view) throws Exception;
		boolean alter(View view) throws Exception;
		boolean drop(View view) throws Exception;


		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		boolean save(MasterTable table) throws Exception;
		boolean create(MasterTable table) throws Exception;
		boolean alter(MasterTable table) throws Exception;
		boolean drop(MasterTable table) throws Exception;


		/* *****************************************************************************************************************
		 * 													partition table
		 ******************************************************************************************************************/

		boolean save(PartitionTable table) throws Exception;
		boolean create(PartitionTable table) throws Exception;
		boolean alter(PartitionTable table) throws Exception;
		boolean drop(PartitionTable table) throws Exception;


		/* *****************************************************************************************************************
		 * 													column
		 ******************************************************************************************************************/
		/**
		 * 修改列  名称 数据类型 位置 默认值
		 * 执行save前先调用column.update()设置修改后的属性
		 * column.update().setName().setDefaultValue().setAfter()....
		 * @param column 列
		 * @throws Exception 异常 SQL异常
		 */
		boolean save(Column column) throws Exception;
		boolean add(Column column) throws Exception;
		boolean alter(Column column) throws Exception;
		boolean drop(Column column) throws Exception;


		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/

		boolean save(Tag tag) throws Exception;
		boolean add(Tag tag) throws Exception;
		boolean alter(Tag tag) throws Exception;
		boolean drop(Tag tag) throws Exception;


		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		boolean add(PrimaryKey primary) throws Exception;
		boolean alter(PrimaryKey primary) throws Exception;
		boolean drop(PrimaryKey primary) throws Exception;
		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		boolean add(ForeignKey foreign) throws Exception;
		boolean alter(ForeignKey foreign) throws Exception;
		boolean drop(ForeignKey foreign) throws Exception;

		/**
		 * 复合外键时调用
		 * @param table 表
		 * @param columns 如果有多列 按复合外键处理,如果需要删除外个外键应该调用多次drop
		 * @return boolean
		 * @throws Exception Exception
		 */
		boolean drop(Table table, String ... columns) throws Exception;
		boolean add(String table, String column, String refTable, String refColumn) throws Exception;

		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		boolean add(Index index) throws Exception;
		boolean alter(Index index) throws Exception;
		boolean drop(Index index) throws Exception;


		/* *****************************************************************************************************************
		 * 													constraint
		 ******************************************************************************************************************/
		/**
		 * 修改约束
		 * @param constraint 约束
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean add(Constraint constraint) throws Exception;
		boolean alter(Constraint constraint) throws Exception;
		boolean drop(Constraint constraint) throws Exception;

		/* *****************************************************************************************************************
		 * 													trigger
		 ******************************************************************************************************************/
		/**
		 * 触发器
		 * @param trigger 触发器
		 * @return trigger
		 * @throws Exception 异常 Exception
		 */
		boolean create(Trigger trigger) throws Exception;
		boolean alter(Trigger trigger) throws Exception;
		boolean drop(Trigger trigger) throws Exception;

		/* *****************************************************************************************************************
		 * 													procedure
		 ******************************************************************************************************************/
		/**
		 * 存储过程
		 * @param procedure 存储过程
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean create(Procedure procedure) throws Exception;
		boolean alter(Procedure procedure) throws Exception;
		boolean drop(Procedure procedure) throws Exception;

		/* *****************************************************************************************************************
		 * 													function
		 ******************************************************************************************************************/
		/**
		 * 函数
		 * @param function 函数
		 * @return boolean
		 * @throws Exception 异常 Exception
		 */
		boolean create(Function function) throws Exception;
		boolean alter(Function function) throws Exception;
		boolean drop(Function function) throws Exception;

	}
}
