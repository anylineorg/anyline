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

import org.anyline.dao.AnylineDao;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.util.BeanUtil;

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
	AnylineService setDao(AnylineDao dao);


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
	 * @param checkPrimary 是否检测主键重复
	 * @param fixs 需要插入哪些列
	 * @param columns 需要插入哪些列
	 * @return 影响行数
	 */
	long insert(String dest, Object data, boolean checkPrimary, List<String> fixs, String ... columns);
	default long insert(Object data, boolean checkPrimary, List<String> fixs, String ... columns){
		return insert(null, data, checkPrimary, fixs, columns);
	}
	default long insert(Object data, List<String> fixs, String ... columns){
		return insert(null, data, false, fixs, columns);
	}
	default long insert(String dest, Object data, List<String> fixs, String ... columns){
		return insert(dest, data, false, fixs, columns);
	}
	default long insert(String dest, Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return insert(dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
	}
	default long insert(Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return insert(null, data, checkPrimary, BeanUtil.array2list(fixs, columns));
	}
	default long insert(Object data, String[] fixs, String ... columns){
		return insert(null, data, false, fixs, columns);
	}
	default long insert(String dest, Object data, String[] fixs, String ... columns){
		return insert(dest, data, false, fixs, columns);
	}
	default long insert(String dest, Object data, boolean checkPrimary, String ... columns){
		return insert(dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(Object data, boolean checkPrimary, String ... columns){
		return insert(null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(Object data, String ... columns){
		return insert(null, data, false, columns);
	}
	default long insert(String dest, Object data, String ... columns){
		return insert(dest, data, false, columns);
	}

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
	 * @param async	  	是否异步
	 * @param fixs	  	需要更新的列
	 * @param columns	需要更新的列
	 * @param dest	   	表
	 * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
	 * @param configs 	更新条件
	 * @return int 影响行数
	 */
	long update(boolean async, String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns);
	default long update(String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns){
		return update(false, dest, data, configs, fixs, columns);
	}
	default long update(String dest, Object data, List<String> fixs, String ... columns){
		return update(false, dest, data, null, fixs, columns);
	}
	default long update(String dest, Object data, String[] fixs, String ... columns){
		return update(false, dest, data, null, BeanUtil.array2list(fixs, columns));
	}
	default long update(String dest, Object data, ConfigStore configs, String[] fixs, String ... columns){
		return update(false, dest, data, configs, BeanUtil.array2list(fixs, columns));
	}
	default long update(String dest, Object data, String ... columns){
		return update(false, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(String dest, Object data, ConfigStore configs, String ... columns){
		return update(false, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, List<String> fixs, String ... columns){
		return update(false, null, data, configs, fixs, columns);
	}
	default long update(Object data, List<String> fixs, String ... columns){
		return update(false, null, data, null, fixs, columns);
	}
	default long update(Object data, String[] fixs, String ... columns){
		return update(false, null, data, null, BeanUtil.array2list(fixs, columns));
	}
	default long update(Object data, ConfigStore configs, String[] fixs, String ... columns){
		return update(false, null, data, configs, BeanUtil.array2list(fixs, columns));
	}
	default long update(Object data, String ... columns){
		return update(false, null, data, null, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, String ... columns){
		return update(false, null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(boolean async, String dest, Object data, List<String> fixs, String ... columns){
		return update(async, dest, data, null, fixs, columns);
	}
	default long update(boolean async, String dest, Object data, String[] fixs, String ... columns){
		return update(async, dest, data, null, BeanUtil.array2list(fixs, columns));
	}
	default long update(boolean async, String dest, Object data, ConfigStore configs, String[] fixs, String ... columns){
		return update(async, dest, data, configs, BeanUtil.array2list(fixs, columns));
	}
	default long update(boolean async, String dest, Object data, String ... columns){
		return update(async, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(boolean async, String dest, Object data, ConfigStore configs, String ... columns){
		return update(async, dest, data, configs, BeanUtil.array2list(columns));
	}
	
	default long update(boolean async, Object data, List<String> fixs, String ... columns){
		return update(async, null, data, null, fixs, columns);
	}
	default long update(boolean async, Object data, ConfigStore configs, List<String> fixs, String ... columns){
		return update(async, null, data, configs, fixs, columns);
	}
	default long update(boolean async, Object data, String[] fixs, String ... columns){
		return update(async, null, data, null, BeanUtil.array2list(fixs, columns));
	}
	default long update(boolean async, Object data, ConfigStore configs, String[] fixs, String ... columns){
		return update(async, null, data, configs, BeanUtil.array2list(fixs, columns));
	}
	default long update(boolean async, Object data, String ... columns){
		return update(async, null, data, null, BeanUtil.array2list(columns));
	}
	default long update(boolean async, Object data, ConfigStore configs, String ... columns){
		return update(async, null, data, configs, BeanUtil.array2list(columns));
	}


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
	 * @param checkPrimary 是否检测主键
	 * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
	 * @param columns 指定更新或保存的列
	 * @param dest 表
	 * @return 影响行数
	 */
	long save(String dest, Object data, boolean checkPrimary, List<String> fixs, String ... columns);
	default long save(Object data, boolean checkPrimary, List<String> fixs, String ... columns){
		return save(null, data, checkPrimary, BeanUtil.merge(fixs, columns));
	}
	default long save(Object data, List<String> fixs, String ... columns){
		return save(null, data, false, fixs, columns);
	}
	default long save(String dest, Object data, List<String> fixs, String ... columns){
		return save(dest, data, false, fixs, columns);
	}

	default long save(String dest, Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return save(dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
	}
	default long save(Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return save(null, data, checkPrimary, BeanUtil.array2list(fixs, columns));
	}
	default long save(Object data, String[] fixs, String ... columns){
		return save(null, data, false, fixs, columns);
	}
	default long save(String dest, Object data, String[] fixs, String ... columns){
		return save(dest, data, false, fixs, columns);
	}
	default long save(String dest, Object data, boolean checkPrimary, String ... columns){
		return save(dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(Object data, boolean checkPrimary, String ... columns){
		return save(null, data, checkPrimary, columns);
	}
	default long save(Object data, String ... columns){
		return save(null, data, false, columns);
	}
	default long save(String dest, Object data, String ... columns){
		return save(dest, data, false, columns);
	}


	/**
	 * 保存(insert|update)根据是否有主键值确定insert或update
	 * @param async 是否异步执行
	 * @param data  数据
	 * @param checkPrimary 是否检测主键
	 * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
	 * @param columns 指定更新或保存的列
	 * @param dest 表
	 * @return 影响行数
	 */
	long save(boolean async, String dest, Object data, boolean checkPrimary, List<String> fixs, String ... columns);
	default long save(boolean async, Object data, boolean checkPrimary, List<String> fixs, String ... columns){
		return save(async, null, data, checkPrimary, fixs, columns);
	}
	default long save(boolean async, Object data, List<String> fixs, String ... columns){
		return save(async, null, data, false, fixs, columns);
	}
	default long save(boolean async, String dest, Object data, List<String> fixs, String ... columns){
		return save(async, dest, data, false, fixs, columns);
	}

	default long save(boolean async, String dest, Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return save(async, dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
	}
	default long save(boolean async, Object data, boolean checkPrimary, String[] fixs, String ... columns){
		return save(async, null, data, checkPrimary, fixs, columns);
	}
	default long save(boolean async, Object data, String[] fixs, String ... columns){
		return save(async, null, data, false, fixs, columns);
	}
	default long save(boolean async, String dest, Object data, String[] fixs, String ... columns){
		return save(async, dest, data, false, fixs, columns);
	}

	default long save(boolean async, String dest, Object data, boolean checkPrimary, String ... columns){
		return save(async, dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(boolean async, Object data, boolean checkPrimary, String ... columns){
		return save(async, null, data, checkPrimary, columns);
	}
	default long save(boolean async, Object data, String ... columns){
		return save(async, null, data, false, columns);
	}
	default long save(boolean async, String dest, Object data, String ... columns){
		return save(async, dest, data, false, columns);
	}


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
	default DataSet querys(String src, int first, int last, ConfigStore configs, Object obj, String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return querys(src, configs, obj, conditions);
	}
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
	default DataSet querys(String src, int first, int last, ConfigStore configs, String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		if(null == configs){
			configs = new DefaultConfigStore();
		}
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return querys(src, configs, conditions);
	}
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
	List<Map<String,Object>> maps(String src, PageNavi nvi, String ... conditions);
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
	default DataSet caches(String cache, String src, int first, int last, ConfigStore configs, Object obj, String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return caches(cache, src, configs, obj, conditions);
	}
	DataSet caches(String cache, String src, Object obj, String ... conditions);
	DataSet caches(String cache, String src, int first, int last, Object obj, String ... conditions);
	DataRow cache(String cache, String src, ConfigStore configs, Object obj, String ... conditions);
	DataRow cache(String cache, String src, Object obj, String ... conditions);

	DataSet caches(String cache, String src, ConfigStore configs,  String ... conditions);
	default DataSet caches(String cache, String src, int first, int last, ConfigStore configs,  String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return caches(cache, src, configs, conditions);
	}
	DataSet caches(String cache, String src, String ... conditions);
	DataSet caches(String cache, String src, int first, int last, String ... conditions);
	DataRow cache(String cache, String src, ConfigStore configs, String ... conditions);
	DataRow cache(String cache, String src, String ... conditions);



	/*多表查询,左右连接时使用*/
	DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default DataSet querys(RunPrepare prepare, int first, int last, ConfigStore configs, Object obj, String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return querys(prepare, configs, obj, conditions);
	}
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
	default DataRow cache(String cache, RunPrepare prepare, int first, int last, ConfigStore configs, String ... conditions){
		DefaultPageNavi navi = new DefaultPageNavi();
		navi.setFirstRow(first);
		navi.setLastRow(last);
		configs.setPageNavi(navi);
		return cache(cache, prepare, configs, conditions);
	}
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

	/**
	 * count
	 * @param src 表或视图或自定义SQL
	 * @param configs 过滤条件
	 * @param obj 根据obj生成的过滤条件
	 * @param conditions 简单过滤条件
	 * @return long
	 */
	long count(String src, ConfigStore configs, Object obj, String ... conditions);
	default long count(String src, Object obj, String ... conditions){
		return count(src, null, obj, conditions);
	}
	default long count(String src, ConfigStore configs, String ... conditions){
		return count(src, configs, null, conditions);
	}
	default long count(String src, String ... conditions){
		return count(src, null, null, conditions);
	}

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
	long execute(String src, ConfigStore configs, String ... conditions); 
	long execute(String src, String ... conditions); 
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
	long delete(String table, ConfigStore configs, String ... conditions);
	/**
	 * 删除 根据columns列删除 可设置复合主键<br/>
	 * 注意:为了避免整表删除,columns必须提供否则会抛出异常 <br/>
	 * 如果要删除整表需要单独写原生的SQL调用execute(sql) <br/>
	 * @param dest 表 如果不指定表名则根据set中的表名删除
	 * @param set 数据
	 * @param columns 生成删除条件的列,如果不设置则根据主键删除
	 * @return 影响行数
	 */
	long delete(String dest, DataSet set, String ... columns);
	long delete(DataSet set, String ... columns);
	long delete(String dest, DataRow row, String ... columns);

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
	long delete(Object obj, String ... columns);

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
	long delete(String table, String ... kvs);

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
	public<T> long deletes(String table, String key, Collection<T> values);

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
	public<T> long deletes(String table, String key, T ... values);

	int truncate(String table);


	/* *****************************************************************************************************************
	 * 													METADATA
	 ******************************************************************************************************************/

	List<String> tables(boolean greedy, String catalog, String schema, String name, String types);
	default List<String> tables(boolean greedy, String schema, String name, String types){
		return tables(greedy, null, schema, name, types);
	}
	default List<String> tables(boolean greedy, String name, String types){
		return tables(greedy, null, null, name, types);
	}
	default List<String> tables(boolean greedy, String types){
		return tables(greedy, null, null, null, types);
	}
	default List<String> tables(boolean greedy){
		return tables(greedy, null);
	}
	default List<String> tables(String catalog, String schema, String name, String types){
		return tables(false, catalog, schema, name, types);
	}
	default List<String> tables(String schema, String name, String types){
		return tables(false, null, schema, name, types);
	}
	default List<String> tables(String name, String types){
		return tables(false, null, null, name, types);
	}
	default List<String> tables(String types){
		return tables(false, null, null, null, types);
	}
	default List<String> tables(){
		return tables(false, null);
	}


	List<String> views(boolean greedy, String catalog, String schema, String name, String types);
	default List<String> views(boolean greedy, String schema, String name, String types){
		return views(greedy, null, schema, name, types);
	}
	default List<String> views(boolean greedy, String name, String types){
		return views(greedy, null, null, name, types);
	}
	default List<String> views(boolean greedy, String types){
		return views(greedy, null, null, null, types);
	}
	default List<String> views(boolean greedy){
		return views(greedy, null);
	}
	default List<String> views(String catalog, String schema, String name, String types){
		return views(false, catalog, schema, name, types);
	}
	default List<String> views(String schema, String name, String types){
		return views(false, null, schema, name, types);
	}
	default List<String> views(String name, String types){
		return views(false, null, null, name, types);
	}
	default List<String> views(String types){
		return views(false, null, null, null, types);
	}
	default List<String> views(){
		return views(false, null);
	}


	List<String> mtables(boolean greedy, String catalog, String schema, String name, String types);
	default List<String> mtables(boolean greedy, String schema, String name, String types){
		return mtables(greedy, null, schema, name, types);
	}
	default List<String> mtables(boolean greedy, String name, String types){
		return mtables(greedy, null, null, name, types);
	}
	default List<String> mtables(boolean greedy, String types){
		return mtables(greedy, null, null, null, types);
	}
	default List<String> mtables(boolean greedy){
		return mtables(greedy, "STABLE");
	}
	default List<String> mtables(String catalog, String schema, String name, String types){
		return mtables(false, catalog, schema, name, types);
	}
	default List<String> mtables(String schema, String name, String types){
		return mtables(false, null, schema, name, types);
	}
	default List<String> mtables(String name, String types){
		return mtables(false, null, null, name, types);
	}
	default List<String> mtables(String types){
		return mtables(false, null, null, null, types);
	}
	default List<String> mtables(){
		return mtables(false, "STABLE");
	}


	List<String> columns(boolean greedy, Table table);
	default List<String> columns(boolean greedy, String table){
		return columns(greedy, null, null, table);
	}
	default List<String> columns(boolean greedy, String catalog, String schema, String table){
		return columns(greedy, new Table(catalog, schema, table));
	}
	default List<String> columns(Table table){
		return columns(false, table);
	}
	default List<String> columns(String table){
		return columns(false, null, null, table);
	}
	default List<String> columns(String catalog, String schema, String table){
		return columns(false, catalog, schema, table);
	}

	List<String> tags(boolean greedy, Table table);
	default List<String> tags(boolean greedy, String catalog, String schema, String table){
		return tags(greedy, new Table(catalog, schema, table));
	}
	default List<String> tags(boolean greedy, String table){
		return tags(greedy, null, null, table);
	}
	default List<String> tags(Table table){
		return tags(false, table);
	}
	default List<String> tags(String table){
		return tags(false, new Table(table));
	}
	default List<String> tags(String catalog, String schema, String table){
		return tags(false, new Table(catalog, schema, table));
	}





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
		/* *************************************************************************************************************
		 * 													database
		 **************************************************************************************************************/
		/**
		 * 查询所有数据库
		 * @return databases
		 */
		LinkedHashMap<String, Database> databases();
		Database database(String name);

		/* *************************************************************************************************************
		 * 													table
		 **************************************************************************************************************/

		/**
		 * 表是否存在
		 * @param table 表
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @return boolean
		 */
		boolean exists(boolean greedy, Table table);
		default boolean exists(Table table){
			return exists(false, table);
		}
		boolean exists(boolean greedy, View view);
		default boolean exists(View view){
			return exists(false, view);
		}
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
		default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String schema, String name, String types){
			return tables(greedy, null, schema, name, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String name, String types){
			return tables(greedy, null, null, name, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String types){
			return tables(greedy, null, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy){
			return tables(greedy, null);
		}

		default <T extends Table> LinkedHashMap<String, T> tables(String catalog, String schema, String name, String types){
			return tables(false, catalog, schema, name, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String schema, String name, String types){
			return tables(false, null, schema, name, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String name, String types){
			return tables(false, null, null, name, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(String types){
			return tables(false, null, types);
		}
		default <T extends Table> LinkedHashMap<String, T> tables(){
			return tables(false, null);
		}


		/**
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
		 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
		 * @param name 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
		 * @param struct 是否查询详细结构(列、索引、主外键、约束等)
		 * @return Table
		 */
		Table table(boolean greedy, String catalog, String schema, String name, boolean struct);
		default Table table(boolean greedy, String schema, String name, boolean struct){
			return table(greedy, null, schema, name, struct);
		}
		default Table table(boolean greedy, String name, boolean struct){
			return table(greedy, null, null, name, struct);
		}

		default Table table(String catalog, String schema, String name, boolean struct){
			return table(false, catalog, schema, name, struct);
		}
		default Table table(String schema, String name, boolean struct){
			return table(false, null, schema, name, struct);
		}
		default Table table(String name, boolean struct){
			return table(false, null, null, name, struct);
		}


		default Table table(boolean greedy, String catalog, String schema, String name){
			return table(greedy, catalog, schema, name, true);
		}
		default Table table(boolean greedy, String schema, String name){
			return table(greedy, null, schema, name, true);
		}
		default Table table(boolean greedy, String name){
			return table(greedy, null, null, name, true);
		}

		default Table table(String catalog, String schema, String name){
			return table(false, catalog, schema, name, true);
		}
		default Table table(String schema, String name){
			return table(false, null, schema, name, true);
		}
		default Table table(String name){
			return table(false, null, null, name, true);
		}

		/**
		 * 表的创建SQL
		 * @param table table
		 * @param init 是否还原初始状态 默认false
		 * @return ddl
		 */
		List<String> ddl(Table table, boolean init);
		default List<String> ddl(String table, boolean init){
			return ddl(new Table(table), init);
		}
		default List<String> ddl(Table table){
			return ddl(table, false);
		}
		default List<String> ddl(String table){
			return ddl(new Table(table));
		}


		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/


		<T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String name, String types);
		default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String schema, String name, String types){
			return views(greedy, null, schema, name, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, String types){
			return views(greedy, null, null, name, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String types){
			return views(greedy, null, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(boolean greedy){
			return views(greedy, null);
		}

		<T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String name, String types);
		default <T extends View> LinkedHashMap<String, T> views(String schema, String name, String types){
			return views(false, null, schema, name, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(String name, String types){
			return views(false, null, null, name, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(String types){
			return views(false, null, types);
		}
		default <T extends View> LinkedHashMap<String, T> views(){
			return views(false, null);
		}


		View view(boolean greedy, String catalog, String schema, String name);
		default View view(boolean greedy, String schema, String name){
			return view(greedy, null, schema, name);
		}
		default View view(boolean greedy, String name){
			return view(greedy, null, null, name);
		}

		default View view(String catalog, String schema, String name){
			return view(false, catalog, schema, name);
		}
		default View view(String schema, String name){
			return view(false, null, schema, name);
		}
		default View view(String name){
			return view(false, null, null, name);
		}
		List<String> ddl(View view);

		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		/**
		 * 主表
		 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
		 * @param table 表名
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy, MasterTable table);
		default boolean exists(MasterTable table){
			return exists(false, table);
		}
		<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String name, String types);
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types){
			return mtables(greedy, null, schema, name, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types){
			return mtables(greedy, null, null, name, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types){
			return mtables(greedy, null, null, null, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy){
			return mtables(greedy, "STABLE");
		}


		default <T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String name, String types){
			return mtables(false, catalog, schema, name, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types){
			return mtables(false,  schema, name, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types){
			return mtables(false,  name, types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(String types){
			return mtables(false,  types);
		}
		default <T extends MasterTable> LinkedHashMap<String, T> mtables(){
			return mtables(false);
		}

		MasterTable mtable(boolean greedy, String catalog, String schema, String name, boolean strut);
		default MasterTable mtable(boolean greedy, String schema, String name, boolean strut){
			return mtable(greedy, null, schema, name, strut);
		}
		default MasterTable mtable(boolean greedy, String name, boolean strut){
			return mtable(greedy, null, null, name, strut);
		}

		default MasterTable mtable(String catalog, String schema, String name, boolean strut){
			return mtable(false, catalog, schema, name, strut);
		}
		default MasterTable mtable(String schema, String name, boolean strut){
			return mtable(false, schema, name, strut);
		}
		default MasterTable mtable(String name, boolean strut){
			return mtable(false, name, strut);
		}


		default MasterTable mtable(boolean greedy, String catalog, String schema, String name){
			return mtable(greedy, catalog, schema, name, true);
		}
		default MasterTable mtable(boolean greedy, String schema, String name){
			return mtable(greedy, schema, name, true);
		}
		default MasterTable mtable(boolean greedy, String name){
			return mtable(greedy, name, true);
		}
		default MasterTable mtable(String catalog, String schema, String name){
			return mtable(false, catalog, schema, name, true);
		}
		default MasterTable mtable(String schema, String name){
			return mtable(false, schema, name, true);
		}
		default MasterTable mtable(String name){
			return mtable(false, name, true);
		}
		List<String> ddl(MasterTable table);


		/* *************************************************************************************************************
		 * 													partition table
		 **************************************************************************************************************/

		/**
		 * 子表
		 * @param table 表名
		 * @return LinkedHashMap
		 */
		boolean exists(boolean greedy, PartitionTable table);
		default boolean exists(PartitionTable table){
			return exists(false, table);
		}
		/**
		 * 根据主表与标签值查询分区表(子表)
		 * @param master 主表
		 * @param tags 标签值
		 * @param name 子表名
		 * @return PartitionTables
		 */
		<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags, String name);
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags, String name){
			return ptables(false, master, tags, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags){
			return ptables(greedy, master, tags, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags){
			return ptables(false, master, tags, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name){
			return ptables(greedy, new MasterTable(catalog, schema, master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name){
			return ptables(greedy, new MasterTable(schema, master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name){
			return ptables(greedy, new MasterTable(master), null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master){
			return ptables(greedy, new MasterTable(master), null, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master){
			return ptables(greedy, master, null, null);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, String name){
			return ptables(greedy, master, null, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name){
			return ptables(false, catalog, schema, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name){
			return ptables(false, schema, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name){
			return ptables(false, master, name);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master){
			return ptables(false, master);
		}
		default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master){
			return ptables(false, master);
		}


		PartitionTable ptable(boolean greedy, MasterTable master, String name);
		default PartitionTable ptable(boolean greedy, String catalog, String schema, String master, String name){
			return ptable(greedy, new MasterTable(catalog, schema, master), name);
		}
		default PartitionTable ptable(boolean greedy, String schema, String master, String name){
			return ptable(greedy, new MasterTable(schema, master), name);
		}
		default PartitionTable ptable(boolean greedy, String master, String name){
			return ptable(greedy, new MasterTable(master), name);
		}
		default PartitionTable ptable(String catalog, String schema, String master, String name){
			return ptable(false, catalog, schema, master, name);
		}
		default PartitionTable ptable(String schema, String master, String name){
			return ptable(false, new MasterTable(schema, master), name);
		}
		default PartitionTable ptable(String master, String name){
			return ptable(false, new MasterTable(master), name);
		}

		List<String> ddl(PartitionTable table);
		/* *****************************************************************************************************************
		 * 													column
		 ******************************************************************************************************************/
		/**
		 * 列是否存在
		 * @param column 列
		 * @return boolean
		 */
		boolean exists(boolean greedy, Table table, Column column);
		default boolean exists(boolean greedy, Column column){
			return exists(greedy, null, column);
		}
		default boolean exists(boolean greedy, String table, String column){
			return exists(greedy, new Table(table), new Column(column));
		}
		default boolean exists(boolean greedy, String catalog, String schema, String table, String column){
			return exists(greedy, new Table(catalog, schema, table), new Column(column));
		}
		default boolean exists(Column column){
			return exists(false, null, column); 
		}
		default boolean exists(String table, String column){
			return exists(false, new Table(table), new Column(column));
		}
		default boolean exists(Table table, String column){
			return exists(false, table, new Column(column));
		}
		default boolean exists(String catalog, String schema, String table, String column){
			return exists(false, new Table(catalog, schema, table), new Column(column));
		}
		/**
		 * 查询表中所有的表,注意这里的map.KEY全部转大写
		 * @param table 表
		 * @return map
		 */
		<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table);
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table){
			return columns(greedy, new Table(table));
		}
		default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String catalog, String schema, String table){
			return columns(greedy, new Table(catalog, schema, table));
		}
		default <T extends Column> LinkedHashMap<String, T> columns(Table table){
			return columns(false, table);
		}
		default <T extends Column> LinkedHashMap<String, T> columns(String table){
			return columns(false, new Table(table));
		}
		default <T extends Column> LinkedHashMap<String, T> columns(String catalog, String schema, String table){
			return columns(false, new Table(catalog, schema, table));
		}

		/**
		 * 查询table中的column列
		 * @param table 表
		 * @param name 列名(不区分大小写)
		 * @return Column
		 */
		Column column(boolean greedy, Table table, String name);
		default Column column(boolean greedy, String table, String name){
			return column(greedy, new Table(table), name);
		}
		default Column column(boolean greedy, String catalog, String schema, String table, String name){
			return column(greedy, new Table(catalog, schema, table), name);
		}
		default Column column(Table table, String name){
			return column(false, table, name);
		}
		default Column column(String table, String name){
			return column(false, new Table(table), name);
		}
		default Column column(String catalog, String schema, String table, String name){
			return column(false, new Table(catalog, schema, table), name);
		}


		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/
		<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table);
		default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table){
			return tags(greedy, new Table(table));
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table){
			return tags(greedy, new Table(catalog, schema, table));
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(Table table){
			return tags(false, table);
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(String table){
			return tags(false, null, null, table);
		}
		default <T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table){
			return tags(false, new Table(catalog, schema, table));
		}


		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		PrimaryKey primary(boolean greedy, Table table);
		default PrimaryKey primary(boolean greedy, String table){
			return primary(greedy, new Table(table));
		}
		default PrimaryKey primary(boolean greedy, String catalog, String schema, String table){
			return primary(greedy, new Table(catalog, schema, table));
		}
		default PrimaryKey primary(Table table){
			return primary(false, table);
		}
		default PrimaryKey primary(String table){
			return primary(false, new Table(table));
		}
		default PrimaryKey primary(String catalog, String schema, String table){
			return primary(false, new Table(catalog, schema, table));
		}

		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table);
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String table){
			return foreigns(greedy, new Table(table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String catalog, String schema, String table){
			return foreigns(greedy, new Table(catalog, schema, table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Table table){
			return foreigns(false, table);
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String table){
			return foreigns(false, new Table(table));
		}
		default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String catalog, String schema, String table){
			return foreigns(false, new Table(catalog, schema, table));
		}
		ForeignKey foreign(boolean greedy, Table table, List<String> columns);
		default ForeignKey foreign(boolean greedy, Table table, String ... columns){
			return foreign(greedy, table, BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(boolean greedy, String table, String ... columns){
			return foreign(greedy, new Table(table), BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(boolean greedy, String table, List<String> columns){
			return foreign(greedy, new Table(table), columns);
		}
		//与上面的foreign(boolean greedy, String table, String ... columns)冲突
		//ForeignKey foreign(boolean greedy, String catalog, String schema, String table, String ... columns);
		default ForeignKey foreign(boolean greedy, String catalog, String schema, String table, List<String> columns){
			return foreign(greedy, new Table(catalog, schema, table), columns);
		}
		default ForeignKey foreign(Table table, String ... columns){
			return foreign(false, table, BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(Table table, List<String> columns){
			return foreign(false, table, columns);
		}
		default ForeignKey foreign(String table, String ... columns){
			return foreign(false, new Table(table), BeanUtil.array2list(columns));
		}
		default ForeignKey foreign(String table, List<String> columns){
			return foreign(false, new Table(table), columns);
		}
		//与上面的foreign(String table, String ... columns)冲突
		//ForeignKey foreign(String catalog, String schema, String table, String ... columns);
		default ForeignKey foreign(String catalog, String schema, String table, List<String> columns){
			return foreign(false, new Table(catalog, schema, table), columns);
		}
		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table);
		default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table){
			return indexs(greedy, new Table(table));
		}
		default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String catalog, String schema, String table){
			return indexs(greedy, new Table(catalog, schema, table));
		}
		default <T extends Index> LinkedHashMap<String, T> indexs(Table table){
			return indexs(false, table);
		}
		default <T extends Index> LinkedHashMap<String, T> indexs(String table){
			return indexs(false, new Table(table));
		}
		default <T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table){
			return indexs(false, new Table(catalog, schema, table));
		}

		Index index(boolean greedy, Table table, String name);
		default Index index(boolean greedy, String table, String name){
			return index(greedy, new Table(table), name);
		}
		default Index index(boolean greedy, String name){
			return index(greedy, (Table) null, name);
		}
		default Index index(Table table, String name){
			return index(false, table, name);
		}
		default Index index(String table, String name){
			return index(false, new Table(table), name);
		}
		default Index index(String name){
			return index(false, name);
		}


		/* *****************************************************************************************************************
		 * 													constraint
		 ******************************************************************************************************************/

		<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table);
		default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table){
			return constraints(greedy, new Table(table));
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String catalog, String schema, String table){
			return constraints(greedy, new Table(catalog, schema, table));
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table){
			return constraints(false, table);
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(String table){
			return constraints(false, table);
		}
		default <T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table){
			return constraints(false, new Table(catalog, schema, table));
		}
		Constraint constraint(boolean greedy, Table table, String name);
		default Constraint constraint(boolean greedy, String table, String name){
			return constraint(greedy, new Table(table), name);
		}
		default Constraint constraint(boolean greedy, String name){
			return constraint(greedy, (Table)null, name);
		}
		default Constraint constraint(Table table, String name){
			return constraint(false, table, name);
		}
		default Constraint constraint(String table, String name){
			return constraint(false, table, name);
		}
		default Constraint constraint(String name){
			return constraint(false, name);
		}

		/* *****************************************************************************************************************
		 * 													trigger
		 ******************************************************************************************************************/
		<T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events);
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String catalog, String schema, String table, List<Trigger.EVENT> events){
			return triggers(greedy, new Table(catalog, schema, table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String schema, String table, List<Trigger.EVENT> events){
			return triggers(greedy, new Table(schema, table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String table, List<Trigger.EVENT> events){
			return triggers(greedy, new Table(table), events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, List<Trigger.EVENT> events){
			return triggers(greedy, (Table) null, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy){
			return triggers(greedy,(Table) null, null);
		}
 		default <T extends Trigger> LinkedHashMap<String, T> triggers(String catalog, String schema, String table, List<Trigger.EVENT> events){
			return triggers(false, new Table(catalog, schema, table), events);
		}


		default <T extends Trigger> LinkedHashMap<String, T> triggers(String schema, String table, List<Trigger.EVENT> events){
			return triggers(false, schema, table, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(String table, List<Trigger.EVENT> events){
			return triggers(false, table, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(List<Trigger.EVENT> events){
			return triggers(false, events);
		}
		default <T extends Trigger> LinkedHashMap<String, T> triggers(){
			return triggers(false);
		}


		Trigger trigger(boolean greedy, String catalog, String schema, String name);
		default Trigger trigger(boolean greedy, String schema, String name){
			return trigger(greedy, null, schema, name);
		}
		default Trigger trigger(boolean greedy, String name){
			return trigger(greedy, null, null, name);
		}

		default Trigger trigger(String catalog, String schema, String name){
			return trigger(false, catalog, schema, name);
		}
		default Trigger trigger(String schema, String name){
			return trigger(false, null, schema, name);
		}
		default Trigger trigger(String name){
			return trigger(false, name);
		}

		/* *****************************************************************************************************************
		 * 													procedure
		 ******************************************************************************************************************/

		<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name);
		default <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String schema, String name){
			return procedures(greedy, null, schema, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String name){
			return procedures(greedy, null, null, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy){
			return procedures(greedy, null, null, null);
		}

		default <T extends Procedure> LinkedHashMap<String, T> procedures(String catalog, String schema, String name){
			return procedures(false, catalog, schema, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(String schema, String name){
			return procedures(false, null, schema, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(String name){
			return procedures(false, name);
		}
		default <T extends Procedure> LinkedHashMap<String, T> procedures(){
			return procedures(false);
		}


		Procedure procedure(boolean greedy, String catalog, String schema, String name);
		default Procedure procedure(boolean greedy, String schema, String name){
			return procedure(greedy, null, schema, name);
		}
		default Procedure procedure(boolean greedy, String name){
			return procedure(greedy, null, null, name);
		}
		default Procedure procedure(String catalog, String schema, String name){
			return procedure(false, catalog, schema, name);
		}
		default Procedure procedure(String schema, String name){
			return procedure(false, schema, name);
		}
		default Procedure procedure(String name){
			return procedure(false, name);
		}

		/* *****************************************************************************************************************
		 * 													function
		 ******************************************************************************************************************/

		<T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name);
		default <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String schema, String name){
			return functions(greedy, null, schema, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String name){
			return functions(greedy, null, null, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(boolean greedy){
			return functions(greedy, null, null, null);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(String catalog, String schema, String name){
			return functions(false, catalog, schema, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(String schema, String name){
			return functions(false, schema, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(String name){
			return functions(false, name);
		}
		default <T extends Function> LinkedHashMap<String, T> functions(){
			return functions(false);
		}


		Function function(boolean greedy, String catalog, String schema, String name);
		default Function function(boolean greedy, String schema, String name){
			return function(greedy, null, schema, name);
		}
		default Function function(boolean greedy, String name){
			return function(greedy, null, null, name);
		}
		default Function function(String catalog, String schema, String name){
			return function(false, catalog, schema, name);
		}
		default Function function(String schema, String name){
			return function(false, schema, name);
		}
		default Function function(String name){
			return function(false, name);
		}
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
		boolean rename(Table origin, String name) throws Exception;

		/* *****************************************************************************************************************
		 * 													view
		 ******************************************************************************************************************/

		boolean save(View view) throws Exception;
		boolean create(View view) throws Exception;
		boolean alter(View view) throws Exception;
		boolean drop(View view) throws Exception;
		boolean rename(View origin, String name) throws Exception;


		/* *****************************************************************************************************************
		 * 													master table
		 ******************************************************************************************************************/

		boolean save(MasterTable table) throws Exception;
		boolean create(MasterTable table) throws Exception;
		boolean alter(MasterTable table) throws Exception;
		boolean drop(MasterTable table) throws Exception;
		boolean rename(MasterTable origin, String name) throws Exception;


		/* *****************************************************************************************************************
		 * 													partition table
		 ******************************************************************************************************************/

		boolean save(PartitionTable table) throws Exception;
		boolean create(PartitionTable table) throws Exception;
		boolean alter(PartitionTable table) throws Exception;
		boolean drop(PartitionTable table) throws Exception;
		boolean rename(PartitionTable origin, String name) throws Exception;



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
		boolean rename(Column origin, String name) throws Exception;


		/* *****************************************************************************************************************
		 * 													tag
		 ******************************************************************************************************************/

		boolean save(Tag tag) throws Exception;
		boolean add(Tag tag) throws Exception;
		boolean alter(Tag tag) throws Exception;
		boolean drop(Tag tag) throws Exception;
		boolean rename(Tag origin, String name) throws Exception;


		/* *****************************************************************************************************************
		 * 													primary
		 ******************************************************************************************************************/

		boolean add(PrimaryKey primary) throws Exception;
		boolean alter(PrimaryKey primary) throws Exception;
		boolean drop(PrimaryKey primary) throws Exception;
		boolean rename(PrimaryKey origin, String name) throws Exception;
		/* *****************************************************************************************************************
		 * 													foreign
		 ******************************************************************************************************************/

		boolean add(ForeignKey foreign) throws Exception;
		boolean alter(ForeignKey foreign) throws Exception;
		boolean drop(ForeignKey foreign) throws Exception;
		boolean rename(ForeignKey origin, String name) throws Exception;


		/* *****************************************************************************************************************
		 * 													index
		 ******************************************************************************************************************/

		boolean add(Index index) throws Exception;
		boolean alter(Index index) throws Exception;
		boolean drop(Index index) throws Exception;
		boolean rename(Index origin, String name) throws Exception;


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
		boolean rename(Constraint origin, String name) throws Exception;

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
		boolean rename(Trigger origin, String name) throws Exception;

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
		boolean rename(Procedure origin, String name) throws Exception;

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
		boolean rename(Function origin, String name) throws Exception;
	}

}
