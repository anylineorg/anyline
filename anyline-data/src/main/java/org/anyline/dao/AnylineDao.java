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


package org.anyline.dao;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.GraphTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineDao<E>{
	void setRuntime(DataRuntime runtime);
	DataRuntime runtime();


	/**
	 * 根据差异生成SQL
	 * @param differ differ
	 * @return sqls
	 */
	List<Run> ddls(DataRuntime runtime, MetadataDiffer differ);
	default List<Run> ddls(MetadataDiffer differ){
		return ddls(runtime(), differ);
	}
	/**
	 * 根据差异生成SQL
	 * @param differs differs
	 * @return sqls
	 */
	List<Run> ddls(DataRuntime runtime, List<MetadataDiffer> differs);
	default List<Run> ddls(List<MetadataDiffer> differs){
		return ddls(runtime(), differs);
	}
	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 查询row列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return querys(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 查询entity列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	<T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions);
	default <T> EntitySet<T> selects(RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions){
		return selects(runtime(), null, prepare, clazz, configs, conditions);
	}

	/**
	 * 查询map列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default List<Map<String, Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return maps(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 合计总行数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return long
	 */
	long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default long count(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return count(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 创建查询序列SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 可以是多个序列
	 * @return DataRow
	 */
	DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names);
	default DataRow sequence(boolean next, String ... names){
		return sequence(runtime(), null, next, names);
	}

	/**
	 * 是否存在
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return boolean
	 */
	boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return exists(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 更新记录
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data		需要更新的数据
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */

	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime(), null, batch, dest, data, configs, columns);
	}
	default long update(int batch, String dest, Object data, ConfigStore configs, String ... columns){
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, ConfigStore configs, String ... columns){
		return update(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, ConfigStore configs, List<String> columns){
		return update(batch, (String)null, data, configs, columns);
	}
	default long update(int batch, String dest, Object data, String ... columns){
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, String dest, Object data, List<String> columns){
		return update(batch, dest, data, null, columns);
	}
	default long update(int batch, Object data, List<String> columns){
		return update(batch, (String)null, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime, random, 0, dest, data, configs, columns);
	}
	default long update(String dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime(), null, dest, data, configs, columns);
	}
	default long update(String dest, Object data, ConfigStore configs, String ... columns){
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, String ... columns){
		return update(null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, List<String> columns){
		return update(null, data, configs, columns);
	}
	default long update(String dest, Object data, String ... columns){
		return update(dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(String dest, Object data, List<String> columns){
		return update(dest, data, null, columns);
	}
	default long update(Object data, List<String> columns){
		return update(null, data, null, columns);
	}
	long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long update(int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime(), null, batch, dest, data, configs, columns);
	}
	default long update(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Table dest, Object data, String ... columns){
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, Table dest, Object data, List<String> columns){
		return update(batch, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime, random, 0, dest, data, configs, columns);
	}

	/** 
	 * 添加
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data 需要插入的数据
	 * @param columns  需要插入的列 
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return int
	 */
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns){
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(int batch, String dest, Object data, List<String> columns){
		return insert(runtime(), null, batch, dest, data, columns);
	}
	default long insert(int batch, String dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime(), null, batch, dest, data, configs, columns);
	}
	default long insert(int batch, String dest, Object data, String ... columns){
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, String dest, Object data, ConfigStore configs, String ... columns){
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, String ... columns){
		return insert(batch, (String)null, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, ConfigStore configs, String ... columns){
		return insert(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, List<String> columns){
		return insert(batch, (String)null, data, columns);
	}
	default long insert(int batch, Object data, ConfigStore configs, List<String> columns){
		return insert(batch, (String)null, data, configs, columns);
	}

	default long insert(DataRuntime runtime, String random, String dest, Object data, List<String> columns){
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime, random, 0, dest, data, configs, columns);
	}
	default long insert(String dest, Object data, List<String> columns){
		return insert(runtime(), null, 0, dest, data, columns);
	}
	default long insert(String dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime(), null, 0, dest, data, configs, columns);
	}
	default long insert(String dest, Object data, String ... columns){
		return insert(0, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, ConfigStore configs, String ... columns){
		return insert(0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Object data, String ... columns){
		return insert(0, (String)null, data, BeanUtil.array2list(columns));
	}
	default long insert( Object data, ConfigStore configs, String ... columns){
		return insert(0, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Object data, List<String> columns){
		return insert(0, (String)null, data, columns);
	}
	default long insert(Object data, ConfigStore configs, List<String> columns){
		return insert(0, (String)null, data, configs, columns);
	}

	long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns){
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(int batch, Table dest, Object data, List<String> columns){
		return insert(runtime(), null, batch, dest, data, columns);
	}
	default long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime(), null, batch, dest, data, configs, columns);
	}
	default long insert(int batch, Table dest, Object data, String ... columns){
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, List<String> columns){
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime, random, 0, dest, data, configs, columns);
	}
	default long insert(Table dest, Object data, List<String> columns){
		return insert(runtime(), null, 0, dest, data, columns);
	}
	default long insert(Table dest, Object data, ConfigStore configs, List<String> columns){
		return insert(runtime(), null, 0, dest, data, configs, columns);
	}
	default long insert(Table dest, Object data, String ... columns){
		return insert(0, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(Table dest, Object data, ConfigStore configs, String ... columns){
		return insert(0, dest, data, configs, BeanUtil.array2list(columns));
	}

	/** 
	 * 保存(insert|update)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data  data
	 * @param columns  columns
	 * @return int
	 */
	long save(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String>  columns);
	default long save(DataRuntime runtime, String random, int batch, String dest, Object data, List<String>  columns){
		return save(runtime, random, batch, dest, data, null, columns);
	}
	default long save(int batch, String dest, Object data, List<String>  columns){
		return save(runtime(), null, batch, dest, data, columns);
	}
	default long save(int batch, String dest, Object data, ConfigStore configs, List<String>  columns){
		return save(runtime(), null, batch, dest, data, configs, columns);
	}
	default long save(int batch, String dest, Object data, String ... columns){
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, String dest, Object data, ConfigStore configs, String ... columns){
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(int batch, Object data, String ... columns){
		return save(batch, (String)null, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, Object data, ConfigStore configs, String ... columns){
		return save(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, List<String>  columns){
		return save(runtime, random, 0, dest, data, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String>  columns){
		return save(runtime, random, 0, dest, data, configs, columns);
	}
	default long save(String dest, Object data, List<String>  columns){
		return save(runtime(), null, 0, dest, data, columns);
	}
	default long save(String dest, Object data, ConfigStore confnigs, List<String>  columns){
		return save(runtime(), null, 0, dest, data, confnigs, columns);
	}
	default long save(String dest, Object data, String ... columns){
		return save(0, dest, data, BeanUtil.array2list(columns));
	}
	default long save(String dest, Object data, ConfigStore configs, String ... columns){
		return save(0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(Object data, String ... columns){
		return save(0, (String)null, data, BeanUtil.array2list(columns));
	}
	default long save(Object data, ConfigStore configs, String ... columns){
		return save(0, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	long save(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String>  columns);
	default long save(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String>  columns){
		return save(runtime, random, batch, dest, data, null, columns);
	}
	default long save(int batch, Table dest, Object data, List<String>  columns){
		return save(runtime(), null, batch, dest, data, columns);
	}
	default long save(int batch, Table dest, Object data, ConfigStore configs, List<String>  columns){
		return save(runtime(), null, batch, dest, data, configs, columns);
	}
	default long save(int batch, Table dest, Object data, String ... columns){
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, Table dest, Object data, List<String>  columns){
		return save(runtime, random, 0, dest, data, columns);
	}
	default long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String>  columns){
		return save(runtime, random, 0, dest, data, configs, columns);
	}
	default long save(Table dest, Object data, List<String>  columns){
		return save(runtime(), null, 0, dest, data, columns);
	}
	default long save(Table dest, Object data, ConfigStore confnigs, List<String>  columns){
		return save(runtime(), null, 0, dest, data, confnigs, columns);
	}
	default long save(Table dest, Object data, String ... columns){
		return save(0, dest, data, BeanUtil.array2list(columns));
	}
	default long save(Table dest, Object data, ConfigStore configs, String ... columns){
		return save(0, dest, data, configs, BeanUtil.array2list(columns));
	}

	/**
	 * 执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return 影响行数
	 */
	long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default long execute(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return execute(runtime(), null, prepare, configs, conditions);
	}
	default long execute(RunPrepare prepare, String ... conditions){
		return execute(prepare, null, conditions);
	}

	long execute(DataRuntime runtime, String random, int batch, RunPrepare prepare, Collection<Object> values);
	default long execute(int batch, RunPrepare prepare, Collection<Object> values){
		return execute(runtime(), null, batch, prepare, values);
	}

	/** 
	 * 执行存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure  存储过程
	 * @return boolean
	 */
	boolean execute(DataRuntime runtime, String random, Procedure procedure);
	default boolean execute(Procedure procedure){
		return execute(runtime(), null, procedure);
	}

	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);
	default DataSet querys(Procedure procedure, PageNavi navi){
		return querys(runtime(), null, procedure, navi);
	}
	default long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String ... columns){
		return delete(runtime, random, DataSourceUtil.parseDest(dest, null, configs), configs, obj, columns);
	}
	default long delete(DataRuntime runtime, String random, String dest, Object obj, String ... columns){
		return delete(runtime, random, dest, null, obj, columns);
	}
	default long delete(String dest, Object obj, String ... columns){
		return delete(runtime(), null, dest, obj, columns);
	}
	default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String ... conditions){
		return delete(runtime, random, new Table(table), configs, conditions);
	}
	default long delete(String table, ConfigStore configs, String ... conditions){
		return delete(runtime(), null, table, configs, conditions);
	}
	long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String ... columns);
	default long delete(DataRuntime runtime, String random, Table dest, Object obj, String ... columns){
		return delete(runtime, random, dest, null, obj, columns);
	}
	default long delete(Table dest, Object obj, String ... columns){
		return delete(runtime(), null, dest, obj, columns);
	}
	long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String ... conditions);
	default long delete(Table table, ConfigStore configs, String ... conditions){
		return delete(runtime(), null, table, configs, conditions);
	}

	/**
	 * 删除多行
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String key, Collection<T> values){
		return deletes(runtime, random, batch, new Table(table), key, values);
	}
	default <T> long deletes(int batch, String table, String key, Collection<T> values){
		return deletes(runtime(), null, batch, table, key, values);
	}
	default <T> long deletes(int batch, String table, String key, T ... values){
		return deletes(batch, table, key, BeanUtil.array2list(values));
	}
	<T> long deletes(DataRuntime runtime, String random, int batch, Table table, String key, Collection<T> values);
	default <T> long deletes(int batch, Table table, String key, Collection<T> values){
		return deletes(runtime(), null, batch, table, key, values);
	}
	default <T> long deletes(int batch, Table table, String key, T ... values){
		return deletes(batch, table, key, BeanUtil.array2list(values));
	}
	long truncate(DataRuntime runtime, String random, Table table);
	default long truncate(DataRuntime runtime, String random, String table){
		return truncate(runtime, random, new Table(table));
	}
	default long truncate(String table){
		return truncate(runtime(), null, table);
	}
	default long truncate(Table table){
		return truncate(runtime(), null, table);
	}

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

	/**
	 * 根据sql获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param prepare RunPrepare
	 * @param comment 是否需要注释
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Column> metadata(RunPrepare prepare, boolean comment);
	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	/**
	 * 当前数据源 数据库类型
	 * @return DatabaseType
	 */
	DatabaseType type();

	/**
	 * 当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String version(DataRuntime runtime, String random);
	default String version(){
		return version(runtime(), null);
	}

	/**
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String product(DataRuntime runtime, String random);
	default String product(){
		return product(runtime(), null);
	}
	Database database(DataRuntime runtime, String random);
	default Database database(){
		return database(runtime(), null);
	}
	LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name);
	default LinkedHashMap<String, Database> databases(String name){
		return databases(runtime(), null, name);
	}


	List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name);
	default List<Database> databases(boolean greedy, String name){
		return databases(runtime(), null, greedy, name);
	}
	Database database(DataRuntime runtime, String random, String name);
	default Database database(String name){
		return database(runtime(), null, name);
	}

	/* *****************************************************************************************************************
	 * 													catalog
	 ******************************************************************************************************************/
	Catalog catalog(DataRuntime runtime, String random);
	default Catalog catalog(){
		return catalog(runtime(), null);
	}
	LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name);
	default LinkedHashMap<String, Catalog> catalogs(String name){
		return catalogs(runtime(), null, name);
	}
	List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name);
	default List<Catalog> catalogs(boolean greedy, String name){
		return catalogs(runtime(), null, greedy, name);
	}

	/* *****************************************************************************************************************
	 * 													schema
	 ******************************************************************************************************************/
	Schema schema(DataRuntime runtime, String random);
	default Schema schema(){
		return schema(runtime(), null);
	}
	LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name);
	default LinkedHashMap<String, Schema> schemas(Catalog catalog, String name){
		return schemas(runtime(), null, catalog, name);
	}

	List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name);
	default List<Schema> schemas(boolean greedy, Catalog catalog, String name){
		return schemas(runtime(), null, greedy, catalog, name);
	}


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	<T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return tables(runtime, random, greedy, catalog, schema, name, types, config);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return tables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		return tables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, int struct){
		return tables(greedy, null, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, boolean struct){
		return tables(greedy, null, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types, int struct){
		return tables(greedy, null, null, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types, boolean struct){
		return tables(greedy, null, null, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types, int struct){
		return tables(greedy, null, null, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types, boolean struct){
		return tables(greedy, null, null, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, boolean struct){
		return tables(greedy, null, null, null, 1, struct);
	}
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return tables(runtime, random, catalog, schema, name, types, config);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct){
		return tables(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct){
		return tables(runtime(), null, catalog, schema, name, types, struct);
	}

	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, int struct){
		return tables( null, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, boolean struct){
		return tables( null, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, int struct){
		return tables( null, null, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, boolean struct){
		return tables( null, null, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct){
		return tables( null, null, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct){
		return tables( null, null, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(){
		return tables( null, null, null, 1, false);
	}


	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return tables(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return tables(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types){
		return tables(greedy, null, schema, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types){
		return tables(greedy, null, null, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types){
		return tables(greedy, null, null, types);
	}
	default <T extends Table> List<T> tables(boolean greedy){
		return tables(greedy, null, null, null, 1);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types){
		return tables(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types){
		return tables(runtime(), null, catalog, schema, name, types);
	}

	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types){
		return tables( null, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types){
		return tables( null, null, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types){
		return tables( null, null, types);
	}

	/**
	 *
	 * 查询表的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, Table table, boolean init);
	default List<String> ddl(Table table, boolean init){
		return ddl(runtime(), null, table, init);
	}


	/* *****************************************************************************************************************
	 * 													VertexTable
	 ******************************************************************************************************************/
	<T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return vertexTables(runtime, random, greedy, catalog, schema, name, types, config);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return vertexTables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		return vertexTables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types, int struct){
		return vertexTables(greedy, null, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types, boolean struct){
		return vertexTables(greedy, null, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types, int struct){
		return vertexTables(greedy, null, null, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types, boolean struct){
		return vertexTables(greedy, null, null, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types, int struct){
		return vertexTables(greedy, null, null, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types, boolean struct){
		return vertexTables(greedy, null, null, types, struct);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, boolean struct){
		return vertexTables(greedy, null, null, null, 1, struct);
	}
	<T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return vertexTables(runtime, random, catalog, schema, name, types, config);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, int struct){
		return vertexTables(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, boolean struct){
		return vertexTables(runtime(), null, catalog, schema, name, types, struct);
	}

	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types, int struct){
		return vertexTables( null, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types, boolean struct){
		return vertexTables( null, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types, int struct){
		return vertexTables( null, null, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types, boolean struct){
		return vertexTables( null, null, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, int struct){
		return vertexTables( null, null, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types, boolean struct){
		return vertexTables( null, null, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(){
		return vertexTables( null, null, null, 1, false);
	}


	default <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return vertexTables(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return vertexTables(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, Schema schema, String name, int types){
		return vertexTables(greedy, null, schema, name, types);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, String name, int types){
		return vertexTables(greedy, null, null, name, types);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy, int types){
		return vertexTables(greedy, null, null, types);
	}
	default <T extends VertexTable> List<T> vertexTables(boolean greedy){
		return vertexTables(greedy, null, null, null, 1);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types){
		return vertexTables(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types){
		return vertexTables(runtime(), null, catalog, schema, name, types);
	}

	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(Schema schema, String name, int types){
		return vertexTables( null, schema, name, types);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(String name, int types){
		return vertexTables( null, null, name, types);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexTables(int types){
		return vertexTables( null, null, types);
	}

	/**
	 *
	 * 查询点类型的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 点类型
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, VertexTable meta, boolean init);
	default List<String> ddl(VertexTable meta, boolean init){
		return ddl(runtime(), null, meta, init);
	}


	/* *****************************************************************************************************************
	 * 													EdgeTable
	 ******************************************************************************************************************/
	<T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return edgeTables(runtime, random, greedy, catalog, schema, name, types, config);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return edgeTables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct){
		return edgeTables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types, int struct){
		return edgeTables(greedy, null, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types, boolean struct){
		return edgeTables(greedy, null, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types, int struct){
		return edgeTables(greedy, null, null, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types, boolean struct){
		return edgeTables(greedy, null, null, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types, int struct){
		return edgeTables(greedy, null, null, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types, boolean struct){
		return edgeTables(greedy, null, null, types, struct);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, boolean struct){
		return edgeTables(greedy, null, null, null, 1, struct);
	}
	<T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct);
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct){
		int config = 0;
		if(struct){
			config = 255;
		}
		return edgeTables(runtime, random, catalog, schema, name, types, config);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, int struct){
		return edgeTables(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, boolean struct){
		return edgeTables(runtime(), null, catalog, schema, name, types, struct);
	}

	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types, int struct){
		return edgeTables( null, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types, boolean struct){
		return edgeTables( null, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types, int struct){
		return edgeTables( null, null, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types, boolean struct){
		return edgeTables( null, null, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, int struct){
		return edgeTables( null, null, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types, boolean struct){
		return edgeTables( null, null, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(){
		return edgeTables( null, null, null, 1, false);
	}


	default <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return edgeTables(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return edgeTables(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, Schema schema, String name, int types){
		return edgeTables(greedy, null, schema, name, types);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, String name, int types){
		return edgeTables(greedy, null, null, name, types);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy, int types){
		return edgeTables(greedy, null, null, types);
	}
	default <T extends EdgeTable> List<T> edgeTables(boolean greedy){
		return edgeTables(greedy, null, null, null, 1);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types){
		return edgeTables(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types){
		return edgeTables(runtime(), null, catalog, schema, name, types);
	}

	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(Schema schema, String name, int types){
		return edgeTables( null, schema, name, types);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(String name, int types){
		return edgeTables( null, null, name, types);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(int types){
		return edgeTables( null, null, types);
	}

	/**
	 *
	 * 查询边类型的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 边类型
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init);
	default List<String> ddl(EdgeTable meta, boolean init){
		return ddl(runtime(), null, meta, init);
	}

	/* *****************************************************************************************************************
	 * 													views
	 ******************************************************************************************************************/

	/**
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog  catalog
	 * @param schema schema
	 * @param pattern 表名
	 * @param types VIEW.TYPE
	 * @return LinkedHashMap
	 * @param <T> entity.class
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types);
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return views(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, Schema schema, String name, int types){
		return views(greedy, null, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, int types){
		return views(greedy, null, null, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, int types){
		return views(greedy, null, null, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy){
		return views(greedy, null, null, null, View.TYPE.NORMAL.value);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types){
		return views(false, catalog, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types){
		return views(false, null, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(String name, int types){
		return views(false, null, null, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(int types){
		return views(false, null, null, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(){
		return views(false, null, null, null, View.TYPE.NORMAL.value);
	}
	List<String> ddl(DataRuntime runtime, String random, View view);
	default List<String> ddl(View view){
		return ddl(runtime(), null, view);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	<T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types);
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types){
		return masterTables(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(boolean greedy, Schema schema, String name, int types){
		return masterTables(greedy, null, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(boolean greedy, String name, int types){
		return masterTables(greedy, null, null, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(boolean greedy, int types){
		return masterTables(greedy, null, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(boolean greedy){
		return masterTables(greedy, MasterTable.TYPE.NORMAL.value);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types){
		return masterTables(false, catalog, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(Schema schema, String name, int types){
		return masterTables(false, null, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(String name, int types){
		return masterTables(false, null, null, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(int types){
		return masterTables(false, null, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masterTables(){
		return masterTables(false, MasterTable.TYPE.NORMAL.value);
	}
	List<String> ddl(DataRuntime runtime, String random, MasterTable table);
	default List<String> ddl(MasterTable table){
		return ddl(runtime(), null, table);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	<T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String name);

	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, Catalog catalog, Schema schema, String master, String name){
		return partitionTables(greedy, new MasterTable(catalog, schema, master), null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, Schema schema, String master, String name){
		return partitionTables(greedy, null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, String master, String name){
		return partitionTables(greedy, null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, String master){
		return partitionTables(greedy, null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master){
		return partitionTables(greedy, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String, Object> tags, String name){
		return partitionTables(runtime(), null, greedy, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String, Object> tags){
		return partitionTables(runtime(), null, greedy, master, tags, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Catalog catalog, Schema schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return partitionTables(false, mtable, null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Schema schema, String master, String name){
		return partitionTables(false, null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master, String name){
		return partitionTables(false, null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master){
		return partitionTables(false, null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master){
		return partitionTables(false, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String, Object> tags, String name){
		return partitionTables(false, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String, Object> tags){
		return partitionTables(false, master, tags, null);
	}
	List<String> ddl(DataRuntime runtime, String random, PartitionTable table);
	default List<String> ddl(PartitionTable table){
		return ddl(runtime(), null, table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测列的主键标识
	 * @return LinkedHashMap
	 * @param <T> Column
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table){
		return columns(runtime(), null, greedy, table, ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table){
		return columns(greedy, new Table(table));
	}

	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table){
		return columns(greedy, new Table(catalog, schema, table));
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Table table){
		return columns(false, table);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(String table){
		return columns(false, table);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table){
		return columns(new Table(catalog, schema, table));
	}

	<T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema);

	default <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema){
		return columns(runtime(), null, greedy, catalog, schema);
	}
	default <T extends Column> List<T> columns(Catalog catalog, Schema schema){
		return columns(false, catalog, schema);
	}
	default <T extends Column> List<T> columns(boolean greedy){
		return columns(greedy, null, null);
	}
	default <T extends Column> List<T> columns(){
		return columns(false, null, null);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table);
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table){
		return tags(runtime(), null, greedy, new Table(table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Catalog catalog, Schema schema, String table){
		return tags(runtime(), null, greedy, new Table(catalog, schema, table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table){
		return tags(runtime(), null, greedy, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(Table table){
		return tags(runtime(), null, false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(String table){
		return tags(false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(Catalog catalog, Schema schema, String table){
		return tags(false, catalog, schema, table);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table);
	default PrimaryKey primary(boolean greedy, Table table){
		return primary(runtime(), null, greedy, table);
	}
	default PrimaryKey primary(boolean greedy, String table){
		return primary(greedy, new Table(table));
	}
	default PrimaryKey primary(boolean greedy, Catalog catalog, Schema schema, String table){
		return primary(greedy, new Table(catalog, schema, table));
	}
	default PrimaryKey primary(Table table){
		return primary(false, table);
	}
	default PrimaryKey primary(String table){
		return primary(false, table);
	}
	default PrimaryKey primary(Catalog catalog, Schema schema, String table){
		return primary(false, catalog, schema, table);
	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table){
		return foreigns(runtime(), null, greedy, table);
	}
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	<T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String name);
	default <T extends Index> List<T> indexs(boolean greedy, Table table, String name){
		return indexs(runtime(), null, greedy, table, name);
	}
	default <T extends Index> List<T> indexs(boolean greedy, String table, String name){
		return indexs(greedy, new Table(table), name);
	}
	default <T extends Index> List<T> indexs(boolean greedy, Table table){
		return indexs(greedy, table, null);
	}
	default <T extends Index> List<T> indexs(boolean greedy, String table){
		return indexs(greedy, new Table(table), null);
	}
	default <T extends Index> List<T> indexs(boolean greedy, Catalog catalog, Schema schema, String table){
		return indexs(greedy, new Table(catalog, schema, table), null);
	}


	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, Table table, String name);
	default <T extends Index> LinkedHashMap<String, T> indexs(Table table, String name){
		return indexs(runtime(), null, table, name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(String table, String name){
		return indexs(new Table(table), name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(Table table){
		return indexs(table, null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(String table){
		return indexs(new Table(table), null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(Catalog catalog, Schema schema, String table){
		return indexs(new Table(catalog, schema, table), null);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	<T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String name);
	default <T extends Constraint> List<T> constraints(boolean greedy, Table table, String name){
		return constraints(runtime(), null, greedy, table, name);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, String table, String name){
		return constraints(greedy, new Table(table), name);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, Table table){
		return constraints(greedy, table, null);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, String table){
		return constraints(greedy, new Table(table), null);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, Catalog catalog, Schema schema, String table){
		return constraints(greedy, new Table(catalog, schema, table), null);
	}
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String name);
	default <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, String name){
		return constraints(runtime, random, table, null, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name){
		return constraints(runtime(), null, table, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Column column, String name){
		return constraints(runtime(), null, column.getTable(), column, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T>  constraints(String table, String name){
		return constraints(new Table(table), name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table){
		return constraints(table, null);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(String table){
		return constraints(new Table(table));
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Catalog catalog, Schema schema, String table){
		return constraints(new Table(catalog, schema, table));
	}



	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events);
	default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events){
		return triggers(runtime(), null, greedy, table, events);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	<T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Procedure> List<T> procedures(boolean greedy, Catalog catalog, Schema schema, String name){
		return procedures(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name){
		return procedures(runtime(), null, catalog, schema, name);
	}
	List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	default List<String> ddl(Procedure procedure){
		return ddl(runtime(), null, procedure);
	}

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	<T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Function> List<T> functions(boolean greedy, Catalog catalog, Schema schema, String name){
		return functions(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name){
		return functions(runtime(), null, catalog, schema, name);
	}


	List<String> ddl(DataRuntime runtime, String random, Function function);
	default List<String> ddl(Function function){
		return ddl(runtime(), null, function);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 ******************************************************************************************************************/

	<T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Sequence> List<T> sequences(boolean greedy, Catalog catalog, Schema schema, String name){
		return sequences(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Sequence> LinkedHashMap<String, T> sequences(Catalog catalog, Schema schema, String name){
		return sequences(runtime(), null, catalog, schema, name);
	}

	List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	default List<String> ddl(Sequence sequence){
		return ddl(runtime(), null, sequence);
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

	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	boolean create(Table table) throws Exception;
	boolean alter(Table table) throws Exception;
	boolean drop(Table table) throws Exception;
	boolean rename(Table origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/
	boolean create(View view) throws Exception;
	boolean alter(View view) throws Exception;
	boolean drop(View view) throws Exception;
	boolean rename(View origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	boolean create(MasterTable table) throws Exception;
	boolean alter(MasterTable table) throws Exception;
	boolean drop(MasterTable table) throws Exception;
	boolean rename(MasterTable origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	boolean create(PartitionTable table) throws Exception;
	boolean alter(PartitionTable table) throws Exception;
	boolean drop(PartitionTable table) throws Exception;
	boolean rename(PartitionTable origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/
	boolean add(Column column) throws Exception;
	boolean alter(Table table, Column column) throws Exception;
	boolean alter(Column column) throws Exception;
	boolean drop(Column column) throws Exception;
	boolean rename(Column origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	boolean add(Tag tag) throws Exception;
	boolean alter(Table table, Tag tag) throws Exception;
	boolean alter(Tag tag) throws Exception;
	boolean drop(Tag tag) throws Exception;
	boolean rename(Tag origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	boolean add(PrimaryKey primary) throws Exception;
	boolean alter(PrimaryKey primary) throws Exception;
	boolean alter(Table table, PrimaryKey primary) throws Exception;
	boolean drop(PrimaryKey primary) throws Exception;
	boolean rename(PrimaryKey origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/
	boolean add(ForeignKey foreign) throws Exception;
	boolean alter(ForeignKey foreign) throws Exception;
	boolean alter(Table table, ForeignKey foreign) throws Exception;
	boolean drop(ForeignKey foreign) throws Exception;
	boolean rename(ForeignKey origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	boolean add(Index index) throws Exception;
	boolean alter(Index index) throws Exception;
	boolean alter(Table table, Index index) throws Exception;
	boolean drop(Index index) throws Exception;
	boolean rename(Index origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	boolean add(Constraint constraint) throws Exception;
	boolean alter(Constraint constraint) throws Exception;
	boolean alter(Table table, Constraint constraint) throws Exception;
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
	boolean add(Trigger trigger) throws Exception;
	boolean alter(Trigger trigger) throws Exception;
	boolean drop(Trigger trigger) throws Exception;
	boolean rename(Trigger origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	/**
	 * 触发器
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
	/* *****************************************************************************************************************
	 * 													sequence
	 ******************************************************************************************************************/
	/**
	 * 函数
	 * @param sequence 序列
	 * @return boolean
	 * @throws Exception 异常 Exception
	 */
	boolean create(Sequence sequence) throws Exception;
	boolean alter(Sequence sequence) throws Exception;
	boolean drop(Sequence sequence) throws Exception;
	boolean rename(Sequence origin, String name) throws Exception;
} 
